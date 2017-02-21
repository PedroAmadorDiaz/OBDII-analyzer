/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.bitaria.obdii_analyzer.threads;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import es.bitaria.obdii_analyzer.dataStructures.IMUData;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Creado por Pedro Amador Diaz el 05/01/2017.
 */

public class IMU extends Thread implements SensorEventListener {
    private Context mContext;
    private static final String TAG= "IMU"; // Identificador del proceso
    public static final int ID = 3;
    public static final int IMU_DATA = 2;
    private final Handler mHandler;
    private Message mMessage;

    static final float ALPHA = 0.5f; // ALPHA = de  1 a 0, 0 no aplica filtro.

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGyroscope, mMagneticField;

    private IMUData mIMUData;

    private boolean isRunning = false;

    public IMU()
    {
        mHandler = null;
    }

    // Constructor. Preparamos un nuevo hilo para el proceso IMU
    public IMU(Context context, Handler handler)
    {
        this.mContext = context;
        mHandler = handler;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Mandamos el objeto mGPSData al hilo principal para que sea procesado
                mMessage = new Message();
                mMessage.what = ID;
                mMessage.arg1 = IMU_DATA;
                mMessage.obj = mIMUData;
                mHandler.sendMessage(mMessage);
                Log.d(TAG, "IMUData mandado al hilo principal");
            }
        };

        Timer timer = new Timer("IMUTimer");

        timer.schedule(task, 100, 100);
    }

    @Override
    public void run() {
        super.run();

        isRunning = true;

        // Inicializacion de los sensotes: acelerometros, gitorcopos y magnetometros
        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope,SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mMagneticField,SensorManager.SENSOR_DELAY_FASTEST);

        // Miembro que almacena los datos cinematicos extraidos de los sensores inerciales
        mIMUData = new IMUData();
    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try
        {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mIMUData.accelerometer = sensorEvent.values.clone();
                    getLinearAcceleration();
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    mIMUData.gyroscope = sensorEvent.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mIMUData.magnetometer = sensorEvent.values.clone();
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG,"ERROR: "+ e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void getLinearAcceleration()
    {
        mIMUData.linearAcceleration = (float)(Math.sqrt(Math.pow((double)mIMUData.accelerometer[0], 2) + Math.pow((double)mIMUData.accelerometer[1], 2) + Math.pow((double)mIMUData.accelerometer[2], 2)) - 9.81);
    }

    @Override
    public void interrupt() {
        super.interrupt();

        isRunning = false;
    }
}
