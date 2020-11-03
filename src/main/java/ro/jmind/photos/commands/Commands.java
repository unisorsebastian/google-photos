package ro.jmind.photos.commands;

import com.google.photos.types.proto.Album;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ro.jmind.photos.model.AuditResult;
import ro.jmind.photos.service.CommandService;
import ro.jmind.photos.service.DataService;
import ro.jmind.photos.service.FileService;
import ro.jmind.photos.service.GoogleService;

import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
public class Commands {

    private CommandService commandService;
    private GoogleService googleService;
    private FileService fileService;
    private DataService dataService;

    public Commands(CommandService commandService, GoogleService googleService, FileService fileService, DataService dataService) {
        this.commandService = commandService;
        this.googleService = googleService;
        this.fileService = fileService;
        this.dataService = dataService;
    }

//    @ShellMethod("Gather data to be uploaded")
//    public String gatherData() {
//        long startTime = System.currentTimeMillis();
//
//        File parentDirectory = new File("D:\\OneDrive\\_backup\\photos\\seba");
//        Map<String, List<UploadDetail>> albumNameUploadDetailsMap = dataService.collectLocalData(parentDirectory);
//
//        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
//        String format = String.format("done gathering data to be published in %s seconds", timeTook);
//        return format;
//    }

    @ShellMethod("Create json files with local data")
    public String prepareDataToUpload() {
        long startTime = System.currentTimeMillis();
        commandService.prepareDataToUpload();
        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
        String format = String.format("done gathering data to be published in %s seconds", timeTook);
        return format;
    }

    @ShellMethod("Populate missing album data")
    public String populateMissingAlbumData() {
        long startTime = System.currentTimeMillis();
        commandService.populateMissingAlbumData();
        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
        String format = String.format("done missing album data in %s seconds", timeTook);
        return format;
    }

    @ShellMethod("Upload data for each json file")
    public String uploadData() {
        long startTime = System.currentTimeMillis();
        commandService.uploadData();
        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
        String format = String.format("done uploading data in %s seconds", timeTook);
        return format;
    }

    @ShellMethod("Create media to albums")
    public String createMediaToAlbums() {
        long startTime = System.currentTimeMillis();
        commandService.createMediaToAlbums();
        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
        String format = String.format("done creating media in albums took %s seconds", timeTook);
        return format;
    }

    @ShellMethod("Calculate upload size")
    public String calculateUploadSize() {
        long startTime = System.currentTimeMillis();
        int size = commandService.calculateMediaSize();
        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
//        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
//        symbols.setGroupingSeparator(' ');
//        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
//        String format1 = formatter.format(size);
        String format = String.format("done calculating media upload size %,d MB from all albums took %s seconds", size, timeTook);
        return format;
    }

    @ShellMethod("get remote albums data")
    public List<Album> getGoogleAlbums() {
        System.out.println("get google albums");
        List<Album> allGoogleAlbums = googleService.getRemoteAlbums();
        List<Album> allWriteableAlbums = allGoogleAlbums.stream().filter(Album::getIsWriteable).collect(Collectors.toList());
        Album removeMe = allWriteableAlbums.stream().filter(album -> album.getTitle().equals("_old__removeMe")).findFirst().get();
        Album renamedAlbum = googleService.renameAlbum(removeMe, String.format("_old_%s", removeMe.getTitle()));

        return allWriteableAlbums;
    }


    @ShellMethod("Create album")
    public String createAlbum(String albumName) {
        Album album = googleService.createAlbum(albumName);

        String format = String.format("albumName: %s\nalbumId:%s", album.getTitle(), album.getId());
        return format;
    }

    @ShellMethod("audit")
    public String audit() {
        List<AuditResult> auditResults = commandService.auditMediaCreation();
        String format = String.format("audit is done");
        return format;
    }

    @ShellMethod("credentials")
    public String googleCredentialToken() {
        String stringAuth = "";

//        final GoogleCredentials googleCredentials = ServiceAccountCredentials
//                .createScoped(Collections.singletonList(StorageScopes.DEVSTORAGE_FULL_CONTROL));
//        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(googleCredentials);
//
//        final com.google.api.services.storage.Storage myStorage = new com.google.api.services.storage.Storage.Builder(
//                new NetHttpTransport(), new JacksonFactory(), requestInitializer).build();

        String format = String.format("google credential %s", stringAuth);
        return format;
    }

//    @ShellMethod("Upload file to album by name")
//    public String uploadFileToAlbumByName(String albumName) {
//
//        List<Album> collect = googleService.getAllAlbums().stream().filter(album -> album.getTitle().equals(albumName)).collect(Collectors.toList());
//
//        File file = new File("D:\\OneDrive\\_backup\\photos\\seba\\2009_05_09_RO_Cota2000_Private\\20090509_150032-.jpg");
//        UploadDetail uploadDetail = googleService.uploadFile(file);
//        uploadDetail.setAlbumId("ALFNrei8Gcog0Ix16viUlCkBSk3Fnv3vRAEBmFdCdyDZwABwUamP_g1ltZxrHqEsZTrNJrPN8SKe");
//        googleService.addMediaToAlbum(Arrays.asList(uploadDetail));
//
//        String format = String.format("looking for album %s and found %s", albumName, collect.size());
//        return format;
//    }


//    public String gatherData(
////            @ShellOption(mandatory = true) String text,
////            @ShellOption(mandatory = true, defaultValue = "en_US") Locale from,
////            @ShellOption(mandatory = true) Locate to
//    ) {
//        // invoke service
//        return "hello";
//    }
}