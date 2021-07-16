package ro.jmind.photos.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class ExcelServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ExcelServiceTest.class);
    @Autowired
    private ExcelService excelService;
    @Value("${excelOutputImages}")
    private String outputImages;

    @Test
    public void readFileTest() {
        assertNotNull(outputImages);
        String filePath = "D:\\Users\\sebastian\\Documents\\excel\\oferta_email_format_xlsx (4).xlsx";
        try {
            excelService.processFile(filePath);
        } catch (IOException e) {
            logger.error("unable to parse file", e);
        }
    }

}
