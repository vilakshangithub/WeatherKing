package com.vilakshan.weathermaster.views;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.vilakshan.weathermaster.R;

/**
 * <p>
 * Created by VilakshanSaxena on 12/12/2015.
 * </p>
 */
public class CustomProgressDialog extends ProgressDialog {
    private static final int TEXT_COLOR_CHANGE_DURATION = 1000;
    private static final int RED = 0xffFF8080;
    private static final int BLUE = 0xff8080FF;

    private AnimationDrawable mWeatherAnimation;
    private ValueAnimator mColorAnimation;
    private TextView mTvMessage;

    public CustomProgressDialog(Context context) {
        super(context);
    }

    @Override
    public void setMessage(CharSequence message) {
        if (mTvMessage != null) {
            mTvMessage.setText(message);
        }
        super.setMessage(message);
    }

    @Override
    public void show() {
        super.show();
        mColorAnimation.start();
        mWeatherAnimation.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mColorAnimation.cancel();
        mWeatherAnimation.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_progress_dialog);

        mTvMessage = (TextView) findViewById(R.id.loadingMessage);
        TextView appName = (TextView) findViewById(R.id.loadingHeader);
        ImageView imgLoading = (ImageView) findViewById(R.id.loadingImage);

        imgLoading.setImageResource(R.drawable.animation_weather);
        mWeatherAnimation = (AnimationDrawable) imgLoading.getDrawable();

        mColorAnimation = ObjectAnimator.ofInt(appName, "textColor", RED, BLUE);
        mColorAnimation.setDuration(TEXT_COLOR_CHANGE_DURATION);
        mColorAnimation.setEvaluator(new ArgbEvaluator());
        mColorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        mColorAnimation.setRepeatMode(ValueAnimator.REVERSE);
    }


}
