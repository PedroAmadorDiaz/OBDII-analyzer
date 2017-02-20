package es.bitaria.obdii_analyzer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import java.util.Random;

/**
 * Creado por Pedro Amador Diaz el 25/12/2016.
 */
public class OBDChart extends Fragment {

    // Variables glovales de toda la app
    private GlobalClass globalVariable;

    // referencias a alementos del UI
    private View rootView;
    private ListView mPIDListView;
    private LineChart mLineChart;

    // Variables empleadas en la generacion de la lista de PIDs
    private ArrayList<PID> mPIDs = new ArrayList<>();
    int selected = 0;

    public OBDChart() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // El fragment tiene su propio menu
        setHasOptionsMenu(true);

        globalVariable = GlobalClass.getIntante(this.getContext());
        rootView = inflater.inflate(R.layout.fragment_obd_chart, container, false);
        mLineChart = (LineChart)rootView.findViewById(R.id.lineChart);
        mPIDListView = (ListView)rootView.findViewById(R.id.pidsListView);

        // Inicializamos el ListView con los PIDs a mostrar
        for(int index = 0; index < GlobalClass.mPIDs.size();index++)
            mPIDs.add(GlobalClass.mPIDs.get(index));

        globalVariable.mPIDsAdapter = new PIDsAdapter(getActivity(), mPIDs);
        mPIDListView.setAdapter(globalVariable.mPIDsAdapter);

        // Mostramos la grafica sin valores
        for(int index = 0; index < globalVariable.mPIDs.size();index++){
            if(globalVariable.mPIDs.get(index).selected)
                lineChart(globalVariable.mPIDs.get(index).name);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    // Solo muestra la grafica, sin agregar valores
    public void lineChart(String name)
    {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineDataSet lineDataSet0 = new LineDataSet(globalVariable.mOBDyAXES0, name);
        lineDataSet0.setDrawCircles(false);
        lineDataSet0.setDrawValues(false);
        // lineDataSet0.setDrawCubic(true);
        lineDataSet0.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet0);
        mLineChart.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart.setDescription("");
        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart.setTouchEnabled(false);
        mLineChart.setData(new LineData(globalVariable.mOBDxAXES0, lineDataSets));
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    public void lineChart(String name, float yValue)
    {
        // Rellenamos la grafica con los datos que debe mostrar
        if (globalVariable.mOBDxAXES0.size()>50){
            globalVariable.mOBDxAXES0.remove(0);
            globalVariable.mOBDyAXES0.remove(0);

            for(int index = 0; index<50; index++){
                globalVariable.mOBDyAXES0.get(index).setXIndex(index);
            }
        }
        globalVariable.mOBDxAXES0.add(String.valueOf(globalVariable.mOBDxValue0));
        globalVariable.mOBDyAXES0.add(new Entry(yValue, globalVariable.mOBDxValue0));

        ++globalVariable.mOBDxValue0; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet0 = new LineDataSet(globalVariable.mOBDyAXES0, name);
        lineDataSet0.setDrawCircles(false);
        lineDataSet0.setDrawValues(false);
        // lineDataSet0.setDrawCubic(true);
        lineDataSet0.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet0);
        mLineChart.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart.setDescription("");
        YAxis yAxis = mLineChart.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla


        mLineChart.setTouchEnabled(false);
        mLineChart.setData(new LineData(globalVariable.mOBDxAXES0, lineDataSets));
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.obd_chart, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case  R.id.add_sensor:
                pidsListDialog().show();
                return true;
            case R.id.scanner_connection:
                globalVariable.clearChartsMemory(); // Borramos el contenido actual de las graficas
                globalVariable.replay = false; // Pasamos las graficas a mostrar datos en tiempo real

                // Abrimos el archivo de configuraciones de la app
                SharedPreferences preferences = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
                if(preferences.getString("connection_type", "bluetooth").equals("bluetooth"))
                    globalVariable.obd.bluetoothConnect();
                else if(preferences.getString("connection_type", "bluetooth").equals("wifi"))
                    globalVariable.obd.wifiConnect();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    *   Crea un dialogo con una lista de PID que se pueden mostrar
    */
    public AlertDialog pidsListDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] items = new CharSequence[GlobalClass.mPIDs.size()];

        // Cargamos los datos en las arrays para ser mandados al MultiChoiceItems
        for(int index = 0; index < GlobalClass.mPIDs.size();index++){
            items[index] = GlobalClass.mPIDs.get(index).name;
            if(GlobalClass.mPIDs.get(index).selected)
                selected = index;
        }

        builder.setTitle(R.string.choose_sensors).setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                    GlobalClass.mPIDs.get(selected).selected = false;
                    GlobalClass.mPIDs.get(which).selected = true;

                     // Redibuja la grafica
                    if(globalVariable.replay) {
                        globalVariable.sqLite.playSQLiteFile();
                        lineChart(GlobalClass.mPIDs.get(which).name);
                    }
                }
        });
        return builder.create();
    }

}
