package com.seandurban.apps.simpleweighttracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import java.math.BigDecimal;

public class Settings extends AppCompatActivity {

    final String WEIGHIN_DB_NAME= "myWeighins.db";
    final String GOALWEIGHT_DB_NAME="goalWeight.db";
    final String UNIT_DB_NAME= "unit.db";
    final String GOAL_DB_NAME="goal.db";
    final String WEIGHINS_TABLE = "weighins";
    final String GOAL_CHANGED_MESSAGE="Changed Goal Successfully.";
    final String UNITS_CHANGED_MESSAGE="Changed Units Successfully.";
    final String CLEARED_DATABASE_MESSAGE="Cleared Database.";
    final String INVALID_INPUT_MESSAGE="Invalid input, try again.";
    final int DEFAULT_GOAL_WEIGHT=0;

    SQLiteDatabase weighinsDB = null;
    SQLiteDatabase unitDB = null;
    SQLiteDatabase goalDB = null;
    SQLiteDatabase goalWeightDB =null;
    DBHandler myDBHandler;
    RadioButton unitsKg, unitsLb, lossButton, gainButton;
    int KG_UNIT_VALUE=0;
    int LB_UNIT_VALUE=1;
    int LOSS_GOAL_VALUE=0;
    int GAIN_GOAL_VALUE=1;
    double KG_LB_MULTIPLIER = 2.20462;
    double LB_KG_MULTIPLIER = 0.453592;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        unitsKg = (RadioButton) findViewById(R.id.kgButton);
        unitsLb = (RadioButton) findViewById(R.id.lbButton);
        lossButton = (RadioButton) findViewById(R.id.lossButton);
        gainButton = (RadioButton) findViewById(R.id.gainButton);
        myDBHandler = new DBHandler(this,null,null,1);

        goalDB = this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
        int goal =myDBHandler.getGoal(goalDB);

        unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
        int unit = myDBHandler.getUnit(unitDB);

