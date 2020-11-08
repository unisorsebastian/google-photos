package ro.jmind.photos.model;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class FileDetails {
    private File file;
    private String nameWithoutExtenstion;
    private String parent;
    private String fileAbsolutePath;

    private FileDetails() {
    }

    public String getFileAbsolutePath() {
        return fileAbsolutePath;
    }

    public String getNameWithoutExtenstion() {
        return nameWithoutExtenstion;
    }

    public String getParent() {
        return parent;
    }

    public File getFile() {
        return file;
    }

    public static class FileDetailsBuilder {
        private FileDetails fd = new FileDetails();

        public FileDetails fromFile(File file) {
            if (file == null || file.isDirectory() || !file.exists()) {
                throw new RuntimeException("incorrect file");
            }
            fd.file = file;
            fd.nameWithoutExtenstion = FilenameUtils.removeExtension(file.getName());
            fd.parent = file.getAbsoluteFile().getParent();
            fd.fileAbsolutePath = file.getAbsoluteFile().getAbsolutePath();
            return fd;
        }

    }
}
