package BuxferSyncer.Buxfer.NewTransaction;

import lombok.Getter;

@Getter
public class NewBuxferTransaction {

    String id;

    String description;

    String date;

    String type;

    Double amount;

    String accountId;

    String tags;

}