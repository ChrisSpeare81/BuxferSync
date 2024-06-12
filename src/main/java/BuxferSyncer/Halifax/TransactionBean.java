package BuxferSyncer.Halifax;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
@Setter
public class TransactionBean {

    @CsvBindByName(column = "Transaction Date")
    private String date;

    @CsvBindByName(column = "Transaction Type")
    private String type;

    @CsvBindByName(column = "Sort Code")
    private String sortCode;

    @CsvBindByName(column = "Account Number")
    private String accountNumber;

    @CsvBindByName(column = "Transaction Description")
    private String description;

    @CsvBindByName(column = "Debit Amount")
    private Double debitAmount;

    @CsvBindByName(column = "Credit Amount")
    private Double creditAmount;

    @CsvBindByName(column = "Balance")
    private Double balance;

    public boolean isDebit() {

        return (debitAmount != null) && debitAmount > 0;

    }

    public String getDate() {

        // Define a DateTimeFormatter for the output format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Define a DateTimeFormatter for the input format
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {

            // Parse the input date string to a LocalDate object
            LocalDate localDate = LocalDate.parse(date, inputFormatter);

            // Format the LocalDate object to the desired format
            return localDate.format(outputFormatter);

        } catch (DateTimeParseException e) {

            System.err.println("Failed to parse the date string: " + e.getMessage());

        }

        return date;

    }

}
