package ro.jmind.photos.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CheckDirectoiesTest {
    private final String[] jpegs = {"jpg", "gif", "png", "heic"};
    private final String[] raws = {"dng", "cr2", "raw", "tif"};
    private FileService fileService = new FileService(new ObjectMapper());

    @Test
    public void checkDirectoriesTest() {
        Path oneDrivePath = Paths.get("D:\\OneDrive\\_backup\\photos\\seba");
        LinkedHashSet<String> uniqueExt = fileService.getRecursiveFileList("D:\\OneDrive\\_backup\\photos\\seba").stream()
                .map(f -> FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, List<String>> extensionMap = new TreeMap<>();
        fileService.getDirectoryList(oneDrivePath.toFile().getAbsoluteFile()).stream()
                .forEach(file -> {
                    LinkedHashSet<String> extenstions = fileService.getRecursiveFileList(file.getAbsolutePath()).stream()
                            .map(f -> FilenameUtils.getExtension(f.getAbsolutePath()).toLowerCase())
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    extensionMap.put(file.getAbsolutePath().replaceAll("\\\\", "/"), new ArrayList<>(extenstions));
                });
        fileService.writeJson(new File("d:/extentionMap.json"), extensionMap);
        extensionMap.entrySet().stream()
                .map(entry -> {
                    entry.getValue().stream()
                            .filter(s -> Arrays.asList(jpegs).contains(s));

                    return new AbstractMap.SimpleEntry<String, String>(entry.getKey(), "");
                }).collect(Collectors.toList());

        System.out.println("done");
    }
}
