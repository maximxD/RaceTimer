package com.maximxd.racetimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScramblesList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrambles_list);

        Bundle extras = getIntent().getExtras();

        ListView list_view_scrambles_1 = findViewById(R.id.list_view_scrambles_1);
        ListView list_view_scrambles_2 = findViewById(R.id.list_view_scrambles_2);

        ArrayList<String> scrambles = extras.getStringArrayList("scrambles");
        int font_size = Integer.parseInt(extras.getString("font_size"));

        ArrayAdapter <String> adapter = new ArrayAdapter<String>(this, R.layout.scramble_text_view, scrambles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                TextView tv = (TextView) super.getView(position, convertView, parent);

                String new_text = (position + 1) + ". " + tv.getText().toString();
                tv.setText(new_text);

                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);

                return tv;
            }
        };

        list_view_scrambles_1.setAdapter(adapter);
        list_view_scrambles_2.setAdapter(adapter);

        Button btn_back_to_timer = findViewById(R.id.btn_back_to_timer);
        btn_back_to_timer.setOnClickListener(view -> onBackPressed());
    }
}