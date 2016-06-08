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
public enum UAFIntentType {

    DISCOVER(0, "DISCOVER"),
    DISCOVER_RESULT(1, "DISCOVER_RESULT"),
    CHECK_POLICY(2, "CHECK_POLICY"),
    CHECK_POLICY_RESULT(3, "CHECK_POLICY_RESULT"),
    UAF_OPERATION(4, "UAF_OPERATION"),
    UAF_OPERATION_RESULT(5, "UAF_OPERATION_RESULT"),
    UAF_OPERATION_COMPLETION_STATUS(6, "UAF_OPERATION_COMPLETION_STATUS");

    private final int    VALUE;
    private final String DESCRIPTION;

    public static UAFIntentType getByValue(final String description) {
        for (final UAFIntentType uafIntentType : values()) {
            if (uafIntentType.getDescription().equals(description)) {
                return uafIntentType;
            }
        }
        throw new IllegalArgumentException("Invalid uaf intent type description: " + description);
    }

    public int getValue() {
        return VALUE;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    UAFIntentType(final int value, final String description) {
        this.VALUE = value;
        this.DESCRIPTION = description;
    }
}
