package com.maximxd.racetimer;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyPopupWindow extends PopupWindow {

    public MyPopupWindow(View popup_view, MainActivity main_activity, int popup_window_id) {
        super(popup_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

        showAtLocation(main_activity.main_layout, Gravity.CENTER, 0, 0);
        setContentView(popup_view);

        popup_view.setOnClickListener(view -> dismiss());

        if (popup_window_id == R.layout.popup_window_puzzles) {
            bind_change_puzzle_buttons(popup_view, main_activity);
        } else if (popup_window_id == R.layout.popup_window_penalty) {
            bind_penalty_button(popup_view.findViewById(R.id.penalty_layout_1), main_activity.solve_times_1,
                    main_activity.btn_time_1.text_view_time, main_activity);
            bind_penalty_button(popup_view.findViewById(R.id.penalty_layout_2), main_activity.solve_times_2,
                    main_activity.btn_time_2.text_view_time, main_activity);
        } else {
            set_average(popup_view.findViewById(R.id.stat_list_1), main_activity.solve_times_1, main_activity.averages_1);
            set_average(popup_view.findViewById(R.id.stat_list_2), main_activity.solve_times_2, main_activity.averages_2);
        }
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

    private void bind_penalty_button(LinearLayout layout, ArrayList<int[]> solve_times, TextView text_view_time, MainActivity main_activity) {
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            layout.getChildAt(i).setOnClickListener(view -> {
                main_activity.set_penalty(solve_times, text_view_time, id);
                main_activity.calculate_all_avg();
                main_activity.recalculate_score();
                dismiss();
            });
        }
    }

    private void bind_change_puzzle_buttons(View popup_view, MainActivity main_activity) {
        final int[] BUTTONS_IDS = new int[]{R.id.btn_cube2x2, R.id.btn_cube3x3, R.id.btn_cube4x4,
                R.id.btn_cube5x5, R.id.btn_cube6x6, R.id.btn_cube7x7,
                R.id.btn_pyraminx, R.id.btn_square1, R.id.btn_skewb,
                R.id.btn_megaminx};

        final Map<Integer, String[]> PUZZLES_DICT = get_id_to_puzzle_dict(BUTTONS_IDS);

        Button[] buttons = new Button[10];
        for (int i = 0; i < 10; i++) {
            buttons[i] = popup_view.findViewById(BUTTONS_IDS[i]);
        }

        for (Button button : buttons) {
            button.setOnClickListener(view -> {
                change_puzzle(PUZZLES_DICT, button, main_activity);
                main_activity.reset_all();
                this.dismiss();
            });
        }
    }

    private void change_puzzle(Map<Integer, String[]> PUZZLES_DICT, Button button, MainActivity main_activity) {
        main_activity.puzzle_properties = Objects.requireNonNull(PUZZLES_DICT.get(button.getId()));
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
}
