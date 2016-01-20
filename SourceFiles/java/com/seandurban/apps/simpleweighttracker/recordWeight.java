package com.seandurban.apps.simpleweighttracker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;

public class recordWeight extends AppCompatActivity {

    Button addButton;
    EditText weightInput;
    TextView unitsText, goalWeightText, goalText, unitsText2;
    SQLiteDatabase weighinsDB = null;
    SQLiteDatabase unitDB =null;
    SQLiteDatabase goalDB =null;
    SQLiteDatabase goalWeightDB=null;
    DBHandler myDBHandler;
    int unit;

    final int LOSS = 0;
    final int GAIN= 1;
    final int DEFAULT_GOAL_WEIGHT=0;
    final String WEIGHIN_DB_NAME= "myWeighins.db";
    final String GOALWEIGHT_DB_NAME="goalWeight.db";
    final String UNIT_DB_NAME= "unit.db";
    final String GOAL_DB_NAME="goal.db";

    final String DEFAULT_PROGRESS_MESSAGE="Weigh-in Added!";
    final String HIT_GOAL_PROGRESS_MESSAGE="Congratulations!\nYou have hit your Goal Weight";
    final String INVALID_INPUT_MESSAGE="Invalid Weight, try again.";
    final String UNIT_KG="Kg";
    final String UNIT_LB="lb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_weight);

        /*
        Intialise all buttons in view.
         */
        addButton = (Button) findViewById(R.id.addButton);
        weightInput = (EditText) findViewById((R.id.weightInput));
        unitsText = (TextView) findViewById(R.id.unitsText);
        goalWeightText = (TextView) findViewById(R.id.goalWeightText);
        goalText= (TextView) findViewById(R.id.goalText);
        unitsText2 = (TextView) findViewById(R.id.unitsText2);

        myDBHandler = new DBHandler(this, null, null, 1);
        setGoalWeightText();
        setUnitText();
    }

    /*
        Get GoalWeight, if has been set show on screen. If not set then don't show anything.
    */
    private void setGoalWeightText(){
        goalWeightDB = this.openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);
        double goalWeight = myDBHandler.getGoalWeight(goalWeightDB);
        if (goalWeight != DEFAULT_GOAL_WEIGHT) {
            unitsText2.setVisibility(View.VISIBLE);
            goalText.setVisibility(View.VISIBLE);
            goalWeightText.setVisibility(View.VISIBLE);
            goalWeightText.setText(Double.toString(goalWeight));
        }
        else{
            unitsText2.setVisibility(View.INVISIBLE);
            goalText.setVisibility(View.INVISIBLE);
            goalWeightText.setVisibility(View.INVISIBLE);
        }
    }

    /*
        Get current unit of weight and set values on screen.
    */
    private void setUnitText(){

        unitDB = this.openOrCreateDatabase(UNIT_DB_NAME, MODE_PRIVATE, null);
        unit = myDBHandler.getUnit(unitDB);
        if (unit == 0) {
            unitsText.setText(UNIT_KG);
            unitsText2.setText(UNIT_KG);
        } else {
            unitsText.setText(UNIT_LB);
            unitsText2.setText(UNIT_LB);
        }
    }

    /*
        Get input from the text field when the 'add' button has been pressed and add it to database.
      */
    public void onAddClicked(View view) {

        EditText weightInput = (EditText) findViewById(R.id.weightInput);
        String input = weightInput.getText().toString();
        if(input.equals("")) {
            Toast.makeText(this, INVALID_INPUT_MESSAGE, Toast.LENGTH_SHORT).show();
        }
        else {
            double weight = Double.parseDouble(input);
            weighinsDB = this.openOrCreateDatabase(WEIGHIN_DB_NAME, MODE_PRIVATE, null);
            myDBHandler.addWeighin(weighinsDB, weight);
            String progressMessage = getProgressMessage(weight);
            Toast.makeText(this, progressMessage, Toast.LENGTH_SHORT).show();

            //Return to Main Menu
            Intent i = new Intent(this, MainMenu.class);
            startActivity(i);
        }
    }

    /*
    Get specialised message to display on screen which shows progress to goal, or standard message if goal weight not set.
     */
    public String getProgressMessage(double newWeight){

        goalDB=this.openOrCreateDatabase(GOAL_DB_NAME, MODE_PRIVATE, null);
        int goal = myDBHandler.getGoal(goalDB);
        goalWeightDB =this.openOrCreateDatabase(GOALWEIGHT_DB_NAME, MODE_PRIVATE, null);
        double goalWeight = myDBHandler.getGoalWeight(goalWeightDB);
        if(goalWeight!=0){
            if(goalWeight>=newWeight&&goal==LOSS){
                myDBHandler.updateGoalWeight(0,0,goalWeightDB);
                return HIT_GOAL_PROGRESS_MESSAGE;

            }
            else if(goalWeight<=newWeight&&goal==GAIN){
                myDBHandler.updateGoalWeight(0,0,goalWeightDB);
                return HIT_GOAL_PROGRESS_MESSAGE;
            }
            else{
                double difference;
                if(goal==LOSS){
                    difference=newWeight-goalWeight;
                }
                else{
                    difference=goalWeight-newWeight;
                }
                BigDecimal toGoal= new BigDecimal(Double.toString(difference));
                toGoal=toGoal.setScale(2,BigDecimal.ROUND_HALF_DOWN);
                return "Now "+toGoal.doubleValue()+unitsText.getText()+" from Goal Weight, keep it up!";
            }
        }
        return DEFAULT_PROGRESS_MESSAGE;
    }
}
