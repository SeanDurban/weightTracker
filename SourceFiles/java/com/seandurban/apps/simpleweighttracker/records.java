package com.seandurban.apps.simpleweighttracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class records extends AppCompatActivity {

    final String WEIGHIN_DB_NAME= "myWeighins.db";
    final String WEIGHINS_TABLE = "weighins";
    final String TOP_ROW="Date"+"::"+"Weight"+"::"+"Change"+"::"+"Overall";
    final String DELIMITER="::";
    final String EMPTY_DATABASE_MESSAGE="No Results in Database";

    SQLiteDatabase weighinsDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        getRecords();
    }

    /*
      Using cursor add all details in database to an arraylist that is then passed to the ArrayAdapter to display on screen.
      If database is empty display message and returns to main menu.
     */
    private void getRecords(){
        weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = weighinsDB.rawQuery("SELECT * FROM " + WEIGHINS_TABLE, null);
        int dateColumn = cursor.getColumnIndex("date");
        int weightColumn = cursor.getColumnIndex("weight");
        int progressColumn = cursor.getColumnIndex("progress");
        int changeColumn = cursor.getColumnIndex("change");
        cursor.moveToLast();

        ArrayList<String> data = new ArrayList<>();
        data.add(TOP_ROW);     //Adds row at the top with description of values in that column.
        String result;
        if(cursor.getCount()>0){
            do {
                String date = cursor.getString(dateColumn);
                String weight = Double.toString(cursor.getDouble(weightColumn));
                String progress = cursor.getString(progressColumn);
                String change = cursor.getString(changeColumn);

                result =date+DELIMITER+weight+DELIMITER+change+DELIMITER+progress;
                data.add(result);
            }while(cursor.moveToPrevious());

            cursor.close();
            ListAdapter theAdapter= new myAdapter(this, data);
            ListView theListView = (ListView) findViewById(R.id.theListView);
            theListView.setAdapter(theAdapter);
        }
        else{
            Toast.makeText(this,EMPTY_DATABASE_MESSAGE , Toast.LENGTH_SHORT ).show();

            //Return to Main Menu
            Intent i = new Intent(this, MainMenu.class);
            startActivity(i);

        }
    }
}
