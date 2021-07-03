package ro.jmind.photos.service;

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


    public void processFile(String excelFileName) throws IOException {
        if (!Paths.get(excelOutputImages).toFile().exists()) {
            logger.info("output directory is missing, trying to create");
            Files.createDirectories(Paths.get(excelOutputImages));
        }
        if (!Paths.get(excelOutputImages).toFile().exists()) {
            String message = String.format("unable to create path %s", excelOutputImages);
            logger.error(message);
            throw new RuntimeException(message);
        }

        final FileInputStream sourceFile = new FileInputStream(new File(excelFileName));
        final Workbook workbook = new XSSFWorkbook(sourceFile);
        List<ExcelOutputModel> excelOutputData = null;

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

        final String acceroriiSheet = "Accesorii_CCTV";
        excelOutputData = collectData(workbook, acceroriiSheet);
        writeExcelOutputModel(excelOutputData, acceroriiSheet);

    }

    // works for sheets ElectricVehicle chargers, Camere IP_MegaPixel Hikvision, FeverScreeningThermal, IP-NVR Hikvision, Camere IP HiWatch by Hikvision,
    // Sisteme_TurboHD_Hikvision, Camere TVI HiWatch by Hikvision, LPR&parking HIKVISION,
    // Sisteme_TURBO_VTX, VideoInterfoane Hikvision, Accesorii_CCTV
    private List<ExcelOutputModel> collectData(Workbook workbook, String sourceSheetName) throws IOException {
        List<ExcelOutputModel> excelOutputData = new ArrayList<>();

        Sheet sheet = workbook.getSheet(sourceSheetName);
        XSSFDrawing dp = (XSSFDrawing) sheet.createDrawingPatriarch();

        List<XSSFShape> shapes = dp.getShapes();
        for (XSSFShape shape : shapes) {
            XSSFPicture inpPic = null;
            try {
                inpPic = (XSSFPicture) shape;
            } catch (ClassCastException e) {
                //fails in Camere IP_MegaPixel Hikvision
                logger.info("unable to get picture, will skip it");
                continue;
            }

            XSSFClientAnchor clientAnchor = null;
            try {
                clientAnchor = inpPic.getClientAnchor();
                clientAnchor.getCol1();
            } catch (Exception e) {
                //fails in ElectricVehicle chargers
                logger.info("there is an exception but was handled");
                continue;
            }
            int rowLocation = clientAnchor.getRow1();
            short col1Location = clientAnchor.getCol1();
            //we don't need the picture if is not in colNo 1
            if (col1Location != 1 || clientAnchor.getCol2() != 1) {
                continue;
            }
            Cell cell = sheet.getRow(rowLocation).getCell(col1Location, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getStringCellValue() == null) {
                //fails in Camere IP_MegaPixel Hikvision
                logger.info("ignore cell at row {} and column {} in sheet {}", rowLocation, col1Location, sourceSheetName);
                continue;
            }
            Cell cellPrice = sheet.getRow(rowLocation).getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cellPrice == null) {
                continue;
            }
            String pictureName = cell.getStringCellValue().replace("/", "-").replace("\n", "").replace("\"", "");
            String pictureNameFormat = String.format("%s_r%sc%s_%s.jpeg", sourceSheetName.replace(" ", ""), rowLocation, col1Location, pictureName);
            inpPic.getShapeName();
            PictureData pict = inpPic.getPictureData();
            Path pictureParentPath = Paths.get(excelOutputImages, sourceSheetName);
            Files.createDirectories(pictureParentPath);
            Path pictureFilePath = Paths.get(pictureParentPath.toString(), pictureNameFormat);
            FileOutputStream out = new FileOutputStream(new File(pictureFilePath.toString()), true);
            byte[] data = pict.getData();
            out.write(data);
            out.close();
            ExcelOutputModel excelOutputModel = new ExcelOutputModel.ExcelOutputBuilder()
                    .setRow(String.valueOf(rowLocation))
                    .setUid(pictureName)
                    .setPictureLocation(excelImagesRelativePath + FILE_SYSTEM_PATH_SEPARATOR + sourceSheetName + FILE_SYSTEM_PATH_SEPARATOR + pictureNameFormat)
                    .setPrice(cellPrice)
                    .setDescription(sheet.getRow(rowLocation).getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))
                    .createExcelOutputModel();
            excelOutputData.add(excelOutputModel);
        }
        return excelOutputData;
    }

    private void writeExcelOutputModel(List<ExcelOutputModel> models, String sourceSheetName) throws IOException {
        XSSFWorkbook workbookOutput = new XSSFWorkbook();
        XSSFSheet outputSheet = workbookOutput.createSheet("sheet1");
        int rowCount = 0;
        XSSFRow header = outputSheet.createRow(rowCount);
        header.createCell(0).setCellValue("originalRowNo");
        header.createCell(1).setCellValue("uid");
        header.createCell(2).setCellValue("imageRelativePath");
        header.createCell(3).setCellValue("description");
        header.createCell(4).setCellValue("price");

        models.sort(Comparator.comparing(ExcelOutputModel::getRow));
        for (ExcelOutputModel model : models) {
            XSSFRow row = outputSheet.createRow(++rowCount);

            XSSFCell originalRowNoCell = row.createCell(0);
            XSSFCell uidCell = row.createCell(1);
            XSSFCell imageRelativePath = row.createCell(2);
            XSSFCell descriptionCell = row.createCell(3);
            XSSFCell priceCell = row.createCell(4);

            originalRowNoCell.setCellValue(model.getRow());
            uidCell.setCellValue(model.getUid());
            imageRelativePath.setCellValue(model.getPictureLocation());
            descriptionCell.setCellValue(model.getDescription().stream().collect(Collectors.joining("\n")));
            priceCell.setCellValue(model.getPrice());
        }

        try (FileOutputStream outputStream = new FileOutputStream(excelTargetPath + FILE_SYSTEM_PATH_SEPARATOR + sourceSheetName + ".xlsx")) {
            workbookOutput.write(outputStream);
        }
    }

}
