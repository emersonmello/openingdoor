package br.edu.ifsc.mello.openingdoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private ProgressBar mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ncf_auth);
        mProgressView = (ProgressBar) findViewById(R.id.auth_nfc_progress);

        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        String username = mSharedPreferences.getString("usernameMain", "");
        if (!username.isEmpty()) {
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
        } else {
            showError("You need to register an authenticator first");
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
                finishAndRemoveTask();
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
//            mApplicationContextDoorLock.fidoClientWorking = false;
            finishAndRemoveTask();
        }
    }

    //Sending the response message created by the client to the server
    @Override
    protected void processUafClientResponse(String uafResponseJson) {
        showProgress(true);
        mApplicationContextDoorLock.protocolStep = SUCCESS;
        mApplicationContextDoorLock.fidoClientResponse = uafResponseJson;
//        mApplicationContextDoorLock.fidoClientWorking = false;
        Log.d("process", "fido uaf client sent response");
        this.countDownTimer = new CountDownTimer(6000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.d("uafError", "Activity timeout.");
                mApplicationContextDoorLock.fidoClientResponse = "";
                mApplicationContextDoorLock.protocolStep = ERROR;
                showProgress(false);
                finishAndRemoveTask();
            }
        }.start();
    }

    public void animation(boolean accessGranted) {
        ImageView imageView = (ImageView) findViewById(R.id.img_door);
        TextView textView = (TextView) findViewById(R.id.status_message);
        showProgress(false);
        if (accessGranted) {
            imageView.setImageResource(R.mipmap.open);
            textView.setText("Access granted");
        } else {
            textView.setText("Access denied");
        }
        textView.setVisibility(View.VISIBLE);
        assert countDownTimer != null;
        this.countDownTimer.cancel();
        this.countDownTimer = new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.d("animation", "timeout");
                finishAndRemoveTask();
            }
        }.start();
    }

    @Override
    protected void onActivityResultFailure(String errorMsg) {
        showProgress(true);
        mApplicationContextDoorLock.protocolStep = ERROR;
        mApplicationContextDoorLock.fidoClientResponse = "";
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


}
