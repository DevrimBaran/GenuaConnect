package de.uni_stuttgart.informatik.sopra.sopraapp.cameraQrCode;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import de.uni_stuttgart.informatik.sopra.sopraapp.R;
import de.uni_stuttgart.informatik.sopra.sopraapp.monitoring.ApplianceManager;
import de.uni_stuttgart.informatik.sopra.sopraapp.snmp.SimpleSNMPClientV1AndV2c;
import de.uni_stuttgart.informatik.sopra.sopraapp.snmp.SimpleSNMPClientv3;
import de.uni_stuttgart.informatik.sopra.sopraapp.wifi.WifiConnect;

/**
 * This class defines how to react if a QR-Code is scanned.
 */
class ReactionController {

    /**
     * Constructor
     *
     * @param activity The transfered activity.
     * @param qrCode   Decodes the scanned QR-Code.
     */
    ReactionController(Activity activity, String qrCode) {
        Log.d("Reacting To QR-Code", "QR-String = " + qrCode);

        try {
            //WIFI QR Code
            if (qrCode.contains("WIFI")) {
                Log.d("Reacting To QR-Code", "detected a WIFI QR-String");
                new WifiConnect().tryConnect(qrCode, activity);

                // SNMPv3 QR COde
            } else if (new ApplianceQrDecode(qrCode).getSnmpVersion().equals("3")) {
                ApplianceManager applianceManager = ApplianceManager.getInstance(activity);
                applianceManager.addClient(new SimpleSNMPClientv3(qrCode));
                Toast.makeText(activity, activity.getString(R.string.detected_apliance) + 3, Toast.LENGTH_SHORT).show();

                //SNMPv1v2c QR Code.
            } else if (new ApplianceQrDecode(qrCode).getSnmpVersion().equals("1") || new ApplianceQrDecode(qrCode).getSnmpVersion().equals("2c")) {
                ApplianceManager applianceManager = ApplianceManager.getInstance(activity);
                applianceManager.addClient(new SimpleSNMPClientV1AndV2c(qrCode));
                Toast.makeText(activity, activity.getString(R.string.detected_apliance) + new ApplianceQrDecode(qrCode).getSnmpVersion(), Toast.LENGTH_SHORT).show();
            }


        }catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(activity, "Something went wrong. Check QR-Code format.", Toast.LENGTH_LONG).show();
        }
    }
}