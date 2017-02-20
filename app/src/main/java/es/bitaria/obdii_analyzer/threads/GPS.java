package es.bitaria.obdii_analyzer.threads;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.Timer;
import java.util.TimerTask;

import es.bitaria.obdii_analyzer.dataStructures.GPSData;
import es.bitaria.obdii_analyzer.R;

/**
 * Creado por Pedro Amador Diaz el 04/01/2017.
 */

public class GPS extends Thread implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private Context mContext;
    private static final String TAG = "GPS"; // Identificador del proceso
    public static final int ID = 1;
    public static final int ACCURACY_STATE = 1;
    public static final int GPS_DATA = 2;
    private final Handler mHandler;
    private Message mMessage;

    private GPSData mGPSData;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mLocationClient;
    private int mAccuracyState, mOldAccuracyState;

    private boolean isRunning = false;

    public GPS() {
        mHandler = null;
    }

    // Constructor. Preparamos un nuevo hilo para el proceso GPS
    public GPS(Context context, Handler handler) {

        this.mContext = context;
        mHandler = handler;
    }

    @Override
    public void interrupt() {
        super.interrupt();

        isRunning = false;

        // Cerramos el servicio de localizacion
        if( mLocationClient.isConnected()){
            mLocationClient.disconnect();
        }
    }

    @Override
    public void run() {
        super.run();

        isRunning = true;

        // Inicializacion del servicio de localizacion
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Actualizar la localizacion cada segundo
        mLocationClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationClient.connect();

        // Miembro que almacena los datos de localizacion proporcionados por los servicios de localizacion
        mGPSData = new GPSData();

        // Inicializacion del timer para el mandado periodico de la localizacion al hilo principal
        while (isRunning){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Mandamos el objeto mGPSData al hilo principal para que sea procesado
            mMessage = new Message();
            mMessage.what = ID;
            mMessage.arg1 = GPS_DATA;
            mMessage.obj = mGPSData;
            mHandler.sendMessage(mMessage);
            Log.d(TAG, "GPSData mandado al hilo principal");
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mGPSData.latitude = (float) location.getLatitude();
        mGPSData.longitude = (float) location.getLongitude();
        mGPSData.altitude = (float) location.getAltitude();
        mGPSData.bearing = location.getBearing();
        mGPSData.speed = location.getSpeed();
        mGPSData.accuracy = location.getAccuracy();

        // Estimamos la precision de la localizacion para pasarla al GUI si ha cambiado de forma significativa
        if (location.getAccuracy() < 10) {
            mAccuracyState = R.string.high;
        } else if (location.getAccuracy() >= 10 && location.getAccuracy() <= 20) {
            mAccuracyState = R.string.mean;
        } else {
            mAccuracyState = R.string.low;
        }
        // Si ha cambiado la precision de la localizacion, mandamos la nueva presicion al hilo principal para que la muestre en la UI
        if (mOldAccuracyState != mAccuracyState || mOldAccuracyState==0) {
            mMessage = new Message();
            mMessage.what = ID;
            mMessage.arg1 = ACCURACY_STATE;
            mMessage.obj = mAccuracyState;
            mHandler.sendMessage(mMessage);
            Log.d(TAG, "Accuracy: " + mContext.getString(mAccuracyState));
            mOldAccuracyState = mAccuracyState;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Location client is connected: " + mLocationClient.isConnected());
        if (ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
