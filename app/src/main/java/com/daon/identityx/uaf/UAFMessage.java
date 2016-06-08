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

/**
 * Created by Daon
 */
public class UAFMessage {

    final private String uafProtocolMessage;
    final private Object additionalData;

    /**
     *
     * @param uafProtocolMessage
     *            The JSON-serialised UAF message, e.g. RegistrationRequest, AuthenticationRequest,
     *            etc...
     * @param additionalData
     *            Allows the FIDO Server or client application to attach additional data for use by
     *            the FIDO UAF Client as a JSON object, or the FIDO UAF Client or client application
     *            to attach additional data for use by the client application.
     */
    public UAFMessage(final String uafProtocolMessage, final Object additionalData) {
        this.uafProtocolMessage = uafProtocolMessage;
        this.additionalData = additionalData;
    }

    public String getUafProtocolMessage() {
        return uafProtocolMessage;
    }

    public Object getAdditionalData() {
        return additionalData;
    }
}
