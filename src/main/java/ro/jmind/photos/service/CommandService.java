package ro.jmind.photos.service;

import com.google.photos.types.proto.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.jmind.photos.model.ApplicationBusinessException;
import ro.jmind.photos.model.AuditResult;
import ro.jmind.photos.model.UploadDetail;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    @Value("${parentDataLocation}")
    private String parentDataLocation;
    @Value("${dataToUploadLocation}")
    private String dataToUploadLocation;
    @Value("${uploadDataLocation}")
    private String uploadDataLocation;
    @Value("${createMediaLocation}")
    private String createMediaLocation;
    @Value("${missingAlbumDataLocation}")
    private String missingAlbumDataLocation;
    @Value("${auditDataLocation}")
    private String auditDataLocation;


    private FileService fileService;
    private DataService dataService;
    private GoogleService googleService;


    public CommandService(FileService fileService, DataService dataService, GoogleService googleService) {
        this.fileService = fileService;
        this.dataService = dataService;
        this.googleService = googleService;
    }

    public void prepareDataToUpload() {
        File baseDir = new File(parentDataLocation);
        Map<String, List<UploadDetail>> albumNameUploadDetailsMap = dataService.collectLocalData(baseDir);
        Iterator<Map.Entry<String, List<UploadDetail>>> it = albumNameUploadDetailsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<UploadDetail>> record = it.next();
            String directoryName = record.getKey();
            List<UploadDetail> uploadDetails = record.getValue();
            if (uploadDetails.size() > 0) {
                File file = new File(dataToUploadLocation + "/" + directoryName + ".json");
                fileService.writeJson(file, uploadDetails);
            } else {
                logger.info("directory {} without data", directoryName);
            }
        }
    }

    public void uploadData() {
        File uploadSourceDataSourceDirectory = new File(uploadDataLocation);

        File errorDirectory = fileService.createDirectory(uploadSourceDataSourceDirectory, "/_error");
        File successDirectory = fileService.createDirectory(uploadSourceDataSourceDirectory, "/_success");

        logger.info("obtaining uploadDetails from json files");
        Map<String, List<UploadDetail>> stringListMap = dataService.deserializeUploadDetailsFilesFromDirectory(uploadSourceDataSourceDirectory);

        Set<Map.Entry<String, List<UploadDetail>>> entries = stringListMap.entrySet();
        int itemsToUploadCount = entries.stream()
                .map(Map.Entry::getValue)
                .mapToInt(List::size)
                .sum();

        logger.info("obtained {} json files, containing {} media files to upload", entries.size(), itemsToUploadCount);

        entries.stream().forEach(stringListEntry -> {
            String albumName = stringListEntry.getKey();
            List<UploadDetail> uploadDetails = stringListEntry.getValue();
            if (uploadDetails != null) {
                googleService.uploadFilesFromUploadDetails(uploadDetails);
                long successTokensCount = uploadDetails.stream()
                        .filter(uploadDetail -> uploadDetail.getTokenUpload() != null && !uploadDetail.getTokenUpload().isEmpty())
                        .count();
                //overwrite the file
                File file = new File(uploadSourceDataSourceDirectory + "/" + albumName + ".json");
                fileService.writeJson(file, uploadDetails);
                if (successTokensCount == uploadDetails.size()) {
                    fileService.moveFile(successDirectory, file);
                } else {
                    fileService.moveFile(errorDirectory, file);
                }
            }
        });
    }

    public void createMediaToAlbums() {
        File directory = new File(createMediaLocation);
        File errorDirectory = fileService.createDirectory(directory, "/_error");
        File successDirectory = fileService.createDirectory(directory, "/_success");

        Map<String, List<UploadDetail>> stringListMap = dataService.deserializeUploadDetailsFilesFromDirectory(directory);
        stringListMap.forEach((s, uploadDetails) -> {

            File jsonFile = new File(directory.getAbsolutePath() + "/" + s + ".json");

            try {
                logger.info("processing album {} containing {} items", s, uploadDetails.size());
                googleService.addMediaInAlbums(uploadDetails);
                //move file to success
                fileService.writeJson(jsonFile, uploadDetails);
                fileService.moveFile(successDirectory, jsonFile);
            } catch (ApplicationBusinessException e) {
                logger.info("fail processing album {}", s, e);
                fileService.writeJson(jsonFile, uploadDetails);
                fileService.moveFile(errorDirectory, jsonFile);
            }
        });
        System.out.println("done");
    }


    public void populateMissingAlbumData() {
        File directory = new File(missingAlbumDataLocation);
        File errorDirectory = fileService.createDirectory(directory, "/_error");
        File successDirectory = fileService.createDirectory(directory, "/_success");

        Map<String, List<UploadDetail>> stringListMap = dataService.deserializeUploadDetailsFilesFromDirectory(directory);

        if (stringListMap.isEmpty()) {
            logger.info("no data to update");
            return;
        }

        List<Album> remoteAlbums = null;
        try {
            remoteAlbums = googleService.getRemoteAlbums();
        } catch (Exception e) {
            logger.info("cannot get remote albums", e);
            //move all files to error
            stringListMap.forEach((key, list) -> {
                File json = new File(directory.getAbsolutePath() + "/" + key + ".json");
                fileService.writeJson(json, list);
                fileService.moveFile(errorDirectory, json);
            });
        }
        Iterator<Map.Entry<String, List<UploadDetail>>> iterator = stringListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<UploadDetail>> next = iterator.next();
            try {
                dataService.populateMissingAlbumData(next.getValue(), remoteAlbums);
                File json = new File(directory.getAbsolutePath() + "/" + next.getKey() + ".json");
                fileService.writeJson(json, next.getValue());
                fileService.moveFile(successDirectory, json);
            } catch (Exception e) {
                logger.info("unable to add missing album {} data", next.getKey(), e);
                File json = new File(directory.getAbsolutePath() + "/" + next.getKey() + ".json");
                fileService.writeJson(json, next.getValue());
                fileService.moveFile(errorDirectory, json);

            }
        }
    }

    public int calculateMediaSize() {
        File baseDir = new File(parentDataLocation);
        Map<String, List<UploadDetail>> albumNameUploadDetailsMap = dataService.collectLocalData(baseDir);
        Iterator<Map.Entry<String, List<UploadDetail>>> it = albumNameUploadDetailsMap.entrySet().iterator();

        long startTime = System.currentTimeMillis();
        long sum = albumNameUploadDetailsMap.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .mapToLong(uploadDetails -> {
                    return uploadDetails.stream().mapToLong(uploadDetail -> {
                        return new File(uploadDetail.getFileLocation()).length();
//                        return 10;
                    }).sum();
                })
                .sum();
        double totalTime = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 10)) / 10;
        logger.info("accessing files length took {} seconds", totalTime);

        return (int) (sum / 1024 / 1024);
    }

    public void auditMediaCreation() {
        File directory = new File(auditDataLocation);
        File errorDirectory = fileService.createDirectory(directory, "/_error");
        File successDirectory = fileService.createDirectory(directory, "/_success");

        Map<String, List<UploadDetail>> stringListMap = dataService.deserializeUploadDetailsFilesFromDirectory(directory);

        if (stringListMap.isEmpty()) {
            logger.info("no data to update");
            return;
        }

        List<Album> remoteAlbums = googleService.getRemoteAlbums();

        List<AuditResult> auditResults = new ArrayList<>();

        stringListMap.forEach((s, uploadDetails) -> {

            File jsonFile = new File(directory.getAbsolutePath() + "/" + s + ".json");

            List<Album> foundRemoteAlbums = remoteAlbums.stream()
                    .filter(album -> album.getTitle().equals(s))
                    .collect(Collectors.toList());

            int foundAlbumCount = 0;
            for (Album album : foundRemoteAlbums) {
                foundAlbumCount++;
                if (foundAlbumCount > 1) {
                    AuditResult ar = new AuditResult();
                    ar.setLocalAlbum(s);
                    ar.setLocalMediaCount(uploadDetails.size());
                    ar.setAlbumId(album.getId());
                    ar.setRemoteMediaCount((int) album.getMediaItemsCount());
                    auditResults.add(ar);
                } else {
                    AuditResult ar = new AuditResult();
                    ar.setLocalAlbum(s);
                    ar.setLocalMediaCount(uploadDetails.size());
                    ar.setAlbumId(album.getId());
                    ar.setRemoteMediaCount((int) album.getMediaItemsCount());
                    auditResults.add(ar);
                }
            }

            if (foundRemoteAlbums.size() > 1) {
                foundRemoteAlbums.forEach(album -> {
                    AuditResult ar = new AuditResult();
                    ar.setLocalAlbum(s);
                    ar.setLocalMediaCount(uploadDetails.size());
                    ar.setAlbumId(album.getId());
                    ar.setRemoteMediaCount((int) album.getMediaItemsCount());
                    auditResults.add(ar);
                });
            } else {
                Album album = foundRemoteAlbums.get(0);
                AuditResult ar = new AuditResult();
                ar.setLocalAlbum(s);
                ar.setLocalMediaCount(uploadDetails.size());
                ar.setAlbumId(album.getId());
                ar.setRemoteMediaCount((int) album.getMediaItemsCount());
                auditResults.add(ar);
            }


//            try {
//                logger.info("processing album {} containing {} items", s, uploadDetails.size());
//                googleService.addMediaInAlbums(uploadDetails);
//                //move file to success
//                fileService.writeJson(jsonFile, uploadDetails);
//                fileService.moveFile(successDirectory, jsonFile);
//            } catch (ApplicationBusinessException e) {
//                logger.info("fail processing album {}", s, e);
//                fileService.writeJson(jsonFile, uploadDetails);
//                fileService.moveFile(errorDirectory, jsonFile);
//            }
        });

    }


}



