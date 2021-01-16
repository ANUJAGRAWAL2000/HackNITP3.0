package com.example.chatapp.password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private LinearLayout llReset,llMessage;
    private Button resetPasssword,retry,close;
    private TextView Message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail=(TextInputEditText)findViewById(R.id.etEmail);
        llReset=(LinearLayout)findViewById(R.id.llReset);
        llMessage=(LinearLayout)findViewById(R.id.llMessage);
        resetPasssword=(Button)findViewById(R.id.Reset);
        retry=(Button)findViewById(R.id.retry);
        close=(Button)findViewById(R.id.close);
        Message=(TextView)findViewById(R.id.Message);
    }

    public void resetPasssword(View V)
    {

        String Email=etEmail.getText().toString().trim();
        if(Email.equals(""))
        {
            etEmail.setError(getString(R.string.etEmail));
        }
        else
        {
            FirebaseAuth mAuth=FirebaseAuth.getInstance();
            FirebaseUser firebaseUser=mAuth.getCurrentUser();

            mAuth.sendPasswordResetEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    llReset.setVisibility(View.GONE);
                    llMessage.setVisibility(View.VISIBLE);
                    if(task.isSuccessful())
                    {
                        Message.setText(getString(R.string.resetLink,Email));
                        new CountDownTimer(60000,1000) {

                            @Override
                            public void onTick(long l) {
                                retry.setText(getString(R.string.Resend, String.valueOf(l / 1000)));
                                retry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                retry.setText(R.string.Retry);
                                retry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        llReset.setVisibility(View.VISIBLE);
                                        llMessage.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }.start();
                    }
                    else
                    {
                        Toast.makeText(ResetPasswordActivity.this,"Reset Link not Sent : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                        retry.setText(R.string.Retry);
                        retry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                llReset.setVisibility(View.VISIBLE);
                                llMessage.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }
    }
    public void btnClose(View V)
    {
        finish();
    }
}