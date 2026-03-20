package com.example.apptp1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private final String IP = "192.168.1.xxx"; // Remplacer par l'IP de votre interlocuteur
    private final int PORT = 10000;
    private InetAddress address;
    private DatagramSocket udpSocket;
    private Thread udpReceiveThread;
    private volatile boolean udpRunning;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximitySensor;
    private TextView textViewTop;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private EditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Récupération des références
        Button btn = findViewById(R.id.button);
        textViewTop = findViewById(R.id.textViewTop);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        messageInput = findViewById(R.id.messageInput);

        // Ajout du listener au bouton
        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                textView2.setText("Alors? c'est pas beau ça? :D");
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendUdpMessage(message);
                }
            }
        });

        // Initialisation du gestionnaire de capteurs
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Récupération des capteurs
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Enregistrement des écouteurs
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }

        initUdp();
        startUdpReceiver();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Affichage des données de l'accéléromètre
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            textView1.setText(String.format(Locale.US, "Accel X: %.2f", x));
            textView2.setText(String.format(Locale.US, "Accel Y: %.2f", y));
            textView3.setText(String.format(Locale.US, "Accel Z: %.2f", z));
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            // Affichage des données du capteur de proximité
            float proximity = event.values[0];
            textView1.setText("Proximité: " + proximity);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Rien à faire ici
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Réenregistrement des capteurs quand l'application reprend
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }

        initUdp();
        startUdpReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Désenregistrement des capteurs quand l'application est en pause
        sensorManager.unregisterListener(this);

        stopUdpReceiver();
    }

    private void initUdp() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            return;
        }
        if (IP.contains("xxx")) {
            textViewTop.setText("UDP: remplacez l'IP du serveur");
            return;
        }
        try {
            address = InetAddress.getByName(IP);
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(1000);
        } catch (SocketException | IOException exception) {
            textViewTop.setText("UDP error: " + exception.getMessage());
        }
    }

    private void startUdpReceiver() {
        if (udpSocket == null || udpSocket.isClosed() || udpReceiveThread != null) {
            return;
        }
        udpRunning = true;
        udpReceiveThread = new Thread(() -> {
            byte[] buffer = new byte[2048];
            while (udpRunning && udpSocket != null && !udpSocket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    udpSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    runOnUiThread(() -> textViewTop.setText(message));
                } catch (SocketTimeoutException timeout) {
                    // Timeout allows periodic checks of udpRunning.
                } catch (IOException exception) {
                    if (udpRunning) {
                        runOnUiThread(() -> textViewTop.setText("UDP error: " + exception.getMessage()));
                    }
                }
            }
        });
        udpReceiveThread.start();
    }

    private void stopUdpReceiver() {
        udpRunning = false;
        if (udpReceiveThread != null) {
            udpReceiveThread.interrupt();
            udpReceiveThread = null;
        }
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
    }

    private void sendUdpMessage(String message) {
        if (udpSocket == null || udpSocket.isClosed() || address == null) {
            textViewTop.setText("UDP: socket non initialisé");
            return;
        }
        new Thread(() -> {
            try {
                byte[] data = message.getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
                udpSocket.send(packet);
            } catch (IOException exception) {
                Log.d(TAG, "UDP send error", exception);
                runOnUiThread(() -> textViewTop.setText("UDP error: " + exception.getMessage()));
            }
        }).start();
    }
}