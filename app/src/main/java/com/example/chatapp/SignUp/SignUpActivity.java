package com.example.chatapp.SignUp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatapp.Common.Node;
import com.example.chatapp.Login.LoginActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName,etEmail,etPassword,etConfPass;
    private String Email,Name,Password,ConfPassword;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseStorage mStorage;
    private ImageView ivProfile;
    private Uri localFileUri,ServerFileUri;

    public void pickImage(View V)
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        }
        else
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101)
        {
            if(resultCode==RESULT_OK)
            {
              localFileUri=data.getData();
              ivProfile.setImageURI(localFileUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updateNameAndPhoto()
    {
        String strFile = firebaseUser.getUid()+".jpg";
        final StorageReference FileRef = mStorage.getInstance().getReference().child("Images/"+strFile);
        FileRef.putFile(localFileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    FileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ServerFileUri=uri;

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().
                                    setDisplayName(etName.getText().toString().trim())
                                    .setPhotoUri(ServerFileUri)
                                    .build();

                            firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        String UserID=firebaseUser.getUid();
                                        databaseReference= FirebaseDatabase.getInstance().getReference().child(Node.Users);
                                        HashMap<String,String> hashMap=new HashMap<>();
                                        hashMap.put(Node.Name,etName.getText().toString().trim());
                                        hashMap.put(Node.Email,etEmail.getText().toString().trim());
                                        hashMap.put(Node.Photo,ServerFileUri.getPath());
                                        hashMap.put(Node.Online,"true");

                                        databaseReference.child(UserID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                                                    if(firebaseUser.isEmailVerified()) {
                                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(SignUpActivity.this,"Please Verify ur Email Id",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                else
                                                {
                                                    Toast.makeText(SignUpActivity.this,"User Not Created",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(SignUpActivity.this,"Failed to Update : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        }
                    });
                }
            }
        });
    }

    public void updateNameOnly()
    {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().
                setDisplayName(etName.getText().toString().trim()).build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    String UserID=firebaseUser.getUid();
                    databaseReference= FirebaseDatabase.getInstance().getReference().child(Node.Users);
                    HashMap<String,String> hashMap=new HashMap<>();
                    hashMap.put(Node.Name,etName.getText().toString().trim());
                    hashMap.put(Node.Email,etEmail.getText().toString().trim());
                    hashMap.put(Node.Photo,"");
                    hashMap.put(Node.Online,"true");

                    databaseReference.child(UserID).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                                if(firebaseUser.isEmailVerified()) {
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                }
                                else
                                {
                                    Toast.makeText(SignUpActivity.this,"Please Verify ur Email Id",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(SignUpActivity.this,"User Not Created",Toast.LENGTH_SHORT).show();
                            }
                            }
                    });
                }
                else
                {
                    Toast.makeText(SignUpActivity.this,"Failed to Update : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName=(TextInputEditText)findViewById(R.id.etName);
        etEmail=(TextInputEditText)findViewById(R.id.etEmail);
        etPassword=(TextInputEditText)findViewById(R.id.etPassword);
        etConfPass=(TextInputEditText)findViewById(R.id.etConfPassword);
        ivProfile=findViewById(R.id.Profile);
    }

    public void SignUpClk(View V)
    {
        Email=etEmail.getText().toString().trim();
        Name=etName.getText().toString().trim();
        Password=etPassword.getText().toString().trim();
        ConfPassword=etConfPass.getText().toString().trim();


        if(Email.equals(""))
        {
            etEmail.setError(getString(R.string.etEmail));
        }
        else if(Name.equals(""))
        {
            etName.setError(getString(R.string.etName));
        }
        else if(Password.equals(""))
        {
            etPassword.setError(getString(R.string.etPassword));
        }
        else if(ConfPassword.equals(""))
        {
            etConfPass.setError(getString(R.string.etConfPassword));
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
        {
           etEmail.setError(getString(R.string.CorrectEmail));
        }
        else if(!Password.equals(ConfPassword))
        {
            etConfPass.setError(getString(R.string.PasswordMismatch));
        }
        else
            {
                FirebaseAuth mAuth=FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            firebaseUser=mAuth.getCurrentUser();
                            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUpActivity.this,"Verification Link Sent to ur Email : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                                        if (localFileUri != null)
                                            updateNameAndPhoto();
                                        else
                                            updateNameOnly();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUpActivity.this,"Email Verification Link not Sent : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SignUpActivity.this,"SignUp Failed: %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        }
    }
}