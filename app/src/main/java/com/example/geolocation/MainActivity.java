package com.example.geolocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public final int LOCATION_PERMISSION = 1001;

    Button showMapButton;
    TextView latText, lonText, timeText;

    LocationManager locationManager;
    Location location;

    private boolean granted = false;

    //TODO описать listener
    LocationListener listener = new LocationListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if(provider.equals(LocationManager.GPS_PROVIDER))
                Toast.makeText(getApplicationContext(),
                        "Status: " + status,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            //что делать, если провайдер включен
            //прямое воздействие из программного кода запрещено
            if(granted || checkPermission())
                showLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }

        @Override
        public void onProviderDisabled(String provider) {
            //что делать, если провайдер выключен
        }
    };

    private void showLocation(Location location){
        if(location == null){
            return;
        }
        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
            this.location = location;
            String koord = String.format("%.4f", location.getLatitude());
            latText.setText(koord);
            koord = String.format("%.4f", location.getLongitude());
            lonText.setText(koord);
            koord = new Date(location.getTime()).toString();
            timeText.setText(koord);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showMapButton   =   findViewById(R.id.toMapButton);
        lonText         =   findViewById(R.id.lon);
        latText         =   findViewById(R.id.lat);
        timeText        =   findViewById(R.id.timeText);

        //TODO подключить менеджер местоположения
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                if(location != null) {
                    intent.putExtra("latitude", location.getLatitude());
                    intent.putExtra("longitude", location.getLongitude());
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Местоположение на определено",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO реализовать функцию получения координат с запросом разрешения
        if (granted || checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000 * 10, 20, listener);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null){
                    String koord = String.format("%.4f", location.getLatitude());
                    latText.setText(koord);
                    koord = String.format("%.4f", location.getLongitude());
                    lonText.setText(koord);
                    koord = new Date(location.getTime()).toString();
                    timeText.setText(koord);
                }
            }
        }
    }

    private boolean checkPermission(){
        //относится к опасным разрешениям, требует запроса. Функция вторична - не сразу
        //объяснить зачем разрешение, запрос только из активностей или фрагментов
        if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            //что делать, если разрешение не дано: попробовать запросить повторно
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
            return false;
        }
        return true;
    }

    //TODO переопределение функции обратного вызова для обработки ответа пользователя
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            granted = true;
            if (grantResults.length > 0) {
                for (int res : grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
                        granted = false;
                    }
                }
            } else {
                Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
                granted = false;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(listener);
    }
}
