package com.example.lab5;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import java.io.FileOutputStream;
import androidx.annotation.NonNull;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import androidx.appcompat.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import java.io.File;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_SHOW_POPUP = "show_popup";
    private static final int REQUEST_CODE = 1;
    private EditText journalIdEditText;
    private Button viewButton, deleteButton;
    private String downloadedFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        journalIdEditText = findViewById(R.id.journalIdEditText);
        Button downloadButton = findViewById(R.id.downloadButton);
        viewButton = findViewById(R.id.viewButton);
        deleteButton = findViewById(R.id.deleteButton);

        downloadButton.setOnClickListener(v -> downloadFile());
        viewButton.setOnClickListener(v -> openFile());
        deleteButton.setOnClickListener(v -> deleteFile());

        checkShowPopup();
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на чтение файлов предоставлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешение на чтение файлов не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile() {
        String journalId = journalIdEditText.getText().toString().trim();
        if (journalId.isEmpty()) {
            Toast.makeText(this, "Введите ID журнала", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileUrl = "https://ntv.ifmo.ru/file/journal/" + journalId + ".pdf";

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            InputStream is = null;
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                String contentType = conn.getContentType();
                Log.d("MainActivity", "Response Code: " + responseCode);
                Log.d("MainActivity", "Content Type: " + contentType);

                if (responseCode == HttpURLConnection.HTTP_OK && "application/pdf".equals(contentType)) {
                    File dir = new File(getExternalFilesDir(null), "MyJournalFiles");
                    if (!dir.exists() && !dir.mkdirs()) {
                        Log.e("MainActivity", "Не удалось создать директорию: " + dir.getAbsolutePath());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка при создании директории", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    File file = new File(dir, "journal_" + journalId + ".pdf");
                    is = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();

                    downloadedFilePath = file.getAbsolutePath();
                    Log.d("MainActivity", "Файл загружен по пути: " + downloadedFilePath);

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Файл загружен", Toast.LENGTH_SHORT).show();
                        viewButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);
                    });
                } else {
                    Log.e("MainActivity", "Ошибка: " + responseCode + " " + contentType);
                    String fallbackUrl = "https://ntv.ifmo.ru/file/journal/2.pdf";
                    runOnUiThread(() -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                        startActivity(browserIntent);
                    });
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Ошибка загрузки файла: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка загрузки файла: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        Log.e("MainActivity", "Ошибка при закрытии InputStream", e);
                    }
                }
            }
        }).start();
    }

    private void openFile() {
        if (downloadedFilePath != null) {
            File file = new File(downloadedFilePath);
            if (file.exists()) {
                Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Нет приложения для открытия PDF", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Файл не загружен", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFile() {
        if (downloadedFilePath != null) {
            File file = new File(downloadedFilePath);
            if (file.exists() && file.delete()) {
                Toast.makeText(this, "Файл удален", Toast.LENGTH_SHORT).show();
                viewButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                downloadedFilePath = null;
            } else {
                Toast.makeText(this, "Ошибка при удалении файла", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ошибка: файл не выбран", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    private void checkShowPopup() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean showPopup = preferences.getBoolean(KEY_SHOW_POPUP, true);
        if (showPopup) {
            showPopupWindow();
        }
    }

    private void showPopupWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Инструкция")
                .setMessage("Это приложение позволяет скачивать и просматривать журналы Научно-технического вестника.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Не показывать снова", (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    preferences.edit().putBoolean(KEY_SHOW_POPUP, false).apply();
                    dialog.dismiss();
                })
                .show();
    }
}