package es.bitaria.obdii_analyzer.threads;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.bitaria.obdii_analyzer.GlobalClass;
import es.bitaria.obdii_analyzer.dataStructures.GPSData;
import es.bitaria.obdii_analyzer.dataStructures.IMUData;
import es.bitaria.obdii_analyzer.SQLiteDB;
import es.bitaria.obdii_analyzer.dataStructures.OBDData;

/**
 * Creado por Pedro Amador Diaz el 04/01/2017.
 */
public class SQLite extends Thread {
    private Context mContext;
    private static final String TAG= "SQLite"; // Identificador del proceso
    public static final int ID = 2;
    public static final int SQLITE_LEFT_TIME = 1;
    public static final int SQLITE_DATA = 2;
    private Handler mHandler;
    private Message mMessage;
    private SQLiteDB mSQLiteDB;
    private SQLiteDatabase mDb;
    private static final int VERSION = 1;
    private boolean isRunning = false;
    private static int leftTime = 0;
    // Variables glovales de toda la app
    private GlobalClass globalVariable;

    public SQLite()
    {
        mHandler = null;
    }

    // Constructor. Preparamos un nuevo hilo para el proceso SQLite
    public SQLite(Context context, Handler handler)
    {
        this.mContext = context;
        mHandler = handler;
        globalVariable = GlobalClass.getIntante(context);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(globalVariable.recording){

                    leftTime = leftTime - 1;

                    // Se manda al activity el tiempo que queda de grabacion
                    mMessage = new Message();
                    mMessage.what = ID;
                    mMessage.arg1 = SQLITE_LEFT_TIME;
                    mMessage.obj = leftTime;
                    mHandler.sendMessage(mMessage);
                }
            }
        };

        Timer timer = new Timer("SQLiteTimer");

        timer.schedule(task, 1000, 1000);
    }

    @Override
    public void interrupt() {
        super.interrupt();

        isRunning = false;
    }

    @Override
    public void run() {

        isRunning = true;

    }

    // Crea una nueva base de datos
    public void newDB(String fileName){
        try{
            String name = Environment.getExternalStorageDirectory().getPath()+"/OBDII-analyzer/"+ fileName +".db3";
            mSQLiteDB = new SQLiteDB(mContext,name,null, VERSION);
            mDb = mSQLiteDB.getWritableDatabase();
            leftTime = 30; // Una nueva grabación tiene una duracion maxima de 30 segundos
        }catch (Exception e) {
            Log.e(TAG, "ERROR: ", e);
        }
    }

    // Insertado de un registro de datos procedentes del OBD
    public void insertOBD(float engineLoad, float engineTemperature, float intakeManifold, float engineSpeed, float vehicleSpeed, float ignitionAdvance, float throttlePosition)
    {
        mDb.execSQL("INSERT INTO obd (engine_load, engine_temperature, intake_manifold, engine_speed, vehicle_speed, ignition_advance, throttle_position) VALUES ("+engineLoad+", "+engineTemperature+", "+intakeManifold+", "+engineSpeed+", "+vehicleSpeed+", "+ignitionAdvance+", "+throttlePosition+")");
    }

    // Insertado de un registro de localizacion
    public void insertGPS(float latitude, float longitude, float altitude, float bearing, float speed, float accuracy)
    {
        mDb.execSQL("INSERT INTO gps (latitude, longitude, altitude, bearing, speed, accuracy) VALUES ("+latitude+", "+longitude+", "+altitude+", "+bearing+", "+speed+", "+accuracy+")");
    }

    // Insercion de un registro de mediciones cinematicas
    public void insertIMU(float accelerationX, float accelerationY, float accelerationZ, float gyroscopeX, float gyroscopeY, float gyroscopeZ, float magnetometerX, float magnetometerY, float magnetometerZ, float linear_acceleration)
    {
        mDb.execSQL("INSERT INTO imu (acceleration_x, acceleration_y, acceleration_z, gyroscope_x, gyroscope_y, gyroscope_z, magnetometer_x, magnetometer_y, magnetometer_z, linear_acceleration) VALUES ("+accelerationX+", "+accelerationY+", "+accelerationZ+", "+gyroscopeX+", "+gyroscopeY+", "+gyroscopeZ+", "+magnetometerX+", "+magnetometerY+", "+magnetometerZ+", "+linear_acceleration+")");
    }

    // Insercion de un conjunto de registros de OBD
    public void insertOBDList(List<OBDData> mOBDDataList)
    {
        Log.d(TAG,"OBD list recording");
        mDb.beginTransaction();
        for(OBDData data : mOBDDataList)
        {
            insertOBD(data.engineLoad, data.engineTemperature, data.intake_manifold, data.engineSpeed, data.vehicleSpeed, data.ignition_advance, data.throttlePosition);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    // Insercion de un conjunto de registros de localizaciones
    public void insertGPSList(List<GPSData> mGPSDataList)
    {
        Log.d(TAG,"GPS list recording");
        mDb.beginTransaction();
        for(GPSData data : mGPSDataList)
        {
            insertGPS(data.latitude, data.longitude, data.altitude, data.bearing, data.speed, data.accuracy);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    // Insercion de un conjunto de registros de mediciones inerciales
    public void insertIMUList(List<IMUData> mIMUDataList)
    {
        Log.d(TAG,"IMU list recording");
        mDb.beginTransaction();
        for(IMUData data : mIMUDataList)
        {
            insertIMU(data.accelerometer[0], data.accelerometer[1], data.accelerometer[2], data.gyroscope[0], data.gyroscope[1], data.gyroscope[2], data.magnetometer[0], data.magnetometer[1], data.magnetometer[2], data.linearAcceleration);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    // Borrado de de los registros antes de una nueva grabacion
    public void deleteTables()
    {
        Log.d(TAG, "Deleting tables");
        mDb.delete("gps", null, null);
        mDb.delete("imu", null, null);
    }

    // Carga las graficas con los valores almacenados en la base de datos
    public void playSQLiteFile()
    {
        // Borramos el contenido actual de las graficas
        globalVariable.clearChartsMemory();

       // Abrimos la base de datos seleccionada en el ListView
        mSQLiteDB = new SQLiteDB(mContext,globalVariable.playFileName,null, VERSION);
        mDb = mSQLiteDB.getWritableDatabase();

        // Leemos de la base de datos y metemos los valores en la grafica del OBD
        for(int index = 0; index < globalVariable.mPIDs.size();index++){
            if(globalVariable.mPIDs.get(index).selected == true){
                String columns[] ={globalVariable.mPIDs.get(index).sqlName};
                Cursor cursor = mDb.query("obd", columns,null, null, null, null, null );

                if(cursor.moveToFirst()){
                    do{
                        globalVariable.mOBDxAXES0.add(String.valueOf(globalVariable.mOBDxValue0));
                        globalVariable.mOBDyAXES0.add(new Entry(cursor.getFloat(0), globalVariable.mOBDxValue0));
                        ++globalVariable.mOBDxValue0; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y
                    } while(cursor.moveToNext());
                }
            }
        }

        // Leemos de la base de datos y metemos los valores en la grafica de localizacion
        for(int index = 0; index < globalVariable.mGPSs.size();index++){
            if(globalVariable.mGPSs.get(index).selected == true){
                String columns[] ={globalVariable.mGPSs.get(index).sqlName};
                Cursor cursor = mDb.query("gps", columns,null, null, null, null, null );

                if(cursor.moveToFirst()){
                    do{
                        globalVariable.mGPSxAXES0.add(String.valueOf(globalVariable.mGPSxValue0));
                        globalVariable.mGPSyAXES0.add(new Entry(cursor.getFloat(0), globalVariable.mGPSxValue0));
                        ++globalVariable.mGPSxValue0; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y
                    } while(cursor.moveToNext());
                }
            }
        }

        // Leemos de la base de datos y metemos los valores en la grafica 0 de sensores inerciales
        for(int index = 0; index < globalVariable.mIMUs.size();index++) {
            if (globalVariable.mIMUs.get(index).onChartView == 0) {
                String columns[] = {globalVariable.mIMUs.get(index).sqlName};
                Cursor cursor = mDb.query("imu", columns, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        globalVariable.mIMUxAXES0.add(String.valueOf(globalVariable.mIMUxValue0));
                        globalVariable.mIMUyAXES0.add(new Entry(cursor.getFloat(0), globalVariable.mIMUxValue0));
                        ++globalVariable.mIMUxValue0; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y
                    } while (cursor.moveToNext());
                }
            }
        }

        // Leemos de la base de datos y metemos los valores en la grafica 1 de sensores inerciales
        for(int index = 0; index < globalVariable.mIMUs.size();index++){
            if(globalVariable.mIMUs.get(index).onChartView ==1){
                String columns[] ={globalVariable.mIMUs.get(index).sqlName};
                Cursor cursor = mDb.query("imu", columns,null, null, null, null, null );

                if(cursor.moveToFirst()){
                    do{
                        globalVariable.mIMUxAXES1.add(String.valueOf(globalVariable.mIMUxValue1));
                        globalVariable.mIMUyAXES1.add(new Entry(cursor.getFloat(0), globalVariable.mIMUxValue1));
                        ++globalVariable.mIMUxValue1; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y
                    } while(cursor.moveToNext());
                }
            }
        }

        // Leemos de la base de datos y metemos los valores en la grafica 2 de sensores inerciales
        for(int index = 0; index < globalVariable.mIMUs.size();index++){
            if(globalVariable.mIMUs.get(index).onChartView ==2){
                String columns[] ={globalVariable.mIMUs.get(index).sqlName};
                Cursor cursor = mDb.query("imu", columns,null, null, null, null, null );

                if(cursor.moveToFirst()){
                    do{
                        globalVariable.mIMUxAXES2.add(String.valueOf(globalVariable.mIMUxValue2));
                        globalVariable.mIMUyAXES2.add(new Entry(cursor.getFloat(0), globalVariable.mIMUxValue2));
                        ++globalVariable.mIMUxValue2; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y
                    } while(cursor.moveToNext());
                }
            }
        }

        // Leemos de la base de datos y metemos los valores en la grafica 3 de sensores inerciales
        for(int index = 0; index < globalVariable.mIMUs.size();index++){
            if(globalVariable.mIMUs.get(index).onChartView ==3){
                String columns[] ={globalVariable.mIMUs.get(index).sqlName};
                Cursor cursor = mDb.query("imu", columns,null, null, null, null, null );

                if(cursor.moveToFirst()){
                    do{
                        globalVariable.mIMUxAXES3.add(String.valueOf(globalVariable.mIMUxValue3));
                        globalVariable.mIMUyAXES3.add(new Entry(cursor.getFloat(0), globalVariable.mIMUxValue3));
                        ++globalVariable.mIMUxValue3; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y
                    } while(cursor.moveToNext());
                }
            }
        }
    }
}

