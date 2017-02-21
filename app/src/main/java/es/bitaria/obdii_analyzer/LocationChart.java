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


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;


/**
 * Creado por Pedro Amador Diaz el 17/01/2017.
 */
public class LocationChart extends Fragment {


    // Variables glovales de toda la app
    private GlobalClass globalVariable;

    // referencias a alementos del UI
    private View rootView;
    private ListView mGPSListView;
    private LineChart mLineChart0;

    // Variables empleadas en la generacion de la lista de datos de GPS
    private ArrayList<Location> mGPSs = new ArrayList<>();
    int selected = 0;

    public LocationChart() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // El fragment tiene su propio menu
        setHasOptionsMenu(true);

        globalVariable = GlobalClass.getIntante(this.getContext());
        rootView = inflater.inflate(R.layout.fragment_gps_chart, container, false);
        mLineChart0 = (LineChart)rootView.findViewById(R.id.lineChart0);
        mGPSListView = (ListView)rootView.findViewById(R.id.gpsListView);

        // Inicializamos el ListView con los PIDs a mostrar
        for(int index = 0; index < GlobalClass.mGPSs.size();index++)
            mGPSs.add(GlobalClass.mGPSs.get(index));

        globalVariable.mGPSsAdapter = new GPSsAdapter(getActivity(), mGPSs);
        mGPSListView.setAdapter(globalVariable.mGPSsAdapter);

        // Mostramos la grafica sin valores
        for(int index = 0; index < globalVariable.mGPSs.size();index++){
            if(globalVariable.mGPSs.get(index).selected)
                lineChart0(globalVariable.mGPSs.get(index).name);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    // Solo muestra la grafica, sin agregar valores
    public void lineChart0(String name)
    {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineDataSet lineDataSet0 = new LineDataSet(globalVariable.mGPSyAXES0, name);
        lineDataSet0.setDrawCircles(false);
        lineDataSet0.setDrawValues(false);
        // lineDataSet0.setDrawCubic(true);
        lineDataSet0.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet0);
        mLineChart0.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart0.setDescription("");
        YAxis yAxis = mLineChart0.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart0.setTouchEnabled(false);
        mLineChart0.setData(new LineData(globalVariable.mGPSxAXES0, lineDataSets));
        mLineChart0.notifyDataSetChanged();
        mLineChart0.invalidate();
    }

    public void lineChart0(String name, float yValue)
    {
        // Rellenamos la grafica con los datos que debe mostrar
        if (globalVariable.mGPSxAXES0.size()>300){
            globalVariable.mGPSxAXES0.remove(0);
            globalVariable.mGPSyAXES0.remove(0);

            for(int index = 0; index<300; index++){
                globalVariable.mGPSyAXES0.get(index).setXIndex(index);
            }
        }
        globalVariable.mGPSxAXES0.add(String.valueOf(globalVariable.mGPSxValue0));
        globalVariable.mGPSyAXES0.add(new Entry(yValue, globalVariable.mGPSxValue0));

        ++globalVariable.mGPSxValue0; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet0 = new LineDataSet(globalVariable.mGPSyAXES0, name);
        lineDataSet0.setDrawCircles(false);
        lineDataSet0.setDrawValues(false);
        // lineDataSet0.setDrawCubic(true);
        lineDataSet0.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet0);
        mLineChart0.setTouchEnabled(false);
        mLineChart0.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart0.setDescription("");
        YAxis yAxis = mLineChart0.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart0.setData(new LineData(globalVariable.mGPSxAXES0, lineDataSets));
        //mLineChart0.notifyDataSetChanged();
        mLineChart0.invalidate();
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.kinetic_chart, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case  R.id.add_sensor:
                chartListDialog().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
*   Crea un dialogo con una lista de datos de localizacion que se pueden mostrar
*/
    public AlertDialog chartListDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] items = new CharSequence[GlobalClass.mGPSs.size()];

        // Cargamos los datos en las arrays para ser mandados al MultiChoiceItems
        for(int index = 0; index < GlobalClass.mGPSs.size();index++){
            items[index] = GlobalClass.mGPSs.get(index).name;
            if(GlobalClass.mGPSs.get(index).selected)
                selected = index;
        }

        builder.setTitle("Choose sensors").setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                for(int index = 0; index < GlobalClass.mGPSs.size();index++)
                    GlobalClass.mGPSs.get(index).selected = false;
                GlobalClass.mGPSs.get(which).selected = true;

                // Redibuja la grafica
                if(globalVariable.replay) {
                    globalVariable.sqLite.playSQLiteFile();
                    lineChart0(GlobalClass.mGPSs.get(which).name);
                }
            }
        });

        return builder.create();
    }
}
