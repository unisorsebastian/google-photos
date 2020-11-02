package ro.jmind.photos.model;

public class AuditResult {
    private String localAlbum;
    private int localMediaCount;
    private String albumId;
    private int remoteMediaCount;

    public String getLocalAlbum() {
        return localAlbum;
    }

    public void setLocalAlbum(String localAlbum) {
        this.localAlbum = localAlbum;
    }

    public int getLocalMediaCount() {
        return localMediaCount;
    }

    public void setLocalMediaCount(int localMediaCount) {
        this.localMediaCount = localMediaCount;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public int getRemoteMediaCount() {
        return remoteMediaCount;
    }

    public void setRemoteMediaCount(int remoteMediaCount) {
        this.remoteMediaCount = remoteMediaCount;
    }
}
