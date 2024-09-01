package BuxferSyncer;

import BuxferSyncer.Buxfer.BuxferService;
import BuxferSyncer.Buxfer.NewTransaction.NewBuxferTransaction;
import BuxferSyncer.Buxfer.Transaction.BuxferTransaction;
import BuxferSyncer.Halifax.HalifaxReader;
import BuxferSyncer.Halifax.TransactionBean;
import BuxferSyncer.Pojos.Transaction;
import org.apache.commons.math3.util.Precision;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.MessageFormat;
import java.util.stream.Collectors;

public class BuxferSyncer {

    private final BuxferService buxferService;

    private final TransactionMapper mapper = new TransactionMapper();

    private ArrayList<BuxferTransaction> pendingTransactions;

    public BuxferSyncer() throws URISyntaxException {

        buxferService = new BuxferService();
        buxferService.refreshAccounts();

    }

    public boolean validateMappings() throws URISyntaxException {

        URL url = BuxferSyncer.class.getClassLoader().getResource("LatestHalifax.csv");
        Path resourcePath = Path.of(url.toURI());

        HalifaxReader reader = new HalifaxReader();
        List<TransactionBean> transactions = reader.readCsv(resourcePath, TransactionBean.class);

        ArrayList<String> unMapped = buxferService.validateMappers(transactions);

        Collections.reverse(transactions);
        Collections.sort(unMapped);

        if (!unMapped.isEmpty()) {

            System.out.println(MessageFormat.format(
                "Unable to proceed, {0} descriptions could not be matched:",
                unMapped.size()
            ));

            unMapped.forEach(System.out::println);

            return false;

        }

        return true;

    }

    private BuxferTransaction getLastClearedTransaction() {

        LocalDate currentDate = LocalDate.now();
        LocalDate lastTransactionDate = currentDate.minusMonths(6);

        ArrayList<BuxferTransaction> lastTransactions = buxferService.getClearedTransactions(
            "Halifax",
            lastTransactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        return lastTransactions.get(0);

    }

    public void populatePendingTransactions() {

        BuxferTransaction lastClearedTransaction = getLastClearedTransaction();
        LocalDate transactionDate = LocalDate.parse(lastClearedTransaction.getDate());

        pendingTransactions = buxferService.getPendingTransactions(
            "Halifax",
            transactionDate.minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        Collections.reverse(pendingTransactions);

    }

    public void processHalifaxTransactions() throws URISyntaxException {

        ArrayList<NewBuxferTransaction> addedTransactions = new ArrayList<>();

        URL url = BuxferSyncer.class.getClassLoader().getResource("LatestHalifax.csv");
        Path resourcePath = Path.of(url.toURI());

        HalifaxReader reader = new HalifaxReader();
        List<TransactionBean> transactions = reader.readCsv(resourcePath, TransactionBean.class);

        Collections.reverse(transactions);

        LinkedHashMap<String, List<TransactionBean>> groupedByDate = transactions.stream()
            .collect(Collectors.groupingBy(
                TransactionBean::getDate,
                LinkedHashMap::new,        // Use HashMap as the desired map implementation
                Collectors.toList()  // Collect the elements into a list
            )
        );

        BuxferTransaction lastTransaction = getLastClearedTransaction();
        LocalDate lastTransactionDate = LocalDate.parse(lastTransaction.getDate());

        for (String date : groupedByDate.keySet()) {

            LocalDate groupedDate = LocalDate.parse(date);

            if (groupedDate.isAfter(lastTransactionDate)) {

                List<TransactionBean> dailyTransactions = groupedByDate.get(date);

                System.out.println(MessageFormat.format("Processing transactions for {0}", date));

                for (TransactionBean transaction : dailyTransactions) {

                    System.out.print(MessageFormat.format(
                            "\tProcessing {0} on {1}",
                            transaction.getDescription(),
                            transaction.getDate()
                    ));

                    boolean transactionUpdated = false;

                    // Check if there's a pending transaction which matches this transaction
                    if (!pendingTransactions.isEmpty()) {

                        BuxferTransaction pendingTransaction = pendingTransactions
                                .stream()
                                .filter(t -> {
                                    LocalDate pendingDate = LocalDate.parse(t.getDate());
                                    LocalDate transactionDate = LocalDate.parse(transaction.getDate());

                                    return (
                                            Objects.equals(t.getAmount(), transaction.getDebitAmount()) &&
                                                    transactionDate.isAfter(pendingDate)
                                    );
                                })
                                .findFirst()
                                .orElse(null);

                        if (pendingTransaction != null) {

                            transactionUpdated = true;
                            pendingTransactions.remove(pendingTransaction);

                            buxferService.clearTransaction(pendingTransaction, transaction.getDate());
                            System.out.println(" - converted pending to cleared");


                        }

                    }

                    if (!transactionUpdated) {

                        addedTransactions.add(buxferService.addTransaction(transaction));
                        System.out.println(" - added");

                    }

                }

                buxferService.refreshAccounts();

                Double buxferBalance = buxferService.getAccount("Halifax").getBalance();

                double closingDayBalance = dailyTransactions.get(dailyTransactions.size() - 1).getBalance();

                if (!Precision.equals(closingDayBalance, buxferBalance)) {

                    System.err.println(MessageFormat.format(
                            "Balance incorrect on {0} - expected {1}, returned {2}",
                            date,
                            closingDayBalance,
                            buxferBalance
                    ));

                    //addedTransactions.forEach(t -> buxferService.deleteTransaction(t.getId()));
                    break;

                }

                System.out.println();

            }

        }

    }

    public static void main (String[] args) throws URISyntaxException {

        BuxferSyncer syncer = new BuxferSyncer();

        if (syncer.validateMappings()) {

            syncer.populatePendingTransactions();
            syncer.processHalifaxTransactions();

        }

    }

}
