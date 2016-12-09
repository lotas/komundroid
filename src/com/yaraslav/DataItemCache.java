package com.yaraslav;

import java.util.Date;
import java.util.Hashtable;

import android.database.Cursor;
import android.database.SQLException;

public class DataItemCache 
{
	
	private Hashtable<String, Float> cache;
	private DbAdapter dbAdapter;
	private Boolean isEmpty = false;
	
	public static Boolean hasChanged = false;

	public DataItemCache()
	{
		dbAdapter = KomundroidApplication.instance.dbAdapter;
		cache = new Hashtable<String, Float>();
		
		fillCache();
	}	
	
	private void fillCache()
	{
		Cursor c = dbAdapter.getAllDataItems();
		String key = "";
		
		if ((c.getCount() == 0) || !c.moveToFirst()) {
			isEmpty = true;
			return; // nothing to cache
	    }
		isEmpty = false;
		
		do {
			long gaugeId = c.getLong(c.getColumnIndex(DbAdapter.KEY_DATA_GAUGE_ID));
			float value  = c.getFloat(c.getColumnIndex(DbAdapter.KEY_VALUE));
			Date dt      = new Date(c.getLong(c.getColumnIndex(DbAdapter.KEY_DT)));
			
			key = "val-" + gaugeId + "-" + (dt.getYear() + 1900) + "-" + dt.getMonth(); 
			
			// if we have value for given month, we compare and store the minimal of them
			if (cache.containsKey(key)) {
				Float oldVal = (Float) cache.get(key);
				if (value < oldVal.floatValue()) {
					cache.put(key, value);
				}
			} else {
				cache.put(key, new Float(value));
			}
			
		} while (c.moveToNext());
	}
	
	/**
	 * load diff from db
	 */
	public float loadDiffData(String key, long gaugeId, int year, int month)
	{		
		Float diff = 0f;
				
		String keyTarget = "val-" + key;
		String keyNext = "val-" + gaugeId + "-" + (month == 11 ? year+1 : year) + "-" + (month == 11 ? 0 : month+1);

		// if we have data for target month, we calc difference, else, assume it's still 0f
		if (cache.containsKey(keyNext)) {			
			//	calculating difference between two dates
			diff = (Float) cache.get(keyNext);
				
			//if we don't have data for prev date, we assume 0
			diff -= cache.containsKey(keyTarget) ? (Float) cache.get(keyTarget) : 0f;
		}
		
		cache.put(key, diff);
		
		return diff.floatValue();
	}
	
	/**
	 * flush cache data, if data changed
	 */
	public void flush()
	{
		cache.clear();
		fillCache();
		
		hasChanged = true;
	}

	
	public float getDiffForPeriod(long gaugeId, ReportMonth reportMonth)
	{
		//no data in database yet
		if (isEmpty || reportMonth == null) {
			return 0;
		}
		
		String key = gaugeId + "-" + reportMonth.getYear() + "-" + reportMonth.getMonth(); 
		if (!cache.containsKey(key)) {
			return loadDiffData(key, gaugeId, reportMonth.getYear(), reportMonth.getMonth());
		}		
		
		Float diff = (Float) cache.get(key);
		
		return diff.floatValue();
	}
	
	public float getValueForPeriod(long gaugeId, ReportMonth reportMonth)
	{
		//no data in database yet		
		if (isEmpty || reportMonth == null) {
			return 0;
		}
		
		int year = reportMonth.getYear();
		int month = reportMonth.getMonth();		
		
		String key = "val-" + gaugeId + "-" + year + "-" + month;
		
		if (!cache.containsKey(key)) {
			return 0;
		}
		
		Float value = (Float) cache.get(key);
		
		return value.floatValue();
	}
	
	
	//singleton
	private static DataItemCache singleton;
	public static DataItemCache getInstance() {
		if (singleton == null) {
			singleton = new DataItemCache();
		}
		return singleton;
	}
}
