package ro.jmind.photos.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Invoice {
    private String invoiceId;
    private LocalDate invoiceDate;
    private double vatRate;
    private double amount;
    private String template;

}
