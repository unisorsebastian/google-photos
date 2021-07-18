package ro.jmind.photos.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.jmind.photos.Utils;

import java.util.ArrayList;
import java.util.List;


public class ExcelOutputModel {
    private Integer row;
    private String uid;
    private String pictureName;
    private String pictureLocation;
    private String pictureLocalLocation;
    private List<String> description;
    private Integer price;
    private byte[] imageAsBytes;

    private ExcelOutputModel(Integer row, String uid, String pictureName, String pictureLocation, String pictureLocalLocation, List<String> description, Integer price, byte[] imageAsBytes) {
        this.row = row;
        this.uid = uid;
        this.pictureName = pictureName;
        this.pictureLocation = pictureLocation;
        this.pictureLocalLocation = pictureLocalLocation;
        this.description = description;
        this.price = price;
        this.imageAsBytes = imageAsBytes;
    }


    public Integer getRow() {
        return row;
    }

    public String getUid() {
        return uid;
    }

    public String getPictureName() {
        return pictureName;
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

    public byte[] getImageAsBytes() {
        return imageAsBytes;
    }

    public static class ExcelOutputBuilder {
        private static final Logger LOGGER = LoggerFactory.getLogger(ExcelOutputBuilder.class);

        private Integer row;
        private String uid;
        private String pictureName;
        private String pictureLocation;
        private String pictureLocalLocation;
        private List<String> description;
        private Integer price;
        private byte[] imageAsBytes;

        public ExcelOutputBuilder setRow(String row) {
            this.row = Integer.valueOf(row);
            return this;
        }

        public ExcelOutputBuilder setImageAsBytes(byte[] bytes) {
            this.imageAsBytes = bytes;
            return this;
        }

        public ExcelOutputBuilder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public ExcelOutputBuilder setPictureName(String pictureName) {
            this.pictureName = pictureName;
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
            this.description = Utils.parseDescription(stringCellValue);
            return this;
        }

        public ExcelOutputBuilder setDescription(List<String> descriptionList) {
            this.description = new ArrayList<>();
            description.addAll(descriptionList);
            return this;
        }

        public ExcelOutputBuilder setDescription(Cell description) {
            String stringCellValue = description.getStringCellValue();
            return setStringDescription(stringCellValue);
        }

        public ExcelOutputBuilder setPrice(Integer price) {
            this.price = price;
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
                LOGGER.error("unable to get price at address {}, set zero", price.getAddress());
                this.price = 0;
            }
            return this;
        }

        public ExcelOutputModel createExcelOutputModel() {
            return new ExcelOutputModel(row, uid, pictureName, pictureLocation, pictureLocalLocation, description, price, imageAsBytes);
        }
    }
}
