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

    private TextView textViewScore1, textViewScore2;
    private TextView textViewScramble1, textViewScramble2;
    private Button btnStats1, btnStats2;
    private LinearLayout layoutStats1, layoutStats2;

    private final ArrayList<String> scrambles = new ArrayList<>();
    private final ArrayList<Integer> winsList = new ArrayList<>();    // list of winners for each solve

    protected RelativeLayout mainLayout;
    protected TimeButton btnTime1, btnTime2;

    protected String[] puzzleProperties = {"THREE", "23"};    // format: {<PUZZLE>, <FONT_SIZE>}

    protected final ArrayList<Integer> solveTimes1 = new ArrayList<>();
    protected final ArrayList<Integer> solveTimes2 = new ArrayList<>();

    protected final ArrayList<Integer> solvePenalties1 = new ArrayList<>();
    protected final ArrayList<Integer> solvePenalties2 = new ArrayList<>();

    protected double[] averages1 = {0, 0, 0, 0, 0};
    protected double[] averages2 = {0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        btnTime1 = findViewById(R.id.btnTime1);
        btnTime2 = findViewById(R.id.btnTime2);
        textViewScore1 = findViewById(R.id.textViewScore1);
        textViewScore2 = findViewById(R.id.textViewScore2);
        textViewScramble1 = findViewById(R.id.textViewScramble1);
        textViewScramble2 = findViewById(R.id.textViewScramble2);
        btnStats1 = findViewById(R.id.btnStats1);
        btnStats2 = findViewById(R.id.btnStats2);
        layoutStats1 = findViewById(R.id.layoutStats1);
        layoutStats2 = findViewById(R.id.layoutStats2);

        processNewScramble();

        btnTime1.textViewTime = findViewById(R.id.textViewTime1);
        btnTime2.textViewTime = findViewById(R.id.textViewTime2);
        btnTime1.solveTimes = solveTimes1;
        btnTime2.solveTimes = solveTimes2;
        btnTime1.solvePenalties = solvePenalties1;
        btnTime2.solvePenalties = solvePenalties2;
        btnTime1.mainActivity = this;
        btnTime2.mainActivity = this;
        btnTime1.btnStats = btnStats1;
        btnTime1.layoutStats = layoutStats1;
        btnTime2.btnStats = btnStats2;
        btnTime2.layoutStats = layoutStats2;
        btnTime1.averages = averages1;
        btnTime2.averages = averages2;

        Button btnPuzzles = findViewById(R.id.btnPuzzles);
        btnPuzzles.setOnClickListener(view -> {
            if (!btnTime1.isStarted && !btnTime2.isStarted) {
                hideStats(btnStats1, layoutStats1);
                hideStats(btnStats2, layoutStats2);

                createPuzzlePopupWindow();
            }
        });

        Button btnPenalties = findViewById(R.id.btnPenalties);
        btnPenalties.setOnClickListener(view -> {
            if (btnTime1.isProcessed && btnTime2.isProcessed && !btnTime1.isStarted && !btnTime2.isStarted) {
                hideStats(btnStats1, layoutStats1);
                hideStats(btnStats2, layoutStats2);

                createPenaltyPopupWindow();
            }
        });

        btnStats1.setOnClickListener(view -> {
            if (!btnTime1.isStarted) {
                setAverage(findViewById(R.id.statList1), solvePenalties1, averages1);
                showStats(btnStats1, layoutStats1);
            }
        });

        btnStats2.setOnClickListener(view -> {
            if (!btnTime2.isStarted) {
                setAverage(findViewById(R.id.statList2), solvePenalties2, averages2);
                showStats(btnStats2, layoutStats2);
            }
        });

        Button btnSolves1 = findViewById(R.id.btnScrambles1);
        btnSolves1.setOnClickListener(view -> {
            if (!btnTime2.isStarted && btnTime1.isProcessed && btnTime2.isProcessed) {
                goToSolveListActivity();
            }
        });

        Button btnSolves2 = findViewById(R.id.btnScrambles2);
        btnSolves2.setOnClickListener(view -> {
            if (!btnTime1.isStarted && btnTime1.isProcessed && btnTime2.isProcessed) {
                goToSolveListActivity();
            }
        });
    }

    private void goToSolveListActivity() {
        Intent intent = new Intent(this, ScramblesListActivity.class);
        intent.putExtra("scrambles", new ArrayList<>(scrambles.subList(0, solveTimes1.size())));
        intent.putExtra("fontSize", Integer.parseInt(puzzleProperties[1]));
        intent.putExtra("solveTimes1", solveTimes1);
        intent.putExtra("solveTimes2", solveTimes2);
        intent.putExtra("solvePenalties1", solvePenalties1);
        intent.putExtra("solvePenalties2", solvePenalties2);
        startActivity(intent);
        hideStats(btnStats1, layoutStats1);
        hideStats(btnStats2, layoutStats2);
    }

    private void createPenaltyPopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_penalty, mainLayout, false);

        new MyPopupWindow(popupView, this, R.layout.popup_window_penalty);
    }

    private void createPuzzlePopupWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_puzzles, mainLayout, false);

        new MyPopupWindow(popupView, this, R.layout.popup_window_puzzles);
    }

    private void showStats(Button btnStats, LinearLayout layoutStats) {
        layoutStats.setVisibility(View.VISIBLE);

        btnStats.setVisibility(View.GONE);
    }

    protected void hideStats(Button btnStats, LinearLayout layoutStats) {
        layoutStats.setVisibility(View.GONE);

        btnStats.setVisibility(View.VISIBLE);
    }

    private void resetAverages() {
        String[] avgStrList = {"avg5: ", "avg12: ", "avg25: ", "avg50: ", "avg100: "};
        GridLayout glStatList1 = findViewById(R.id.statList1);
        GridLayout glStatList2 = findViewById(R.id.statList2);
        TextView textViewAvg;
        for (int i = 0; i < 5; i++) {
            textViewAvg = (TextView) glStatList1.getChildAt(i);
            textViewAvg.setText(avgStrList[i]);

            textViewAvg = (TextView) glStatList2.getChildAt(i);
            textViewAvg.setText(avgStrList[i]);
        }
        String solves = "solves: 0/0";
        textViewAvg = (TextView) glStatList1.getChildAt(5);
        textViewAvg.setText(solves);

        textViewAvg = (TextView) glStatList2.getChildAt(5);
        textViewAvg.setText(solves);
    }

    private void setAverage(GridLayout glAvgList, ArrayList<Integer> solveTimes, double[] averages) {
        int[] avgNumList = {5, 12, 25, 50, 100};
        String[] avgStrList = {"avg5: ", "avg12: ", "avg25: ", "avg50: ", "avg100: "};

        TextView textViewStats;
        String newStatsStr;
        for (int i = 0; i < 5; i++) {
            if (solveTimes.size() >= avgNumList[i]) {
                // if number of solves enough to set this average
                textViewStats = (TextView) glAvgList.getChildAt(i);
                if (averages[i] == 0) {
                    // if average == 0 -> DNF
                    newStatsStr = avgStrList[i] + "DNF";
                } else {
                    newStatsStr = avgStrList[i] + TimeButton.getFormattedTime(averages[i]);
                }
                textViewStats.setText(newStatsStr);
            }
            else {
                break;
            }
        }
        textViewStats = (TextView) glAvgList.getChildAt(5);
        int withoutDnfCount = 0;
        for (int solve: solveTimes) {
            if (solve != 3) {
                withoutDnfCount += 1;
            }
        }

        newStatsStr = "solves: " + withoutDnfCount + "/" + solveTimes.size();
        textViewStats.setText(newStatsStr);
    }

    protected void recalculateScore() {
        if (winsList.size() > 0) {
            winsList.remove(winsList.size() - 1);

            setScore(solveTimes1.get(solveTimes1.size() - 1), solveTimes2.get(solveTimes2.size() - 1),
                    solvePenalties1.get(solvePenalties1.size() - 1), solvePenalties2.get(solvePenalties2.size() - 1));
        }
    }

    private void setScore(int time1, int time2, int penaltyId1, int penaltyId2) {
        if (solveTimes1.size() > 0) {
            if (penaltyId1 == 3 || penaltyId2 == 3) {
                // if any cuber got DNF
                if (penaltyId1 == 3 && penaltyId2 != 3) {
                    // if only cuber1 got DNF, then cuber2 won
                    winsList.add(2);
                } else if (penaltyId1 != 3) {
                    // if only cuber2 got DNF, then cuber1 won
                    winsList.add(1);
                } else {
                    // both cubers got DNF, it is a draw
                    winsList.add(0);
                }
            } else if (time1 < time2) {
                winsList.add(1);
            } else if (time1 > time2) {
                winsList.add(2);
            } else {
                winsList.add(0);
            }
            textViewScore1.setText(HtmlCompat.fromHtml(getScoreHtmlString(
                                     Collections.frequency(winsList, 1),
                                     Collections.frequency(winsList, 2)),
                                     HtmlCompat.FROM_HTML_MODE_LEGACY));
            textViewScore2.setText(HtmlCompat.fromHtml(getScoreHtmlString(
                                     Collections.frequency(winsList, 2),
                                     Collections.frequency(winsList, 1)),
                                     HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            textViewScore1.setText(HtmlCompat.fromHtml(getScoreHtmlString(0, 0), HtmlCompat.FROM_HTML_MODE_LEGACY));
            textViewScore2.setText(HtmlCompat.fromHtml(getScoreHtmlString(0, 0), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    protected void setPenalty(ArrayList<Integer> solveTimes, ArrayList<Integer> solvePenalties, TextView textView, int penaltyId) {
        // set penalty on last solve (penaltyId == 1 -> OK; penaltyId == 2 -> +2; penaltyId == 3 -> DNF)
        int numberOfSolves = solveTimes.size();
        if (numberOfSolves > 0 && solvePenalties.get(numberOfSolves - 1) != penaltyId) {
            int currTime = solveTimes.get(numberOfSolves - 1);
            if (solvePenalties.get(numberOfSolves - 1) == 2) {
                currTime -= 2000;    // -2 seconds
            }
            if (penaltyId == 2) {
                currTime += 2000;    // +2 seconds
            }

            solveTimes.remove(numberOfSolves - 1);
            solvePenalties.remove(numberOfSolves - 1);
            solveTimes.add(currTime);
            solvePenalties.add(penaltyId);
            String newTimeStr;
            if (penaltyId == 1) {
                newTimeStr = TimeButton.getFormattedTime(currTime);
            } else if (penaltyId == 2) {
                newTimeStr = TimeButton.getFormattedTime(currTime) + "+";
            } else {
                newTimeStr = getString(R.string.dnf);
            }
            textView.setText(newTimeStr);
        }
    }

    protected void calculateAverage(ArrayList<Integer> solveTimes, ArrayList<Integer> solvePenalties, double[] averages) {
        int[] avgNumList = {5, 12, 25, 50, 100};
        int[] thrownList = {1, 1, 2, 3, 5};
        for (int i = 0; i < 5; i++) {
            if (solveTimes.size() >= avgNumList[i]) {
                averages[i] = getAvg(solveTimes, solvePenalties, avgNumList[i], thrownList[i]);
            }
            else {
                break;
            }
        }
    }

    private double getAvg(ArrayList<Integer> solveTimes, ArrayList<Integer> solvePenalties, int avgOf, int thrown) {
        int numberOfSolves = solveTimes.size();
        ArrayList<Integer> solveTimesNeed = new ArrayList<>();    // last <avgOf> solves
        for (int i = numberOfSolves - avgOf; i < numberOfSolves; i++) {
            // Add only non-dnf solves in solveTimesNeed
            if (solvePenalties.get(i) != 3) {
                solveTimesNeed.add(solveTimes.get(i));
            }
        }
        if (solveTimesNeed.size() < avgOf - thrown) {
            return 0;
        } else {
            // sort times to make average counting easier
            sortSolveTimes(solveTimesNeed, 0, solveTimesNeed.size() - 1);

            // throw a given number of best solves
            solveTimesNeed.subList(0, thrown).clear();

            // throw a given number of worst solves
            solveTimesNeed.subList(avgOf - thrown*2, solveTimesNeed.size()).clear();

            return getMean(solveTimesNeed);
        }
    }

    private double getMean(ArrayList<Integer> solveTimes) {
        int sumTimes = 0;
        int count = 0;

        for (int time: solveTimes) {
            sumTimes += time - time % 10;
            count += 1;
        }

        return (double) (sumTimes - sumTimes % 10) / count;
    }

    private static void sortSolveTimes(ArrayList<Integer> solveTimes, int low, int high) {
        if (solveTimes.size() == 0) {
            return;
        }
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        int main = solveTimes.get(middle);

        int i = low, j = high;
        while (i <= j) {
            while (solveTimes.get(i) < main) {
                i++;
            }
            while (solveTimes.get(j) > main) {
                j--;
            }
            if (i <= j) {
                Collections.swap(solveTimes, i, j);
                i++;
                j--;
            }
        }

        if (low < j)
            sortSolveTimes(solveTimes, low, j);
        if (high > i)
            sortSolveTimes(solveTimes, i, high);
    }

    protected void resetAll() {
        // reset all stats
        scrambles.clear();
        processNewScramble();

        solveTimes1.clear();
        solveTimes2.clear();
        solvePenalties1.clear();
        solvePenalties2.clear();

        winsList.clear();

        btnTime1.isProcessed = true;
        btnTime2.isProcessed = true;
        btnTime1.textViewTime.setText(getString(R.string.start_time));
        btnTime2.textViewTime.setText(getString(R.string.start_time));

        for (int i = 0; i < 5; i++) {
            averages1[i] = 0;
            averages2[i] = 0;
        }
        resetAverages();

        setScore(0, 0, 1, 1);
    }

    private void setScrambleFontSize(int fontSize) {
        textViewScramble1.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        textViewScramble2.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    }

    protected boolean isBothSolved() {
        // did both cubers solve cubes?
        return !btnTime1.isProcessed && !btnTime2.isProcessed;
    }

    protected void processTimes() {
        setScore(btnTime1.currTime, btnTime2.currTime,1, 1);
        btnTime1.isProcessed = true;
        btnTime2.isProcessed = true;
    }

    protected void processNewScramble() {
        // generate and set new scramble in another thread
        Thread thread = new Thread(() -> {
            if (scrambles.isEmpty()) {
                // if scramble list is empty, generate first scramble
                runOnUiThread(() -> {
                    // set text "Generating scramble..."
                    setScrambleFontSize(25);
                    setNewScramble(getString(R.string.generating_scramble));
                });
                // generate and add new scramble to scramble list
                scrambles.add(PuzzleRegistry.valueOf(puzzleProperties[0]).getScrambler().generateScrambles(1)[0]);
            }
            runOnUiThread(() -> {
                // set last scramble from scramble list
                setNewScramble(scrambles.get(scrambles.size() - 1));
                setScrambleFontSize(Integer.parseInt(puzzleProperties[1]));
            });
            // generate and add new scramble to scramble list to avoid waiting when setting up the next scramble
            scrambles.add(PuzzleRegistry.valueOf(puzzleProperties[0]).getScrambler().generateScrambles(1)[0]);
        });
        thread.start();
    }

    private String getScoreHtmlString(int score1, int score2) {
        return "<font color='blue'>" + score1 + "</font> : " + score2;
    }

    private void setNewScramble(String scramble) {
        textViewScramble1.setText(scramble);
        textViewScramble2.setText(scramble);
    }
}