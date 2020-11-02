package ro.jmind.photos.model;

public class UploadDetail {
    private String fileLocation;
    private String parentLocation;
    private String albumId;
    private String albumName;
    private String tokenUpload;
    private String tokenMedia;
    private String statusUpload;
    private String statusMedia;

    @Override
    public String toString() {
        return "UploadDetail{" +
                "fileLocation='" + fileLocation + '\'' +
                ", parentLocation='" + parentLocation + '\'' +
                ", albumId='" + albumId + '\'' +
                ", albumName='" + albumName + '\'' +
                ", tokenUpload='" + tokenUpload + '\'' +
                ", tokenMedia='" + tokenMedia + '\'' +
                ", statusUpload='" + statusUpload + '\'' +
                ", statusMedia='" + statusMedia + '\'' +
                '}';
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getParentLocation() {
        return parentLocation;
    }

    public void setParentLocation(String parentLocation) {
        this.parentLocation = parentLocation;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getTokenUpload() {
        return tokenUpload;
    }

    public void setTokenUpload(String tokenUpload) {
        this.tokenUpload = tokenUpload;
    }

    public String getTokenMedia() {
        return tokenMedia;
    }

    public void setTokenMedia(String tokenMedia) {
        this.tokenMedia = tokenMedia;
    }

    public String getStatusUpload() {
        return statusUpload;
    }

    public void setStatusUpload(String statusUpload) {
        this.statusUpload = statusUpload;
    }

    public String getStatusMedia() {
        return statusMedia;
    }

    public void setStatusMedia(String statusMedia) {
        this.statusMedia = statusMedia;
    }
}
