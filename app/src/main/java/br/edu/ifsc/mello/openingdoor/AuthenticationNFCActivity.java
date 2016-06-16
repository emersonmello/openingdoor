package br.edu.ifsc.mello.openingdoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.daon.identityx.exception.UafProcessingException;
import com.daon.identityx.uaf.FidoOperation;

import org.json.JSONException;
import org.json.JSONObject;


public class AuthenticationNFCActivity extends BaseActivity {

    private SharedPreferences mSharedPreferences;
    private ApplicationContextDoorLock mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
    private boolean nfc;
    private String uafAuthRequest;
    private final String FIDO_KEY = "fido_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ncf_auth);
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            nfc = extras.getBoolean("NFC", false);
            uafAuthRequest = extras.getString("uafMsg");
        }
        this.attemptAuthResponse();
        ApplicationContextDoorLock.activity = this;
    }

    private void attemptAuthResponse() {
        setCurrentFidoOperation(FidoOperation.Authentication);
        Intent intent = getUafClientUtils()
                .getUafOperationIntent(FidoOperation.Authentication, uafAuthRequest);
        Bundle extra = intent.getExtras();
        intent.putExtras(extra);
        try {
            sendUafClientIntent(intent, FidoOpCommsType.Return);
        } catch (UafProcessingException e) {
            Toast.makeText(this.getApplicationContext(), R.string.no_fido_client_found, Toast.LENGTH_LONG).show();
            mApplicationContextDoorLock.setPayload("ERROR");
            mApplicationContextDoorLock.setTryingToAuthenticate(false);
            addSharedPrefs(FIDO_KEY, "no fido client found");
            finish();
        }
    }

    //Sending the response message created by the client to the server
    @Override
    protected void processUafClientResponse(String uafResponseJson) {
        mApplicationContextDoorLock.setPayload("SUCCESS");
        mApplicationContextDoorLock.setTryingToAuthenticate(false);
        mApplicationContextDoorLock.clearLongMessage();
        mApplicationContextDoorLock.appendLongMessage(uafResponseJson);
        Log.d("process", uafResponseJson);
    }

    public void animation() {
        ImageView imageView = (ImageView) findViewById(R.id.img_door);
        imageView.setImageResource(R.mipmap.open);
        new CountDownTimer(10000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                finish();
            }
        }.start();
    }

    private void addSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    protected void onActivityResultFailure(String errorMsg) {
        mApplicationContextDoorLock.setPayload("ERROR");
        mApplicationContextDoorLock.setTryingToAuthenticate(false);
        addSharedPrefs(FIDO_KEY, errorMsg);
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
        finish();
    }


}
