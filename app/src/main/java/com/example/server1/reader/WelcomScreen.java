package com.example.server1.reader;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class WelcomScreen extends AppCompatActivity {
    private static int timeout=2000;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom_screen);
        imageView=(ImageView)findViewById(R.id.welcomeimg);
        Animation animation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade);
        imageView.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(WelcomScreen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },timeout);
    }
}
