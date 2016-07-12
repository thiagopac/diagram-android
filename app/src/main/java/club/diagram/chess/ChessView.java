package club.diagram.chess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import club.chess.Move;
import club.chess.Pos;
import club.chess.board.BoardConstants;
import club.chess.board.BoardMembers;
import club.chess.board.ChessBoard;
import com.wefika.flowlayout.FlowLayout;

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
/**
 *
 */
public class ChessView extends UI {

    public static final String TAG = "ChessView";

    private ChessViewBase _view;

    private ChessActivity _parent;
    private ProgressBar _progressPlay;
    private int _dpadPos;
    private int _playMode;
    private ScrollView _vScrollHistory;
    private FlowLayout _layoutHistory;
    private ArrayList<PGNView> _arrPGNView;
    private LayoutInflater _inflater;
    private boolean _bAutoFlip, _bShowMoves, _bShowLastMove, _bPlayAsBlack;
    private Timer _timer;
    private Vibrator _vibrator;

    private TextView tabuleiro;

    static class InnerHandler extends Handler {
        WeakReference<ChessView> _chessView;

        InnerHandler(ChessView view) {
            _chessView = new WeakReference<ChessView>(view);
        }

        @Override
        public void handleMessage(Message msg) {

            ChessView chessView = _chessView.get();
            if (chessView != null) {
                long lTmp;
                if (chessView._view._flippedBoard) {
                    lTmp = chessView.getBlackRemainClock();
                } else {
                    lTmp = chessView.getWhiteRemainClock();
                }
            }
        }
    }

    protected InnerHandler m_timerHandler = new InnerHandler(this);


