package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Common.Internet;
import com.example.chatapp.R;

public class MessageActivity extends AppCompatActivity {

    private TextView Message;
    private View InternetLoad;
    private ConnectivityManager.NetworkCallback networkCallback;
    //This will tell the system that our intent is to receive notifications about network changes whenever they occur.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Message=(TextView)findViewById(R.id.message);
        InternetLoad=findViewById(R.id.progressBar2);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            networkCallback=new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    Message.setText(getString(R.string.no_internet));
                }
            };

            ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),networkCallback);
        }
    }

    public void retryInternet(View V)
    {
        InternetLoad.setVisibility(View.VISIBLE);
        if(Internet.connectionAvailable(this))
        {
            finish();
        }
        else
        {
           new android.os.Handler().postDelayed(new Runnable() {
               @Override
               public void run() {
                   InternetLoad.setVisibility(View.GONE);
               }
           },1000);
        }
    }

    public void btnClose(View V)
    {
        finishAffinity();
    }


}