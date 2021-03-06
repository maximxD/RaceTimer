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

    public MyPopupWindow(View popupView, MainActivity mainActivity, int popupWindowId) {
        super(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

        showAtLocation(mainActivity.mainLayout, Gravity.CENTER, 0, 0);
        setContentView(popupView);

        popupView.setOnClickListener(view -> dismiss());

        if (popupWindowId == R.layout.popup_window_puzzles) {
            bindChangePuzzleButtons(popupView, mainActivity);
        } else {
            bindPenaltyButton(popupView.findViewById(R.id.penaltyLayout1),
                    mainActivity.timer1.getSolveTimes(), mainActivity.timer1.getSolvePenalties(),
                    mainActivity.timer1.getTextViewTime(), mainActivity.timer1.getAverages(),
                    mainActivity);
            bindPenaltyButton(popupView.findViewById(R.id.penaltyLayout2),
                    mainActivity.timer2.getSolveTimes(), mainActivity.timer2.getSolvePenalties(),
                    mainActivity.timer2.getTextViewTime(), mainActivity.timer2.getAverages(),
                    mainActivity);
        }
    }

    private void bindPenaltyButton(LinearLayout layout, ArrayList<Integer> solveTimes, ArrayList<Integer> solvePenalties,
                                   TextView textViewTime, double[] averages, MainActivity mainActivity) {
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            layout.getChildAt(i).setOnClickListener(view -> {
                mainActivity.setPenalty(solveTimes, solvePenalties, textViewTime, id);
                mainActivity.calculateAverage(solveTimes, solvePenalties, averages);
                mainActivity.recalculateScore();
                dismiss();
            });
        }
    }

    private void bindChangePuzzleButtons(View popupView, MainActivity mainActivity) {
        final int[] BUTTONS_IDS = new int[]{R.id.btnCube2x2, R.id.btnCube3x3, R.id.btnCube4x4,
                R.id.btnCube5x5, R.id.btnCube6x6, R.id.btnCube7x7,
                R.id.btnPyraminx, R.id.btnSquare1, R.id.btnSkewb,
                R.id.btnMegaminx};

        final Map<Integer, String[]> PUZZLES_DICT = getIdToPuzzleDict(BUTTONS_IDS);

        Button[] buttons = new Button[10];
        for (int i = 0; i < 10; i++) {
            buttons[i] = popupView.findViewById(BUTTONS_IDS[i]);
        }

        for (Button button : buttons) {
            button.setOnClickListener(view -> {
                mainActivity.setPuzzleProperties(PUZZLES_DICT.get(button.getId()));
                mainActivity.resetAll();
                this.dismiss();
            });
        }
    }

    private Map<Integer, String[]> getIdToPuzzleDict(int[] BUTTONS_IDS){
        // create id to puzzle properties dictionary (R.id.<button> : {<PUZZLE>, <FONT_SIZE>})
        String[][] PUZZLE_LIST = {{"TWO", "25"}, {"THREE", "23"}, {"FOUR", "19"}, {"FIVE", "18"},
                {"SIX", "17"}, {"SEVEN", "15"}, {"PYRA", "25"}, {"SQ1", "20"}, {"SKEWB", "25"}, {"MEGA", "16"}};
        Map<Integer, String[]> puzzlesDict = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            puzzlesDict.put(BUTTONS_IDS[i], PUZZLE_LIST[i]);
        }
        return puzzlesDict;
    }
}
