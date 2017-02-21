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

package es.bitaria.obdii_analyzer.Communications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import es.bitaria.obdii_analyzer.R;

/**
 * Creado por Pedro Amador Diaz el 19/02/2017.
 */

public class WifiService extends AsyncTask<Void, Void, Void>{
    private Context mContext;
    private Socket socket;

    // Debugging
    private static final String TAG = "WifiService";

    // Member fields
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DEVICE_NAME = 4;
    public static final int STATE_PROTOCOL =5;

    /**
     * Constructor. Prepara una nueva conexion wifi
     *
     * @param context UI Activity Context
     * @param handler un Handler al que mandar mensajes hacia la UI Activity
     */
    public WifiService(Context context, Handler handler) {
        mState = STATE_NONE;
        mHandler = handler;
        this.mContext = context;
    }

    // Creacion del socket de comunicacion wifi con los parametros almacenados en la memoria premamente SharedPreferences
    @Override
    protected Void doInBackground(Void... params) {
        try {
            // Recuperamos los datos de conexion con el dispositivo scanner de la memoria permanente
            SharedPreferences preferences = mContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
            String ip = preferences.getString("wifi_address", "192.168.0.10");
            String port = preferences.getString("wifi_port", "35000");

            socket = new Socket(ip, Integer.parseInt(port));

            setState(STATE_CONNECTING);
        } catch(IOException e){
            Log.e(TAG, "Exception open socket", e);
        }
        return null;
    }

    /**
     * Establece el estado actual de la conexion wifi
     * @param state Entero que define el actual estado de la conexion
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Returna el actual estado de la conexion
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Comienza el hilo encargado de la comunicacion wifi pasandole como parametro
     * el socket creado previmente
     */
    public synchronized void onPostExecute(Void result) {
        if(socket == null) // Si no se ha podido crear el socked salimos del proceso de conexion
            return;

        Log.d(TAG, "Wifi connected");

        // Inicie el hilo para administrar la conexión y realizar transmisiones
        mConnectedThread = new WifiService.ConnectedThread(socket);

        // Envíe el nombre del dispositivo conectado a la actividad de la interfaz de usuario
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, "Wifi");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
        mConnectedThread.start();
    }

    /**
     * Para todos los hilos
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Escribir al hilo de conexion de una manera no sincronizada
     * @param out os bytes a escribir
     */
    public void write(byte[] out) {
        // Crear objeto temporal
        WifiService.ConnectedThread r;
        // Sincronizar una copia del hilo de conexion
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Realizar la escritura sin sincronizar
        r.write(out);
    }

    /**
     * Indica que la conexión se ha perdido y notifica la actividad de la UI.
     */
    private void connectionLost() {
        // Manda el mensaje de desconexion al UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, mContext.getString(R.string.device_connection_was_lost));
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Cambiamos el estado a no conectado
        setState(STATE_NONE);
    }

    /**
     * Este hilo se ejecuta durante una conexión con un dispositivo remoto.
     * Maneja todas las transmisiones entrantes y salientes.
     */
    private class ConnectedThread extends Thread {
        private final Socket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(Socket socket) {
            Log.d(TAG, "create wifi ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Creamos los streams de entrada y salida de la conexion wifi
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Sigue escuchando el InputStream mientras está conectado
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (mState == STATE_CONNECTED) {
                try {
                    // Leemos del InputStream
                    bytes = mmInStream.read(buffer);

                    // Mandamos los bytes obtenidos del escaner wifi al UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    buffer = new byte[1024]; // Una vez mandado a la cola de mensajes del hilo para ser procesado, borramos el buffer
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Escribe en en el OutStream abierto.
         * @param buffer Los bytes a escribir
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Compartir el mensaje enviado de nuevo a la actividad de la interfaz de usuario
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
