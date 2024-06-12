package BuxferSyncer;

import BuxferSyncer.Halifax.TransactionBean;
import BuxferSyncer.Pojos.Transaction;
import BuxferSyncer.Pojos.TransactionType;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static BuxferSyncer.Pojos.TransactionType.*;

public class TransactionMapper {

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public TransactionMapper() {

        registerTransactions();

    }

    private void registerTransactions() {

        URL url = TransactionMapper.class.getClassLoader().getResource("Transaction Rules.xlsx");
        Path resourcePath;

        try {

            resourcePath = Path.of(url.toURI());

        } catch (URISyntaxException e) {

            throw new RuntimeException(e);

        }

        try (FileInputStream file = new FileInputStream(resourcePath.toString()); ReadableWorkbook wb = new ReadableWorkbook(file)) {

            Optional<Sheet> sheet = wb.findSheet("Expense");

            if (sheet.isPresent()) {

                try (Stream<Row> rows = sheet.get().openStream()) {

                    rows.skip(1).forEach(r -> transactions.add(
                        new Transaction(r.getCellText(0), r.getCellText(1), r.getCellText(2), EXPENSE))
                    );

                }

            }

           sheet = wb.findSheet("Income");

            if (sheet.isPresent()) {

                try (Stream<Row> rows = sheet.get().openStream()) {

                    rows.skip(1).forEach(r -> transactions.add(
                        new Transaction(r.getCellText(0), r.getCellText(1), r.getCellText(2), INCOME))
                    );

                }

            }

            sheet = wb.findSheet("Transfer");

            if (sheet.isPresent()) {

                try (Stream<Row> rows = sheet.get().openStream()) {

                    rows.skip(1).forEach(r -> transactions.add(
                        new Transaction(
                            r.getCellText(0),
                            r.getCellText(1),
                            r.getCellText(2),
                            TRANSFER,
                            r.getCellText(3),
                            r.getCellText(4),
                            r.getCellText(5).contentEquals("Yes")
                        ))
                    );

                }

            }

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

    }

    public Transaction getTransaction(TransactionBean transactionBean) {

        String description = transactionBean.getDescription();

        if (isTransfer(transactionBean) != null) {

            return isTransfer(transactionBean);

        } else if (transactionBean.isDebit()) {

            return isExpense(description);

        } else {

            return isIncome(description);

        }

    }

    public Transaction isExpense(String description) {

        for (Transaction transaction : transactions) {

            if (transaction.checkMatches(EXPENSE, description)) {

                return transaction;

            }

        }

        return null;

    }

    public Transaction isIncome(String description) {

        for (Transaction transaction : transactions) {

            if (transaction.checkMatches(INCOME, description)) {

                return transaction;

            }

        }

        return null;

    }

    public Transaction isTransfer(TransactionBean transactionBean) {

        for (Transaction transaction : transactions) {

            if (transaction.checkMatches(TRANSFER, transactionBean.getDescription())) {

                if ((transaction.getIsCreditOnly() && !transactionBean.isDebit()) ||
                    (!transaction.getIsCreditOnly() && transactionBean.isDebit())) {

                    return transaction;

                }

            }

        }

        return null;

    }

}
