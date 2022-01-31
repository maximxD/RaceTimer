package com.maximxd.racetimer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimeButton extends androidx.appcompat.widget.AppCompatButton {

    private long startTime;
    private Timer timer;
    private int pressCounter = 0;    // need to get right delay before start
    private final int defaultColor = this.getCurrentTextColor();

    protected boolean isStarted = false;
    protected int currTime;
    protected boolean isProcessed = true;
    protected Button btnStats;
    protected LinearLayout layoutStats;

    protected TextView textViewTime;
    protected ArrayList<Integer> solveTimes = new ArrayList<>();
    protected ArrayList<Integer> solvePenalties = new ArrayList<>();
    protected double[] averages;
    protected MainActivity mainActivity;

    public TimeButton(Context context) {
        super(context);
    }

    public TimeButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static String getFormattedTime(int time) {
        String strTime;
        double floatTime = (double) time / 1000;

        if (floatTime < 60) {
            strTime = String.format(Locale.US, "%.3f", floatTime);
        } else {
            String formattedSeconds = String.format(Locale.US, ":%06.3f", floatTime % 60);
            if (floatTime < 3600) {
                strTime = (int) floatTime / 60 + formattedSeconds;
            } else {
                strTime = (int) floatTime / 3600 + String.format(Locale.US,":%02d", (int) (floatTime / 60) % 60) + formattedSeconds;
            }
        }
        return strTime;
    }

    public static String getFormattedTime(double time) {
        String strTime;
        time /= 1000;

        if (time < 60) {
            strTime = String.format(Locale.US, "%.2f", time);
        } else {
            String formattedSeconds = String.format(Locale.US, ":%05.2f", time % 60);
            if (time < 3600) {
                strTime = (int) time / 60 + formattedSeconds;
            } else {
                strTime = (int) time / 3600 + String.format(Locale.US,":%02d", (int) (time / 60) % 60) + formattedSeconds;
            }
        }
        return strTime;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onPressTimer();
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            onReleaseTimer();
            return true;
        }
        performClick(); // performClick is empty, this line here just to avoid warning
        return false;
    }

    private void onPressTimer() {
        if (layoutStats.getVisibility() == View.VISIBLE) {
            // if stats are shown, hide it
            mainActivity.hideStats(btnStats, layoutStats);
        } else if (isProcessed) {
            // if times were already processed (both cubers solved cubes), allow to start/stop the timer.
            if (isStarted) {
                // if the timer was already started, it means that the timer should be stopped.
                timer.cancel();
                solveTimes.add(currTime);
                solvePenalties.add(1);
                mainActivity.calculateAverage(solveTimes, solvePenalties, averages);
                isProcessed = false;
                if (mainActivity.isBothSolved()) {
                    mainActivity.processNewScramble();
                    mainActivity.processTimes();
                }
                System.gc();
                isStarted = !isStarted;
            } else {
                // if the timer is not started, prepare it to start
                textViewTime.setTextColor(Color.RED);
                currTime = 0;
                Timer timer = new Timer();
                int localCounter = pressCounter;    // copy of pressCounter to compare with it later
                TimerTask timerTask = new TimerTask() {
                    public void run() {
                        isStillPressed(localCounter);
                    }
                };
                timer.schedule(timerTask, 350);
            }
        }
    }

    private void isStillPressed(int counter) {
        if (pressCounter == counter) {
            // is button still pressed and was not released during delay
            textViewTime.setTextColor(Color.GREEN);
            isStarted = !isStarted;
        }
    }

    private void onReleaseTimer() {
        pressCounter++;  // change pressCounter to indicate that the button has been released
        textViewTime.setTextColor(defaultColor);
        if (isStarted && isProcessed) {
            // if the timer was already processed (both cubers solved cubes), allow to start/stop the timer.
            // if the timer is ready to start, start it.
            startTime = System.currentTimeMillis();
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    currTime = (int) (System.currentTimeMillis() - startTime);
                    mainActivity.runOnUiThread(() -> textViewTime.setText(getFormattedTime(currTime)));
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 1);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
