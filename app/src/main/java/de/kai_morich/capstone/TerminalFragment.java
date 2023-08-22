package de.kai_morich.capstone;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayDeque;

import de.kai_morich.simple_bluetooth_le_terminal.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;
    private String sensorData;

    private TextView receiveText, angleText, right, statusText, closeText, zigzagText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;
    ImageView imageView;

    int closeAmount = 0, zigzagAmount = 0;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        angleText = view.findViewById(R.id.angle);
        right = view.findViewById(R.id.another);
        statusText = view.findViewById(R.id.status);
        closeText = view.findViewById(R.id.amountclose);
        zigzagText = view.findViewById(R.id.amountzigzag);
        imageView = view.findViewById(R.id.img);
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
        angleText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        angleText.setMovementMethod(ScrollingMovementMethod.getInstance());

        right.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        right.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = view.findViewById(R.id.send_text);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));


        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(ArrayDeque<byte[]> datas) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        SpannableStringBuilder r= new SpannableStringBuilder();
        SpannableStringBuilder angle = new SpannableStringBuilder();
        String msg;
        String[] arr = new String[0];
        String[] distanceL = new String[0];
        String[] distanceR = new String[0];
        String[] angleArr = new String[0];
        String url = "http://172.20.10.7:8080/data/endpost/";
        OkHttpClient client = new OkHttpClient();

        for (byte[] data : datas) {
            msg = new String(data);
            arr = msg.split("/");
            if (hexEnabled) {
                spn.append(TextUtil.toHexString(data)).append('\n');
            } else {
                if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                    // don't show CR as ^M if directly before LF
                    msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                    // special handling if CR and LF come in separate fragments
                    if (pendingNewline && msg.charAt(0) == '\n') {
                        if(spn.length() >= 2) {
                            spn.delete(spn.length() - 2, spn.length());
                        } else {
                            Editable edt = receiveText.getEditableText();
                            if (edt != null && edt.length() >= 2)
                                edt.delete(edt.length() - 2, edt.length());
                        }
                    }
                    pendingNewline = msg.charAt(msg.length() - 1) == '\r';
                }
                distanceR = arr[0].split("-");
                spn.append(TextUtil.toCaretString(distanceR[0], newline.length() != 0));
                distanceL = arr[1].split("-");
                angleArr = arr[2].split("-");
                r.append(distanceL[0]);
                angle.append(angleArr[0]);
            }
        }
        if(Float.parseFloat(angle.toString()) > 40.0 && angleArr[0]!=null){
            imageView.setImageResource(R.drawable.bir);
            angleText.setTextColor(getResources().getColor(R.color.colorPrimaryDark)); // set as default color to reduce number of spans
            angleText.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("지그재그 운행중");
            if(Integer.parseInt(angleArr[1]) >= 3){
                // data 보내기
            }
        }
        if(Float.parseFloat(angle.toString()) < -40.0 && angleArr[0]!=null ){
            imageView.setImageResource(R.drawable.bil);
            angleText.setTextColor(getResources().getColor(R.color.colorPrimaryDark)); // set as default color to reduce number of spans
            angleText.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("지그재그 운행중");
            if(Integer.parseInt(angleArr[1]) >= 3){
                // data 보내기
            }
        }
        if(Float.parseFloat(angle.toString())>-40.0 && Float.parseFloat(angle.toString()) < 40.0 && angleArr[0] != null){
            imageView.setImageResource(R.drawable.bi);
            angleText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
            angleText.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("안전 운전중");
        }
        if(Float.parseFloat(spn.toString())<=40.0 && distanceR[0]!=null){
            imageView.setImageResource(R.drawable.closel);
            receiveText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("차간 운행중");

            if(Integer.parseInt(distanceR[1]) >= 3){
                // data 보내기
            }
        }
        if(Float.parseFloat(r.toString())<=40.0 && distanceL[0]!=null){
            imageView.setImageResource(R.drawable.closer);
            right.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            right.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("차간 운행중");
            if(Integer.parseInt(distanceL[1]) >= 3){
                // data 보내기
            }
        }
        if(Float.parseFloat(r.toString())<=40.0 && distanceL[0]!=null && Float.parseFloat(spn.toString())<=40.0 && distanceR[0]!=null) {
            imageView.setImageResource(R.drawable.closeboth);
            right.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            right.setMovementMethod(ScrollingMovementMethod.getInstance());
            receiveText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("차간 운행중");
            if(Integer.parseInt(distanceL[1]) >= 3 && Integer.parseInt(distanceR[1]) >= 3){
                // data 보내기
            }
        }

        if(Float.parseFloat(r.toString()) > 40.0 && distanceL[0]!=null && Float.parseFloat(spn.toString())>40.0 && distanceR[0]!=null){
            imageView.setImageResource(R.drawable.bi);
            right.setTextColor(getResources().getColor(R.color.colorRecieveText));
            right.setMovementMethod(ScrollingMovementMethod.getInstance());
            receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText));
            receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());
            statusText.setText("안전 운전중");
        }



        receiveText.setText(spn);
        right.setText(r);
        angleText.setText(angle);
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Toast.makeText(service.getApplicationContext(), str, Toast.LENGTH_SHORT).show();;
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        ArrayDeque<byte[]> datas = new ArrayDeque<>();
        datas.add(data);
        receive(datas);
    }

    public void onSerialRead(ArrayDeque<byte[]> datas) {
        receive(datas);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

//    public void sendStatus(String s){
//        String url = "http://172.17.33.117:8080/data/endpost/";
//
//        JSONObject data = new JSONObject();
//        try {
//            data.put("type", s);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), data.toString());
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder().addHeader("Content-Type","application/json").url(url).post(body).build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if(!response.isSuccessful()) {
//                    Log.i("tag", "응답 실패");
//                }else{
//                    Log.i("tag","응답 성공");
//                    final String responseData = response.body().string();
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Toast.makeText(service, "응답"+responseData, Toast.LENGTH_SHORT).show();
//
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }
//            }
//
//        });
//    }

    private void runOnUiThread(Runnable runnable) {

    }

}
