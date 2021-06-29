package ro.jmind.photos;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String... a) throws IOException {

        String FILE_NAME = "D:\\Users\\sebastian\\Documents\\excel\\oferta_email_format_xlsx (4).xlsx";
        FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Sheet sheet1 = workbook.getSheet("Camere IP HiWatch by Hikvision");

        List<? extends PictureData> allPictures = workbook.getAllPictures();
        Iterator<Row> iterator = datatypeSheet.iterator();

        XSSFDrawing dp = (XSSFDrawing) workbook.getSheet("ElectricVehicle chargers").createDrawingPatriarch();
        List<XSSFShape> pics = dp.getShapes();
        for (XSSFShape inpPics : pics) {
            XSSFPicture inpPic = (XSSFPicture) inpPics;
            XSSFClientAnchor clientAnchor = null;
            try {
                clientAnchor = inpPic.getClientAnchor();
                clientAnchor.getCol1();
            } catch (Exception e) {
                System.out.println("---");
                continue;
            }
            inpPic.getShapeName(); // узнаю название картинки
            PictureData pict = inpPic.getPictureData();
            FileOutputStream out = new FileOutputStream("D:\\Users\\sebastian\\Documents\\excel\\pict.jpg");
            byte[] data = pict.getData();
            out.write(data);
            out.close();
            System.out.println("col1: " + clientAnchor.getCol1() + ", col2: " + clientAnchor.getCol2() + ", row1: " + clientAnchor.getRow1() + ", row2: " + clientAnchor.getRow2());
            System.out.println("x1: " + clientAnchor.getDx1() + ", x2: " + clientAnchor.getDx2() + ", y1: " + clientAnchor.getDy1() + ", y2: " + clientAnchor.getDy2());

        }

        while (iterator.hasNext()) {

            Row currentRow = iterator.next();
            Iterator<Cell> cellIterator = currentRow.iterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getStringCellValue().isEmpty())
                    continue;
                System.out.println(cell);
            }

        }
    }
}
