package ro.jmind.photos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ro.jmind.photos.model.FileDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckJpegFilesVsOneDriveTest {
    public static final String NOT_FOUND_ = "_notFound_";
    private FileService fileService = new FileService(new ObjectMapper());

    @Test
    public void FileDetailsBuiderTest() {
        FileDetails.FileDetailsBuilder fdb = new FileDetails.FileDetailsBuilder();
        FileDetails fileDetails = fdb.fromFile(Paths.get("src/test/resources/dummyFile.txt").toFile());
        assertTrue("dummyFile".equals(fileDetails.getNameWithoutExtenstion()));
    }

    @Test
    public void filesInJpegVsOneDrive() throws IOException {
        Path oneDrivePath = Paths.get("D:\\OneDrive\\_backup\\photos\\seba");
        Path jpegPath = Paths.get("d:\\_photosJpeg");

        List<FileDetails> jpegFileDetails = fileService.getRecursiveFileList(jpegPath.toString()).stream()
                .map(file -> new FileDetails.FileDetailsBuilder().fromFile(file))
                .collect(Collectors.toList());
        List<FileDetails> oneDriveFileDetails = fileService.getRecursiveFileList(oneDrivePath.toString()).stream()
                .map(file -> new FileDetails.FileDetailsBuilder().fromFile(file))
                .collect(Collectors.toList());


        List<FileDetails> jpegSourceNotFound = jpegFileDetails.stream()
                .filter(jpegFD -> {
                    return !oneDriveFileDetails.stream()
                            .filter(oneDriveFD -> jpegFD.getNameWithoutExtenstion().equalsIgnoreCase(oneDriveFD.getNameWithoutExtenstion()))
                            .findFirst()
                            .isPresent();
                }).collect(Collectors.toList());

        Map<String, List<FileDetails>> notFoundDirectoryFilesMap = new TreeMap<>();
        jpegSourceNotFound.stream()
                .forEach(fd -> {
                    List<FileDetails> orDefault = notFoundDirectoryFilesMap.getOrDefault(fd.getParent(), new ArrayList<FileDetails>());
                    orDefault.add(fd);
                    notFoundDirectoryFilesMap.put(fd.getParent(), orDefault);
                });

        List<AbstractMap.SimpleEntry> notFoundCount = notFoundDirectoryFilesMap.entrySet().stream()
                .map(stringListEntry -> new AbstractMap.SimpleEntry(stringListEntry.getKey(), stringListEntry.getValue().size()))
                .collect(Collectors.toList());

        List<FileDetails> flatNotFound = notFoundDirectoryFilesMap.entrySet().stream()
                .map(fdl -> fdl.getValue()).collect(Collectors.toList()).stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

//                .collect(Collectors.toCollection(LinkedHashSet::new));

        File notFoundFiles = Paths.get("d:/_notFoundDirectoryFilesMap.json").toFile();
        File notFoundCountFiles = Paths.get("d:/_notFoundCount.json").toFile();
        fileService.writeJson(notFoundFiles, notFoundDirectoryFilesMap);
        fileService.writeJson(notFoundCountFiles, notFoundCount);

        //review
//        List<FileDetails> jpegSourceFound = jpegFileDetails.stream()
//                .map(jpegFD -> oneDriveFileDetails.stream()
//                        .filter(oneDriveFD -> jpegFD.getNameWithoutExtenstion().equalsIgnoreCase(oneDriveFD.getNameWithoutExtenstion()))
//                        .findFirst().orElse(null)
//                )
//                .collect(Collectors.toList());
//
//        System.out.println("");

    }

    @Test
    public void renameFilesStartingWith() {
        Path jpegPath = Paths.get("d:\\_photosJpeg");
        List<String> affectedFiles = new ArrayList<>();
        List<FileDetails> jpegFileDetails = fileService.getRecursiveFileList(jpegPath.toString()).stream()
                .map(file -> new FileDetails.FileDetailsBuilder().fromFile(file))
                .collect(Collectors.toList());
        jpegFileDetails.stream()
                .filter(fd -> fd.getNameWithoutExtenstion().startsWith(NOT_FOUND_))
                .forEach(fileDetails -> {
                    Path absoluteFilePath = fileDetails.getFile().getAbsoluteFile().toPath();
                    String newName = fileDetails.getFile().getName().replace(NOT_FOUND_, "");
                    try {
                        Files.move(absoluteFilePath, absoluteFilePath.resolveSibling(newName));
                        affectedFiles.add("absoluteFilePath");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        System.out.println(affectedFiles);
    }

    @Test
    public void renameNotFoundFiles() {
        Path oneDrivePath = Paths.get("D:\\OneDrive\\_backup\\photos\\seba");
        Path jpegPath = Paths.get("d:\\_photosJpeg");
//        Path jpegPath = Paths.get("D:\\_photosJpeg\\2016_06_19_HR_Croiatia_Private");
        List<String> affectedFiles = new ArrayList<>();

        FileService fileService = new FileService(new ObjectMapper());

        List<FileDetails> jpegFileDetails = fileService.getRecursiveFileList(jpegPath.toString()).stream()
                .map(file -> new FileDetails.FileDetailsBuilder().fromFile(file))
                .collect(Collectors.toList());
        List<FileDetails> oneDriveFileDetails = fileService.getRecursiveFileList(oneDrivePath.toString()).stream()
                .map(file -> new FileDetails.FileDetailsBuilder().fromFile(file))
                .collect(Collectors.toList());

        List<FileDetails> jpegSourceNotFound = jpegFileDetails.stream()
                .filter(jpegFD -> {
                    return !oneDriveFileDetails.stream()
                            .filter(oneDriveFD -> jpegFD.getNameWithoutExtenstion().equalsIgnoreCase(oneDriveFD.getNameWithoutExtenstion()))
                            .findFirst()
                            .isPresent();
                }).collect(Collectors.toList());

        jpegSourceNotFound.stream().forEach(fileDetails -> {
            //rename file
            Path absoluteFilePath = fileDetails.getFile().getAbsoluteFile().toPath();
            String newName = String.format(NOT_FOUND_ + "%s", fileDetails.getFile().getName());
            try {
                Files.move(absoluteFilePath, absoluteFilePath.resolveSibling(newName));
                affectedFiles.add(fileDetails.getFile().getAbsoluteFile().getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println(affectedFiles);
    }


}

