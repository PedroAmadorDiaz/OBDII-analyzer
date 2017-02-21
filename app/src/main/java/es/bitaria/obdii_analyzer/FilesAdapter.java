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
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import es.bitaria.obdii_analyzer.dataStructures.FileData;


/**
 * Creado por Pedro Amador Diaz el 04/02/2017.
 */

public class FilesAdapter extends BaseAdapter {
    // Variables glovales de toda la app
    private GlobalClass globalVariable;

    private List<FileData> mFileDataList;
    private Context mContext;

    public FilesAdapter(Context context, List<FileData> objects){
        super();
        this.mContext = context;
        mFileDataList = objects;
        globalVariable = GlobalClass.getIntante(context);
    }

    @Override
    public int getCount() {
        return mFileDataList.size();
    }

    @Override
    public FileData getItem(int position) {
        return mFileDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mFileDataList.indexOf(getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        // Obtener inflater
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FilesAdapter.ViewHolder holder;

        // ¿Existe el view actualmente?
        if (convertView == null){
            convertView = inflater.inflate(R.layout.file_item, parent, false);

            // Referencias UI
            holder = new FilesAdapter.ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textFileName);
            holder.data = (TextView) convertView.findViewById(R.id.textFileData);
            holder.size = (TextView) convertView.findViewById(R.id.textFileSize);
            holder.btPlay = (ImageButton) convertView.findViewById(R.id.buttonPlay);
            holder.btDelete = (ImageButton) convertView.findViewById(R.id.buttonDelete);
            holder.btDelete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);

                    String dir = Environment.getExternalStorageDirectory().getPath()+"/OBDII-analyzer/";
                    File file = new File(dir, mFileDataList.get(position).name);
                    file.delete();
                    File fileJournal = new File(dir, mFileDataList.get(position).name+"-journal");
                    fileJournal.delete();

                    mFileDataList.remove(position);
                    update();
                }
            });
            holder.btPlay.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);

                    String dir = Environment.getExternalStorageDirectory().getPath()+"/OBDII-analyzer/";
                    globalVariable.playFileName = dir + mFileDataList.get(position).name;
                    globalVariable.replay = true; // Pasamos al modo replay para que no entren nuevos datos en las graficas
                    globalVariable.recording = false;
                    globalVariable.sqLite.playSQLiteFile();

                }
            });
            convertView.setTag(holder);
        }
        else {
            holder = (FilesAdapter.ViewHolder) convertView.getTag();
        }

        // File actual
        FileData fileData = getItem(position);

        // Setup
        holder.name.setText(fileData.name);
        holder.data.setText(fileData.data);
        holder.size.setText(fileData.size);

        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView size;
        ImageButton btPlay;
        ImageButton btDelete;

    }

    // Notificar de los cambios
    private void update(){
        this.notifyDataSetChanged();
    }
}
