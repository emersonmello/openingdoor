package br.edu.ifsc.mello.openingdoor;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static br.edu.ifsc.mello.openingdoor.DoorProtocol.*;

public class CardHostApduService extends HostApduService {

    private final int MAX_FRAME_SIZE = 250;
    private int blockSize;
    public StringBuilder messageBuilder;
    public ProtocolAsyncTask mProtocolAsyncTask;
    private ApplicationContextDoorLock mApplicationContextDoorLock;
    private String lastSentMessage;
    private String lastReceivedMessage;
    private ArrayList<String> arrayList;
    private int blockSent;

    @Override
    public void onCreate() {
        super.onCreate();
        blockSize = 0;
        messageBuilder = new StringBuilder();
        mProtocolAsyncTask = null;
        lastReceivedMessage = "";
        lastSentMessage = "";
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String sentMessage = "";
        if (selectAidApdu(commandApdu)) {
            Log.d("process", "SELECT AID APDU");
            sentMessage = DoorProtocol.HELLO.getDesc();
            return sentMessage.getBytes();
        } else {
            String receivedMessage = new String(commandApdu);

            // ******************************************
            // DEBUG purpose only
            if (!lastReceivedMessage.equals(receivedMessage)) {
                Log.d("REC", "Message received: " + receivedMessage);
                lastReceivedMessage = receivedMessage;
            }

            // ******************************************


            if (receivedMessage.contains("BLOCK")) {
                try {
                    String size = receivedMessage.split(":")[1];
                    blockSize = Integer.parseInt(size);
                    sentMessage = DoorProtocol.NEXT.getDesc();
                    Log.d("SENT", "Message sent: " + sentMessage);
                    return sentMessage.getBytes();
                } catch (Exception e) {
                    sentMessage = ERROR.getDesc();
                    Log.d("SENT", "Message sent: " + sentMessage);
                    return sentMessage.getBytes();
                }
            }

            if (blockSize > 0) {
                messageBuilder.append(receivedMessage);
                blockSize--;
                sentMessage = OK.getDesc();
                Log.d("SENT", "Message sent: " + sentMessage);
                return sentMessage.getBytes();
            }

            if (receivedMessage.equals(NEXT.getDesc())) {
                Log.d("Block","----> "+ blockSent);
                if (blockSent < arrayList.size()) {
                    sentMessage = arrayList.get(blockSent);
                    blockSent++;
                    return sentMessage.getBytes();
                }
            }
            if (receivedMessage.equals(ERROR.getDesc())) {
                Log.d("error", "error on card reader");
                try {
                    ApplicationContextDoorLock.activity.animation(false);
                }catch(Exception e){

                }
            }

            if (receivedMessage.equals(READY.getDesc())) {
                if (!mApplicationContextDoorLock.fidoClientWorking) {
                    mApplicationContextDoorLock.fidoClientWorking = true;
                    mProtocolAsyncTask = new ProtocolAsyncTask(this);
                    mProtocolAsyncTask.execute(messageBuilder.toString());
                } else {
                    if (mApplicationContextDoorLock.protocolStep == SUCCESS) {
                        mApplicationContextDoorLock.protocolStep = RESPONSE;
                        mApplicationContextDoorLock.fidoClientWorking = false;
                        sentMessage = DONE.getDesc();
                        Log.d("SENT", "Message sent: " + sentMessage);
                        return sentMessage.getBytes();
                    }
                    if (mApplicationContextDoorLock.protocolStep == ERROR) {
                        sentMessage = ERROR.getDesc();
                        mApplicationContextDoorLock.cleanup();
                        Log.d("SENT", "Message sent: " + sentMessage);
                        return sentMessage.getBytes();
                    }
                }
                return WAIT.getDesc().getBytes();
            }
            if (receivedMessage.equals(RESPONSE.getDesc())) {
                if (mApplicationContextDoorLock.protocolStep == RESPONSE) {
                    mApplicationContextDoorLock.protocolStep = RESULT;
                    sentMessage = mApplicationContextDoorLock.fidoClientResponse;
                    this.arrayList = (ArrayList<String>) splitEqually(sentMessage, MAX_FRAME_SIZE);
                    this.blockSent = 0;
                    sentMessage = "BLOCK:" + arrayList.size();
                    Log.d("SENT", "Message sent: " + sentMessage);
                    return sentMessage.getBytes();
                }
            }
            if ((mApplicationContextDoorLock.protocolStep == RESULT)) {
                if (receivedMessage.equals(GRANTED.getDesc())) {
                    ApplicationContextDoorLock.activity.animation(true);
                    sentMessage = BYE.getDesc();
                    Log.d("SENT", "Message sent: " + sentMessage);
                    return sentMessage.getBytes();
                }
                if (receivedMessage.equals(DENY.getDesc())) {
                    ApplicationContextDoorLock.activity.animation(false);
                    sentMessage = BYE.getDesc();
                    Log.d("SENT", "Message sent: " + sentMessage);
                    return sentMessage.getBytes();
                }
            }
            sentMessage = "";
            return sentMessage.getBytes();
        }//else - main block
    }

    public static List<String> splitEqually(String text, int size) {
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);
        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
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
        mApplicationContextDoorLock.cleanup();
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d("deactivated", "Bye bye reader");
    }

    private boolean selectAidApdu(byte[] apdu) {
        return apdu.length >= 2 && apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4;
    }
}
