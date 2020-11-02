package ro.jmind.photos.service;


import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;
import com.google.rpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.jmind.photos.model.ApplicationBusinessException;
import ro.jmind.photos.model.UploadDetail;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GoogleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleService.class);
    private PhotosLibraryClient client;
    private List<Album> remoteAlbums;

    public GoogleService(PhotosLibraryClient client) {
        this.client = client;
    }


    public List<MediaItem> getMediaItems(String albumId) {
        InternalPhotosLibraryClient.SearchMediaItemsPagedResponse searchMediaItemsPagedResponse = client.searchMediaItems(albumId);
        List<MediaItem> mediaItems = new ArrayList<>();
        searchMediaItemsPagedResponse.iterateAll().forEach(mediaItem -> {
            mediaItems.add(mediaItem);
        });
        return mediaItems;
    }

    public Album renameAlbum(Album album, String newTitle) {
        return client.updateAlbumTitle(album, newTitle);
    }

    //TODO refactor - method is modifying uploadDetails
    public void uploadFilesFromUploadDetails(List<UploadDetail> uploadDetails) {

        AtomicInteger counter = new AtomicInteger(1);
        uploadDetails.stream().forEach(u -> {
            LOGGER.info("uploading {} of {} files", counter.getAndIncrement(), uploadDetails.size());
            File file = new File(u.getFileLocation());
            //actual file upload
            UploadDetail fileUploadDetail = uploadFile(file);

            //update object
            u.setTokenUpload(fileUploadDetail.getTokenUpload());
            u.setStatusUpload(fileUploadDetail.getStatusUpload());

        });
    }

    public UploadDetail uploadFile(File file) {
        UploadDetail uploadDetail = new UploadDetail();
        uploadDetail.setFileLocation(file.getAbsolutePath());

        long startTime = System.currentTimeMillis();
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

            UploadMediaItemRequest uploadRequest = UploadMediaItemRequest.newBuilder()
                    //filename of the media item along with the file extension
                    .setFileName(file.getName())
                    .setDataFile(randomAccessFile)
                    .build();
            // Upload and capture the response
            UploadMediaItemResponse uploadResponse = client.uploadMediaItem(uploadRequest);
            if (uploadResponse.getError().isPresent()) {
                // If the upload results in an error, handle it
                uploadDetail.setStatusUpload(uploadResponse.getError().get().toString());
            } else {
                // If the upload is successful, get the uploadToken
                uploadDetail.setTokenUpload(uploadResponse.getUploadToken().get());
                //TODO: save date
                // Use this upload token to create a media item
            }
        } catch (Exception e) {
            uploadDetail.setStatusUpload("upload exception");
            LOGGER.info("upload error {}", e.getMessage());
        }

        double size = (double) Math.round((double) file.length() / 1024 / 1024 * 100) / 100;
        double totalTime = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 10)) / 10;

        LOGGER.info("uploaded {} MB in {} secs, file: {}", size, totalTime, file.getAbsolutePath());
        return uploadDetail;
    }


    public void addMediaInAlbums(List<UploadDetail> uploadDetails) throws ApplicationBusinessException {
        List<UploadDetail> batch = new ArrayList<>();
        int maxSize = 50;

        checkMandatoryData(uploadDetails);

        for (UploadDetail u : uploadDetails) {
            batch.add(u);
            if (batch.size() % maxSize == 0) {
                LOGGER.info("add {} items", batch.size());
                createMedia(batch);
                batch = new ArrayList<>();
            }
        }
        if (batch.size() > 0) {
            LOGGER.info("add last batch of {} items", batch.size());
            createMedia(batch);
        }
    }

    private void createMedia(List<UploadDetail> uploadDetails) throws ApplicationBusinessException {
        if (uploadDetails == null || uploadDetails.isEmpty()) {
            return;
        }
        long startTime = System.currentTimeMillis();

        String albumId = null;
        String albumName = null;
        List<NewMediaItem> newMediaItems = new ArrayList<>();

        for (UploadDetail ud : uploadDetails) {
            if (albumId == null) {
                albumId = ud.getAlbumId();
                albumName = ud.getAlbumName();
            }
            String tokenUpload = ud.getTokenUpload();
            newMediaItems.add(NewMediaItemFactory.createNewMediaItem(tokenUpload));
        }

        // Create new media items in a specific album
        BatchCreateMediaItemsResponse response = null;
        try {
            response = client.batchCreateMediaItems(albumId, newMediaItems);
        } catch (Exception e) {
            LOGGER.info("unable to add media to album {} {}", albumName, albumId, e);
            for (UploadDetail u : uploadDetails) {
                u.setStatusMedia(e.getMessage());
            }
            throw new ApplicationBusinessException(e);
        }

        // The response contains a list of NewMediaItemResults
        if (response != null) {
            for (NewMediaItemResult result : response.getNewMediaItemResultsList()) {
                // Each result item is identified by its uploadToken
                String uploadToken = result.getUploadToken();
                Status status = result.getStatus();

                UploadDetail uploadDetail = uploadDetails.stream().filter(e -> e.getTokenUpload().equals(uploadToken)).findFirst().get();
                uploadDetail.setStatusMedia(status.getMessage());
            }
        }
        double totalTime = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 10)) / 10;
        LOGGER.info("took {} to add {} media items to album {}", totalTime, uploadDetails.size(), albumName);
    }

    public Album createAlbum(String albumName) {
        Album album;
        try {
            album = client.createAlbum(Album.newBuilder().setTitle(albumName).build());
            remoteAlbums.add(album);
        } catch (Exception e) {
            LOGGER.info("unable to create album {}", albumName);
            throw new RuntimeException(e);
        }
        LOGGER.info("added new created album {} with id {}", albumName, album.getId());
        LOGGER.info("remote album size {}", remoteAlbums.size());
        return album;
    }

    public List<Album> getRemoteAlbumsForced() {
        long startTime = System.currentTimeMillis();
        LOGGER.info("getting all remote albums");
        List<Album> albumList = new ArrayList<>();
        InternalPhotosLibraryClient.ListAlbumsPagedResponse response = client.listAlbums();
        for (Album album : response.iterateAll()) {
            // Get some properties of an album
            String id = album.getId();
            String title = album.getTitle();
            String productUrl = album.getProductUrl();
            String coverPhotoBaseUrl = album.getCoverPhotoBaseUrl();
            // The cover photo media item id field may be empty
            String coverPhotoMediaItemId = album.getCoverPhotoMediaItemId();
            boolean isWritable = album.getIsWriteable();
            long mediaItemsCount = album.getMediaItemsCount();
            albumList.add(album);
        }
        double totalTime = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 10)) / 10;
        LOGGER.info("got all {} albums in {} seconds", albumList.size(), totalTime);
        remoteAlbums = new ArrayList<>();
        remoteAlbums.addAll(albumList);
        return remoteAlbums;
    }

    public List<Album> getRemoteAlbums() {
        if (remoteAlbums != null) {
            return remoteAlbums;
        }

        return getRemoteAlbumsForced();
    }

    private void checkMandatoryData(List<UploadDetail> uploadDetails) throws ApplicationBusinessException {
        boolean dataIsMissing = false;
        if (uploadDetails == null) {
            throw new ApplicationBusinessException("upload details is null");
        }
        for (UploadDetail ud : uploadDetails) {
            String tokenUpload = ud.getTokenUpload();
            if (tokenUpload == null || tokenUpload.isEmpty()) {
                LOGGER.info("uploadToken is missing for {} ", ud.getFileLocation());
                dataIsMissing = true;
                ud.setStatusUpload("missing token");
                ud.setStatusMedia("missing token");
            }
            if (ud.getAlbumId() == null || ud.getAlbumId().isEmpty()) {
                LOGGER.info("albumId is missing from uploadDetail {}", ud.getFileLocation());
                dataIsMissing = true;
                ud.setStatusMedia("missing album id");
            }
        }
        if (dataIsMissing) {
            throw new ApplicationBusinessException("data is missing");
        }
    }


}
