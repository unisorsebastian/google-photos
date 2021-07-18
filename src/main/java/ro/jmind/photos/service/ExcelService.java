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
import java.util.function.Predicate;
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
        List<ExcelOutputModel> excelPictureData = null;
        List<ExcelOutputModel> excelStringData = null;
        List<ExcelOutputModel> enrichedData = null;

//        final String electricVehicleSheet = "ElectricVehicle chargers";
//        excelPictureData = collectPictureData(workbook, electricVehicleSheet);
//        excelStringData = collectStringData(workbook, electricVehicleSheet);
//        enrichedData = enrichPictureData(excelStringData, excelPictureData);
//        writeExcelOutputModel(enrichedData, electricVehicleSheet);

        final String ipCameraSheet = "Camere IP_MegaPixel Hikvision";
        Path baseDirectoryPath = Paths.get(excelOutputImages, replaceSpecificPathChars(ipCameraSheet));
        excelPictureData = collectPictureData(workbook, ipCameraSheet);
        excelStringData = collectStringData(workbook, ipCameraSheet);
        enrichedData = enrichPictureData(excelStringData, excelPictureData);
        writePictureFile(baseDirectoryPath, enrichedData);
        writeExcelOutputModel(enrichedData, ipCameraSheet);

//        final String feverScreeningSheet = "FeverScreeningThermal";
//        excelPictureData = collectPictureData(workbook, feverScreeningSheet);
//        writeExcelOutputModel(excelPictureData, feverScreeningSheet);
//
//        final String idDvrSheet = "IP-NVR Hikvision";
//        excelPictureData = collectPictureData(workbook, idDvrSheet);
//        writeExcelOutputModel(excelPictureData, idDvrSheet);
//
//        final String ipCameraHiWatchSheet = "Camere IP HiWatch by Hikvision";
//        excelPictureData = collectPictureData(workbook, ipCameraHiWatchSheet);
//        writeExcelOutputModel(excelPictureData, ipCameraHiWatchSheet);
//
//        final String sistemeTurboSheet = "Sisteme_TurboHD_Hikvision";
//        excelPictureData = collectPictureData(workbook, sistemeTurboSheet);
//        writeExcelOutputModel(excelPictureData, sistemeTurboSheet);
//
//        final String camereTVISheet = "Camere TVI HiWatch by Hikvision";
//        excelPictureData = collectPictureData(workbook, camereTVISheet);
//        writeExcelOutputModel(excelPictureData, camereTVISheet);
//
//        final String parkingSheet = "LPR&parking HIKVISION";
//        excelPictureData = collectPictureData(workbook, parkingSheet);
//        writeExcelOutputModel(excelPictureData, parkingSheet);
//
//        final String turboVtxSheet = "Sisteme_TURBO_VTX";
//        excelPictureData = collectPictureData(workbook, turboVtxSheet);
//        writeExcelOutputModel(excelPictureData, turboVtxSheet);
//
//        final String interfonSheet = "VideoInterfoane Hikvision";
//        excelPictureData = collectPictureData(workbook, interfonSheet);
//        writeExcelOutputModel(excelPictureData, interfonSheet);
//
//        final String accesoriiSheet = "Accesorii_CCTV";
//        excelPictureData = collectPictureData(workbook, accesoriiSheet);
//        writeExcelOutputModel(excelPictureData, accesoriiSheet);
//
//        final String naAnalogiceConventionaleSheet = "NA_analogice_conventionale";
//        excelPictureData = collectPictureData(workbook, naAnalogiceConventionaleSheet);
//        writeExcelOutputModel(excelPictureData, naAnalogiceConventionaleSheet);
//
//        final String gsmBodyCameraHikvision = "GSM Body Camera Hikvision";
//        excelPictureData = collectPictureData(workbook, gsmBodyCameraHikvision);
//        writeExcelOutputModel(excelPictureData, gsmBodyCameraHikvision);
//
//        final String smartHomeSolutions = "SmartHome solutions";
//        excelPictureData = collectPictureData(workbook, smartHomeSolutions);
//        writeExcelOutputModel(excelPictureData, smartHomeSolutions);
//
//        final String incendiuAdresabilGlobalFire = "Incendiu_Adresabil_GLOBAL_FIRE_";
//        excelPictureData = collectPictureData(workbook, incendiuAdresabilGlobalFire);
//        writeExcelOutputModel(excelPictureData, incendiuAdresabilGlobalFire);
//
//        final String incendiuAdresabilTeletek = "Incendiu_adresabil_Teletek";
//        excelPictureData = collectPictureData(workbook, incendiuAdresabilTeletek);
//        writeExcelOutputModel(excelPictureData, incendiuAdresabilTeletek);
//
//
//        final String incendiuBentel = "Incendiu Bentel adr si conv";
//        excelPictureData = collectPictureData(workbook, incendiuBentel);
//        excelStringData = collectStringData(workbook, incendiuBentel);
//        enrichedData = enrichPictureData(excelStringData, excelPictureData);
//        writeExcelOutputModel(enrichedData, incendiuBentel);
//
//        final String incendiuConventional = "Incendiu_conventional";
//        excelPictureData = collectPictureData(workbook, incendiuConventional);
//        writeExcelOutputModel(excelPictureData, incendiuConventional);
//
//        final String surseAlimentareEN54 = "Surse alimentare EN54";
//        excelPictureData = collectPictureData(workbook, surseAlimentareEN54);
//        excelStringData = collectStringData(workbook, surseAlimentareEN54);
//        enrichedData = enrichPictureData(excelStringData, excelPictureData);
//        writeExcelOutputModel(enrichedData, surseAlimentareEN54);
//
//
//        final String pyronixHikvision = "Pyronix-Hikvision";
//        excelPictureData = collectPictureData(workbook, pyronixHikvision);
//        writeExcelOutputModel(excelPictureData, pyronixHikvision);
//
//        final String teletek = "TELETEK";
//        excelPictureData = collectPictureData(workbook, teletek);
//        writeExcelOutputModel(excelPictureData, teletek);
//
//        final String teletekWireless = "Teletek Wireless";
//        excelPictureData = collectPictureData(workbook, teletekWireless);
//        writeExcelOutputModel(excelPictureData, teletekWireless);
//
//        final String eldesAlarms = "ELDES alarms";
//        excelPictureData = collectPictureData(workbook, eldesAlarms);
//        writeExcelOutputModel(excelPictureData, eldesAlarms);
//
//        final String opex = "OPTEX";
//        excelPictureData = collectPictureData(workbook, opex);
//        writeExcelOutputModel(excelPictureData, opex);
//
//        final String accesoriiAlarme = "Accesorii_alarme";
//        excelPictureData = collectPictureData(workbook, accesoriiAlarme);
//        excelStringData = collectStringData(workbook, accesoriiAlarme);
//        enrichedData = enrichPictureData(excelStringData, excelPictureData);
//        writeExcelOutputModel(enrichedData, accesoriiAlarme);
//
//        final String controlAccess = "Control_Acces";
//        excelPictureData = collectPictureData(workbook, controlAccess);
//        excelStringData = collectStringData(workbook, controlAccess);
//        enrichedData = enrichPictureData(excelStringData, excelPictureData);
//        writeExcelOutputModel(enrichedData, controlAccess);
//
//        final String publicAddressSonorizare = "PUBLIC_ADDRESS_Sonorizare";
//        excelPictureData = collectPictureData(workbook, publicAddressSonorizare);
//        excelStringData = collectStringData(workbook, publicAddressSonorizare);
//        enrichedData = enrichPictureData(excelStringData, excelPictureData);
//        writeExcelOutputModel(enrichedData, publicAddressSonorizare);
    }

    private void writePictureFile(Path imageDirectoryPath, List<ExcelOutputModel> enrichedData) {
        //writing images
        enrichedData.stream()
                .filter(e -> e.getImageAsBytes() != null)
                .forEach(e -> {
                    String pictureNameTarget = String.format("r%s_%s.jpeg", e.getRow(), replaceSpecificPathChars(e.getUid()));
                    Path pictureOutputFilePath = Paths.get(imageDirectoryPath.toString(), pictureNameTarget);
                    File pictureOutputFile = new File(pictureOutputFilePath.toString());
                    if (Files.exists(pictureOutputFilePath)) {
//                logger.info("there are multiple pictures at {}, picture overwrite {}", cellPicture.getAddress(), pictureFilePath);
                        logger.info("there are multiple pictures for row index {}, in directory {}", e.getRow(), pictureOutputFilePath);
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(pictureOutputFile, true);
                        out.write(e.getImageAsBytes());
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });


    }

    public List<ExcelOutputModel> enrichPictureData(List<ExcelOutputModel> dataWithoutPictures, List<ExcelOutputModel> dataWithPictures) {
        List<ExcelOutputModel> result = new ArrayList<>();
        Predicate<ExcelOutputModel> missingRow = d -> d.getRow() == null || d.getRow().longValue() == 0;
        Predicate<ExcelOutputModel> missingPictureData = d -> d.getImageAsBytes() == null || d.getImageAsBytes().length <= 1;
        boolean hasMissingRowInfo = dataWithPictures.stream().filter(missingRow).findAny().isPresent();
        boolean hasMissingPictureData = dataWithPictures.stream().filter(missingPictureData).findAny().isPresent();
        if (hasMissingRowInfo || hasMissingPictureData) {
            logger.error("missing dataWithPictures {}", missingPictureData);
            throw new RuntimeException("unexpected missing picture data");
        }

        dataWithoutPictures.forEach(e -> {
            Integer row = e.getRow();
            ExcelOutputModel data = null;
            //TODO
            String pictureNameTarget = String.format("r%s_%s.jpeg", row, replaceSpecificPathChars(e.getUid()));
            List<ExcelOutputModel> foundPictureData = dataWithPictures.stream()
                    .filter(o -> o.getRow().equals(row))
                    .collect(Collectors.toList());
            if (foundPictureData != null && foundPictureData.size() > 0) {
                data = new ExcelOutputModel.ExcelOutputBuilder()
                        .setImageAsBytes(foundPictureData.get(foundPictureData.size() - 1).getImageAsBytes())
                        .setPictureLocalLocation(pictureNameTarget)
                        .setRow(e.getRow().toString())
                        .setUid(e.getUid())
                        .setDescription(e.getDescription())
                        .setPrice(e.getPrice())
                        .createExcelOutputModel();
            } else {
                data = new ExcelOutputModel.ExcelOutputBuilder()
                        .setImageAsBytes(null)
                        .setPictureLocalLocation(pictureNameTarget)
                        .setRow(e.getRow().toString())
                        .setUid(e.getUid())
                        .setDescription(e.getDescription())
                        .setPrice(e.getPrice())
                        .createExcelOutputModel();
                logger.info("no pic data found at row index {} for {}", data.getRow(), data.getPictureLocalLocation());
            }

            result.add(data);
        });
        return result;
    }

    public List<ExcelOutputModel> collectStringData(Workbook workbook, String sourceSheetName) {
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
//                    .setPictureLocation(pictureLocation)
//                    .setPictureLocalLocation(pictureOutputFile.getAbsolutePath())
                    .createExcelOutputModel();

            data.add(dataWithoutPictures);
        }
        return data;
    }

    public List<ExcelOutputModel> collectPictureData(Workbook workbook, String sourceSheetName) throws IOException {
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
            //TODO skip records without price?
            Cell cellPrice = sheet.getRow(rowPictureIndex).getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cellPrice == null) {
                continue;
            }
