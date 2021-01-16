package com.example.chatapp.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.chatapp.Common.Internet;
import com.example.chatapp.MainActivity;
import com.example.chatapp.Profile.ProfileActivity;
import com.example.chatapp.R;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.SignUp.SignUpActivity;
import com.example.chatapp.password.ResetPasswordActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail,etPassword;
    private CardView loginCardView;
    private LinearLayout signUpLayout;
    private String Email,Password;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail=(TextInputEditText)findViewById(R.id.etEmail);
        etPassword=(TextInputEditText)findViewById(R.id.etPassword);
        progressBar=findViewById(R.id.pbFriendRequest);
        loginCardView=(CardView) findViewById(R.id.loginCardView);
        signUpLayout=(LinearLayout) findViewById(R.id.signUpLayout);

    }

    public void SignUp(View V)
    {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    public void LoginClk(View V)
    {
        Email=etEmail.getText().toString().trim();
        Password=etPassword.getText().toString().trim();


        if(Email.equals(""))
        {
            etEmail.setError(getString(R.string.etEmail));
        }
        else if(Password.equals(""))
        {
            etPassword.setError(getString(R.string.etPassword));
        }
        else
        {
            if(Internet.connectionAvailable(LoginActivity.this))
            {
                progressBar.setVisibility(View.VISIBLE);
                loginCardView.setVisibility(View.INVISIBLE);
                signUpLayout.setVisibility(View.INVISIBLE);

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            if(mAuth.getCurrentUser().isEmailVerified()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                            else
                            {
                                progressBar.setVisibility(View.GONE);
                                loginCardView.setVisibility(View.VISIBLE);
                                signUpLayout.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this,"Please Verify ur Email Id",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            progressBar.setVisibility(View.GONE);
                            loginCardView.setVisibility(View.VISIBLE);
                            signUpLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Login Failed:" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else
            {
                startActivity(new Intent(LoginActivity.this, MessageActivity.class));
            }
        }
    }

    public void resetPassword(View V)
    {
        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    Internet.UpdateDeviceToken(instanceIdResult.getToken(),LoginActivity.this);
                }
            });
            if(firebaseUser.isEmailVerified()) {
                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                finish();
            }
        }
    }



}