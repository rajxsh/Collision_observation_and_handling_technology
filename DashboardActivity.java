package com.example.coht;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardActivity  extends AppCompatActivity {

    private final int MY_PERMISSION_REQUEST_CODE = 1;
    private PermissionHandler mPermissionHandler;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ServiceHandler mServiceHandler;
    private static boolean isTracking;
    private Button buttonToggleTracking;
    private DBEmergency mDatabase;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ExpandableListAdapter listAdapter;
    ExpandableListView mDrawerexpList;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    int[] login_icons=new int[]{
            R.drawable.carpool_32,
            R.drawable.myaccount,
            R.drawable.policy1,
            R.drawable.mobile,
            R.drawable.logout,
    };
    private Typeface custom_font;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        custom_font = Typeface.createFromAsset(getAssets(), "AvenirNextLTPro-MediumCn.otf");

        mPermissionHandler = new PermissionHandler(this);
        firebaseAuth = FirebaseAuth.getInstance();
        mServiceHandler = new ServiceHandler(this);
        isTracking = false;
        buttonToggleTracking = (Button) findViewById(R.id.buttonToggleTracking);
        CustomToastActivity.CustomToastActivity(this);
        mDatabase = new DBEmergency(this);

        setupFirebase();

        prepareListDataSignin();
        listAdapter=new ExpandableListAdapter1(this,listDataHeader,login_icons,custom_font,custom_font);

        mTitle = mDrawerTitle = getTitle();
        mDrawerexpList = (ExpandableListView)findViewById(R.id.left_drawer);
        mDrawerexpList.setGroupIndicator(null);
        custom_font = Typeface.createFromAsset(getAssets(), "AvenirNextLTPro-MediumCn.otf");
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


        //set drawer expandable list adapter
        mDrawerexpList.setAdapter(listAdapter);

        //Creation of expandable listView
        mDrawerexpList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if (groupPosition == 0) {
                    // Already in this Activity
                    mDrawerLayout.closeDrawer(mDrawerexpList);
                }
                if (groupPosition == 1) {
                    finish();
                    Intent intent=new Intent(DashboardActivity.this, MyEmerContActivity.class);
                    startActivity(intent);
                    mDrawerLayout.closeDrawer(mDrawerexpList);
                }
                if (groupPosition == 2) {
                    finish();
                    Intent intent=new Intent(DashboardActivity.this, MyAccount.class);
                    startActivity(intent);
                    mDrawerLayout.closeDrawer(mDrawerexpList);
                }
                if(groupPosition==3){
                    finish();
                    Intent intent = new Intent(DashboardActivity.this,MusicMain.class);
                    startActivity(intent);
                    mDrawerLayout.closeDrawer(mDrawerexpList);

                }
                if(groupPosition==4){
                    finish();
                    Intent intent = new Intent(DashboardActivity.this,Chat.class);
                    startActivity(intent);
                    mDrawerLayout.closeDrawer(mDrawerexpList);

                }
                if (groupPosition == 5){
                    logout();
                }
                return false;

            }
        });
        // Listview Group expanded listener
        mDrawerexpList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        // Listview Group collasped listener
        mDrawerexpList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

            }
        });

        final androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close)
        {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().getThemedContext();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isTracking) {
            toggleTracking();
        }
    }

    public void startTrackingWrapper(View view) {
        if (isTracking) {
            toggleTracking();
        } else {
            // Ask for Permissions
            // Add permissions to the permissionName List
            List<String> permissionName = new ArrayList<>();
            List<String> permissionTag = new ArrayList<>();
            permissionName.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionName.add(Manifest.permission.READ_PHONE_STATE);
            permissionName.add(Manifest.permission.SEND_SMS);
            permissionTag.add("Access Location");
            permissionTag.add("Read Phone State");
            permissionTag.add("Send SMS");

            if (!mPermissionHandler.requestPermissions(MY_PERMISSION_REQUEST_CODE, permissionName, permissionTag)
                    || !locationServicesStatusCheck() || !hasContact())
                return;

            toggleTracking();
        }
    }

    private void toggleTracking() {
        //Intent intent = new Intent(DashboardActivity.this, TrackingActivity.class);
        //startActivity(intent);

        if (isTracking) {
            mServiceHandler.doUnbindService();
            buttonToggleTracking.setText("Start Tracking");
            isTracking = false;
        } else {
            mServiceHandler.doBindService();
            buttonToggleTracking.setText("Stop Tracking");
            isTracking = true;
        }
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            finish();
            startActivity(new Intent(this, LoginScreenActivity.class));
        }
    }

    public void logout()
    {
        try {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginScreenActivity.class));
        } catch (Exception e) {
            Toast.makeText(this, "Unsuccessful", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (mPermissionHandler.handleRequestResult(requestCode, permissions, grantResults)) {
                    toggleTracking();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean locationServicesStatusCheck() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setTitle("Enable GPS")
                .setMessage("This function needs your GPS, do you want to enable it now?")
                .setIcon(android.R.drawable.ic_menu_mylocation)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();


        return false;
    }

    private boolean hasContact() {
        String email = firebaseUser.getEmail();
        List<EmerContact> contact = mDatabase.getContact(email);
        if (contact.isEmpty()) {
            CustomToastActivity.showCustomToast("Please add at least 1 Emergency Contact");
            return false;
        } else {
            return true;
        }
    }

    private void prepareListDataSignin() {
        listDataHeader =new ArrayList<String>();
        //listDataChild = new HashMap<String, List<String>>();

        // Adding group data
        listDataHeader.add("Dashboard");
        listDataHeader.add("Emergency Contacts");
        listDataHeader.add("My Account");
        listDataHeader.add("Music");
       listDataHeader.add("Chat");
        listDataHeader.add("Log Out");

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            //case R.id.search:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void onBackPressed() {

        Log.d("Finish","Change Password   Activity");
        finish();
    }
}
