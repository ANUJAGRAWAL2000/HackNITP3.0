package com.example.chatapp.Common;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Internet {

    public static boolean connectionAvailable(Context context)
    {
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null  &&  connectivityManager.getActiveNetworkInfo()!=null)
        {
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else
        {
            return false;
        }
    }

    public static void UpdateDeviceToken(String Token,Context context)
    {

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();

        if(firebaseUser!=null)
        {
            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference databaseReferenceNode = mRootRef.child(Node.Token).child(firebaseUser.getUid());

            HashMap<String,String> hashMap=new HashMap<>();
            hashMap.put(Node.DEVICE_TOKEN,Token);
            databaseReferenceNode.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(!task.isSuccessful())
                    {
                        Toast.makeText(context,"Failed To Update Token",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //UserId is for the person whom we are sending Notification..
    public  static void SendNotification(String userId,Context context,String Title,String Message)
    {
        DatabaseReference mRootRef=FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReferenceToken=mRootRef.child(Node.Token).child(userId);

        databaseReferenceToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Node.DEVICE_TOKEN).getKey()!=null)
                {
                 String deviceToken=dataSnapshot.child(Node.DEVICE_TOKEN).getKey().toString();

                    JSONObject notification=new JSONObject();
                    JSONObject notificationData=new JSONObject();

                    try
                    {
                        notification.put(Constants.NOTIFICATION_TO,deviceToken);

                        notificationData.put(Constants.NOTIFICATION_MESSAGE,Message);
                        notificationData.put(Constants.NOTIFICATION_TITLE,Title);

                        notification.put(Constants.NOTIFICATION_DATA,notificationData);

                        //TO Send Notification we required to make a call to Web API
                        //For making a call we required Libraries which are Volley,etc.

                        String fcmApiUrl="https://fcm.googleapis.com/fcm/send";
                        String contentType="application/json";

                        Response.Listener successListener=new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {
                                Toast.makeText(context,"Notification Sent",Toast.LENGTH_SHORT).show();
                            }
                        };

                        Response.ErrorListener failureListener=new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context,"Failed to Send Notification : %1$s"+error.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        };

                        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(fcmApiUrl,notification,successListener,failureListener){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError
                            {
                                Map<String,String > params=new HashMap<>();
                                params.put("Authorization","Key="+Constants.FIREBASE_KEY);
                                params.put("Sender","id="+Constants.SENDER_ID);
                                params.put("Content-Type",contentType);
                                return params;
                            }
                        };

                        RequestQueue requestQueue= Volley.newRequestQueue(context);
                        requestQueue.add(jsonObjectRequest);

                    }
                    catch (JSONException e) {
                        Toast.makeText(context,"Failed to Send Notification : %1$s"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(context,"Failed to Send Notification : %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }


    //This Unread Count Function will be call when User will Send Message..
    public static void UnreadCount(Context context,String currentUserId,String chatUserId,String lastMessage)
    {
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRef=rootRef.child(Node.Chats).child(chatUserId).child(currentUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String currentCount="0";

                if(dataSnapshot.child(Node.Unread_Count).getValue()!=null)
                {
                    currentCount=dataSnapshot.child(Node.Unread_Count).getValue().toString();

                    Map chatMap=new HashMap();
                    chatMap.put(Node.TIME_STAMP, ServerValue.TIMESTAMP);
                    chatMap.put(Node.Unread_Count,String.valueOf(Integer.valueOf(currentCount)+1));
                    chatMap.put(Node.Last_Message,lastMessage);
                    chatMap.put(Node.Last_Message_Time,ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put(Node.Chats+"/"+chatUserId+"/"+currentUserId,chatMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                          if(databaseError!=null)
                          {
                              Toast.makeText(context,"Something Went Wrong : %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                          }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,"Something Went Wrong : %1$s"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static String MessageTime(long time)
    {
        final int SECOND_MILLIS=1000;
        final int MINUTE_MILLIS=60*SECOND_MILLIS;
        final int HOUR_MILLIS=60*MINUTE_MILLIS;
        final int DAY_MILLIS=24*HOUR_MILLIS;

        //Converting Because to get time in MILLIS
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now =System.currentTimeMillis();

        if(time>now  ||  time<=0)
            return "";

        final long diff=now-time;

        if(diff<MINUTE_MILLIS)
            return "just now";
        else if(diff<2*MINUTE_MILLIS)
            return "a minute ago";
        else if(diff<59*MINUTE_MILLIS)
            return diff/MINUTE_MILLIS+"minute ago";
        else if(diff<90*MINUTE_MILLIS)
            return "an hour ago";
        else if(diff<24*HOUR_MILLIS)
            return diff/HOUR_MILLIS+"hour ago";
        else if(diff<2*DAY_MILLIS)
            return "yesterday";
        else
            return diff/DAY_MILLIS+"day ago";

    }

}
