package de.kai_morich.capstone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.Map;

import de.kai_morich.simple_bluetooth_le_terminal.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_map#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_map extends Fragment implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private MapView mapView;
    private View view;
    private ViewGroup viewGroup;
    Context ct;

    public Fragment_map() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_map.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_map newInstance(String param1, String param2) {
        Fragment_map fragment = new Fragment_map();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext();

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        ct = container.getContext();
        View balloonView = getLayoutInflater().inflate(R.layout.balloon_layout, null);

        try{
            PackageInfo info = ct.getPackageManager().getPackageInfo(ct.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        int permission = ContextCompat.checkSelfPermission(ct, Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한을 물어본다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
        }

        mapView = new MapView(ct);
        viewGroup = (ViewGroup) view.findViewById(R.id.mapView);
        viewGroup.addView(mapView);
        mapView.setMapViewEventListener((MapView.MapViewEventListener) this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setCalloutBalloonAdapter(new CustomBalloon());

        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(36.34974536587864,127.30010913328103);
        MapPOIItem maker = new MapPOIItem();
        maker.setItemName("난폭운전");
        maker.setTag(1);
        maker.setMapPoint(mapPoint);
        maker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        maker.setCustomImageResourceId(R.drawable.warning);
        maker.setCustomImageAutoscale(false);
        maker.setCustomImageAnchor(0.5f,1.0f);

        mapView.addPOIItem(maker);


        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if (check_result == false) {
                try {
                    finalize();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class CustomBalloon implements CalloutBalloonAdapter{
        private final View balloon;

        public CustomBalloon() {
            balloon = getLayoutInflater().inflate(R.layout.balloon_layout,null);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getCalloutBalloon(MapPOIItem mapPOIItem) {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String getTime = dateFormat.format(date);

            ((TextView) balloon.findViewById(R.id.type)).setText("차간 주행");
            ((TextView) balloon.findViewById(R.id.time)).setText(getTime);
            return balloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem mapPOIItem) {
            return balloon;
        }
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

//    @Override
//    public void onMapReady(@NonNull NaverMap naverMap)
//    {
//
//    }
}