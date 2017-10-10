package ua.nure.dzhafarov.vkontakte.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public abstract class SingleFragmentActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {
    
    protected abstract Fragment createFragment();
    
    private TextView fullName;
    private TextView isOnline;
    private CircleImageView photo;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        View header = navigationView.getHeaderView(0);
        fullName = (TextView) header.findViewById(R.id.header_user_full_name);
        isOnline = (TextView) header.findViewById(R.id.header_user_is_online);
        photo = (CircleImageView) header.findViewById(R.id.header_user_photo);
        
        if (fullName.getText().toString().isEmpty()) {
            VKManager.getInstance().loadCurrentUser(new OperationListener<User>() {
                @Override
                public void onSuccess(User object) {
                    loadCurrentUserInUI(object);
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(SingleFragmentActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });    
        }
        
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_host);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_host, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    public void loadCurrentUserInUI(final User currUser) {
        if (currUser != null) {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            fullName.setText(currUser.getFirstName() + " " + currUser.getLastName());
                            isOnline.setText(currUser.isOnline() ? "online" : "offline");
                            Picasso.with(SingleFragmentActivity.this).load(currUser.getPhotoURL()).into(photo);
                        }
                    }
            );   
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.initial, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        
        int id = item.getItemId();
        
        if (id == R.id.nav_communities) {
            Intent intent = new Intent(this, ActivityListCommunities.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
