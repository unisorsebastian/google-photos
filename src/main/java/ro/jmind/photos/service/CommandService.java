package ro.jmind.photos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    @Value("${parentDataLocation}")
    private String parentDataLocation;
    @Value("${dataToUploadLocation}")
    private String dataToUploadLocation;
    @Value("${uploadDataLocation}")
    private String uploadDataLocation;
    @Value("${createMediaLocation}")
    private String createMediaLocation;
    @Value("${missingAlbumDataLocation}")
    private String missingAlbumDataLocation;
    @Value("${auditDataLocation}")
    private String auditDataLocation;


    private ExcelService excelService;


    public CommandService(ExcelService excelService) {

        this.excelService = excelService;
    }

    public void processFile(String excelFileName) {
        logger.info("start processing excel file {}", excelFileName);
        try {
            excelService.processFile(excelFileName);
        } catch (IOException e) {
            logger.error("some unhandled exception", e);
            throw new RuntimeException("some unhandled exception", e);
        }
        logger.info("done processing excel file {} {}", excelFileName, excelService);
    }


}



