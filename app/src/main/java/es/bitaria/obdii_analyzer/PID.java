package es.bitaria.obdii_analyzer;

/**
 * Creado por Pedro Amador Diaz el 31/01/2017.
 */

public class PID {
    public String id;
    public String name;
    public float value;
    public String unit;
    public boolean selected;

    public PID (){
        super();
    }

    public PID (String id, String name, float value, String unit, boolean selected){
        this.id = id;
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.selected = selected;
    }
}
