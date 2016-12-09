package com.yaraslav;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;


public class GaugesList extends ListActivity  {
	
    private DbAdapter dbAdapter;
	private Cursor gaugesCursor;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.gauges); 
                

        dbAdapter = KomundroidApplication.instance.dbAdapter;
    }
    
    public void initGauges()
    {
        gaugesCursor = dbAdapter.getObjectGaugeItemsCursor(1);
        startManagingCursor(gaugesCursor);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
        			android.R.layout.simple_list_item_1, 
        			gaugesCursor, 
        			DbAdapter.GAUGES_TABLE_FIELDS, 
        			null);
        
        setListAdapter(adapter);  
    }   

}