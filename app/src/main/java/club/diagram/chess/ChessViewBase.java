package club.diagram.chess;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TableRow.LayoutParams;

import java.util.ArrayList;

import club.chess.JNI;
import club.chess.Pos;
import club.chess.board.BoardConstants;
import club.chess.board.ChessBoard;

/**
 *
 */
public class ChessViewBase{

	public static final String TAG = "ChessViewBase";

	private RelativeLayout _mainLayout;
	private ChessImageView[] _arrImages = new ChessImageView[64];
	//public static final int SELECTED = 2;
	protected boolean _flippedBoard;
	protected Activity _activity;
	protected ImageCacheObject[] _arrImgCache;

	//protected ImageView _imgOverlay;


	public ChessViewBase(Activity activity) {
		_activity = activity;
		_arrImgCache = new ImageCacheObject[64];
	}

	public void init(OnClickListener ocl){
		Log.i("ChessViewBase", "init() called");
		_flippedBoard = false;

		_mainLayout = (RelativeLayout)_activity.findViewById(R.id.LayoutMain);

		_arrImages[0] = (ChessImageView)_activity.findViewById(R.id.a8);
		_arrImages[1] = (ChessImageView)_activity.findViewById(R.id.b8);
		_arrImages[2] = (ChessImageView)_activity.findViewById(R.id.c8);
		_arrImages[3] = (ChessImageView)_activity.findViewById(R.id.d8);
		_arrImages[4] = (ChessImageView)_activity.findViewById(R.id.e8);
		_arrImages[5] = (ChessImageView)_activity.findViewById(R.id.f8);
		_arrImages[6] = (ChessImageView)_activity.findViewById(R.id.g8);
		_arrImages[7] = (ChessImageView)_activity.findViewById(R.id.h8);

		_arrImages[8] = (ChessImageView)_activity.findViewById(R.id.a7);
		_arrImages[9] = (ChessImageView)_activity.findViewById(R.id.b7);
		_arrImages[10] = (ChessImageView)_activity.findViewById(R.id.c7);
		_arrImages[11] = (ChessImageView)_activity.findViewById(R.id.d7);
		_arrImages[12] = (ChessImageView)_activity.findViewById(R.id.e7);
		_arrImages[13] = (ChessImageView)_activity.findViewById(R.id.f7);
		_arrImages[14] = (ChessImageView)_activity.findViewById(R.id.g7);
		_arrImages[15] = (ChessImageView)_activity.findViewById(R.id.h7);

		_arrImages[16] = (ChessImageView)_activity.findViewById(R.id.a6);
		_arrImages[17] = (ChessImageView)_activity.findViewById(R.id.b6);
		_arrImages[18] = (ChessImageView)_activity.findViewById(R.id.c6);
		_arrImages[19] = (ChessImageView)_activity.findViewById(R.id.d6);
		_arrImages[20] = (ChessImageView)_activity.findViewById(R.id.e6);
		_arrImages[21] = (ChessImageView)_activity.findViewById(R.id.f6);
		_arrImages[22] = (ChessImageView)_activity.findViewById(R.id.g6);
		_arrImages[23] = (ChessImageView)_activity.findViewById(R.id.h6);

		_arrImages[24] = (ChessImageView)_activity.findViewById(R.id.a5);
		_arrImages[25] = (ChessImageView)_activity.findViewById(R.id.b5);
		_arrImages[26] = (ChessImageView)_activity.findViewById(R.id.c5);
		_arrImages[27] = (ChessImageView)_activity.findViewById(R.id.d5);
		_arrImages[28] = (ChessImageView)_activity.findViewById(R.id.e5);
		_arrImages[29] = (ChessImageView)_activity.findViewById(R.id.f5);
		_arrImages[30] = (ChessImageView)_activity.findViewById(R.id.g5);
		_arrImages[31] = (ChessImageView)_activity.findViewById(R.id.h5);

		_arrImages[32] = (ChessImageView)_activity.findViewById(R.id.a4);
		_arrImages[33] = (ChessImageView)_activity.findViewById(R.id.b4);
		_arrImages[34] = (ChessImageView)_activity.findViewById(R.id.c4);
		_arrImages[35] = (ChessImageView)_activity.findViewById(R.id.d4);
		_arrImages[36] = (ChessImageView)_activity.findViewById(R.id.e4);
		_arrImages[37] = (ChessImageView)_activity.findViewById(R.id.f4);
		_arrImages[38] = (ChessImageView)_activity.findViewById(R.id.g4);
		_arrImages[39] = (ChessImageView)_activity.findViewById(R.id.h4);

		_arrImages[40] = (ChessImageView)_activity.findViewById(R.id.a3);
		_arrImages[41] = (ChessImageView)_activity.findViewById(R.id.b3);
		_arrImages[42] = (ChessImageView)_activity.findViewById(R.id.c3);
		_arrImages[43] = (ChessImageView)_activity.findViewById(R.id.d3);
		_arrImages[44] = (ChessImageView)_activity.findViewById(R.id.e3);
		_arrImages[45] = (ChessImageView)_activity.findViewById(R.id.f3);
		_arrImages[46] = (ChessImageView)_activity.findViewById(R.id.g3);
		_arrImages[47] = (ChessImageView)_activity.findViewById(R.id.h3);

		_arrImages[48] = (ChessImageView)_activity.findViewById(R.id.a2);
		_arrImages[49] = (ChessImageView)_activity.findViewById(R.id.b2);
		_arrImages[50] = (ChessImageView)_activity.findViewById(R.id.c2);
		_arrImages[51] = (ChessImageView)_activity.findViewById(R.id.d2);
		_arrImages[52] = (ChessImageView)_activity.findViewById(R.id.e2);
		_arrImages[53] = (ChessImageView)_activity.findViewById(R.id.f2);
		_arrImages[54] = (ChessImageView)_activity.findViewById(R.id.g2);
		_arrImages[55] = (ChessImageView)_activity.findViewById(R.id.h2);

		_arrImages[56] = (ChessImageView)_activity.findViewById(R.id.a1);
		_arrImages[57] = (ChessImageView)_activity.findViewById(R.id.b1);
		_arrImages[58] = (ChessImageView)_activity.findViewById(R.id.c1);
		_arrImages[59] = (ChessImageView)_activity.findViewById(R.id.d1);
		_arrImages[60] = (ChessImageView)_activity.findViewById(R.id.e1);
		_arrImages[61] = (ChessImageView)_activity.findViewById(R.id.f1);
		_arrImages[62] = (ChessImageView)_activity.findViewById(R.id.g1);
		_arrImages[63] = (ChessImageView)_activity.findViewById(R.id.h1);



		//_imgOverlay = (ImageView)_activity.findViewById(R.id.ImageBoardOverlay);

		AssetManager am = _activity.getAssets();
		SharedPreferences prefs = _activity.getSharedPreferences("ChessPlayer", Activity.MODE_PRIVATE);

		//String sFolder = prefs.getString("pieceSet", "highres") + "/";
        String sFolder = "highres/";
		String sPat  	= prefs.getString("tileSet", "");

		try{

            if(prefs.getBoolean("extrahighlight", false)) {
                ChessImageView._bmpBorder = BitmapFactory.decodeStream(am.open(sFolder + "border.png"));
            } else {
                ChessImageView._bmpBorder = null;
            }

			ChessImageView._bmpSelect = BitmapFactory.decodeStream(am.open(sFolder + "select.png"));
			ChessImageView._bmpSelectLight = BitmapFactory.decodeStream(am.open(sFolder + "select_light.png"));

			if(sPat.length() > 0){
				ChessImageView._bmpTile = BitmapFactory.decodeStream(am.open("tiles/" + sPat + ".png"));
			} else {
				ChessImageView._bmpTile = null;
			}
			// pawn
			ChessImageView._arrPieceBitmaps[ChessBoard.BLACK][BoardConstants.PAWN] = BitmapFactory.decodeStream(am.open(sFolder + "pb.png"));
			ChessImageView._arrPieceBitmaps[ChessBoard.WHITE][BoardConstants.PAWN] = BitmapFactory.decodeStream(am.open(sFolder + "pw.png"));

			// kNight
			ChessImageView._arrPieceBitmaps[ChessBoard.BLACK][BoardConstants.KNIGHT] = BitmapFactory.decodeStream(am.open(sFolder + "nb.png"));
			ChessImageView._arrPieceBitmaps[ChessBoard.WHITE][BoardConstants.KNIGHT] = BitmapFactory.decodeStream(am.open(sFolder + "nw.png"));

			// bishop
			ChessImageView._arrPieceBitmaps[ChessBoard.BLACK][BoardConstants.BISHOP] = BitmapFactory.decodeStream(am.open(sFolder + "bb.png"));
			ChessImageView._arrPieceBitmaps[ChessBoard.WHITE][BoardConstants.BISHOP] = BitmapFactory.decodeStream(am.open(sFolder + "bw.png"));

			// rook
			ChessImageView._arrPieceBitmaps[ChessBoard.BLACK][BoardConstants.ROOK] = BitmapFactory.decodeStream(am.open(sFolder + "rb.png"));
			ChessImageView._arrPieceBitmaps[ChessBoard.WHITE][BoardConstants.ROOK] = BitmapFactory.decodeStream(am.open(sFolder + "rw.png"));

			// queen
			ChessImageView._arrPieceBitmaps[ChessBoard.BLACK][BoardConstants.QUEEN] = BitmapFactory.decodeStream(am.open(sFolder + "qb.png"));
			ChessImageView._arrPieceBitmaps[ChessBoard.WHITE][BoardConstants.QUEEN] = BitmapFactory.decodeStream(am.open(sFolder + "qw.png"));

			// king
			ChessImageView._arrPieceBitmaps[ChessBoard.BLACK][BoardConstants.KING] = BitmapFactory.decodeStream(am.open(sFolder + "kb.png"));
			ChessImageView._arrPieceBitmaps[ChessBoard.WHITE][BoardConstants.KING] = BitmapFactory.decodeStream(am.open(sFolder + "kw.png"));

		}catch(Exception ex){

		}

		// yellow
		//TEMA PADRÃO CINZA CLARO
		ChessImageView._arrColorScheme[0][0] = 0xffECECF0; //cinza claro
		ChessImageView._arrColorScheme[0][1] = 0xffffffff; //branco
		ChessImageView._arrColorScheme[0][2] = 0xaaf3f372; //amarelo

		for(int i = 0; i < 64; i++){
			_arrImages[i].setOnClickListener(ocl);
			//_arrImages[i].setFocusable(false);

			_arrImgCache[i] = new ImageCacheObject();
		}

		final View layout = (View) _activity.getWindow().getDecorView().findViewById(android.R.id.content);
		ViewTreeObserver vto = layout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				adjustWidth();
			}
		});
	}

	private void adjustWidth(){

		ChessImageView._matrix = null;

		final Window window = _activity.getWindow();
		final View v = window.getDecorView();

		v.post(new Runnable() {
			@Override
			public void run() {
				Rect rectangle = new Rect();
				v.getWindowVisibleDisplayFrame(rectangle);
				int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
				//int titleBarHeight= contentViewTop - statusBarHeight;
				int availableHeight = (rectangle.bottom - rectangle.top) - contentViewTop;
				int availableWidth = rectangle.right - rectangle.left;
				int length, margin = 0;

				// portrait
				if (availableHeight > availableWidth) {
					length = availableWidth / 8;
					margin = (availableWidth - 8 * length) / 2;
				} else {
					length = availableHeight / 8;
				}

				if (margin > 0) {
					View viewBoard = (View) _activity.findViewById(R.id.includeboard);
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewBoard.getLayoutParams();
					params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
					viewBoard.setLayoutParams(params);
                }

				LayoutParams params = new LayoutParams(length, length);
				for (int i = 0; i < 64; i++) {
					_arrImages[i].setLayoutParams(params);
				}
			}
		});
	}

	public int getIndexOfButton(View but){
		for(int i = 0; i < 64; i++){
			if(_arrImages[i] == ((ChessImageView)but)){

				_arrImages[i].setPressed(false);

				return i;
			}
		}
		return -1;
	}

	public void paintBoard(JNI jni, int[] arrSelPositions, ArrayList<Integer> arrPos){
		boolean bPiece, bSelected, bSelectedPosition;

		int i, iResource, iPiece = BoardConstants.PAWN, iColor = ChessBoard.BLACK, iFieldColor;


		// Before and after this method
		System.gc();

		ImageCacheObject._flippedBoard = _flippedBoard;

		for(i = 0; i < 64; i++){
			_arrImages[i].setPressed(false);
			_arrImages[i].setSelected(false);
		}

		ImageCacheObject tmpCache;

		for(i = 0; i < 64; i++)
		{
			bPiece = true;
			bSelected = false;
			bSelectedPosition = false;

			iFieldColor = (i&1)==0 ? (((i >> 3) & 1) == 0 ? ChessBoard.WHITE : ChessBoard.BLACK) : (((i >> 3) & 1) == 0 ? ChessBoard.BLACK: ChessBoard.WHITE);

			iColor = ChessBoard.BLACK;
			iPiece = jni.pieceAt(iColor, i);

			if(iPiece == BoardConstants.FIELD)
			{
				iColor = ChessBoard.WHITE;
				iPiece = jni.pieceAt(iColor, i);

				if(iPiece == BoardConstants.FIELD)
					bPiece = false;
			}

			for(int j = 0; j < arrSelPositions.length; j++){
				if(arrSelPositions[j] == i)
					bSelected = true;
			}
			if(arrPos != null){
				bSelectedPosition = arrPos.contains(i);
			}

			String coord = null;
			tmpCache = _arrImgCache[i];
			if(tmpCache._bPiece == bPiece &&
			   tmpCache._piece == iPiece &&
			   tmpCache._color == iColor &&
			   tmpCache._fieldColor == iFieldColor &&
			   tmpCache._selectedPos == bSelectedPosition &&
			   tmpCache._selected == bSelected &&
			   tmpCache._coord == coord){

				continue;
			}
			else{

				tmpCache._coord = coord;

                tmpCache._bPiece = bPiece;
                tmpCache._piece = iPiece;
                tmpCache._color = iColor;
                tmpCache._fieldColor = iFieldColor;
                tmpCache._selectedPos = bSelectedPosition;
                tmpCache._selected = bSelected;

				_arrImages[getFieldIndex(i)].setICO(tmpCache);
				_arrImages[getFieldIndex(i)].invalidate();

			} // cache check
		}
		System.gc();

	}

	public void setFlippedBoard(boolean flipped){
		resetImageCache();
		_flippedBoard = flipped;
	}
	public boolean getFlippedBoard(){
		return _flippedBoard;
	}
	public void flipBoard(){

		resetImageCache();
		_flippedBoard = _flippedBoard ? false : true;
		setFlippedBoard(_flippedBoard);
	}
	public int getFieldIndex(int i){
		if(_flippedBoard){
			return 63 - i;
		}
		return i;
	}

	public void resetImageCache(){
		for(int i = 0; i < 64; i++){
			_arrImgCache[i]._bPiece = false;
			_arrImgCache[i]._fieldColor = (i&1)==0 ? (((i >> 3) & 1) == 0 ? ChessBoard.WHITE : ChessBoard.BLACK) : (((i >> 3) & 1) == 0 ? ChessBoard.BLACK: ChessBoard.WHITE);
			_arrImgCache[i]._selectedPos = false;
			_arrImgCache[i]._selected = false;
			_arrImgCache[i]._color = -1;
			_arrImgCache[i]._piece = -1;
		}
	}

}