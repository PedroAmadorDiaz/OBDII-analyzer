package es.bitaria.obdii_analyzer.dataStructures;

/**
 * Creado por Pedro Amador Diaz el 09/01/2017.
 */

public class IMUData {
    public float[] accelerometer = new float[3];
    public float[] gyroscope = new float[3];
    public float[] magnetometer = new float[3];
    public float linearAcceleration;

    public IMUData(){
        super();
    }

    public IMUData(float[] accelerometer, float[] gyroscope, float[] magnetometer, float linearAcceleration) {
        this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;
        this.linearAcceleration = linearAcceleration;
        this.magnetometer = magnetometer;
    }
}
