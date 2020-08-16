package com.example.firebasedemo.modules;

import android.app.Activity;
import android.graphics.Color;

import com.example.firebasedemo.R;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;

public class PreBuiltSuperToast {
    public static SuperToast progressBarToast(Activity activity, String msg) {
        return SuperActivityToast.create(activity, new Style(), Style.TYPE_PROGRESS_CIRCLE)
                .setProgressBarColor(Color.WHITE)
                .setDuration(Style.DURATION_VERY_LONG)
                .setText(msg)
                .setFrame(Style.FRAME_STANDARD)
                .setColor(activity.getColor(R.color.color_main_Color))
                .setAnimations(Style.ANIMATIONS_POP);
    }

    public static SuperToast snackBarToast(Activity activity, String msg) {
        return SuperActivityToast.create(activity, new Style(), Style.TYPE_STANDARD)
                .setProgressBarColor(Color.WHITE)
                .setDuration(Style.DURATION_MEDIUM)
                .setText(msg)
                .setFrame(Style.FRAME_STANDARD)
                .setColor(activity.getColor(R.color.color_main_Color))
                .setAnimations(Style.ANIMATIONS_POP);
    }
}
