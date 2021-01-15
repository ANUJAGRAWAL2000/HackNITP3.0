package com.example.chatapp.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chatapp.Common.Constants;
import com.example.chatapp.Common.Internet;
import com.example.chatapp.Login.LoginActivity;
import com.example.chatapp.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//For Extending FirebaseMessaging Service we have include service in Android Manifest File..
public class ChatMessagingService extends FirebaseMessagingService
{
    @Override
    public void onNewToken(@NonNull String s) {

        super.onNewToken(s);

        Internet.UpdateDeviceToken(s,this);

    }

    @Override
    public void onMessageReceived(RemoteMessage  remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title=remoteMessage.getData().get(Constants.NOTIFICATION_TITLE);
        String message=remoteMessage.getData().get(Constants.NOTIFICATION_MESSAGE);

        Intent intentChat=new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intentChat,PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //Sound When Notification will receive
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder;

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel channel=new NotificationChannel(Constants.CHANNEL_ID,Constants.CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(Constants.CHANNEL_DESC);

            notificationManager.createNotificationChannel(channel);

            notificationBuilder=new NotificationCompat.Builder(this,Constants.CHANNEL_ID);
        }
        else
        {
            notificationBuilder=new NotificationCompat.Builder(this);
        }

        notificationBuilder.setSmallIcon(R.drawable.ic_chat);
        notificationBuilder.setColor(getResources().getColor(R.color.navy));
        notificationBuilder.setContentTitle(title);
        //this auto cancel that after opening/clicking it will clear
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setContentIntent(pendingIntent);

        if(message.startsWith("https://firebasestorage."))
        {
            try
            {
                final NotificationCompat.BigPictureStyle bigPictureStyle=new NotificationCompat.BigPictureStyle();

                Glide.with(this)
                        .asBitmap()
                        .load(message)
                        .into(new CustomTarget<Bitmap>(200,100) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                bigPictureStyle.bigPicture(resource);
                                notificationBuilder.setStyle(bigPictureStyle);
                                notificationManager.notify(999,notificationBuilder.build());
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
            catch (Exception e)
            {
                notificationBuilder.setContentText("New File Received");

                notificationManager.notify(999, notificationBuilder.build());
            }
        }
        else {
            notificationBuilder.setContentText(message);

            notificationManager.notify(999, notificationBuilder.build());
        }
        }
}