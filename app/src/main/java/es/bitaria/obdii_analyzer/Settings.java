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


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;


/**
 * Creado por Pedro Amador Diaz el 17/01/2017.
 */
public class Settings extends Fragment implements AdapterView.OnItemClickListener {

    static final int REQUEST_ENABLE_BT = 1;
    View rootView;
    private ListView settingsListView;
    private ListView pairedDevicesListView;
    private String[] settings;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    // Constructor por defecto
    public Settings() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate el layout para este fragment
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        settings  = new String[] {getString (R.string.bluetooth_obd_connection), getString (R.string.wifi_obd_connection)};
        settingsListView = (ListView) rootView.findViewById(R.id.settingsListView);
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, settings);
        settingsListView.setAdapter(adapter);
        settingsListView.setOnItemClickListener(this);

        pairedDevicesListView = (ListView) rootView.findViewById(R.id.pairedDevicesListView);
        pairedDevicesListView.setOnItemClickListener(this);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) this.getActivity().getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Settings");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END shared_tracker]

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK) Toast.makeText(getActivity(),getString (R.string.bluetooth_actived), Toast.LENGTH_LONG).show();
                else Toast.makeText(getActivity(), getString (R.string.bluetooth_defuse), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView == rootView.findViewById(R.id.settingsListView)){
            switch (i)
            {
                // Conexion con el dispositivo OBD mediante bluetooth
                case 0:

                    // Build and send an Event.
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("device")
                            .setAction("bluetooth")
                            .build());

                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        Toast.makeText(getActivity(), getString (R.string.not_bluetooth_support), Toast.LENGTH_LONG).show();
                        break;
                    }
                    else if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }

                    // Si el dispositivo tiene un interface bluetooth y este esta activado
                    if(mBluetoothAdapter.isEnabled())
                    {
                        // Si hay dispositivos emparejados
                        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        String[] array = {};
                        ArrayList<String> pairedDevicesList = new ArrayList(Arrays.asList(array));
                        final ArrayAdapter<String>  pairedDevicesAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, pairedDevicesList);
                        pairedDevicesListView.setAdapter(pairedDevicesAdapter);

                        if (pairedDevices.size() > 0) {
                            final CharSequence[] items = new CharSequence[pairedDevices.size()];
                            final CharSequence[] deviceAddress = new CharSequence[pairedDevices.size()];
                            int index = 0;
                            for (BluetoothDevice device : pairedDevices) {
                                items[index] = device.getName() + "\n" + device.getAddress();
                                deviceAddress[index] = device.getAddress();
                                index++;
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setTitle(getString (R.string.paired_devides))
                                    .setItems(items, new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which){

                                            // Almacenamos el nombre y la direccion del dispositivo seleccionado de forma permanente
                                            SharedPreferences preferences = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("connection_type", "bluetooth");
                                            editor.putString("bluetooth_name", items[which].toString());
                                            editor.putString("bluetooth_address", deviceAddress[which].toString());
                                            editor.commit();
                                        }
                                    });
                            builder.show();
                        } else {
                            Toast.makeText(getActivity(), getString (R.string.no_paired_device), Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                case 1:
                    /* Comienza el codigo del AlerDialog de configuración del acceso al escaner wifi*/

                    // Build and send an Event.
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("device")
                            .setAction("wifi")
                            .build());

                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View dialogview = inflater.inflate(R.layout.wifi_settings_alert, null);
                    final EditText ip = (EditText) dialogview.findViewById(R.id.editIP);
                    final EditText port = (EditText) dialogview.findViewById(R.id.editPort);


                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setView(dialogview);
                    alert.setTitle("WIFI connection setting");

                    // Abrimos el archivo de configuraciones de la app
                    SharedPreferences preferences = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = preferences.edit();

                    // Cargamos los calores de la configuracion actual en los editText
                    ip.setText(preferences.getString("wifi_address", "192.168.0.10"));
                    port.setText(preferences.getString("wifi_port", "35000" ));

                    editor.commit();

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Guardamos los valores introdicidos por los editText en el archivo de configuraciones de la app
                            editor.putString("connection_type", "wifi");
                            editor.putString("wifi_address", ip.getText().toString());
                            editor.putString("wifi_port", port.getText().toString());
                            editor.commit();
                            Toast.makeText(getContext(),"WIFI connection activated",Toast.LENGTH_LONG).show();
                        } // End of onClick(DialogInterface dialog, int whichButton)
                    }); // Final de alert.setPositiveButton
                    alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Cancelar.
                            dialog.cancel();
                        }
                    }); // Final de alert.setNegativeButton
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                break;
            }
        }
        else if(adapterView == rootView.findViewById(R.id.pairedDevicesListView)){
        }
    }
}
