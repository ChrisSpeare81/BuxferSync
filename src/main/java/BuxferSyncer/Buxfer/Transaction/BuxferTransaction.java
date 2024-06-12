package BuxferSyncer.Buxfer.Transaction;

import lombok.Getter;

@Getter
public class BuxferTransaction {

    Integer id;

    String description;

    String date;

    String type;

    Double amount;

    String accountId;

    String tags;

}