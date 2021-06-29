package ro.jmind.photos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    private ObjectMapper objectMapper;

    public FileService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeJson(File file, Object item) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, item);
        } catch (IOException e) {
            String format = String.format("unable to write json file %s \n%s", file.getAbsolutePath(), item.toString());
            throw new RuntimeException(format, e);
        }
    }

    public List<File> getRecursiveFileList(String location) {
        File directory = new File(location);
        List<File> files = new ArrayList<File>();
        gatherFilesInList(files, directory);
        return files;
    }


    public List<File> getDirectoryList(File directory) {
        return Arrays.asList(directory.listFiles(File::isDirectory));
//        File[] listFiles = directory.listFiles();
//        List<File> result = new ArrayList<>();
//        for (File f : listFiles) {
//            if (f.isDirectory()) {
//                result.add(f);
//            }
//        }
//        return result;
    }

    private void gatherFilesInList(List<File> fileList, File directory) {
        File[] listFiles = directory.listFiles();
        for (File f : listFiles) {
            if (f.isDirectory()) {
                gatherFilesInList(fileList, f);
            } else {
                fileList.add(f);
            }
        }
    }

    public File createDirectory(File parentDirectory, String directoryName) {
        File targetDir = new File(parentDirectory.getAbsolutePath() + directoryName);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    public void moveFile(File targetDirectory, File file) {
        try {
            Files.move(file, new File(targetDirectory.getAbsolutePath() + "/" + file.getName()));
        } catch (IOException ex) {
            LOGGER.error("unable to move file {} to {}", file.getName(), targetDirectory.getAbsolutePath());
        }
    }
}
