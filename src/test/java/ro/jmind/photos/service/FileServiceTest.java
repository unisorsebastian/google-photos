package ro.jmind.photos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class FileServiceTest {

    @Test
    void getRecursiveFileList() {
        FileService fs = new FileService(new ObjectMapper());
        List<File> recursiveFileList = fs.getRecursiveFileList("D:\\OneDrive\\_backup\\photos\\seba");
        Set<String> extensions = recursiveFileList.stream()
                .filter(file -> file.isFile() == true)
                .map(file -> {
                    return file.getName();

                })
                .map(s -> s.split("\\.")[1])
                .collect(Collectors.toSet());
        System.out.println("done");
    }
}