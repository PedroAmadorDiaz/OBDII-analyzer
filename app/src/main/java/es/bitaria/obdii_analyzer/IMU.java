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
