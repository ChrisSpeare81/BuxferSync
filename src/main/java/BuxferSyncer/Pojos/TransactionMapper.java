package BuxferSyncer.Pojos;

import java.util.ArrayList;
import java.util.List;

public class TransactionMapper {

    private ArrayList<Transaction> transactions;

    public TransactionMapper() {

        registerTransactions();

    }

    private void registerTransactions() {

        transactions = new ArrayList<>(
                List.of(
                        new Transaction(
                                new ArrayList<>(List.of("DANI")),
                                "expense",
                                "Dani"
                        )
                )
        );

    }

    public Transaction matchDescription(String description) {

        for (Transaction transaction : transactions) {

            if (transaction.testDescription(description)) {

                return transaction;

            }

        }

        return null;

    }

}
