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
public class UafOperationFactory {

    public static UafOperation createDiscover() {
        return new UafOperation(UAFIntentType.DISCOVER, null, null, null, null, null);
    }

    public static UafOperation createCheckPolicy(final UAFMessage message, final String origin) {
        return new UafOperation(UAFIntentType.CHECK_POLICY, message, origin, null, null, null);
    }

    public static UafOperation createUAFOperation(final UAFMessage message, final String origin, final String channelBindings) {
        return new UafOperation(UAFIntentType.UAF_OPERATION, message, origin, channelBindings, null, null);
    }

    public static UafOperation createUAFOperationCompletionStatus(final UAFMessage message, final Short responseCode,
                                                                  final String responseCodeMessage) {
        return new UafOperation(UAFIntentType.UAF_OPERATION_COMPLETION_STATUS, message, null, null, responseCode,
                responseCodeMessage);
    }
}
