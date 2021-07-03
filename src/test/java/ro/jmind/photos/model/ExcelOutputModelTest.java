package ro.jmind.photos.model;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ExcelOutputModelTest {

    private ExcelOutputModel excelOutputModel = null;

    @Before
    public void setup() {

    }

    @Test
    public void getDescription() {
        String value1 = "• 4 MegaPixel TVI 1440p camera video exterior, TURBO HD, IR, rezolutie 2560x 1440pixeli 12.5 fps (25 fps pe seria HWD-71xyMH-Gz)\n" +
                "• iluminator IR automat 40 metri EXIR 2.0 cu optimizare IRCut Day&Night, 0.01 Lux@(F1.2, AGC ON), 0 Lux with IR;\n" +
                "• obiectiv VariFocal Manual Zoom Manual Focus 2.8-12mm deschidere 108.4-32.6 grade\n" +
                "• Digital noise reduction, Mirror, SMART IR, BLC, DigitalWideDynamicRange\n" +
                "• cablare cu cablu coaxial pana la 500 metri si conectori BNC; 4 in 1 output (switchable TVI/AHD/CVI/CVBS)\n" +
                "• utilizare interior/exterior grad de protectie la intemperii IP66; \n" +
                "• temperaturi de utilizare -40C pana la +60C\n" +
                "• alimentare 12Vcc/1A; dimensiuni 256.4 mm × 83.3 mm × 78.2 mm ; doza DS-1280ZJ-S\n" +
                "-functioneaza impreuna cu DVR-uri HIKVISION TurboHD si DVR-uri HDCVI/AHD/analogice.";

        String value2 = "Statie de incarcare masini electrice, alimentare monofazata, " +
                "220V/32A, 7 kW, display cu afisare de tensiune, current, putere si energie consumata, " +
                "cu cablu de alimentare si mufa Type 2, LED prezenta tensiune, LED incarcare, " +
                "dimensiuni 250x200x115, alimentare 220VAC ± 10%, frecventa 50Hz, protectie intemperii IP55, " +
                "protectie antivandal IK10, carcasa aluminiu, culoare verde RAL 6018, " +
                "montaj pe perete. Contine suport de cablu DMT4 si support DMT 2 pentru mufa Type 2.\n\n" +
                "Statia include protectie diferentiala electronica de 30mA.";

        ExcelOutputModel excelOutputModel = this.excelOutputModel = new ExcelOutputModel.ExcelOutputBuilder()
                .setStringDescription(value1)
                .createExcelOutputModel();
        List<String> description = excelOutputModel.getDescription();
        assertEquals(description.get(2).substring(0, 1), "O");
        assertEquals(description.get(8).substring(0, 1), "F");

        excelOutputModel = this.excelOutputModel = new ExcelOutputModel.ExcelOutputBuilder()
                .setStringDescription(value2)
                .createExcelOutputModel();
        description = excelOutputModel.getDescription();
        assertEquals(description.size(), 19);
    }
}