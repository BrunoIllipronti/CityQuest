package com.illipronti.cityquest;
import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;

public class QrReader extends AppCompatActivity {

    Context context;
    IntentIntegrator integrator;

    public QrReader(Context c){
        context = c;
        integrator = new IntentIntegrator((Activity) c);
    }

    public void readCode(){
        // Set capture activity based on Class
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("En garde! Magic Scanner activated!");
        integrator.initiateScan();
    }

}
