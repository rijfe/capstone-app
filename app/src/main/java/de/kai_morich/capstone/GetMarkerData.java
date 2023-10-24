package de.kai_morich.capstone;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetMarkerData {
    String url = "http://121.159.178.99:8080/data/list/";
    //String user = "kyw@test.com";

    public ArrayList<MarkerData> getData(String email){
        ArrayList<MarkerData> dataArr = new ArrayList<MarkerData>();

        //RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), data.toString());
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
                    JSONArray array;
                    JSONObject object;
                    try {
                        array = new JSONArray(responseData);
                        for(int i=0; i<array.length(); i++){
                            object = array.getJSONObject(i);
                            Log.i("응답 바디", object.toString());
                            String type = object.getString("type");
                            String time = object.getString("time");
                            Double la = object.getDouble("latitude");
                            Double lo = object.getDouble("longitude");
                            int id = object.getInt("id");

                            dataArr.add(new MarkerData(time, la, lo, type,id));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        return dataArr;
    }


}
