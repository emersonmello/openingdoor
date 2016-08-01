package br.edu.ifsc.mello.openingdoor;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static br.edu.ifsc.mello.openingdoor.DoorProtocol.HELLO;

/**
 * Created by mello on 31/05/16.
 */
public class ApplicationContextDoorLock extends Application {

    private static ApplicationContextDoorLock instance;

    private static Context sContext;
    public static AuthenticationNFCActivity activity = null;
    private static SharedPreferences sSharedPreferences;
    public DoorProtocol protocolStep = HELLO;
    public boolean fidoClientWorking = false;
    public String fidoClientResponse = "";

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public synchronized static ApplicationContextDoorLock getInstance() {
        if (instance == null) {
            instance = new ApplicationContextDoorLock();
            instance.protocolStep = DoorProtocol.HELLO;
            ApplicationContextDoorLock.sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);
        }
        return instance;
    }

    public void cleanup(){
        fidoClientWorking = false;
        fidoClientResponse = "";
        protocolStep = HELLO;
    }

    public static Context getContext() {
        return sContext;
    }

    public static SharedPreferences getsSharedPreferences() {
        return sSharedPreferences;
    }

}
