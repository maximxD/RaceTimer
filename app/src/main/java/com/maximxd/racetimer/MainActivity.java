package com.maximxd.racetimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private TextView textView_score_1, textView_score_2;
    private TextView textView_scramble_1, textView_scramble_2;
    private Button btn_stats_1, btn_stats_2;
    private LinearLayout layout_stats_1, layout_stats_2;

    private final ArrayList<String> scrambles = new ArrayList<>();
    private final ArrayList<Integer> wins_list = new ArrayList<>();    // list of winners for each solve

    protected RelativeLayout main_layout;
    protected TimeButton btn_time_1, btn_time_2;

    protected String[] puzzle_properties = {"THREE", "23"};    // format: {<PUZZLE>, <FONT_SIZE>}

    protected final ArrayList<Integer> solve_times_1 = new ArrayList<>();
    protected final ArrayList<Integer> solve_times_2 = new ArrayList<>();

    protected final ArrayList<Integer> solve_penalties_1 = new ArrayList<>();
    protected final ArrayList<Integer> solve_penalties_2 = new ArrayList<>();

    protected double[] averages_1 = {0, 0, 0, 0, 0};
    protected double[] averages_2 = {0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_layout = findViewById(R.id.main);
        btn_time_1 = findViewById(R.id.btn_time_1);
        btn_time_2 = findViewById(R.id.btn_time_2);
        textView_score_1 = findViewById(R.id.textView_score_1);
        textView_score_2 = findViewById(R.id.textView_score_2);
        textView_scramble_1 = findViewById(R.id.textView_scramble_1);
        textView_scramble_2 = findViewById(R.id.textView_scramble_2);
        btn_stats_1 = findViewById(R.id.btn_stats_1);
        btn_stats_2 = findViewById(R.id.btn_stats_2);
        layout_stats_1 = findViewById(R.id.layout_stats_1);
        layout_stats_2 = findViewById(R.id.layout_stats_2);

        process_new_scramble();

        btn_time_1.text_view_time = findViewById(R.id.textView_time_1);
        btn_time_2.text_view_time = findViewById(R.id.textView_time_2);
        btn_time_1.solve_times = solve_times_1;
        btn_time_2.solve_times = solve_times_2;
        btn_time_1.solve_penalties = solve_penalties_1;
        btn_time_2.solve_penalties = solve_penalties_2;
        btn_time_1.main_activity = this;
        btn_time_2.main_activity = this;
        btn_time_1.btn_stats = btn_stats_1;
        btn_time_1.layout_stats = layout_stats_1;
        btn_time_2.btn_stats = btn_stats_2;
        btn_time_2.layout_stats = layout_stats_2;
        btn_time_1.averages = averages_1;
        btn_time_2.averages = averages_2;

        Button btn_puzzles = findViewById(R.id.btn_puzzles);
        btn_puzzles.setOnClickListener(view -> {
            if (!btn_time_1.is_started && !btn_time_2.is_started) {
                hide_stats(btn_stats_1, layout_stats_1);
                hide_stats(btn_stats_2, layout_stats_2);
                create_puzzle_popup_window();
            }
        });

        Button btn_penalties = findViewById(R.id.btn_penalties);
        btn_penalties.setOnClickListener(view -> {
            if (btn_time_1.is_processed && btn_time_2.is_processed && !btn_time_1.is_started && !btn_time_2.is_started) {
                hide_stats(btn_stats_1, layout_stats_1);
                hide_stats(btn_stats_2, layout_stats_2);

                create_penalty_popup_window();
            }
        });

        btn_stats_1.setOnClickListener(view -> {
            if (!btn_time_1.is_started) {
                set_average(findViewById(R.id.stat_list_1), solve_penalties_1, averages_1);
                show_stats(btn_stats_1, layout_stats_1);
            }
        });

        btn_stats_2.setOnClickListener(view -> {
            if (!btn_time_2.is_started) {
                set_average(findViewById(R.id.stat_list_2), solve_penalties_2, averages_2);
                show_stats(btn_stats_2, layout_stats_2);
            }
        });

        Button btn_scrambles_1 = findViewById(R.id.btn_scrambles_1);
        btn_scrambles_1.setOnClickListener(view -> {
            if (!btn_time_2.is_started && btn_time_1.is_processed && btn_time_2.is_processed) {
                Intent intent = new Intent(this, ScramblesListActivity.class);
                intent.putExtra("scrambles", new ArrayList<>(scrambles.subList(0, solve_times_1.size())));
                intent.putExtra("font_size", Integer.parseInt(puzzle_properties[1]));
                intent.putExtra("solve_times_1", solve_times_1);
                intent.putExtra("solve_times_2", solve_times_2);
                intent.putExtra("solve_penalties_1", solve_penalties_1);
                intent.putExtra("solve_penalties_2", solve_penalties_2);
                startActivity(intent);
                hide_stats(btn_stats_1, layout_stats_1);
                hide_stats(btn_stats_2, layout_stats_2);
            }
        });

        Button btn_scrambles_2 = findViewById(R.id.btn_scrambles_2);
        btn_scrambles_2.setOnClickListener(view -> {
            if (!btn_time_1.is_started && btn_time_1.is_processed && btn_time_2.is_processed) {
                Intent intent = new Intent(this, ScramblesListActivity.class);
                intent.putExtra("scrambles", new ArrayList<>(scrambles.subList(0, solve_times_2.size())));
                intent.putExtra("font_size", Integer.parseInt(puzzle_properties[1]));
                intent.putExtra("solve_times_1", solve_times_1);
                intent.putExtra("solve_times_2", solve_times_2);
                intent.putExtra("solve_penalties_1", solve_penalties_1);
                intent.putExtra("solve_penalties_2", solve_penalties_2);
                startActivity(intent);
                hide_stats(btn_stats_1, layout_stats_1);
                hide_stats(btn_stats_2, layout_stats_2);
            }
        });
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

    private void show_stats(Button btn_stats, LinearLayout layout_stats) {
        layout_stats.setVisibility(View.VISIBLE);

        btn_stats.setVisibility(View.GONE);
    }

    protected void hide_stats(Button btn_stats, LinearLayout layout_stats) {
        layout_stats.setVisibility(View.GONE);

        btn_stats.setVisibility(View.VISIBLE);
    }

    private void reset_averages() {
        String[] avg_str_list = {"avg5: ", "avg12: ", "avg25: ", "avg50: ", "avg100: "};
        GridLayout gl_stat_list_1 = findViewById(R.id.stat_list_1);
        GridLayout gl_stat_list_2 = findViewById(R.id.stat_list_2);
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

    protected void set_average(GridLayout gl_avg_list, ArrayList<Integer> solve_times, double[] averages) {
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
        for (int solve: solve_times) {
            if (solve != 3) {
                without_dnf_count += 1;
            }
        }

        String solves_str = "solves: " + without_dnf_count + "/" + solve_times.size();
        text_view_solve.setText(solves_str);
    }

    protected void recalculate_score() {
        if (wins_list.size() > 0) {
            wins_list.remove(wins_list.size() - 1);

            set_score(solve_times_1.get(solve_times_1.size() - 1), solve_times_2.get(solve_times_2.size() - 1),
                    solve_penalties_1.get(solve_penalties_1.size() - 1), solve_penalties_2.get(solve_penalties_2.size() - 1));
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
                    // both cubers got DNF, it is a draw
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

    protected void set_penalty(ArrayList<Integer> solve_times, ArrayList<Integer> solve_penalties, TextView textView, int penalty_id) {
        // set penalty on current solve (penalty_id == 1 -> OK; penalty_id == 2 -> +2; penalty_id == 3 -> DNF)
        int number_of_solves = solve_times.size();
        if (number_of_solves > 0 && solve_penalties.get(number_of_solves - 1) != penalty_id) {
            int curr_time = solve_times.get(number_of_solves - 1);
            if (solve_penalties.get(number_of_solves - 1) == 2) {
                curr_time -= 2000;    // -2 seconds
            }
            if (penalty_id == 2) {
                curr_time += 2000;    // +2 seconds
            }

            solve_times.add(curr_time);
            solve_penalties.add(penalty_id);
            solve_times.remove(number_of_solves - 1);
            solve_penalties.remove(number_of_solves - 1);
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

    protected void calculate_average(ArrayList<Integer> solve_times, ArrayList<Integer> solve_penalties, double[] averages) {
        int[] avg_num_list = {5, 12, 25, 50, 100};
        int[] thrown_list = {1, 1, 2, 3, 5};
        for (int i = 0; i < 5; i++) {
            if (solve_times.size() >= avg_num_list[i]) {
                averages[i] = get_avg(solve_times, solve_penalties, avg_num_list[i], thrown_list[i]);
            }
            else {
                break;
            }
        }
    }

    private double get_avg(ArrayList<Integer> solve_times, ArrayList<Integer> solve_penalties, int avg_of, int thrown) {
        int number_of_solves = solve_times.size();
        ArrayList<Integer> solve_times_need = new ArrayList<>();
        for (int i = number_of_solves - avg_of; i < number_of_solves; i++) {
            // Add only non-dnf solves in solve_times_need
            if (solve_penalties.get(i) != 3) {
                solve_times_need.add(solve_times.get(i));
            }
        }
        if (solve_times_need.size() < avg_of - thrown) {
            return 0;
        } else {
            // sort times to make average counting easier
            sort_solve_times(solve_times_need, 0, solve_times_need.size() - 1);

            solve_times_need.subList(0, thrown).clear();

            solve_times_need.subList(avg_of - thrown*2, solve_times_need.size()).clear();

            return get_mean(solve_times_need);
        }
    }

    private double get_mean(ArrayList<Integer> solve_times) {
        int sum_times = 0;
        int count = 0;

        for (int time: solve_times) {
            sum_times += time - time % 10;
            count += 1;
        }

        return (double) (sum_times - sum_times % 10) / count;
    }

    private static void sort_solve_times(ArrayList<Integer> solve_times, int low, int high) {
        if (solve_times.size() == 0) {
            return;
        }
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        int main = solve_times.get(middle);

        int i = low, j = high;
        while (i <= j) {
            while (solve_times.get(i) < main) {
                i++;
            }
            while (solve_times.get(j) > main) {
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
        solve_penalties_1.clear();
        solve_penalties_2.clear();

        wins_list.clear();

        btn_time_1.is_processed = true;
        btn_time_2.is_processed = true;
        btn_time_1.text_view_time.setText(getString(R.string.start_time));
        btn_time_2.text_view_time.setText(getString(R.string.start_time));

        for (int i = 0; i < 5; i++) {
            averages_1[i] = 0;
            averages_2[i] = 0;
        }
        reset_averages();

        set_score(0, 0, 1, 1);
    }

    private void set_scramble_font_size(int font_size) {
        textView_scramble_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);
        textView_scramble_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);
    }

    protected boolean isBothSolved() {
        // did both cubers solve cubes?
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