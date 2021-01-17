package com.example.chatapp.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.News.NewsActivity;
import com.example.chatapp.Common.Node;
import com.example.chatapp.Login.LoginActivity;
import com.example.chatapp.MainActivity;
import com.example.chatapp.News.NewsMainActivity;
import com.example.chatapp.R;
import com.example.chatapp.SignUp.SignUpActivity;
import com.example.chatapp.password.changePasswordActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName,etEmail;
    private String Email,Name;
    private String Password;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference mStorage;
    private ImageView ivProfile;
    private Uri localFileUri,ServerFileUri;

    public void ChangeView(View V)
    {
        if(ServerFileUri==null)
        {
            PickImage();
        }
        else if(ServerFileUri!=null)
        {
            PopupMenu popupMenu=new PopupMenu(this,V);
            //Inflate :- Convert Xml File to Visible..
            popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int Id=item.getItemId();
                    if(Id==R.id.menu_picture)
                    {
                        PickImage();
                    }
                    else if(Id==R.id.removePicture)
                    {
                        RemovePicture();
                    }
                        return false;
                }
            });
            popupMenu.show();
        }
    }

    private void RemovePicture()
    {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().
                setDisplayName(etName.getText().toString().trim())
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    String UserID=firebaseUser.getUid();
                    databaseReference= FirebaseDatabase.getInstance().getReference().child(Node.Users);
                    HashMap hashMap=new HashMap();
                    hashMap.put(Node.Photo,"");

                    databaseReference.child(UserID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Photo Removed Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"Picture Not Removed",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,"Failed to Update : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    public void btnLogout(View view)
    {
        FirebaseAuth mAuth=FirebaseAuth.getInstance();

        //Here before signOut we will delete the Token

        FirebaseUser currentUser=mAuth.getCurrentUser();
        DatabaseReference mRootRef=FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReferenceToken=mRootRef.child(Node.Token).child(currentUser.getUid());
        databaseReferenceToken.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    mAuth.signOut();
                    finish();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void btnSave(View V)
    {
        if(etName.getText().toString().trim().equals(""))
        {
            etName.setError(getString(R.string.etName));
        }
        else
        {
            if(localFileUri!=null)
                updateNameAndPhoto();
            else
                updateNameOnly();
        }
    }

    public void PickImage() {
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
        mStorage=FirebaseStorage.getInstance().getReference();
        final StorageReference FileRef = mStorage.child("Images/"+strFile);
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
                                        HashMap hashMap=new HashMap();
                                        hashMap.put(Node.Name,etName.getText().toString().trim());
                                        hashMap.put(Node.Photo,ServerFileUri.getPath());

                                        databaseReference.child(UserID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    finish();
                                                }
                                                else
                                                {
                                                    Toast.makeText(ProfileActivity.this,"Not Updated",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(ProfileActivity.this,"Failed to Update : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();

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
                    HashMap hashMap=new HashMap();
                    hashMap.put(Node.Name,etName.getText().toString().trim());

                    databaseReference.child(UserID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                finish();
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this,"User Not Created",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ProfileActivity.this,"Failed to Update : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    public void changePassword(View view)
    {
        startActivity(new Intent(ProfileActivity.this, changePasswordActivity.class));
    }

    public void deleteAcc(View view) {

        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Do you want to delete your account?")
                .setMessage("Your account will be delete forever!!!")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setTitle("Enter your Password");

                        final EditText input = new EditText(ProfileActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                Password = input.getText().toString();

                                final String email = firebaseUser.getEmail();
                                AuthCredential credential = EmailAuthProvider.getCredential(email, Password);

                                firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                startActivity(new Intent(ProfileActivity.this, SignUpActivity.class));
                                                            }
                                                        }
                                                    });
                                        } else {

                                            Toast.makeText(ProfileActivity.this, "Wrong Old Password!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        });
                        builder.setNegativeButton("Cancel", null);

                        builder.show();

                    }
                })
                .setNegativeButton("Cancel",null).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        super.onOptionsItemSelected(item);
        startActivity(new Intent(ProfileActivity.this, NewsMainActivity.class));
        return true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName=(TextInputEditText)findViewById(R.id.etName);
        etEmail=(TextInputEditText)findViewById(R.id.etEmail);
        ivProfile=findViewById(R.id.Profile);

        mStorage= FirebaseStorage.getInstance().getReference();
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser!=null)
        {
            etName.setText(firebaseUser.getDisplayName());
            etEmail.setText(firebaseUser.getEmail());
            ServerFileUri=firebaseUser.getPhotoUrl();

            if(ServerFileUri!=null)
            {
                Glide.with(this)
                        .load(ServerFileUri)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(ivProfile);

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
    }
}