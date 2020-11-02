package ro.jmind.photos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibraryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.jmind.photos.factories.PhotosLibraryClientFactory;
import ro.jmind.photos.helpers.UIHelper;
import ro.jmind.photos.model.RunSettings;
import ro.jmind.photos.routes.MainRoute;
import ro.jmind.photos.service.DataService;
import ro.jmind.photos.service.GoogleService;

import java.util.List;
import java.util.Optional;

@Configuration
public class ApplicationConfiguration {

    @Value("${credentialFileLocation}")
    private String credentialFileLocation;
    @Value("${storedCredentials}")
    private String storedCredentials;

    @Autowired
    private DataService dataService;
    @Autowired
    private RunSettings runSettings;
    @Autowired
    private GoogleService googleService;

    @Bean
    public ObjectMapper createObjectMaper() {
        return new ObjectMapper();
    }

    @Bean
    public MainRoute createMainRoute() {
        return new MainRoute(dataService, googleService, runSettings);
    }

    @Bean
    public void UIHelper() {
        UIHelper.setUp();
    }

    @Bean
    public PhotosLibraryClient buildClient() {
        final List<String> REQUIRED_SCOPES =
                ImmutableList.of(
                        "https://www.googleapis.com/auth/photoslibrary.edit.appcreateddata",
                        "https://www.googleapis.com/auth/photoslibrary.readonly",
                        "https://www.googleapis.com/auth/photoslibrary.appendonly",
                        "https://www.googleapis.com/auth/photoslibrary",
                        "https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata",
                        "https://www.googleapis.com/auth/photoslibrary.sharing"
                );

        Optional<String> credentialsFile = Optional.empty();
//        credentialsFile = Optional.of("D:\\Users\\sebastian\\workspace\\credentials2019.json");
        credentialsFile = Optional.of(credentialFileLocation);

        try {
            PhotosLibraryClient client = PhotosLibraryClientFactory.createClient(credentialsFile.get(), storedCredentials, REQUIRED_SCOPES);
            return client;
        } catch (Exception e) {
            throw new RuntimeException("unable to get client");
        }
    }
}
