package club.diagram.chess;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

public class PGNView {

	public String _sMove;
	public boolean _bAnnotated;
	public View _view;
	public ChessView _parent;
	private TextView _tvItem;
	
	public PGNView(ChessView parent, View view, int num, String sMove, boolean bAnno){
		_parent = parent;
		_view = view;
		_bAnnotated = bAnno;
		
		TextView tvItemNum = (TextView)_view.findViewById(R.id.TextViewNumMove);
		if(num % 2 == 1){
			int i = ((int)num/2 + 1);
			String s = "";
			if(parent.hasVerticalScroll()){
				if(i < 100)
					s += " ";
				if(i < 10)
					s += " ";
			}
			tvItemNum.setText(s + i + ". ");

		} else {
			tvItemNum.setWidth(0);
		}
		tvItemNum.setTypeface(Typeface.SERIF);
		_tvItem = (TextView)_view.findViewById(R.id.TextViewMove);
		_tvItem.setText(sMove);
		_tvItem.setTypeface(Typeface.SERIF);
		_sMove = sMove;
		
		_view.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0) {
        		_parent.onClickPGNView(PGNView.this);
        	}
    	});
		
		_view.setOnLongClickListener(new OnLongClickListener(){

			public boolean onLongClick(View v) {
				_parent.onLongClickPGNView(PGNView.this);
				return false;
			}
		});
	}
	public void setAnnotated(boolean b){
		_bAnnotated = b;
	}
	public void setSelected(boolean b){

		ShapeDrawable sd = new ShapeDrawable();
		sd.getPaint().setStrokeWidth(10f);
		sd.getPaint().setStyle(Paint.Style.STROKE);
        sd.getPaint().setColor(0xffd2527f); //rosa


		if(b){
			_tvItem.setTextColor(0xffd2527f); //item com seleção
		}
		else{
			_tvItem.setTextColor(0xff6a6a6a); //item sem seleção
		}
	}
}
