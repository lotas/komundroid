package com.yaraslav;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.yaraslav.Constants.PREF_SELECTED_MONTH;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class main extends ListActivity  {
	
	DbAdapter dbAdapter;
	
	ArrayAdapter<GaugeItem> aa;
	ArrayList<GaugeItem> gauges = new ArrayList<GaugeItem>(); 
	ListView gaugesListView;
	
	private Intent i;

	private Dialog aboutDialog;

	private Spinner monthSelect;

	private Cursor gaugesCursor;

	private GaugeCursorAdapter adapter;
	
	private ReportMonth currentReportMonth;
	private ArrayAdapter<ReportMonth> spinnerAdapter;
	private ReportMonth[] reportMonths;

	protected ProgressDialog progressDialog;
	
	public Boolean localeChanged = false;

	private String appVersion;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main); 
                
        dbAdapter = KomundroidApplication.instance.dbAdapter;

        //initSpinner will launch data list
        initSpinner();
        initSpinnerHandler();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
    	if (localeChanged) {
    		menu.clear();
    		
    		MenuInflater inflater = getMenuInflater();
        	inflater.inflate(R.menu.main_menu, menu);
        	
    		localeChanged = false;
    	}
    	
    	return super.onMenuOpened(featureId, menu);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_about:
    			showAbout();
    			return true;
    			
    		case R.id.menu_reports:
    			//showReports();
    			return true;
    			
    		case R.id.menu_tarifs:
    			showTariffs();
    			return true;
    		
    		case R.id.menu_dbman:
    			showDbManager();
    			return true;
    			
    		case R.id.menu_lang:
    			showLanguages();
    			return true;
    			
    		case R.id.menu_quit:
    			finish();
    			return true;
    	}    	
    	
    	return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	i = new Intent(this, GaugeActivity.class);
    	i.putExtra("gaugeId", id);
    	startActivity(i);
    }
    
    private String getVersion()
    {
    	if (appVersion == null) {
    		try {
    			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
    			appVersion = pi.versionName + " (" + String.valueOf(pi.versionCode) + ")";
    		} catch (NameNotFoundException e) {
    			appVersion = getResources().getString(R.string.version_unknown);
    		}
    	}
    	
    	return appVersion;
    }
 
    public void showAbout() {
    	aboutDialog = new Dialog(this);

    	aboutDialog.setContentView(R.layout.about_dialog);
    	aboutDialog.setTitle(R.string.dialog_about_title);
    	aboutDialog.setCanceledOnTouchOutside(true);

    	TextView text = (TextView) aboutDialog.findViewById(R.id.text);
    	//text.setText(Html.fromHtml(getResources().getString(R.string.dialog_about)));
    	text.setText(getResources().getString(R.string.dialog_about) + " " + getVersion());
    	ImageView image = (ImageView) aboutDialog.findViewById(R.id.image);
    	image.setImageResource(R.drawable.icon);
    	
    	Button imBtn = (Button) aboutDialog.findViewById(R.id.ImageButton01);
    	imBtn.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				aboutDialog.cancel();
			}
		});
    	
    	aboutDialog.show();
    }
    
    public void showLanguages()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.select_language);
    	
    	final CharSequence[] locales = {"ru", "en", "by"};
    	final CharSequence[] localeNames = {"Русский", "English", "Беларускi"};
    	
    	builder.setItems(localeNames, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int which) {
				String newLocale = (String)locales[which];
				
				KomundroidApplication.instance.updateLocale(newLocale);
				
				main.this.localeChanged = true;
				main.this.initSpinner();
				main.this.setTitle(R.string.app_name);
				
				Toast.makeText(getApplicationContext(), localeNames[which], Toast.LENGTH_SHORT).show();
			}
		});
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    	
    }
    

    
    public void recalcReportMonths()
    {
    	Date[] datesRange = dbAdapter.getDatesRange();
    	Date firstDate = datesRange[0];
    	Date now = datesRange[1];
    	
    	int endYear = now.getYear();
    	int startYear = firstDate.getYear();
    	int endMonth = now.getMonth();
    	int startMonth = firstDate.getMonth() ;	
    	
    	Integer monthsCount = (endYear - startYear) * 12 + (endMonth - startMonth);
    	
    	reportMonths = new ReportMonth[monthsCount+1];
    	
    	String[] monthNames = getResources().getStringArray(R.array.months); 
    	
    	startYear += 1900;
    	for (int i = 0; i <= monthsCount; i++)
    	{
  			reportMonths[monthsCount- i] = new ReportMonth(startYear, startMonth, monthNames[startMonth] + " " + startYear);
  			startMonth++;
  			if (startMonth > 11) {
  				startMonth = 0;
  				startYear++;
  			}
    	}

   	  	spinnerAdapter = new ArrayAdapter<ReportMonth>(this, R.layout.spinner_item, reportMonths);
   	  	monthSelect.setAdapter(spinnerAdapter);
    }
    
    private void initSpinner()
    {
    	monthSelect = (Spinner)findViewById(R.id.monthSelect);

    	recalcReportMonths();    	    	

    	//restoring selection
    	try {
    		monthSelect.setSelection(KomundroidApplication.instance.preferences.getInt(PREF_SELECTED_MONTH, 0));
    	} catch (Exception ex) {
    		Log.e("komundroid", ex.getMessage());
    	}
    }
    
    private void initSpinnerHandler()
    {
    	monthSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				
				ReportMonth oldReportMonth = currentReportMonth;
				
				KomundroidApplication.instance.saveSelectedReportMonth(pos);
				
				currentReportMonth = (ReportMonth)parent.getItemAtPosition(pos);
				
				Toast.makeText(
						parent.getContext(), 
						parent.getResources().getString(R.string.selected_report_month) + " " + currentReportMonth.toString(), 
						Toast.LENGTH_SHORT
					).show();

				if (!currentReportMonth.equals(oldReportMonth)) {
					initGauges();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {}			
		});
    }
    
    public void initGauges()
    {
        gaugesCursor = dbAdapter.getObjectGaugeItemsCursor(1);
        startManagingCursor(gaugesCursor);
        
        adapter = new GaugeCursorAdapter(
        		this,//context 
        		R.layout.gauge_row, 
        		gaugesCursor, 
        		new String[] {DbAdapter.KEY_GAUGE_ID, DbAdapter.KEY_TITLE, DbAdapter.KEY_CURRENT_VALUE}, 
        		new int[] {R.id.text1},
        		currentReportMonth
        );
        
        setListAdapter(adapter);  
    }    
    
    public void showTariffs() {
    	Intent i = new Intent(main.this, Preferences.class);
    	startActivity(i);
    }
    
    public void showDbManager()
    {
    	Intent i = new Intent(main.this, DbManager.class);
    	startActivity(i);    	
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if (DataItemCache.hasChanged) {
    		recalcReportMonths();
    	}
    }
}