package com.example.locationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final int SEND_SMS_PERMISSION_REQUEST = 0;
    private static final String TAG = "MainActivity";

    private TextView locationText;
    private Button locationBtn;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        locationBtn = findViewById(R.id.locationBtn);
        locationText = findViewById(R.id.locationText);

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocationPermission();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(), "Application will not run without location permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocationPermission(){

        Log.d(TAG, "requestLocationPermission: requesting location permission");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED){
                getLocation();
            }
            else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                    Toast.makeText(getApplicationContext(), "Application required to access location", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            getLocation();
        }

    }

    void getLocation(){

        Log.d(TAG, "getLocation: fetching location");

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        }
        catch (SecurityException e){
            e.printStackTrace();
            Log.e(TAG, "getLocation: " + e.getMessage());
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location location) {

        String locationString = "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude();

        locationText.setText(locationString);

        try{
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            locationString = locationText.getText()+ "\n" + addresses.get(0).getAddressLine(0) + "\n"
                    + addresses.get(0).getAddressLine(1) + "\n" + addresses.get(0).getAddressLine(2);

            locationText.setText(locationString);

            email();
            SMS();
        }
        catch (Exception e){

            e.printStackTrace();
            Log.d(TAG, "OnLocationChanged: " + e.getMessage());

            Toast.makeText(getApplicationContext(), locationString, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Please enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    private void email(){

        new Thread(new Runnable() {
            public void run() {
                try {
                    GMailSender sender = new GMailSender("devr52222@gmail.com", "dev2242000");

                    sender.addAttachment(Environment.getExternalStorageDirectory().getPath() + "/IMG-20191212-WA0000.jpg");

                    sender.sendMail("Test mail", "This mail has been sent from Location Tracking app along with attachment",
                            "devr52222@gmail.com",
                            "devrahul2215@gmail.com");

                } catch (Exception e) {

                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
                    Log.d(TAG, "email: failed to send mail");
                }
            }
        }).start();
    }

    private void SMS(){

        if (checkSMSpermission() && checkPhoneStatePermission()){
            sendSMS();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE}, SEND_SMS_PERMISSION_REQUEST);
        }
    }

    public boolean checkSMSpermission(){

        int check = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        return (check == PackageManager.PERMISSION_GRANTED);

    }

    public boolean checkPhoneStatePermission(){

        int check = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        return (check == PackageManager.PERMISSION_GRANTED);

    }

    private void sendSMS(){
        Log.d(TAG, "sendSMS: trying to send SMS");

        try {
            String destPhone = "6265105303";
            String message = locationText.getText().toString();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(destPhone, null, message, null, null);
        }
        catch (Exception e){
            Log.d(TAG, "sendSMS: failed to send SMS" + e.getMessage());
        }
    }
}







