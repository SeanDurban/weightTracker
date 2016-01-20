package com.seandurban.apps.simpleweighttracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Statistics extends AppCompatActivity {

    SQLiteDatabase weighinsDB = null;
    SQLiteDatabase unitDB=null;
    SQLiteDatabase goalWeightDB=null;
    SQLiteDatabase goalDB = null;
    TextView averageWeightValue, lowestWeightValue, highestWeightValue, totalWeighinsValue, averageChangeValue, unitsText, totalWeightChangeValue, totalWeightChangeText,
            weightRemainingValue, progressValue;
    ProgressBar progressBar;

    final int LOSS = 0;
    final String WEIGHIN_DB_NAME= "myWeighins.db";
    final String GOALWEIGHT_DB_NAME="goalWeight.db";
    final String UNIT_DB_NAME= "unit.db";
    final String GOAL_DB_NAME="goal.db";
    final String WEIGHINS_TABLE = "weighins";

    final String TOTAL_CHANGE_GAIN="Total Weight gain: ";
    final String TOTAL_CHANGE_LOSS="Total Weight loss: ";
    final int DEFAULT_PROGRESS=1;
    final String DEFAULT_PROGRESS_PERCENT="0%";
    final int TO_PERCENT_MULTIPLIER=100;
    final String EMPTY_DATABASE_MESSAGE="No Results in Database";
    final String UNIT_KG="Units-Kg";
    final String UNIT_LB="Units-lb";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        /*
        Initialise all text fields used/adapted.
         */
        averageWeightValue = (TextView) findViewById(R.id.averageWeightValue);
        lowestWeightValue = (TextView) findViewById(R.id.lowestWeightValue);
        highestWeightValue = (TextView) findViewById(R.id.highestWeightValue);
        totalWeighinsValue = (TextView) findViewById(R.id.totalWeighinsValue);
        averageChangeValue = (TextView) findViewById(R.id.averageChangeValue);
        unitsText = (TextView) findViewById(R.id.unitsText);
        totalWeightChangeValue = (TextView) findViewById(R.id.totalWeightChangeValue);
        totalWeightChangeText = (TextView) findViewById(R.id.totalWeightChangeText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        weightRemainingValue = (TextView) findViewById(R.id.weightRemainingValue);
        progressValue = (TextView) findViewById(R.id.progressValue);

        setUnitsText();
        setTotalChangeText();
        setValues();
    }

    /*
    Calculate all relevant stats using values in the databases. Then display results on screen.
     */
    private void setValues(){
        weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
        Cursor cursor = weighinsDB.rawQuery("SELECT * FROM " + WEIGHINS_TABLE, null);
        int weightColumn = cursor.getColumnIndex("weight");
        int progressColumn = cursor.getColumnIndex("progress");
        int changeColumn = cursor.getColumnIndex("change");
        cursor.moveToLast();

        double highestWeight=0;
        double lowestWeight=0;
        double weightSum=0;
        double changeSum=0;
        String totalWeightChange="";
        int amount=0;


        int count=0;
        if (cursor.getCount() > 0) {
            do {
                double weight =cursor.getDouble(weightColumn);
                String progress = cursor.getString(progressColumn);
                String change = cursor.getString(changeColumn);
                changeSum+=Double.parseDouble(change);
                if(count==0){
                    count++;
                    totalWeightChange=progress.trim();
                }
                if(amount==0){
                    highestWeight=weight;
                    lowestWeight=weight;
                }
                else if(weight>highestWeight){
                    highestWeight=weight;
                }
                else if(weight<lowestWeight){
                    lowestWeight=weight;
                }
                weightSum+=weight;
                amount++;
            } while (cursor.moveToPrevious());
            cursor.close();

            double average = getScaledAverage(weightSum,amount);
            double averageChangeVal = getScaledAverage(changeSum,(amount-1));

            DBHandler myDBHandler = new DBHandler(this,null,null,1);
            goalWeightDB =this.openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);
            weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
            goalDB=this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);

            double goalWeight = myDBHandler.getGoalWeight(goalWeightDB);
            double goalStartWeight = myDBHandler.getGoalStartWeight(goalWeightDB);
            double firstWeight = myDBHandler.getFirstWeighin(weighinsDB);
            double currentWeight = myDBHandler.getPreviousWeighin(weighinsDB);
            int goal = myDBHandler.getGoal(goalDB);
          //  Toast.makeText(this,goal,Toast.LENGTH_SHORT).show();

            double weightToGoal =0;
            double progress=DEFAULT_PROGRESS;    //Defaults to 1 so user can see progress bar.
            if(goalWeight!=0) {
                if (goal == LOSS) {
                    weightToGoal = currentWeight - goalWeight;
                    progress = (goalStartWeight - currentWeight) / (goalStartWeight - goalWeight);
                } else {
                    weightToGoal = goalWeight - currentWeight;
                    progress = (currentWeight-goalStartWeight) / (goalWeight-goalStartWeight);

                }
                progress *= TO_PERCENT_MULTIPLIER;   //convert to percentage
                BigDecimal percentToGoal = new BigDecimal(Double.toString(progress));
                percentToGoal=percentToGoal.setScale(2, RoundingMode.HALF_DOWN);
                progressValue.setText(percentToGoal.toString()+"%");
            }
            if(progress<DEFAULT_PROGRESS){
                progress=1;
                progressValue.setText(DEFAULT_PROGRESS_PERCENT);
            }
            BigDecimal weightRemaining = new BigDecimal(Double.toString(weightToGoal));
            weightRemaining=weightRemaining.setScale(2, RoundingMode.HALF_DOWN);

            /*
            Set Value text fields.
             */
            progressBar.setProgress((int) progress);
            weightRemainingValue.setText(weightRemaining.toString());
            averageWeightValue.setText(Double.toString(average));
            lowestWeightValue.setText(Double.toString(lowestWeight));
            highestWeightValue.setText(Double.toString(highestWeight));
            totalWeighinsValue.setText(Integer.toString(amount));
            averageChangeValue.setText(Double.toString(averageChangeVal));
            totalWeightChangeValue.setText(totalWeightChange);

        }
        else {
            Toast.makeText(this, EMPTY_DATABASE_MESSAGE, Toast.LENGTH_SHORT).show();
            //Return to Main Menu
            Intent i = new Intent(this, MainMenu.class);
            startActivity(i);
        }
    }

    /*
    Returns average of 2 values rounded to 2 decimal places.
     */
    private double getScaledAverage(double weightSum,double numberOfItems)
    {
        if(numberOfItems>1) {
            BigDecimal sum = new BigDecimal(Double.toString(weightSum));
            BigDecimal amount = new BigDecimal(Double.toString(numberOfItems));
            sum = sum.setScale(2, BigDecimal.ROUND_DOWN);
            amount=amount.setScale(2, BigDecimal.ROUND_DOWN);
            return sum.divide(amount, 2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        return weightSum;
    }

    /*
    Gets unit from database and sets display accordingly.
     */
    private void setUnitsText(){
        DBHandler myDBHandler = new DBHandler(this,null,null,1);
        unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
        int unit = myDBHandler.getUnit(unitDB);
        if(unit==0){
            unitsText.setText(UNIT_KG);
        }
        else{
            unitsText.setText(UNIT_LB);
        }
    }

    /*
    Get goal from database and sets display accordingly.
    */
    private void setTotalChangeText(){
        SQLiteDatabase goalDB = this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
        DBHandler myDBHandler = new DBHandler(this, null,null,1);
        int goal= myDBHandler.getGoal(goalDB);
        if(goal==0){
            totalWeightChangeText.setText(TOTAL_CHANGE_LOSS);
        }
        else{
            totalWeightChangeText.setText(TOTAL_CHANGE_GAIN);
        }
    }

}
