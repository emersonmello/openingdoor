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

import static br.edu.ifsc.mello.openingdoor.DoorProtocol.*;

public class AuthenticationNFCActivity extends BaseActivity {

    private SharedPreferences mSharedPreferences;
    private ApplicationContextDoorLock mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
    private String uafAuthRequest;
    public CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ncf_auth);
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String message = extras.getString("message");
            if (!message.isEmpty()) {
                if (!message.equals(READER_ERROR.getDesc())) {
                    uafAuthRequest = message;
                    this.attemptAuthResponse();
                    ApplicationContextDoorLock.activity = this;
                } else {
                    showError(message);
                }
            }
        }

    }

    private void showError(String message) {
        TextView textView = (TextView) findViewById(R.id.status_message);
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
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
            mApplicationContextDoorLock.protocolStep = ERROR;
            mApplicationContextDoorLock.fidoClientResponse = "";
            mApplicationContextDoorLock.fidoClientWorking = false;
            finish();
        }
    }

    //Sending the response message created by the client to the server
    @Override
    protected void processUafClientResponse(String uafResponseJson) {
        mApplicationContextDoorLock.protocolStep = SUCCESS;
        mApplicationContextDoorLock.fidoClientResponse = uafResponseJson;
        mApplicationContextDoorLock.fidoClientWorking = false;
        Log.d("process", "fido uaf client sent response");
        this.countDownTimer = new CountDownTimer(6000, 100) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Log.d("uafError", "Activity timeout.");
                mApplicationContextDoorLock.fidoClientResponse = "";
                mApplicationContextDoorLock.protocolStep = ERROR;
                finish();
            }
        }.start();
    }

    public void animation(boolean accessGranted) {
        ImageView imageView = (ImageView) findViewById(R.id.img_door);
        TextView textView = (TextView) findViewById(R.id.status_message);
        if (accessGranted) {
            imageView.setImageResource(R.mipmap.open);
            textView.setText("Access granted");
        } else {
            textView.setText("Access denied");
        }
        textView.setVisibility(View.VISIBLE);
        assert countDownTimer != null;
        this.countDownTimer.cancel();
        this.countDownTimer = new CountDownTimer(10000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.d("animation", "timeout");
                finish();
            }
        }.start();
    }

    @Override
    protected void onActivityResultFailure(String errorMsg) {
        mApplicationContextDoorLock.protocolStep = ERROR;
        mApplicationContextDoorLock.fidoClientResponse = "";
        mApplicationContextDoorLock.fidoClientWorking = false;
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
        finish();
    }


}
