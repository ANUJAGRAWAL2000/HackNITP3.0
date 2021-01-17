package com.example.chatapp.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Common.Constants;
import com.example.chatapp.Common.Extras;
import com.example.chatapp.Common.Internet;
import com.example.chatapp.Common.Node;
import com.example.chatapp.R;
import com.example.chatapp.SelectFriend.SelectFriendActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

public class SendActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText Message;
    private ImageView Send,File_Option,ivCustom,ivDisappearOn,ivDisappearOff;
    private RecyclerView rvMessage;
    private TextView tvCustom,tvStatus;
    private String UserName,PhotoName;

    private String Charset="ISO-8859-1";
    private LinearLayout llProgress;
    private SwipeRefreshLayout srl;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messageModelList;
    public DatabaseReference mRootRef,databaseReferenceMessages;
    private FirebaseAuth mAuth;
    private String currentUserId,chatUserId;

    private ChildEventListener childEventListener;
    private int currentPage=1,isDisappear=0;
    private static final int RECORD_PER_PAGE=30;

    private static final int REQUEST_CODE_PICK_IMAGE=101;
    private static final int REQUEST_CODE_CAPTURE_IMAGE=102;
    private static final int REQUEST_CODE_PICK_VIDEO=103;

    private static final int REQUEST_CODE_FORWARD_MESSAGE=104;

    private BottomSheetDialog bottomSheetDialog;

    //Here the Encryption and Decryption Part
    private byte encryptionKey[]={9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    private Cipher cipher,decipher;
    private SecretKeySpec secretKeySpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        //getSupportActionBar is to instantiate object of ActionBar
        ActionBar actionBar=getSupportActionBar();

        if(actionBar!=null)
        {
            actionBar.setTitle("");
            ViewGroup actionBarLayout=(ViewGroup) getLayoutInflater().inflate(R.layout.custom_layout,null);
            //It will show an arrow to go back
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);
            //To add the CustomLayout to this ActionBar
            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        }

        Send=findViewById(R.id.ivSend);
        File_Option=findViewById(R.id.ivFile);
        Message=findViewById(R.id.etMessage);

        Send.setOnClickListener(this);
        File_Option.setOnClickListener(this);

        mAuth=FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        currentUserId=mAuth.getCurrentUser().getUid();

        srl=findViewById(R.id.srlMessage);
        rvMessage=findViewById(R.id.rvMessage);

        llProgress=findViewById(R.id.llProgress);

        tvStatus=findViewById(R.id.tvStatus);
        tvCustom=findViewById(R.id.tvCustom);
        ivCustom=findViewById(R.id.ivCustom);
        ivDisappearOn=findViewById(R.id.DisappearOn);
        ivDisappearOff=findViewById(R.id.DisappearOff);


        ivDisappearOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDisappear=1;
                ivDisappearOn.setVisibility(View.GONE);
                ivDisappearOff.setVisibility(View.VISIBLE);
            }
        });

        ivDisappearOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDisappear=0;
                ivDisappearOff.setVisibility(View.GONE);
                ivDisappearOn.setVisibility(View.VISIBLE);
            }
        });

        if(getIntent().hasExtra(Extras.UserKey))
        {
            chatUserId=getIntent().getStringExtra(Extras.UserKey);
        }
        if(getIntent().hasExtra(Extras.UserName))
        {
            UserName=getIntent().getStringExtra(Extras.UserName);
        }
        if(getIntent().hasExtra(Extras.PhotoName))
        {
            PhotoName=getIntent().getStringExtra(Extras.PhotoName);
        }

        tvCustom.setText(UserName);

        if(!TextUtils.isEmpty(PhotoName)) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGE_FOLDER).child(PhotoName);
            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(SendActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(ivCustom);
                }
            });
        }

        //If the CurrentUser open the chat of the User who sent some messages so for that Unread Count will be >0
        //So we will set the Unread Count now as 0
        mRootRef.child(Node.Chats).child(currentUserId).child(chatUserId).child(Node.Unread_Count).setValue(0);

        messageModelList=new ArrayList<>();
        messageAdapter=new MessageAdapter(SendActivity.this,messageModelList);


        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        rvMessage.setAdapter(messageAdapter);

        loadMessage();

        rvMessage.scrollToPosition(messageModelList.size()-1);

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                loadMessage();
            }
        });


