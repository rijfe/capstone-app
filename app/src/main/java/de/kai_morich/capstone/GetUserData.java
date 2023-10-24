package de.kai_morich.capstone;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetUserData {
    String url = "http://121.159.178.99:8080/list/";
    String rank, name;


    public String getUserRank(String email){

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("Content-Type","application/json").url(url+email).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseData = response.body().string();
                    Log.i("test", responseData);
                    try {
                        JSONObject object = new JSONObject(responseData);
                        Log.i("rank",object.getString("Rank"));
                        Log.i("rank",object.getJSONObject("UserInfo").getString("name"));

                        rank = object.getString("Rank");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return rank;
    }

    public String getUserName(String email){

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("Content-Type","application/json").url(url+email).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseData = response.body().string();
                    Log.i("test", responseData);
                    try {
                        JSONObject object = new JSONObject(responseData);
                        Log.i("rank",object.getString("Rank"));
                        Log.i("rank",object.getJSONObject("UserInfo").getString("name"));

                        name = object.getString("Rank");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return name;
    }
}
