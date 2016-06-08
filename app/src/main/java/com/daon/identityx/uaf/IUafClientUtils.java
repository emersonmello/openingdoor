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

import com.daon.identityx.exception.UafProcessingException;

/**
 * UAF client intent management methods.
 * Created by Daon
 */
public interface IUafClientUtils {
    /**
     * Gets an intent which will perform a UAF Registration, Authentication or Deregistration operation using the UAF client app.
     * @param fidoOpType FIDO UAF operation type (Registration, Authentication, Deregistration)
     * @param uafRequest UAF request message
     * @return Intent
     */
    Intent getUafOperationIntent(FidoOperation fidoOpType, String uafRequest);

    /**
     * Gets an intent which will perform a discovery operation using the UAF client app.
     *
     */
    Intent getDiscoverIntent();

    /**
     * Gets an intent which will perform a check policy operation using the UAF client app.
     * @param uafRequest UAF request message
     */
    Intent getCheckPolicyIntent(String uafRequest);

    /**
     * Gets an intent which will perform a UAF operation completion operation using the UAF client app.
     * @param uafResponseMsg response message from the UAF message which has just been processed by the UAF server.
     * @param serverResponseCode server response code - see section 3.1 of FIDO UAF Application API and Transport Binding Specification
     *                           v1.0 for values
     * @param serverResponseMsg server response message (optional - may be null)
     */
    Intent getUafOperationCompletionStatusIntent(String uafResponseMsg, int serverResponseCode, String serverResponseMsg);

    /**
     * Return response data from a UAF message.
     * @param fidoOpType FIDO UAF operation type
     * @param resultIntent UAF client result intent
     * @return client output in JSON format - the UAF response message in the case of Registration and Authentication and the
     * DiscoveryData for a Discovery operation. Deregistration and Check Policy operations return null.
     * @throws UafProcessingException with error details if the intent contains an error
     */
    String getUafClientResponse(FidoOperation fidoOpType, Intent resultIntent) throws UafProcessingException;
}
