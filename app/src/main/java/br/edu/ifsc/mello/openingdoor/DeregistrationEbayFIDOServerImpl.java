package br.edu.ifsc.mello.openingdoor;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import com.google.gson.Gson;

import org.ebayopensource.fidouaf.marvin.client.msg.DeregisterAuthenticator;
import org.ebayopensource.fidouaf.marvin.client.msg.DeregistrationRequest;
import org.ebayopensource.fidouaf.marvin.client.msg.Operation;
import org.ebayopensource.fidouaf.marvin.client.msg.OperationHeader;
import org.ebayopensource.fidouaf.marvin.client.msg.Version;
import org.json.JSONObject;

import java.security.MessageDigest;

/**
 * The code present here if from github.com/eBay/UAF Android RP app.
 * It only works with eBay RP FIDO Server APP
 */
public class DeregistrationEbayFIDOServerImpl implements IDeregistration {


    @Override
    public String getDeregJsonMessage(SharedPreferences sharedPreferences) {
        try {
            Gson gson = new Gson();
            DeregistrationRequest[] regResponse = new DeregistrationRequest[1];
            regResponse[0] = new DeregistrationRequest();

            DeregisterAuthenticator deregisterAuthenticator = new DeregisterAuthenticator();
            deregisterAuthenticator.aaid = sharedPreferences.getString("aaid", "");
            deregisterAuthenticator.keyID = sharedPreferences.getString("keyid", "");

            regResponse[0].header = new OperationHeader();
            regResponse[0].header.upv = new Version(1, 0);
            regResponse[0].header.op = Operation.Dereg;
            regResponse[0].header.appID = this.getFacetId();
            regResponse[0].authenticators = new DeregisterAuthenticator[1];
            regResponse[0].authenticators[0] = deregisterAuthenticator;

            String jsonRegResponse = gson.toJson(regResponse, DeregistrationRequest[].class);

            return jsonRegResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getFacetId() {
        String comma = "";
        try {
            StringBuilder result = new StringBuilder();
            PackageInfo packageInfo = ApplicationContextDoorLock.getContext().getPackageManager().getPackageInfo(ApplicationContextDoorLock.getContext().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature sign : packageInfo.signatures) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
                messageDigest.update(sign.toByteArray());
                String currentSignature = Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT);
                result.append("android:apk-key-hash:");
                result.append(currentSignature.substring(0, currentSignature.length() - 2));
                result.append(comma);
                comma = ",";
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
