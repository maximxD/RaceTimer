package com.maximxd.racetimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FrameLayout main_layout;
    private TimeButton btn_time_1, btn_time_2;
    private TextView textView_score_1, textView_score_2;
    private TextView textView_scramble_1, textView_scramble_2;

    private String[] puzzle_properties = {"THREE", "23"};    // format: {<PUZZLE>, <FONT_SIZE>}
    private final ArrayList<String> scrambles = new ArrayList<>();

    private final ArrayList<Integer> wins_list = new ArrayList<>();    // list of winners for each solve

    private final ArrayList<int[]> solve_times_1 = new ArrayList<>();    // format: {<TIME>, <PENALTY_ID>}
    private final ArrayList<int[]> solve_times_2 = new ArrayList<>();    // format: {<TIME>, <PENALTY_ID>}

    private final float[] averages_1 = {0, 0, 0, 0, 0};
    private final float[] averages_2 = {0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_layout = findViewById(R.id.main);
        btn_time_1 = findViewById(R.id.btn_time_1);
        btn_time_2 = findViewById(R.id.btn_time_2);
        TextView textView_time_1 = findViewById(R.id.textView_time_1);
        TextView textView_time_2 = findViewById(R.id.textView_time_2);
        textView_score_1 = findViewById(R.id.textView_score_1);
        textView_score_2 = findViewById(R.id.textView_score_2);
        textView_scramble_1 = findViewById(R.id.textView_scramble_1);
        textView_scramble_2 = findViewById(R.id.textView_scramble_2);

        process_new_scramble();

        btn_time_1.textView = textView_time_1;
        btn_time_2.textView = textView_time_2;
        btn_time_1.mainActivity = this;
        btn_time_2.mainActivity = this;

        final int[] BUTTONS_IDS = new int[] {R.id.btn_cube2x2, R.id.btn_cube3x3, R.id.btn_cube4x4,
                R.id.btn_cube5x5, R.id.btn_cube6x6, R.id.btn_cube7x7,
                R.id.btn_pyraminx, R.id.btn_square1, R.id.btn_skewb,
                R.id.btn_megaminx};
        final Map<Integer, String[]> PUZZLES_DICT = get_id_to_puzzle_dict(BUTTONS_IDS);

        Button btn_puzzles = findViewById(R.id.btn_puzzles);
        btn_puzzles.setOnClickListener(view -> {
            if (!btn_time_1.is_started && !btn_time_2.is_started) {
                bind_change_puzzle_buttons(get_popup_window(R.layout.activity_puzzles_popup), PUZZLES_DICT, BUTTONS_IDS);
            }
        });

        Button btn_options = findViewById(R.id.btn_options);
        btn_options.setOnClickListener(view -> {
            if (btn_time_1.is_processed && btn_time_2.is_processed && !btn_time_1.is_started && !btn_time_2.is_started) {
                bind_penalty_buttons(get_popup_window(R.layout.activity_penalties_popup));
            }
        });

        Button btn_stats_1 = findViewById(R.id.stats_1);
        btn_stats_1.setOnClickListener(view -> {
            if (!btn_time_1.is_started && !btn_time_2.is_started) {
                set_averages(get_popup_window(R.layout.activity_stats_popup));
            }
        });

        Button btn_stats_2 = findViewById(R.id.stats_2);
        btn_stats_2.setOnClickListener(view -> {
            if (!btn_time_1.is_started && !btn_time_2.is_started) {
                set_averages(get_popup_window(R.layout.activity_stats_popup));
            }
        });
    }

    private Object[] get_popup_window(int layout) {
        // create popup window and return it to bind buttons on it
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(layout, main_layout, false);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        popupWindow.showAtLocation(this.main_layout, Gravity.CENTER, 0, 0);
        popupWindow.setContentView(popupView);

        popupView.setOnClickListener(view -> popupWindow.dismiss());
        return new Object[]{popupView, popupWindow};
    }

    private void set_averages(Object[] popupObjects) {
        View popupView = (View) popupObjects[0];

        LinearLayout ll_avg_list_1, ll_avg_list_2;
        ll_avg_list_1 = popupView.findViewById(R.id.avg_list_1);
        ll_avg_list_2 = popupView.findViewById(R.id.avg_list_2);

        int[] avg_num_list = {5, 12, 25, 50, 100};
        String[] avg_str_list = {getString(R.string.avg5), getString(R.string.avg12), getString(R.string.avg25),
                getString(R.string.avg50), getString(R.string.avg100)};

        int column_in_ll = 0;
        for (int i = 0; i < 5; i++) {
            if (solve_times_1.size() >= avg_num_list[i]) {
                // get LinearLayout (column) in LinearLayout of averages first cuber
                LinearLayout ll_column_avg_1 = (LinearLayout) ll_avg_list_1.getChildAt(column_in_ll);
                // get TextView in LinearLayout
                TextView text_view_avg_1 = (TextView) ll_column_avg_1.getChildAt(i % 3);
                // get LinearLayout (column) in LinearLayout of averages second cuber
                LinearLayout ll_column_avg_2 = (LinearLayout) ll_avg_list_2.getChildAt(column_in_ll);
                // get TextView in LinearLayout
                TextView text_view_avg_2 = (TextView) ll_column_avg_2.getChildAt(i % 3);
                String new_avg_str;
                if (averages_1[i] != 0) {
                    new_avg_str = avg_str_list[i] + " " + TimeButton.get_formatted_time(averages_1[i], 2);
                } else {
                    new_avg_str = avg_str_list[i] + " DNF";
                }
                text_view_avg_1.setText(new_avg_str);
                if (averages_2[i] != 0) {
                    new_avg_str = avg_str_list[i] + " " + TimeButton.get_formatted_time(averages_2[i], 2);
                } else {
                    new_avg_str = avg_str_list[i] + " DNF";
                }
                text_view_avg_2.setText(new_avg_str);
            }
            else {
                break;
            }

            if (i == 2) {
                // go to second column in LinearLayout of averages
                column_in_ll++;
            }
        }
        // set solves (format: solves: solves_length_without_dnf / solves_length)
        LinearLayout ll_column = (LinearLayout) ll_avg_list_1.getChildAt(1);
        TextView text_view_solve = (TextView) ll_column.getChildAt(2);
        ArrayList<int[]> solve_times_wo_dnf = new ArrayList<>(solve_times_1);
        solve_times_wo_dnf.removeIf(n -> (n[1] == 3));
        String solves_str = getString(R.string.solves) + " " + solve_times_wo_dnf.size() + "/" + solve_times_1.size();
        text_view_solve.setText(solves_str);
        ll_column = (LinearLayout) ll_avg_list_2.getChildAt(1);
        text_view_solve = (TextView) ll_column.getChildAt(2);
        solve_times_wo_dnf = new ArrayList<>(solve_times_2);
        solve_times_wo_dnf.removeIf(n -> (n[1] == 3));
        solves_str = getString(R.string.solves) + " " + solve_times_wo_dnf.size() + "/" + solve_times_2.size();
        text_view_solve.setText(solves_str);
    }

    private void bind_penalty_buttons(Object[] popupObjects) {
        // bind penalty buttons on popup window
        View popupView = (View) popupObjects[0];
        PopupWindow popupWindow = (PopupWindow) popupObjects[1];

        LinearLayout penalties_1 = popupView.findViewById(R.id.penalties_1);
        LinearLayout penalties_2 = popupView.findViewById(R.id.penalties_2);

        // bind penalty buttons for first cuber
        for (int i = 0; i < 3; i++) {
            int id = i;
            penalties_1.getChildAt(i).setOnClickListener(view -> {
                set_penalty(solve_times_1, btn_time_1.textView, id + 1);
                calculate_all_avg();
                recalculate_score();
                popupWindow.dismiss();
            });
        }

        // bind penalty buttons for second cuber
        for (int i = 0; i < 3; i++) {
            int id = i;
            penalties_2.getChildAt(i).setOnClickListener(view -> {
                set_penalty(solve_times_2, btn_time_2.textView, id + 1);
                calculate_all_avg();
                recalculate_score();
                popupWindow.dismiss();
            });
        }
    }

    private void recalculate_score() {
        if (wins_list.size() > 0) {
            wins_list.remove(wins_list.size() - 1);

            set_score(solve_times_1.get(solve_times_1.size() - 1)[0], solve_times_2.get(solve_times_2.size() - 1)[0],
                    solve_times_1.get(solve_times_1.size() - 1)[1], solve_times_2.get(solve_times_2.size() - 1)[1]);
        }
    }

    private void set_score(float time_1, float time_2, float penalty_id_1, float penalty_id_2) {
        if (solve_times_1.size() > 0) {
            if (penalty_id_1 == 3 || penalty_id_2 == 3) {
                // if any cuber got DNF
                if (penalty_id_1 == 3 && penalty_id_2 != 3) {
                    // if only cuber_1 got DNF, then cuber_2 won
                    wins_list.add(2);
                } else if (penalty_id_1 != 3) {
                    // if only cuber_2 got DNF, then cuber_1 won
                    wins_list.add(1);
                } else {
                    // both cubers got DNF, it is draw
                    wins_list.add(0);
                }
            } else if (time_1 < time_2) {
                wins_list.add(1);
            } else if (time_1 > time_2) {
                wins_list.add(2);
            } else {
                wins_list.add(0);
            }
            textView_score_1.setText(HtmlCompat.fromHtml(get_score_html_string(
                                     Collections.frequency(wins_list, 1),
                                     Collections.frequency(wins_list, 2)),
                                     HtmlCompat.FROM_HTML_MODE_LEGACY));
            textView_score_2.setText(HtmlCompat.fromHtml(get_score_html_string(
                                     Collections.frequency(wins_list, 2),
                                     Collections.frequency(wins_list, 1)),
                                     HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            textView_score_1.setText(HtmlCompat.fromHtml(get_score_html_string(0, 0), HtmlCompat.FROM_HTML_MODE_LEGACY));
            textView_score_2.setText(HtmlCompat.fromHtml(get_score_html_string(0, 0), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    private void set_penalty(ArrayList<int[]> solve_times, TextView textView, int penalty_id) {
        // set penalty on current solve (OK -> penalty_id == 1, +2 -> penalty_id == 2, DNF -> penalty_id == 3)
        int number_of_solves = solve_times.size();
        if (number_of_solves > 0 && solve_times.get(number_of_solves - 1)[1] != penalty_id) {
            int curr_time = solve_times.get(number_of_solves - 1)[0];
            if (solve_times.get(number_of_solves - 1)[1] == 2) {
                curr_time -= 2000;
            }
            if (penalty_id == 2) {
                curr_time += 2000;
            }

            solve_times.add(new int[] {curr_time, penalty_id});
            solve_times.remove(number_of_solves - 1);
            String new_time_str;
            if (penalty_id == 1) {
                new_time_str = TimeButton.get_formatted_time(curr_time, 3);
            } else if (penalty_id == 2) {
                new_time_str = TimeButton.get_formatted_time(curr_time, 3) + "+";
            } else {
                new_time_str = getString(R.string.dnf);
            }
            textView.setText(new_time_str);
        }
    }

    private void bind_change_puzzle_buttons(Object[] popupObjects, Map<Integer, String[]> PUZZLES_DICT, int[] BUTTONS_IDS) {
        // bind change puzzle buttons on popup window
        View popupView = (View) popupObjects[0];
        PopupWindow popupWindow = (PopupWindow) popupObjects[1];

        Button[] buttons = new Button[10];
        for (int i = 0; i < 10; i++) {
            buttons[i] = popupView.findViewById(BUTTONS_IDS[i]);
        }

        for (Button button: buttons) {
            button.setOnClickListener(view -> {
                popupWindow.dismiss();
                change_puzzle(PUZZLES_DICT, button);
                reset_all();
            });
        }
    }

    private void calculate_all_avg() {
        int[] avg_num_list = {5, 12, 25, 50, 100};
        int[] thrown_list = {1, 1, 2, 3, 5};
        for (int i = 0; i < 5; i++) {
            if (solve_times_1.size() >= avg_num_list[i]) {
                averages_1[i] = get_avg(new ArrayList<>(solve_times_1), avg_num_list[i], thrown_list[i]);
                averages_2[i] = get_avg(new ArrayList<>(solve_times_2), avg_num_list[i], thrown_list[i]);
            }
            else {
                break;
            }
        }
    }

    private float get_avg(ArrayList<int[]> solve_times, int avg_of, int thrown) {
        int number_of_solves = solve_times.size();
        ArrayList<int[]> solve_times_need = new ArrayList<>(solve_times.subList(number_of_solves - avg_of, number_of_solves));
        solve_times_need.removeIf(n -> (n[1] == 3));
        if (solve_times_need.size() < avg_of - thrown) {
            return 0;
        } else {
            // sort times to make average counting easier
            sort_solve_times(solve_times_need, 0, solve_times_need.size() - 1);

            solve_times_need.subList(0, thrown).clear();

            while (solve_times_need.size() != avg_of - thrown*2) {
                solve_times_need.remove(solve_times_need.size() - 1);
            }

            return get_mean(solve_times_need);
        }
    }

    private float get_mean(ArrayList<int[]> solve_times) {
        int sum_times = 0;
        int count = 0;

        for (int[] time: solve_times) {
            sum_times += time[0] - time[0] % 10;
            count += 1;
        }
        return (float) (sum_times - sum_times % 10) / count;
    }

    private static void sort_solve_times(ArrayList<int[]> solve_times, int low, int high) {
        if (solve_times.size() == 0) {
            return;
        }
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        int[] main = solve_times.get(middle);

        int i = low, j = high;
        while (i <= j) {
            while (solve_times.get(i)[0] < main[0]) {
                i++;
            }

            while (solve_times.get(j)[0] > main[0]) {
                j--;
            }

            if (i <= j) {
                Collections.swap(solve_times, i, j);
                i++;
                j--;
            }
        }

        if (low < j)
            sort_solve_times(solve_times, low, j);

        if (high > i)
            sort_solve_times(solve_times, i, high);
    }

    private void reset_all() {
        // reset all stats
        scrambles.clear();
        solve_times_1.clear();
        solve_times_2.clear();
        process_new_scramble();
        wins_list.clear();
        btn_time_1.is_processed = true;
        btn_time_2.is_processed = true;
        btn_time_1.textView.setText(getString(R.string.start_time));
        btn_time_2.textView.setText(getString(R.string.start_time));
        set_score(0, 0, 1, 1);
    }

    private void set_scramble_font_size(int font_size) {
        textView_scramble_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);
        textView_scramble_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);
    }

    private void change_puzzle(Map<Integer, String[]> PUZZLES_DICT, Button button) {
        puzzle_properties = Objects.requireNonNull(PUZZLES_DICT.get(button.getId()));
    }

    private Map<Integer, String[]> get_id_to_puzzle_dict(int[] BUTTONS_IDS){
        // create id to puzzle properties dictionary (R.id.<button> : {<PUZZLE>, <FONT_SIZE>})
        String[][] PUZZLE_LIST = {{"TWO", "25"}, {"THREE", "23"}, {"FOUR_FAST", "19"}, {"FIVE", "18"},
                {"SIX", "17"}, {"SEVEN", "15"}, {"PYRA", "25"}, {"SQ1", "20"}, {"SKEWB", "25"}, {"MEGA", "16"}};
        Map<Integer, String[]> puzzles_dict = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            puzzles_dict.put(BUTTONS_IDS[i], PUZZLE_LIST[i]);
        }
        return puzzles_dict;
    }

    protected boolean isBothSolved() {
        // did both cubers solved cubes?
        return !btn_time_1.is_processed && !btn_time_2.is_processed;
    }

    protected void processTimes() {
        int time1 = (int) btn_time_1.curr_time;
        int time2 = (int) btn_time_2.curr_time;

        solve_times_1.add(new int[] {time1, 1});
        solve_times_2.add(new int[] {time2, 1});

        set_score(time1, time2 ,1, 1);
        btn_time_1.is_processed = true;
        btn_time_2.is_processed = true;
        calculate_all_avg();
    }

    protected void process_new_scramble() {
        // generate and set new scramble in another thread
        Thread thread = new Thread(() -> {
            if (scrambles.isEmpty()) {
                // if scramble list is empty, generate first scramble
                runOnUiThread(() -> {
                    // set text "Generating scramble..."
                    set_scramble_font_size(25);
                    set_new_scramble(getString(R.string.generating_scramble));
                });
                // generate and add new scramble to scramble list
                scrambles.add(PuzzleRegistry.valueOf(puzzle_properties[0]).getScrambler().generateScrambles(1)[0]);
            }
            runOnUiThread(() -> {
                // set last scramble from scramble list
                set_new_scramble(scrambles.get(scrambles.size() - 1));
                set_scramble_font_size(Integer.parseInt(puzzle_properties[1]));
            });
            // generate and add new scramble to scramble list to avoid waiting when setting up the next scramble
            scrambles.add(PuzzleRegistry.valueOf(puzzle_properties[0]).getScrambler().generateScrambles(1)[0]);
        });
        thread.start();
    }

    private String get_score_html_string(int score1, int score2) {
        return "<font color='blue'>" + score1 + "</font> : " + score2;
    }

    private void set_new_scramble(String scramble) {
        textView_scramble_1.setText(scramble);
        textView_scramble_2.setText(scramble);
    }
}