package com.maximxd.racetimer;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPopupWindow extends PopupWindow {

    public MyPopupWindow(View popup_view, MainActivity main_activity, int popup_window_id) {
        super(popup_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

        showAtLocation(main_activity.main_layout, Gravity.CENTER, 0, 0);
        setContentView(popup_view);

        popup_view.setOnClickListener(view -> dismiss());

        if (popup_window_id == R.layout.popup_window_puzzles) {
            bind_change_puzzle_buttons(popup_view, main_activity);
        } else {
            bind_penalty_button(popup_view.findViewById(R.id.penalty_layout_1), main_activity.solve_times_1,
                    main_activity.solve_penalties_1, main_activity.btn_time_1.text_view_time, main_activity.averages_1,
                    main_activity);
            bind_penalty_button(popup_view.findViewById(R.id.penalty_layout_2), main_activity.solve_times_2,
                    main_activity.solve_penalties_2, main_activity.btn_time_2.text_view_time, main_activity.averages_2,
                    main_activity);
        }
    }

    private void bind_penalty_button(LinearLayout layout, ArrayList<Integer> solve_times, ArrayList<Integer> solve_penalties,
                                     TextView text_view_time, double[] averages, MainActivity main_activity) {
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            layout.getChildAt(i).setOnClickListener(view -> {
                main_activity.set_penalty(solve_times, solve_penalties, text_view_time, id);
                main_activity.calculate_average(solve_times, solve_penalties, averages);
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
        main_activity.puzzle_properties = PUZZLES_DICT.get(button.getId());
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
