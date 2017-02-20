package es.bitaria.obdii_analyzer;

/**
 * Creado por Pedro Amador Diaz el 08/02/2017.
 */

public class IMU {

    public String name;
    public String sqlName;
    public float value;
    public int onChartView;

    public IMU(){
        super();
    }

    public IMU(String name, String sqlName, float value, int onChartView){

        this.name = name;
        this.sqlName = sqlName;
        this.value = value;
        this.onChartView = onChartView;
    }
}
