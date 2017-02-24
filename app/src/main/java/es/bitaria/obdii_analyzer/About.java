package es.bitaria.obdii_analyzer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.Fragment;

/**
 * Creado por Pedro Amador Diaz el 24/02/2017.
 */

public class About extends Fragment {

    // referencias a alementos del UI
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView url = (TextView) rootView.findViewById(R.id.tURL);
        Button policy = (Button) rootView.findViewById(R.id.btPolicy);

        url.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.bitaria.es/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        policy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.bitaria.es/policy/Condiciones de uso y politica de privacidad.pdf");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }
}
