package BuxferSyncer;

import BuxferSyncer.Buxfer.BuxferService;
import BuxferSyncer.Halifax.HalifaxReader;
import BuxferSyncer.Halifax.TransactionBean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuxferSyncer {

    BuxferService buxferService;

    public BuxferSyncer() {

        buxferService = new BuxferService();

        buxferService.refreshAccounts();

        Path latestCsv = Paths.get("C:\\Users\\horus\\Downloads\\LatestHalifax.csv");

        HalifaxReader reader = new HalifaxReader();
        List<TransactionBean> transactions = reader.readCsv(latestCsv, TransactionBean.class);

        ArrayList<String> unMapped = buxferService.validateMappers(transactions);

        Collections.reverse(transactions);

        transactions.forEach(transaction -> {

           System.out.println(transaction.getDate());

       });



    }

    public static void main (String args[]) {

        BuxferSyncer syncer = new BuxferSyncer();

    }

}
