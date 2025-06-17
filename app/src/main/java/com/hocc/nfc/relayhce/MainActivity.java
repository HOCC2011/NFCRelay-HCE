package com.hocc.nfc.relayhce;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    EditText IPAdress;
    TextView enter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IPAdress = findViewById(R.id.ipAddress);
        enter = findViewById(R.id.enter);
        enter.setOnClickListener(v -> {
            this.getSharedPreferences("NetworkPref", MODE_PRIVATE).edit() // All in string
                    .putString("IpAddress", IPAdress.getText().toString())
                    .apply();
            Toast.makeText(this, "Successfully saved the new ip address to the app.", Toast.LENGTH_LONG).show();
        });
    }
}

