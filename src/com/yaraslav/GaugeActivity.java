package com.yaraslav;

import com.yaraslav.charts.AbstractChart;
import com.yaraslav.charts.GaugeChart;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import static com.yaraslav.Constants.PREF_PRICE_WATER;
import static com.yaraslav.Constants.PREF_PRICE_GAS;
import static com.yaraslav.Constants.PREF_PRICE_ELECTRICITY;

public class GaugeActivity extends Activity {
	
	public static final int MENU_ITEM_EDIT = Menu.FIRST;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 1;

	private DbAdapter dbAdapter;

	private ListView dataView;

	private long gaugeId;
	private Cursor dataCursor;
	private DataItem curData;
	private DataCursorAdapter adapter;
	private GaugeItem gauge;
	private TextView textAmount;
	private TextView textCurView;
	private SharedPreferences preferences;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	
	    setContentView(R.layout.gauge_activitiy); 
	    
	    dbAdapter = KomundroidApplication.instance.dbAdapter;
	    
	    Intent i = getIntent();
	    
	    gaugeId = i.getLongExtra("gaugeId", 0);	    
	    gauge = dbAdapter.getGaugeItem(gaugeId);
	    
	    preferences = KomundroidApplication.instance.preferences;

		initControls();

	    // set title & texts
	    initGaugeInfo();	    
	    
	    // SET list view adapter
	    initDataView();    	    
	    
	    // Context menus
	    initContextMenu();	      
  	    
	    Button addBtn = (Button)findViewById(R.id.addDataBtn);
	    addBtn.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {			
				startForm(gaugeId, 0);
			}
		});
	    	
	    Button chartBtn = (Button)findViewById(R.id.gaugeChartBtn);
	    chartBtn.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				AbstractChart gaugeChart = new GaugeChart();
				Intent i = gaugeChart.execute(getApplicationContext(), gaugeId, gauge.typeId);
				startActivity(i);
			}
		});
	}
	
	private void initControls() {
		setTitle(KomundroidApplication.instance.meterTitles[(int) (gauge.gaugeId - 1)]);
		
		textCurView = (TextView) findViewById(R.id.textCurGaugeVal);		
		textAmount = (TextView) findViewById(R.id.gauge_table_text_amount);
	}	

	private void initGaugeInfo() {
		Float[] values = dbAdapter.getLatestTwoData(gaugeId);
		
		String tarifPref = PREF_PRICE_WATER;
		if (gauge.typeId == GaugeTypes.GAS) {
			tarifPref = PREF_PRICE_GAS;
		} else if (gauge.typeId == GaugeTypes.ELECTRICITY) {
			tarifPref = PREF_PRICE_ELECTRICITY;
		}
		//android.R.drawable.ic_dialog_info
		Float tarif = Float.parseFloat(preferences.getString(tarifPref, "0"));				

		textAmount.setText(String.format("%.2f х %.2f = %.2f", values[0]-values[1], tarif, (values[0] - values[1])*tarif));
	}
	

	private void initContextMenu() {
		dataView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {			
			public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {				
				menu.setHeaderTitle(getResources().getString(R.string.meter_context_menu_title));
				menu.add(0, MENU_ITEM_EDIT, 0, getResources().getString(R.string.meter_context_menu_edit));
				menu.add(0, MENU_ITEM_DELETE, 0, getResources().getString(R.string.meter_context_menu_remove));
			}
		});
	}

	private void initDataView() {
		dataView = (ListView) findViewById(R.id.dataListView);
		dataCursor = dbAdapter.getGaugeDataItemsCursor(gaugeId);
        startManagingCursor(dataCursor);
        
        adapter = new DataCursorAdapter(
        		this,//context 
        		R.layout.gauge_data_row, 
        		dataCursor, 
        		new String[] {DbAdapter.KEY_DATA_ID, DbAdapter.KEY_VALUE, DbAdapter.KEY_DT}, 
        		new int[] {R.id.text1}
        );
        
        dataView.setAdapter(adapter);
        
        adapter.registerDataSetObserver(new DataSetObserver() {
        	@Override
        	public void onChanged() {
				initGaugeInfo(); //recalculating values;
        	}
		});
        
        dataView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		startForm(gaugeId, arg3);
			}
		});
        
	}
	
	protected void startForm(long gaugeId, long dataId) {
		Intent i = new Intent(GaugeActivity.this, AddDataForm.class);
    	i.putExtra("gaugeId", gaugeId);
    	i.putExtra("dataId", dataId);
    	startActivity(i);
	}

	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		curData = new DataItem(dataCursor);
		
		switch (item.getItemId()) {
			case MENU_ITEM_DELETE:
				
				new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.alert_remove_data)
					.setMessage(R.string.alert_remove_data_text)
					.setPositiveButton(R.string.alert_remove_data_btn, new DialogInterface.OnClickListener() {						
						public void onClick(DialogInterface dialog, int which) {
														
							if (dbAdapter.removeData(curData.dataId)) {
								dataCursor.requery();
								adapter.notifyDataSetChanged();
								Toast.makeText(GaugeActivity.this, R.string.toast_delete_data, Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(GaugeActivity.this, R.string.toast_delete_data_failed, Toast.LENGTH_LONG).show();
							}
						}
					})
					.setNegativeButton(R.string.alert_remove_data_btn_no, null)
					.show();

		        break;
		        
			case MENU_ITEM_EDIT:
				startForm(gaugeId, curData.dataId);

				
		        break;
		}
		
		return false;
	}

}
