package com.example.nejcvesel.pazikjehodis;

//import android.app.FragmentManager;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.nejcvesel.pazikjehodis.Handlers.FabHandler;
import com.example.nejcvesel.pazikjehodis.Fragments.AddFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.LocationDetailFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.LocationFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.LocationInPathDetailFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.LogInFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.MapsFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.MyLocationsFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.PathAddFormFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.PathAddFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.PathListFragment;
import com.example.nejcvesel.pazikjehodis.Fragments.PathLocationsFragment;
import com.example.nejcvesel.pazikjehodis.Walkthrough.WalkthroughActivity;
import com.example.nejcvesel.pazikjehodis.retrofitAPI.BackendAPICall;
import com.example.nejcvesel.pazikjehodis.retrofitAPI.Models.BackendToken;
import com.example.nejcvesel.pazikjehodis.retrofitAPI.Models.Location;
import com.example.nejcvesel.pazikjehodis.retrofitAPI.Models.Path;
import com.example.nejcvesel.pazikjehodis.retrofitAPI.Models.User;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.Marker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brani on 12/18/2016.
 */

/*
TODO: Brane - fab buttons
TODO: Nejc - Authentication, Google, Facebook, all flow
TODO: Nejc - My location and My Paths backend and frontend
TODO: Brane - design everything!
TODO: Brane - margine on every view (is inccorect)
TODO: Brane - login screen
TODO: Nejc - search
TODO: Brane/Nejc - menu not show up my location and my paths when is login
TODO: Brane/Nejc - upload fail permissions for every phone (testiraj)
TODO: Brane - reverse geocode on googlemap for giving your current location
TODO: Brane/Nejc - loader
TODO: Brane/Nejc - when loading more then 1 min , give error (TOAST)!
TODO: Brane - on click mapinfowindow - open location details
* */
public class MainActivity extends AppCompatActivity implements
        LocationFragment.OnListFragmentInteractionListener,
        LocationDetailFragment.OnFragmentInteractionListener, PathLocationsFragment.OnListFragmentInteractionListener,
        LocationInPathDetailFragment.OnFragmentInteractionListener, GoogleApiClient.OnConnectionFailedListener,
        PathAddFormFragment.OnFragmentInteractionListener, PathAddFragment.OnListFragmentInteractionListener, BackendAPICall.BackendCallback{
    public static String authToken;
    public Marker currentMarker = null;
    public Uri url = null;
    public CallbackManager callbackManager;
    public AccessTokenTracker accessTokenTracker;
    public ProfileTracker profileTracker;
    private FloatingActionButton fab, fab1, fab2;
    public ArrayList<String> pathLocations = new ArrayList<String>();
    public Profile profile;
    public HashMap<String, String> locationsToAddToPath = new HashMap<String, String>();
    public GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";
    BackendAPICall api;
    public SharedPreferences sharedPref;
    public UserProfile userProfile = null;
    public FabHandler fabClick;
    public boolean markerAddEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        //sharedPref.edit().clear().commit();
        userProfile = new UserProfile();
        api = new BackendAPICall(this);

        Map<?, ?> sharedPrefValues = sharedPref.getAll();
        for (Object key : sharedPrefValues.keySet()) {
            String tmpKey = key.toString();
            if (tmpKey.contains("_token")) {
                String userToken = tmpKey.split("_token")[0];
                Object currToken = sharedPrefValues.get(key);
                userProfile.setUserToken(userToken);
                userProfile.setBackendAccessToken(currToken.toString());
            }
            if (tmpKey.contains("_refresh")) {
                Object currToken = sharedPrefValues.get(key);
                userProfile.setRefreshToken(currToken.toString());
            }

            if(tmpKey.contains("_firstName"))
            {
                Object currFirstName = sharedPrefValues.get(key);
                userProfile.setFirstName(currFirstName.toString());
            }
            if(tmpKey.contains("_lastName"))
            {
                Object currLastName = sharedPrefValues.get(key);
                userProfile.setLastName(currLastName.toString());
            }
        }

        if (!userProfile.getRefreshToken().equals(""))
        {
            api.refreshToken(userProfile.getRefreshToken());
        }

        if (!userProfile.getBackendAccessToken().equals(""))
        {
            api.getUserProfile(userProfile.getBackendAccessToken());
        }



        // BackendAPICall callProfile = new BackendAPICall();
        //callProfile.getUserProfile(userProfile.getBackendAccessToken());

//        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        authToken = loginResult.getAccessToken().getToken();
                        AccessToken.setCurrentAccessToken(loginResult.getAccessToken());
                        userProfile.setUserToken(authToken);
                        userProfile.setLoginType("Facebook");
                        if (userProfile.getBackendAccessToken().equals(""))
                        {
                            api.convertToken(authToken);
                        } else {
                            api.refreshToken(userProfile.getRefreshToken());
                        }
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        //TODO: login registration for fb and google!
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                TextView userName = (TextView) findViewById(R.id.navHeaderText);
                if (currentAccessToken == null) {
                    userName.setText("Anonimen uporabnik");
                }
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                if (currentProfile != null) {
                    TextView name = (TextView) findViewById(R.id.navHeaderText);
                    name.setText(currentProfile.getFirstName() + " " + currentProfile.getLastName());
                    userProfile.setFirstName(currentProfile.getFirstName());
                    userProfile.setLastName(currentProfile.getLastName());
                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putString(authToken + "_firstName", currentProfile.getFirstName());
                    edit.putString(authToken + "_lastName", currentProfile.getLastName());
                    edit.commit();
                }

            }
        };

        //TODO: fab buttons - set right images as well right colors for fab buttons
        //TODO: fab buttons - on/off in different fragments
        profile = Profile.getCurrentProfile();



        fabClick = new FabHandler(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(fabClick);
        fab1.setOnClickListener(fabClick);
        fab2.setOnClickListener(fabClick);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        android.support.v7.app.ActionBarDrawerToggle toggle = new android.support.v7.app.ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        TextView profileName = (TextView) findViewById(R.id.navHeaderText);
        if (!userProfile.getFirstName().equals("") && !userProfile.getLastName().equals("")) {
            profileName.setText(userProfile.getFirstName() + " " + userProfile.getLastName());
        } else {
            profileName.setText("Anonimen uporabnik");
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MapsFragment(), "MapFragment").addToBackStack("MapFragment").commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {

            getFragmentManager().popBackStack();
        }


        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //  if (drawer.isDrawerOpen(GravityCompat.START)) {
        //  drawer.closeDrawer(GravityCompat.START);
        // } else {
        //   super.onBackPressed();
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            System.out.println(acct.getDisplayName());
            // updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            // updateUI(false);
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(Location item) {

    }




    //Map interaction method
    public boolean isMarker() {
        return currentMarker != null;
    }

    public void RemoveMarker() {
        currentMarker.remove();
    }

    public void AddMarker(Marker marker) {
        currentMarker = marker;
    }

    public void showLocationOnMap(View v, String pathLocations) {
        this.pathLocations.clear();
        pathLocations = pathLocations.replaceAll("\\[", "");
        pathLocations = pathLocations.replaceAll("\\]", "");
        pathLocations = pathLocations.replaceAll(" ", "");
        String[] pathLocationArray = pathLocations.split(",");

        for (int i = 0; i < pathLocationArray.length; i++) {
            this.pathLocations.add(pathLocationArray[i]);
        }

        System.out.println(Arrays.toString(pathLocationArray));
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MapsFragment(), "MapFragment").addToBackStack("MapFragment").commit();

    }

    public void uploadPath(Path path) {
        AccessToken at = AccessToken.getCurrentAccessToken();
        authToken = at.getToken().toString();
//        BackendAPICall api = new BackendAPICall(this);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String token = sharedPref.getString(authToken + "_token", "noToken");

        if (token.equals("noToken")) {
            api.convertTokenAndAddPath(path, authToken, sharedPref);
        } else {
            api.addPath(path, authToken, sharedPref);
            //api.refreshToken(authToken,sharedPref);
        }

    }

    public static String[] stringToStringArray(String pathLocations) {

        pathLocations = pathLocations.replaceAll("\\[", "");
        pathLocations = pathLocations.replaceAll("\\]", "");
        pathLocations = pathLocations.replaceAll(" ", "");
        String[] pathLocationArray = pathLocations.split(",");
        return pathLocationArray;

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*Navigation drawer on clicked item handler*/
    public void navigationViewLocationClick(View view) {
        CloseMarkerInfoWindow();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new LocationFragment(), "LocationFragment").addToBackStack("LocationFragment").commit();
        closeDrawer();
    }

    public void navigationViewMapClick(View view) {
        CloseMarkerInfoWindow();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MapsFragment(), "MapFragment").addToBackStack("MapFragment").commit();
        closeDrawer();
    }

    public void navigationViewMyLocationClick(View view) {
        CloseMarkerInfoWindow();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new MyLocationsFragment(), "MyLocationsFragment").addToBackStack("MyLocationsFragment").commit();
        closeDrawer();
    }

    public void navigationViewPathClick(View view) {
        CloseMarkerInfoWindow();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new PathListFragment(), "PathListFragment").addToBackStack("PathListFragment").commit();
        closeDrawer();
    }

    public void navigationViewMyPathClick(View view) {
        CloseMarkerInfoWindow();
//        FragmentManager fm = getFragmentManager();
//        fm.beginTransaction().replace(R.id.content_frame, new MapsFragment(), "MapFragment").addToBackStack("MapFragment").commit();
        closeDrawer();
    }

    public void navigationViewSearchClick(View view) {
        CloseMarkerInfoWindow();
//        FragmentManager fm = getFragmentManager();
//        fm.beginTransaction().replace(R.id.content_frame, new MapsFragment(), "MapFragment").addToBackStack("MapFragment").commit();
        closeDrawer();
    }

    public void navigationViewAddClick(View view) {
        CloseMarkerInfoWindow();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new AddFragment(), "AddFragment").addToBackStack("AddFragment").commit();
        closeDrawer();
    }

    public void navigationViewWhatsNewClick(View view) {
        CloseMarkerInfoWindow();
        Intent intent = new Intent(getApplicationContext(), WalkthroughActivity.class);
        startActivity(intent);
        closeDrawer();
    }

    public void navigationViewLogingClick(View view) {
        CloseMarkerInfoWindow();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, new LogInFragment(), "LogInFragment").addToBackStack("LogInFragment").commit();
        closeDrawer();
    }

    public void navigationViewHomeClick(View view) {
        CloseMarkerInfoWindow();
        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(intent);
    }

    public void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    public void setScreenLayout(boolean isAnonym) {
        View l1 = findViewById(R.id.my_location_layout);
        View l2 = findViewById(R.id.my_paths_layout);
        View d1 = findViewById(R.id.my_location_divisor);
        View d2 = findViewById(R.id.my_paths_divisor);
        if (isAnonym) {
            l1.setVisibility(View.GONE);
            l2.setVisibility(View.GONE);
            d1.setVisibility(View.GONE);
            d2.setVisibility(View.GONE);
        } else {
            l1.setVisibility(View.VISIBLE);
            l2.setVisibility(View.VISIBLE);
            d1.setVisibility(View.VISIBLE);
            d2.setVisibility(View.VISIBLE);
        }
    }

    public void RefreshUser() {

    }

    public void CloseMarkerInfoWindow() {
        final View infoWindow = findViewById(R.id.infoCardMarker);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        infoWindow.setX(0);
        infoWindow.setY(-200);
        infoWindow.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void getAllPathsCallback(List<Path> paths, String message) {

    }

    @Override
    public void getAllLocationsCallback(List<Location> loactions, String status) {

    }

    @Override
    public void getSpecificLocationCallback(Location loaction, String message) {

    }

    @Override
    public void getSpecificUserCallback(User user, String message) {

    }

    @Override
    public void getAllUsersCallback(List<User> user, String message) {

    }

    @Override
    public void getUserLocationCallback(List<Location> location, String message) {

    }

    @Override
    public void getUserProfileCallback(User user, String message) {
    }

    @Override
    public void getRefreshTokeneCallback(BackendToken token, String message) {
        if(message.equals("OK")){
            userProfile.setRefreshToken(token.getRefreshToken());
            userProfile.setBackendAccessToken(token.getAccessToken());
            SharedPreferences.Editor spEdit = sharedPref.edit();
            spEdit.putString(userProfile.getUserToken() + "_token",token.getAccessToken());
            spEdit.putString(userProfile.getUserToken() + "_token", token.getAccessToken());
            spEdit.commit();
        }
    }

    @Override
    public void getConvertTokenCallback(BackendToken token, String message) {
        if (message.equals("OK")) {
            userProfile.setBackendAccessToken(token.getAccessToken());
            userProfile.setRefreshToken(token.getRefreshToken());
            SharedPreferences.Editor spEdit = sharedPref.edit();
            spEdit.putString(userProfile.getUserToken() + "_token", token.getAccessToken());
            spEdit.putString(userProfile.getUserToken() + "_refresh", token.getRefreshToken());
            spEdit.commit();
        }
    }

    @Override
    public void getAddMessageCallback(String message, String backendCall) {

    }
}
