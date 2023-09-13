package de.kai_morich.capstone;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetMarkerData {
    String url = "";
    String user = "";

    public ArrayList<MarkerData> getData(){
        ArrayList<MarkerData> dataArr = new ArrayList<MarkerData>();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream("user.dat");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        DataInputStream dis = new DataInputStream(fis);

        try {
            user = dis.readUTF();
            dis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), data.toString());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().addHeader("Content-Type","application/json").url(url).get().build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseBody responseBody = null;
        if(response.isSuccessful()){
            responseBody = response.body();
            if(responseBody != null){
                System.out.println("Response:" + responseBody.toString());
            }
        }

        return dataArr;
    }


}
