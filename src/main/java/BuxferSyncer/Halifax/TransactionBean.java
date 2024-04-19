package BuxferSyncer.Halifax;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

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


}
