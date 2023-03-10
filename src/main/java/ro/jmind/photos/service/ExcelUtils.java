package ro.jmind.photos.service;

import org.apache.poi.ss.usermodel.*;
import ro.jmind.photos.model.NotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ExcelUtils {


    public static Cell createCell(Row nextAvailableRow, Map<String, Integer> columnNameIndexMap, String colName) {
        Integer index = columnNameIndexMap.get(colName);
        Cell cell = nextAvailableRow.getCell(index);
        if (Objects.isNull(cell)) {
            nextAvailableRow.createCell(index);
            cell = nextAvailableRow.getCell(index);
        }
        return cell;
    }

    public static boolean rowReady(Row row) {
        if (row.getCell(0).getNumericCellValue() < 1) {
            throw new NotFoundException("counter not incremented");
        }
        for (int i = 1; i < 4; i++) {
            Cell cell = row.getCell(i);
            if (!Objects.isNull(cell) && !cell.getStringCellValue().isEmpty())
                throw new NotFoundException("invalid row " + row.getRowNum());

        }
        return true;
    }

    public static boolean isNullOrEmpty(String s) {
        return null == s || s.isEmpty();
    }


    public static Map<String, Integer> columnNameIndexMap(Workbook workbook, String sheetName) {
        Map<String, Integer> result = new TreeMap<>();
        final Sheet sheet = workbook.getSheet(sheetName);
        Iterator<Cell> it = sheet.getRow(0).cellIterator();
        while (it.hasNext()) {
            Cell next = it.next();
            result.put(next.getStringCellValue(), next.getColumnIndex());
        }
        return result;
    }


    public static Row getNextAvailableRowByColName(Workbook workbook, String sheetName, String colName) {
        final Sheet mainSheet = workbook.getSheet(sheetName);
        int invoiceIdColIndex = getColumnIndex(colName, mainSheet);
        Iterator<Row> rowIterator = mainSheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row next = rowIterator.next();
            Cell cell = next.getCell(invoiceIdColIndex);
            if (Objects.isNull(cell)) {
                return next;
            }
            String stringCellValue = cell.getStringCellValue();
            if (isNullOrEmpty(stringCellValue)) {
                return cell.getRow();
            }
        }

        throw new NotFoundException("Next available row not found");
    }

    public static Row getLastRowByColName(Workbook workbook, String sheetName, String colName) {
        final Sheet mainSheet = workbook.getSheet(sheetName);
        int invoiceIdColIndex = getColumnIndex(colName, mainSheet);
        Iterator<Row> rowIterator = mainSheet.rowIterator();
        Row lastRow = null;
        while (rowIterator.hasNext()) {
            Row next = rowIterator.next();
            if (Objects.isNull(lastRow))
                lastRow = next;

            Cell cell = next.getCell(invoiceIdColIndex);
            if (Objects.isNull(cell)) {
                return lastRow;
            }
            String stringCellValue = cell.getStringCellValue();
            if (isNullOrEmpty(stringCellValue)) {
                return cell.getRow();
            }
            lastRow = next;
        }

        throw new NotFoundException("Next available row not found");
    }

    public static Row findByInvoiceId(Workbook workbook, String sheetName, String colName, String invoiceId) {
        final Sheet mainSheet = workbook.getSheet(sheetName);
        int invoiceIdColIndex = getColumnIndex(colName, mainSheet);
        Iterator<Row> rowIterator = mainSheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row next = rowIterator.next();
            Cell cell = next.getCell(invoiceIdColIndex);
            if (Objects.isNull(cell)) {
                return next;
            }
            String stringCellValue = cell.getStringCellValue();
            if (invoiceId.equalsIgnoreCase(stringCellValue)) {
                return cell.getRow();
            }
        }

        throw new NotFoundException("Next available row not found");
    }


    public static int getColumnIndex(String colName, Sheet sheet) {
        Iterator<Cell> it = sheet.getRow(0).cellIterator();
        while (it.hasNext()) {
            Cell next = it.next();
            if (next.getStringCellValue().equalsIgnoreCase(colName)) {
                return next.getAddress().getColumn();
            }
        }
        throw new NotFoundException("Column not found");
    }

    public static void writeExcelFile(File generatedFile, Workbook billWorkbook) throws IOException {
        FileOutputStream output = new FileOutputStream(generatedFile);
        billWorkbook.write(output);
        output.close();
    }


    public static void removeOtherSheets(String sheetName, Workbook book) {
        for (int i = book.getNumberOfSheets() - 1; i >= 0; i--) {
            Sheet tmpSheet = book.getSheetAt(i);
            if (!tmpSheet.getSheetName().equals(sheetName)) {
                book.removeSheetAt(i);
            }
        }
    }


}
