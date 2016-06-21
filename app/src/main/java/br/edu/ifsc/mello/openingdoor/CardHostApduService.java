package br.edu.ifsc.mello.openingdoor;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import static br.edu.ifsc.mello.openingdoor.DoorProtocol.*;

public class CardHostApduService extends HostApduService {

    private int blockSize;
    public StringBuilder messageBuilder;
    public ProtocolAsyncTask mProtocolAsyncTask;
    private ApplicationContextDoorLock mApplicationContextDoorLock;

    @Override
    public void onCreate() {
        super.onCreate();
        blockSize = 0;
        messageBuilder = new StringBuilder();
        mProtocolAsyncTask = null;
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

        if (selectAidApdu(commandApdu)) {
            Log.d("process", "SELECT AID APDU");
            return DoorProtocol.HELLO.getDesc().getBytes();
        } else {
            String receivedMessage = new String(commandApdu);

            if (receivedMessage.contains("BLOCK")) {
                try {
                    String size = receivedMessage.split(":")[1];
                    blockSize = Integer.parseInt(size);
                    return DoorProtocol.NEXT.getDesc().getBytes();
                } catch (Exception e) {
                    return ERROR.getDesc().getBytes();
                }
            }

            if (blockSize > 0) {
                messageBuilder.append(receivedMessage);
                blockSize--;
                return OK.getDesc().getBytes();
            }

            if (receivedMessage.equals(READY.getDesc())) {
                if (!mApplicationContextDoorLock.fidoClientWorking) {
                    mApplicationContextDoorLock.fidoClientWorking = true;
                    mProtocolAsyncTask = new ProtocolAsyncTask(this);
                    mProtocolAsyncTask.execute(messageBuilder.toString());
                } else {
                    if (mApplicationContextDoorLock.protocolStep == SUCCESS) {
                        mApplicationContextDoorLock.protocolStep = RESPONSE;
                        return DONE.getDesc().getBytes();
                    }
                    if (mApplicationContextDoorLock.protocolStep == ERROR) {
                        return ERROR.getDesc().getBytes();
                    }
                }
                return WAIT.getDesc().getBytes();
            }
            if (receivedMessage.equals(RESPONSE.getDesc())) {
                if (mApplicationContextDoorLock.protocolStep == RESPONSE) {
                    mApplicationContextDoorLock.protocolStep = RESULT;
                    return mApplicationContextDoorLock.fidoClientResponse.getBytes();
                }
            }
            if ((mApplicationContextDoorLock.protocolStep == RESULT)) {
                if (receivedMessage.equals(GRANTED.getDesc())) {
                    ApplicationContextDoorLock.activity.animation(true);
                    return BYE.getDesc().getBytes();
                }
                if (receivedMessage.equals(DENY.getDesc())) {
                    ApplicationContextDoorLock.activity.animation(false);
                    return BYE.getDesc().getBytes();
                }
            }
            return new byte[0];
        }//else - main block
    }


    public class ProtocolAsyncTask extends AsyncTask<String, String, Void> {
        private CardHostApduService cardHostApduService;

        public ProtocolAsyncTask(CardHostApduService c) {
            this.cardHostApduService = c;
        }

        @Override
        protected Void doInBackground(String... params) {
            String message = params[0];
            Bundle data = new Bundle();
            data.putString("message", message);
            Intent intent = new Intent(this.cardHostApduService, AuthenticationNFCActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtras(data);
            startActivity(intent);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            mProtocolAsyncTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        blockSize = 0;
        messageBuilder = new StringBuilder();
        mProtocolAsyncTask = null;
        mApplicationContextDoorLock.fidoClientWorking = false;
        mApplicationContextDoorLock.fidoClientResponse = "";
        mApplicationContextDoorLock.protocolStep = HELLO;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d("deactivated", "Bye bye reader");
    }

    private boolean selectAidApdu(byte[] apdu) {
        return apdu.length >= 2 && apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4;
    }
}
