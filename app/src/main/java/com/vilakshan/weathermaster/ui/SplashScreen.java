package com.vilakshan.weathermaster.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.utils.Utils;

public class SplashScreen extends AppCompatActivity {


    AnimationDrawable mSunAnimation;
    AnimationDrawable mCloudAnimation;
    AnimationDrawable mFogAnimation;
    AnimationDrawable mRainAnimation;
    AnimationDrawable mSnowAnimation;
    AnimationDrawable mThunderAnimation;

    ValueAnimator mColorAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        TextView appName = (TextView) findViewById(R.id.txtAppName);

        ImageView imgLogo = (ImageView) findViewById(R.id.img_Logo);

        ImageView imgSun = (ImageView) findViewById(R.id.img_Sun);
        ImageView imgCloud = (ImageView) findViewById(R.id.img_Cloud);
        ImageView imgFog = (ImageView) findViewById(R.id.img_Fog);
        ImageView imgRain = (ImageView) findViewById(R.id.img_Rain);
        ImageView imgSnow = (ImageView) findViewById(R.id.img_Snow);
        ImageView imgThunder = (ImageView) findViewById(R.id.img_Thunder);

        imgSun.setImageResource(R.drawable.animation_weather);
        imgCloud.setImageResource(R.drawable.animation_cloud);
        imgFog.setImageResource(R.drawable.animation_fog);
        imgRain.setImageResource(R.drawable.animation_rain);
        imgSnow.setImageResource(R.drawable.animation_snow);
        imgThunder.setImageResource(R.drawable.animation_thunder);

        imgLogo.setImageResource(R.mipmap.ic_launcher);
        imgLogo.setScaleType(ImageView.ScaleType.CENTER);


        mSunAnimation = (AnimationDrawable) imgSun.getDrawable();
        mCloudAnimation = (AnimationDrawable) imgCloud.getDrawable();
        mFogAnimation = (AnimationDrawable) imgFog.getDrawable();
        mRainAnimation = (AnimationDrawable) imgRain.getDrawable();
        mSnowAnimation = (AnimationDrawable) imgSnow.getDrawable();
        mThunderAnimation = (AnimationDrawable) imgThunder.getDrawable();

        mColorAnimation = ObjectAnimator.ofInt(appName, "textColor", Constants.RED, Constants.BLUE);
        mColorAnimation.setDuration(Constants.TEXT_COLOR_CHANGE_DURATION);
        mColorAnimation.setEvaluator(new ArgbEvaluator());
        mColorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        mColorAnimation.setRepeatMode(ValueAnimator.REVERSE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                String locationSetting = Utils.getPreferredLocation(SplashScreen.this);
                Intent i;
                if (locationSetting.trim().equals("")) {
                    i = new Intent(SplashScreen.this, SettingsActivity.class);
                    //Setting flag to represent source as splash screen activity
                    i.putExtra(Constants.SPLASH_SCREEN_FLAG, Constants.SPLASH_SCREEN_FLAG_VALUE);
                } else {
                    i = new Intent(SplashScreen.this, MainActivity.class);
                }
                startActivity(i);
                // close this activity
                finish();
            }
        }, Constants.SPLASH_TIME_OUT);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //If window is currently active and has focus
            mColorAnimation.start();

            mSunAnimation.start();
            mCloudAnimation.start();
            mFogAnimation.start();
            mRainAnimation.start();
            mSnowAnimation.start();
            mThunderAnimation.start();
        } else {
            //If window is currently inactive and do not have focus
            mColorAnimation.end();

            mSunAnimation.stop();
            mCloudAnimation.stop();
            mFogAnimation.stop();
            mRainAnimation.stop();
            mSnowAnimation.stop();
            mThunderAnimation.stop();
        }
    }
}
