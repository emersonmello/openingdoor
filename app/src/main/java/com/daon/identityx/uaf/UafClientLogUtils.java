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
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * UAF Client-specific debug logging utility methods
 */
public class UafClientLogUtils extends LogUtils {
    /**
     * Display start registration message
     */
    public static void logRegistrationStart() {
        logDebug(TAG, "**********************");
        logDebug(TAG, "* START REGISTRATION *");
        logDebug(TAG, "**********************");
    }

    /**
     * Display start authentication message
     */
    public static void logAuthenticationStart() {
        logDebug(TAG, "************************");
        logDebug(TAG, "* START AUTHENTICATION *");
        logDebug(TAG, "************************");
    }

    /**
     * Display start deregistration message
     */
    public static void logDeregistrationStart() {
        logDebug(TAG, "************************");
        logDebug(TAG, "* START DEREGISTRATION *");
        logDebug(TAG, "************************");
    }

    /**
     * Display start discover message
     */
    public static void logDiscoverStart() {
        logDebug(TAG, "******************");
        logDebug(TAG, "* START DISCOVER *");
        logDebug(TAG, "******************");
    }

    /**
     * Display start check policy message
     */
    public static void logCheckPolicyStart() {
        logDebug(TAG, "**********************");
        logDebug(TAG, "* START CHECK POLICY *");
        logDebug(TAG, "**********************");
    }

    /**
     * Log UAF discover request intent details
     *
     * @param uafDiscoverRequest UAF client application Discover intent
     */
    public static void logUafDiscoverRequest(Intent uafDiscoverRequest) {
        logDebug(TAG, "*** UAF DISCOVER REQUEST ***");

        logUafIntentType(uafDiscoverRequest);
    }

    /**
     * Display UAFIntentType extra in UAF intent
     * @param uafIntent UAF intent
     */
    private static void logUafIntentType(Intent uafIntent) {
        String uafIntentType = uafIntent.getStringExtra("UAFIntentType");

        if(uafIntentType!=null) {
            logDebug(TAG, "UAFIntentType: " + uafIntentType);
        }
    }

    /**
     * Log UAF discover result intent details
     * @param uafDiscoverResult UAF discover result intent
     */
    public static void logUafDiscoverResult(Intent uafDiscoverResult) {
        logDebug(TAG, "*** UAF DISCOVER RESULT ***" );

        logUafIntentType(uafDiscoverResult);

        String discoveryData = uafDiscoverResult.getStringExtra("discoveryData");
        short errorCode = uafDiscoverResult.getShortExtra("errorCode", (short)-1);
        String componentName = uafDiscoverResult.getStringExtra("componentName");

        if(discoveryData!=null) {
            logDebug(TAG, "discoveryData: " + discoveryData);
        }
        if(errorCode!=-1) {
            logDebug(TAG, "errorCode: " + errorCode);
        }
        if(componentName!=null) {
            logDebug(TAG, "componentName: " + componentName);
        }
    }

    /**
     * Log UAF check policy request intent details
     * @param uafOperationRequest UAF check policy request intent
     */
    public static void logUafCheckPolicyRequest(Intent uafOperationRequest) {
        logDebug(TAG, "*** UAF CHECK POLICY REQUEST ***" );

        logUafIntentType(uafOperationRequest);

        String message = uafOperationRequest.getStringExtra("message");
        String origin = uafOperationRequest.getStringExtra("origin");

        if(message!=null) {
            logDebug(TAG, "message: " + message);
        }
        if(origin!=null) {
            logDebug(TAG, "origin: " + origin);
        }
    }

    /**
     * Log UAF check policy result intent details
     * @param uafOperationResult UAF check policy result intent
     */
    public static void logUafCheckPolicyResult(Intent uafOperationResult) {
        logDebug(TAG, "*** UAF CHECK POLICY RESULT ***" );

        logUafIntentType(uafOperationResult);

        int errorCode = uafOperationResult.getShortExtra("errorCode", (short) -1);
        String componentName = uafOperationResult.getStringExtra("componentName");

        if(errorCode!=-1) {
            logDebug(TAG, "errorCode: " + errorCode);
        }
        if(componentName!=null) {
            logDebug(TAG, "componentName: " + componentName);
        }
    }

