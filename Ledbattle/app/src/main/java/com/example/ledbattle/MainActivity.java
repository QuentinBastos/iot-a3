package com.example.ledbattle;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private final int PORT = 10000;
    private InetAddress address;
    private DatagramSocket udpSocket;
    private Thread udpReceiveThread;
    private volatile boolean udpRunning;

    private TextView textViewTop, tvSensor;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate = 0;
    private static final int SHAKE_THRESHOLD = 15;

    private EditText etIp;
    private String currentPlayerMsg = "(1)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialisation UI
        textViewTop = findViewById(R.id.textViewTop);
        tvSensor = findViewById(R.id.tvSensor);
        etIp = findViewById(R.id.etIp);
        findViewById(R.id.etPort);
        RadioButton rbPlayer1 = findViewById(R.id.rbPlayer1);
        RadioButton rbPlayer2 = findViewById(R.id.rbPlayer2);
        Button btnReset = findViewById(R.id.btnReset);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Capteurs
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Listeners
        btnReset.setOnClickListener(v -> sendUdpMessage("(0)"));
        rbPlayer1.setOnClickListener(v -> currentPlayerMsg = "(1)");
        rbPlayer2.setOnClickListener(v -> currentPlayerMsg = "(2)");
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Axe horizontal
            long curTime = System.currentTimeMillis();

            // On ne traite le signal que si le délai anti-spam est passé
            if (curTime > lastUpdate) {

                // "Rotation gauche" : on penche le téléphone vers la gauche
                // x devient positif. On teste si x dépasse un seuil raisonnable (ex: 5.0)
                if (x > 5.0f) {
                    sendUdpMessage(currentPlayerMsg);

                    // On bloque les envois pendant 300ms pour qu'un seul coup
                    // ne fasse bouger la LED que d'une seule case
                    lastUpdate = curTime + 300;

                    tvSensor.setText("Coup envoyé ! (" + currentPlayerMsg + ")");
                } else {
                    // Affiche la valeur en temps réel pour t'aider à calibrer
                    tvSensor.setText("Inclinaison X: " + String.format("%.2f", x));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        initUdp();
        startUdpReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopUdpReceiver();
    }

    @SuppressLint("SetTextI18n")
    private void initUdp() {
        new Thread(() -> {
            try {
                String ip = etIp.getText().toString();
                address = InetAddress.getByName(ip);
                if (udpSocket == null || udpSocket.isClosed()) {
                    udpSocket = new DatagramSocket();
                    udpSocket.setSoTimeout(1000);
                }
            } catch (IOException e) {
                runOnUiThread(() -> textViewTop.setText("Erreur Init: " + e.getMessage()));
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
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