//            String pictureNameTarget = replaceSpecificPathChars(cellPicture);
//            String pictureNameTargetFormat = String.format("%s_r%sc%s_%s.jpeg",
//                    sourceSheetName.replace(" ", ""),
//                    rowPictureIndex, colPictureIndex, pictureNameTarget);
            PictureData pictureData = picture.getPictureData();


//            Path pictureFilePath = Paths.get(pictureParentPath.toString(), pictureNameTargetFormat);
//            File pictureOutputFile = new File(pictureFilePath.toString());

            //writing images
//            if (Files.exists(pictureFilePath)) {
//                logger.info("there are multiple pictures at {}, picture overwrite {}", cellPicture.getAddress(), pictureFilePath);
//            }
//            FileOutputStream out = new FileOutputStream(pictureOutputFile, true);
//            byte[] data = pictureData.getData();
//            out.write(data);
//            out.close();

//            Cell descriptionCell = sheet.getRow(rowPictureIndex).getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
//            String pictureLocation = excelImagesRelativePath + FILE_SYSTEM_PATH_SEPARATOR + sourceSheetName + FILE_SYSTEM_PATH_SEPARATOR + pictureNameTargetFormat;
            ExcelOutputModel excelOutputModel = new ExcelOutputModel.ExcelOutputBuilder()
                    .setRow(String.valueOf(rowPictureIndex))
                    .setImageAsBytes(pictureData.getData())
//                    .setUid(pictureNameTarget)
//                    .setPictureLocation(pictureLocation)
//                    .setPictureLocalLocation(pictureOutputFile.getAbsolutePath())
//                    .setPrice(cellPrice)
//                    .setDescription(descriptionCell)
                    .createExcelOutputModel();
            excelOutputData.add(excelOutputModel);
        }
        return excelOutputData;
    }

    public String replaceSpecificPathChars(Cell cellPicture) {
        return replaceSpecificPathChars(cellPicture.getStringCellValue());
    }

    public String replaceSpecificPathChars(String pictureName) {
        return pictureName.
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
