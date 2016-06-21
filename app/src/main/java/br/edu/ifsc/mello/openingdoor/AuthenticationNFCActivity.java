package br.edu.ifsc.mello.openingdoor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daon.identityx.exception.UafProcessingException;
import com.daon.identityx.uaf.FidoOperation;


public class AuthenticationNFCActivity extends BaseActivity {

    private SharedPreferences mSharedPreferences;
    private ApplicationContextDoorLock mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
    private boolean nfc;
    public boolean problem;
    private String uafAuthRequest;
    private final String FIDO_KEY = "fido_result";
    public CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        problem = false;
        setContentView(R.layout.activity_ncf_auth);
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            nfc = extras.getBoolean("NFC", false);
            String message = extras.getString("message");
            if (message.isEmpty()) {
                if (!message.equals(DoorProtocol.READER_ERROR.getDesc())) {
                    uafAuthRequest = message;
                    this.attemptAuthResponse();
                    ApplicationContextDoorLock.activity = this;
                } else {
                    showError(message);
                }
            }
        }

    }

    private void showError(String message){
        TextView textView = (TextView) findViewById(R.id.status_message);
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
//                mApplicationContextDoorLock.resetting();
                finish();
            }
        }.start();
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
//            mApplicationContextDoorLock.resetting();
            finish();
        }
    }

    //Sending the response message created by the client to the server
    @Override
    protected void processUafClientResponse(String uafResponseJson) {
        mApplicationContextDoorLock.setPayload("SUCCESS");
        mApplicationContextDoorLock.setTryingToAuthenticate(false);
        mApplicationContextDoorLock.clearMessageBuffer();
        mApplicationContextDoorLock.appendMessageBuffer(uafResponseJson);
        Log.d("process", "fido uaf client sent response");
        if (problem){
            problem = false;
            finish();
        }
        new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (mApplicationContextDoorLock.getPayload().equals("RESPONSE")){
                    Log.d("process", "timeout");
//                    mApplicationContextDoorLock.resetting();
                    finish();
                }
            }
        }.start();
    }

    public void animation(boolean accessGranted) {
        ImageView imageView = (ImageView) findViewById(R.id.img_door);
        TextView textView = (TextView) findViewById(R.id.status_message);
        if (accessGranted) {
            imageView.setImageResource(R.mipmap.open);
            textView.setText("Access granted");
        }else{
            textView.setText("Access denied");
        }
        textView.setVisibility(View.VISIBLE);
        this.countDownTimer = new CountDownTimer(10000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.d("animation", "timeout");
//                mApplicationContextDoorLock.resetting();
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
