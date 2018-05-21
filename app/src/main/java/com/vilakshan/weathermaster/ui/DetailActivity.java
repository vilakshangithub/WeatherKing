package com.vilakshan.weathermaster.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.utils.Utils;

public class DetailActivity extends AppCompatActivity {

    private DetailActivityFragment mFragment;
    private boolean mIsMetric;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(this, SettingsActivity.class);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(intent);
        } else if (id == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isMetric = Utils.isMetric(this);
        if (isMetric != mIsMetric) {
            mFragment.OnTempUnitChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mIsMetric = Utils.isMetric(this);

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
            args.putBoolean(DetailActivityFragment.DETAIL_FINAL_ANIMATION, true);
            args.putString(MainActivity.TRANSITION_NAME_TAG, getIntent().getStringExtra(
                    MainActivity.TRANSITION_NAME_TAG));
            mFragment = new DetailActivityFragment();
            mFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, mFragment)
                    .commit();
        }

        //App is in animation mode
        supportPostponeEnterTransition();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
