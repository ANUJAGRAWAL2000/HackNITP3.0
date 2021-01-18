package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatapp.Chat.ChatFragment;
import com.example.chatapp.Chat.ChatListModel;
import com.example.chatapp.Common.Constants;
import com.example.chatapp.Common.Node;
import com.example.chatapp.FriendRequest.FriendListFragment;
import com.example.chatapp.Profile.ProfileActivity;
import com.example.chatapp.RequestReceived.RequestFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabmain;
    private ViewPager vpmain;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceChats;
    private FirebaseUser firebaseUser;
    Intent callIntent;
    private String numberToCall = "8619921796";
    private LocationManager locationManager;
    private LocationListener locationListener;
    protected static String address = "";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                    startActivity(callIntent);

                }
            }

        } else if(requestCode == 2) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

               if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, locationListener);

               }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabmain=findViewById(R.id.tabmain);
        vpmain=findViewById(R.id.vpmain);
        mAuth=FirebaseAuth.getInstance();

        //Here When the user will reach to main Activity we will set the Online status as true
        DatabaseReference databaseReferenceUsers= FirebaseDatabase.getInstance().getReference()
                .child(Node.Users).child(mAuth.getCurrentUser().getUid());

        databaseReferenceUsers.child(Node.Online).setValue(true);

        databaseReferenceUsers.child(Node.Online).onDisconnect().setValue(false);

        firebaseUser=mAuth.getCurrentUser();
        databaseReferenceChats=FirebaseDatabase.getInstance().getReference().child(Node.Chats).child(firebaseUser.getUid());

        setViewPager();
    }

    class Adapter extends FragmentPagerAdapter
    {
        public Adapter(@NonNull FragmentManager fm, int behavior)
        {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    ChatFragment chatFragment=new ChatFragment();
                    return chatFragment;
                case 1:
                    RequestFragment requestFragment=new RequestFragment();
                    return requestFragment;
                case 2:
                    FriendListFragment friendListFragment=new FriendListFragment();
                    return friendListFragment;

            }
            return null;
        }

        @Override
        public int getCount()
        {
            return tabmain.getTabCount();
        }
    }

    private void setViewPager()
    {
        tabmain.addTab(tabmain.newTab().setCustomView(R.layout.tab_chat));
        tabmain.addTab(tabmain.newTab().setCustomView(R.layout.tab_request));
        tabmain.addTab(tabmain.newTab().setCustomView(R.layout.tab_friend));

        tabmain.setTabGravity(TabLayout.GRAVITY_FILL);
        //It will Occupy the Whole Screen..

        Adapter adapter=new Adapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        //Behaviour Resume will make all the other fragments as inactive and the shown fragment active..
        vpmain.setAdapter(adapter);

        tabmain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                vpmain.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        vpmain.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabmain));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            case R.id.menu_unHide:
                Log.i("UNHIDE", "HI");
                databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(Node.Chats).child(firebaseUser.getUid());
                databaseReferenceChats.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String UserID = ds.getKey();
                            Log.i("ID", UserID);
                            databaseReferenceChats.child(UserID).child(Node.HIDE).setValue(Constants.FALSE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            case R.id.menuCall:
                callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + numberToCall));//change the number
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CALL_PHONE}, 1);

                } else {

                    startActivity(callIntent);

                }
                return true;
            case R.id.menuLocation:

                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                address += addresses.get(0).getAddressLine(0);
                                Log.i("Latitude", String.valueOf(location.getLatitude()));
                                Log.i("Address!!!!!!", address);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else{
                            Log.i("NULL", "IS NULL");
                        }
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {

                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                };
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, locationListener);
                }

                default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean doubleBackPress=false;

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//This super already has the close Functionality so it will Close activity if we will not Comment Out and the following Code will be useless..
        if(tabmain.getSelectedTabPosition()>0)
        {
            tabmain.selectTab(tabmain.getTabAt(0));
        }
        else
        {
            if(doubleBackPress)
            {
                finishAffinity();
            }
            else
            {
                doubleBackPress=true;
                Toast.makeText(MainActivity.this,"Press Again to exit",Toast.LENGTH_SHORT).show();

                Thread timer = new Thread(){
                    public void run(){
                        try
                        {
                            sleep(2000);
                            doubleBackPress=false;
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                timer.start();
            }
        }
    }
}