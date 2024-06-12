package BuxferSyncer.Pojos;

import lombok.Getter;

import java.util.regex.Pattern;

public class Transaction {

    private final Pattern regex;

    @Getter
    private final String description;

    @Getter
    private final String tags;

    @Getter
    private final String sourceAccount;

    @Getter
    private final String targetAccount;

    @Getter
    private final TransactionType transactionType;

    @Getter
    private final Boolean isCreditOnly;

    public Transaction(String regex, String description, String tags, TransactionType type) {

        this(regex, description, tags, type, null, null, false);

    }

    public Transaction(String regex, String description, String tags, TransactionType type, String sourceAccount,
                       String targetAccount, boolean checkCredit) {

        this.regex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.description = description;
        this.tags = tags;
        this.transactionType = type;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.isCreditOnly = checkCredit;

    }

    public boolean checkMatches(TransactionType type, String description) {

        return type == transactionType && regex.matcher(description).matches();

    }

    public boolean isExpense() {

        return transactionType == TransactionType.EXPENSE;

    }

    public boolean isIncome() {

        return transactionType == TransactionType.INCOME;

    }

    public boolean isTransfer() {

        return transactionType == TransactionType.TRANSFER;

    }

}
