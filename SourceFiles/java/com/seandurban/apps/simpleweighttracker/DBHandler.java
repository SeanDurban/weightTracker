package com.seandurban.apps.simpleweighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;



public class DBHandler extends SQLiteOpenHelper {

    final int KG_UNIT_VALUE=0;
    final int LOSS_GOAL_VALUE=0;
    final int DEFAULT_GOAL_WEIGHT=0;
    final int DEFAULT_START_WEIGHT=0;
    final String GOAL_TABLE = "goalTable";
    final String GOAL_ITEM = "goal";
    final String WEIGHINS_TABLE = "weighins";
    final String GOAL_WEIGHT_TABLE = "goalWeightTable";
    final String GOAL_WEIGHT_ITEM = "goalWeight";
    final String GOAL_START_WEIGHT= "startingWeight";
    final String UNIT_TABLE = "unitTable";
    final String UNIT_ITEM = "unit";

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version, null);
    }

    /*
    Creates database used to store current unit (0-Kg, 1-lb)
     */
    public void createUnitDB(SQLiteDatabase unitDB){
        try {
            unitDB.execSQL("CREATE TABLE IF NOT EXISTS "+UNIT_TABLE+" (id integer primary key, " +
                   UNIT_ITEM+ " integer);");
        } catch (Exception e) {
            Log.e("ERROR", "error creating Database");
        }
        //Unit defaults to kg value (int 0)
        unitDB.execSQL("INSERT INTO " + UNIT_TABLE + " (" + UNIT_ITEM + ") VALUES ('" +
                KG_UNIT_VALUE + "');");

    }

    /*
    Creates database used to store weighin values (date,weight,change,progress)
     */
    public void createWeighinDB(SQLiteDatabase weighinsDB){
        try {
            weighinsDB.execSQL("CREATE TABLE IF NOT EXISTS "+WEIGHINS_TABLE + " (id integer primary key," +
                    " date VARCHAR, weight double, change VARCHAR, progress VARCHAR);");

        } catch (Exception e) {
            Log.e("ERROR", "error creating Database");
        }
    }

    /*
   Creates database used to store current goalWeight values(goalWeight and starting weight)
    */
    public void createGoalWeightDB(SQLiteDatabase goalWeightDB){
        try {
            goalWeightDB.execSQL("CREATE TABLE IF NOT EXISTS "+GOAL_WEIGHT_TABLE  + " (id integer primary key, " +
                    " "+GOAL_START_WEIGHT+" double, "+GOAL_WEIGHT_ITEM+" double);");

        } catch (Exception e) {
            Log.e("ERROR", "error creating Database");
        }
        //Both values default to 0.
        goalWeightDB.execSQL("INSERT INTO "+GOAL_WEIGHT_TABLE+" ("+GOAL_WEIGHT_ITEM+", "+GOAL_START_WEIGHT+") VALUES ('" +
                DEFAULT_GOAL_WEIGHT+"', '"+DEFAULT_START_WEIGHT + "');");
    }
    /*
    Creates database used to store current goal (0-loss, 1-gain)
  */
    public void createGoalDB(SQLiteDatabase goalDB){
        try {
            goalDB.execSQL("CREATE TABLE IF NOT EXISTS "+GOAL_TABLE  + " (id integer primary key, " +
                    GOAL_ITEM+" integer);");
        } catch (Exception e) {
            Log.e("ERROR", "error creating Database");
        }
        //Goal defaults to loss value (int 0)
        goalDB.execSQL("INSERT INTO " + GOAL_TABLE + " (" + GOAL_ITEM + ") VALUES ('" +
                LOSS_GOAL_VALUE + "');");



    }

    /*
    Takes a new weight and adds a new weighin to the database.
     */
    public void addWeighin(SQLiteDatabase weighinsDB,double weight){
        double firstWeight= getFirstWeighin(weighinsDB);
        if(firstWeight==0){
            firstWeight=weight; //New weighin is first weighin recorded.
        }

        double lastWeighin=getPreviousWeighin(weighinsDB);
        if(lastWeighin==0){
            lastWeighin=weight; //New weighin is first weighin recorded.
        }

        /*
            Use BigDecimal to round weights to 2 decimal places to ensure no formatting issues
            when displaying data.
         */

        //Converted and passed as Strings as doubles can have unpredictable results.
        BigDecimal w1 = new BigDecimal(Double.toString(weight));
        BigDecimal w2 = new BigDecimal(Double.toString(firstWeight));
        BigDecimal lastWeighinBD= new BigDecimal(Double.toString(lastWeighin));

        w1=w1.setScale(2,BigDecimal.ROUND_DOWN);
        w2=w2.setScale(2,BigDecimal.ROUND_DOWN);
        lastWeighinBD=lastWeighinBD.setScale(2,BigDecimal.ROUND_DOWN);
        weight=w1.doubleValue();

        /*
        Add weighin details to the database
         */
        String date = new SimpleDateFormat("dd-MM").format(new Date());
        double difference = (w1.subtract(w2)).doubleValue();
        double difference2 = (w1.subtract(lastWeighinBD).doubleValue());
        String progress=(difference>0)?("+".concat(Double.toString(difference))):(Double.toString(difference));
        String change=(difference2>0)?("+".concat(Double.toString(difference2))):(Double.toString(difference2));
        if(difference==0){
            String temp = "   ";
            progress=temp.concat(Double.toString(difference));
        }
        if(difference2==0){
            change= "  "+(Double.toString(difference2));
        }
        weighinsDB.execSQL("INSERT INTO "+WEIGHINS_TABLE+" (date, weight, change, progress) VALUES ('" +
                date + "', '" + weight + "', '"+ change +"', '"+ progress + "');");

    }

    /*
    Returns very first inputted weight in database (First item)
    */
    public double getFirstWeighin(SQLiteDatabase weighinsDB){
        double firstWeight=0;
        Cursor cursor = weighinsDB.rawQuery("SELECT * FROM "+WEIGHINS_TABLE, null);
        cursor.moveToFirst();
        if(cursor.getCount()>0)
        {
            int weightColumn = cursor.getColumnIndex("weight");
            firstWeight = cursor.getDouble(weightColumn);
        }
        cursor.close();
        return firstWeight;
    }
    /*
    Returns Last/previous weight inputted into the Database.
     */
    public double getPreviousWeighin(SQLiteDatabase weighinsDB){
        double lastWeighin=0;
        Cursor cursor = weighinsDB.rawQuery("SELECT * FROM "+WEIGHINS_TABLE, null);
        cursor.moveToLast();
        if(cursor.getCount()>0)
        {
            int weightColumn = cursor.getColumnIndex("weight");
            lastWeighin = cursor.getDouble(weightColumn);
        }
        cursor.close();
        return lastWeighin;
    }

    /*
    Returns current goal from goal Database
     */
    public  int getGoal(SQLiteDatabase goalDB) {
        Cursor cursor = goalDB.rawQuery("SELECT * FROM "+GOAL_TABLE, null);
        int goalColumn = cursor.getColumnIndex(GOAL_ITEM);
        cursor.moveToLast();
        int goal = 0;
        if(cursor.getCount()>0){
            do {
                goal = cursor.getInt(goalColumn);
            }
            while (cursor.moveToPrevious());
        }
        cursor.close();
        return goal;
    }

    /*
    Returns current goalWeight from goalWeight Database.
    If returns 0, goal weight has not been set.
     */
    public double getGoalWeight(SQLiteDatabase goalWeightDB) {
        Cursor cursor = goalWeightDB.rawQuery("SELECT * FROM "+GOAL_WEIGHT_TABLE, null);
        int goalWeightColumn = cursor.getColumnIndex(GOAL_WEIGHT_ITEM);
        cursor.moveToLast();
        double goalWeight = 0;
        if(cursor.getCount()>0){
            do {
                goalWeight = cursor.getDouble(goalWeightColumn);
            }
            while (cursor.moveToPrevious());
        }
        cursor.close();
        return goalWeight;
    }

    /*
    Returns current starting weight when goal was set from goalWeight database.
    If returns 0, goal weight has not been set.
     */
    public double getGoalStartWeight(SQLiteDatabase goalWeightDB){
        Cursor cursor = goalWeightDB.rawQuery("SELECT * FROM "+GOAL_WEIGHT_TABLE, null);
        int goalStartWeightColumn = cursor.getColumnIndex(GOAL_START_WEIGHT);
        cursor.moveToLast();
        double goalStartWeight = 0;
        if(cursor.getCount()>0){
            do {
                goalStartWeight = cursor.getDouble(goalStartWeightColumn);
            }
            while (cursor.moveToPrevious());
        }
        cursor.close();
        return goalStartWeight;
    }

    /*
    Returns current unit from unit database.
     */
    public int getUnit(SQLiteDatabase unitDB) {
        Cursor cursor = unitDB.rawQuery("SELECT * FROM "+UNIT_TABLE, null);
        int unitColumn = cursor.getColumnIndex(UNIT_ITEM);
        cursor.moveToLast();
        int unit = 0;
        if(cursor.getCount()>0){
            do {
                unit = cursor.getInt(unitColumn);
            }
            while (cursor.moveToPrevious());
        }
        cursor.close();
        return unit;
    }

    /*
    Returns amount of weighins/rows in the weighins database.
     */
   public int getAmountOfEntries(SQLiteDatabase weighinsDB) {
       Cursor cursor = weighinsDB.rawQuery("SELECT * FROM "+WEIGHINS_TABLE, null);
       int res=cursor.getCount();
       cursor.close();
       return res;
    }

    /*
    Updates the unit database with the new unit.
     */
   public void updateUnit(int newUnit, SQLiteDatabase unitDB){
        ContentValues values = new ContentValues();
        values.put(UNIT_ITEM, newUnit);
        unitDB.update(UNIT_TABLE, values, "id" + " = ?", new String[]{Integer.toString(1)});
    }

    /*
    Updates the goal database with the new goal.
    */
    public void updateGoal(int newGoal, SQLiteDatabase goalDB){
        ContentValues values = new ContentValues();
        values.put(GOAL_ITEM, newGoal);
        goalDB.update(GOAL_TABLE, values, "id" + " = ?", new String[]{Integer.toString(1)});
    }

    /*
    Updates the goalWeight database with the new goalWeight and new goalStartWeight values.
    */
    public void updateGoalWeight(double newGoalWeight, double goalStartWeight, SQLiteDatabase goalWeightDB){
        ContentValues values = new ContentValues();
        values.put(GOAL_WEIGHT_ITEM, newGoalWeight);
        values.put(GOAL_START_WEIGHT,goalStartWeight);
        goalWeightDB.update(GOAL_WEIGHT_TABLE, values, "id" + " = ?", new String[]{Integer.toString(1)});
    }

    /*
    Deletes all entries in the weighins database
     */
    public void clearDatabase(SQLiteDatabase weighinsDB) {
        int amount =getAmountOfEntries(weighinsDB);
        for (int i = 0; i < amount; i++) {
            weighinsDB.execSQL("DELETE FROM "+WEIGHINS_TABLE+" WHERE id = " + Integer.toString(i + 1) + ";");
        }

    }



    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