    public ChessView(Activity activity) {
        super();
        _parent = (ChessActivity) activity;
        _view = new ChessViewBase(activity);

        Fabric.with(_parent, new Answers());

        _playMode = HUMAN_HUMAN;
        _bAutoFlip = false;
        _bPlayAsBlack = false;
        _bShowMoves = false;
        _bShowLastMove = true;
        _dpadPos = -1;

        _arrPGNView = new ArrayList<PGNView>();

        _inflater = (LayoutInflater) _parent.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        OnClickListener ocl = new OnClickListener() {
            public void onClick(View arg0) {
                handleClick(_view.getIndexOfButton(arg0));
            }
        };

        _vScrollHistory = null;
        _layoutHistory = null;

        _view.init(ocl);

        //_vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
        _vibrator = null;

        // below was previously in init() method
        _layoutHistory = (FlowLayout) _parent.findViewById(R.id.LayoutHistory);
        _vScrollHistory = (ScrollView) _parent.findViewById(R.id.VScrollViewHistory);


        OnClickListener oclUndo = new OnClickListener() {
            public void onClick(View arg0) {
                if (m_bActive) {
                    previous();
                } else {
                    stopThreadAndUndo();
                }
            }
        };

        OnClickListener backAll = new OnClickListener() {
            public void onClick(View view) {
                jumptoMove(1);
                updateState();
            }
        };

        ImageButton butBackAll = (ImageButton) _parent.findViewById(R.id.ButtonBackAll);
        if (butBackAll != null) {
            //butBackAll.setFocusable(false);
            butBackAll.setOnClickListener(backAll);
        }

        ImageButton butPrevious = (ImageButton) _parent.findViewById(R.id.ButtonPrevious);
        if (butPrevious != null) {
            //butPrevious.setFocusable(false);
            butPrevious.setOnClickListener(oclUndo);
        }

        OnClickListener oclFf = new OnClickListener() {
            public void onClick(View arg0) {
                if (m_bActive) {
                    next();
                }

            }
        };

        OnClickListener forwardAll = new OnClickListener() {
            public void onClick(View view) {
                jumptoMove(_layoutHistory.getChildCount());
                updateState();
            }
        };

        OnClickListener flip = new OnClickListener() {
            public void onClick(View view) {
                flipBoard();
            }
        };

        OnClickListener menu = new OnClickListener() {
            public void onClick(View view) {
                _parent.openOptionsMenu();
            }
        };

        ImageButton butNext = (ImageButton) _parent.findViewById(R.id.ButtonNext);
        if (butNext != null) {
            //butNext.setFocusable(false);
            butNext.setOnClickListener(oclFf);
        }

        ImageButton butForwardAll = (ImageButton) _parent.findViewById(R.id.ButtonForwardAll);
        if (butForwardAll != null) {
            //butForwardAll.setFocusable(false);
            butForwardAll.setOnClickListener(forwardAll);
        }

        ImageButton butFlip = (ImageButton) _parent.findViewById(R.id.ButtonFlip);
        if (butFlip != null) {
            //butForwardAll.setFocusable(false);
            butFlip.setOnClickListener(flip);
        }

        ImageButton butMenu = (ImageButton) _parent.findViewById(R.id.ButtonMenu);
        if (butMenu != null) {
            //butForwardAll.setFocusable(false);
            butMenu.setOnClickListener(menu);
        }

        _progressPlay = (ProgressBar) _parent.findViewById(R.id.ProgressBarPlay);
        _selectedLevel = 3;

        _lClockStartWhite = 0;
        _lClockStartBlack = 0;
        _lClockTotal = 0;


        _timer = new Timer(true);
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                m_timerHandler.sendMessage(msg);
            }
        }, 1000, 1000);

    }

    protected void next() {
        jumptoMove(_jni.getNumBoard());
        updateState();
    }

    protected void previous() {
        undo();
    }


    public void onClickPGNView(PGNView item) {
//        Os movimentos PGN não são links para o movimento
//
//        if (m_bActive) {
//            int i = _arrPGNView.indexOf(item);
//            Log.i("onClickPGNView", "index " + i);
//            if (_jni.getNumBoard() - 1 > i)
//                jumptoMove(i + 2);
//            else
//                jumptoMove(i + 1);
//
//            //if(_arrPGN.get(i)._sAnnotation.length() > 0){
//            //_parent.doToast(_arrPGN.get(i)._sMove + " :" + _arrPGN.get(i)._sAnnotation);
//
//            //}
//            updateState();
//        } else {
//            //
//        }
    }

    public void onLongClickPGNView(PGNView item) {

    }

    public void clearPGNView() {
        _arrPGNView.clear();
        if (_layoutHistory != null) {
            _layoutHistory.removeAllViews();
        }
        updateState();
    }

    @Override
    public void newGame() {
        super.newGame();
        clearPGNView();
    }

    @Override
    public int newGameRandomFischer(int seed) {

        int ret = super.newGameRandomFischer(seed);
        clearPGNView();

        return ret;
    }

    @Override
    public void addPGNEntry(int ply, String sMove, String sAnnotation, int move, boolean bScroll) {
        super.addPGNEntry(ply, sMove, sAnnotation, move, bScroll);
        //Log.i("ChessView", "sMove =  " + sMove);


        while (ply >= 0 && _arrPGNView.size() >= ply)
            _arrPGNView.remove(_arrPGN.size() - 1);

        View v = _inflater.inflate(R.layout.pgn_item, null, false);
        v.setId(ply);
        _arrPGNView.add(new PGNView(this, v, ply, sMove, sAnnotation.length() > 0));

        int acrescimo = 0;
        if (ply % 2 != 0){
            acrescimo = (int)(v.getResources().getDimension(R.dimen.pgn_ply_1_width));
        }else{
            acrescimo = (int)(v.getResources().getDimension(R.dimen.pgn_ply_2_width));

        }

//        v.setBackgroundColor(Color.RED);

        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(acrescimo+(sMove.length()*15), (int)(v.getResources().getDimension(R.dimen.pgn_ply_height)));
        params.rightMargin = 2;
        params.leftMargin = 2;
        v.setLayoutParams(params);

        if (_layoutHistory != null) {
            while (ply >= 0 && _layoutHistory.getChildCount() >= ply)
                _layoutHistory.removeViewAt(_layoutHistory.getChildCount() - 1);
                _layoutHistory.addView(v);

        }

        if (bScroll) {
            scrollToEnd();
        }
    }

    @Override
    public void setAnnotation(int i, String sAnno) {
        super.setAnnotation(i, sAnno);

        _arrPGNView.get(i).setAnnotated(sAnno.length() > 0);
        _arrPGNView.get(i).setSelected(false);
    }

    @Override
    public void paintBoard() {

        int[] arrSelPositions;

        int lastMove = _jni.getMyMove();
        if (lastMove != 0 && _bShowLastMove) {
            arrSelPositions = new int[4];
            arrSelPositions[0] = m_iFrom;
            arrSelPositions[1] = Move.getTo(lastMove);
            arrSelPositions[2] = Move.getFrom(lastMove);
            arrSelPositions[3] = _dpadPos;
        } else {
            arrSelPositions = new int[2];
            arrSelPositions[0] = m_iFrom;
            arrSelPositions[1] = _dpadPos;
        }
        int turn = _jni.getTurn();

        ArrayList<Integer> arrPos = new ArrayList<Integer>();
        // collect legal moves if pref is set
        if (_bShowMoves && m_iFrom != -1) {
            try {
                // via try catch because of empty or mem error results in exception

                if (_jni.isEnded() == 0) {
                    synchronized (this) {
                        int size = _jni.getMoveArraySize();
                        //Log.i("paintBoard", "# " + size);
                        int move;
                        for (int i = 0; i < size; i++) {
                            move = _jni.getMoveArrayAt(i);
                            if (Move.getFrom(move) == m_iFrom) {
                                arrPos.add(Move.getTo(move));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.gc();
            }
        }

        _view.paintBoard(_jni, arrSelPositions, arrPos);

        if (_layoutHistory != null) {
            for (int i = 0; i < _layoutHistory.getChildCount(); i++) {
                _arrPGNView.get(i).setSelected(i == _jni.getNumBoard() - 2);
            }
        }
    }

    public int getPlayMode() {
        return _playMode;
    }

    public void flipBoard() {
        _view.flipBoard();
        updateState();
    }


    public void setFlippedBoard(boolean b) {
        _view.setFlippedBoard(b);
    }

    public boolean getFlippedBoard() {
        return _view._flippedBoard;
    }

    @Override
    public void play() {
        if (_jni.isEnded() == 0) {
            if (_progressPlay.getVisibility() == View.VISIBLE) {
                _progressPlay.setVisibility(View.INVISIBLE);
            } else {
                _progressPlay.setVisibility(View.VISIBLE);
            }

        }
        super.play();
    }

    public boolean handleClickFromPositionString(String s) {
        int index = Pos.fromString(s);
        if (_view._flippedBoard) {
            index = 63 - index;
        }
        return handleClick(index);
    }

    @Override
    public boolean handleClick(int index) {
        if (false == m_bActive) {
            setMessage(R.string.msg_wait);
            return false;
        }

        final int iTo = _view.getFieldIndex(index);
        if (m_iFrom != -1) {

            // check if it is a promotion piece
            if (_jni.pieceAt(BoardConstants.WHITE, m_iFrom) == BoardConstants.PAWN &&
                    BoardMembers.ROW_TURN[BoardConstants.WHITE][m_iFrom] == 6 &&
                    BoardMembers.ROW_TURN[BoardConstants.WHITE][iTo] == 7
                    ||
                    _jni.pieceAt(BoardConstants.BLACK, m_iFrom) == BoardConstants.PAWN &&
                            BoardMembers.ROW_TURN[BoardConstants.BLACK][m_iFrom] == 6 &&
                            BoardMembers.ROW_TURN[BoardConstants.BLACK][iTo] == 7) {

                final String[] items = _parent.getResources().getStringArray(R.array.promotionpieces);

                AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
                builder.setTitle(R.string.title_pick_promo);
                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        _jni.setPromo(4 - item);
                        boolean bValid = requestMove(m_iFrom, iTo);
                        m_iFrom = -1;
                        if (false == bValid)
                            paintBoard();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                if (_vibrator != null) {
                    _vibrator.vibrate(40L);
                }

                return true;
            } else if (_jni.isAmbiguousCastle(m_iFrom, iTo) != 0) { // in case of Fischer

                AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
                builder.setTitle(R.string.title_castle);
                builder.setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        requestMoveCastle(m_iFrom, iTo);
                        m_iFrom = -1;
                    }
                });
                builder.setNegativeButton(R.string.alert_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        if (m_iFrom != iTo) {
                            requestMove(m_iFrom, iTo);
                        }
                        m_iFrom = -1;
                        paintBoard();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                if (_vibrator != null) {
                    _vibrator.vibrate(40L);
                }

                return true; // done, return from method!
            }
            //Log.i("ChessView", "====== not a special move");
        }
        // if to is same as from (not in case of Fischer random castle!
        if (m_iFrom == iTo) {
            m_iFrom = -1;
            paintBoard();
            return false;
        }
        if (super.handleClick(iTo)) {
            if (_vibrator != null) {
                _vibrator.vibrate(40L);
            }
            return true;
        }

        return false;
    }

    @Override
    public void setMessage(String sMsg) {
        _parent.doToast(sMsg);
        //_tvMessage.setText(sMsg);
    }

    @Override
    public void setMessage(int res) {
        //_tvMessage.setText(res);
//        _parent.doToast(_parent.getString(res)); // retirado o toast de illegal move
    }

    public void setPlayMode(int mode) {
        _playMode = mode;
    }

    public void OnPause(SharedPreferences.Editor editor) {

        if (_uci.isReady()) {
            _uci.quit();
        } else {
            if (m_bActive == false) {
                _jni.interrupt();
            }
        }

        editor.putBoolean("flippedBoard", _view.getFlippedBoard());
        editor.putInt("levelMode", m_iLevelMode);
        editor.putInt("level", _selectedLevel);
        editor.putInt("levelPly", _selectedLevelPly);
        editor.putInt("playMode", _playMode);
        editor.putBoolean("autoflipBoard", _bAutoFlip);
        editor.putBoolean("showMoves", _bShowMoves);
        editor.putBoolean("playAsBlack", _bPlayAsBlack);
        editor.putInt("boardNum", _jni.getNumBoard());
        pauzeTimer();
        editor.putLong("clockTotalMillies", _lClockTotal);
        editor.putLong("clockWhiteMillies", _lClockWhite);
        editor.putLong("clockBlackMillies", _lClockBlack);
    }

    public void OnResume(SharedPreferences prefs) {
        super.OnResume();
        
        _view.setFlippedBoard(prefs.getBoolean("flippedBoard", false));
        _bAutoFlip = prefs.getBoolean("autoflipBoard", false);
        _bShowMoves = prefs.getBoolean("showMoves", true);
        _bShowLastMove = prefs.getBoolean("showLastMove", true);

        if (_jni.getTurn() == 0)
            _bPlayAsBlack = true;
        else if (_jni.getTurn() == 1){
            _bPlayAsBlack = false;
        }

        setLevelMode(prefs.getInt("levelMode", LEVEL_TIME));
        _selectedLevel = prefs.getInt("level", 3);
        _selectedLevelPly = prefs.getInt("levelPly", 10);
        _playMode = prefs.getInt("playMode", HUMAN_HUMAN);

        if (prefs.getBoolean("onLoadJumpToLastMove", true)) {

        } else {
            jumptoMove(prefs.getInt("boardNum", 0));
        }

        _lClockTotal = prefs.getLong("clockTotalMillies", 0);
        _lClockWhite = prefs.getLong("clockWhiteMillies", 0);
        _lClockBlack = prefs.getLong("clockBlackMillies", 0);
        continueTimer();

        ChessImageView._colorScheme = prefs.getInt("ColorScheme", 0);

        if (_bPlayAsBlack) {
            // playing as black

            if (false == _view.getFlippedBoard()) {
                flipBoard();
            }

            if (_playMode == HUMAN_PC && _jni.getTurn() == ChessBoard.WHITE) {
                play();
            }

        } else {
            // playing as white
            if (_view.getFlippedBoard() == true) {
                flipBoard();
            }
            if (_playMode == HUMAN_PC && _jni.getTurn() == ChessBoard.BLACK) {
                play();
            }
        }
    }


    @Override
    public void updateState() {
        super.updateState();

        if (_progressPlay != null) {
            if (_progressPlay.getVisibility() == View.VISIBLE) {
                if (m_bActive) {
                    _progressPlay.setVisibility(View.INVISIBLE);
                }
            } else {
                if (false == m_bActive) {
                    _progressPlay.setVisibility(View.VISIBLE);
                }
            }
        }

        int state = _jni.getState();
        int res = chessStateToR(state);
        Log.i("getHashKey: ", String.valueOf(_jni.getHashKey()));

        SharedPreferences prefs = this.getPrefs();
        long lastHashKey = prefs.getLong("lastHashKey", 0);

        String strState = _parent.getString(res);

        if (!strState.equals("in play") && !strState.equals("check") && !strState.equals("forfeits on time") && !strState.equals("resigned") && !strState.equals("aborted") && !strState.equals("adjourned")) {

            if(lastHashKey != _jni.getHashKey()) {

                if (strState.equals("draw (material)")) {
                    showAlert("Draw", "No mate possible");
                    onKeyMetric("Draw", "Reason", "No mate possible");
                }
                if (strState.equals("draw (50 move rule)")) {
                    showAlert("Draw", "50 non-reversible moves");
                    onKeyMetric("Draw", "Reason", "50 non-reversible moves");
                }
                if (strState.equals("draw (stalemate)")) {
                    showAlert("Draw", "Stalemate");
                    onKeyMetric("Draw", "Reason", "Stalemate");
                }
                if (strState.equals("draw (3-fold repetition)")) {
                    showAlert("Draw", "3rd repetition");
                    onKeyMetric("Draw", "Reason", "3rd repetition");
                }
                if (strState.equals("checkmated")) {
                    showAlert(_jni.getTurn() == 0 ? "White wins" : "Black wins", "Checkmate");
                    onKeyMetric("Checkmate", "Winner", _jni.getTurn() == 0 ? "White" : "Black");
                }
            }
        }
    }


    public void showAlert(String title, String msg){

        AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        SharedPreferences.Editor editor = _parent.getPrefs().edit();
        editor.putLong("lastHashKey", _jni.getHashKey());
        editor.commit();
    }


    public static SharedPreferences getPrefs(Activity activity){
        return activity.getSharedPreferences("ChessPlayer", Activity.MODE_PRIVATE);
    }

    public SharedPreferences getPrefs(){
        return getPrefs(_parent);
    }

    public void scrollToEnd() {
        if (_vScrollHistory != null) {
            _vScrollHistory.post(new Runnable() {
                public void run() {
                    _vScrollHistory.fullScroll(ScrollView.FOCUS_DOWN);
                }

            });
        }
    }


    public boolean hasVerticalScroll() {
        return (_vScrollHistory != null);
    }

    protected void dpadFirst() {
        if (_dpadPos == -1) {
            _dpadPos = _jni.getTurn() == ChessBoard.BLACK ? ChessBoard.e8 : ChessBoard.e1;
        }
    }

    protected void onKeyMetric(String event, String key, String value) {
        Answers.getInstance().logCustom(new CustomEvent(event)
                .putCustomAttribute(key, value));
    }

}
