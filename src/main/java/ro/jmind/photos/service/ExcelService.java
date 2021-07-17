package ro.jmind.photos.service;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.jmind.photos.model.ExcelOutputModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelService {
    public static final String FILE_SYSTEM_PATH_SEPARATOR = System.getProperty("file.separator");
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    @Value("${excelOutputImages}")
    private String excelOutputImages;

    @Value("${excelTargetPath}")
    private String excelTargetPath;

    @Value("${excelImagesRelativePath}")
    private String excelImagesRelativePath;

    public ExcelService() {
    }

    public void processFile(String excelFileName) throws IOException {
        logger.info("writing images to base directory {}", excelOutputImages);
        if (!Paths.get(excelOutputImages).toFile().exists()) {
            logger.info("output directory '{}' is missing, trying to create", excelOutputImages);
            Path directories = Files.createDirectories(Paths.get(excelOutputImages));
            logger.info("output directory '{}' created", directories);
        }
        if (!Paths.get(excelOutputImages).toFile().exists()) {
            String message = String.format("unable to create path %s", excelOutputImages);
            logger.error(message);
            throw new RuntimeException(message);
        }

        final FileInputStream sourceFile = new FileInputStream(new File(excelFileName));
        final Workbook workbook = new XSSFWorkbook(sourceFile);
        List<ExcelOutputModel> excelOutputData = null;
        List<ExcelOutputModel> dataWithoutPictures = null;
        List<ExcelOutputModel> enrichedData = null;

        final String electricVehicleSheet = "ElectricVehicle chargers";
        excelOutputData = collectData(workbook, electricVehicleSheet);
        writeExcelOutputModel(excelOutputData, electricVehicleSheet);

        final String ipCameraSheet = "Camere IP_MegaPixel Hikvision";
        excelOutputData = collectData(workbook, ipCameraSheet);
        writeExcelOutputModel(excelOutputData, ipCameraSheet);

        final String feverScreeningSheet = "FeverScreeningThermal";
        excelOutputData = collectData(workbook, feverScreeningSheet);
        writeExcelOutputModel(excelOutputData, feverScreeningSheet);

        final String idDvrSheet = "IP-NVR Hikvision";
        excelOutputData = collectData(workbook, idDvrSheet);
        writeExcelOutputModel(excelOutputData, idDvrSheet);

        final String ipCameraHiWatchSheet = "Camere IP HiWatch by Hikvision";
        excelOutputData = collectData(workbook, ipCameraHiWatchSheet);
        writeExcelOutputModel(excelOutputData, ipCameraHiWatchSheet);

        final String sistemeTurboSheet = "Sisteme_TurboHD_Hikvision";
        excelOutputData = collectData(workbook, sistemeTurboSheet);
        writeExcelOutputModel(excelOutputData, sistemeTurboSheet);

        final String camereTVISheet = "Camere TVI HiWatch by Hikvision";
        excelOutputData = collectData(workbook, camereTVISheet);
        writeExcelOutputModel(excelOutputData, camereTVISheet);

        final String parkingSheet = "LPR&parking HIKVISION";
        excelOutputData = collectData(workbook, parkingSheet);
        writeExcelOutputModel(excelOutputData, parkingSheet);

        final String turboVtxSheet = "Sisteme_TURBO_VTX";
        excelOutputData = collectData(workbook, turboVtxSheet);
        writeExcelOutputModel(excelOutputData, turboVtxSheet);

        final String interfonSheet = "VideoInterfoane Hikvision";
        excelOutputData = collectData(workbook, interfonSheet);
        writeExcelOutputModel(excelOutputData, interfonSheet);

        final String accesoriiSheet = "Accesorii_CCTV";
        excelOutputData = collectData(workbook, accesoriiSheet);
        writeExcelOutputModel(excelOutputData, accesoriiSheet);

        final String naAnalogiceConventionaleSheet = "NA_analogice_conventionale";
        excelOutputData = collectData(workbook, naAnalogiceConventionaleSheet);
        writeExcelOutputModel(excelOutputData, naAnalogiceConventionaleSheet);

        final String gsmBodyCameraHikvision = "GSM Body Camera Hikvision";
        excelOutputData = collectData(workbook, gsmBodyCameraHikvision);
        writeExcelOutputModel(excelOutputData, gsmBodyCameraHikvision);

        final String smartHomeSolutions = "SmartHome solutions";
        excelOutputData = collectData(workbook, smartHomeSolutions);
        writeExcelOutputModel(excelOutputData, smartHomeSolutions);

        final String incendiuAdresabilGlobalFire = "Incendiu_Adresabil_GLOBAL_FIRE_";
        excelOutputData = collectData(workbook, incendiuAdresabilGlobalFire);
        writeExcelOutputModel(excelOutputData, incendiuAdresabilGlobalFire);

        final String incendiuAdresabilTeletek = "Incendiu_adresabil_Teletek";
        excelOutputData = collectData(workbook, incendiuAdresabilTeletek);
        writeExcelOutputModel(excelOutputData, incendiuAdresabilTeletek);


        final String incendiuBentel = "Incendiu Bentel adr si conv";
        excelOutputData = collectData(workbook, incendiuBentel);
        dataWithoutPictures = collectDataWithoutPictures(workbook, incendiuBentel);
        enrichedData = enrichData(dataWithoutPictures, excelOutputData);
        writeExcelOutputModel(enrichedData, incendiuBentel);

        final String incendiuConventional = "Incendiu_conventional";
        excelOutputData = collectData(workbook, incendiuConventional);
        writeExcelOutputModel(excelOutputData, incendiuConventional);

        final String surseAlimentareEN54 = "Surse alimentare EN54";
        excelOutputData = collectData(workbook, surseAlimentareEN54);
        dataWithoutPictures = collectDataWithoutPictures(workbook, surseAlimentareEN54);
        enrichedData = enrichData(dataWithoutPictures, excelOutputData);
        writeExcelOutputModel(enrichedData, surseAlimentareEN54);


        final String pyronixHikvision = "Pyronix-Hikvision";
        excelOutputData = collectData(workbook, pyronixHikvision);
        writeExcelOutputModel(excelOutputData, pyronixHikvision);

        final String teletek = "TELETEK";
        excelOutputData = collectData(workbook, teletek);
        writeExcelOutputModel(excelOutputData, teletek);

        final String teletekWireless = "Teletek Wireless";
        excelOutputData = collectData(workbook, teletekWireless);
        writeExcelOutputModel(excelOutputData, teletekWireless);

        final String eldesAlarms = "ELDES alarms";
        excelOutputData = collectData(workbook, eldesAlarms);
        writeExcelOutputModel(excelOutputData, eldesAlarms);

        final String opex = "OPTEX";
        excelOutputData = collectData(workbook, opex);
        writeExcelOutputModel(excelOutputData, opex);

        final String accesoriiAlarme = "Accesorii_alarme";
        excelOutputData = collectData(workbook, accesoriiAlarme);
        dataWithoutPictures = collectDataWithoutPictures(workbook, accesoriiAlarme);
        enrichedData = enrichData(dataWithoutPictures, excelOutputData);
        writeExcelOutputModel(enrichedData, accesoriiAlarme);

        final String controlAccess = "Control_Acces";
        excelOutputData = collectData(workbook, controlAccess);
        dataWithoutPictures = collectDataWithoutPictures(workbook, controlAccess);
        enrichedData = enrichData(dataWithoutPictures, excelOutputData);
        writeExcelOutputModel(enrichedData, controlAccess);

        final String publicAddressSonorizare = "PUBLIC_ADDRESS_Sonorizare";
        excelOutputData = collectData(workbook, publicAddressSonorizare);
        dataWithoutPictures = collectDataWithoutPictures(workbook, publicAddressSonorizare);
        enrichedData = enrichData(dataWithoutPictures, excelOutputData);
        writeExcelOutputModel(enrichedData, publicAddressSonorizare);
    }

    public List<ExcelOutputModel> enrichData(List<ExcelOutputModel> dataWithoutPictures, List<ExcelOutputModel> dataWithPictures) {
        List<ExcelOutputModel> result = new ArrayList<>();
        result.addAll(dataWithPictures);

        dataWithoutPictures.forEach(e -> {
            Integer row = e.getRow();
            boolean rowFoundInPictureData = result.stream().
                    filter(o -> o.getRow().equals(row))
                    .findAny()
                    .isPresent();
            if (!rowFoundInPictureData) {
                result.add(e);
            }
        });
        return result;
    }

    public List<ExcelOutputModel> collectDataWithoutPictures(Workbook workbook, String sourceSheetName) {
        List<ExcelOutputModel> data = new ArrayList<>();
        Sheet sheet = workbook.getSheet(sourceSheetName);
        for (Row row : sheet) {
            int rowIndex = row.getRowNum();
            if (rowIndex < 1)
                continue;
            Cell descCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell uidCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell cellPrice = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            String descriptionCellValue = descCell.getStringCellValue();
            if (descriptionCellValue.length() < 3)
                continue;
            if (cellPrice == null)
                continue;

            ExcelOutputModel dataWithoutPictures = new ExcelOutputModel.ExcelOutputBuilder()
                    .setStringDescription(descriptionCellValue)
                    .setRow(String.valueOf(rowIndex))
                    .setUid(uidCell.getStringCellValue())
                    .setPrice(cellPrice)
                    .createExcelOutputModel();

            data.add(dataWithoutPictures);
        }
        return data;
    }

    public List<ExcelOutputModel> collectData(Workbook workbook, String sourceSheetName) throws IOException {
        List<ExcelOutputModel> excelOutputData = new ArrayList<>();

        Path pictureParentPath = Paths.get(excelOutputImages, sourceSheetName);
        FileUtils.deleteDirectory(new File(pictureParentPath.toUri()));
        Files.createDirectories(pictureParentPath);

        Sheet sheet = workbook.getSheet(sourceSheetName);
        XSSFDrawing dp = (XSSFDrawing) sheet.createDrawingPatriarch();

        List<XSSFShape> shapes = dp.getShapes();
        for (XSSFShape shape : shapes) {
            XSSFPicture picture = null;
            try {
                picture = (XSSFPicture) shape;
            } catch (ClassCastException e) {
                //fails in Camere IP_MegaPixel Hikvision
                logger.trace("unable to get picture, will skip it");
                continue;
            }
            if (picture == null)
                continue;

            XSSFClientAnchor clientAnchor = null;
            try {
                clientAnchor = picture.getClientAnchor();
                if (clientAnchor == null)
                    continue;
                clientAnchor.getCol1();
            } catch (Exception e) {
                //fails in ElectricVehicle chargers
                logger.trace("there is an exception but was handled", e);
                continue;
            }

            //picture coordinates
            int rowPictureIndex = clientAnchor.getRow1();
            short colPictureIndex = clientAnchor.getCol1();
            short col2PictureIndex = clientAnchor.getCol2();
            //we don't need the picture if is not in colNo 1
            if (colPictureIndex != 1 || col2PictureIndex != 1) {
                continue;
            }

            Cell cellPicture = sheet.getRow(rowPictureIndex).getCell(colPictureIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            //skip pictures without cell text
            if (cellPicture == null || cellPicture.getStringCellValue() == null) {
                // skip because fails in Camere IP_MegaPixel Hikvision - pics without text in cell
                logger.info("ignore picture because no description at rowIndex {} and collIndex {} in sheet {}", rowPictureIndex, colPictureIndex, sourceSheetName);
                continue;
            }
            Cell cellPrice = sheet.getRow(rowPictureIndex).getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cellPrice == null) {
                continue;
            }
            String pictureNameTarget = replaceSpecificPathChars(cellPicture);
            String pictureNameTargetFormat = String.format("%s_r%sc%s_%s.jpeg",
                    sourceSheetName.replace(" ", ""),
                    rowPictureIndex, colPictureIndex, pictureNameTarget);
            PictureData pictureData = picture.getPictureData();


            Path pictureFilePath = Paths.get(pictureParentPath.toString(), pictureNameTargetFormat);
            if (Files.exists(pictureFilePath)) {
                logger.info("there are multiple pictures at {}, picture overwrite {}", cellPicture.getAddress(), pictureFilePath);
            }
            File pictureOutputFile = new File(pictureFilePath.toString());
            FileOutputStream out = new FileOutputStream(pictureOutputFile, true);
            byte[] data = pictureData.getData();
            out.write(data);
            out.close();

            Cell descriptionCell = sheet.getRow(rowPictureIndex).getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            String pictureLocation = excelImagesRelativePath + FILE_SYSTEM_PATH_SEPARATOR + sourceSheetName + FILE_SYSTEM_PATH_SEPARATOR + pictureNameTargetFormat;
            ExcelOutputModel excelOutputModel = new ExcelOutputModel.ExcelOutputBuilder()
                    .setRow(String.valueOf(rowPictureIndex))
                    .setUid(pictureNameTarget)
                    .setPictureLocation(pictureLocation)
                    .setPictureLocalLocation(pictureOutputFile.getAbsolutePath())
                    .setPrice(cellPrice)
                    .setDescription(descriptionCell)
                    .createExcelOutputModel();
            excelOutputData.add(excelOutputModel);
        }
        return excelOutputData;
    }

    public String replaceSpecificPathChars(Cell cellPicture) {
        return cellPicture.getStringCellValue().
                replace("/", "-")
                .replace("\\", "-")
                .replace("\n", "")
                .replace("\"", "");
    }


    private void writeExcelOutputModel(List<ExcelOutputModel> models, String sourceSheetName) throws IOException {
        XSSFWorkbook workbookOutput = new XSSFWorkbook();
        XSSFSheet outputSheet = workbookOutput.createSheet("sheet1");
        int rowCount = 0;
        XSSFRow header = outputSheet.createRow(rowCount);
        header.createCell(0).setCellValue("originalRowNo");
        header.createCell(1).setCellValue("uid");
        header.createCell(2).setCellValue("imageRelativePath");
        header.createCell(3).setCellValue("imageLocalPath");
        header.createCell(4).setCellValue("description");
        header.createCell(5).setCellValue("price");

        models.sort(Comparator.comparing(ExcelOutputModel::getRow));
        for (ExcelOutputModel model : models) {
            XSSFRow row = outputSheet.createRow(++rowCount);

            XSSFCell originalRowNoCell = row.createCell(0);
            XSSFCell uidCell = row.createCell(1);
            XSSFCell imageRelativePath = row.createCell(2);
            XSSFCell imageLocalPath = row.createCell(3);
            XSSFCell descriptionCell = row.createCell(4);
            XSSFCell priceCell = row.createCell(5);

            originalRowNoCell.setCellValue(model.getRow());
            uidCell.setCellValue(model.getUid());
            imageRelativePath.setCellValue(model.getPictureLocation());
            imageLocalPath.setCellValue(model.getPictureLocalLocation());
            descriptionCell.setCellValue(model.getDescription().stream().collect(Collectors.joining("\n")));
            priceCell.setCellValue(model.getPrice());
        }

        try (FileOutputStream outputStream = new FileOutputStream(excelTargetPath + FILE_SYSTEM_PATH_SEPARATOR + sourceSheetName + ".xlsx")) {
            workbookOutput.write(outputStream);
        }
    }

}
