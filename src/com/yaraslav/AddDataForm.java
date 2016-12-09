package com.yaraslav;

import java.sql.Date;

import com.yaraslav.flashlight.FroyoLedFlashlight;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddDataForm extends Activity {

		private Intent intent;
		private TextView editTextValue;
		private DatePicker datePicker;
		
		private DataItem editItem;
		
		private DbAdapter dbAdapter;
		private long dataId;
		private long gaugeId;
		private Button flashBtn;
		private FroyoLedFlashlight froyoFleshlight;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.add_data_form);
			
			dbAdapter = KomundroidApplication.instance.dbAdapter;
			
			intent = getIntent();
			
			editTextValue = (EditText) findViewById(R.id.EditText01);						
			datePicker = (DatePicker) findViewById(R.id.DatePicker01);
			Button cancel = (Button) findViewById(R.id.cancelDataBtn);
			Button add = (Button) findViewById(R.id.saveDataBtn);
			
			
			dataId = intent.getLongExtra("dataId", 0);
			gaugeId = intent.getLongExtra("gaugeId", 0);
			
			GaugeItem gauge = dbAdapter.getGaugeItem(gaugeId);
			
			setTitle(KomundroidApplication.instance.meterTitles[(int) (gauge.gaugeId - 1)]);
			
			if (dataId > 0) {
				editItem = dbAdapter.getDataItem(dataId);
				editTextValue.setText("" + editItem.value);
				
				datePicker.updateDate(1900 + editItem.dt.getYear(), editItem.dt.getMonth(), editItem.dt.getDate());
				
				add.setText(R.string.btn_edit_text);
				
			} else {
				editItem = new DataItem();
				editItem.setGaugeId(gaugeId);
				
				editTextValue.setText("");
				editTextValue.requestFocus();
				
				//set current date
				Date dt = new Date(java.lang.System.currentTimeMillis());
				datePicker.updateDate(1900 + dt.getYear(), dt.getMonth(), 1/*dt.getDate()*/);
				
				add.setText(R.string.btn_add_text);
			}
			
			
			cancel.setOnClickListener(new OnClickListener() {			
				public void onClick(View v) {
					setResult(-1);
					finish();
				}
			});
			
			add.setOnClickListener(new OnClickListener() {				
				public void onClick(View v) {
					
					float value = 0;
					Date dt = null;
					
					try {
						value = Float.parseFloat("" + editTextValue.getText());
						//limit value to 5 digits only... sorry folks ;)
						if (value > 99999) {
							value = value % 100000;
						}
					} catch (NumberFormatException e) {
						Toast.makeText(AddDataForm.this, getString(R.string.error_parsing_value), Toast.LENGTH_SHORT).show();
						editTextValue.requestFocus();
						return;
					}
					
					try {
						dt = new Date(datePicker.getYear() - 1900, datePicker.getMonth(), datePicker.getDayOfMonth());
					} catch (Exception e) {
						Toast.makeText(AddDataForm.this, getString(R.string.error_parsing_value), Toast.LENGTH_SHORT).show();
						datePicker.requestFocus();
						return;
					}
					
					editItem.setValue(value);
					editItem.setDt(dt);

					try {
						dbAdapter.saveDataItem(editItem);
						
						Toast.makeText(AddDataForm.this, getString(R.string.data_saved_succesfully), Toast.LENGTH_SHORT).show();
						setResult(1);
						finish();
						
					} catch (Exception e) {
						Toast.makeText(AddDataForm.this, "Error saving: "+e.getMessage(), Toast.LENGTH_SHORT).show();
					}
					
				}
			});


			
			boolean flashSupported = KomundroidApplication.instance.flashSupported;
			flashBtn = (Button) findViewById(R.id.FlashlightBtn);
			flashBtn.setVisibility(flashSupported ? View.VISIBLE : View.INVISIBLE);						

			if (flashSupported) {			
				flashBtn.setOnClickListener(new OnClickListener() {				
					public void onClick(View v) {
						if (flashBtn.isSelected()) {
							KomundroidApplication.instance.froyoFleshlight.setOn(false, getApplicationContext());
							flashBtn.setText(KomundroidApplication.instance.flashStates[0]);
							flashBtn.setSelected(false);
						} else {
							KomundroidApplication.instance.froyoFleshlight.setOn(true, getApplicationContext());
							flashBtn.setText(KomundroidApplication.instance.flashStates[1]);
							flashBtn.setSelected(true);
						}
					}
				});
			}
		}
		
		private void killFlash() {
			KomundroidApplication.instance.froyoFleshlight.setOn(false, getApplicationContext());
		}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			killFlash();
		}
		
		@Override
		public void onBackPressed() {
			super.onBackPressed();
			killFlash();
		}
		
		@Override
		protected void onPause() {
			super.onPause();
			killFlash();
		}
}
