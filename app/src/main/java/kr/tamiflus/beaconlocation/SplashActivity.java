package kr.tamiflus.beaconlocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.logging.Handler;

/**
 * Created by juwoong on 16. 1. 24..
 */
public class SplashActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        Intent intent;

        SharedPreferences prefs = getSharedPreferences("beaconSetting", MODE_PRIVATE);

        if(prefs.getBoolean("isLogin", false) == false || prefs.getString("token", null) == null) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}