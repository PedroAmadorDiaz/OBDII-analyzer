package es.bitaria.obdii_analyzer;

/**
 * Creado por Pedro Amador Diaz el 09/02/2017.
 */

public class Location {

    public String name;
    public String sqlName;
    public float value;
    public String unit;
    public boolean selected;

    public Location (){
        super();
    }

    public Location (String name, String sqlName, float value, String unit, boolean selected){
        this.name = name;
        this.sqlName = sqlName;
        this.value = value;
        this.unit = unit;
        this.selected = selected;
    }
}
