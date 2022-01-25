package com.maximxd.racetimer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimeButton extends androidx.appcompat.widget.AppCompatButton {

    private long start_time;
    private Timer timer;
    private int pressed_counter = 0;    // need to get right delay before start
    private final int default_color = this.getCurrentTextColor();
    protected boolean is_started = false;
    protected int curr_time;
    protected boolean is_processed = true;
    protected TextView text_view_time;
    protected ArrayList<int[]> solve_times = new ArrayList<>();
    protected MainActivity main_activity;

    public TimeButton(Context context) {
        super(context);
    }

    public TimeButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static String get_formatted_time(int time) {
        String str_time;
        float float_time = (float) time / 1000;
        if (float_time < 60) {
            str_time = String.format(Locale.US, "%.3f", float_time);
        } else {
            String formatted_seconds = String.format(Locale.US, ":%06.3f", float_time % 60);
            if (float_time < 3600) {
                str_time = (int) float_time / 60 + formatted_seconds;
            } else {
                str_time = (int) float_time / 3600 + String.format(Locale.US,":%02d", (int) (float_time / 60) % 60) + formatted_seconds;
            }
        }
        return str_time;
    }

    public static String get_formatted_time(float time) {
        String str_time;
        time /= 1000;
        if (time < 60) {
            str_time = String.format(Locale.US, "%.2f", time);
        } else {
            String formatted_seconds = String.format(Locale.US, ":%05.2f", time % 60);
            if (time < 3600) {
                str_time = (int) time / 60 + formatted_seconds;
            } else {
                str_time = (int) time / 3600 + String.format(Locale.US,":%02d", (int) (time / 60) % 60) + formatted_seconds;
            }
        }
        return str_time;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            on_press_timer();
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            on_release_timer();
            return true;
        }
        performClick(); // performClick is empty, this line here just to avoid warning
        return false;
    }

    private void on_press_timer() {
        if (is_processed) {
            // if times were already processed (both cubers solved cubes), allow to start/stop the timer.
            if (is_started) {
                // if the timer was already started, it means that the timer should be stopped.
                timer.cancel();
                solve_times.add(new int[] {curr_time, 1});
                main_activity.calculate_and_set_averages();
                is_processed = false;
                if (main_activity.isBothSolved()) {
                    main_activity.process_new_scramble();
                    main_activity.processTimes();
                }
                System.gc();
                is_started = !is_started;
            } else {
                // if the timer is not started, prepare it to start
                text_view_time.setTextColor(Color.RED);
                curr_time = 0;
                Timer timer = new Timer();
                long local_counter = pressed_counter;    // copy of pressed_counter to it later
                TimerTask timer_task = new TimerTask() {
                    public void run() {
                        is_still_pressed(local_counter);
                    }
                };
                timer.schedule(timer_task, 350);
            }
        }
    }

    private void is_still_pressed(long counter) {
        if (pressed_counter == counter) {
            // is button still pressed and was not released during delay
            text_view_time.setTextColor(Color.GREEN);
            is_started = !is_started;
        }
    }

    private void on_release_timer() {
        pressed_counter++;  // changing pressed_counter to indicate that the button has been released
        text_view_time.setTextColor(default_color);
        if (is_started && is_processed) {
            // if the timer was already processed (both cubers solved cubes), allow to start/stop the timer.
            // if the timer is ready to start, start it.
            start_time = System.currentTimeMillis();
            timer = new Timer();
            TimerTask timer_task = new TimerTask() {
                public void run() {
                    curr_time = (int) (System.currentTimeMillis() - start_time);
                    main_activity.runOnUiThread(() -> text_view_time.setText(get_formatted_time(curr_time)));
                }
            };
            timer.scheduleAtFixedRate(timer_task, 0, 1);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
