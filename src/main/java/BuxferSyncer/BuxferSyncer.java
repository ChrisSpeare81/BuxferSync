package BuxferSyncer;

import BuxferSyncer.Buxfer.BuxferService;
import BuxferSyncer.Buxfer.NewTransaction.NewBuxferTransaction;
import BuxferSyncer.Halifax.HalifaxReader;
import BuxferSyncer.Halifax.TransactionBean;
import BuxferSyncer.Pojos.Transaction;
import org.apache.commons.math3.util.Precision;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.MessageFormat;

public class BuxferSyncer {

    private final BuxferService buxferService;

    private final TransactionMapper mapper = new TransactionMapper();

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

    public void processHalifaxTransactions() throws URISyntaxException {

        Double buxferBalance = buxferService.getAccount("Halifax").getBalance();
        Boolean processTransaction = false;
        ArrayList<NewBuxferTransaction> addedTransactions = new ArrayList<>();

        URL url = BuxferSyncer.class.getClassLoader().getResource("LatestHalifax.csv");
        Path resourcePath = Path.of(url.toURI());

        HalifaxReader reader = new HalifaxReader();
        List<TransactionBean> transactions = reader.readCsv(resourcePath, TransactionBean.class);

        Collections.reverse(transactions);

        for (TransactionBean transaction : transactions) {

            if (Precision.equals(transaction.getBalance(), buxferBalance)) {

                processTransaction = true;
                continue;

            }

            if (processTransaction) {

                System.out.println(MessageFormat.format(
                        "Processing transaction {0} on {1}",
                        transaction.getDescription(),
                        transaction.getDate()
                ));

                addedTransactions.add(buxferService.addTransaction(transaction));

                buxferService.refreshAccounts();
                buxferBalance = buxferService.getAccount("Halifax").getBalance();

                if (!Precision.equals(transaction.getBalance(), buxferBalance)) {

                    Transaction badMapping = mapper.getTransaction(transaction);

                    System.err.println(MessageFormat.format(
                            "Balance mismatch - {0} - expected {1}, returned {2}",
                            transaction.getDescription(),
                            transaction.getBalance(),
                            buxferBalance
                    ));

                    addedTransactions.forEach(t -> buxferService.deleteTransaction(t.getId()));
                    break;

                }

            }

        }

    }

    public static void main (String[] args) throws URISyntaxException, IOException {

        BuxferSyncer syncer = new BuxferSyncer();

        if (syncer.validateMappings()) {

            syncer.processHalifaxTransactions();

        }

    }

}
