package com.yaraslav;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbAdapter {
	private static final String DATABASE_NAME = "komundroid.db";
	
	private static final String OBJECTS_TABLE = "objects";
	private static final String GAUGES_TABLE = "gauges";
	private static final String DATA_TABLE = "data";

	private static final int DATABASE_VERSION = 11;
	
	// objects table
	public static final String KEY_OBJECT_ID = "_id";
	public static final String KEY_TITLE = "title";
	// gauges table
	public static final String KEY_GAUGE_ID = "_id";
	public static final String KEY_GAUGE_OBJECT_ID = "objectId";
	public static final String KEY_TYPE_ID = "typeId";
	public static final String KEY_CURRENT_VALUE  = "currentValue";
	public static final String KEY_PREV_VALUE  = "prevValue";
	public static final String KEY_DT = "dt";	
	// data table
	public static final String KEY_DATA_ID = "_id";
	public static final String KEY_DATA_GAUGE_ID = "gaugeId";
	public static final String KEY_VALUE = "value";
	// other field names are the same across the tables: objectId, dt, title, gaugeId..
	
	//table fields
	public static final String[] OBJECTS_TABLE_FIELDS = new String[] {KEY_OBJECT_ID, KEY_TITLE};
	public static final String[] GAUGES_TABLE_FIELDS = new String[] {KEY_GAUGE_ID, KEY_GAUGE_OBJECT_ID, KEY_TYPE_ID, KEY_TITLE, KEY_CURRENT_VALUE, KEY_PREV_VALUE, KEY_DT};
	public static final String[] DATA_TABLE_FIELDS = new String[] {KEY_DATA_ID, KEY_DATA_GAUGE_ID, KEY_VALUE, KEY_DT};

	//utility queries
	private static final String QUERY_MIN_MONTH = "SELECT MIN(" + KEY_DT + ") FROM " + DATA_TABLE;
	private static final String QUERY_MAX_MONTH = "SELECT MAX(" + KEY_DT + ") FROM " + DATA_TABLE;
	
	private SQLiteDatabase db;
	private final Context context;
	private KomundroidDbOpenHelper dbHelper;
	
	
	public DbAdapter(Context _context) {
		this.context = _context;
		dbHelper = new KomundroidDbOpenHelper(_context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void close() {
		db.close();
	}
	
	public void open() throws SQLiteException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}
	
	// objects
	// Insert new object
	public long insertObject(ObjectItem _object) {
		ContentValues newObjectValues = new ContentValues();		
		newObjectValues.put(KEY_TITLE, _object.getTitle());		
		return db.insert(OBJECTS_TABLE, null, newObjectValues);
	}
	
	//remove object by id
	public boolean removeObject(long _objectId) {
		return db.delete(OBJECTS_TABLE, KEY_OBJECT_ID + "=" + _objectId, null) > 0;
	}
	
	// Insert new gauge
	public long insertGauge(GaugeItem _gauge) {
		ContentValues newValues = new ContentValues();		

		newValues.put(KEY_GAUGE_OBJECT_ID, _gauge.objectId);
		newValues.put(KEY_TYPE_ID, _gauge.typeId);
		newValues.put(KEY_TITLE, _gauge.title);
		newValues.put(KEY_CURRENT_VALUE, _gauge.currentValue);
		newValues.put(KEY_PREV_VALUE, _gauge.prevValue);
		newValues.put(KEY_DT, _gauge.dt.getTime());
		
		return db.insert(GAUGES_TABLE, null, newValues);
	}	
	
	// update gauge values
	public boolean updateGauge(GaugeItem _gauge) {
		ContentValues newValues = new ContentValues();		

		newValues.put(KEY_GAUGE_OBJECT_ID, _gauge.objectId);
		newValues.put(KEY_TYPE_ID, _gauge.typeId);
		newValues.put(KEY_TITLE, _gauge.title);
		newValues.put(KEY_CURRENT_VALUE, _gauge.currentValue);
		newValues.put(KEY_PREV_VALUE, _gauge.prevValue);
		newValues.put(KEY_DT, _gauge.dt.getTime());
		
		return db.update(GAUGES_TABLE, newValues, KEY_GAUGE_ID + "=" + _gauge.gaugeId, null) > 0;
	}	
	
	/**
	 * Removing gauge, and all it's dataObjectItem object =
	 * 
	 * @param _gaugeId
	 * @return boolean
	 */	
	public boolean removeGauge(long _gaugeId) {
		return db.delete(GAUGES_TABLE, KEY_GAUGE_ID + "=" + _gaugeId, null) > 0 && removeGaugeData(_gaugeId);
	}
	
	public DataItem getLatestDataItemForGauge(long gaugeId)
	{
		Cursor cursor = db.query(DATA_TABLE, DATA_TABLE_FIELDS, KEY_DATA_GAUGE_ID+"="+gaugeId, null, null, null, KEY_DT+" DESC");
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			return null;
	    }

		return new DataItem(cursor);
	}

	public Float[] getLatestTwoData(long _gaugeId) {
		Float[] values = new Float[2];
		values[0] = 0f;
		values[1] = 0f;
		
		Cursor cursor = db.query(DATA_TABLE, new String[] {KEY_VALUE}, KEY_DATA_GAUGE_ID+"="+_gaugeId, null, null, null, KEY_DT+" DESC");
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			return values;
	    }
		
		values[0] = cursor.getFloat(0);
		if (cursor.moveToNext()) {
			values[1] = cursor.getFloat(0);
		}
		
		return values;
	}
	
	
	public Date[] getDatesRange()
	{
		Date[] range = new Date[2];
		
		Cursor cursor = db.query(DATA_TABLE, new String[]{"MIN("+KEY_DT+"), MAX("+KEY_DT+")"}, null, null, null, null, null);
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return null;
		}
		
		range[0] = new Date(cursor.getLong(0));
		range[1] = new Date(cursor.getLong(1));

		return range;
	}
	
	public long saveDataItem(DataItem _item) {
		ContentValues newValues = new ContentValues();		

		newValues.put(KEY_DATA_GAUGE_ID, _item.gaugeId);
		newValues.put(KEY_VALUE, _item.value);
		newValues.put(KEY_DT, _item.dt.getTime());

		long result;

		if (_item.dataId > 0) {
			result = db.update(DATA_TABLE, newValues, KEY_DATA_ID + "=" + _item.dataId, null);
		} else {
			result = db.insert(DATA_TABLE, null, newValues);
		}
		
		//update cur value for gauge too
		//DataItem latestData = getLatestDataItemForGauge(_item.getGaugeId());
		Float[] latestData = getLatestTwoData(_item.gaugeId);
		ContentValues gaugeValues = new ContentValues();		

		gaugeValues.put(KEY_CURRENT_VALUE, latestData[0]);
		gaugeValues.put(KEY_PREV_VALUE, latestData[1]);
		gaugeValues.put(KEY_DT, _item.dt.getTime());
		db.update(GAUGES_TABLE, gaugeValues, KEY_GAUGE_ID + "=" + _item.gaugeId, null);
		
		
		//flushing cache
		DataItemCache.getInstance().flush();
		
		return result;
	}
	
	public boolean removeData(long _dataId) {
		DataItem _item = getDataItem(_dataId);
		boolean result = db.delete(DATA_TABLE, KEY_DATA_ID + "=" + _dataId, null) > 0;

		//update cur value for gauge too
		Float[] latestData = getLatestTwoData(_item.gaugeId);
		ContentValues gaugeValues = new ContentValues();		

		gaugeValues.put(KEY_CURRENT_VALUE, latestData[0]);
		gaugeValues.put(KEY_PREV_VALUE, latestData[1]);
		gaugeValues.put(KEY_DT, _item.dt.getTime());
		db.update(GAUGES_TABLE, gaugeValues, KEY_GAUGE_ID + "=" + _item.gaugeId, null);
	
		
		return result;
	}
	
	public boolean removeGaugeData(long _gaugeId) {
		return db.delete(DATA_TABLE, KEY_DATA_GAUGE_ID + "=" + _gaugeId, null) > 0;
	}
	
	// ***** retrieving objects
	public ObjectItem getObjectItem(long _objectId) throws SQLException {
		Cursor cursor = db.query(true, OBJECTS_TABLE, OBJECTS_TABLE_FIELDS,				
				KEY_OBJECT_ID + "=" + _objectId, null, null, null, null, null);
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No object item found for row: " + _objectId);
	    }
		
		String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
		
		return new ObjectItem(title, _objectId);		
	}
	
	public GaugeItem getGaugeItem(long _gaugeId) throws SQLException {
		Cursor cursor = db.query(true, GAUGES_TABLE, GAUGES_TABLE_FIELDS,				
			KEY_GAUGE_ID + "=" + _gaugeId, null, null, null, null, null);
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No gauge item found for row: " + _gaugeId);
	    }
		
		return new GaugeItem(cursor);
	}
	
	public DataItem getDataItem(long _dataId) throws SQLException {
		Cursor cursor = db.query(true, DATA_TABLE, DATA_TABLE_FIELDS,				
			KEY_DATA_ID + "=" + _dataId, null, null, null, null, null);
		
		if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
			throw new SQLException("No data item found for row: " + _dataId);
	    }
		
		return new DataItem(cursor);
	}
	
	public Cursor getObjectGaugeItemsCursor(long _objectId) throws SQLException {
		return db.query(false, GAUGES_TABLE, GAUGES_TABLE_FIELDS, 
				KEY_GAUGE_OBJECT_ID + "=" + _objectId, null, null, null, null, null);
	}

	

	public Cursor getGaugeDataItemsCursor(long _gaugeId) throws SQLException {
		return this.getGaugeDataItemsCursor(_gaugeId, "DESC");
	}
	
	public Cursor getGaugeDataItemsCursor(long _gaugeId, String sort) throws SQLException {
		return db.query(false, DATA_TABLE, DATA_TABLE_FIELDS, 
				KEY_DATA_GAUGE_ID + "=" + _gaugeId, null, null, null, KEY_DT+" " + sort, null);
	}

	
	public Cursor getAllDataItems() throws SQLException {
		return db.query(false, DATA_TABLE, DATA_TABLE_FIELDS, 
				null, null, null, null, KEY_DT+" ASC", null);
	}
	
	
	/** backup/restore stuff **/
	public void cleanup() {
		if ((db != null) && db.isOpen()) {
			db.close();
		}
	}	
	
	public void resetDbConnection() {
		Log.i(DbManager.LOG_TAG, "resetting database connection (close and re-open).");
		cleanup();
		db = SQLiteDatabase.openDatabase("/data/data/com.yaraslav/databases/"
				+ DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
	}
	
	// super delete - clears all tables
	public void deleteAllDataYesIAmSure() {
		Log.i(DbManager.LOG_TAG, "deleting all data from database - deleteAllYesIAmSure invoked");
		db.beginTransaction();
		try {
			db.delete(DATA_TABLE, null, null);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.execSQL("vacuum");
	}
	
	/* **************** SQLiteOpenHelper  ************** */
	private static class KomundroidDbOpenHelper extends SQLiteOpenHelper {
	
		public KomundroidDbOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		//SQL to create new database
		private static final String DATABASE_CREATE_OBJECTS = "CREATE TABLE " + OBJECTS_TABLE + " (" +
			KEY_OBJECT_ID + " integer primary key autoincrement, " + 
			KEY_TITLE + " text not null);";
		
		private static final String DATABASE_CREATE_GAUGES = "CREATE TABLE " + GAUGES_TABLE + " (" +
			KEY_GAUGE_ID + " integer primary key autoincrement, " +
			KEY_GAUGE_OBJECT_ID + " integer not null, " +
			KEY_TYPE_ID + " integer not null, " + 
			KEY_TITLE + " text not null, " + 
			KEY_CURRENT_VALUE + " REAL default 0, " +
			KEY_PREV_VALUE + " REAL default 0, " + 
			KEY_DT + " long);";
		
		private static final String DATABASE_CREATE_DATA = "CREATE TABLE " + DATA_TABLE + " (" +
			KEY_DATA_ID + " integer PRIMARY KEY autoincrement, " +
			KEY_DATA_GAUGE_ID + " integer NOT NULL, " +
			KEY_VALUE + " real default 0, " +
			KEY_DT + " long );";
		
		@Override
		public void onCreate(SQLiteDatabase _db) {
			//create main tables
			_db.execSQL(DATABASE_CREATE_OBJECTS);
			_db.execSQL(DATABASE_CREATE_GAUGES);
			_db.execSQL(DATABASE_CREATE_DATA);
			
			//insert common data
			_db.execSQL("INSERT INTO " + OBJECTS_TABLE + " VALUES(1, 'Квартира');");
			
			long now = java.lang.System.currentTimeMillis();
			_db.execSQL("INSERT INTO " + GAUGES_TABLE + " VALUES(1, 1, " + GaugeTypes.COLD_WATER + ", 'Кухня - холодная вода', 10, 0, '"+now+"');");
			_db.execSQL("INSERT INTO " + GAUGES_TABLE + " VALUES(2, 1, " + GaugeTypes.HOT_WATER + ", 'Кухня - горячая вода', 20, 0, '"+now+"');");
			_db.execSQL("INSERT INTO " + GAUGES_TABLE + " VALUES(3, 1, " + GaugeTypes.COLD_WATER + ", 'Санузел - холодная вода', 30, 0, '"+now+"');");
			_db.execSQL("INSERT INTO " + GAUGES_TABLE + " VALUES(4, 1, " + GaugeTypes.HOT_WATER + ", 'Санузел - горячая вода', 40, 0, '"+now+"');");
			
			_db.execSQL("INSERT INTO " + GAUGES_TABLE + " VALUES(5, 1, " + GaugeTypes.GAS + ", 'Газ', 50, 0, '"+now+"');");
			_db.execSQL("INSERT INTO " + GAUGES_TABLE + " VALUES(6, 1, " + GaugeTypes.ELECTRICITY + ", 'Электричество', 60, 0, '"+now+"');");
			
			_db.execSQL("INSERT INTO " + DATA_TABLE + " VALUES(1, 1, 10, '"+now+"');");
			_db.execSQL("INSERT INTO " + DATA_TABLE + " VALUES(2, 2, 20, '"+now+"');");
			_db.execSQL("INSERT INTO " + DATA_TABLE + " VALUES(3, 3, 30, '"+now+"');");
			_db.execSQL("INSERT INTO " + DATA_TABLE + " VALUES(4, 4, 40, '"+now+"');");
			_db.execSQL("INSERT INTO " + DATA_TABLE + " VALUES(5, 5, 50, '"+now+"');");
			_db.execSQL("INSERT INTO " + DATA_TABLE + " VALUES(6, 6, 60, '"+now+"');");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
			Log.w("KomundroidDbAdapter", "Upgrading from version " + _oldVersion + " to " + _newVersion + ", old data will be destroyed");
			
			//dropping old
			_db.execSQL("DROP TABLE IF EXISTS " + OBJECTS_TABLE);
			_db.execSQL("DROP TABLE IF EXISTS " + GAUGES_TABLE);
			_db.execSQL("DROP TABLE IF EXISTS " + DATA_TABLE);
			
			//re-create new one
			onCreate(_db);
		}
		 
		 
	 }
}
