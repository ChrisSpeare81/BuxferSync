package BuxferSyncer.Buxfer;

import BuxferSyncer.Buxfer.Account.BuxferAccount;
import BuxferSyncer.Buxfer.Account.BuxferAccountResponse;
import BuxferSyncer.Buxfer.DeleteTransaction.DeleteTransactionResponse;
import BuxferSyncer.Buxfer.NewTransaction.NewBuxferTransaction;
import BuxferSyncer.Buxfer.NewTransaction.NewBuxferTransactionResponse;
import BuxferSyncer.Buxfer.Token.BuxferTokenResponse;
import BuxferSyncer.Buxfer.Transaction.BuxferTransaction;
import BuxferSyncer.Buxfer.Transaction.BuxferTransactionResponse;
import BuxferSyncer.Halifax.TransactionBean;
import BuxferSyncer.Pojos.Transaction;
import BuxferSyncer.TransactionMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.*;

public class BuxferService {

    private static final String API_BASE_URL = "https://www.buxfer.com/api/";

    private final String username = "cspeare81@googlemail.com";

    private final String password = "A9Y4EheKr3Yn";

    private String token;

    private HashMap<String, BuxferAccount> accounts;

    private final TransactionMapper mapper = new TransactionMapper();

    private static Retrofit retrofit;

    private static BuxferAPI service;

    public BuxferService() {

        retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        //.addInterceptor(logging)
                        .addInterceptor(chain -> {

                            HttpUrl url = chain.request().url().newBuilder()
                                    .addQueryParameter("token", token)
                                    .build();

                            // Request customization: add request headers
                            Request.Builder requestBuilder = chain.request().newBuilder().url(url);
                            Request request = requestBuilder.build();
                            return chain.proceed(request);

                        })
                        .build()
                )
                .build();

        service = retrofit.create(BuxferAPI.class);
        token = getToken();

    }

    private String getToken() {

        try {

            Call<BuxferTokenResponse> callSync = service.getToken(username, password);
            Response<BuxferTokenResponse> response = callSync.execute();

            BuxferTokenResponse loginResponse = response.body();

            assert loginResponse != null;
            return loginResponse.getResponse().getToken();

        } catch (Exception ignored) {

        }

        return null;

    }

    public BuxferAccount getAccount(String name) {

        return accounts.get(name);

    }

    public void refreshAccounts() {

        accounts = new HashMap<>();

        try {

            Call<BuxferAccountResponse> callSync = service.getAccounts();
            Response<BuxferAccountResponse> response = callSync.execute();

            BuxferAccountResponse accountResponse = response.body();

            assert accountResponse != null;

            accountResponse.getResponse().getAccounts().forEach(account -> {

                accounts.put(account.getName(), account);

            });

        } catch (Exception ignored) {

        }

    }

    public ArrayList<BuxferTransaction> getClearedTransactions(String accountName, String dateFrom) {

        return getTransactions(accountName, dateFrom, "cleared");

    }

    public ArrayList<BuxferTransaction> getPendingTransactions(String accountName, String dateFrom) {

        return getTransactions(accountName, dateFrom, "pending");

    }

    private ArrayList<BuxferTransaction> getTransactions(String accountName, String dateFrom, String status) {

        try {

            Call<BuxferTransactionResponse> callSync = service.getTransactions(
                    accounts.get(accountName).getId(),
                    dateFrom,
                    status
            );

            Response<BuxferTransactionResponse> response = callSync.execute();

            BuxferTransactionResponse transactionsResponse = response.body();

            assert transactionsResponse != null;

            return transactionsResponse.getResponse().getTransactions();

        } catch (Exception ignored) {

        }

        return new ArrayList<>();

    }

    public NewBuxferTransaction addTransaction(TransactionBean transaction) {

        try {

            Call<NewBuxferTransactionResponse> callSync;

            Transaction mappedTransaction = mapper.getTransaction(transaction);

            if (mappedTransaction.isTransfer()) {

                callSync = service.addTransfer(
                    mappedTransaction.getDescription(),
                    transaction.isDebit() ? transaction.getDebitAmount() : transaction.getCreditAmount(),
                    accounts.get(mappedTransaction.getSourceAccount()).getId(),
                    accounts.get(mappedTransaction.getTargetAccount()).getId(),
                    transaction.getDate(),
                    "transfer",
                    mappedTransaction.getTags(),
                    "cleared"
                );

            } else {

                callSync = service.addTransaction(
                    mappedTransaction.getDescription(),
                    transaction.isDebit() ? transaction.getDebitAmount() : transaction.getCreditAmount(),
                    accounts.get("Halifax").getId(),
                    transaction.getDate(),
                    mappedTransaction.getTransactionType().name().toLowerCase(),
                    mappedTransaction.getTags(),
                    "cleared"
                );

            }

            Response<NewBuxferTransactionResponse> response = callSync.execute();
            assert response.body() != null;
            return response.body().getResponse();

        } catch (Exception ex) {

            System.err.println(ex);
        }

        return null;

    }

    public void deleteTransaction(String id) {

        try {

            Call<DeleteTransactionResponse> callSync = service.deleteTransfer(id);
            callSync.execute();

        } catch (IOException ignored) {
        }

    }

    public ArrayList<String> validateMappers(List<TransactionBean> transactions) {

        ArrayList<String> unMapped = new ArrayList<>();

        transactions.forEach(transaction -> {

            String description = transaction.getDescription();
            Transaction matchedTransaction = mapper.getTransaction(transaction);

            if (matchedTransaction == null && !unMapped.contains(description)) {

                unMapped.add(description);

            }

        });

        return unMapped;

    }

}


