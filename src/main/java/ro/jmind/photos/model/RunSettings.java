package ro.jmind.photos.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RunSettings {
    @Value("${parentDataLocation}")
    private String parentDataLocation;
    @Value("${dataToUploadLocation}")
    private String dataToUploadLocation;
    @Value("${uploadDataLocation}")
    private String uploadDataLocation;
    @Value("${createMediaLocation}")
    private String createMediaLocation;
    @Value("${missingAlbumDataLocation}")
    private String missingAlbumDataLocation;
    @Value("${auditDataLocation}")
    private String auditDataLocation;

    public String getParentDataLocation() {
        return parentDataLocation;
    }

    public String getDataToUploadLocation() {
        return dataToUploadLocation;
    }

    public String getUploadDataLocation() {
        return uploadDataLocation;
    }

    public String getCreateMediaLocation() {
        return createMediaLocation;
    }

    public String getMissingAlbumDataLocation() {
        return missingAlbumDataLocation;
    }

    public String getAuditDataLocation() {
        return auditDataLocation;
    }
}
