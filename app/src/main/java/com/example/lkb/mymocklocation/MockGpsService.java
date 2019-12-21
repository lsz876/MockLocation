//package com.example.service;
package com.example.lkb.mymocklocation;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

//import com.baidu.mapapi.model.LatLng;
//import com.example.mockgps.R;

import java.util.UUID;

public class MockGpsService extends Service {


    private String TAG = "MockGpsService";

    private LocationManager locationManager;
    private HandlerThread handlerThread;
    private Handler handler;

    private boolean isStop=true;

    //private String latLngInfo="104.0610342&30.5437233"; //成都长虹科技大厦
//    private static final double mockLat = 30.5437233;
//    private static final double mockLng = 104.0610342;

    //22.5699111089,113.8632630861 //shi dai cheng
    //22.5706261089,113.8627720861 //cai fu gang
    private static final double mockLat = 22.5706261089;
    private static final double mockLng = 113.8627720861;
    //private static final double mockLat = 22.5699111089;
    //private static final double mockLng = 113.8632630861;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        //remove default network location provider
        rmProvider();

        //add a new network location provider
        setGPSProvider();
//        setNewNetworkProvider();

        //thread
        handlerThread=new HandlerThread(getUUID(),-2);
        handlerThread.start();

        handler=new Handler(handlerThread.getLooper()){
            public void handleMessage(Message msg){
                setNetworkLocation();
                sendEmptyMessageDelayed(0,500);
            }
        };
        handler.sendEmptyMessage(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        DisplayToast("Mock Location Service Start");
        //

        String channelId = "channel_01";
        String name="channel_name";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW);
            Log.i(TAG, mChannel.toString());
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
            notification = new Notification.Builder(this)
                    .setChannelId(channelId)
                    .setContentTitle("模拟位置已经启动")
                    .setContentText("MyMockLocation service is running")
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("位置模拟服务已启动")
                    .setContentText("MyMockLocation service is running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setChannelId(channelId);//无效
            notification = notificationBuilder.build();
        }
        startForeground(1,notification);
        //

//        //get location info from mainActivity
//        latLngInfo=intent.getStringExtra("key");
//        Log.d(TAG, "dataFromMain is "+latLngInfo);

        //start to refresh location
        isStop=false;

//        return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
//        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();

//        DisplayToast("Mock Loction Service finish");
        isStop=true;

        handler.removeMessages(0);
        handlerThread.quit();
        rmProvider();

        stopForeground(true);

        super.onDestroy();
    }


    //generate a location
    public Location generateLocation(double lat, double lng) {
        Location loc = new Location(LocationManager.GPS_PROVIDER); //(LocationManager.GPS_PROVIDER);

        loc.setAccuracy(2.0F);
        loc.setAltitude(55.0D);
        loc.setBearing(1.0F);
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", 7);
        loc.setExtras(bundle);

        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setAccuracy(1.0F);
        loc.setAltitude(10);
        loc.setBearing(90);
        loc.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= 17) {
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
//        Log.d("WATCH",loc.toString());
        return loc;
    }

    //set network location
    private void  setNetworkLocation() {
        //default location 30.5437233 104.0610342 成都长虹科技大厦
        try {
//            locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, generateLocation(mockLat, mockLng));
            //for test
//            113.954226
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, generateLocation(22.5607, 113.954226));
        } catch (Exception e) {
            Log.d(TAG, "setNetworkLocation error");
            e.printStackTrace();
        }
    }

    //remove network provider
    private void rmProvider(){
        try {
            String providerStr = LocationManager.NETWORK_PROVIDER;
            if (locationManager.isProviderEnabled(providerStr)){
                Log.d(TAG, "now remove NetworkProvider");
//                locationManager.setTestProviderEnabled(providerStr,true);
                locationManager.removeTestProvider(providerStr);
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "rmNetworkProvider error");
        }
    }

    //set new network provider
    private void setNewNetworkProvider(){
        String providerStr = LocationManager.NETWORK_PROVIDER;
        try {
            locationManager.addTestProvider(providerStr, false, false,
                    false, false, false, false,
                    false, 1, Criteria.ACCURACY_FINE);
            Log.d(TAG,"addTestProvider[network] success");
            locationManager.setTestProviderStatus("network", LocationProvider.AVAILABLE, null,//lkb
                    System.currentTimeMillis());
        }catch (SecurityException e){
            Log.d(TAG,"setNewNetworkProvider error");
        }
        if (!locationManager.isProviderEnabled(providerStr)){
            Log.d(TAG, "now  setTestProviderEnabled[network]");
            locationManager.setTestProviderEnabled(providerStr,true);
        }
    }

    // for test: set GPS provider
    private void setGPSProvider(){
        LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (provider != null) {
            locationManager.addTestProvider(
                    provider.getName()
                    , provider.requiresNetwork()
                    , provider.requiresSatellite()
                    , provider.requiresCell()
                    , provider.hasMonetaryCost()
                    , provider.supportsAltitude()
                    , provider.supportsSpeed()
                    , provider.supportsBearing()
                    , provider.getPowerRequirement()
                    , provider.getAccuracy());
        } else {
            locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false,
                    false, true, true, true, 0, 5);
        }

        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        //新
        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null,
                System.currentTimeMillis());
    }


    //uuid random
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    //get service
    public class ServiceBinder extends Binder {
        public MockGpsService getService() {
            return MockGpsService.this;
        }
    }

    public void DisplayToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 220);
        toast.show();
    }

}


