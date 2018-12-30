package de.uni_stuttgart.informatik.sopra.sopraapp.SNMP;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.uni_stuttgart.informatik.sopra.sopraapp.ApplianceQrDecode;

/**
 * TODO
 */
public class SimpleSNMPClientv3 {

    /**
     * Inspired by https://blog.jayway.com/2010/05/21/introduction-to-snmp4j
     * A client for the SNMP version3 management.
     * Maybe will be done in next sprint.
     */
    String address;
    private volatile Snmp snmp;
    private UserTarget target;
    private TransportMapping<UdpAddress> transportMapping;
    private ApplianceQrDecode decode;

    public SimpleSNMPClientv3(String qrCode) {
        decode = new ApplianceQrDecode(qrCode);
        this.address = decode.getAddress();
        Log.d("StartAusfuehren", "Ausgefueht");
    }

    public void stop() throws IOException {
        snmp.close();
        Log.d("Stop", "Gestoppt");
    }

    /**
     * Starts the SNMP Interface.
     *
     * @throws IOException
     */
    private void start() throws IOException {
        transportMapping = new DefaultUdpTransportMapping();
        Log.d("Snmp Connect", "asynchroner Nebenthread gestartet");

        snmp = new Snmp(transportMapping);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        try {
            transportMapping.listen();
        } catch (IOException e) {
            Log.e("connect", e.getMessage());
        }
        Log.d("Snmp Connect", "isListening: " + transportMapping.isListening());
    }

    /**
     * Returns a target, which contains the information about to where and how the data should be fetched.
     *
     * @return Returns the given target.
     */
    protected Target getTarget() {
        if (target != null) {
            return target;
        }
        String addrToUse = address;
        String port = "";
        if (address.lastIndexOf(':') != -1) {
            // strip port out of address
            addrToUse = address.substring(0, address.lastIndexOf(':'));
            port = address.substring(1);
        }
        Log.d("address", "received address: " + addrToUse);
        Address targetAdress = null;
        // Da in SoPra "/" benutzt wird fuer die Trennung des Ports.
        if (decode.getAddress().contains("/")) {
            Log.d("/ oder : zur Trennung", "'/' erkannt");
            if (port == "") {
                Log.d("port", "port null");
                targetAdress = GenericAddress.parse("udp:" + addrToUse + "/" + "161");
            } else {
                Log.d("port", "port nicht null");
                targetAdress = GenericAddress.parse("udp:" + addrToUse + "/" + port);
            }
            // Fuer Unterstuetzung des internationalen Standards (Ports werden normalerweise mit ":" getrennt),
            // damit wir des auf dem PC oder raspberry pie testen Koennen, da wir sonst immer hoch muessen im Info Gebaeude.
        } else if (decode.getAddress().contains(":")) {
            Log.d("/ oder : zur Trennung", "':' erkannt");
            if (port == "") {
                Log.d("port", "port null");
                targetAdress = GenericAddress.parse("udp:" + addrToUse + ":" + "161");
            } else {
                Log.d("port", "port nicht null");
                targetAdress = GenericAddress.parse("udp:" + addrToUse + ":" + port);
            }
        }
        System.out.println(targetAdress);
        Address targetAddress = GenericAddress.parse(address);
        target = new UserTarget();
        target.setAddress(targetAddress);
        //target.setSecurityName();
        target.setRetries(2);
        target.setTimeout(5000);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(new OctetString("batmanuser"));
        Log.d("getTarget", "gesettet");
        return target;
    }

    /**
     * Handles multiple OIDs.
     *
     * @param oids Array of OIDs.
     * @return Returns the ResponseEvent.
     * @throws IOException
     */
    public ResponseEvent get(OID oids[]) throws IOException {
        ScopedPDU pdu = new ScopedPDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(ScopedPDU.GET);
        ResponseEvent event = snmp.send(pdu, getTarget(), transportMapping);
        if (event != null) {
            return event;
        }
        throw new RuntimeException("Zeitüberschreitung für GET");
    }

    /**
     * Returns the response of the OID as a string.
     *
     * @param oid Is the OID.
     * @return Returns the response.
     * @throws IOException
     */
    public String getAsString(OID oid) throws IOException {
        Log.d("getAsString", "String bekommen: " + oid.toDottedString());
        return sendGet(oid.toString());
    }

    public void getAsString(OID oids, ResponseEvent listener) {
        try {
            snmp.send(getPDU(new OID[]{oids}), getTarget(), null, (ResponseListener) listener);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the PDU.
     *
     * @param oids Array of the OIDs.
     * @return Returns the getted PDU.
     */
    private ScopedPDU getPDU(OID oids[]) {
        ScopedPDU pdu = new ScopedPDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(ScopedPDU.GET);
        Log.d("getPDU", "got the PDU");
        return pdu;
    }

    /**
     * Lists the informations of the OIDs as a table.
     *
     * @param oids Array of OIDs.
     * @return Returns the List.
     */
    public List<List<String>> getTableAsStrings(OID[] oids) {
        TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory());
        List<TableEvent> events = utils.getTable(getTarget(), oids, null, null);
        List<List<String>> list = new ArrayList<List<String>>();
        for (TableEvent event : events) {
            if (event.isError()) {
                throw new RuntimeException(event.getErrorMessage());
            }
            List<String> stringsList = new ArrayList<String>();
            list.add(stringsList);
            for (VariableBinding variableBinding : event.getColumns()) {
                stringsList.add(variableBinding.getVariable().toString());
            }
        }
        return list;
    }

    /**
     * @param stringOID
     * @return
     */
    public String sendGet(String stringOID) {
        ScopedPDU scopedPDU = (ScopedPDU) DefaultPDUFactory.createPDU(1);

        //add OID to PDU
        scopedPDU.add(new VariableBinding(new OID(stringOID)));

        //setting type when sending request to the SNMP server
        scopedPDU.setType(ScopedPDU.GETNEXT);

        ResponseEvent responseEvent;
        try {
            //sending the request
            responseEvent = snmp.get(scopedPDU, getTarget());

            if (responseEvent != null) {
                System.out.println(responseEvent);
                //get the PDU response
                ScopedPDU pduResult = (ScopedPDU) responseEvent.getResponse();
                System.out.println(pduResult);
                if (pduResult == null) {
                    return null;
                }

                for (VariableBinding varBind : pduResult.getVariableBindings()) {
                    return varBind.toValueString();
                }

                //checking if response throws an error
                if (pduResult.getErrorStatus() == ScopedPDU.noError) {

                    //gets the variable when there's no error
                    Variable variable = pduResult.getVariable(new OID(stringOID));

                    if (variable != null) {
                        //result
                        return variable.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractSingleString(ResponseEvent event) {
        return event.getResponse().get(0).getVariable().toString();
    }
}
