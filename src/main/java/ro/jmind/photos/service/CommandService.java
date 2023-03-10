package ro.jmind.photos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CommandService {

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    private ExcelService excelService;
    private SummaryService summaryService;

    public CommandService(ExcelService excelService, SummaryService summaryService) {
        this.excelService = excelService;
        this.summaryService = summaryService;
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


    public void readExcelData(){
        logger.info("start processing excel file {}", "readExcelData");
        try {
            summaryService.readExcel();
        } catch (IOException e) {
            logger.error("some unhandled exception", e);
            throw new RuntimeException("some unhandled exception", e);
        }
        logger.info("done processing excel file {}", "readExcelData");
    }

}



