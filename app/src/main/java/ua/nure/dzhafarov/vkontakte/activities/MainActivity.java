package ua.nure.dzhafarov.vkontakte.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.models.LongPoll;
import ua.nure.dzhafarov.vkontakte.services.UpdateService;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class MainActivity extends AppCompatActivity {

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
    private ProgressBar progressBar;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getAccessToken();
        long time = getExpiresTime();

        if (token.equals(EMPTY_ACCESS_TOKEN) || System.currentTimeMillis() > time) {
            setContentView(R.layout.activity_main);

            webView = (WebView) findViewById(R.id.authorize_web_view);
            webView.setWebViewClient(webViewClient);
            webView.setVisibility(View.GONE);

            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            title = (TextView) findViewById(R.id.title);
            title.setVisibility(View.VISIBLE);

            webView.loadUrl(authorizeUrl);
        } else {
            startInitialActivity();
        }
    }

    private WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
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
                startInitialActivity();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            progressBar.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    };

    private void startInitialActivity() {
        final VKManager vkManager = VKManager.getInstance();
        
        vkManager.initialize(this, getAccessToken(), new OperationListener<LongPoll>() {
            @Override
            public void onSuccess(LongPoll longPoll) {
                Intent intent = new Intent(MainActivity.this, ActivityListFriends.class);
                startActivity(intent);
                finish();
                
                Intent serviceIntent = new Intent(MainActivity.this, UpdateService.class);
                startService(serviceIntent);
            }

            @Override
            public void onFailure(final String message) {
                runOnUiThread(
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
