package com.seandurban.apps.simpleweighttracker;

import android.content.Context;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;


public class myAdapter extends ArrayAdapter<String> {

    final String DELIMITER="::";
    final String UNIT_DB_NAME= "unit.db";
    final String GOAL_DB_NAME="goal.db";
    final int GREEN = Color.parseColor("#23B01C");


    public myAdapter(Context context, List<String> objects) {
        super(context, R.layout.row_layout, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater theInflater = LayoutInflater.from(getContext());
        View theView = theInflater.inflate(R.layout.row_layout, parent, false);

        //Check if in landscape and adjust layout to landscape version if necessary.
        int orientation= getContext().getResources().getConfiguration().orientation;
        if(orientation==Configuration.ORIENTATION_LANDSCAPE){
             theView = theInflater.inflate(R.layout.row_layout_landscape, parent, false);
        }

        DBHandler myDBHandler = new DBHandler(getContext(),null, null, 1);
        String result = getItem(position);
        String[] results = result.split(DELIMITER);

        //Set Date Column
        TextView dateColumn = (TextView) theView.findViewById(R.id.dateColumn);
        dateColumn.setText(results[0]);

        //Set Weight Column
        TextView weightColumn = (TextView) theView.findViewById(R.id.weightColumn);
        if((results[1].trim()).equals("Weight")){
            SQLiteDatabase unitDB = getContext().openOrCreateDatabase(UNIT_DB_NAME, Context.MODE_PRIVATE, null);
            int unit=myDBHandler.getUnit(unitDB);
            results[1]=results[1].concat((unit==0)?( " (Kg)"):(" (lb)"));
        }
        weightColumn.setText(results[1]);

        //Set change column
        TextView changeColumn = (TextView) theView.findViewById(R.id.changeColumn);
        changeColumn.setText(results[2]);
        SQLiteDatabase goalDB = getContext().openOrCreateDatabase(GOAL_DB_NAME, Context.MODE_PRIVATE, null);
        int goal = myDBHandler.getGoal(goalDB);
        if (!results[2].equals("Change")) {
            double progress = Double.parseDouble(results[2]);
            if (progress != 0) {
                {
                    if (goal == 0) {
                        int color = (progress > 0) ? (Color.RED) : (GREEN);
                        changeColumn.setTextColor(color);
                    } else {
                        int color = (progress > 0) ? (GREEN) : (Color.RED);
                        changeColumn.setTextColor(color);
                    }
                }
            }
        }

        //Set Progress Column
        TextView progressColumn= (TextView) theView.findViewById(R.id.progressColumn);
        progressColumn.setText(results[3]);
        if (!results[3].equals("Overall")) {
            double progress = Double.parseDouble(results[3]);
            if (progress != 0) {
                {
                    if (goal == 0) {
                        int color = (progress > 0) ? (Color.RED) : (GREEN);
                        progressColumn.setTextColor(color);
                    } else {
                        int color = (progress > 0) ? (GREEN) : (Color.RED);
                        progressColumn.setTextColor(color);
                    }
                }
            }
        }
            return theView;

    }
}