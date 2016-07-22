package br.edu.ifsc.mello.openingdoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daon.identityx.uaf.FidoOperation;

public class DeRegisterActivity extends BaseActivity {

    private SharedPreferences mSharedPreferences;
    private DeRegAsyncTask mDeRegAsyncTask;
    private ProgressBar mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_de_register);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContextDoorLock.getContext());
        mDeRegAsyncTask = null;
        mProgressView = (ProgressBar) findViewById(R.id.dereg_progress);
        deregister();
    }

    public void deregister() {
        IDeregistration deregistration = new DeregistrationEbayFIDOServerImpl();

        String uafMessage = deregistration.getDeregJsonMessage(mSharedPreferences);

        setCurrentFidoOperation(FidoOperation.Deregistration);
        Intent intent = getUafClientUtils().getUafOperationIntent(FidoOperation.Deregistration, uafMessage);
        sendUafClientIntent(intent, BaseActivity.FidoOpCommsType.Return);
    }


    @Override
    protected void processUafClientResponse(String uafResponseJson) {
        showProgress(true);
        if (mDeRegAsyncTask == null) {
            mDeRegAsyncTask = new DeRegAsyncTask();
            mDeRegAsyncTask.execute(uafResponseJson);
        }
    }

    @Override
    protected void onActivityResultFailure(String errorMsg) {
        mDeRegAsyncTask = null;
        showProgress(false);
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
        finish();
    }


    public void finishDeReg(String result) {
        showProgress(false);
        Bundle extras = new Bundle();
        extras.putString("result", result);
        Intent intent = new Intent();
        intent.putExtras(extras);
        setResult(RESULT_OK, intent);
        finish();
    }


    private class DeRegAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDeRegAsyncTask = null;
            finishDeReg(s);
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String url = mSharedPreferences.getString("fido_server_endpoint", "");
            String endpoint = mSharedPreferences.getString("fido_dereg_request", "");
            try {
                result = HttpUtils.post(url + endpoint, params[0]).getPayload();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
            return result;
        }
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
