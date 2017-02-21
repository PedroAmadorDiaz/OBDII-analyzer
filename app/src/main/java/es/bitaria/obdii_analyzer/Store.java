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

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.bitaria.obdii_analyzer.dataStructures.FileData;

/**
 * Creado por Pedro Amador Diaz el 17/01/2017.
 */
public class Store extends Fragment {

    private static final String TAG= "Store";
    // Variables glovales de toda la app
    private GlobalClass globalVariable;

    private ListView fileListView;
    private Button btRecord;
    private EditText etNameFile;
    private TextView txLeftTime;
    private List<File> files;

    // Variables empleadas en la generacion de la lista de PIDs
    private ArrayList<FileData> mFileDatas = new ArrayList<>();

    public Store() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        globalVariable = GlobalClass.getIntante(this.getContext());

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_store, container, false);
        fileListView = (ListView) rootView.findViewById(R.id.fileListView);
        btRecord = (Button) rootView.findViewById(R.id.btRecord);
        etNameFile = (EditText) rootView.findViewById(R.id.editText);
        txLeftTime = (TextView) rootView.findViewById(R.id.textLeftTime);

        // Actualizamos la lista de archivos, mostrando su contenido actual
        fileListUpdate();

        if(globalVariable.recording == false)
        {
            // Cambiar la apariencia del boton
            btRecord.setBackgroundColor(Color.parseColor("#303F9F"));
            btRecord.setText( getString (R.string.record));
        }
        else
        {
            // Cambiar la apariencia del boton
            btRecord.setBackgroundColor(Color.parseColor("#606060"));
            btRecord.setText( getString (R.string.recording));
        }

        btRecord.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(globalVariable.recording == false && etNameFile.getText().toString().equals("")){
                    etNameFile.setHint(getString (R.string.file_name_required));
                    return;
                }

                if (globalVariable.recording == false)
                    startRecod();
                else
                    stopRecord();
            }
        });

        return rootView;
    }

    // Retorna la lista de archivos tipo SQLite
    private List<File> readSQLiteFiles()
    {
        List<File> resultListFiles = new ArrayList<File>();
        File root = new File(Environment.getExternalStorageDirectory().getPath()+"/OBDII-analyzer/");
        File[] files = root.listFiles();

        if(files != null) {
            for (int index = 0; index < files.length; index++) {
                File file = files[index];
                String fileName = file.getName();
                if (fileName.endsWith(".db3")) // Condición para comprobar la extensión de archivo .db3
                    resultListFiles.add(files[index]);
            }
        }
        return resultListFiles;
    }

    // Actualiza el contenido del ListView con los achivos encontrados por la funcion
    private void fileListUpdate()
    {
        // Busqueda de los archivos de SQLite almacenados en la memoria interna readSQLiteFiles
        files = readSQLiteFiles();
        mFileDatas.clear();
        for(int index=0; index<files.size(); index++) {
            FileData mFileDataItem = new FileData();
            mFileDataItem.name = files.get(index).getAbsoluteFile().getName();
            mFileDataItem.data = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(files.get(index).getAbsoluteFile().lastModified()));
            mFileDataItem.size = Long.toString(files.get(index).getAbsoluteFile().length());
            mFileDatas.add(mFileDataItem);
        }

        // Rellenamos el ListView con los datos de los archivos encontrados en el directorio del sistema
        FilesAdapter  adapter = new FilesAdapter(getActivity(), mFileDatas);
        fileListView.setAdapter(adapter);
    }

    // Indicas en el UI el tiempo que queda de grabacion
    // Parametro de entrada leftTime: Tiempo en segundos que queda para finalizar la grabacion
    public void setLeftTime(int leftTime){
        txLeftTime.setText(String.format("%1$02d", leftTime));
    }

    // Metodo que inicia una nueva grabacion
    public void startRecod(){

        // Se crea una nueva base de datos
        globalVariable.sqLite.newDB(etNameFile.getText().toString());
        // Si era una base de datos ya existente, borramos su contenido
        globalVariable.sqLite.deleteTables();

        // Borrado de los datos que puedan quedar en la memoria RAM (se descartan)
        globalVariable.mIMUDataList.clear();
        globalVariable.mGPSDataList.clear();

        // Activamos el flag para permitir la insecion de datos en la BD
        globalVariable.recording = true;

        // Cambiar la apariencia del boton
        btRecord.setBackgroundColor(Color.parseColor("#606060"));
        btRecord.setText( getString (R.string.recording));
    }

    // Metodo que finaciza una grabacion en curso
    public void stopRecord(){

        // Desactivamos el flag para detener la insercion de datos en la BD
        globalVariable.recording = false;
        setLeftTime(0);

        // Se guardan los datos que queden en la memoria RAM
        globalVariable.sqLite.insertGPSList(globalVariable.mGPSDataList);
        globalVariable.sqLite.insertIMUList(globalVariable.mIMUDataList);

        // Se borran los datos de la memoria RAM una vez han sido guardados en la BD
        globalVariable.mGPSDataList.clear();
        globalVariable.mIMUDataList.clear();

        // Cambiar la apariencia del boton
        btRecord.setBackgroundColor(Color.parseColor("#303F9F"));
        btRecord.setText( getString (R.string.record));

        // Actualizamos la lista de archivos, mostrando su contenido actual
        fileListUpdate();
    }

}
