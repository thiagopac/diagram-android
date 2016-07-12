package club.diagram.chess;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import club.chess.GameControl;
import club.chess.JNI;
import club.chess.PGNColumns;
import club.chess.board.BoardConstants;

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;


public class main extends ChessActivity implements OnInitListener, GestureDetector.OnGestureListener {

    public static final String TAG = "main";

    /**
     * instances for the view and game of chess *
     */
    private ChessView _chessView;

    private long _lGameID;
    private float _fGameRating;

    private Uri _uriNotification;
    private String _keyboardBuffer;
    private TextToSpeech _speech = null;

    private GestureDetector _gestureDetector;

    private boolean _skipReturn;
    public JNI _jni;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers());

        _uriNotification = null;

        setContentView(R.layout.main);
        getActionBar().hide(); // escondendo a action bar na tela principal

        this.makeActionOverflowMenuShown();

        SharedPreferences prefs = this.getPrefs();

        if (prefs.getBoolean("speechNotification", false)) {
            try {
                _speech = new TextToSpeech(this, this);
            } catch (Exception ex){
                _speech = null;
            }
        } else {
            _speech = null;
        }

        _chessView = new ChessView(this);
        _keyboardBuffer = "";

        _lGameID = 0;
        _fGameRating = 2.5F;

        _gestureDetector = new GestureDetector(this, this);

        float dimval =  getResources().getDimension(R.dimen.device_loaded);
        String strDimval = Float.toString(dimval);
        Log.d("device_loaded", strDimval);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_barmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setGameMode(int mode) {

        _jni = new JNI();

        SharedPreferences.Editor editor = this.getPrefs().edit();


        int turn = _jni.getTurn();

        if (mode == 0){ //white

            editor.putBoolean("playAsBlack", false);
            _chessView.setPlayMode(GameControl.HUMAN_PC);

            if(_chessView.getFlippedBoard() == true){
                _chessView.flipBoard();
            }

            if(turn == BoardConstants.BLACK){
                _chessView.play();
            }


        }else if (mode == 1){ //black

            editor.putBoolean("playAsBlack", true);
            _chessView.setPlayMode(GameControl.HUMAN_PC);


            if(_chessView.getFlippedBoard() == false){
                _chessView.flipBoard();
            }

            if(turn == BoardConstants.WHITE){
                _chessView.play();
            }

        }else if (mode == 2){ //both

            editor.putInt("playMode", GameControl.HUMAN_HUMAN);
            _chessView.setPlayMode(GameControl.HUMAN_HUMAN);

        }


        editor.putInt("levelMode", GameControl.LEVEL_TIME);
        editor.putInt("level", 3);
        editor.putInt("levelPly", 10);

        editor.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        String s;
        switch (item.getItemId()) {
            case R.id.action_scan:
                Log.i(TAG, "clicado em 1");
                intent = new Intent();
                intent.setClass(main.this, QrcodeScanner.class);
                startActivity(intent);
                return true;
            case R.id.action_white:
                Log.i(TAG, "clicado em 2");
                setGameMode(0);
                return true;
            case R.id.action_black:
                Log.i(TAG, "clicado em 3");
                setGameMode(1);
                return true;
            case R.id.action_both:
                Log.i(TAG, "clicado em 4");
                setGameMode(2);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean isDown = action == 0;

        if(_skipReturn && keyCode == KeyEvent.KEYCODE_ENTER){  // skip enter key
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return isDown ? this.onKeyDown(keyCode, event) : this.onKeyUp(keyCode, event);
        }

        return super.dispatchKeyEvent(event);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {

        //View v = getWindow().getCurrentFocus();
        //Log.i("main", "current focus " + (v == null ? "NULL" : v.toString()));
        int c = (event.getUnicodeChar());
        Log.i("main", "onKeyDown " + keyCode + " = " + (char)c);
        if(keyCode == KeyEvent.KEYCODE_MENU){
            //showMenu();
            return true;
        }

        // preference is to skip a carriage return
        if(_skipReturn && (char)c == '\r'){
            return true;
        }

        if(c > 48 && c < 57 || c > 96 && c < 105){
            _keyboardBuffer += ("" + (char)c);
        }
        if(_keyboardBuffer.length() >= 2){
            Log.i("main", "handleClickFromPositionString " + _keyboardBuffer);
            _chessView.handleClickFromPositionString(_keyboardBuffer);
            _keyboardBuffer = "";
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     *
     */


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {

        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        SharedPreferences prefs = this.getPrefs();

        _skipReturn = prefs.getBoolean("skipReturn", true);

        String sPGN = "";
        String sFEN = prefs.getString("FEN", null);

        String sTmp = prefs.getString("NotificationUri", null);
        if (sTmp == null) {
            _uriNotification = null;
        } else {
            _uriNotification = Uri.parse(sTmp);
        }

        final Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri uri = intent.getData();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            _lGameID = 0;
            Log.i("onResume", "action send with type " + type);
            if ("application/x-chess-pgn".equals(type)) {
                sPGN = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sPGN != null) {
                    sPGN = sPGN.trim();
                    loadPGN(sPGN);
                }
            } else {
                sFEN = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sFEN != null) {
                    sFEN = sFEN.trim();
                    loadFEN(sFEN);
                }
            }
        } else if (uri != null) {

            _lGameID = 0;
            sPGN = "";
            Log.i("onResume", "opening " + uri.toString());
            InputStream is;
            try {
                is = getContentResolver().openInputStream(uri);
                byte[] b = new byte[4096];
                int len;

                while ((len = is.read(b)) > 0) {
                    sPGN += new String(b);
                }
                is.close();

                sPGN = sPGN.trim();

                loadPGN(sPGN);

            } catch (Exception e) {
                Log.e("onResume", "Failed " + e.toString());
            }
        } else if (sFEN != null) {
            // default, from prefs
            Log.i("onResume", "Loading FEN " + sFEN);
            _lGameID = 0;
            loadFEN(sFEN);
        } else {
            _lGameID = prefs.getLong("game_id", 0);
            if (_lGameID > 0) {
                Log.i("onResume", "loading saved game " + _lGameID);
                loadGame();
            } else {
                sPGN = prefs.getString("game_pgn", null);
                Log.i("onResume", "pgn: " + sPGN);
                loadPGN(sPGN);
            }
        }


        String urlGame = "";
        Uri valor = getIntent().getData();

        if (valor != null){
            urlGame = valor.getQueryParameter("v");
        }

        Bundle extras = intent.getExtras();


        if(extras != null || !urlGame.equals("")) {

            String s = "";

            String qrCodeGame = extras.getString("GAME_CODE");

            if (qrCodeGame != null && !qrCodeGame.equals("")){
                s = qrCodeGame;
            }else if(urlGame != null && !urlGame.equals("")){
                s = urlGame;
            }

            SharedPreferences.Editor editor = this.getPrefs().edit();
            editor.putInt("playMode", GameControl.HUMAN_HUMAN);
            _chessView.setPlayMode(GameControl.HUMAN_HUMAN);

            _jni = new JNI();

            if (s != "") {

                _chessView.clearPGNView();

                if (s.indexOf("1.") >= 0) {
                    //se for pgn

                    if (qrCodeGame != null && !qrCodeGame.equals("")){
                        onKeyMetric("PGN QR-Code scanned");
                    }else if(urlGame != null && !urlGame.equals("")){
                        onKeyMetric("PGN URL clicked");
                    }

                    loadPGN(s);
                } else {
                    //se for fen

                    if (qrCodeGame != null && !qrCodeGame.equals("")){
                        onKeyMetric("FEN QR-Code scanned");
                    }else if(urlGame != null && !urlGame.equals("")){
                        onKeyMetric("FEN URL clicked");
                    }

                    loadFEN(s);
                }

                s = "";
                qrCodeGame = "";
                urlGame = "";
                intent.putExtra("GAME_CODE", s);
            }
        }

        _chessView.jumptoMove(_chessView.getArrPGNSize()+1);
//        _chessView.jumptoMove(0);
        _chessView.OnResume(prefs);
        _chessView.updateState();
        super.onResume();
    }

    public void onKeyMetric(String event) {
        Answers.getInstance().logCustom(new CustomEvent(event));
    }


    @Override
    protected void onPause() {
        //Debug.stopMethodTracing();

        if (_lGameID > 0) {
            ContentValues values = new ContentValues();

            values.put(PGNColumns.DATE, _chessView.getDate().getTime());
            values.put(PGNColumns.WHITE, _chessView.getWhite());
            values.put(PGNColumns.BLACK, _chessView.getBlack());
            values.put(PGNColumns.PGN, _chessView.exportFullPGN());
            values.put(PGNColumns.RATING, _fGameRating);
            values.put(PGNColumns.EVENT, _chessView.getPGNHeadProperty("Event"));

            saveGame(values, false);
        }
        SharedPreferences.Editor editor = this.getPrefs().edit();
        editor.putLong("game_id", _lGameID);
        editor.putString("game_pgn", _chessView.exportFullPGN());
        editor.putString("FEN", null); // 
        if (_uriNotification == null)
            editor.putString("NotificationUri", null);
        else
            editor.putString("NotificationUri", _uriNotification.toString());
        _chessView.OnPause(editor);

        editor.commit();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _chessView.OnDestroy();
        super.onDestroy();
    }

    private void loadFEN(String sFEN) {
        if (sFEN != null) {
            Log.i("loadFEN", sFEN);
            if (false == _chessView.initFEN(sFEN, true)) {
                doToast(getString(R.string.err_load_fen));
                Log.e("loadFEN", "FAILED");
            }
            _chessView.updateState();
        }
    }

    private void loadPGN(String sPGN) {
        if (sPGN != null) {
            if (false == _chessView.loadPGN(sPGN)) {
                doToast(getString(R.string.err_load_pgn));
            }
            _chessView.updateState();
        }
    }

    private void loadGame() {
        if (_lGameID > 0) {
            Uri uri = ContentUris.withAppendedId(MyPGNProvider.CONTENT_URI, _lGameID);
            Cursor c = managedQuery(uri, PGNColumns.COLUMNS, null, null, null);
            if (c != null && c.getCount() == 1) {

                c.moveToFirst();

                _lGameID = c.getLong(c.getColumnIndex(PGNColumns._ID));
                String sPGN = c.getString(c.getColumnIndex(PGNColumns.PGN));
                _chessView.loadPGN(sPGN);

                _chessView.setPGNHeadProperty("Event", c.getString(c.getColumnIndex(PGNColumns.EVENT)));
                _chessView.setPGNHeadProperty("White", c.getString(c.getColumnIndex(PGNColumns.WHITE)));
                _chessView.setPGNHeadProperty("Black", c.getString(c.getColumnIndex(PGNColumns.BLACK)));
                _chessView.setDateLong(c.getLong(c.getColumnIndex(PGNColumns.DATE)));

                _fGameRating = c.getFloat(c.getColumnIndex(PGNColumns.RATING));

            } else {
                _lGameID = 0; // probably deleted
            }
        } else {
            _lGameID = 0;
        }
    }


    // 
    public void saveGame() {
        String sEvent = _chessView.getPGNHeadProperty("Event");
        if (sEvent == null)
            sEvent = "event ?";
        String sWhite = _chessView.getWhite();
        if (sWhite == null)
            sWhite = "white ?";
        String sBlack = _chessView.getBlack();
        if (sBlack == null)
            sBlack = "black ?";

        Date dd = _chessView.getDate();
        if (dd == null)
            dd = Calendar.getInstance().getTime();

        Calendar cal = Calendar.getInstance();
        cal.setTime(dd);
    }

    public void saveGame(ContentValues values, boolean bCopy) {

        SharedPreferences.Editor editor = this.getPrefs().edit();
        editor.putString("FEN", null);
        editor.commit();

        _chessView.setPGNHeadProperty("Event", (String) values.get(PGNColumns.EVENT));
        _chessView.setPGNHeadProperty("White", (String) values.get(PGNColumns.WHITE));
        _chessView.setPGNHeadProperty("Black", (String) values.get(PGNColumns.BLACK));
        _chessView.setDateLong((Long) values.get(PGNColumns.DATE));

        _fGameRating = (Float) values.get(PGNColumns.RATING);
        //

        if (_lGameID > 0 && (bCopy == false)) {
            Uri uri = ContentUris.withAppendedId(MyPGNProvider.CONTENT_URI, _lGameID);
            getContentResolver().update(uri, values, null, null);
        } else {
            Uri uri = MyPGNProvider.CONTENT_URI;
            Uri uriInsert = getContentResolver().insert(uri, values);
            Cursor c = managedQuery(uriInsert, new String[]{PGNColumns._ID}, null, null, null);
            if (c != null && c.getCount() == 1) {
                c.moveToFirst();
                _lGameID = c.getLong(c.getColumnIndex(PGNColumns._ID));
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = _speech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                doToast("Speech does not support US locale");
                _speech = null;
            } else {
                _speech.setSpeechRate(0.80F);
                _speech.setPitch(0.85F);
            }
        } else {
            doToast("Speech not supported");
            _speech = null;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        //Log.i("main", "onFling " + motionEvent.getX() + " " + motionEvent1.getX());

        int Xdiff = (int) motionEvent.getX() - (int) motionEvent1.getX();
        int Ydiff = (int) motionEvent.getY() - (int) motionEvent1.getY();

        if (Xdiff < -150) {
            //Log.i("main", "ButNext");
            _chessView.next();
        }

        if (Xdiff > 150) {
            //Log.i("main", "ButPrevious");
            _chessView.previous();
        }

        if (Ydiff > 150 || Ydiff < -150) {
            //Log.i("main", "flipBoard");
            _chessView.flipBoard();
        }
        return true;
    }

}