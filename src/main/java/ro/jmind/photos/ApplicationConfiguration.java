package ro.jmind.photos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.jmind.photos.model.RunSettings;

@Configuration
public class ApplicationConfiguration {

    @Value("${credentialFileLocation}")
    private String credentialFileLocation;
    @Value("${storedCredentials}")
    private String storedCredentials;


    @Autowired
    private RunSettings runSettings;

    @Bean
    public ObjectMapper createObjectMaper() {
        return new ObjectMapper();
    }


}
