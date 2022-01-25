package com.maximxd.racetimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    protected RelativeLayout main_layout;
    protected TimeButton btn_time_1, btn_time_2;

    protected String[] puzzle_properties = {"THREE", "23"};    // format: {<PUZZLE>, <FONT_SIZE>}

    protected final ArrayList<int[]> solve_times_1 = new ArrayList<>();    // format: {<TIME>, <PENALTY_ID>}
    protected final ArrayList<int[]> solve_times_2 = new ArrayList<>();    // format: {<TIME>, <PENALTY_ID>}

    protected float[] averages_1 = {0, 0, 0, 0, 0};
    protected float[] averages_2 = {0, 0, 0, 0, 0};

    private final boolean[] is_stats_shows = {false, false};

    private TextView textView_score_1, textView_score_2;
    private TextView textView_scramble_1, textView_scramble_2;

    private final ArrayList<String> scrambles = new ArrayList<>();
    private final ArrayList<Integer> wins_list = new ArrayList<>();    // list of winners for each solve

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

        btn_time_1.text_view_time = textView_time_1;
        btn_time_2.text_view_time = textView_time_2;
        btn_time_1.solve_times = solve_times_1;
        btn_time_2.solve_times = solve_times_2;
        btn_time_1.main_activity = this;
        btn_time_2.main_activity = this;

        Button btn_puzzles = findViewById(R.id.btn_puzzles);
        btn_puzzles.setOnClickListener(view -> {
            if (!btn_time_1.is_started && !btn_time_2.is_started) {
                if (is_stats_shows[0]) {
                    hide_stats(R.id.btn_stats_1, R.id.layout_stats_1, 0);
                }
                if (is_stats_shows[1]) {
                    hide_stats(R.id.btn_stats_2, R.id.layout_stats_2, 1);
                }
                create_puzzle_popup_window();
            }
        });

        Button btn_penalties = findViewById(R.id.btn_penalties);
        btn_penalties.setOnClickListener(view -> {
            if (btn_time_1.is_processed && btn_time_2.is_processed && !btn_time_1.is_started && !btn_time_2.is_started) {
                if (is_stats_shows[0]) {
                    hide_stats(R.id.btn_stats_1, R.id.layout_stats_1, 0);
                }
                if (is_stats_shows[1]) {
                    hide_stats(R.id.btn_stats_2, R.id.layout_stats_2, 1);
                }
                create_penalty_popup_window();
            }
        });

        Button btn_stats_1 = findViewById(R.id.btn_stats_1);
        btn_stats_1.setOnClickListener(view -> {
            if (!btn_time_1.is_started) {
                show_stats(R.id.btn_stats_1, R.id.layout_stats_1, 0);
            }
        });

        Button btn_stats_2 = findViewById(R.id.btn_stats_2);
        btn_stats_2.setOnClickListener(view -> {
            if (!btn_time_2.is_started) {
                show_stats(R.id.btn_stats_2, R.id.layout_stats_2, 1);
            }
        });

        Button btn_close_stats_1 = findViewById(R.id.btn_close_stats_1);
        btn_close_stats_1.setOnClickListener(view -> hide_stats(R.id.btn_stats_1, R.id.layout_stats_1, 0));

        Button btn_close_stats_2 = findViewById(R.id.btn_close_stats_2);
        btn_close_stats_2.setOnClickListener(view -> hide_stats(R.id.btn_stats_2, R.id.layout_stats_2, 1));
    }

    private void create_penalty_popup_window() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_penalty, main_layout, false);

        new MyPopupWindow(popupView, this, R.layout.popup_window_penalty);
    }

    private void create_puzzle_popup_window() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_puzzles, main_layout, false);

        new MyPopupWindow(popupView, this, R.layout.popup_window_puzzles);
    }

    private void show_stats(int btn_stats_id, int gl_stat_list_id, int stat_num) {
        if (stat_num == 0) {
            set_average(findViewById(R.id.stat_list_1), solve_times_1, averages_1);
        } else {
            set_average(findViewById(R.id.stat_list_2), solve_times_2, averages_2);
        }

        is_stats_shows[stat_num] = true;

        Button btn_stat = findViewById(btn_stats_id);
        btn_stat.setVisibility(View.GONE);

        RelativeLayout gl_stat_list = findViewById(gl_stat_list_id);
        gl_stat_list.setVisibility(View.VISIBLE);
    }

    private void hide_stats(int btn_stats_id, int gl_stat_list_id, int stat_num) {
        is_stats_shows[stat_num] = false;

        Button btn_stat = findViewById(btn_stats_id);
        btn_stat.setVisibility(View.VISIBLE);

        RelativeLayout gl_stat_list = findViewById(gl_stat_list_id);
        gl_stat_list.setVisibility(View.GONE);
    }

    private void reset_averages() {
        String[] avg_str_list = {"avg5: ", "avg12: ", "avg25: ", "avg50: ", "avg100: "};
        GridLayout gl_stat_list_1 = (GridLayout) findViewById(R.id.stat_list_1);
        GridLayout gl_stat_list_2 = (GridLayout) findViewById(R.id.stat_list_2);
        TextView text_view_avg;
        for (int i = 0; i < 5; i++) {
            text_view_avg = (TextView) gl_stat_list_1.getChildAt(i);
            text_view_avg.setText(avg_str_list[i]);

            text_view_avg = (TextView) gl_stat_list_2.getChildAt(i);
            text_view_avg.setText(avg_str_list[i]);
        }
        String solves = "solves: 0/0";
        text_view_avg = (TextView) gl_stat_list_1.getChildAt(5);
        text_view_avg.setText(solves);

        text_view_avg = (TextView) gl_stat_list_2.getChildAt(5);
        text_view_avg.setText(solves);
    }

    private void set_average(GridLayout gl_avg_list, ArrayList<int[]> solve_times, float[] averages) {
        int[] avg_num_list = {5, 12, 25, 50, 100};
        String[] avg_str_list = {"avg5: ", "avg12: ", "avg25: ", "avg50: ", "avg100: "};

        for (int i = 0; i < 5; i++) {
            if (solve_times.size() >= avg_num_list[i]) {
                TextView text_view_avg_1 = (TextView) gl_avg_list.getChildAt(i);
                String new_avg_str;
                if (averages[i] != 0) {
                    new_avg_str = avg_str_list[i] + TimeButton.get_formatted_time(averages[i]);
                } else {
                    new_avg_str = avg_str_list[i] + "DNF";
                }
                text_view_avg_1.setText(new_avg_str);
            }
            else {
                break;
            }
        }
        TextView text_view_solve = (TextView) gl_avg_list.getChildAt(5);
        int without_dnf_count = 0;
        for (int[] solve: solve_times) {
            if (solve[1] != 3) {
                without_dnf_count += 1;
            }
        }

        String solves_str = "solves: " + without_dnf_count + "/" + solve_times.size();
        text_view_solve.setText(solves_str);
    }

    protected void recalculate_score() {
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

    protected void set_penalty(ArrayList<int[]> solve_times, TextView textView, int penalty_id) {
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
                new_time_str = TimeButton.get_formatted_time(curr_time);
            } else if (penalty_id == 2) {
                new_time_str = TimeButton.get_formatted_time(curr_time) + "+";
            } else {
                new_time_str = getString(R.string.dnf);
            }
            textView.setText(new_time_str);
        }
    }

    protected void calculate_all_avg() {
        int[] avg_num_list = {5, 12, 25, 50, 100};
        int[] thrown_list = {1, 1, 2, 3, 5};
        for (int i = 0; i < 5; i++) {
            if (solve_times_1.size() >= avg_num_list[i]) {
                averages_1[i] = get_avg(new ArrayList<>(solve_times_1), avg_num_list[i], thrown_list[i]);
            }
            if (solve_times_2.size() >= avg_num_list[i]) {
                averages_2[i] = get_avg(new ArrayList<>(solve_times_2), avg_num_list[i], thrown_list[i]);
            }
            if (solve_times_1.size() < avg_num_list[i] && solve_times_2.size() < avg_num_list[i]) {
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

    protected void reset_all() {
        // reset all stats
        scrambles.clear();
        process_new_scramble();

        solve_times_1.clear();
        solve_times_2.clear();
        wins_list.clear();

        btn_time_1.is_processed = true;
        btn_time_2.is_processed = true;
        btn_time_1.text_view_time.setText(getString(R.string.start_time));
        btn_time_2.text_view_time.setText(getString(R.string.start_time));

        averages_1 = new float[] {0, 0, 0, 0, 0};
        averages_2 = new float[] {0, 0, 0, 0, 0};
        reset_averages();

        set_score(0, 0, 1, 1);
    }

    private void set_scramble_font_size(int font_size) {
        textView_scramble_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);
        textView_scramble_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);
    }

    protected boolean isBothSolved() {
        // did both cubers solved cubes?
        return !btn_time_1.is_processed && !btn_time_2.is_processed;
    }

    protected void processTimes() {
        set_score(btn_time_1.curr_time, btn_time_2.curr_time ,1, 1);
        btn_time_1.is_processed = true;
        btn_time_2.is_processed = true;
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