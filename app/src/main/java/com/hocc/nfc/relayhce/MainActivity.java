package com.hocc.nfc.relayhce;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    EditText IPAdress;
    TextView Enter;
    TextView Logs;
    TextView ClearLogs;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IPAdress = findViewById(R.id.ipAddress);
        Logs = findViewById(R.id.log);
        Enter = findViewById(R.id.enter);
        Enter.setOnClickListener(v -> {
            this.getSharedPreferences("NetworkPref", MODE_PRIVATE).edit() // All in string
                    .putString("IpAddress", IPAdress.getText().toString())
                    .apply();
            Toast.makeText(this, "Successfully saved the new ip address to the app.", Toast.LENGTH_LONG).show();
        });
        ClearLogs = findViewById(R.id.clearLogs);
        ClearLogs.setOnClickListener(v -> {
            Logs.setText("Logs:");
        });
        stopService(new Intent(this, ForegroundService.class));
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        startForegroundService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(
                LogReceiver_Reader,
                new IntentFilter("com.hocc.nfc.relayhce.logApdu"),
                Context.RECEIVER_NOT_EXPORTED
        );
        registerReceiver(
                LogReceiver_Card,
                new IntentFilter("com.hocc.nfc.relayhce.logResponse"),
                Context.RECEIVER_NOT_EXPORTED
        );
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(LogReceiver_Reader);
        unregisterReceiver(LogReceiver_Card);
    }

    private final BroadcastReceiver LogReceiver_Reader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LogReceiver", "Received Broadcast!");
            LoadData(0);
        }
    };

    private final BroadcastReceiver LogReceiver_Card = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("LogReceiver", "Received Broadcast!");
            LoadData(1);
        }
    };

    private void LoadData(int ByteSource) {
        if(ByteSource == 0) {
            SharedPreferences pref = this.getSharedPreferences("Logs", MODE_PRIVATE);
            String returnBytes = pref.getString("receivedBytes", "6A 00");
            Logs.setText(Logs.getText().toString() + "\n\nReader:\n" + returnBytes);
        } else {
            SharedPreferences pref = this.getSharedPreferences("Logs", MODE_PRIVATE);
            String returnBytes = pref.getString("returnBytes", "6A 00");
            Logs.setText(Logs.getText().toString() + "\n\nCard:\n" + returnBytes);
        }
    }

}

