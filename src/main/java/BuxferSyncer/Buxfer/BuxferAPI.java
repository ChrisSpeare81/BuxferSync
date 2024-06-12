package BuxferSyncer.Buxfer;

import BuxferSyncer.Buxfer.Account.BuxferAccountResponse;
import BuxferSyncer.Buxfer.DeleteTransaction.DeleteTransactionResponse;
import BuxferSyncer.Buxfer.NewTransaction.NewBuxferTransactionResponse;
import BuxferSyncer.Buxfer.Token.BuxferTokenResponse;
import BuxferSyncer.Buxfer.Transaction.BuxferTransactionResponse;
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

    @POST("transactions")
    Call<BuxferTransactionResponse> getTransactions(
            @Query(value = "accountId") String accountId,
            @Query(value = "startDate") String startDate,
            @Query(value = "status") String status
    );

    @POST("transaction_add")
    Call<NewBuxferTransactionResponse> addTransaction(
            @Query(value = "description") String description,
            @Query(value = "amount") Double startDate,
            @Query(value = "accountId") String accountId,
            @Query(value = "date") String date,
            @Query(value = "type") String type,
            @Query(value = "tags") String tags,
            @Query(value = "status") String status
    );

    @POST("transaction_add")
    Call<NewBuxferTransactionResponse> addTransfer(
            @Query(value = "description") String description,
            @Query(value = "amount") Double startDate,
            @Query(value = "fromAccountId") String fromAccountId,
            @Query(value = "toAccountId") String toAccountId,
            @Query(value = "date") String date,
            @Query(value = "type") String type,
            @Query(value = "tags") String tags,
            @Query(value = "status") String status
    );

    @POST("transaction_delete")
    Call<DeleteTransactionResponse> deleteTransfer(@Query(value = "id") String id);
}
