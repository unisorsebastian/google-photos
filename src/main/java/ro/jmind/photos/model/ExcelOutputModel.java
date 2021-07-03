package ro.jmind.photos.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelOutputModel {
    private Integer row;
    private String uid;
    private String pictureLocation;
    private List<String> description;
    private Integer price;

    private ExcelOutputModel(Integer row, String uid, String pictureLocation, List<String> description, Integer price) {
        this.row = row;
        this.uid = uid;
        this.pictureLocation = pictureLocation;
        this.description = description;
        this.price = price;
    }

    public Integer getRow() {
        return row;
    }

    public String getUid() {
        return uid;
    }

    public String getPictureLocation() {
        return pictureLocation;
    }

    public List<String> getDescription() {
        return description;
    }

    public Integer getPrice() {
        return price;
    }

    public static class ExcelOutputBuilder {
        private static final Logger LOGGER = LoggerFactory.getLogger(ExcelOutputBuilder.class);

        private Integer row;
        private String uid;
        private String pictureLocation;
        private List<String> description;
        private Integer price;

        public ExcelOutputBuilder setRow(String row) {
            this.row = Integer.valueOf(row);
            return this;
        }

        public ExcelOutputBuilder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public ExcelOutputBuilder setPictureLocation(String pictureLocation) {
            this.pictureLocation = pictureLocation;
            return this;
        }

        public ExcelOutputBuilder setDescription(Cell description) {
            String stringCellValue = description.getStringCellValue();
            Pattern pattern = Pattern.compile("\n");
            Matcher matcher = pattern.matcher(stringCellValue);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            if (stringCellValue.contains("•")) {
                stringCellValue = stringCellValue.replace("•", "");
                this.description = Stream.of(stringCellValue.split("\n", -1))
                        .map(s -> s.trim())
                        .map(s -> StringUtils.capitalize(s))
                        .collect(Collectors.toList());
            } else if (count > 2) {
                this.description = Stream.of(stringCellValue.split("\n", -1))
                        .map(s -> s.trim())
                        .map(s -> StringUtils.capitalize(s))
                        .collect(Collectors.toList());
            } else {
                this.description = Stream.of(stringCellValue.split(",", -1))
                        .map(s -> s.trim())
                        .map(s -> StringUtils.capitalize(s))
                        .collect(Collectors.toList());
            }
            return this;
        }

        public ExcelOutputBuilder setPrice(Cell price) {
            try {
                if (price.getCellType() == CellType.NUMERIC) {
                    this.price = Double.valueOf(price.getNumericCellValue()).intValue();
                } else {
                    this.price = Double.valueOf(price.getStringCellValue()).intValue();
                }
            } catch (NumberFormatException e) {
                LOGGER.error("unable to get price, set zero");
                this.price = 0;
            }
            return this;
        }

        public ExcelOutputModel createExcelOutputModel() {
            return new ExcelOutputModel(row, uid, pictureLocation, description, price);
        }
    }
}
