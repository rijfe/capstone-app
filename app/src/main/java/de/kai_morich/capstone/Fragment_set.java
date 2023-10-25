package de.kai_morich.capstone;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import de.kai_morich.simple_bluetooth_le_terminal.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_set#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_set extends Fragment {
    private View view;
    String email;
    GetUserData getUserData = new GetUserData();
    String rank;
    String name;

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
        rank = getArguments().getString("rank");
        name = getArguments().getString("name");
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
        if ("안전".equals(rank)) {
            imageView.setImageResource(R.drawable.greenbike);
            rankText.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("위험".equals(rank)) {
            imageView.setImageResource(R.drawable.redbike);
            rankText.setTextColor(Color.parseColor("#F31100"));
        } else if ("주의".equals(rank)) {
            imageView.setImageResource(R.drawable.yellowbike);
            rankText.setTextColor(Color.parseColor("#FF9800"));
        }


        return view;
    }
}