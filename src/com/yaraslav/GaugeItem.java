package com.yaraslav;

import java.util.Date;
import java.text.SimpleDateFormat;

import android.database.Cursor;

public class GaugeItem {

	long gaugeId;
	long objectId;	
	int typeId;
	String title;
	float currentValue;
	float prevValue;
	Date dt;
	
	private ObjectItem _objectItem;
	
	public void setTitle(String _title) {
		title = _title;
	}
	
	public void setGaugeId(long _gaugeId) {
		gaugeId = _gaugeId;
	}
	
	public void setObjectId(long _objectId) {
		objectId = _objectId;
	}
	
	public void setTypeId(int _typeId) {
		typeId = _typeId;
	}
	
	public void setCurrentValue(float _currentValue) {
		currentValue = _currentValue;
	}

	public void setPrevValue(float _prevValue) {
		prevValue = _prevValue;
	}

	public void setDt(Date _dt) {
		dt = _dt;
	}
	
	public float getDiff() {
		return currentValue - prevValue;
	}
		
	public GaugeItem() {		
	}

	public GaugeItem(Cursor cursor) {
		gaugeId = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_GAUGE_ID));
		title = cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_TITLE));
		objectId = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_GAUGE_OBJECT_ID));
		typeId = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_TYPE_ID));
		currentValue = cursor.getFloat(cursor.getColumnIndex(DbAdapter.KEY_CURRENT_VALUE));
		prevValue = cursor.getFloat(cursor.getColumnIndex(DbAdapter.KEY_PREV_VALUE));
		dt = new Date(cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_DT)));
	}
	
	public GaugeItem(long _gaugeId, long _objectId, int _gaugeTypeId, String _title, float _currentValue, float _prevValue, Date _dt) {
		gaugeId = _gaugeId;
		objectId = _objectId;
		typeId = _gaugeTypeId;
		title = _title;
		currentValue = _currentValue;
		prevValue = _prevValue;
		dt = _dt;
	}	
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
		String dateString = sdf.format(dt);
		
		return title + " " + String.format("%f", currentValue) + " " + dateString;  
	}
}
