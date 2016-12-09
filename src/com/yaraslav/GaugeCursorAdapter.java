package com.yaraslav;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class GaugeCursorAdapter extends SimpleCursorAdapter {
	private Cursor mCursor;
	private Context mContext;
	
	private Resources mRes;
	private Bitmap mTypeBitmaps[] = new Bitmap[4];
	
	private Integer mDigitsMap[] = new Integer[10];
	private Integer mDigitsRedMap[] = new Integer[10];
	private Integer mGaugeColorMap[] = new Integer[4];
	private String[] mGaugeTitles = new String[7];
	
	private ReportMonth mReportMonth;
	private DataItemCache mDataItemCache;

	public GaugeCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, ReportMonth reportMonth) {
		super(context, layout, c, from, to);

		mCursor = c;
		mContext = context;
		mRes = context.getResources();
		mReportMonth = reportMonth;
		
		mDataItemCache = DataItemCache.getInstance();
		
		mTypeBitmaps = KomundroidApplication.instance.gaugeIconsMap;		
		mGaugeColorMap = KomundroidApplication.instance.gaugeColorMap;
		mGaugeTitles = KomundroidApplication.instance.meterTitles;
		
		mDigitsMap[0] = R.drawable.d0;
		mDigitsMap[1] = R.drawable.d1;
		mDigitsMap[2] = R.drawable.d2;
		mDigitsMap[3] = R.drawable.d3;
		mDigitsMap[4] = R.drawable.d4;
		mDigitsMap[5] = R.drawable.d5;
		mDigitsMap[6] = R.drawable.d6;
		mDigitsMap[7] = R.drawable.d7;
		mDigitsMap[8] = R.drawable.d8;
		mDigitsMap[9] = R.drawable.d9;
		
		mDigitsRedMap[0] = R.drawable.d0r;
		mDigitsRedMap[1] = R.drawable.d1r;
		mDigitsRedMap[2] = R.drawable.d2r;
		mDigitsRedMap[3] = R.drawable.d3r;
		mDigitsRedMap[4] = R.drawable.d4r;
		mDigitsRedMap[5] = R.drawable.d5r;
		mDigitsRedMap[6] = R.drawable.d6r;
		mDigitsRedMap[7] = R.drawable.d7r;
		mDigitsRedMap[8] = R.drawable.d8r;
		mDigitsRedMap[9] = R.drawable.d9r;
	}


	//@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		
		ImageView icon = (ImageView) view.findViewById(R.id.gaugeIcon);
		TextView text1 = (TextView) view.findViewById(R.id.text1);
		TextView textDiff = (TextView) view.findViewById(R.id.textDiff);
	
		GaugeItem gauge = new GaugeItem(mCursor); //creating from current cursor
		
		//text1.setText(gauge.title);
		text1.setText(mGaugeTitles[(int) (gauge.gaugeId - 1)]);
		icon.setImageBitmap(mTypeBitmaps[gauge.typeId]);
		text1.setTextColor(mGaugeColorMap[gauge.typeId]);
		
		//float diff = gauge.getDiff();
		float diff = mDataItemCache.getDiffForPeriod(gauge.gaugeId, mReportMonth);
		
		if (Math.abs(diff) < 0.001f) {
			textDiff.setText("0");
			textDiff.setTextColor(context.getResources().getColor(R.color.diff_zero));
		} else if (diff > 0) {
			textDiff.setText("+" + String.format("%d", Math.round(diff)));
			textDiff.setTextColor(context.getResources().getColor(R.color.diff_pos));
		} else {
			textDiff.setText(String.format("%d", Math.round(diff)));
			textDiff.setTextColor(context.getResources().getColor(R.color.diff_neg));
		}		
		
		ImageView digit1 = (ImageView) view.findViewById(R.id.gaugeDigit1);
		ImageView digit2 = (ImageView) view.findViewById(R.id.gaugeDigit2);
		ImageView digit3 = (ImageView) view.findViewById(R.id.gaugeDigit3);
		ImageView digit4 = (ImageView) view.findViewById(R.id.gaugeDigit4);
		ImageView digit5 = (ImageView) view.findViewById(R.id.gaugeDigit5);
		
		ImageView digit1d = (ImageView) view.findViewById(R.id.gaugeDigit1d);
		ImageView digit2d = (ImageView) view.findViewById(R.id.gaugeDigit2d);
		ImageView digit3d = (ImageView) view.findViewById(R.id.gaugeDigit3d);
		
		//float valueF = gauge.currentValue;
		float valueF = mDataItemCache.getValueForPeriod(gauge.gaugeId, mReportMonth);
		
		Integer valueD = (int) valueF;
		Integer fractD = (int) (valueF * 1000) % 1000;	//shift < 3 & get
				
		digit1.setImageResource(mDigitsMap[valueD % 10]);
		digit2.setImageResource(mDigitsMap[(valueD/10) % 10]);
		digit3.setImageResource(mDigitsMap[(valueD/100) % 10]);
		digit4.setImageResource(mDigitsMap[(valueD/1000) % 10]);
		digit5.setImageResource(mDigitsMap[(valueD/10000) % 10]);
		
		digit3d.setImageResource(mDigitsRedMap[fractD % 10]);
		digit2d.setImageResource(mDigitsRedMap[(fractD/10) % 10]);
		digit1d.setImageResource(mDigitsRedMap[(fractD/100) % 10]);
	}	
}
