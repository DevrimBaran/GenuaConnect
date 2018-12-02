package de.uni_stuttgart.informatik.sopra.sopraapp;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;


public class MainActivity extends AppCompatActivity {

    //Intent that can start the scan of qr codes
    IntentIntegrator intentIntegrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intent intent = new Intent(this, RotatingCaptureActivity.class);
        //startActivity(intent);

        //initialising the IntentIntegrator and setting a few options
         intentIntegrator = new IntentIntegrator(this);
         intentIntegrator.setBeepEnabled(false);
         intentIntegrator.setOrientationLocked(false);
         intentIntegrator.setCaptureActivity(RotatingCaptureActivity.class);
         intentIntegrator.initiateScan();
    }


}
