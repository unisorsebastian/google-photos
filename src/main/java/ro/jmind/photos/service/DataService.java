package ro.jmind.photos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.photos.types.proto.Album;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.jmind.photos.model.ApplicationBusinessException;
import ro.jmind.photos.model.UploadDetail;

import java.io.File;
import java.io.IOException;
import java.util.*;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

@Service
public class DataService {
    //    private static final Logger logger = LogManager.getLogger(DataService.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    private ObjectMapper objectMapper;
    private FileService fileService;
    private GoogleService googleService;

    public DataService(ObjectMapper objectMapper, FileService fileService, GoogleService googleService) {
        this.objectMapper = objectMapper;
        this.fileService = fileService;
        this.googleService = googleService;
    }

    public void updateUploadDetailWithAlbumData(List<UploadDetail> uploadDetails, Album album) {
        //update uploadDetail with album details
        for (UploadDetail u : uploadDetails) {
            u.setAlbumId(album.getId());
            u.setAlbumName(album.getTitle());
        }
    }

    public void populateMissingAlbumData(List<UploadDetail> data, List<Album> remoteAlbums) {
//        long missingDataCount = data.stream()
//                .filter(uploadDetail -> {
//                    return uploadDetail.getAlbumId() == null || uploadDetail.getAlbumId().isEmpty();
//                })
//                .count();
//        if (missingDataCount < 1) {
//            logger.info("no data to be updated");
//            return;
//        }

        String localAlbumName = new File(data.get(0).getParentLocation()).getName();

        data.stream()
//                .filter(uploadDetail -> {
//                    return uploadDetail.getAlbumId() == null || uploadDetail.getAlbumId().isEmpty();
//                })
                .forEach(uploadDetail -> {

                    Album album = remoteAlbums.stream()
                            .filter(a -> {
                                return a.getTitle().equalsIgnoreCase(localAlbumName) && a.getIsWriteable() && !a.getId().isEmpty();
                            })
                            .findFirst()
                            .orElseGet(() -> {
                                Album newAlbumCreated = googleService.createAlbum(localAlbumName);
                                remoteAlbums.add(newAlbumCreated);
                                return newAlbumCreated;
                            });

                    uploadDetail.setAlbumId(album.getId());
                    uploadDetail.setAlbumName(album.getTitle());
                });
    }

    public UploadDetail cloneUploadDetail(UploadDetail uploadDetail) {
        UploadDetail result = null;
        try {
            String json = objectMapper.writeValueAsString(uploadDetail);
            result = objectMapper.readValue(json, UploadDetail.class);
        } catch (Exception e) {
            LOGGER.info("unable to clone object");
        }
        return result;
    }

    public Map<String, List<UploadDetail>> deserializeUploadDetailsFilesFromDirectory(File uploadDetailsParentDirectory) {
        File errorDirectory = fileService.createDirectory(uploadDetailsParentDirectory, "/_error");

        Map<String, List<UploadDetail>> result = new TreeMap<>();

        Arrays.stream(uploadDetailsParentDirectory.listFiles())
                //get only json files
                .filter(file -> {
                    return (!file.isDirectory() && file.getName().toLowerCase().endsWith(".json"));
                })
                .forEach(file -> {
                    List<UploadDetail> uploadDetails = null;
                    try {
                        uploadDetails = deserializeUploadDetailsFile(file);
                    } catch (ApplicationBusinessException e) {
                        LOGGER.error("move file {} to error {}", file.getName(), errorDirectory.getAbsolutePath());
                        fileService.moveFile(errorDirectory, file);
                    }

                    if (uploadDetails != null) {
                        result.put(FilenameUtils.removeExtension(file.getName()), uploadDetails);
                    }
                });
        return result;
    }

    public List<UploadDetail> deserializeUploadDetailsFile(File file) throws ApplicationBusinessException {
        List<UploadDetail> result = null;
        LOGGER.info("start deserialize file {}", file.getName());
        try {
            result = Arrays.asList(objectMapper.readValue(file, ro.jmind.photos.model.UploadDetail[].class));
        } catch (IOException e) {
            LOGGER.error("unable to deserialize the file {}", file.getName());
            throw new ApplicationBusinessException(e);
        }
        return result;
    }


    public Map<String, List<UploadDetail>> collectLocalData(File parentDirectory) {
        LOGGER.info("start collecting data");
        long startTime = System.currentTimeMillis();
        List<File> albumNamesFromLocal;
        albumNamesFromLocal = fileService.getDirectoryList(parentDirectory);
        Map<String, List<UploadDetail>> albumNameUploadDetailsMap = new TreeMap<>();
        for (File dir : albumNamesFromLocal) {
            final List<UploadDetail> albumUploadDetail = new ArrayList<>();
            //get all files from directory and subdirectories
            List<File> fileList = fileService.getRecursiveFileList(dir.getAbsolutePath());
            //filter files keep only png jpg jpeg heic gif
            fileList.stream().filter(file -> {
                return file.getName().toLowerCase().endsWith(".jpeg")
                        || file.getName().toLowerCase().endsWith(".jpg")
                        || file.getName().toLowerCase().endsWith(".heic")
                        || file.getName().toLowerCase().endsWith(".gif")
                        || file.getName().toLowerCase().endsWith(".png");
            }).forEach(file -> {
                //create upload details
                UploadDetail ud = new UploadDetail();
                ud.setParentLocation(dir.getAbsolutePath());
                ud.setFileLocation(file.getAbsolutePath());
                albumUploadDetail.add(ud);
            });
            //Collections.sort(albumNamesFromLocal,((o1, o2)->o2.getName().compareTo(o1.getName())));
            albumNameUploadDetailsMap.put(dir.getName(), albumUploadDetail);
        }
        double totalTime = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 10)) / 10;
        LOGGER.info("collecting data took {} seconds", totalTime);
        return albumNameUploadDetailsMap;
    }
}
