package de.kai_morich.capstone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.kai_morich.simple_bluetooth_le_terminal.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Signup extends AppCompatActivity {

    public Button signup;
    public EditText idText, pwdText;
    public static String email, pwd;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        signup = (Button) findViewById(R.id.suBtn);
        idText = (EditText) findViewById(R.id.sign_id);
        pwdText = (EditText) findViewById(R.id.sign_pw);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = idText.getText().toString();
                pwd = pwdText.getText().toString();
                String url = "http://172.20.10.7:8080/user/signup1/";

                JSONObject data = new JSONObject();
                try {
                    data.put("email", email);
                    data.put("password",pwd);
                    data.put("bike_num", "1234");
                    data.put("phone_num", "01012341234");
                    data.put("name", "hi");
                    data.put("birth", "0000000");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


                //RequestBody body = new FormBody.Builder().add("email", email).add("password", pwd).build();
                RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), data.toString());
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().addHeader("Content-Type","application/json").url(url).post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if(!response.isSuccessful()) {
                            Log.i("tag", "응답 실패");
                        }else{
                            Log.i("tag","응답 성공");
                            final String responseData = response.body().string();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(getApplicationContext(), "응답"+responseData, Toast.LENGTH_SHORT).show();

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                            Intent intent = new Intent(Signup.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
//                NwTask nwTask = new NwTask(url, null);
//                nwTask.execute();

            }
        });
    }

//    public class NwTask extends AsyncTask<Void, Void, String> {
//
//        private String url;
//        private ContentValues values;
//
//        public NwTask(String url, ContentValues values){
//            this.url = url;
//            this.values = values;
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            String result;
//            RequestURL requestURL = new RequestURL();
//            result = requestURL.request(url, values);
//            System.out.println("pwd="+pwd);
//            System.out.println("email="+email);
//            System.out.println("signup="+result);
////            AlertDialog.Builder dlg = new AlertDialog.Builder(Signup.this);
////            dlg.setTitle("requset");
////            dlg.setMessage(result);
//
////            dlg.show();
//
//            return result;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }
//    }
}
