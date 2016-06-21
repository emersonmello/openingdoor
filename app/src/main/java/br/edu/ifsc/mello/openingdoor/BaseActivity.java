/*
* Copyright Daon.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package br.edu.ifsc.mello.openingdoor;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.daon.identityx.exception.UafProcessingException;
import com.daon.identityx.uaf.AndroidClientIntentParameters;
import com.daon.identityx.uaf.FidoOperation;
import com.daon.identityx.uaf.IUafClientUtils;
import com.daon.identityx.uaf.LogUtils;
import com.daon.identityx.uaf.UafClientLogUtils;
import com.daon.identityx.uaf.UafClientUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This abstract base class is the super class of all the activities in the project.
 * It provides the ability to handle the processing of FIDO intents and is used to
 * determine if there is a FIDO client on the device and the authenticators available.
 *
 */
public abstract class BaseActivity extends AppCompatActivity {

    FidoOperation currentFidoOperation;

    protected enum FidoOpCommsType {OneWay, Return}

    // The list of FIDO Clients
    static List<ResolveInfo> uafClientList = new ArrayList<>();

    // The set of authenticators on the device
    private static Set<String> availableAuthenticatorAaids = new HashSet<>();

    // UAF Client Utility class
    private static final IUafClientUtils uafClientUtils = new UafClientUtils();


    /**
     * Initialise global interfaces which are made available to all activities which derive from this class
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * React to user selecting Settings
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Send a UAF intent to a UAF client.
     * <p>If a client cannot be found, the operation fails.</p>
     * @param uafClientIntent intent
     * @param opCommsType comms type. {com.daon.identityx.fidoapp.BaseActivity.FidoOpCommsType#Return} indicates that the
     *                    intent will generate a result from the client.
     *                    {com.daon.identityx.fidoapp.BaseActivity.FidoOpCommsType#OneWay} indicates that the intent is
     *                    one-way and has no result.
     */
    protected void sendUafClientIntent(Intent uafClientIntent, FidoOpCommsType opCommsType) {
        try{
            if(opCommsType== FidoOpCommsType.Return) {
                startActivityForResult(uafClientIntent, AndroidClientIntentParameters.requestCode);
            } else {
                startActivity(uafClientIntent);
            }
        }catch (Exception e){
            throw new UafProcessingException((String)getText(R.string.no_fido_client_found));
        }
    }

    /**
     * Log a UAF operation completion intent and send it to the UAF client.
     * @param uafOperationCompletionIntent intent
     */
    protected void sendFidoOperationCompletionIntent(Intent uafOperationCompletionIntent) {
        UafClientLogUtils.logUafOperationCompletionRequest(uafOperationCompletionIntent);
        this.sendUafClientIntent(uafOperationCompletionIntent, FidoOpCommsType.OneWay);
    }

    /***
     * This is the generic callback handler.  This method assumes that the callback is for the
     * purpose of receiving the FIDO client response.  The method will examine the response
     * and determine if the response has been successful or not and calls the appropriate
     * method (processUafClientResponse, onActivityResultFailure) depending on whether the
     * call has been successful or not.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Received response from FIDO UAF Client. Process it on success, or display error on failure
        try {
            super.onActivityResult(requestCode, resultCode, data);
            Log.d(LogUtils.TAG, "********************************");
            Log.d(LogUtils.TAG, "***** onActivityResult. requestCode = " + requestCode + ". resultCode = " + resultCode + " *********");
            Log.d(LogUtils.TAG, "********************************");
            if (requestCode == AndroidClientIntentParameters.requestCode) {
                String uafResponseJson;
                switch (resultCode) {
                    case RESULT_OK:
                        Log.d(LogUtils.TAG, "RESULT_OK");
                        uafResponseJson = uafClientUtils.getUafClientResponse(currentFidoOperation, data);
                        this.processUafClientResponse(uafResponseJson);
                        break;
                    case RESULT_CANCELED:
                        Log.d(LogUtils.TAG, "RESULT_CANCELLED");
                        if (data == null) {
                            Log.d(LogUtils.TAG, "No intent returned");
                            this.onActivityResultFailure((String) getText(R.string.operation_cancelled));
                        } else {
                            Log.d(LogUtils.TAG, "Intent returned, process it.");
                            uafResponseJson = uafClientUtils.getUafClientResponse(currentFidoOperation, data);
                            this.processUafClientResponse(uafResponseJson);
                        }
                        break;
                    default:
                        Log.d(LogUtils.TAG, "Unexpected activity result code: " + resultCode);
                        this.onActivityResultFailure(getText(R.string.activity_result_error) + Integer.toString(resultCode));
                        break;
                }
            }
        } catch(Throwable ex) {
            Log.d(LogUtils.TAG, "UAF Client Activity Error: " + ex.getMessage());
            this.onActivityResultFailure(ex.getMessage());
        }
    }

    /***
     * This is a default implementation of the FIDO activity callback when the FIDO client
     * calls back to this app and the response has been successful.
     *
     * @param uafResponseJson
     */
    protected void processUafClientResponse(String uafResponseJson) {
        Log.d(LogUtils.TAG,"processing uafClient response");
    }

    /***
     * This is a default implementation of the FIDO activity callback when the FIDO client
     * calls back to this app and the response has been unsuccessful.
     *
     * @param errorMsg
     */
    protected void onActivityResultFailure(String errorMsg) {}

    protected void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected boolean hasFIDOClient() {

        return (getUafClientList().size() > 0);
    }

    protected static List<ResolveInfo> getUafClientList() {
        return uafClientList;
    }

    protected static boolean hasAuthenticator(String aaid) {

        return availableAuthenticatorAaids.contains(aaid);
    }

    public Set<String> getAvailableAuthenticatorAaidsAsSet() {
        return availableAuthenticatorAaids;
    }

    public String[] getAvailableAuthenticatorAaids() {
        return availableAuthenticatorAaids.toArray(new String[0]);
    }

    protected FidoOperation getCurrentFidoOperation() {
        return this.currentFidoOperation;
    }

    protected void setCurrentFidoOperation(FidoOperation currentFidoOperation) {
        this.currentFidoOperation = currentFidoOperation;
    }

//    protected static IRelyingPartyComms getRelyingPartyComms() {
//        return relyingPartyComms;
//    }

    protected static IUafClientUtils getUafClientUtils() {
        return uafClientUtils;
    }

}
