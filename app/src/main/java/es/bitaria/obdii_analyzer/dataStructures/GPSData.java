package es.bitaria.obdii_analyzer.dataStructures;

/**
 * Creado por Pedro Amador Diaz el 09/01/2017.
 */

public class GPSData {
    public float latitude, longitude, altitude, bearing, speed, accuracy;

    public GPSData(){
        super();
    }

    public GPSData(float latitude, float longitude, float altitude, float bearing, float speed, float accuracy){
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.bearing = bearing;
        this.speed = speed;
        this.accuracy = accuracy;
    }
}