    /**
     * Log UAF operation completion request intent details
     * @param uafOperationCompletionIntent UAF operation completion request intent
     */
    public static void logUafOperationCompletionRequest(Intent uafOperationCompletionIntent) {
        logDebug(TAG, "*** UAF OPERATION COMPLETION REQUEST ***" );

        logUafIntentType(uafOperationCompletionIntent);

        String message = uafOperationCompletionIntent.getStringExtra("message");
        int responseCode = uafOperationCompletionIntent.getShortExtra("responseCode", (short)-1);
        String responseCodeMessage = uafOperationCompletionIntent.getStringExtra("responseCodeMessage");

        if(message!=null) {
            logDebug(TAG, "message: " + message);
        }
        if(responseCode!=-1) {
            logDebug(TAG, "responseCode: " + responseCode);
        }
        if(responseCodeMessage!=null) {
            logDebug(TAG, "responseCodeMessage: " + responseCodeMessage);
        }
    }

    /**
     * Log UAF operation (reg, auth, dereg) request intent details
     * @param uafOperationRequest UAF operation intent
     * @param fidoOpType FIDO operation type
     */
    public static void logUafOperationRequest(Intent uafOperationRequest, FidoOperation fidoOpType) {
        logDebug(TAG, "*** UAF OPERATION REQUEST ***" );
        logDebug(TAG, "Operation type: " + fidoOpType );

        logUafIntentType(uafOperationRequest);

        String message = uafOperationRequest.getStringExtra("message");
        String origin = uafOperationRequest.getStringExtra("origin");
        String channelBindings = uafOperationRequest.getStringExtra("channelBindings");

        if(message!=null) {
            logDebug(TAG, "message: " + message);
        }
        if(origin!=null) {
            logDebug(TAG, "origin: " + origin);
        }
        if(channelBindings!=null) {
            logDebug(TAG, "channelBindings: " + channelBindings);
        }
    }

    /**
     * Log result intent from a UAF client operation
     * @param clientResultIntent result intent
     * @param fidoOpType FIDO operation type
     */
    public static void logClientResultIntent(Intent clientResultIntent, FidoOperation fidoOpType) {
        switch(fidoOpType) {
            case Registration:
            case Authentication:
            case Deregistration:
                logUafOperationResult(clientResultIntent, fidoOpType);
                break;
            case Discover:
                logUafDiscoverResult(clientResultIntent);
                break;
            case CheckPolicy:
                logUafCheckPolicyResult(clientResultIntent);
                break;
        }
    }

    /**
     * Log UAF client activities that are available on the device
     * @param list list of available UAF client activities
     */
    public static void logUafClientActivities(List<ResolveInfo> list) {
        logDebug(TAG, "-~-~-~-~-~-~-~-~-~-~-~-~-~-");
        logDebug(TAG, "---UAF Client Activities---");

        if (list != null && list.size() > 0) {
            for(ResolveInfo resolveInfo: list) {
                logDebug(TAG, resolveInfo.activityInfo.toString());
            }
        } else {
            logDebug(TAG, "None");
        }

        logDebug(TAG, "-~-~-~-~-~-~-~-~-~-~-~-~-~-");
    }

    /**
     * Log UAF operation (reg, auth, dereg) result intent
     * @param uafOperationResult UAF operation result intent
     * @param fidoOpType FIDO operation type
     */
    public static void logUafOperationResult(Intent uafOperationResult, FidoOperation fidoOpType) {
        logDebug(TAG, "*** UAF OPERATION RESULT ***" );
        logDebug(TAG, "Operation type: " + fidoOpType);

        logUafIntentType(uafOperationResult);

        String message = uafOperationResult.getStringExtra("message");
        int errorCode = uafOperationResult.getShortExtra("errorCode", (short)-1);
        String componentName = uafOperationResult.getStringExtra("componentName");

        if(message!=null) {
            logDebug(TAG, "message: " + message);
        }
        if(errorCode!=-1) {
            logDebug(TAG, "errorCode: " + errorCode);
        }
        if(componentName!=null) {
            logDebug(TAG, "componentName: " + componentName);
        }
    }

    public static void logUafClientDetails(ResolveInfo resolveInfo) {
        if(resolveInfo!=null & resolveInfo.activityInfo!=null) {
            if(resolveInfo.activityInfo.packageName != null) {
                logDebug(TAG, "UAF Client package name: " + resolveInfo.activityInfo.packageName);
            }
            if(resolveInfo.activityInfo.name != null) {
                logDebug(TAG, "UAF Client class: " + resolveInfo.activityInfo.name);
            }
        }
    }
}
