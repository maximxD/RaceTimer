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

    private int fontSize;
    private ArrayList<String> scrambles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrambles_list);

        Bundle extras = getIntent().getExtras();

        ListView listViewScrambles1 = findViewById(R.id.listViewScrambles1);
        ListView listViewScrambles2 = findViewById(R.id.listViewScrambles2);

        scrambles = extras.getStringArrayList("scrambles");
        fontSize = extras.getInt("fontSize");
        ArrayList<Integer> solveTimes1 = extras.getIntegerArrayList("solveTimes1");
        ArrayList<Integer> solveTimes2 = extras.getIntegerArrayList("solveTimes2");
        ArrayList<Integer> solvePenalties1 = extras.getIntegerArrayList("solvePenalties1");
        ArrayList<Integer> solvePenalties2 = extras.getIntegerArrayList("solvePenalties2");

        setMyAdapter(listViewScrambles1, solveTimes1, solvePenalties1);
        setMyAdapter(listViewScrambles2, solveTimes2, solvePenalties2);

        Button btnBackToTimer = findViewById(R.id.btnBackToTimer);
        btnBackToTimer.setOnClickListener(view -> onBackPressed());
    }

    private ArrayList<String> getStringTimes(ArrayList<Integer> solveTimes, ArrayList<Integer> solvePenalties) {
        ArrayList<String> stringsTimes = new ArrayList<>();
        String stringTime;
        for (int i = 0; i < solveTimes.size(); i++) {
            if (solvePenalties.get(i) != 3) {
                stringTime = TimeButton.getFormattedTime(solveTimes.get(i));
                if (solvePenalties.get(i) == 2) {
                    stringTime = stringTime + "+";
                }
            } else {
                stringTime = "DNF(" + TimeButton.getFormattedTime(solveTimes.get(i)) + ")";
            }
            stringsTimes.add(stringTime);
        }
        return stringsTimes;
    }

    private List<HashMap<String, String>> getHashMapSolves(ArrayList<String> scrambles, ArrayList<String> stringTimes) {
        List<HashMap<String, String>> list = new ArrayList<>();
        for (int i = scrambles.size() - 1; i > -1; i--) {
            // write solves in the list from the last to the first (in the list view the last solve will be at the top)
            HashMap<String, String> hashMapSolve = new HashMap<>();
            hashMapSolve.put("scramble", scrambles.get(i));
            hashMapSolve.put("time", stringTimes.get(i));
            list.add(hashMapSolve);
        }
        return list;
    }

    private void setMyAdapter(ListView listViewScrambles, ArrayList<Integer> solveTimes, ArrayList<Integer> solvePenalties) {
        ArrayList<String> stringTimes = getStringTimes(solveTimes, solvePenalties);
        List<HashMap<String, String>> hashMapSolves = getHashMapSolves(scrambles, stringTimes);
        SimpleAdapter adapter = new SimpleAdapter(this, hashMapSolves, R.layout.scramble_text_view,
                                                  new String[]{"scramble", "time"},
                                                  new int[]{R.id.textViewScramble, R.id.textViewTime}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                LinearLayout layout = (LinearLayout) super.getView(position, convertView, parent);

                TextView textView = (TextView) layout.getChildAt(0);
                String newText = (scrambles.size() - position) + ") " + textView.getText().toString();
                textView.setText(newText);

                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

                return layout;
            }
        };

        listViewScrambles.setAdapter(adapter);
    }
}