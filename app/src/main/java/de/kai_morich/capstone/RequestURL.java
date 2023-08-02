package de.kai_morich.capstone;

import static de.kai_morich.capstone.Signup.email;
import static de.kai_morich.capstone.Signup.pwd;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RequestURL {
    public String request(String _url, ContentValues _params){
        HttpURLConnection urlConnection = null;
        StringBuffer stringBuffer = new StringBuffer();

        if(_params == null){
            stringBuffer.append("email="+email+"&password="+pwd);
        }
        else{
            boolean isAnd = false;

            String key;
            String value;

            for (Map.Entry<String, Object> param : _params.valueSet()){
                key = param.getKey();
                value = param.getValue().toString();

                if(isAnd){
                    stringBuffer.append("&");
                }

                stringBuffer.append(key).append("=").append(value);

                if(true){
                    if(_params.size() >= 2){
                        isAnd = true;
                    }
                }
            }
        }
        try {
            URL url = new URL(_url);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type", "application/json");
//            urlConnection.setRequestProperty();


            String strParam = stringBuffer.toString();
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password",pwd);
            OutputStream os = urlConnection.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();

            if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK){

                return String.valueOf(urlConnection.getResponseCode());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

            String line;
            String page = "";

            while ((line = reader.readLine()) != null){
                page += line;
            }

            return page;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return "nothing";
    }
}
