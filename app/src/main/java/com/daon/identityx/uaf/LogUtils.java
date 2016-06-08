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

import android.util.Log;

import java.util.List;

/**
 * Base class for log utility methods
 * Created by Daon
 */
public class LogUtils {

    /**
     * Default tag used for debug logging
     */
    public static String TAG = "OpeningDoors";

    /**
     * Logs a message using {@link Log#d(String, String)}. The message is split into multiple chunks if it is too long for logcat.
     *
     * @param tag     Used to identify the source of a log message.
     * @param message message to log
     */
    public static void logDebug(String tag, String message) {
        int maxLogMsgLen = 4000;

        if (message == null) {
            return;
        }
        int messageLength = message.length();
        if (messageLength <= maxLogMsgLen) {
            Log.d(tag, message);
        } else {
            int start = 0;
            int end = maxLogMsgLen;
            boolean done = false;
            do {
                Log.d(tag, message.substring(start, end));
                if (end == messageLength) {
                    done = true;
                } else {
                    start += maxLogMsgLen;
                    end += maxLogMsgLen;
                    if (end > messageLength) {
                        end = messageLength;
                    }
                }
            } while (!done);
        }
    }

    /**
     * Display start AAID retrieval message
     */
    public static void logAaidRetrievalStart() {
        logDebug(TAG, "************************");
        logDebug(TAG, "* START AAID RETRIEVAL *");
        logDebug(TAG, "************************");
    }

    /**
     * Display end AAID retrieval message
     */
    public static void logAaidRetrievalEnd() {
        logDebug(TAG, "**********************");
        logDebug(TAG, "* END AAID RETRIEVAL *");
        logDebug(TAG, "**********************");
    }


    public static void logAaidRetrievalUpdate(int uafClientIdx, List<String> retrievedAaids) {
        logDebug(TAG, "*************************");
        logDebug(TAG, "* UPDATE AAID RETRIEVAL *");
        logDebug(TAG, "*************************");
        logDebug(TAG, "Client index: " + uafClientIdx);
        if(retrievedAaids==null || retrievedAaids.size()==0) {
            logDebug(TAG, "No Retrieved AAIDs");
        } else {
            logDebug(TAG, "Retrieved AAIDs:");
            for (String aaid : retrievedAaids) {
                logDebug(TAG, aaid);
            }
        }
    }

    public static void logAaidRetrievalContinue(int uafClientIdx) {
        logDebug(TAG, "***************************");
        logDebug(TAG, "* CONTINUE AAID RETRIEVAL *");
        logDebug(TAG, "***************************");
        logDebug(TAG, "Client index: " + uafClientIdx);
    }
}