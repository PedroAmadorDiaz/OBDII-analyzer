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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.os.PowerManager;
import java.util.ArrayList;
import es.bitaria.obdii_analyzer.Communications.BluetoothChatService;
import es.bitaria.obdii_analyzer.dataStructures.OBDData;
import es.bitaria.obdii_analyzer.threads.GPS;
import es.bitaria.obdii_analyzer.threads.IMU;
import es.bitaria.obdii_analyzer.threads.OBD;
import es.bitaria.obdii_analyzer.threads.SQLite;
import es.bitaria.obdii_analyzer.dataStructures.GPSData;
import es.bitaria.obdii_analyzer.dataStructures.IMUData;

/**
 * Creado por Pedro Amador Diaz el 03/01/2017.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName(); // Identificador del proceso
    private PowerManager.WakeLock wakelock;

    private Handler mMainHandler;
    private GlobalClass globalVariables;

    private OBDChart obdChart;
    private KineticChart kineticChart;
    private LocationChart locationChart;
    private Store store;
    private About about;

    String[] name ={"Calculated engine load","Engine coolant temperature","Intake manifold Absolute","RPM","Speed","Ignition timing advance","Throttle position"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CPU siempre al 100% de rendimiento
        final PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        this.wakelock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OBDII-Analyzer");
        wakelock.acquire();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        globalVariables = GlobalClass.getIntante(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Llamamos al fragment obdChart, pues se trata de la pagina inicial
        obdChart = new OBDChart();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main,obdChart, obdChart.getTag()).commit();

        // Procesado de los mensajes mandados por los hilos en segundo plano al hilo principal
        class MainHandler extends Handler {

            @Override
            public void handleMessage(Message message) {
                try
                {
                    switch(message.what)
                    {
                        case GPS.ID:
                            //Log.d(TAG,"GPS class send message to GUI");
                            switch (message.arg1)
                            {
                                case GPS.ACCURACY_STATE:
                                    TextView locationAccuracy = (TextView) findViewById(R.id.location_accuracy);
                                    locationAccuracy.setText(getString(R.string.location_accuracy) + "\n" + getString((int)(message.obj)));
                                    break;
                                case GPS.GPS_DATA:

                                    // Pasamos los datos a la estructura de datos del globalVariables para que puedan ser mandados a las graficas
                                    globalVariables.mGPSs.get(0).value = ((GPSData)message.obj).latitude;
                                    globalVariables.mGPSs.get(1).value = ((GPSData)message.obj).longitude;
                                    globalVariables.mGPSs.get(2).value = ((GPSData)message.obj).altitude;
                                    globalVariables.mGPSs.get(3).value = ((GPSData)message.obj).bearing;
                                    globalVariables.mGPSs.get(4).value = ((GPSData)message.obj).speed;
                                    globalVariables.mGPSs.get(5).value = ((GPSData)message.obj).accuracy;

                                    // Creamos un nuevo objeto para su guardado en RAM
                                    GPSData gpsData = new GPSData(((GPSData)message.obj).latitude
                                            , ((GPSData)message.obj).longitude
                                            , ((GPSData)message.obj).altitude
                                            , ((GPSData)message.obj).bearing
                                            , ((GPSData)message.obj).speed
                                            , ((GPSData)message.obj).accuracy);

                                    // Los datos pasan a la RAM solo si la grabacion esta activada
                                    if(globalVariables.recording){
                                        globalVariables.mGPSDataList.add(gpsData);
                                    }

                                    // Si se ha llenado la memoria RAM, pasar los datos a la BD
                                    if(globalVariables.mGPSDataList.size()>=5){
                                            globalVariables.sqLite.insertGPSList(globalVariables.mGPSDataList);
                                            globalVariables.mGPSDataList.clear();
                                    }

                                    // Mandamos los datos seleccionados a la grafica y a la lista
                                    globalVariables.mGPSsAdapter.clear();

                                    for(int index = 0; index < globalVariables.mGPSs.size();index++){
                                        globalVariables.mGPSsAdapter.add(globalVariables.mGPSs.get(index));
                                        if(globalVariables.mGPSs.get(index).selected && globalVariables.replay == false)
                                            locationChart.lineChart0(globalVariables.mGPSs.get(index).name, globalVariables.mGPSs.get(index).value);
                                    }
                                    break;
                            }
                            break;
                        case SQLite.ID:
                            //Log.d(TAG,"SQLite class send message to GUI");
                            switch (message.arg1){
                                case SQLite.SQLITE_LEFT_TIME: // Ha llegado el tiempo que queda de grabacion
                                    store.setLeftTime((int)message.obj);
                                    if((int)message.obj <= 0)
                                        store.stopRecord(); // Ya no queda más tiempo, se manda la orden de parar la grabacion
                                    break;
                            }
                        case IMU.ID:
                            //Log.d(TAG,"IMU class send message to GUI");
                            switch (message.arg1){
                                case IMU.IMU_DATA:

                                    // Pasamos los datos a la estructura de datos del globalVariables para que puedan ser mandados a las graficas
                                    globalVariables.mIMUs.get(0).value = ((IMUData)message.obj).accelerometer[0];
                                    globalVariables.mIMUs.get(1).value = ((IMUData)message.obj).accelerometer[1];
                                    globalVariables.mIMUs.get(2).value = ((IMUData)message.obj).accelerometer[2];

                                    globalVariables.mIMUs.get(3).value = ((IMUData)message.obj).gyroscope[0];
                                    globalVariables.mIMUs.get(4).value = ((IMUData)message.obj).gyroscope[1];
                                    globalVariables.mIMUs.get(5).value = ((IMUData)message.obj).gyroscope[2];

                                    globalVariables.mIMUs.get(6).value = ((IMUData)message.obj).magnetometer[0];
                                    globalVariables.mIMUs.get(7).value = ((IMUData)message.obj).magnetometer[1];
                                    globalVariables.mIMUs.get(8).value = ((IMUData)message.obj).magnetometer[2];

                                    globalVariables.mIMUs.get(9).value = ((IMUData)message.obj).linearAcceleration;

                                    // Creamos un nuevo objeto para su guardado en RAM
                                    IMUData imuData = new IMUData(((IMUData)message.obj).accelerometer, ((IMUData)message.obj).gyroscope, ((IMUData)message.obj).magnetometer, ((IMUData)message.obj).linearAcceleration);

                                    // Los datos pasan a la RAM solo si la grabacion esta activada
                                    if(globalVariables.recording)
                                        globalVariables.mIMUDataList.add(imuData);

                                    // Si se ha llenado la memoria RAM, pasar los datos a la BD
                                    if(globalVariables.mIMUDataList.size()>=50){
                                        globalVariables.sqLite.insertIMUList(globalVariables.mIMUDataList);
                                        globalVariables.mIMUDataList.clear();
                                    }
                                    // Actuaclizamos las graficas solo si no estamos en el modo replay
                                    if(globalVariables.replay == false) {
                                        for (int index = 0; index < globalVariables.mIMUs.size(); index++) {
                                            if (globalVariables.mIMUs.get(index).onChartView > -1)
                                                switch (globalVariables.mIMUs.get(index).onChartView) {
                                                    case 0:
                                                        kineticChart.lineChart0(globalVariables.mIMUs.get(index).name, globalVariables.mIMUs.get(index).value);
                                                        break;
                                                    case 1:
                                                        kineticChart.lineChart1(globalVariables.mIMUs.get(index).name, globalVariables.mIMUs.get(index).value);
                                                        break;
                                                    case 2:
                                                        kineticChart.lineChart2(globalVariables.mIMUs.get(index).name, globalVariables.mIMUs.get(index).value);
                                                        break;
                                                    case 3:
                                                        kineticChart.lineChart3(globalVariables.mIMUs.get(index).name, globalVariables.mIMUs.get(index).value);
                                                        break;
                                                }
                                        }
                                    }
                                    break;
                            }
                            break;
                        case OBD.ID:
                            switch (message.arg1)
                            {
                                // Cambios de estado de la conexion con el escaner OBD, mandados por el hilo OBD
                                case OBD.OBD_STATE:

                                    TextView connectionStatus = (TextView) findViewById(R.id.connection_status);

                                    if (message.arg2 == BluetoothChatService.STATE_CONNECTING){
                                        connectionStatus.setText(getString(R.string.connecting));
                                    }
                                    if (message.arg2 == BluetoothChatService.STATE_CONNECTED){
                                        connectionStatus.setText(getString(R.string.connected));
                                    }
                                    if (message.arg2 == BluetoothChatService.STATE_NONE){
                                        connectionStatus.setText(getString(R.string.not_connected));
                                    }
                                    if (message.arg2 == BluetoothChatService.STATE_DEVICE_NAME){
                                        connectionStatus.setText(message.obj.toString());
                                    }
                                    if (message.arg2 == BluetoothChatService.STATE_PROTOCOL){
                                        connectionStatus.setText(connectionStatus.getText() + "\n" + message.obj.toString());
                                    }
                                    break;
                                case OBD.OBD_DATA:
                                    ArrayList<PID> pids = (ArrayList<PID>)message.obj;
                                    globalVariables.mPIDsAdapter.clear();
                                    for(int index = 0; index < globalVariables.mPIDs.size();index++){
                                        globalVariables.mPIDsAdapter.add(pids.get(index));
                                        if(globalVariables.mPIDs.get(index).selected && globalVariables.replay == false)
                                            obdChart.lineChart(globalVariables.mPIDs.get(index).name, pids.get(index).value);
                                    }

                                    OBDData obdData = new OBDData();
                                    obdData.engineLoad = pids.get(0).value;
                                    obdData.engineTemperature = pids.get(1).value;
                                    obdData.intake_manifold = pids.get(2).value;
                                    obdData.engineSpeed = pids.get(3).value;
                                    obdData.vehicleSpeed = pids.get(4).value;
                                    obdData.ignition_advance = pids.get(5).value;
                                    obdData.throttlePosition = pids.get(6).value;

                                    // Los datos pasan a la RAM solo si la grabacion esta activada
                                    if(globalVariables.recording)
                                        globalVariables.mOBDDataList.add(obdData);

                                    // Si se ha llenado la memoria RAM, pasar los datos a la BD
                                    if(globalVariables.mOBDDataList.size()>=50){
                                        globalVariables.sqLite.insertOBDList(globalVariables.mOBDDataList);
                                        globalVariables.mOBDDataList.clear();
                                    }
                                    break;
                            }
                            break;
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG,"ERROR: "+ e.getMessage());
                }

            }
        }

        mMainHandler = new MainHandler();

        // Puesta en marcha de todos los hilos que forman la aplicacion
        startThreads();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Carga el menu de consfiguraciones
        if (id == R.id.action_settings) {
            Settings settings = new Settings();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content_main,settings, settings.getTag());
            transaction.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Tratamiento de los clicks en los items del navigation view
        int id = item.getItemId();
        FragmentManager manager;

        switch(id){
            case R.id.nav_obd2:
                obdChart = new OBDChart();
                manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main,obdChart, obdChart.getTag()).commit();
                break;
            case R.id.nav_kinetic:
                kineticChart = new KineticChart();
                manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main,kineticChart, kineticChart.getTag()).commit();
                break;
            case R.id.nav_location:
                locationChart = new LocationChart();
                manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main,locationChart, locationChart.getTag()).commit();
                break;
            case R.id.nav_store:
                store = new Store();
                manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main,store, store.getTag()).commit();
                break;
            case R.id.nav_about:
                about = new About();
                manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main,about, about.getTag()).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Intantacion y ejecución de los hilos que forman la aplicacion
    private void startThreads()
    {
        globalVariables.gps = new GPS(this, mMainHandler);
        globalVariables.gps.setDaemon(true);
        globalVariables.gps.start();

        globalVariables.imu = new IMU(this, mMainHandler);
        globalVariables.imu.setDaemon(true);
        globalVariables.imu.start();

        globalVariables.obd = new OBD(this, mMainHandler);
        globalVariables.obd.setDaemon(true);
        globalVariables.obd.start();

        globalVariables.sqLite = new SQLite(this, mMainHandler);
        globalVariables.sqLite.setDaemon(true);
        globalVariables.sqLite.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        this.wakelock.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        wakelock.acquire();
    }

    // Al cerrar la app se matan todos los hilos que dependen de ella
    @Override
    protected void onDestroy() {

        this.wakelock.release();

        // Cerramos todos los hilos
        globalVariables.gps.interrupt();
        globalVariables.imu.interrupt();
        globalVariables.obd.interrupt();
        globalVariables.sqLite.interrupt();

        super.onDestroy();
    }

    // Cierra la app
    private void finishAll(){

        finish();
    }
}
