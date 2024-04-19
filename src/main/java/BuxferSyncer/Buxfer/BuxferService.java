package BuxferSyncer.Buxfer;

import BuxferSyncer.Buxfer.Account.BuxferAccount;
import BuxferSyncer.Buxfer.Account.BuxferAccountResponse;
import BuxferSyncer.Buxfer.Token.BuxferTokenResponse;
import BuxferSyncer.Halifax.TransactionBean;
import BuxferSyncer.Pojos.Transaction;
import BuxferSyncer.Pojos.TransactionMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BuxferService {

    private static final String API_BASE_URL  = "https://www.buxfer.com/api/";

    private final String username = "cspeare81@googlemail.com";

    private final String password = "A9Y4EheKr3Yn";

    private String token;

    private HashMap<String, BuxferAccount> accounts;

    private final TransactionMapper mapper = new TransactionMapper();

    private static HttpLoggingInterceptor logging
            = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            //.addInterceptor(logging)
            .build();


    private static Retrofit retrofit;

    public BuxferService() {

        retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(new OkHttpClient.Builder()
                    .addInterceptor(logging)
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

        token = getToken();

    }

    private String getToken() {

        try {

            BuxferAPI service = retrofit.create(BuxferAPI.class);
            Call<BuxferTokenResponse> callSync = service.getToken(username, password);
            Response<BuxferTokenResponse> response = callSync.execute();

            BuxferTokenResponse loginResponse = response.body();

            assert loginResponse != null;
            return loginResponse.getResponse().getToken();

        } catch (Exception ignored) {

        }

        return null;

    }

    public ArrayList<String> validateMappers(List<TransactionBean> transactions) {

        ArrayList<String> unMapped = new ArrayList<>();

        transactions.forEach(transaction -> {

            String description = transaction.getDescription();

            Transaction matchedTransaction = mapper.matchDescription(description);

            if (matchedTransaction == null && !unMapped.contains(description)) {

                unMapped.add(description);

            }

        });

        return unMapped;

    }

    public void refreshAccounts() {

        accounts = new HashMap<>();

        try {

            BuxferAPI service = retrofit.create(BuxferAPI.class);
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

}


