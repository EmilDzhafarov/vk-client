package ua.nure.dzhafarov.vkontakte.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentListCommunities;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentListPhotos;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentListUsers;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.services.UpdateService;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class MainActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String ACCESS_TOKEN = "access_token";
    private static final String EMPTY_ACCESS_TOKEN = "empty_access_token";
    private static final String EXPIRES_TIME = "expires_time";
    private static final long EMPTY_EXPIRES_TIME = -1;

    private final String authorizeUrl = Uri.parse("https://oauth.vk.com/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", "6175897")
            .appendQueryParameter("display", "mobile")
            .appendQueryParameter("redirect", "https://oauth.vk.com/blank.html")
            .appendQueryParameter("scope", "friends,photos,audio,video,pages,status,messages,wall")
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("v", "5.68")
            .build()
            .toString();

    private WebView webView;
    private LinearLayout linearLayoutLoading;

    private TextView fullName;
    private TextView isOnline;
    private CircleImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        String token = getAccessToken();
        long time = getExpiresTime();

        webView = (WebView) findViewById(R.id.authorize_web_view);
        webView.setWebViewClient(webViewClient);
        linearLayoutLoading = (LinearLayout) findViewById(R.id.linear_layout_loading);

        View header = navigationView.getHeaderView(0);
        fullName = (TextView) header.findViewById(R.id.header_user_full_name);
        isOnline = (TextView) header.findViewById(R.id.header_user_is_online);
        photo = (CircleImageView) header.findViewById(R.id.header_user_photo);
        
        if (token.equals(EMPTY_ACCESS_TOKEN) || System.currentTimeMillis() > time) {
            webView.setVisibility(View.GONE);
            linearLayoutLoading.setVisibility(View.VISIBLE);
            webView.loadUrl(authorizeUrl);
        } else {
            startSession();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragments = getSupportFragmentManager().getBackStackEntryCount();

            if (fragments == 1) {
                finish();
            } else {
                if (getFragmentManager().getBackStackEntryCount() > 1) {
                    getFragmentManager().popBackStack();
                } else {
                    super.onBackPressed();
                }
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_communities) {
            addFragment(new FragmentListCommunities());
        } else if (id == R.id.nav_photos) {
            addFragment(new FragmentListPhotos());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    
    private WebViewClient webViewClient = new WebViewClient() {
        
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            String tokenFragment = "#access_token=";
            String tokenUntil = "&expires_in=";
            String expiresInUntil = "&user_id";

            int startToken = url.indexOf(tokenFragment);
            int endToken = url.indexOf(tokenUntil);

            int startExpiresIn = url.indexOf(tokenUntil);
            int endExpiresIn = url.indexOf(expiresInUntil);

            if (startToken > -1 && startExpiresIn > -1) {
                String accessToken = url.substring(startToken + tokenFragment.length(), endToken);

                String expiresInStr =
                        url.substring(startExpiresIn + tokenUntil.length(), endExpiresIn);

                int expiresIn = Integer.parseInt(expiresInStr) * 1000;

                saveAccessToken(accessToken);
                saveExpiresTime(System.currentTimeMillis() + expiresIn);

                webView.setVisibility(View.GONE);
                startSession();
            } else {
                linearLayoutLoading.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);   
            }
        }
    };

    private void initializeCurrentUser() {
        if (fullName.getText().toString().isEmpty()) {
            VKManager.getInstance().loadCurrentUser(new OperationListener<User>() {
                @Override
                public void onSuccess(User object) {
                    loadCurrentUserInUI(object);
                }

                @Override
                public void onFailure(final String message) {
                    MainActivity.this.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            });
        }
    }
    
    private void addFragment(Fragment fr) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_host);

        if (fragment == null) {
            fragment = fr;
            fm.beginTransaction().add(R.id.fragment_host, fragment).commit();
        } else {
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_host, fr);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
    
    private void loadCurrentUserInUI(final User currUser) {
        if (currUser != null) {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            fullName.setText(currUser.getFirstName() + " " + currUser.getLastName());
                            isOnline.setText(currUser.isOnline() ? "online" : "offline");
                            Picasso.with(MainActivity.this).load(currUser.getPhotoURL()).into(photo);
                        }
                    }
            );
        }
    }
    
    private void startSession() {
        final VKManager vkManager = VKManager.getInstance();
        vkManager.initialize(this, getAccessToken());
        initializeCurrentUser();
        Intent serviceIntent = new Intent(MainActivity.this, UpdateService.class);
        startService(serviceIntent);
        addFragment(new FragmentListUsers());
    }

    private void saveAccessToken(String accessToken) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    private String getAccessToken() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getString(ACCESS_TOKEN, EMPTY_ACCESS_TOKEN);
    }

    private void saveExpiresTime(long time) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(EXPIRES_TIME, time);
        editor.apply();
    }

    private long getExpiresTime() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getLong(EXPIRES_TIME, EMPTY_EXPIRES_TIME);
    }
    
}
