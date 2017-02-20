package es.bitaria.obdii_analyzer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
public class KineticChart extends Fragment {

    private KineticChart kineticChart;

    // Variables glovales de toda la app
    private GlobalClass globalVariable;

    // referencias a alementos del UI
    private View rootView;
    private LineChart mLineChart0, mLineChart1, mLineChart2, mLineChart3;

    public KineticChart() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // El fragment tiene su propio menu
        setHasOptionsMenu(true);

        globalVariable = GlobalClass.getIntante(this.getContext());
        rootView = inflater.inflate(R.layout.fragment_kinetic_chart, container, false);
        mLineChart0 = (LineChart)rootView.findViewById(R.id.lineChart0);
        mLineChart1 = (LineChart)rootView.findViewById(R.id.lineChart1);
        mLineChart2 = (LineChart)rootView.findViewById(R.id.lineChart2);
        mLineChart3 = (LineChart)rootView.findViewById(R.id.lineChart3);

        // Inicializamos las graficas
        for (int index = 0; index < globalVariable.mIMUs.size(); index++) {
            if (globalVariable.mIMUs.get(index).onChartView > -1)
                switch (globalVariable.mIMUs.get(index).onChartView) {
                    case 0:
                        lineChart0(globalVariable.mIMUs.get(index).name);
                        break;
                    case 1:
                        lineChart1(globalVariable.mIMUs.get(index).name);
                        break;
                    case 2:
                        lineChart2(globalVariable.mIMUs.get(index).name);
                        break;
                    case 3:
                        lineChart3(globalVariable.mIMUs.get(index).name);
                        break;
                }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    // Solo muestra la grafica, sin agregar valores
    public void lineChart0(String name)
    {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineDataSet lineDataSet0 = new LineDataSet(globalVariable.mIMUyAXES0, name);
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
        mLineChart0.setData(new LineData(globalVariable.mIMUxAXES0, lineDataSets));
        mLineChart0.notifyDataSetChanged();
        mLineChart0.invalidate();
    }

    // Solo muestra la grafica, sin agregar valores
    public void lineChart1(String name)
    {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineDataSet lineDataSet1 = new LineDataSet(globalVariable.mIMUyAXES1, name);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setDrawValues(false);
        // lineDataSet1.setDrawCubic(true);
        lineDataSet1.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet1);
        mLineChart1.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart1.setDescription("");
        YAxis yAxis = mLineChart1.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart1.setTouchEnabled(false);
        mLineChart1.setData(new LineData(globalVariable.mIMUxAXES1, lineDataSets));
        mLineChart1.notifyDataSetChanged();
        mLineChart1.invalidate();
    }

