package ro.jmind.photos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ro.jmind.photos.model.FileDetails;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckJpegFilesVsOneDriveTest {

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

        Map<String, List<FileDetails>> notFoundDirectoryFilesMap = new TreeMap<>();
        jpegSourceNotFound.stream()
                .forEach(fd -> {
                    List<FileDetails> orDefault = notFoundDirectoryFilesMap.getOrDefault(fd.getParent(), new ArrayList<FileDetails>());
                    orDefault.add(fd);
                    notFoundDirectoryFilesMap.put(fd.getParent(), orDefault);

                });

        //review
        List<FileDetails> jpegSourceFound = jpegFileDetails.stream()
                .map(jpegFD -> oneDriveFileDetails.stream()
                        .filter(oneDriveFD -> jpegFD.getNameWithoutExtenstion().equalsIgnoreCase(oneDriveFD.getNameWithoutExtenstion()))
                        .findFirst().orElse(null)
                )
                .collect(Collectors.toList());

        System.out.println("");

    }


}

