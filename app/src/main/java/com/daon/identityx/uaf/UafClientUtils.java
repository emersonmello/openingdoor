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

package com.daon.identityx.uaf;

import android.content.Intent;
import android.util.Log;

import com.daon.identityx.exception.UafProcessingException;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of {@link IUafClientUtils} which conforms to v1.0 FIDO UAF protocols.
 *
 * Created by Daon
 */
public class UafClientUtils implements IUafClientUtils {

    private final static String channelBindings =
            "{\"serverEndPoint\":null,\"tlsServerCertificate\":null,\"tlsUnique\":null,\"cid_pubkey\":null}";

    /**
     * {@inheritDoc}
     */
    @Override
    public Intent getUafOperationIntent(FidoOperation fidoOpType, String uafRequest) {
        switch(fidoOpType) {
            case Registration:
            case Authentication:
            case Deregistration:
                UafOperation uafOperation = this.getFidoUafOperation(uafRequest);
                Intent uafOperationIntent = uafOperation.toIntent();
                UafClientLogUtils.logUafOperationRequest(uafOperationIntent, fidoOpType);
                return uafOperationIntent;
            case Discover:
            case CheckPolicy:
            default:
                throw new UafProcessingException("Invalid FIDO operation type specified: " + fidoOpType.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Intent getDiscoverIntent() {
        UafOperation discoverOperation = this.getFidoDiscoverOperation();
        return discoverOperation.toIntent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Intent getCheckPolicyIntent(String uafRequest) {
        UafOperation checkPolicyOperation = this.getFidoCheckPolicyOperation(uafRequest);
        return checkPolicyOperation.toIntent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Intent getUafOperationCompletionStatusIntent(String uafResponseMsg, int serverResponseCode, String serverResponseMsg) {
        return this.getFidoCompletionOperation(uafResponseMsg, serverResponseCode,
                serverResponseMsg).toIntent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUafClientResponse(FidoOperation fidoOpType, Intent resultIntent) {
        // Check response intent from UAF client for errors. If it's OK send the response message created by the client to the server.
        Log.d(LogUtils.TAG, "processClientResultIntent called.");
        UafClientLogUtils.logClientResultIntent(resultIntent, fidoOpType);
        short errorCode = resultIntent.getShortExtra("errorCode", (short) -1);
        if (errorCode != ErrorCode.NO_ERROR.getValue()) {
            throw new UafProcessingException("FIDO Client Processing Error: " + ErrorCode.getByValue(errorCode).getDescription());
        } else {
            String intentType = resultIntent.getStringExtra("UAFIntentType");
            if(intentType!=null) {
                switch (intentType) {
                    case "UAF_OPERATION_RESULT":
                        String fidoUafMessage = resultIntent.getStringExtra("message");
                        if(fidoUafMessage!=null) {
                            Gson gson = new Gson();
                            //UAFMessage uafMessage = gson.fromJson(fidoUafMessage, UAFMessage.class);
                            //String uafResponseJson = uafMessage.getUafProtocolMessage();
                            //return uafMessage.getUafProtocolMessage();
                            return fidoUafMessage;
                        } else {
                            return null;
                        }
                    case "DISCOVER_RESULT":
                        return resultIntent.getStringExtra("discoveryData");
                    case "CHECK_POLICY_RESULT":
                        Log.d(LogUtils.TAG, "checking policy result.");
                        return null;
                    default:
                        throw new UafProcessingException("Unrecognised UAF client response intent type: " + intentType);
                }
            } else {
                throw new UafProcessingException("UAF client response intent is missing the UAFIntentType extra.");
            }
        }
    }

    /**
     * Creates a FIDO UAF operation from a UAF message and a hard-coded {@link #channelBindings} string.
     *
     * @param uafRequest UAF request message
     * @return FIDO UAF operation
     */
    private UafOperation getFidoUafOperation(String uafRequest) {
        UAFMessage uafMessage = new UAFMessage(uafRequest, null);
        return UafOperationFactory.createUAFOperation(uafMessage, null, channelBindings);
    }

    /**
     * Creates a FIDO UAF discover operation
     * @return FIDO UAF discover operation
     */
    private UafOperation getFidoDiscoverOperation() {
        return UafOperationFactory.createDiscover();
    }

    /**
     * Converts a FIDO version JSON object to a string
     * @param versionObj version object
     * @return String representation of version object
     * @throws JSONException if a parsing error occurs
     */
    private String getVersionString(JSONObject versionObj) throws JSONException {
        return versionObj.getInt("major") + "." + versionObj.getInt("minor");
    }

    /**
     * Creates a FIDO UAF check policy operation for a specified UAF request message.
     * @param uafRequest UAF request message.
     * @return FIDO UAF check policy operation
     */
    private UafOperation getFidoCheckPolicyOperation(String uafRequest) {
        UAFMessage uafMessage = new UAFMessage(uafRequest, null);
        return UafOperationFactory.createCheckPolicy(uafMessage, null);
    }

    /**
     * Creates a FIDO UAF operation completion operation.
     * @param uafResponse response message from a UAF operation which has completed either successfully or with an error.
     * @param responseCode server response code
     * @param responseCodeMessage server response message (optional, may be null)
     * @return FIDO UAF operation completion operation
     */
    private UafOperation getFidoCompletionOperation(String uafResponse, int responseCode, String responseCodeMessage) {
        UAFMessage uafMessage = new UAFMessage(uafResponse, null);
        return UafOperationFactory.createUAFOperationCompletionStatus(uafMessage, (short) responseCode, responseCodeMessage);
    }
}