//          <----------Encryption Part----------->
        try{
            cipher = Cipher.getInstance("AES");
            decipher=Cipher.getInstance("AES");
        }catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec=new SecretKeySpec(encryptionKey,"AES");




        bottomSheetDialog=new BottomSheetDialog(this);
        View view= getLayoutInflater().inflate(R.layout.chat_file_option,null);
        view.findViewById(R.id.llCamera).setOnClickListener(this);
        view.findViewById(R.id.llVideo).setOnClickListener(this);
        view.findViewById(R.id.llGallery).setOnClickListener(this);
        view.findViewById(R.id.ivClose).setOnClickListener(this);
        bottomSheetDialog.setContentView(view);


        if(getIntent().hasExtra(Extras.Message)  &&  getIntent().hasExtra(Extras.Message_TYPE)  &&  getIntent().hasExtra(Extras.Message_ID))
        {
            String Message=getIntent().getStringExtra(Extras.Message);
            String MessageId=getIntent().getStringExtra(Extras.Message_ID);
            String MessageType=getIntent().getStringExtra(Extras.Message_TYPE);


            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child(Node.Messages).child(currentUserId)
                    .child(chatUserId).push();

            String newMessageID=databaseReference.getKey();

            if(MessageType.equals(Constants.MESSAGE_TYPE_TEXT))
            {
                SendMessage(Message,MessageType,MessageId);
            }
            else
            {
                StorageReference mStorage=FirebaseStorage.getInstance().getReference();
                String Folder=MessageType.equals(Constants.MESSAGE_TYPE_IMAGE)?Constants.MESSAGE_IMAGE:Constants.MESSAGE_VIDEO;
                String OldFile=MessageType.equals(Constants.MESSAGE_TYPE_IMAGE)?MessageId+".jpg":MessageId+".mp4";
                String NewFile=MessageType.equals(Constants.MESSAGE_TYPE_IMAGE)?newMessageID+".jpg":newMessageID+".mp4";

                String localFilePath=getExternalFilesDir(null).getAbsolutePath()+"/"+OldFile;
                File localFile=new File(localFilePath);

                StorageReference newFileRef=mStorage.child(Folder).child(NewFile);

                mStorage.child(Folder).child(OldFile).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        UploadTask task=newFileRef.putFile(Uri.fromFile(localFile));
                        uploadProgress(task,MessageType,newFileRef,newMessageID);
                    }
                });
            }
        }

        DatabaseReference databaseReferenceUsers=mRootRef.child(Node.Users).child(chatUserId);
        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status="";
                if(dataSnapshot.child(Node.Online).getValue()!=null)
                  status=dataSnapshot.child(Node.Online).getValue().toString();

                if(status.equals("true"))
                    tvStatus.setText(Constants.STATUS_ONLINE);
                else
                    tvStatus.setText(Constants.STATUS_OFFLINE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //Here we will add the Code For typing ..
        Message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                DatabaseReference currentUserRef=mRootRef.child(Node.Chats).child(currentUserId).child(chatUserId);
                if(s.toString().matches(""))
                {
                    currentUserRef.child(Node.TYPING).setValue(Constants.TYPING_STOP);
                }
                else
                {
                    currentUserRef.child(Node.TYPING).setValue(Constants.TYPING_START);
                }
            }
        });


        DatabaseReference chatUserRef=mRootRef.child(Node.Chats).child(chatUserId).child(currentUserId);
        chatUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Node.TYPING).getValue()!=null)
                {
                    String statusTyping=dataSnapshot.child(Node.TYPING).getValue().toString();

                    if(statusTyping.equals(Constants.TYPING_START))
                        tvStatus.setText(Constants.TYPING);

                    else
                        tvStatus.setText(Constants.STATUS_OFFLINE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void loadMessage()
    {
        messageModelList.clear();
        databaseReferenceMessages=FirebaseDatabase.getInstance().getReference().child(Node.Messages).child(currentUserId).child(chatUserId);

        Query messageQuery=databaseReferenceMessages.limitToLast(currentPage*RECORD_PER_PAGE);

        if(childEventListener!=null)
            messageQuery.removeEventListener(childEventListener);

        messageQuery.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //It will Directly Convert that Info into MessageModel Format;
                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
//                String stringMessage = messageModel.getMessage();
//                String[] stringMessageArray = stringMessage.split(",");
//                Arrays.sort(stringMessageArray);
//                String[] stringFinal = new String[stringMessageArray.length*2];
//
//                try {
//                for (int i = 0; i < stringMessageArray.length; i++)
//                {
//                        stringFinal[i]=AESDecryptionMethod(stringMessageArray[i]);
//                    Log.i("DECRPTY",stringFinal[i]);
//                }
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }


                String MESSAGE= null;
                try {
                    Log.i("MESSAGE",messageModel.getMessage());
                    MESSAGE = AESDecryptionMethod(messageModel.getMessage());
                    Log.i("DECRYPT",MESSAGE);
                    messageModel.setMessage(MESSAGE);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                messageModelList.add(messageModel);
                messageAdapter.notifyDataSetChanged();
                rvMessage.scrollToPosition(messageModelList.size()-1);
                srl.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                loadMessage();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                srl.setRefreshing(false);
            }
        });

