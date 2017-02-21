package es.bitaria.obdii_analyzer.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import es.bitaria.obdii_analyzer.Communications.WifiService;
import es.bitaria.obdii_analyzer.GlobalClass;
import es.bitaria.obdii_analyzer.R;
import es.bitaria.obdii_analyzer.Communications.BluetoothChatService;
import es.bitaria.obdii_analyzer.Communications.Constants;

import static es.bitaria.obdii_analyzer.Communications.Constants.*;

/**
 * Creado por Pedro Amador Diaz on 26/01/2017.
 */

public class OBD extends Thread{
    private Context mContext;
    private static final String TAG= "OBD"; // Identificador del proceso
    public static final int ID = 4;
    public static final int OBD_STATE = 1;
    public static final int OBD_DATA = 2;
    public static int errorsNumber = 0;
    public static String lastOBDError = "no error";
    public static String supportedPIDS = "";
    public static String deviceProtocol = "";
    private static String readBuffer = "";
    private static int timeOut = 10; // 10 segundos de timeOut
    private final Handler activityHandler;
    private Message mMessage;
    private boolean initialized = false;
    private boolean isRunning = false;
    private GlobalClass globalVariable;
    private static String connectionType ="bluetooth";
    private int indexCommand = 0; // Indice del comando que te esta mandado al OBD de una lista ordenada de comandos
    String  ENGINE_LOAD             = "0104",  // (A/255)*100
            ENGINE_COOLANT_TEMP     = "0105",  // A-40
            INTAKE_PRESSURE          = "010B",  // A
            ENGINE_RPM              = "010C",  // ((A*256)+B)/4
            VEHICLE_SPEED           = "010D",  // A
            CYLINDER_TIMING_ADVANCE = "010E",  // (A-128)/2
            THROTTLE_POSITION       = "0111";  // (A/255)*100


    String [] commands = new String[]{ENGINE_LOAD,ENGINE_COOLANT_TEMP,INTAKE_PRESSURE,ENGINE_RPM,VEHICLE_SPEED,CYLINDER_TIMING_ADVANCE,THROTTLE_POSITION};
    String [] initializeCommands = new String[]{"ATDP","ATS0","ATL0","ATE0"};

    // Adaptador Bluetooth local
    private BluetoothAdapter mBluetoothAdapter = null;
    // Objeto que implementa los servicios de comunicacion con el adaptador Bluetooch o wifi
    private BluetoothChatService mChatService = null;
    private WifiService mWifiService = null;

    // Constructor. Preparamos un nuevo hilo para el proceso de PIDs
    public OBD(Context context, Handler handler)
    {
        this.mContext = context;
        activityHandler = handler;
        globalVariable = GlobalClass.getIntante(context);
    }

