package br.edu.ifsc.mello.openingdoor;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by mello on 31/05/16.
 */
public class ApplicationContextDoorLock extends Application {

    private static ApplicationContextDoorLock instance;

    private boolean tryingToAuthenticate = false;
    private boolean handShake = true;
    private String payload = "READY";
    private int blockSize = 0;
    private StringBuilder longMessage;
    private static Context sContext;
    public  static AuthenticationNFCActivity activity = null;
    public  static MainActivity mainActivity = null;
    private static SharedPreferences sSharedPreferences;
    public static String TAG = "OpeningDoors";

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public synchronized static ApplicationContextDoorLock getInstance(){
        if (instance == null){
            instance = new ApplicationContextDoorLock();
            ApplicationContextDoorLock.sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);
        }
        return instance;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int decBlockSize(){
        return --blockSize;
    }

    public void clearLongMessage(){
        this.longMessage = new StringBuilder();
    }

    public String getLongMessage() {
        return longMessage.toString();
    }

    public void appendLongMessage(String longMessage) {
        this.longMessage.append(longMessage);
    }

    public boolean isTryingToAuthenticate() {
        return tryingToAuthenticate;
    }
    public void setTryingToAuthenticate(boolean tryingToAuthenticate) {
        this.tryingToAuthenticate = tryingToAuthenticate;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
    public static Context getContext() {
        return sContext;
    }

    public static SharedPreferences getsSharedPreferences(){
        return sSharedPreferences;
    }

    public boolean isHandShake() {
        return handShake;
    }

    public void setHandShake(boolean handShake) {
        this.handShake = handShake;
    }



}
