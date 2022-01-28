package com.maximxd.racetimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScramblesListActivity extends AppCompatActivity {

    private int font_size;
    private ArrayList<String> scrambles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrambles_list);

        Bundle extras = getIntent().getExtras();

        ListView list_view_scrambles_1 = findViewById(R.id.list_view_scrambles_1);
        ListView list_view_scrambles_2 = findViewById(R.id.list_view_scrambles_2);

        scrambles = extras.getStringArrayList("scrambles");
        font_size = extras.getInt("font_size");
        ArrayList<Integer> solve_times_1 = extras.getIntegerArrayList("solve_times_1");
        ArrayList<Integer> solve_times_2 = extras.getIntegerArrayList("solve_times_2");
        ArrayList<Integer> solve_penalties_1 = extras.getIntegerArrayList("solve_penalties_1");
        ArrayList<Integer> solve_penalties_2 = extras.getIntegerArrayList("solve_penalties_2");

        set_my_adapter(list_view_scrambles_1, solve_times_1, solve_penalties_1);
        set_my_adapter(list_view_scrambles_2, solve_times_2, solve_penalties_2);

        Button btn_back_to_timer = findViewById(R.id.btn_back_to_timer);
        btn_back_to_timer.setOnClickListener(view -> onBackPressed());
    }

    private ArrayList<String> get_string_times(ArrayList<Integer> solve_times, ArrayList<Integer> solve_penalties) {
        ArrayList<String> strings_times = new ArrayList<>();
        String string_time;
        for (int i = 0; i < solve_times.size(); i++) {
            if (solve_penalties.get(i) != 3) {
                string_time = TimeButton.get_formatted_time(solve_times.get(i));
                if (solve_penalties.get(i) == 2) {
                    string_time = string_time + "+";
                }
            } else {
                string_time = "DNF(" + TimeButton.get_formatted_time(solve_times.get(i)) + ")";
            }
            strings_times.add(string_time);
        }
        return strings_times;
    }

    private List<HashMap<String, String>> get_hash_map_solves(ArrayList<String> scrambles, ArrayList<String> string_times) {
        List<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 0; i < scrambles.size(); i++) {
            HashMap<String, String> hash_map_solve = new HashMap<>();
            hash_map_solve.put("scramble", scrambles.get(i));
            hash_map_solve.put("time", string_times.get(i));
            list.add(hash_map_solve);
        }
        return list;
    }

    private void set_my_adapter (ListView list_view_scrambles, ArrayList<Integer> solve_times, ArrayList<Integer> solve_penalties) {
        ArrayList<String> string_times = get_string_times(solve_times, solve_penalties);
        List<HashMap<String, String>> hash_map_solves = get_hash_map_solves(scrambles, string_times);
        SimpleAdapter adapter = new SimpleAdapter(this, hash_map_solves, R.layout.scramble_text_view,
                                                  new String[]{"scramble", "time"},
                                                  new int[]{R.id.text_view_scramble, R.id.text_view_time}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                LinearLayout layout = (LinearLayout) super.getView(position, convertView, parent);

                TextView tv = (TextView) layout.getChildAt(0);
                String new_text = (position + 1) + ") " + tv.getText().toString();
                tv.setText(new_text);

                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);

                return layout;
            }
        };

        list_view_scrambles.setAdapter(adapter);
    }
}