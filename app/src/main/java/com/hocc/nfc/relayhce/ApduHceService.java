package com.hocc.nfc.relayhce;

import android.content.Context;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class ApduHceService extends HostApduService {

    private  String SERVER_IP = null; // Replace with your server IP
    private static final int SERVER_PORT = 8888;
    private static final int TIMEOUT_MS = 500;

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        SERVER_IP = this.getSharedPreferences("NetworkPref", Context.MODE_PRIVATE).getString("IpAddress", "192.168.50.22");
        String hexCommand = bytesToHex(commandApdu);
        Log.d("Tag", "Received APDU:" + hexCommand);
        Log.d("Client", "Connecting to server IP: " + SERVER_IP);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> sendApduToServer(hexCommand));
        try {
            String responseHex = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            Log.d("Client", "Final Response: " + responseHex);
            return hexStringToByteArray(responseHex);
        } catch (Exception e) {
            Log.e("Client", "Timeout or error: " + e);
            return hexStringToByteArray("6F00"); // fallback response
        } finally {
            executor.shutdown();
        }
    }

    private String sendApduToServer(String messageToSend) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            Log.d("Client", "Connecting to server IP: " + SERVER_IP);
            Log.d("Client", "Local IP: " + socket.getLocalAddress().getHostAddress());

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(messageToSend); // send APDU
            String response = in.readLine(); // read response
            Log.d("Client", "Response from server: " + response);

            return response;
        } catch (IOException e) {
            Log.e("Client", "Network error: " + e);
            return "6F00"; // fallback on error
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d("HCE", "Deactivated: " + reason);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(" ", "");
        int len = s.length();
        if (len % 2 != 0) {
            s = "0" + s;
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
