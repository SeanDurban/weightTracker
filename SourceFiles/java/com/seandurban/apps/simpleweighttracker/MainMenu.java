package com.seandurban.apps.simpleweighttracker;

import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import java.io.File;

public class MainMenu extends AppCompatActivity {


    final String WEIGHIN_DB_NAME= "myWeighins.db";
    final String GOALWEIGHT_DB_NAME="goalWeight.db";
    final String UNIT_DB_NAME= "unit.db";
    final String GOAL_DB_NAME="goal.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Check orientation of the screen is in and adjust the layout accordingly.
         */
        int orientation= this.getResources().getConfiguration().orientation;
        if(orientation==Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main_menu);
        }
        else if(orientation==Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_main_menu_landscape);
        }
        checkDatabasesExist();
    }

    /*
      Ensure all databases already exist, if not create them.
       */
    private  void checkDatabasesExist(){

        File dbTest = getApplicationContext().getDatabasePath((WEIGHIN_DB_NAME));
        DBHandler myDBHandler = new DBHandler(this, null,null,1);
        if (!dbTest.exists()) {
            SQLiteDatabase weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.createWeighinDB(weighinsDB);
        }

        dbTest = getApplicationContext().getDatabasePath((UNIT_DB_NAME));
        if (!dbTest.exists()) {
            SQLiteDatabase unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.createUnitDB(unitDB);
        }

        dbTest = getApplicationContext().getDatabasePath((GOAL_DB_NAME));
        if (!dbTest.exists()) {
            SQLiteDatabase goalDB = this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.createGoalDB(goalDB);

        }
        dbTest = getApplicationContext().getDatabasePath((GOALWEIGHT_DB_NAME));
        if (!dbTest.exists()) {
            SQLiteDatabase goalWeightDB = this.openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.createGoalWeightDB(goalWeightDB);

        }
    }

    /*
    OnClick methods for all buttons in view, take app to activity relating to button pressed.
     */
    public void recordOnClick(View view) {
        Intent i = new Intent(this, recordWeight.class);
        startActivity(i);
    }

    public void recordsOnClick(View view) {
        Intent i = new Intent(this, records.class);
        startActivity(i);
    }

    public void settingsOnClick(View view) {
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    public void statsOnClick(View view) {
        Intent i = new Intent(this, Statistics.class);
        startActivity(i);
    }

    /*
    Close databases when app is closed.
     */
    protected void onDestroy(){
        super.onDestroy();
        SQLiteDatabase weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
        SQLiteDatabase unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
        SQLiteDatabase goalDB = this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
        SQLiteDatabase goalWeightDB = this.openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);

        weighinsDB.close();
        goalDB.close();
        unitDB.close();
        goalWeightDB.close();
    }


}
