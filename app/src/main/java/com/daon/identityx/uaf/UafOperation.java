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

import com.google.gson.Gson;

/**
 * Created by Daon
 */
public class UafOperation {

    private final UAFIntentType uafIntentType;
    private final UAFMessage    message;
    private final String origin;
    private final String channelBindings;
    private final Short responseCode;
    private final String responseCodeMessage;

    UafOperation(final UAFIntentType uafIntentType, final UAFMessage message, final String origin, final String channelBindings,
                 final Short responseCode, final String responseCodeMessage) {

        this.uafIntentType = uafIntentType;
        this.message = message;
        this.origin = origin;
        this.channelBindings = channelBindings;
        this.responseCode = responseCode;
        this.responseCodeMessage = responseCodeMessage;
    }

    public Intent toIntent() {
        final Intent result = new Intent();
        result.setAction(AndroidClientIntentParameters.intentAction);
        result.setType(AndroidClientIntentParameters.intentType);
        Gson gson = new Gson();

        if (null != uafIntentType) {
            result.putExtra("UAFIntentType", uafIntentType.getDescription());
        }
        if (message != null) {
            result.putExtra("message", gson.toJson(message));
        }
        if (origin != null) {
            result.putExtra("origin", origin);
        }
        if (channelBindings != null) {
            result.putExtra("channelBindings", channelBindings);
        }
        if (responseCode != null) {
            result.putExtra("responseCode", (short) responseCode);
        }
        if (responseCodeMessage != null) {
            result.putExtra("responseCodeMessage", responseCodeMessage);
        }

        return result;
    }

    public UAFIntentType getUafIntentType() {
        return uafIntentType;
    }

    public UAFMessage getMessage() {
        return message;
    }

    public String getOrigin() {
        return origin;
    }

    public String getChannelBindings() {
        return channelBindings;
    }

    public Short getResponseCode() {
        return responseCode;
    }

    public String getResponseCodeMessage() {
        return responseCodeMessage;
    }
}
