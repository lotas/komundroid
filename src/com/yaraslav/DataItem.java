package com.yaraslav;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;

public class DataItem {

	long dataId;
	long gaugeId;
	float value;
	Date dt;
	
	private GaugeItem _gaugeItem;
	
	public void setGaugeId(long _gaugeId) {
		gaugeId = _gaugeId;
	}
		
	
	public void setDataId(long _dataId) {
		dataId = _dataId;
	}
	
	public void setValue(float _value) {
		value = _value;
	}
	
	
	public void setDt(Date _dt) {
		dt = _dt;
	}
	
	public GaugeItem getGaugeItem() {
		return _gaugeItem;
	}
	
	public DataItem() {		
	}
	
	public DataItem(long _gaugeId, float _value) {
		this(0, _gaugeId, _value, new Date(java.lang.System.currentTimeMillis()));
	}
	
	public DataItem(Cursor cursor) {
		dataId = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_DATA_ID));
		gaugeId = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_DATA_GAUGE_ID));
		value = cursor.getFloat(cursor.getColumnIndex(DbAdapter.KEY_VALUE));
		dt = new Date(cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_DT)));
	}
	
	public DataItem(long _dataId, long _gaugeId, float _value, Date _dt) {
		dataId = _dataId;
		gaugeId = _gaugeId;
		value = _value;
		dt = _dt;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
		String dateString = sdf.format(dt);
		
		return "#" + gaugeId + ": " + String.format("%f", value) + " " + dateString;  
	}
}
