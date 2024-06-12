package BuxferSyncer.Pojos;

import BuxferSyncer.Halifax.TransactionBean;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
public class CsvTransfer {

    private List<TransactionBean> csvList;

    public CsvTransfer() {}

    public List<TransactionBean> getCsvList() {

        if (csvList != null) return csvList;

        return new ArrayList<>();

    }

}