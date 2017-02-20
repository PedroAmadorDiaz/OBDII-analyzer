package es.bitaria.obdii_analyzer.dataStructures;

/**
 * Creado por Pedro Amador Diaz el 29/01/2017.
 */

public class OBDData {
    public float engineLoad, engineTemperature, intake_manifold, engineSpeed, vehicleSpeed, ignition_advance, throttlePosition;

    public OBDData(){
        super();
    }

    public OBDData(float engineLoad, float engineTemperature, float intake_manifold, float engineSpeed, float vehicleSpeed, float ignition_advance, float throttlePosition){
        this.engineLoad = engineLoad;
        this.engineTemperature = engineTemperature;
        this.intake_manifold = intake_manifold;
        this.engineSpeed = engineSpeed;
        this.vehicleSpeed = vehicleSpeed;
        this.ignition_advance = ignition_advance;
        this.throttlePosition = throttlePosition;
    }
}
