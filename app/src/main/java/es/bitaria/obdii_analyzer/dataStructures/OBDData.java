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
