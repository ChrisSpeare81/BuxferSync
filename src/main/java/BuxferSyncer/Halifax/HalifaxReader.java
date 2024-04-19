package BuxferSyncer.Halifax;

import BuxferSyncer.Pojos.CsvTransfer;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HalifaxReader {

    public List<TransactionBean> readCsv(Path path, Class clazz) {

        CsvTransfer csvTransfer = new CsvTransfer();

        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean<TransactionBean> cb = new CsvToBeanBuilder<TransactionBean>(reader)
                    .withType(clazz)
                    .build();

            csvTransfer.setCsvList(cb.parse());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return csvTransfer.getCsvList();

    }

}