//        messageQuery.addChildEventListener(childEventListener);
    }

    public void SendMessage(String msg,String msgType,String pushId)
    {
        try
        {
           if(!msg.equals(""))
           {
               HashMap messageMap=new HashMap();
               messageMap.put(Node.Message,msg);
               messageMap.put(Node.MessageFrom,currentUserId);
               messageMap.put(Node.MessageId,pushId);
               messageMap.put(Node.MessageType,msgType);
               messageMap.put(Node.TIME_STAMP, ServerValue.TIMESTAMP);

               String currentUserRef=Node.Messages+"/"+currentUserId+"/"+chatUserId;
               String chatUserRef=Node.Messages+"/"+chatUserId+"/"+currentUserId;

               HashMap messageUserMap=new HashMap();
               messageUserMap.put(currentUserRef+"/"+pushId,messageMap);
               messageUserMap.put(chatUserRef+"/"+pushId,messageMap);

               Message.setText("");

               mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                   @Override
                   public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                       if(databaseError!=null)
                       {
                           Toast.makeText(SendActivity.this,"Failed to Send Message : %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                       }
                       else
                       {
                           Toast.makeText(SendActivity.this,"Message Sent Successfully",Toast.LENGTH_SHORT).show();
                           String title="";
                           if(msgType.equals(Constants.MESSAGE_TYPE_TEXT))
                               title="New Message";
                           else if(msgType.equals(Constants.MESSAGE_TYPE_IMAGE))
                               title="New Image";
                           else if(msgType.equals(Constants.MESSAGE_TYPE_VIDEO))
                               title="New Video";
                           Internet.SendNotification(chatUserId,SendActivity.this,title,msg);

                           String LastMessage=!title.equals("New Message")?title:msg;

                           Internet.UnreadCount(SendActivity.this,currentUserId,chatUserId,LastMessage);
                       }
                   }
               });
           }
        }
        catch (Exception ex)
        {
            Toast.makeText(SendActivity.this,"Failed to Send Message : %1$s"+ex.getMessage(),Toast.LENGTH_SHORT).show();
        }


        if(isDisappear==1)
        {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   Delete(pushId,msgType);
                }
            },5000);
        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.ivSend:
                if(Internet.connectionAvailable(this))
                {
                    DatabaseReference userMessage = FirebaseDatabase.getInstance().getReference().child(Node.Messages).child(currentUserId).child(chatUserId).push();
                    String pushId = userMessage.getKey();
                    SendMessage(AESEncryptionMethod(Message.getText().toString().trim()), Constants.MESSAGE_TYPE_TEXT, pushId);
                }
                else
                {
                    Toast.makeText(this,"No Internet",Toast.LENGTH_SHORT).show();
                }
                break;

                case R.id.ivFile:
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    if(bottomSheetDialog!=null)
                        bottomSheetDialog.show();
                }
                else
                {
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                //if the Soft keyboard is open then we will also close that
                InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(inputMethodManager!=null)
                {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
                break;

            case R.id.llCamera:
                bottomSheetDialog.dismiss();
                Intent intentCamera=new Intent(ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentCamera,REQUEST_CODE_CAPTURE_IMAGE);
                break;

            case R.id.llGallery:
                bottomSheetDialog.dismiss();
                Intent intentGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentGallery,REQUEST_CODE_PICK_IMAGE);
                break;

            case R.id.llVideo:
                bottomSheetDialog.dismiss();
                Intent intentVideo=new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentVideo,REQUEST_CODE_PICK_VIDEO);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //This For loop is to check that after clicking on File option user select something or not or just close after opening
        if(resultCode==RESULT_OK)
        {
            if(requestCode==REQUEST_CODE_CAPTURE_IMAGE)
            {
                Bitmap bitmap=(Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                uploadImage(bytes,Constants.MESSAGE_TYPE_IMAGE);
            }
            else if(requestCode==REQUEST_CODE_PICK_IMAGE)
            {
                Uri uri=data.getData();
                uploadFile(uri,Constants.MESSAGE_TYPE_IMAGE);
            }
            else if(requestCode==REQUEST_CODE_PICK_VIDEO)
            {
                Uri uri=data.getData();
                uploadFile(uri,Constants.MESSAGE_TYPE_VIDEO);
            }
            else if(requestCode==REQUEST_CODE_FORWARD_MESSAGE)
            {
                Intent I=new Intent(this,SendActivity.class);
                I.putExtra(Extras.UserName,data.getStringExtra(Extras.UserName));
                I.putExtra(Extras.UserKey,data.getStringExtra(Extras.UserKey));
                I.putExtra(Extras.PhotoName,data.getStringExtra(Extras.PhotoName));
                I.putExtra(Extras.Message,data.getStringExtra(Extras.Message));
                I.putExtra(Extras.Message_TYPE,data.getStringExtra(Extras.Message_TYPE));
                I.putExtra(Extras.Message_ID,data.getStringExtra(Extras.Message_ID));
                startActivity(I);
                finish();
            }
        }
    }

    private void uploadProgress(final UploadTask task,final String message_type,final StorageReference filePath,final String pushId)
    {
        View view=getLayoutInflater().inflate(R.layout.file_progress,null);
        final TextView tvFilename=view.findViewById(R.id.tvFileProgress);
        final ProgressBar pbFile=view.findViewById(R.id.pbProgress);
        final ImageView ivPlay=view.findViewById(R.id.ivPlay);
        final ImageView ivPause=view.findViewById(R.id.ivPause);
        ImageView ivCancel=view.findViewById(R.id.ivCancel);

        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.resume();
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
            }
        });

        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.pause();
                ivPlay.setVisibility(View.VISIBLE);
                ivPause.setVisibility(View.GONE);
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.cancel();
            }
        });

        llProgress.addView(view);
        tvFilename.setText(getString(R.string.tvFileProgress,message_type,"0"));

        task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                pbFile.setProgress((int)progress);

                tvFilename.setText(getString(R.string.tvFileProgress,message_type,String.valueOf(pbFile.getProgress())));
            }
        });

        task.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                llProgress.removeView(view);
                if(task.isSuccessful())
                {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUri=uri.toString();
                            SendMessage(AESEncryptionMethod(downloadUri),message_type,pushId);
                        }
                    });
                }
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                llProgress.removeView(view);
                Toast.makeText(SendActivity.this,"Failed to Upload File : %1$s"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadFile(Uri uri,String messageType)
    {
        DatabaseReference databaseReference=mRootRef.child(Node.Message).child(currentUserId).child(chatUserId).push();
        String id=databaseReference.getKey();

        StorageReference storageReference= FirebaseStorage.getInstance().getReference();
        String FolderName=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?Constants.MESSAGE_IMAGE:Constants.MESSAGE_VIDEO;
        String Filename=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?id+".jpg":id+".mp4";

        StorageReference fileRef=storageReference.child(FolderName).child(Filename);
        UploadTask uploadTask=fileRef.putFile(uri);

        uploadProgress(uploadTask,messageType,fileRef,id);
    }

    private void uploadImage(ByteArrayOutputStream bytes,String messageType)
    {
        DatabaseReference databaseReference=mRootRef.child(Node.Message).child(currentUserId).child(chatUserId).push();
        String id=databaseReference.getKey();

        StorageReference storageReference= FirebaseStorage.getInstance().getReference();
        String FolderName=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?Constants.MESSAGE_IMAGE:Constants.MESSAGE_VIDEO;
        String Filename=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?id+".jpg":id+".mp4";

        StorageReference fileRef=storageReference.child(FolderName).child(Filename);
        UploadTask uploadTask=fileRef.putBytes(bytes.toByteArray());

        uploadProgress(uploadTask,messageType,fileRef,id);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //'1' is the request code passed while asking for request
        if(requestCode==1)
        {
         if(grantResults.length>0  &&  grantResults[0]==PackageManager.PERMISSION_GRANTED)
         {
             if(bottomSheetDialog!=null)
                 bottomSheetDialog.show();
         }
         else
         {
             Toast.makeText(this,"Permission Required to Access External Storage",Toast.LENGTH_SHORT).show();
         }
        }
    }

    //This function is for the Action Bar Icon(Arrow) if we click on that arrow we will come back else nothing happens..
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId=item.getItemId();
        switch (itemId)
        {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Edit(String messageId)
    {
        DatabaseReference databaseReference=mRootRef.child(Node.Messages).child(currentUserId).child(chatUserId).child(messageId);

        String CurrentMessage=Message.getText().toString().trim();

        if(databaseReference.child(Node.MessageFrom).getKey()==currentUserId)
        databaseReference.child(Node.Message).setValue(CurrentMessage);
        else
            Toast.makeText(SendActivity.this,"You are not allowed to Edit other's Text",Toast.LENGTH_SHORT).show();

        Message.setText("");
    }

    public void Delete(String messageId,String messageType)
    {
        //After this Function Complete we will call Load Message bacause a child is removed.
        DatabaseReference databaseReference=mRootRef.child(Node.Messages).child(currentUserId).child(chatUserId).child(messageId);

        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    DatabaseReference databaseReferenceChatUser=mRootRef.child(Node.Messages).child(chatUserId).child(currentUserId).child(messageId);

                    databaseReferenceChatUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SendActivity.this,"File Delete Successfully",Toast.LENGTH_SHORT).show();
                                //If the Deleted message is not text then we will also delete from Storage
                                if(!messageType.equals(Constants.MESSAGE_TYPE_TEXT))
                                {
                                    StorageReference storageReference=FirebaseStorage.getInstance().getReference();
                                    String FolderName=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?Constants.MESSAGE_IMAGE:Constants.MESSAGE_VIDEO;
                                    String FileName=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?messageId+".jpg":messageId+".mp4";
                                    StorageReference fileRef=storageReference.child(FolderName).child(FileName);

                                    fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(!task.isSuccessful())
                                            {
                                               Toast.makeText(SendActivity.this,"Failed to delete File : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                            else
                            {
                                Toast.makeText(SendActivity.this,"Failed to Delete File : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(SendActivity.this,"Failed to Delete File : %1$s"+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void Download(String messageId,String messageType,boolean isShare)
    {
        //To Download File we should have permission of WRITE_EXTERNAL_STORAGE in android.manifest file..
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(SendActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
        else
        {
            String folderName=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?Constants.MESSAGE_IMAGE:Constants.MESSAGE_VIDEO;
            String fileName=messageType.equals(Constants.MESSAGE_TYPE_IMAGE)?messageId+".jpg":messageId+".mp4";

            StorageReference storageReference=FirebaseStorage.getInstance().getReference().child(folderName).child(fileName);
            //FileStore Path in Device;
            String localFilePath=getExternalFilesDir(null).getAbsolutePath()+"/"+fileName;

            //localFile is the File Created inside localFilePath
            File localFile=new File(localFilePath);

            try
            {
                if(localFile.exists()  ||  localFile.createNewFile())
                {
                    FileDownloadTask fileDownloadTask=storageReference.getFile(localFile);

                    //Now we will show the Progress of Downloading..

                    View view=getLayoutInflater().inflate(R.layout.file_progress,null);
                    final TextView tvFilename=view.findViewById(R.id.tvFileProgress);
                    final ProgressBar pbFile=view.findViewById(R.id.pbProgress);
                    final ImageView ivPlay=view.findViewById(R.id.ivPlay);
                    final ImageView ivPause=view.findViewById(R.id.ivPause);
                    ImageView ivCancel=view.findViewById(R.id.ivCancel);

                    ivPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileDownloadTask.resume();
                            ivPlay.setVisibility(View.GONE);
                            ivPause.setVisibility(View.VISIBLE);
                        }
                    });

                    ivPause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileDownloadTask.pause();
                            ivPlay.setVisibility(View.VISIBLE);
                            ivPause.setVisibility(View.GONE);
                        }
                    });

                    ivCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileDownloadTask.cancel();
                        }
                    });

                    llProgress.addView(view);

                    tvFilename.setText(getString(R.string.tvDownloadProgress,messageType,"0"));

                    fileDownloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {

                            double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                            pbFile.setProgress((int)progress);

                            tvFilename.setText(getString(R.string.tvDownloadProgress,messageType,String.valueOf(pbFile.getProgress())));
                        }
                    });

                    fileDownloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            llProgress.removeView(view);
                            if(task.isSuccessful())
                            {
                                if(isShare)
                                {
                                    Intent intentShare=new Intent();
                                    intentShare.setAction(Intent.ACTION_SEND);
                                    intentShare.putExtra(Intent.EXTRA_STREAM,Uri.parse(localFilePath));
                                    if(messageType.equals(Constants.MESSAGE_TYPE_IMAGE))
                                        intentShare.setType("image/jpg");
                                    if(messageType.equals(Constants.MESSAGE_TYPE_VIDEO))
                                        intentShare.setType("video/mp4");
                                    startActivity(Intent.createChooser(intentShare,"Share With.."));
                                }
                                else {
                                    Snackbar snackbar = Snackbar.make(llProgress, "File Downloaded Successfully", Snackbar.LENGTH_INDEFINITE);

                                    snackbar.setAction(R.string.view, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Uri uri = Uri.parse(localFilePath);
                                            Intent I = new Intent(Intent.ACTION_VIEW, uri);

                                            if (messageType.equals(Constants.MESSAGE_TYPE_IMAGE))
                                                I.setDataAndType(uri, "image/jpg");
                                            else if (messageType.equals(Constants.MESSAGE_TYPE_VIDEO))
                                                I.setDataAndType(uri, "video/mp4");

                                            startActivity(I);
                                        }
                                    });
                                    snackbar.show();
                                }
                            }
                        }
                    });

                    fileDownloadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            llProgress.removeView(view);
                            Toast.makeText(SendActivity.this,"Failed to Download File : %1$s"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(SendActivity.this,"Failed to store File" ,Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(SendActivity.this,"Failed to Download File : %1$s"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Forward(String messageId, String messageType, String message) {

        Intent I=new Intent(this, SelectFriendActivity.class);

        I.putExtra(Extras.Message,message);
        I.putExtra(Extras.Message_ID,messageId);
        I.putExtra(Extras.Message_TYPE,messageType);

        //ForResult Because we want the result Back
        startActivityForResult(I,REQUEST_CODE_FORWARD_MESSAGE);
    }

    private String AESEncryptionMethod(String string)
    {
        byte[] stringByte=string.getBytes();
        byte[] encryptedByte=new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte=cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString=null;
        try {
           returnString=new String(encryptedByte,Charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }


    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
        byte[] EncryptedByte=string.getBytes(Charset);
        String decryptedString=string;

        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE,secretKeySpec);
            decryption=decipher.doFinal(EncryptedByte);
            decryptedString=new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decryptedString;
    }

    @Override
    public void onBackPressed() {
//        mRootRef.child(Node.Chats).child(currentUserId).child(chatUserId).child(Node.Unread_Count).setValue(0);
        super.onBackPressed();
    }
}