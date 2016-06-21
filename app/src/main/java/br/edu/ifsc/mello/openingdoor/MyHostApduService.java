package br.edu.ifsc.mello.openingdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class MyHostApduService extends HostApduService {
    public boolean isDone;
    public SharedPreferences mSharedPreferences;
    private ApplicationContextDoorLock mApplicationContextDoorLock;
    private ProtocolAsyncTask mProtocolAsyncTask = null;
    private String TAG;
    private String lastSentMessage;
    private String lastReceivedMessage;
    private final String FIDO_KEY = "fido_result";

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        lastSentMessage = "";
        lastReceivedMessage = "";
        this.TAG = ApplicationContextDoorLock.TAG;
        initProtocolVariables();
        Log.d(TAG, "HOST APDU SERVICE created");
    }

    public void initProtocolVariables() {
        mApplicationContextDoorLock.setHandShake(false);
        mApplicationContextDoorLock.setTryingToAuthenticate(false);
        mApplicationContextDoorLock.setBlockSize(0);
        mApplicationContextDoorLock.clearMessageBuffer();
        mProtocolAsyncTask = null;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        this.isDone = false;
        if (selectAidApdu(commandApdu)) {
            Log.d(TAG, "SELECT AID APDU");
            return DoorProtocol.HELLO.getDesc().getBytes();
        } else {
            String message = new String(commandApdu);
            if (!lastReceivedMessage.equals(message)){
                Log.d(TAG, "received message: " + message);
                lastReceivedMessage = message;
            }

            if (message.equals(DoorProtocol.READER_ERROR.getDesc())) {
                resetting(message);
                return DoorProtocol.BYE.getDesc().getBytes();
            }

            if (message.contains("BLOCK")) {
                try {
                    String size = message.split(":")[1];
                    int i = Integer.parseInt(size);
                    mApplicationContextDoorLock.setBlockSize(i);
                    mApplicationContextDoorLock.clearMessageBuffer();
                    return DoorProtocol.NEXT.getDesc().getBytes();
                } catch (Exception e) {
                    e.printStackTrace();
                    resetting(DoorProtocol.ERROR.getDesc());
                    return DoorProtocol.ERROR.getDesc().getBytes();
                }
            } else {
                if (mApplicationContextDoorLock.getBlockSize() > 0) {
                    mApplicationContextDoorLock.appendMessageBuffer(message);
                    mApplicationContextDoorLock.decBlockSize();
                    return "OK".getBytes(); // this message should be discarded by card reader
                } else {
                    if ((message.equals(DoorProtocol.READY.getDesc())
                            || message.equals("RESPONSE")
                            || message.equals(DoorProtocol.GRANTED.getDesc())
                            || message.equals(DoorProtocol.DENY.getDesc()))
                            && !mApplicationContextDoorLock.isHandShake()) {

                        if (message.equals(DoorProtocol.GRANTED.getDesc())) {
                            mApplicationContextDoorLock.setPayload("GRANTED");
                        }
                        if (message.equals(DoorProtocol.DENY.getDesc())) {
                            mApplicationContextDoorLock.setPayload("DENY");
                        }

                        if (!mApplicationContextDoorLock.isTryingToAuthenticate()) {
                            switch (mApplicationContextDoorLock.getPayload()) {
                                case "READY":
                                    Log.d(TAG, "READY");
                                    mApplicationContextDoorLock.setTryingToAuthenticate(true);
                                    mProtocolAsyncTask = new ProtocolAsyncTask(this);
                                    mProtocolAsyncTask.execute(mApplicationContextDoorLock.getMessageBuffer());
                                    mApplicationContextDoorLock.clearMessageBuffer();
                                    break;
                                case "SUCCESS":
                                    Log.d(TAG, "SUCCESS");
                                    mApplicationContextDoorLock.setPayload("RESPONSE");
                                    MyHostApduService.this.sendResponseApdu(DoorProtocol.DONE.getDesc().getBytes());
                                    break;
                                case "ERROR":
                                    String value = mSharedPreferences.getString(FIDO_KEY, " nothing");
                                    Log.d(TAG, "ERROR: " + value);
                                    mApplicationContextDoorLock.setPayload("nil");
                                    mApplicationContextDoorLock.setHandShake(true);
                                    MyHostApduService.this.sendResponseApdu(DoorProtocol.ERROR.getDesc().getBytes());
                                    resetting("Card reader app error");
                                    break;
                                case "RESPONSE":
                                    String response = mApplicationContextDoorLock.getMessageBuffer();
                                    Log.d(TAG, "RESPONSE: " + response);
                                    mApplicationContextDoorLock.setPayload("nil");
                                    MyHostApduService.this.sendResponseApdu(response.getBytes());
                                    break;
                                case "GRANTED":
                                    Log.d(TAG, "Access granted");
                                    mApplicationContextDoorLock.setHandShake(true);
                                    ApplicationContextDoorLock.activity.animation(true);
                                    // card should ignore this message
                                    MyHostApduService.this.sendResponseApdu("BYE".getBytes());
                                    break;
                                case "DENY":
                                    Log.d(TAG, "Access denied");
                                    mApplicationContextDoorLock.setHandShake(true);
                                    ApplicationContextDoorLock.activity.animation(false);
                                    // card should ignore this message
                                    MyHostApduService.this.sendResponseApdu("BYE".getBytes());
                                    break;
                            }
                            lastSentMessage = "";
                        } else {

                            if (!lastSentMessage.equals(DoorProtocol.WAIT.getDesc())){
                                Log.d(TAG, "WAIT");
                                lastSentMessage = DoorProtocol.WAIT.getDesc();
                            }
                            MyHostApduService.this.sendResponseApdu(DoorProtocol.WAIT.getDesc().getBytes());
                        }
                    }
                }
            }
            return null;
        }
    }

    public void resetting(String message) {
//        mApplicationContextDoorLock.resetting();
        mApplicationContextDoorLock.setTryingToAuthenticate(false);
        mApplicationContextDoorLock.setPayload("READY");
        mApplicationContextDoorLock.setHandShake(true);
        mApplicationContextDoorLock.clearMessageBuffer();
        mApplicationContextDoorLock.setBlockSize(0);


        this.isDone = true;
        this.mProtocolAsyncTask = null;
        if (message != null) {
            Bundle data = new Bundle();
            data.putString("message", message);
            Intent intent = new Intent(this, AuthenticationNFCActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtras(data);
            startActivity(intent);
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
        if (!mApplicationContextDoorLock.getPayload().equals("SUCCESS")){
            if (ApplicationContextDoorLock.activity != null) {
                ApplicationContextDoorLock.activity.problem = true;
            }
        }
        resetting(null);
    }


    private boolean selectAidApdu(byte[] apdu) {
        return apdu.length >= 2 && apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4;
    }


    public class ProtocolAsyncTask extends AsyncTask<String, String, String> {
        private MyHostApduService myHostApduService;

        public ProtocolAsyncTask(MyHostApduService m) {
            this.myHostApduService = m;
        }

        @Override
        protected String doInBackground(String... params) {
            String message = params[0];
            Bundle data = new Bundle();
            data.putBoolean("NFC", true);
            data.putString("message", message);
            Intent intent = new Intent(this.myHostApduService, AuthenticationNFCActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtras(data);
            startActivity(intent);
            return DoorProtocol.WAIT.getDesc();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!myHostApduService.isDone) {
                MyHostApduService.this.sendResponseApdu(s.getBytes());
            }
            mProtocolAsyncTask = null;
        }
    }
}
