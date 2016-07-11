package club.diagram.chess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.os.Handler;

public class SplashScreen extends Activity {

    private WebView wvGif;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        getActionBar().hide(); // escondendo a action bar na tela principal

        wvGif = (WebView) findViewById(R.id.wvGif);

        String page_url = "file:///android_asset/splash_page.html";
        wvGif.loadUrl(page_url);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                doStuff();
            }
        }, 2500);
    }

    private void doStuff() {
        Intent i = new Intent();
        i.setClass(this, main.class);
        startActivity(i);
    }
}
