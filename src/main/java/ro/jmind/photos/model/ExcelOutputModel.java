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
    private String pictureLocalLocation;
    private List<String> description;
    private Integer price;

    private ExcelOutputModel(Integer row, String uid, String pictureLocation, String pictureLocalLocation, List<String> description, Integer price) {
        this.row = row;
        this.uid = uid;
        this.pictureLocation = pictureLocation;
        this.pictureLocalLocation = pictureLocalLocation;
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

    public String getPictureLocalLocation() {
        return pictureLocalLocation;
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
        private String pictureLocalLocation;
        private List<String> description;
        private Integer price;

        public static List<String> splitDescriptionBy(String value, String splitString) {
            return Stream.of(value.split(splitString, -1))
                    .map(s -> s.trim())
                    .map(s -> {
                        if (s.length() > 1 && s.substring(0, 1).equals("-")) {
                            return s.substring(1);
                        }
                        return s;
                    })
                    .map(s -> {
                        if (s.length() < 1) {
                            return s;
                        }
                        String lastChar = s.substring(s.length() - 1);
                        if (lastChar.equals(";") || lastChar.equals(".")) {
                            return s.substring(0, s.length() - 1);
                        }
                        return s;
                    })
                    .map(s -> StringUtils.capitalize(s))
                    .collect(Collectors.toList());
        }

        public static List<String> parseDescription(String description) {
            List<String> result;
            description = description.replace("\n\n", ", ");
            Pattern pattern = Pattern.compile("\n");
            Matcher matcher = pattern.matcher(description);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            if (description.contains("•")) {
                description = description.replace("•", "");
                result = splitDescriptionBy(description, "\n");
            } else if (count > 2) {
                result = splitDescriptionBy(description, "\n");
            } else {
                result = splitDescriptionBy(description, ", ");
            }
            return result;
        }

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

        public ExcelOutputBuilder setPictureLocalLocation(String pictureLocalLocation) {
            this.pictureLocalLocation = pictureLocalLocation;
            return this;
        }

        public ExcelOutputBuilder setStringDescription(String stringCellValue) {
            this.description = parseDescription(stringCellValue);
            return this;
        }

        public ExcelOutputBuilder setDescription(Cell description) {
            String stringCellValue = description.getStringCellValue();
            return setStringDescription(stringCellValue);
        }

        public ExcelOutputBuilder setPrice(Cell price) {
            try {
                if (price.getCellType() == CellType.NUMERIC) {
                    this.price = Double.valueOf(price.getNumericCellValue()).intValue();
                } else {
                    this.price = Double.valueOf(price.getStringCellValue()).intValue();
                }
            } catch (NumberFormatException e) {
                LOGGER.error("unable to get price at address {}, set zero", price.getAddress());
                this.price = 0;
            }
            return this;
        }

        public ExcelOutputModel createExcelOutputModel() {
            return new ExcelOutputModel(row, uid, pictureLocation, pictureLocation, description, price);
        }
    }
}
