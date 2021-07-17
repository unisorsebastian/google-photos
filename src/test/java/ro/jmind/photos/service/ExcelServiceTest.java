package ro.jmind.photos.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ro.jmind.photos.ApplicationConfiguration;
import ro.jmind.photos.ApplicationTestConfiguration;
import ro.jmind.photos.model.ExcelOutputModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
    @IfProfileValue(name = "spring.profiles.active", value = "integration")
    public void readFileTest() {
        assertNotNull(outputImages);
        String filePath = "D:\\Users\\sebastian\\Documents\\excel\\oferta_email_format_xlsx (4).xlsx";
        try {
            excelService.processFile(filePath);
        } catch (IOException e) {
            logger.error("unable to parse file", e);
        }
    }

    @Test
    @IfProfileValue(name = "spring.profiles.active", value = "integration")
    public void enhanceData() throws IOException {
        String filePath = "D:\\Users\\sebastian\\Documents\\excel\\oferta_email_format_xlsx (4).xlsx";
        String sheetName = "Accesorii_alarme";
        final FileInputStream sourceFile = new FileInputStream(new File(filePath));
        final Workbook workbook = new XSSFWorkbook(sourceFile);

        List<ExcelOutputModel> dataWithPictures = excelService.collectData(workbook, sheetName);

        List<ExcelOutputModel> dataWithoutPictures = excelService.collectDataWithoutPictures(workbook, sheetName);
        assertTrue(dataWithoutPictures.size() >= dataWithPictures.size());

        Optional<String> descFromPictureData = dataWithPictures.stream()
                .filter(m -> m.getRow() == 97)
                .map(m -> m.getDescription().stream().collect(Collectors.joining("\n")))
                .findFirst();
        Optional<String> descFromData = dataWithoutPictures.stream()
                .filter(m -> m.getRow() == 97)
                .map(m -> m.getDescription().stream().collect(Collectors.joining("\n")))
                .findFirst();
        assertEquals(descFromPictureData, descFromData);
        List<ExcelOutputModel> excelOutputModels = excelService.enrichData(dataWithoutPictures, dataWithPictures);
        assertTrue(excelOutputModels.size() >= dataWithoutPictures.size());
        assertTrue(excelOutputModels.size() >= dataWithPictures.size());
    }

}
