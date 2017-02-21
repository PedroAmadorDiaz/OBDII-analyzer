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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * Creado por Pedro Amador Diaz el 08/01/2017.
 */

public class SQLiteDB extends SQLiteOpenHelper {
    private Context mContext;
    private static final String TAG= "SQLiteDB";
    public static final int ID = 3;
    private String tableCreateOBD ="CREATE TABLE IF NOT EXISTS obd (id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , engine_load REAL, engine_temperature REAL, intake_manifold REAL, engine_speed REAL, vehicle_speed REAL, ignition_advance REAL, throttle_position REAL)";
    private String tableCreateGPS ="CREATE TABLE IF NOT EXISTS gps (id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , latitude REAL, longitude REAL, altitude REAL, bearing REAL, speed REAL, accuracy REAL)";
    private String tableCreateIMU ="CREATE TABLE IF NOT EXISTS imu (id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , acceleration_x REAL, acceleration_y REAL, acceleration_z REAL, gyroscope_x REAL, gyroscope_y REAL, gyroscope_z REAL, magnetometer_x REAL, magnetometer_y REAL, magnetometer_z REAL, linear_acceleration REAL)";

    public SQLiteDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Si no existe la tabla la creamos
        sqLiteDatabase.execSQL(tableCreateOBD);
        sqLiteDatabase.execSQL(tableCreateGPS);
        sqLiteDatabase.execSQL(tableCreateIMU);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // Se elimitan las versiones anteriores de las tablas
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS obd");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS gps");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS imu");

        // Creamos la nueva version de las tablas
        sqLiteDatabase.execSQL(tableCreateOBD);
        sqLiteDatabase.execSQL(tableCreateGPS);
        sqLiteDatabase.execSQL(tableCreateIMU);


    }
}
