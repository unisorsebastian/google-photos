package ro.jmind.photos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ro.jmind.photos.service.DataService;
import ro.jmind.photos.service.FileService;
import ro.jmind.photos.service.GoogleService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//@SpringBootTest
class PhotosApplicationTests {
    ObjectMapper objectMapper = new ObjectMapper();
    FileService fileService = new FileService(objectMapper);
    GoogleService googleService = null;
    DataService dataService = new DataService(objectMapper, fileService, null);

    @Test
    void contextLoads() {
        List<String> items = Arrays.asList(new String[]{"a", "b", "c", "d", "a"});
        List<String> foundItems = items.stream().filter(s -> {
            return "e".equals(s);
        }).collect(Collectors.toList());

        System.out.println("done");

    }


}
