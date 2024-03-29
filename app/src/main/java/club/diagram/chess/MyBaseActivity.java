package club.diagram.chess;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.Field;

public class MyBaseActivity extends android.app.Activity{

	protected PowerManager.WakeLock _wakeLock;
	protected Tracker _tracker;
	public static final String TAG = "MyBaseActivity";
	private long _onResumeTimeMillies = 0;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.prepareWindowSettings();

		_wakeLock = this.getWakeLock();

		MyApplication application = (MyApplication) getApplication();
		_tracker = application .getDefaultTracker();
//		_tracker.setScreenName(TAG);
//		_tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	protected void onResume() {

		SharedPreferences prefs = this.getPrefs();

		if(prefs.getBoolean("wakeLock", true)) {
			_wakeLock.acquire();
		}

		_onResumeTimeMillies = System.currentTimeMillis();

		super.onResume();
	}

	@Override
	protected void onPause() {
		if (_wakeLock.isHeld()) {
			_wakeLock.release();
		}
		
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// API 5+ solution
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void makeActionOverflowMenuShown(Activity activity){
		//devices with hardware menu button (e.g. Samsung Note) don't show action overflow menu
		try {
			ViewConfiguration config = ViewConfiguration.get(activity);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			Log.d("main", e.getLocalizedMessage());
		}
	}

	public void makeActionOverflowMenuShown() {
		makeActionOverflowMenuShown(this);
	}

	public static SharedPreferences getPrefs(Activity activity){
		return activity.getSharedPreferences("ChessPlayer", Activity.MODE_PRIVATE);
	}

	public SharedPreferences getPrefs(){
		return getPrefs(this);
	}

	public static void prepareWindowSettings(Activity activity) {
		SharedPreferences prefs = getPrefs(activity);

//		Comentado para não esconder a barra de status com relógio e pushes
//		if(prefs.getBoolean("fullScreen", true)){
//			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		}

        try {
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(Exception ex){

        }

	}
	public void prepareWindowSettings(){
		prepareWindowSettings(this);
	}

	public PowerManager.WakeLock getWakeLock(){
		PowerManager pm = (PowerManager) this.getSystemService(Activity.POWER_SERVICE);
		return pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DoNotDimScreen");
	}

	public void doToast(final String text){
		Toast t = Toast.makeText(this, text, Toast.LENGTH_LONG);
		t.setGravity(Gravity.BOTTOM, 0, 0);
		t.show();
	}

	public void trackEvent(String category, String action){
		_tracker.send(new HitBuilders.EventBuilder()
				.setCategory(category)
				.setAction(action)
				.build());
	}

	public void trackEvent(String category, String action, String label){
		_tracker.send(new HitBuilders.EventBuilder()
				.setCategory(category)
				.setAction(action)
				.setLabel(label)
				.build());
	}
}