        setButtons(goal,unit);
    }

    //When button is pressed delete weighins database and update goalWeight database.
    public void clearDatabase(View view) {
        Toast.makeText(Settings.this, CLEARED_DATABASE_MESSAGE, Toast.LENGTH_SHORT).show();
        weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
        goalWeightDB = this.openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);
        myDBHandler.clearDatabase(weighinsDB);
        myDBHandler.updateGoalWeight(DEFAULT_GOAL_WEIGHT, DEFAULT_GOAL_WEIGHT, goalWeightDB);
    }

    /*
    When new unit is selected update buttons and convert relevant values in the databases.
     */
    public void setUnitsKg(View view) {
        if (unitsLb.isChecked()) {
            unitsLb.setChecked(false);
            unitsKg.setChecked(true);

            unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.updateUnit(KG_UNIT_VALUE, unitDB);
            convertUnits(LB_KG_MULTIPLIER);
            convertGoalWeight(LB_KG_MULTIPLIER);
        }
    }
    public void setUnitsLb(View view) {
        if (unitsKg.isChecked()) {
            unitsKg.setChecked(false);
            unitsLb.setChecked(true);
            unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.updateUnit(LB_UNIT_VALUE, unitDB);
            convertUnits(KG_LB_MULTIPLIER);
            convertGoalWeight(KG_LB_MULTIPLIER);
        }
    }

    /*
    When new goal is selected update buttons and the goal database.
     */
    public void setGoalLoss(View view) {
        if (gainButton.isChecked()) {
            gainButton.setChecked(false);
            lossButton.setChecked(true);

            goalDB = this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.updateGoal(LOSS_GOAL_VALUE, goalDB);
            Toast.makeText(Settings.this, GOAL_CHANGED_MESSAGE, Toast.LENGTH_SHORT).show();
        }
    }
    public void setGoalGain(View view) {
        if (lossButton.isChecked()) {
            lossButton.setChecked(false);
            gainButton.setChecked(true);

            goalDB = this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.updateGoal(GAIN_GOAL_VALUE,goalDB);
            Toast.makeText(Settings.this, GOAL_CHANGED_MESSAGE, Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Sets all radio buttons when activity is called.
     */
    void setButtons(int goal, int unit){

        if(unit==0){
            unitsKg.setChecked(true);
            unitsLb.setChecked(false);
        }
        else if(unit==1){
            unitsLb.setChecked(true);
            unitsKg.setChecked(false);
        }
        if(goal==0){
            lossButton.setChecked(true);
            gainButton.setChecked(false);
        }
        else if(goal==1){
            lossButton.setChecked(false);
            gainButton.setChecked(true);
        }
    }

    /*
    Converts all relevant values in the weighins database to the new unit using the passed multiplier.
     */
    private void convertUnits(double multiplier) {

        weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = weighinsDB.rawQuery("SELECT * FROM "+WEIGHINS_TABLE, null);
        int weightColumn = cursor.getColumnIndex("weight");
        int progressColumn = cursor.getColumnIndex("progress");
        int idColumn =cursor.getColumnIndex("id");
        int changeColumn = cursor.getColumnIndex("change");
        cursor.moveToLast();

            if (cursor.getCount() > 0){
                do {
                    double weight = cursor.getDouble(weightColumn);
                    double progress = Double.parseDouble(cursor.getString(progressColumn));
                    double change = Double.parseDouble(cursor.getString(changeColumn));
                    int id = cursor.getInt(idColumn);
                    weight = getScaledResult(weight, multiplier);
                    progress = getScaledResult(progress, multiplier);
                    change= getScaledResult(change, multiplier);
                    String progressString = (progress > 0) ? ("+".concat(Double.toString(progress))) : (Double.toString(progress));
                    String changeString = (change > 0) ? ("+".concat(Double.toString(change))) : (Double.toString(change));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("weight", weight);
                    contentValues.put("progress", progressString);
                    contentValues.put("change", changeString);
                    weighinsDB.update("weighins", contentValues, "id" + " = ?", new String[]{Integer.toString(id)});
                } while (cursor.moveToPrevious());
            }
        cursor.close();
        Toast.makeText(Settings.this, UNITS_CHANGED_MESSAGE, Toast.LENGTH_SHORT).show();
        weighinsDB.close();
    }

    /*
    Converts goalWeight database values to the new unit using the passed multiplier.
     */
    private void convertGoalWeight(double multiplier){
        SQLiteDatabase goalWeightDB = openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);
        DBHandler myDBHandler= new DBHandler(this,null,null,1);
        double goalWeight = myDBHandler.getGoalWeight(goalWeightDB);
        double startGoalWeight= myDBHandler.getGoalStartWeight(goalWeightDB);
        goalWeight=getScaledResult(goalWeight,multiplier);
        startGoalWeight=getScaledResult(startGoalWeight,multiplier);
        myDBHandler.updateGoalWeight(goalWeight,startGoalWeight,goalWeightDB);

    }
    /*
    Takes 2 double values and returns their product rounded to 2 decimal places.
     */
    private double getScaledResult(double value, double multiplier) {
        BigDecimal sum = new BigDecimal(Double.toString(value));
        BigDecimal amount = new BigDecimal(Double.toString(multiplier));
        sum = sum.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal result = sum.multiply(amount);
        result = result.setScale(2, BigDecimal.ROUND_HALF_UP);
        return result.doubleValue();
    }

    /*
    When Add button is pressed get the inputted goalWeight and update the database accordingly.
     */
    public void goalWeightAdded(View view){
        EditText weightInput = (EditText) findViewById(R.id.goalInput);
        String input= weightInput.getText().toString();
        if((input).equals("")) {
            Toast.makeText(this, INVALID_INPUT_MESSAGE, Toast.LENGTH_SHORT).show();
        }
        else {
            weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
            DBHandler myDBHandler = new DBHandler(this, null, null, 1);
            double goal = Double.parseDouble(weightInput.getText().toString());
            double goalStartWeight = myDBHandler.getPreviousWeighin(weighinsDB);
            SQLiteDatabase goalWeightDB = openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);

            myDBHandler.updateGoalWeight(goal, goalStartWeight, goalWeightDB);
            Toast.makeText(this, "New Goal: " + goal, Toast.LENGTH_SHORT).show();
        }

    }





}
