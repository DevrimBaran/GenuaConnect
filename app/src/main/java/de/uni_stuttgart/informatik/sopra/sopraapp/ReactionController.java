package de.uni_stuttgart.informatik.sopra.sopraapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import de.uni_stuttgart.informatik.sopra.sopraapp.Requests.RequestMask;
import de.uni_stuttgart.informatik.sopra.sopraapp.SNMP.SimpleSNMPClientV1AndV2c;
import de.uni_stuttgart.informatik.sopra.sopraapp.SNMP.SimpleSNMPClientv3;
import de.uni_stuttgart.informatik.sopra.sopraapp.SNMP.SnmpTask;

public class ReactionController {
    private String qrCode;
    private Activity activity;
    private RequestMask requestMask;

    public ReactionController(Activity activity, String qrCode) {
        this.qrCode = qrCode;
        this.activity = activity;
        requestMask = new RequestMask();

        Log.d("Reacting To QR-Code", "QR-String = " + qrCode);
        if (qrCode.contains("WIFI")) {
            Log.d("Reacting To QR-Code", "detected a WIFI QR-String");
            new WifiConnect().tryConnect(qrCode, activity);
        } else if (!new ApplianceQrDecode(qrCode).getPassword().equals("")) {
            Log.d("Reacting To QR-Code", "detected a Appliance QR-String");

            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED&& ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, 2);
            } else {
                SimpleSNMPClientv3 client = new SimpleSNMPClientv3(qrCode);
                Toast.makeText(activity, "SNMP version 3 wird noch nicht unterstützt", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("React to QR-Code V1/V2c", "detected a Appliance QR-String V1/V2c");

            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET}, 3);
            } else {
                SimpleSNMPClientV1AndV2c clientv2c = new SimpleSNMPClientV1AndV2c(qrCode);

                SnmpTask snmpTask1 = new SnmpTask(clientv2c, activity);
                Log.d("query snmp", "querying syslocation: .1.3.6.1.2.1.1.4.0");
                snmpTask1.execute(".1.3.6.1.2.1.1.4.0");

                try {
                    String result1 = snmpTask1.get();
                    Log.d("snmptest", "result: " + result1);
                    Toast.makeText(activity, result1, Toast.LENGTH_LONG).show();
                    if (result1 == null) {
                        Toast.makeText(activity, "SNMP Abfrage hat nicht funktioniert. Bitte überprüfen Sie ob die Abfrage richtig ist.", Toast.LENGTH_LONG).show();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}