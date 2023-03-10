package ro.jmind.photos.service;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.jmind.photos.model.Invoice;
import ro.jmind.photos.model.NotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Map;

import static ro.jmind.photos.service.ExcelUtils.*;

@Service
public class SummaryService {
    public static final String MAIN_SHEET_NAME = "main";
    public static final String SERIES_JMD = "JMD %03.0f";
    public static final String FILE_SYSTEM_PATH_SEPARATOR = System.getProperty("file.separator");
    private static final Logger logger = LoggerFactory.getLogger(SummaryService.class);


    @Value("${excelBaseFile}")
    private String excelBaseFile;

    public SummaryService() {
    }

    public void createInvoice() throws IOException {

        Invoice newInvoice = Invoice.builder()
                .invoiceDate(LocalDate.of(2023, 3, 9))
                .vatRate(19)
                .template("vauban")
                .amount(27598.5)
                .build();

        final File sourceFile = new File(excelBaseFile);
        FileInputStream sourceFileStream = new FileInputStream(sourceFile);
        Workbook mainWorkbook = new XSSFWorkbook(sourceFileStream);

        mainWorkbook = updateMainSheet(mainWorkbook, newInvoice);
        writeExcelFile(sourceFile, mainWorkbook);

        sourceFileStream = new FileInputStream(sourceFile);
        mainWorkbook = new XSSFWorkbook(sourceFileStream);
        Row lastRowByColName = getLastRowByColName(mainWorkbook, "main", "invoiceId");
        int invoiceIdColIndex = getColumnIndex("invoiceId", mainWorkbook.getSheet("main"));
        int invoiceDateIndex = getColumnIndex("invoiceDate", mainWorkbook.getSheet("main"));
        //JMD 073
        String lastInvoiceId = lastRowByColName.getCell(invoiceIdColIndex).getStringCellValue();
        Invoice invoiceDataFromMainFile = getInvoiceData(mainWorkbook, lastInvoiceId);

        generateExcelInvoice(mainWorkbook, invoiceDataFromMainFile, "D:\\Users\\sebastian\\workspace\\");

    }

    public File generateExcelInvoice(Workbook mainWorkbook, Invoice invoiceData, String fileLocation) throws IOException {

        String templateName = invoiceData.getTemplate();
        File result = null;
        switch (templateName) {
            case "vauban": {
                String mm_yyyy = invoiceData.getInvoiceDate().format(DateTimeFormatter.ofPattern("MM_yyyy"));
                String fileName = String.format("%s_JMIND DEV_Factura.xlsx", mm_yyyy);

                File outputFile = Paths.get(fileLocation, fileName).toFile();

                removeOtherSheets(templateName, mainWorkbook);

                updateVaubanTemplate(mainWorkbook, invoiceData);

                writeExcelFile(outputFile, mainWorkbook);

                result = outputFile;
                break;
            }
            default:
                break;
        }


        return result;

    }

    public void updateVaubanTemplate(Workbook workbook, Invoice invoice) {
        Sheet sheet = workbook.getSheetAt(0);
        //series JMD
        sheet.getRow(2).getCell(3).setCellValue(invoice.getInvoiceId().split(" ")[0]);
        //number 072
        sheet.getRow(2).getCell(5).setCellValue(invoice.getInvoiceId().split(" ")[1]);
        // date
        sheet.getRow(4).getCell(3).setCellValue(invoice.getInvoiceDate());
        // Cota TVA
        String vatRateString = String.format("Cota T.V.A.: %1.0f%%", invoice.getVatRate());
        sheet.getRow(6).getCell(2).setCellValue(vatRateString);
        //perioada
        Locale ro = Locale.forLanguageTag("RO");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(ro);
        DateTimeFormatter customFormatter = new DateTimeFormatterBuilder()
                .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                .appendPattern(" uuuu")
                .toFormatter(ro);
        String formattedDate = invoice.getInvoiceDate().format(customFormatter);
        sheet.getRow(33).getCell(4).setCellValue(formattedDate.toUpperCase());
        //pret unitar
        double amount = invoice.getAmount();
        double vat = new BigDecimal(amount).multiply(new BigDecimal(invoice.getVatRate() / 100)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        double total = amount + vat;
        sheet.getRow(29).getCell(8).setCellValue(amount);
        //tva
        sheet.getRow(29).getCell(9).setCellValue(vat);
        //total valoare
        sheet.getRow(29).getCell(10).setCellValue(total);
        //total
        sheet.getRow(36).getCell(10).setCellValue(amount);
        //accize
        sheet.getRow(37).getCell(10).setCellValue(vat);
        //total plata
        sheet.getRow(38).getCell(10).setCellValue(total);
    }


    public void readExcel() throws IOException {


        createInvoice();


    }

    public Invoice getInvoiceData(final Workbook mainWorkbook, String invoiceId) {
        Row row = findByInvoiceId(mainWorkbook, "main", "invoiceId", invoiceId);
        Map<String, Integer> columnNameIndexMap = columnNameIndexMap(mainWorkbook, "main");

        LocalDate invoiceDate = row.getCell(columnNameIndexMap.get("invoiceDate")).getDateCellValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return Invoice.builder()
                .invoiceId(row.getCell(columnNameIndexMap.get("invoiceId")).getStringCellValue())
                .invoiceDate(invoiceDate)
                .vatRate(row.getCell(columnNameIndexMap.get("VATRate")).getNumericCellValue())
                .amount(row.getCell(columnNameIndexMap.get("amount")).getNumericCellValue())
                .template(row.getCell(columnNameIndexMap.get("template")).getStringCellValue())
                .build();
    }

    public Row getRefRow(final Workbook mainWorkbook) {
        return mainWorkbook.getSheet(MAIN_SHEET_NAME).getRow(1);
    }

    public Workbook updateMainSheet(final Workbook mainWorkbook, Invoice invoice) {

        Row nextAvailableRow = getNextAvailableRowByColName(mainWorkbook, MAIN_SHEET_NAME, "invoiceId");

        Map<String, Integer> columnNameIndexMap = columnNameIndexMap(mainWorkbook, MAIN_SHEET_NAME);
        boolean isReady = rowReady(nextAvailableRow);
        if (!isReady) {
            throw new NotFoundException("row is not ready");
        }

        String invoiceId = String.format(SERIES_JMD, nextAvailableRow.getCell(0).getNumericCellValue());


        createCell(nextAvailableRow, columnNameIndexMap, "invoiceId").setCellValue(invoiceId);
        createCell(nextAvailableRow, columnNameIndexMap, "template").setCellValue(invoice.getTemplate());
        createCell(nextAvailableRow, columnNameIndexMap, "VATRate").setCellValue(invoice.getVatRate());

        Cell amount = createCell(nextAvailableRow, columnNameIndexMap, "amount");
        amount.setCellValue(invoice.getAmount());
        CellStyle refStyle = getRefRow(mainWorkbook).getCell(columnNameIndexMap.get("amount")).getCellStyle();
        amount.setCellStyle(refStyle);

        Cell invoiceDate = createCell(nextAvailableRow, columnNameIndexMap, "invoiceDate");
        invoiceDate.setCellValue(invoice.getInvoiceDate());
        refStyle = getRefRow(mainWorkbook).getCell(columnNameIndexMap.get("invoiceDate")).getCellStyle();
        invoiceDate.setCellStyle(refStyle);


        return mainWorkbook;

    }


}
