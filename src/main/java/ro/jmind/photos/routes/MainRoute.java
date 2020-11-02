package ro.jmind.photos.routes;

import com.google.photos.types.proto.Album;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ro.jmind.photos.model.RunSettings;
import ro.jmind.photos.model.UploadDetail;
import ro.jmind.photos.service.DataService;
import ro.jmind.photos.service.GoogleService;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainRoute extends RouteBuilder {
    public static final String ALBUM_NAME_KEY = "albumName";
    public static final String ALBUM_ID_KEY = "albumId";
    private static Logger LOGGER = LoggerFactory.getLogger(MainRoute.class);

    private DataService dataService;
    private GoogleService googleService;
    private RunSettings runSettings;

    @Value("${parentDataLocation}")
    private File baseDir;

    public MainRoute(DataService dataService, GoogleService googleService, RunSettings runSettings) {
        this.dataService = dataService;
        this.googleService = googleService;
        this.runSettings = runSettings;
    }

    //    private void init() {
//        baseDir = new File(runSettings.getParentDataLocation());
//    }

    @Override
    public void configure() throws Exception {
        JacksonDataFormat df = new JacksonDataFormat(UploadDetail.class);
        JacksonDataFormat dfList = new ListJacksonDataFormat(UploadDetail.class);
        df.setPrettyPrint(true);
//        dfList.setPrettyPrint(true);

        from("direct:defaultRoute").routeId("defaultRouteId")
                .log(LoggingLevel.INFO, LOGGER, "default route called")
                .end();

        from("direct:routeOne").routeId("routeOneId")
                .log(LoggingLevel.INFO, LOGGER, "route ONE called")
                .end();


        from("direct:gatherLocalData").routeId("gatherLocalData")
                .log("start gathering local data to be uploaded")
                .process(exchange -> {
                    Map<String, List<UploadDetail>> body = exchange.getIn().getBody(Map.class);
                    exchange.getIn().setBody(body.entrySet());
//                    this is not working
//                    exchange.getIn().setBody(body.entrySet(),Map.Entry.class);
                })
                .split()
                .body()
                .transform()
                .body(Map.Entry.class, (entry, header) -> {
                    header.put(Exchange.FILE_NAME, String.format("%s.json", entry.getKey()));
                    return entry.getValue();
                })
                .marshal(df)
                .log("creating file ${headers.camelFileName} from body")
                .to("file:" + runSettings.getDataToUploadLocation())
                .end();

        from("file:" + runSettings.getMissingAlbumDataLocation() +
                "?moveFailed=_error" +
                "&delete=true" +
                "&maxMessagesPerPoll=1" +
                "&delay=500")
                .routeId("populateMissingAlbumData")
                .unmarshal(dfList)
//                .unmarshal().json(JsonLibrary.Jackson, List.class)
                .transform()
                .body(UploadDetail[].class, (uploadDetails, headers) -> {
                    //obtain remote album data
                    List<Album> remoteAlbums = googleService.getRemoteAlbums();
                    List<UploadDetail> data = Arrays.asList(uploadDetails);
                    dataService.populateMissingAlbumData(data, remoteAlbums);
                    return uploadDetails;
                })
                .marshal(df)
                .to("file:" + runSettings.getMissingAlbumDataLocation() + "/_success")
                .log("populate album data")
                .end();

        // step 3 - upload raw data
        from("file:" + runSettings.getUploadDataLocation() +
                "?moveFailed=_error" +
                "&delete=true" +
                "&maxMessagesPerPoll=1" +
                "&delay=500")
                .routeId("dataUploading")
                .log("start data uploading for ${headers.camelFileName}")
                .unmarshal(dfList)
                .transform()
                .body(UploadDetail[].class, (uploadDetailsArray, headers) -> {
                    List<UploadDetail> uploadDetails = Arrays.asList(uploadDetailsArray);
                    if (uploadDetails != null) {
                        googleService.uploadFilesFromUploadDetails(uploadDetails);
                        long successTokensCount = uploadDetails.stream()
                                .filter(uploadDetail -> uploadDetail.getTokenUpload() != null && !uploadDetail.getTokenUpload().isEmpty())
                                .count();
                        if (successTokensCount != uploadDetails.size()) {

                            String message = String.format("not all the files were uploaded for %s", headers.get(Exchange.FILE_NAME));
                            throw new RuntimeException(message);
                        }

                    }
                    return uploadDetails;

                })
                .marshal(df)
                .to("file:" + runSettings.getUploadDataLocation() + "/_success")
                .log("done uploading media for ${headers.camelFileName}")
                .end();


        from("direct:mainRoute").routeId("mainRouteId")
                .log("main route called")
                .process(exchange -> {
                    System.out.println(exchange);
                })
                .split()
                .body(List.class)
                .process(exchange -> {
                    System.out.println(exchange);
                })
                .transform()
                .body(UploadDetail.class, (uploadDetail, headers) -> {
                    String fileName = String.format("%s.json", uploadDetail.getAlbumName());
                    headers.put(Exchange.FILE_NAME, fileName);
                    return uploadDetail;
                })
                .marshal(df)
                //TODO
//                .to("file:" + runSettings.getUpdatedAlbumDataLocation())
                .log(LoggingLevel.INFO, LOGGER, "create json ${body}")
                .end();


//        from("file:" + runSettings.getMissingAlbumDataLocation() + "?moveFailed=FAILED").routeId("updateAlbum")
//                .log("reading files and update albums")
//                .process(exchange -> {
//                    File inputJsonFile = exchange.getIn().getBody(File.class);
//                    LOGGER.info("Processing file {}", inputJsonFile.getName());
//                })
//                .unmarshal(df)
//                .process(exchange -> {
//                    UploadDetail uploadDetail = exchange.getIn().getBody(UploadDetail.class);
//                    uploadDetail.setAlbumId(String.format("%s", System.currentTimeMillis()));
//                    LOGGER.info("do something to update the data and write the file");
//                    if (uploadDetail.getAlbumName().contains("469")) {
//                        throw new RuntimeException();
//                    }
//                })
//                .marshal(df)
//                .to("file:" + runSettings.getMissingAlbumDataLocation())
//                .end();


//        from("file:" + runSettings.getMissingAlbumDataLocation() + "?moveFailed=failed&maxMessagesPerPoll=1&delay=5000").routeId("populateMissingAlbumData")
////                .log("Received body ${body}")
//                .unmarshal(dfList)
////                .unmarshal().json(JsonLibrary.Jackson, List.class)
//                .transform()
//                .body((body, headers) -> {
//                    headers.put(ALBUM_NAME_KEY, "remoteAlbumName");
//                    headers.put(ALBUM_ID_KEY, "remoteAlbumId");
//                    return body;
//                })
//                .split()
//                .body(List.class)
//                .transform()
//                .body(UploadDetail.class, (uploadDetail, headers) -> {
//                    uploadDetail.setAlbumName((String)headers.get(ALBUM_NAME_KEY));
//                    uploadDetail.setAlbumId((String)headers.get(ALBUM_ID_KEY));
//                    return uploadDetail;
//                })
//                .log("populate album data")
//                .end();


    }
}
