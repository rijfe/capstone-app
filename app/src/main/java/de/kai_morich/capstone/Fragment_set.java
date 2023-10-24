package de.kai_morich.capstone;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.kai_morich.simple_bluetooth_le_terminal.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_set#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_set extends Fragment {
    GetUserData getUserData = new GetUserData();


    private View view;
    String email;

    String rank = getUserData.getUserRank(email);
    String name;
    String url = "http://121.159.178.99:8080/list/";


//    GetUserData getUserData = new GetUserData();
//
//    UserData data = getUserData.getUserData(email);

    ImageView imageView;

    TextView nameText, rankText;

    public Fragment_set() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_set.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_set newInstance(String param1, String param2) {
        Fragment_set fragment = new Fragment_set();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        email  = getArguments().getString("user");

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_set, container, false);

        imageView = (ImageView) view.findViewById(R.id.set_img);
        nameText = (TextView) view.findViewById(R.id.set_name);
        rankText = (TextView) view.findViewById(R.id.set_rank);

        nameText.setText(name+"님");
        rankText.setText(rank + "등급");
//
//        if(rank == "안전"){
//            imageView.setImageResource(R.drawable.greenbike);
//            rankText.setTextColor(Color.parseColor("#4CAF50"));
//        }
//
//        if(rank == "위험"){
//            imageView.setImageResource(R.drawable.redbike);
//            rankText.setTextColor(Color.parseColor("#F31100"));
//        }
//
//        if(rank == "주의"){
//            imageView.setImageResource(R.drawable.yellowbike);
//            rankText.setTextColor(Color.parseColor("#FF9800"));
//        }





        return view;
    }
}