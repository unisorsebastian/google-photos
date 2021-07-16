package ro.jmind.photos.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ExcelServiceTest {
    private ExcelService excelService;
    private String outputImages;

    @Test
    public void readFileTest(){
        assertNotNull(outputImages);
//        excelService.processFile();
    }

}
