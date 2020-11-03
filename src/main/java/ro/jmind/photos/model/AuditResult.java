package ro.jmind.photos.model;

import com.google.photos.types.proto.Album;

import java.util.ArrayList;
import java.util.List;

public class AuditResult {
    private String localAlbum;
    private int localMediaCount;
    private List<Album> remoteAlbums = new ArrayList<>();

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

    public List<Album> getRemoteAlbums() {
        return remoteAlbums;
    }

}
