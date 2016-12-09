package com.yaraslav;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.yaraslav.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Simplified import/export DB activity.
 * 
 * @author ccollins (and-examples)
 *
 */
public class DbManager extends Activity {
   public static final String LOG_TAG = "DbManager";
	
   private Button exportDbToSdButton;
   private Button importDbFromSdButton;
   private Button clearDbButton;

   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.managedata);

      exportDbToSdButton = (Button) findViewById(R.id.exportdbtosdbutton);
      exportDbToSdButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) 
         {
            Log.i(DbManager.LOG_TAG, "exporting database to external storage");
            new AlertDialog.Builder(DbManager.this).setMessage(
                     getResources().getString(R.string.backup_confirmation)).setPositiveButton(getResources().getString(R.string.yes),
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) 
                        {
                           if (isExternalStorageAvail()) 
                           {
                              Log.i(DbManager.LOG_TAG, "importing database from external storage, and resetting database");
                              new ExportDatabaseTask().execute();
                              DbManager.this.startActivity(new Intent(DbManager.this, main.class));
                           } 
                           else 
                           {
                              Toast.makeText(DbManager.this,
                            		  getResources().getString(R.string.backup_error) , Toast.LENGTH_LONG)
                                       .show();
                           }
                        }
                     }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface arg0, int arg1) {
               }
            }).show();
         }
      });

      importDbFromSdButton = (Button) findViewById(R.id.importdbfromsdbutton);
      importDbFromSdButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) {
            new AlertDialog.Builder(DbManager.this).setMessage(getResources().getString(R.string.import_confirmation))
            	.setPositiveButton(getResources().getString(R.string.yes),
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) 
                        {
                           if (isExternalStorageAvail()) {
                              Log.i(DbManager.LOG_TAG, "importing database from external storage, and resetting database");
                              new ImportDatabaseTask().execute();
                              // sleep momentarily so that database reset stuff has time to take place (else Main reloads too fast)
                              SystemClock.sleep(500);
                              DbManager.this.startActivity(new Intent(DbManager.this, main.class));
                           } else {
                              Toast.makeText(DbManager.this,
                            		   getResources().getString(R.string.import_error), Toast.LENGTH_LONG).show();
                           }
                        }
                     }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface arg0, int arg1) {}
            }).show();
         }
      });

      clearDbButton = (Button) findViewById(R.id.cleardbutton);
      clearDbButton.setOnClickListener(new OnClickListener() {
         public void onClick(final View v) {
            new AlertDialog.Builder(DbManager.this).setMessage(getResources().getString(R.string.cleanupdb_confirmation))
                     .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) 
                        {
                           Log.i(DbManager.LOG_TAG, "deleting database");
                           KomundroidApplication.instance.dbAdapter.deleteAllDataYesIAmSure();
                           KomundroidApplication.instance.dbAdapter.resetDbConnection();
                           
                           Toast.makeText(DbManager.this, getResources().getString(R.string.data_deleted), Toast.LENGTH_LONG).show();
                           DbManager.this.startActivity(new Intent(DbManager.this, main.class));
                        }
                     }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {}
                     }).show();
         }
      });
   }

   private boolean isExternalStorageAvail() {
      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
   }

   private class ExportDatabaseTask extends AsyncTask<Void, Void, Boolean> {
      private final ProgressDialog dialog = new ProgressDialog(DbManager.this);

      // can use UI thread here
      @Override
      protected void onPreExecute() {
         dialog.setMessage(getResources().getString(R.string.exporting));
         dialog.show();
      }

      // automatically done on worker thread (separate from UI thread)
      @Override
      protected Boolean doInBackground(final Void... args) {

         File dbFile = new File(Environment.getDataDirectory() + "/data/com.yaraslav/databases/komundroid.db");

         File exportDir = new File(Environment.getExternalStorageDirectory(), ".komundroid");
         if (!exportDir.exists()) {
            exportDir.mkdirs();
         }
         File file = new File(exportDir, dbFile.getName());

         try {
            file.createNewFile();
            FileUtil.copyFile(dbFile, file);
            return true;
         } catch (IOException e) {
            Log.e(DbManager.LOG_TAG, e.getMessage(), e);
            return false;
         }
      }

      // can use UI thread here
      @Override
      protected void onPostExecute(final Boolean success) {
         if (dialog.isShowing()) {
            dialog.dismiss();
         }
         if (success) {
            Toast.makeText(DbManager.this, getResources().getString(R.string.export_success), Toast.LENGTH_LONG).show();
         } else {
            Toast.makeText(DbManager.this, getResources().getString(R.string.export_fail), Toast.LENGTH_LONG).show();
         }
      }
   }

   private class ImportDatabaseTask extends AsyncTask<Void, Void, String> {
      private final ProgressDialog dialog = new ProgressDialog(DbManager.this);

      @Override
      protected void onPreExecute() {
         dialog.setMessage(getResources().getString(R.string.importing));
         dialog.show();
      }

      // could pass the params used here in AsyncTask<String, Void, String> - but not being re-used
      @Override
      protected String doInBackground(final Void... args) {

         File dbBackupFile = new File(Environment.getExternalStorageDirectory() + "/.komundroid/komundroid.db");
         if (!dbBackupFile.exists()) {
            return getResources().getString(R.string.import_nobackup);
         } else if (!dbBackupFile.canRead()) {
            return getResources().getString(R.string.import_notreadable);
         }

         File dbFile = new File(Environment.getDataDirectory() + "/data/com.yaraslav/databases/komundroid.db");
         if (dbFile.exists()) {
            dbFile.delete();
         }

         try {
            dbFile.createNewFile();
            FileUtil.copyFile(dbBackupFile, dbFile);
            KomundroidApplication.instance.dbAdapter.resetDbConnection();
            return null;
         } catch (IOException e) {
            Log.e(DbManager.LOG_TAG, e.getMessage(), e);
            return e.getMessage();
         }
      }

      @Override
      protected void onPostExecute(final String errMsg) {
         if (dialog.isShowing()) {
            dialog.dismiss();
         }
         if (errMsg == null) {
            Toast.makeText(DbManager.this, getResources().getString(R.string.import_success), Toast.LENGTH_LONG).show();
         } else {
            Toast.makeText(DbManager.this, getResources().getString(R.string.import_fail) + errMsg, Toast.LENGTH_LONG).show();
         }
      }
   }
}