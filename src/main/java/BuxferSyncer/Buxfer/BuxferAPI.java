package BuxferSyncer.Buxfer;

import BuxferSyncer.Buxfer.Account.BuxferAccountResponse;
import BuxferSyncer.Buxfer.Token.BuxferTokenResponse;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BuxferAPI {

    @POST("login")
    Call<BuxferTokenResponse> getToken(
            @Query(value = "email") String email,
            @Query("password") String password
    );

    @POST("accounts")
    Call<BuxferAccountResponse> getAccounts();
}
