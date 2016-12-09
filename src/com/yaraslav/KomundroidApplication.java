package com.yaraslav;

import java.util.Locale;
import static com.yaraslav.Constants.PREF_FLASH_SUPPORTED;
import static com.yaraslav.Constants.PREF_SELECTED_LOCALE;
import static com.yaraslav.Constants.PREF_SELECTED_MONTH;

import com.yaraslav.flashlight.FroyoLedFlashlight;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

public class KomundroidApplication extends Application {
	
	public static KomundroidApplication instance;
	public static KomundroidApplication getInstance() {
		return instance;
	}
	
	private Locale locale = null;
	
	public DbAdapter dbAdapter;
	
	public Integer[] gaugeColorMap;
	public Bitmap[] gaugeIconsMap;
	public String[] meterTitles;

	public String[] flashStates;

	public FroyoLedFlashlight froyoFleshlight;
	public boolean flashSupported = false;

	public SharedPreferences preferences;

	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		
		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		froyoFleshlight = new FroyoLedFlashlight();
		
		if (!preferences.contains(PREF_FLASH_SUPPORTED)) {
			flashSupported = froyoFleshlight.isSupported(this);
			
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean(PREF_FLASH_SUPPORTED, flashSupported);
			editor.commit();
			
			//Log.d("KomundroidApp", "Wrote down preference flashSupported");
		} else {
			flashSupported = preferences.getBoolean(PREF_FLASH_SUPPORTED, false);
			//Log.d("KomundroidApp", "Read preference: " + flashSupported);
		}

		//read locale from preferences and update accordingly
		updateLocaleResources(preferences.getString(PREF_SELECTED_LOCALE, "ru"));
	}
	
	public void initResourcesCache() {	
		//init stuff
		gaugeColorMap = new Integer[5];		
		gaugeColorMap[GaugeTypes.COLD_WATER] = getResources().getColor(R.color.gauge_cwater);
		gaugeColorMap[GaugeTypes.HOT_WATER] = getResources().getColor(R.color.gauge_hwater);
		gaugeColorMap[GaugeTypes.GAS] = getResources().getColor(R.color.gauge_gas);
		gaugeColorMap[GaugeTypes.ELECTRICITY] = getResources().getColor(R.color.gauge_electricity);
		
		gaugeIconsMap = new Bitmap[5];		
		gaugeIconsMap[GaugeTypes.COLD_WATER] = (Bitmap) BitmapFactory.decodeResource(getResources(), R.drawable.ic_cwater);
		gaugeIconsMap[GaugeTypes.HOT_WATER] = (Bitmap) BitmapFactory.decodeResource(getResources(), R.drawable.ic_hwater);
		gaugeIconsMap[GaugeTypes.GAS] = (Bitmap) BitmapFactory.decodeResource(getResources(), R.drawable.ic_gas);
		gaugeIconsMap[GaugeTypes.ELECTRICITY] = (Bitmap) BitmapFactory.decodeResource(getResources(), R.drawable.ic_lamp);
		
		meterTitles = getResources().getStringArray(R.array.meters);
		
		flashStates = new String[2];
		flashStates[0] = getResources().getString(R.string.flashlight_on);
		flashStates[1] = getResources().getString(R.string.flashlight_off);
	}
	
	public void updateLocale(String loc)
	{
		updateLocaleResources(loc);
		saveSelectedLocale(loc);
	}
	
	public void updateLocaleResources(String loc)
	{
		Locale newLoc = new Locale(loc);
		Locale.setDefault(newLoc);
		
		Configuration newConfig = new Configuration();
		newConfig.locale = newLoc;
		
		getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		
		initResourcesCache();
	}
	
	public void saveSelectedLocale(String loc)
	{
		//saving in prefs
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREF_SELECTED_LOCALE, loc);
		editor.commit();
	}
	
	public void saveSelectedReportMonth(int position)
	{
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(PREF_SELECTED_MONTH, position);
		editor.commit();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
		froyoFleshlight.setOn(false, getApplicationContext());
		dbAdapter.close();
	}
		
	private void onpause() {
		// TODO Auto-generated method stub

	}
}
