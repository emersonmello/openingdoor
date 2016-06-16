package br.edu.ifsc.mello.openingdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

public class MyHostApduService extends HostApduService {
    public boolean isDone;
    public SharedPreferences mSharedPreferences;
    private ApplicationContextDoorLock mApplicationContextDoorLock;
    private ProtocolAsyncTask mProtocolAsyncTask = null;
    private String TAG;
    private final String FIDO_KEY = "fido_result";


    public MyHostApduService() {
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        this.TAG = ApplicationContextDoorLock.TAG;
        Log.d(TAG, "HOSTAPDU");
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        this.isDone = false;
        if (selectAidApdu(commandApdu)) {
            Log.d(TAG, "SELECTAIDAPDU");
            mApplicationContextDoorLock.setHandShake(false);
            mProtocolAsyncTask = null;
            mApplicationContextDoorLock.setBlockSize(0);
            mApplicationContextDoorLock.clearLongMessage();
            return DoorProtocol.HELLO.getDesc().getBytes();
        } else {
            String message = new String(commandApdu);
//            Log.d(TAG, "PROTOCOL: " + message);
            if (message.contains("BLOCK")) {
                String size = message.split(":")[1];
                int i = Integer.parseInt(size);
                mApplicationContextDoorLock.setBlockSize(i);
                mApplicationContextDoorLock.clearLongMessage();
                return "NEXT".getBytes();
            } else {
                if (mApplicationContextDoorLock.getBlockSize() > 0) {
                    mApplicationContextDoorLock.appendLongMessage(message);
                    mApplicationContextDoorLock.decBlockSize();
                    return "CONT".getBytes();
                } else {
                    if ((message.equals(DoorProtocol.READY.getDesc())
                            || message.equals("RESPONSE")
                            || message.equals(DoorProtocol.GRANTED.getDesc()))
                            && !mApplicationContextDoorLock.isHandShake()) {

                        if (message.equals(DoorProtocol.GRANTED.getDesc())) {
                            mApplicationContextDoorLock.setPayload("GRANTED");
                        }

                        if (!mApplicationContextDoorLock.isTryingToAuthenticate()) {
                            switch (mApplicationContextDoorLock.getPayload()) {
                                case "READY":
                                    Log.d(TAG, "READY");
                                    mApplicationContextDoorLock.setTryingToAuthenticate(true);
                                    mProtocolAsyncTask = new ProtocolAsyncTask(this);
                                    mProtocolAsyncTask.execute(mApplicationContextDoorLock.getLongMessage());
                                    mApplicationContextDoorLock.clearLongMessage();
                                    break;
                                case "SUCCESS":
                                    Log.d(TAG, "SUCCESS");
                                    mApplicationContextDoorLock.setPayload("RESPONSE");
                                    mApplicationContextDoorLock.setTryingToAuthenticate(false);
                                    MyHostApduService.this.sendResponseApdu(DoorProtocol.DONE.getDesc().getBytes());
                                    break;
                                case "ERROR":
                                    String value = mSharedPreferences.getString(FIDO_KEY, " nothing");
                                    Log.d(TAG, "ERROR: " + value);
                                    mApplicationContextDoorLock.setPayload("nil");
                                    mApplicationContextDoorLock.setHandShake(true);
                                    MyHostApduService.this.sendResponseApdu(DoorProtocol.ERROR.getDesc().getBytes());
                                    onDeactivated(-1);
                                    break;
                                case "RESPONSE":
                                    String response = mApplicationContextDoorLock.getLongMessage();
                                    Log.d(TAG, "RESPONSE: " + response);
                                    mApplicationContextDoorLock.setPayload("nil");
                                    MyHostApduService.this.sendResponseApdu(response.getBytes());
                                    break;
                                case "GRANTED":
                                    Log.d(TAG, "Access granted");
                                    mApplicationContextDoorLock.setHandShake(true);
//                                    if (ApplicationContextDoorLock.activity != null){
                                    ApplicationContextDoorLock.activity.animation();
                                    //                                  }
                                    MyHostApduService.this.sendResponseApdu("BYE".getBytes());
                                    break;
                            }
                        } else {
                            Log.d(TAG, "WAIT");
                            MyHostApduService.this.sendResponseApdu(DoorProtocol.WAIT.getDesc().getBytes());
                        }
                    }
                }
            }
            return null;
        }
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
        mApplicationContextDoorLock.setTryingToAuthenticate(false);
        mApplicationContextDoorLock.setPayload("READY");
        mApplicationContextDoorLock.setHandShake(true);
        mApplicationContextDoorLock.clearLongMessage();
        mApplicationContextDoorLock.setBlockSize(0);
        this.isDone = true;
        this.mProtocolAsyncTask = null;
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
            data.putString("uafMsg", message);
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
