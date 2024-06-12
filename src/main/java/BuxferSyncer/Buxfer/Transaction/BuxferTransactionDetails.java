package BuxferSyncer.Buxfer.Transaction;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class BuxferTransactionDetails {

    Integer numTransactions;

    ArrayList<BuxferTransaction> transactions;

}