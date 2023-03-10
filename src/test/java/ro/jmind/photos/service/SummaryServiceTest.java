package ro.jmind.photos.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ro.jmind.photos.ApplicationConfiguration;
import ro.jmind.photos.ApplicationTestConfiguration;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {ApplicationConfiguration.class, ApplicationTestConfiguration.class})
public class SummaryServiceTest {

    @Autowired
    private SummaryService service;

    @Test
    public void readFileTest() throws IOException {


        service.readExcel();
        assertNotNull(service);

    }

}