    // Solo muestra la grafica, sin agregar valores
    public void lineChart2(String name)
    {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineDataSet lineDataSet2 = new LineDataSet(globalVariable.mIMUyAXES2, name);
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setDrawValues(false);
        // lineDataSet2.setDrawCubic(true);
        lineDataSet2.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet2);
        mLineChart2.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart2.setDescription("");
        YAxis yAxis = mLineChart2.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart2.setTouchEnabled(false);
        mLineChart2.setData(new LineData(globalVariable.mIMUxAXES2, lineDataSets));
        mLineChart2.notifyDataSetChanged();
        mLineChart2.invalidate();
    }

    // Solo muestra la grafica, sin agregar valores
    public void lineChart3(String name)
    {

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineDataSet lineDataSet3 = new LineDataSet(globalVariable.mIMUyAXES3, name);
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setDrawValues(false);
        // lineDataSet3.setDrawCubic(true);
        lineDataSet3.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet3);
        mLineChart3.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart3.setDescription("");
        YAxis yAxis = mLineChart3.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart3.setTouchEnabled(false);
        mLineChart3.setData(new LineData(globalVariable.mIMUxAXES3, lineDataSets));
        mLineChart3.notifyDataSetChanged();
        mLineChart3.invalidate();
    }

    public void lineChart0(String name, float yValue)
    {
        // Rellenamos la grafica con los datos que debe mostrar
        if (globalVariable.mIMUxAXES0.size()>300){
            globalVariable.mIMUxAXES0.remove(0);
            globalVariable.mIMUyAXES0.remove(0);

            for(int index = 0; index<300; index++){
                globalVariable.mIMUyAXES0.get(index).setXIndex(index);
            }
        }
        globalVariable.mIMUxAXES0.add(String.valueOf(globalVariable.mIMUxValue0));
        globalVariable.mIMUyAXES0.add(new Entry(yValue, globalVariable.mIMUxValue0));

        ++globalVariable.mIMUxValue0; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet0 = new LineDataSet(globalVariable.mIMUyAXES0, name);
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
        mLineChart0.setData(new LineData(globalVariable.mIMUxAXES0, lineDataSets));
        //mLineChart0.notifyDataSetChanged();
        mLineChart0.invalidate();
    }

    public void lineChart1(String name, float yValue)
    {
        // Rellenamos la grafica con los datos que debe mostrar
        if (globalVariable.mIMUxAXES1.size()>300){
            globalVariable.mIMUxAXES1.remove(0);
            globalVariable.mIMUyAXES1.remove(0);

            for(int index = 0; index<300; index++){
                globalVariable.mIMUyAXES1.get(index).setXIndex(index);
            }
        }
        globalVariable.mIMUxAXES1.add(String.valueOf(globalVariable.mIMUxValue1));
        globalVariable.mIMUyAXES1.add(new Entry(yValue, globalVariable.mIMUxValue1));

        ++globalVariable.mIMUxValue1; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet1 = new LineDataSet(globalVariable.mIMUyAXES1, name);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setDrawValues(false);
        // lineDataSet2.setDrawCubic(true);
        lineDataSet1.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet1);
        mLineChart1.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart1.setDescription("");
        YAxis yAxis = mLineChart1.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart1.setTouchEnabled(false);
        mLineChart1.setData(new LineData(globalVariable.mIMUxAXES1, lineDataSets));
        //mLineChart1.notifyDataSetChanged();
        mLineChart1.invalidate();
    }

    public void lineChart2(String name, float yValue)
    {
        // Rellenamos la grafica con los datos que debe mostrar
        if (globalVariable.mIMUxAXES2.size()>300){
            globalVariable.mIMUxAXES2.remove(0);
            globalVariable.mIMUyAXES2.remove(0);

            for(int index = 0; index<300; index++){
                globalVariable.mIMUyAXES2.get(index).setXIndex(index);
            }
        }
        globalVariable.mIMUxAXES2.add(String.valueOf(globalVariable.mIMUxValue2));
        globalVariable.mIMUyAXES2.add(new Entry(yValue, globalVariable.mIMUxValue2));

        ++globalVariable.mIMUxValue2; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet2 = new LineDataSet(globalVariable.mIMUyAXES2, name);
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setDrawValues(false);
        // lineDataSet2.setDrawCubic(true);
        lineDataSet2.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet2);
        mLineChart2.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart2.setDescription("");
        YAxis yAxis = mLineChart2.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart2.setTouchEnabled(false);
        mLineChart2.setData(new LineData(globalVariable.mIMUxAXES2, lineDataSets));
        //mLineChart2.notifyDataSetChanged();
        mLineChart2.invalidate();
    }

    public void lineChart3(String name, float yValue)
    {
        // Rellenamos la grafica con los datos que debe mostrar
        if (globalVariable.mIMUxAXES3.size()>300){
            globalVariable.mIMUxAXES3.remove(0);
            globalVariable.mIMUyAXES3.remove(0);

            for(int index = 0; index<300; index++){
                globalVariable.mIMUyAXES3.get(index).setXIndex(index);
            }
        }
        globalVariable.mIMUxAXES3.add(String.valueOf(globalVariable.mIMUxValue3));
        globalVariable.mIMUyAXES3.add(new Entry(yValue, globalVariable.mIMUxValue3));

        ++globalVariable.mIMUxValue3; // Aumentamos en una unidad el eje x pues ha entrado uns nueva serie de valores y

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet3 = new LineDataSet(globalVariable.mIMUyAXES3, name);
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setDrawValues(false);
        // lineDataSet3.setDrawCubic(true);
        lineDataSet3.setColor(Color.BLUE);
        lineDataSets.add(lineDataSet3);
        mLineChart3.getAxisRight().setEnabled(false); // No valores en el eje derecho
        mLineChart3.setDescription("");
        YAxis yAxis = mLineChart3.getAxisLeft();
        yAxis.setDrawGridLines(false); // No lineas de rejilla

        mLineChart3.setTouchEnabled(false);
        mLineChart3.setData(new LineData(globalVariable.mIMUxAXES3, lineDataSets));
        //mLineChart3.notifyDataSetChanged();
        mLineChart3.invalidate();
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
*   Crea un dialogo con una lista de PID que se pueden mostrar
*/
    public AlertDialog chartListDialog(){
        final Spinner spChart0, spChart1, spChart2, spChart3;
        ArrayAdapter<String> aaChart;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.chart_kinetic_dialog, null);
        builder.setView(view);

        spChart0 = (Spinner) view.findViewById(R.id.spChart0);
        spChart1 = (Spinner) view.findViewById(R.id.spChart1);
        spChart2 = (Spinner) view.findViewById(R.id.spChart2);
        spChart3 = (Spinner) view.findViewById(R.id.spChart3);

        String[] items = new String[GlobalClass.mIMUs.size()];

        // Cargamos los datos en las arrays para ser mandados al MultiChoiceItems
        for(int index = 0; index < GlobalClass.mIMUs.size();index++)
            items[index] = GlobalClass.mIMUs.get(index).name;

        aaChart = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_spinner_item, items);

        spChart0.setAdapter(aaChart);
        spChart1.setAdapter(aaChart);
        spChart2.setAdapter(aaChart);
        spChart3.setAdapter(aaChart);

        // Seleccionamos los valores por defecto en los spinners
        for(int index = 0; index < GlobalClass.mIMUs.size();index++){
            if(GlobalClass.mIMUs.get(index).onChartView > -1)
                switch (GlobalClass.mIMUs.get(index).onChartView)
                {
                    case 0:
                        spChart0.setSelection(index);
                        break;
                    case 1:
                        spChart1.setSelection(index);
                        break;
                    case 2:
                        spChart2.setSelection(index);
                        break;
                    case 3:
                        spChart3.setSelection(index);
                        break;
                }
        }

        spChart0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                for(int index = 0; index < GlobalClass.mIMUs.size();index++) {
                    if (GlobalClass.mIMUs.get(index).onChartView == 0)
                        GlobalClass.mIMUs.get(index).onChartView = -1;
                }
                GlobalClass.mIMUs.get(spChart0.getSelectedItemPosition()).onChartView = 0;

                // Redibuja la grafica si estamos en el modo replay
                if(globalVariable.replay){
                    globalVariable.sqLite.playSQLiteFile();
                    lineChart0(GlobalClass.mIMUs.get(spChart0.getSelectedItemPosition()).name);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spChart1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                for(int index = 0; index < GlobalClass.mIMUs.size();index++) {
                    if (GlobalClass.mIMUs.get(index).onChartView == 1)
                        GlobalClass.mIMUs.get(index).onChartView = -1;
                }
                GlobalClass.mIMUs.get(spChart1.getSelectedItemPosition()).onChartView = 1;

                // Redibuja la grafica
                if(globalVariable.replay) {
                    globalVariable.sqLite.playSQLiteFile();
                    lineChart1(GlobalClass.mIMUs.get(spChart1.getSelectedItemPosition()).name);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spChart2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                for(int index = 0; index < GlobalClass.mIMUs.size();index++) {
                    if (GlobalClass.mIMUs.get(index).onChartView == 2)
                        GlobalClass.mIMUs.get(index).onChartView = -1;
                }
                GlobalClass.mIMUs.get(spChart2.getSelectedItemPosition()).onChartView = 2;

                // Redibuja la grafica
                if(globalVariable.replay){
                    globalVariable.sqLite.playSQLiteFile();
                    lineChart2(GlobalClass.mIMUs.get(spChart2.getSelectedItemPosition()).name);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spChart3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                for(int index = 0; index < GlobalClass.mIMUs.size();index++) {
                    if (GlobalClass.mIMUs.get(index).onChartView == 3)
                        GlobalClass.mIMUs.get(index).onChartView = -1;
                }
                GlobalClass.mIMUs.get(spChart3.getSelectedItemPosition()).onChartView = 3;

                // Redibuja la grafica
                if(globalVariable.replay) {
                    globalVariable.sqLite.playSQLiteFile();
                    lineChart3(GlobalClass.mIMUs.get(spChart3.getSelectedItemPosition()).name);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        return builder.create();
    }

}
