package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.Login.LoginActivity;

public class SpalshActivity extends AppCompatActivity {

    private ImageView image;
    private TextView app;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);

        image=(ImageView)findViewById(R.id.ImageFriend);
        app=(TextView)findViewById(R.id.AppName);

        animation= AnimationUtils.loadAnimation(this,R.anim.splash);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SpalshActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        image.startAnimation(animation);
        app.startAnimation(animation);
    }
}