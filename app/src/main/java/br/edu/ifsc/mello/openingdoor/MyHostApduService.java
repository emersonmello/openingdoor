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


    public MyHostApduService() {
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
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
            return DoorProtocol.HELLO.getDesc().getBytes();
        } else {
            Log.d(TAG, "PROTOCOL");
            String message = new String(commandApdu);
            if ((message.equals(DoorProtocol.READY.getDesc())) && (!mApplicationContextDoorLock.isHandShake())) {
                if (!mApplicationContextDoorLock.isTryingToAuthenticate()) {
                    switch (mApplicationContextDoorLock.getPayload()){
                        case "READY":
                            Log.d(TAG, "READY");
                            mApplicationContextDoorLock.setTryingToAuthenticate(true);
                            mProtocolAsyncTask = new ProtocolAsyncTask(this);
                            mProtocolAsyncTask.execute(new String(commandApdu));
                            break;
                        case "SUCCESS":
                            Log.d(TAG, "SUCCESS");
                            mApplicationContextDoorLock.setPayload("READY");
                            mApplicationContextDoorLock.setHandShake(true);
                            MyHostApduService.this.sendResponseApdu(DoorProtocol.DONE.getDesc().getBytes());
                            break;
                        case "ERROR":
                            Log.d(TAG, "ERROR");
                            mApplicationContextDoorLock.setPayload("nil");
                            mApplicationContextDoorLock.setHandShake(true);
                            MyHostApduService.this.sendResponseApdu(DoorProtocol.ERROR.getDesc().getBytes());
                            break;
                    }
                } else {
                    Log.d(TAG, "WAIT");
                    MyHostApduService.this.sendResponseApdu(DoorProtocol.WAIT.getDesc().getBytes());
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
            if (message.equals(DoorProtocol.READY.getDesc())) {
                Intent intent = new Intent(this.myHostApduService, AuthenticationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
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
