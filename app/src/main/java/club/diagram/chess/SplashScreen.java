package club.diagram.chess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.os.Handler;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SplashScreen extends Activity {

    private WebView wvGif;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.splash_screen);

        getActionBar().hide();

        wvGif = (WebView) findViewById(R.id.wvGif);
        String page_url = "file:///android_asset/splash_page.html";
        wvGif.loadUrl(page_url);
    }

    private void doStuff() {
        Intent i = new Intent();
        i.setClass(this, main.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                doStuff();
            }
        }, 2500);
    }
}
