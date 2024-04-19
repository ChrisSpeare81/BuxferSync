package BuxferSyncer.Pojos;

import lombok.Getter;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Transaction {

    private final ArrayList<Pattern> regexs;

    @Getter
    private final String type;

    @Getter
    private final String description;

    public Transaction(ArrayList<String> regexStrings, String type, String description) {

         regexs = new ArrayList<Pattern>();

        regexStrings.forEach(regex -> {

            regexs.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));

        });


        this.type = type;
        this.description = description;

    }

    public boolean testDescription(String description) {

        for (Pattern pattern : regexs) {

            if (pattern.matcher(description).matches()) {

                return true;

            }

        }

        return false;

    }

}
