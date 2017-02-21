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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Creado por Pedro Amador Diaz el 31/01/2017.
 */

public class PIDsAdapter extends ArrayAdapter<PID> {
    public PIDsAdapter(Context context, List<PID> objects){
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // Obtener inflater
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        // ¿Existe el view actualmente?
        if (convertView == null){
            convertView = inflater.inflate(R.layout.pid_item, parent, false);

            // Referencias UI
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textPIDname);
            holder.value = (TextView) convertView.findViewById(R.id.textPIDvalue);
            holder.unit = (TextView) convertView.findViewById(R.id.textPIDunit);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // PID actual
        PID pid = getItem(position);

        // Setup
        holder.name.setText(pid.name);
        holder.value.setText(String.format("%.2f", pid.value));
        holder.unit.setText(pid.unit);

        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView value;
        TextView unit;
    }
}