    // El manejador que recibe informacion desde el BluetoothChatService
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    mMessage = new Message();
                    mMessage.what = ID;
                    mMessage.arg1 = OBD_STATE;
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            mMessage.arg2 = Constants.STATE_CONNECTED;
                            OBD.this.sendMessage("ATZ");
                            break;
                        case  Constants.STATE_CONNECTING:
                            mMessage.arg2 =  Constants.STATE_CONNECTING;
                            break;
                        case  Constants.STATE_LISTEN:
                            mMessage.arg2 =  Constants.STATE_LISTEN;
                            break;
                        case  Constants.STATE_NONE:
                            mMessage.arg2 =  Constants.STATE_NONE;
                            break;
                    }
                    activityHandler.sendMessage(mMessage);
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // Construye un string procedente del buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    readBuffer = readBuffer + new String(readBuf).substring(0, msg.arg1); // Concatenamos lo recibido anteriormente con lo nuevo.
                    // Si se ha detectado un final de trama procesar lo que ha llegado asta entonces
                    if(initialized==false) {
                        if (readBuffer.contains(">")) {
                            compileMessage(readBuffer.substring(0, readBuffer.indexOf(">")));
                            readBuffer = readBuffer.substring(readBuffer.indexOf(">")+1);
                        }
                    }
                    if(initialized==true){
                        if(readBuffer.contains(">")) {
                            sendToOBD(); // Mandamos la siguiente peticion de PID al escaner OBD, pues ha llegado la respuesta de la peticion anterior
                            compileMessage(readBuffer.substring(0,readBuffer.indexOf(">")));
                            readBuffer = readBuffer.substring(readBuffer.indexOf(">")+1);
                        }
                        if(readBuffer.contains("\r")) {
                            compileMessage(readBuffer.substring(0,readBuffer.indexOf("\r")));
                            readBuffer = readBuffer.substring(readBuffer.indexOf("\r")+1);
                        }
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    Toast.makeText(mContext, mContext.getString(R.string.connected_to) + " "
                            + msg.getData().getString(DEVICE_NAME), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(mContext, msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * Mandar mensaje al dispositivo bluetooth
     * @param message  Un string para mandar
     */
    private void sendMessage(String message) {
        if(connectionType.equals("bluetooth")){
            // Comprueba que realmente estamos conectados antes de intentar nada
            if (mChatService.getState() !=  Constants.STATE_CONNECTED) {
                Toast.makeText(mContext, mContext.getString (R.string.not_connected), Toast.LENGTH_LONG).show();
                return;
            }

            // Compruebe que realmente hay algo que enviar
            if (message.length() > 0) {
                message=message+"\r";
                // Obtener los bytes de mensaje y decirle al BluetoothChatService que escriba en el dispositivo bluetooth
                byte[] send = message.getBytes();
                mChatService.write(send);
            }
        }
        else if(connectionType.equals("wifi")){
            // Comprueba que realmente estamos conectados antes de intentar nada
            if (mWifiService.getState() !=  Constants.STATE_CONNECTED) {
                Toast.makeText(mContext, mContext.getString (R.string.not_connected), Toast.LENGTH_LONG).show();
                return;
            }

            // Compruebe que realmente hay algo que enviar
            if (message.length() > 0) {
                message=message+"\r";
                // Obtener los bytes de mensaje y decirle al BluetoothChatService que escriba en el dispositivo wifi
                byte[] send = message.getBytes();
                mWifiService.write(send);
            }
        }
    }

    // Tratamiento de cada uno de los mesajes procedentes del interface OBDII
    private void compileMessage(String msg) {
        //Toast.makeText(mContext,  msg, Toast.LENGTH_LONG).show();
        msg = msg.replace("null", "");
        msg = msg.replaceAll("\n", "");
        msg = msg.replaceAll("\r", "");

        if (!initialized) // Inicializacion del dispositivo lector conectado al OBDII
        {
            if (msg.contains("ELM327"))
            {
                msg = msg.replaceAll("ATZ", "");
                // Mandamos al hilo del UI el nombre del dispositivo conectado al OBDII
                //Toast.makeText(mContext,  msg, Toast.LENGTH_LONG).show();
                mMessage = new Message();
                mMessage.what = ID;
                mMessage.arg1 = OBD_STATE;
                mMessage.arg2 =  Constants.STATE_DEVICE_NAME;
                mMessage.obj = msg;
                activityHandler.sendMessage(mMessage);
            }
            if (msg.contains("ATDP"))
            {
                // Mandamos al hilo del UI el protocolo empleado en la comunicacion OBDII
                deviceProtocol = msg.replace("ATDP", "");
                //Toast.makeText(mContext,  deviceProtocol, Toast.LENGTH_LONG).show();
                mMessage = new Message();
                mMessage.what = ID;
                mMessage.arg1 = OBD_STATE;
                mMessage.arg2 =  Constants.STATE_PROTOCOL;
                mMessage.obj = msg.replace("ATDP", "");
                activityHandler.sendMessage(mMessage);
            }
            if (msg.contains("ATE0")) // Ha llegado la respuesta al ultimo comando AT de la secuencia de inicializacion
            {
                initialized = true;
                sendToOBD(); // Se manda la primera peticion de PID
            }
            if(indexCommand < initializeCommands.length)
            {
                String send = initializeCommands[indexCommand];
                OBD.this.sendMessage(send);
                indexCommand++; // Ejecutar el siguiente comando de inicializacion
            }
        }
        else if(msg.length()>=4)
        {
            // Pasamos del procesado de comandos AT al de PIDs
            int obdval = 0;
            String tmpmsg = "";

            if (msg.substring(0, 4).equals("4100")) // Han llegado los flags que indican los PIDs disponibles
            {
                supportedPIDS = msg;
            }
            else if (msg.substring(0, 2).equals("43")) // Ha llegaddo un mensaje de error
            {
                try {
                    if(deviceProtocol.contains("CAN"))
                    {
                        errorsNumber = Integer.parseInt(msg.substring(2, 4),16);
                        lastOBDError=msg.substring(4, msg.length());
                    }else
                    {
                        lastOBDError=msg.substring(2, msg.length());
                        errorsNumber=lastOBDError.length()/4;
                    }
                } catch (NumberFormatException nFE) {
                    lastOBDError = "Try Error:  " + msg;
                }
            }
            else{
                try{
                    if (msg.length() > 4) // Ha llegado un valor de PID solicitado
                    {
                        /////mode 1//////////
                        if (msg.substring(0, 2).equals("41")) {
                            try {
                                tmpmsg = msg.substring(0, 4);
                            } catch (NumberFormatException nFE) {
                            }
                            if (tmpmsg.contains("41"+globalVariable.mPIDs.get(0).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 6), 16);
                                globalVariable.mPIDs.get(0).value = ((float)obdval*100f)/255f;
                            } else if (tmpmsg.equals("41"+globalVariable.mPIDs.get(1).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 6), 16);
                                globalVariable.mPIDs.get(1).value = (float)obdval-40f;
                            } else if (tmpmsg.equals("41"+globalVariable.mPIDs.get(2).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 6), 16);
                                globalVariable.mPIDs.get(2).value = (float)obdval;
                            } else if (tmpmsg.equals("41"+globalVariable.mPIDs.get(3).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 8), 16);
                                globalVariable.mPIDs.get(3).value = ((float)obdval/4f);
                            } else if (tmpmsg.equals("41"+globalVariable.mPIDs.get(4).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 6), 16);
                                globalVariable.mPIDs.get(4).value = (float)obdval;
                            } else if (tmpmsg.equals("41"+globalVariable.mPIDs.get(5).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 6), 16);
                                globalVariable.mPIDs.get(5).value = ((float)obdval-128f)/2f;
                            } else if (tmpmsg.contains("41"+globalVariable.mPIDs.get(6).id)) {
                                obdval = Integer.parseInt(msg.substring(4, 6), 16);
                                globalVariable.mPIDs.get(6).value = ((float)obdval*100f)/255f;
                            }

                            // Se manda al activity el PID recivido y ya procesado
                            mMessage = new Message();
                            mMessage.what = ID;
                            mMessage.arg1 = OBD_DATA;
                            mMessage.obj = globalVariable.mPIDs;
                            activityHandler.sendMessage(mMessage);
                        }
                    }
                }catch(Exception e){}
            }
        }
    }

    private void setupBluetoothChat()
    {
        // Inicializa el BluetoothChatService para realizar una conexion bluetooth
        initialized = false;
        indexCommand = 0;
        mChatService = new BluetoothChatService(mContext, handler);
    }

    @Override
    public void interrupt() {
        super.interrupt();

        isRunning = false;
    }

    @Override
    public void run() {
        super.run();

        isRunning = true;

            TimerTask taskOBD = new TimerTask() {
                @Override
                public void run() {
                    if(initialized) {
                        timeOut--;
                        if (timeOut == 0)
                            sendToOBD(); // Mandamos una nueva petición de PID si hemos llegado a los 10 segundo de timeOut
                    }
                }
            };

            Timer timerOBD = new Timer("OBDTimer");
            timerOBD.scheduleAtFixedRate(taskOBD, 1000, 1000);
    }

    private void sendToOBD(){

        if (indexCommand >= commands.length) {
            indexCommand = 0;
        }

        ////commands/////////////
        String send = commands[indexCommand] + "1"; // Agregamos un uno pues solo queremos que la ECU espere a la llegada de un dato de una sola fuente.
        OBD.this.sendMessage(send);
        timeOut = 10; // Reseteamos el contador timeOut
        indexCommand++;
    }

    public void bluetoothConnect(){
        connectionType = "bluetooth";

        // Recuperamos los datos de conexion con el dispositivo scanner de la memoria permanente
        SharedPreferences preferences = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String deviceAddress = preferences.getString("bluetooth_address", "00:00:00:00:00:00");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, mContext.getString (R.string.not_bluetooth_support), Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(mContext, mContext.getString (R.string.bluetooth_defuse), Toast.LENGTH_LONG).show();
            return;
        } else {
            // Creamos el objeto BluetoothDevice
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);

            // Inicializamos el estado de la conexion con el dispositivo OBD
            initialized=false;

            if (mChatService == null || mChatService.getState() ==  Constants.STATE_NONE)
            {
                setupBluetoothChat();
            }
            // Si esta conectado desconetamos y salimos
            if (mChatService.getState() ==  Constants.STATE_CONNECTED){
                mChatService.stop();
                return;
            }
            if (mChatService.getState() ==  Constants.STATE_NONE) {
                mChatService.start();
            }
            mChatService.connect(device, false);
        }
    }

    public void wifiConnect(){

        // Inicializa el wifiService para realizar una conexion wifi
        connectionType = "wifi";

        // Estado inicial
        initialized = false;
        indexCommand = 0;

        if(mWifiService == null){
            mWifiService = new WifiService(mContext, handler);
            mWifiService.execute();
            return;
        }

        // Si estamos conectados al dispositivo escaner OBD, nos desconectamos
        if (mWifiService.getState() ==  Constants.STATE_CONNECTED){
            mWifiService.stop();
            mWifiService.cancel(true);
            return;
        }

        mWifiService = new WifiService(mContext, handler);
        mWifiService.execute();


    }
}
