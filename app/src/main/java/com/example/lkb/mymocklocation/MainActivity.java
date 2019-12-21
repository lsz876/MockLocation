package com.example.lkb.mymocklocation;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int REQUESTCODE = 101;
    private static final String TAG = "LKB";
    LocationManager mLocationManager;
    Location mlocation;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);
    }

    public void onClick_getLocation(View view) {
        Log.d(TAG, "click get location");
        getLocation();
        //mlocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
		//mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mlocation);
    }


    public void getLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUESTCODE);
            return ;
        }
        mlocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mlocation == null) {
            Log.d("lkb", "can not get last location with GPS, so to get with network");
            mlocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, mLocationListener);
        mLocationManager.addNmeaListener(mNmeaListener);
        //return mlocation;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTCODE){
            Log.d("lkb", "onRequestResult "+grantResults[0]+" "+grantResults[1]);
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("lkb", "user granted");
            }else{
                Log.d("lkb", "user not granted");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //if (!shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                    //AskForPermission(); //
                    //}
                }
            }
        }
    }

    //这里获取的数据就是在之前一个activity写进去的数据
    LocationListener mLocationListener = new LocationListener() {
        //这个数据是经过LocationManager
        //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onLocationChanged(Location mlocal) {
            if (mlocal == null) return;
            String strResult = "getAccuracy:" + mlocal.getAccuracy() + "\r\n"
                    + "getAltitude:" + mlocal.getAltitude() + "\r\n"
                    + "getBearing:" + mlocal.getBearing() + "\r\n"
                    + "getElapsedRealtimeNanos:" + String.valueOf(mlocal.getElapsedRealtimeNanos()) + "\r\n"
                    + "getLatitude:" + mlocal.getLatitude() + "\r\n"
                    + "getLongitude:" + mlocal.getLongitude() + "\r\n"
                    + "getProvider:" + mlocal.getProvider() + "\r\n"
                    + "getSpeed:" + mlocal.getSpeed() + "\r\n"
                    + "getTime:" + mlocal.getTime() + "\r\n";
            Log.i("Show", strResult);
            if (mTextView != null) {
                mTextView.setText(strResult);
            }

        }

        @Override
        public void onProviderDisabled(String arg0) {
        }

        @Override
        public void onProviderEnabled(String arg0) {
        }

        @Override
        public void onStatusChanged(String provider, int event, Bundle extras) {
            if (event ==100){
                String strResult = extras.getString("test1","") +"\n" +
                        extras.getString("test2","");
            }

        }
    };

    //原始数据监听
    GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long arg0, String arg1) {
            //Log.d("lkb", "nmea data");
            byte[] bytes = arg1.getBytes();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationManager!=null && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.removeNmeaListener(mNmeaListener);
            mLocationManager.removeUpdates(mLocationListener);
        }

    }

    public void onClick_mock(View view) {
        Intent mockLocServiceIntent = new Intent(MainActivity.this, MockGpsService.class);
        //mockLocServiceIntent.putExtra("key", latLngInfo);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(mockLocServiceIntent);
            Log.d(TAG, "startForegroundService");
        } else {
            startService(mockLocServiceIntent);
            Log.d(TAG, "startService");
        }
    }

    //打开开发者模式
    public void openTestProviderLocationException() {
        Intent intent = new Intent("//");
        ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        startActivity(intent);
    }
    public void onClick_developer_options(View view) {
        openTestProviderLocationException();
    }
}


