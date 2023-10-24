package de.kai_morich.capstone;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.kai_morich.simple_bluetooth_le_terminal.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class
MainActivity extends AppCompatActivity{

    Button signIn, signUp;

    EditText idText, pwText;

    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        signIn = (Button) findViewById(R.id.login);
        signUp = (Button) findViewById(R.id.signup);

        idText = (EditText) findViewById(R.id.id);
        pwText = (EditText) findViewById(R.id.pwd);

        ImageView img = (ImageView) findViewById(R.id.testImg);

        Animation rotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);

        signIn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String user = null;
//                getAppKeyHash();
                String url = "http://121.159.178.99:8080/login/";
                String id = idText.getText().toString();
                String pw = pwText.getText().toString();
                JSONObject data = new JSONObject();
                try {
                    data.put("email", id);
                    data.put("password", pw);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), data.toString());
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().addHeader("Content-Type", "application/json").url(url).post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.i("tag", "응답 실패");
                        } else {
                            Log.i("tag", "응답 성공");
                            final String responseData = response.body().string();
                            Intent intent = new Intent(MainActivity.this, MainScreen.class);
                            intent.putExtra("user",id);
                            startActivity(intent);
                            finish();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(getApplicationContext(), "응답" + responseData, Toast.LENGTH_SHORT).show();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });

            }
//            }
//        });

//                Intent intent = new Intent(MainActivity.this, MainScreen.class);
//                startActivity(intent);
//            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                img.startAnimation(rotation);
                Intent intent = new Intent(MainActivity.this, Signup.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void getAppKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures){
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(),0));
                Log.e("Hash key:", something);
            }
        }catch (PackageManager.NameNotFoundException e){
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
