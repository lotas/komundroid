package com.yaraslav;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.util.AttributeSet;
import android.util.Log;

import com.yaraslav.*;

public class GaugeView extends View {
	public static final int DIGIT_WIDTH = 20;
	public static final int DIGIT_HEIGHT = 20;
	
	private Resources mRes;	
	private int mValue;
	private int mDigits = 10;
	private Bitmap mDigitBitmaps[] = new Bitmap[10];

	/**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
	public GaugeView(Context context) {
		super(context);		

		mRes = context.getResources();
		
		initGaugeView();		
	}
	
	/**
     * Construct object, initializing with any attributes we understand from a layout file.
     * 
     *  @param context
     *  @param attrs
     */
	public GaugeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mRes = context.getResources();
		initGaugeView();		
	}
		
	/**
	 * own init routine
	 */
	private final void initGaugeView() {
		//...
		setPadding(1, 1, 1, 1);
		

		
		mDigitBitmaps[0] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d0);
		mDigitBitmaps[1] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d1);
		mDigitBitmaps[2] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d2);
		mDigitBitmaps[3] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d3);
		mDigitBitmaps[4] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d4);
		mDigitBitmaps[5] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d5);
		mDigitBitmaps[6] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d6);
		mDigitBitmaps[7] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d7);
		mDigitBitmaps[8] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d8);
		mDigitBitmaps[9] = (Bitmap) BitmapFactory.decodeResource(mRes, R.drawable.d9);
	}
	
	/**
	 * set gauge value
	 * 
	 * @param value
	 */
	public void setValue(int value)
	{
		mValue = value;
		requestLayout();
		invalidate();
	}
	
	/**
	 * get gauge value
	 * 
	 * @return int
	 */
	public int getValue()
	{
		return mValue;
	}

	public void setDigits(int number)
	{
		mDigits = number;
	}
	
	public int getDigits()
	{
		return mDigits;
	}
	
	
    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mDigits * DIGIT_WIDTH + 2, DIGIT_HEIGHT + 4);
    }
    
    /**
     * Render the gauge
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int i, n, d;
        Paint paint = new Paint();
        String value = String.format("%10d", mValue);

        n = mValue;
        d = 0;
        while (n > 0) {
        	i = n % 10;        	
        	n = n / 10;
        	
         	canvas.drawBitmap(mDigitBitmaps[i], (float)(10-d) * 20, (float)0.0, paint);
        	d++;
        }
        
        for (i = 0; i < d; i++) {
        	canvas.drawBitmap(mDigitBitmaps[0], (float)i * 20, (float)0.0, paint);
        }
        
        
//        for (i = 0; i < value.length()-1; i++) {
//        	try {
//        		n = Integer.parseInt(value.substring(i, 1));
//        	} catch (NumberFormatException ex) {
//        		n = 0;
//        	} catch (StringIndexOutOfBoundsException ex) {
//        		n = 0;
//        	}
//        	
//        	canvas.drawBitmap(mDigitBitmaps[n], (float)i * 20 + 1, (float)0.0, paint);
//        }
        
        //canvas.drawBitmap
        
        //canvas.drawText(mText, getPaddingLeft(), getPaddingTop() - mAscent, mTextPaint);
    }
}
