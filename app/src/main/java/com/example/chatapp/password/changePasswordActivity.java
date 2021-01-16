package com.example.chatapp.password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.Login.LoginActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.chatapp.Common.Node.Email;

public class changePasswordActivity extends AppCompatActivity {

    private TextInputEditText etPassword,etConfPass, etOldPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etOldPassword=findViewById(R.id.etOldPassword);
        etPassword=findViewById(R.id.etPassword);
        etConfPass=findViewById(R.id.etConfPassword);

    }

    public void btnChangePass(View V)
    {
        String OldPassword = etOldPassword.getText().toString().trim();
        String Password=etPassword.getText().toString().trim();
        String ConfPassword=etConfPass.getText().toString().trim();

        if(OldPassword.equals(""))
            etOldPassword.setError(getString(R.string.etPassword));
        else if(Password.equals(""))
            etPassword.setError(getString(R.string.etPassword));
        else if(ConfPassword.equals(""))
            etConfPass.setError(getString(R.string.etConfPassword));
        else if(!Password.equals(ConfPassword))
            etConfPass.setError(getString(R.string.PasswordMismatch));
        else
        {
            FirebaseAuth mAuth=FirebaseAuth.getInstance();
            FirebaseUser firebaseUser=mAuth.getCurrentUser();

            if(firebaseUser!=null) {

                final String email = firebaseUser.getEmail();
                AuthCredential credential = EmailAuthProvider.getCredential(email, OldPassword);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseUser.updatePassword(Password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(changePasswordActivity.this, "Password Update Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(changePasswordActivity.this, "Password Not Updated %1$s" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {

                            Toast.makeText(changePasswordActivity.this, "Wrong Old Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    });

        }}
    }
}
