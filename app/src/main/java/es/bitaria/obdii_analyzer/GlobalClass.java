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

package es.bitaria.obdii_analyzer;

import android.content.Context;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import es.bitaria.obdii_analyzer.dataStructures.GPSData;
import es.bitaria.obdii_analyzer.dataStructures.IMUData;
import es.bitaria.obdii_analyzer.dataStructures.OBDData;
import es.bitaria.obdii_analyzer.threads.GPS;
import es.bitaria.obdii_analyzer.threads.SQLite;
import es.bitaria.obdii_analyzer.threads.OBD;

/**
 * Creado por Pedro Amador Diaz el 17/01/2017.
 */

public class GlobalClass {
    private static GlobalClass instance;
    private Context context;
    public boolean recording = false; // Indica si estamos guardando datos en memoria permanente
    public boolean replay = false; // Indica que las graficas estan reproduciendo una grabacion
    public static ArrayList<PID> mPIDs= new ArrayList<> ();
    public static ArrayList<IMU> mIMUs= new ArrayList<> ();
    public static ArrayList<Location> mGPSs= new ArrayList<> ();
    public static ArrayList<GPSData> mGPSDataList = new ArrayList<>();
    public static ArrayList<IMUData> mIMUDataList = new ArrayList<>();
    public static ArrayList<OBDData> mOBDDataList = new ArrayList<>();
    public static PIDsAdapter mPIDsAdapter; // Publico para que pueda ser modificado por el MainActivity
    public static GPSsAdapter mGPSsAdapter; // Publico para que pueda ser modificado por el MainActivity
    public String playFileName =""; // Nombre del archivo que se esta reproduciendo

    // Variables empleadas en la generacion del graficos
    public static int mOBDxValue0 = 0;
    public static  ArrayList<String> mOBDxAXES0 = new ArrayList<>();
    public static ArrayList<Entry> mOBDyAXES0 = new ArrayList<>();

    public static int mGPSxValue0 = 0;
    public static ArrayList<String> mGPSxAXES0 = new ArrayList<>();
    public static ArrayList<Entry> mGPSyAXES0 = new ArrayList<>();

    public static int mIMUxValue0 = 0;
    public static ArrayList<String> mIMUxAXES0 = new ArrayList<>();
    public static ArrayList<Entry> mIMUyAXES0 = new ArrayList<>();

    public static int mIMUxValue1 = 0;
    public static ArrayList<String> mIMUxAXES1 = new ArrayList<>();
    public static ArrayList<Entry> mIMUyAXES1 = new ArrayList<>();

    public static int mIMUxValue2 = 0;
    public static ArrayList<String> mIMUxAXES2 = new ArrayList<>();
    public static ArrayList<Entry> mIMUyAXES2 = new ArrayList<>();

    public static int mIMUxValue3 = 0;
    public static ArrayList<String> mIMUxAXES3 = new ArrayList<>();
    public static ArrayList<Entry> mIMUyAXES3 = new ArrayList<>();

    // Los cuatro hilos que va ha componer la app que los cuales van a ser abiertos y cerrados por el activity
    public GPS gps;
    public es.bitaria.obdii_analyzer.threads.IMU imu;
    public OBD obd;
    public SQLite sqLite;

    public GlobalClass(Context context){
        this.context = context;
    }

    public static synchronized GlobalClass getIntante(Context context){

        if (instance==null){
            instance = new GlobalClass(context);

            // Definimos los PIDs con los que va a trabajar la aplicacion y si van ha ser mostrados inicialmente
            mPIDs.add(new PID("04","Calculated engine load", "engine_load", (float)0.0,"%", false));      // (A/255)*100
            mPIDs.add(new PID("05","Engine coolant temperature", "engine_temperature",(float)0.0,"ºC", false)); // A-40
            mPIDs.add(new PID("0B","Intake manifold Absolute", "intake_manifold", (float)0.0,"kPa", false));  // A
            mPIDs.add(new PID("0C","RPM", "engine_speed", (float)0.0,"r/min", true));                      // ((A*256)+B)/4
            mPIDs.add(new PID("0D","Speed", "vehicle_speed", (float)0.0,"km/h", false));                    // A
            mPIDs.add(new PID("0E","Ignition timing advance","ignition_advance", (float)0.0,"º", false));     // (A-128)/2
            mPIDs.add(new PID("11","Throttle position", "throttle_position", (float)0.0,"%", false));           // (A/255)*100

            mIMUs.add(new IMU("X Acceleration", "acceleration_x", (float)0.0, 0)); // -1 ninguna grafica asignada
            mIMUs.add(new IMU("Y Acceleration","acceleration_y", (float)0.0, 1));
            mIMUs.add(new IMU("Z Acceleration", "acceleration_z", (float)0.0, 2));
            mIMUs.add(new IMU("X Gyroscope", "gyroscope_x", (float)0.0, -1));
            mIMUs.add(new IMU("Y Gyroscope", "gyroscope_y", (float)0.0, -1));
            mIMUs.add(new IMU("Z Gyroscope", "gyroscope_z", (float)0.0, -1));
            mIMUs.add(new IMU("X Magnetometer", "magnetometer_x", (float)0.0, -1));
            mIMUs.add(new IMU("Y Magnetometer", "magnetometer_y", (float)0.0, -1));
            mIMUs.add(new IMU("Z Magnetometer", "magnetometer_x", (float)0.0, -1));
            mIMUs.add(new IMU("Lineal acceleration", "linear_acceleration", (float)0.0, 3));

            mGPSs.add(new Location("Latitude", "latitude", (float) 0.0, "º", false));
            mGPSs.add(new Location("Longitude", "longitude", (float) 0.0, "º", false));
            mGPSs.add(new Location("Altitude", "altitude", (float) 0.0, "m", false));
            mGPSs.add(new Location("Bearing", " bearing", (float) 0.0, "º", false));
            mGPSs.add(new Location("Speed", "speed", (float) 0.0, "km/h", false));
            mGPSs.add(new Location("Accuracy", "accuracy", (float) 0.0, "m", true));
        }
        return instance;
    }

    // Borramos los datos anteriores de las grafica
    public static void clearChartsMemory(){

        mOBDxAXES0.clear();
        mOBDyAXES0.clear();
        mOBDxValue0 = 0;

        mGPSxAXES0.clear();
        mGPSyAXES0.clear();
        mGPSxValue0 = 0;

        mIMUxAXES0.clear();
        mIMUyAXES0.clear();
        mIMUxValue0 = 0;

        mIMUxAXES1.clear();
        mIMUyAXES1.clear();
        mIMUxValue1 = 0;

        mIMUxAXES2.clear();
        mIMUyAXES2.clear();
        mIMUxValue2 = 0;

        mIMUxAXES3.clear();
        mIMUyAXES3.clear();
        mIMUxValue3 = 0;
    }
}
