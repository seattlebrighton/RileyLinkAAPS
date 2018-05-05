package com.gxwtech.roundtrip2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ShowAAPSActivity extends AppCompatActivity {

    TextView textViewComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_aaps);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        this.textViewComm = findViewById(R.id.textViewComm);


        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }


    public void onAAPSGetStatusButtonClick(View view) {
        putOnDisplay("onAAPSGetStatusButtonClick");
    }

    public void onAAPSSetTBRButtonClick(View view) {
        putOnDisplay("onAAPSSetTBRButtonClick");
    }

    public void onAAPSCancelTBRButtonClick(View view) {
        putOnDisplay("onAAPSCancelTBRButtonClick");
    }

    public void onAAPSSetBolusButtonClick(View view) {
        putOnDisplay("onAAPSSetBolusButtonClick");
    }

    public void onAAPSCancelBolusButtonClick(View view) {
        putOnDisplay("onAAPSCancelBolusButtonClick");
    }

    public void onAAPSGetBasalProfileButtonClick(View view) {
        putOnDisplay("onAAPSGetBasalProfileButtonClick");
    }


    public void onAAPSSetBasalProfileButtonClick(View view) {
        putOnDisplay("onAAPSSetBasalProfileButtonClick");
    }

    public void onAAPSReadHistoryButtonClick(View view) {
        putOnDisplay("onAAPSReadHistoryButtonClick");
    }


    public void onAAPSSetExtBolusButtonClick(View view) {
        putOnDisplay("onAAPSSetExtBolusButtonClick");
    }


    public void onAAPSCancelExtBolusButtonClick(View view) {
        putOnDisplay("onAAPSCancelExtBolusButtonClick");
    }


    public void onAAPSLoadTDDButtonClick(View view) {
        putOnDisplay("onAAPSLoadTDDButtonClick");
    }


    public void putOnDisplay(String text)
    {
        this.textViewComm.append(text + "\n");
    }

}
