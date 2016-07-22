package br.edu.ifsc.mello.openingdoor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.daon.identityx.exception.UafProcessingException;
import com.daon.identityx.uaf.FidoOperation;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends BaseActivity {

    private CreateAccountTask createAccountTask = null;
    private View mProgressView;
    private SharedPreferences mSharedPreferences;
    private ApplicationContextDoorLock mApplicationContextDoorLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mProgressView = findViewById(R.id.account_progress);
        mSharedPreferences = ApplicationContextDoorLock.getsSharedPreferences();
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
        createAccount();
    }

    private void createAccount() {
        if (createAccountTask != null) {
            return;
        }
        showProgress(true);
        String username = mSharedPreferences.getString("username", "").replaceAll("\\s+", "");
        String url = mSharedPreferences.getString("fido_server_endpoint", "");
        String endpoint = mSharedPreferences.getString("fido_reg_request", "");
        createAccountTask = new CreateAccountTask(this);
        createAccountTask.execute(url + endpoint + "/" + username);
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


    @Override
    protected void processUafClientResponse(String uafResponseJson) {
        showProgress(true);
        //Sending the response message created by the client to the server
        ClientSendFIDORegResponseTask clientSendFIDORegResponseTask = new ClientSendFIDORegResponseTask(this);
        clientSendFIDORegResponseTask.execute(uafResponseJson);
    }

    @Override
    protected void onActivityResultFailure(String errorMsg) {
        createAccountTask = null;
        showProgress(false);
        finish();
    }

    public class ClientSendFIDORegResponseTask extends AsyncTask<String, Void, String> {

        private RegisterActivity mRegisterActivity;

        public ClientSendFIDORegResponseTask(RegisterActivity registerActivity) {
            mRegisterActivity = registerActivity;
        }


        @Override
        protected String doInBackground(String... params) {
            StringBuffer res = new StringBuffer();
            String decoded = "";
            try {
                JSONObject json = new JSONObject(params[0]);
                decoded = json.getString("uafProtocolMessage").replace("\\", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            res.append("#uafMessageegOut\n" + decoded);
            res.append("\n\n#ServerResponse\n");
            String url = mSharedPreferences.getString("fido_server_endpoint", "");
            String endpoint = mSharedPreferences.getString("fido_reg_response", "");
            try {
                String serverResponse = HttpUtils.post(url + endpoint, decoded).getPayload();
                res.append(serverResponse);
                return res.toString();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showProgress(false);
            if (result != null) {
                Bundle extras = new Bundle();
                extras.putString("result", result);
                Intent intent = new Intent();
                intent.putExtras(extras);
                setResult(RESULT_OK, intent);
            } else {
                Toast toast = Toast.makeText(mRegisterActivity.getApplicationContext(), "Something is wrong!", Toast.LENGTH_SHORT);
                toast.show();
            }
            finish();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class CreateAccountTask extends AsyncTask<String, Integer, String> {

        private RegisterActivity mRegisterActivity;
        private String result = null;
        private boolean done = false;

        public CreateAccountTask(RegisterActivity registerActivity) {
            mRegisterActivity = registerActivity;
        }

        public boolean isDone() {
            return done;
        }

        public String getResult() {
            return result;
        }

        @Override
        protected String doInBackground(String... args) {
            done = true;
            try {
                result = HttpUtils.get(args[0]).getPayload();
            } catch (Exception e) {
                return "";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != "") {
                createAccountTask = null;
                this.result = result;
                this.done = true;
                String username = mSharedPreferences.getString("username", "").replaceAll("\\s+", "");
                String url = mSharedPreferences.getString("fido_server_endpoint", "");
                String endpoint = mSharedPreferences.getString("fido_reg_request", "");
                String rpEndpoint = url + endpoint + "/" + username;

                setCurrentFidoOperation(FidoOperation.Registration);
                Intent intent = getUafClientUtils().getUafOperationIntent(FidoOperation.Registration,
                        result);

                Bundle extra = intent.getExtras();
                extra.putString("rpServerEndpoint", rpEndpoint);

                intent.putExtras(extra);
                try {
                    sendUafClientIntent(intent, FidoOpCommsType.Return);
                } catch (UafProcessingException e) {
                    Toast.makeText(mRegisterActivity.getApplicationContext(), R.string.no_fido_client_found, Toast.LENGTH_LONG).show();
                }
            } else {
                Context context = mRegisterActivity.getApplicationContext();
                Toast.makeText(context, R.string.connection_error, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            createAccountTask = null;
            showProgress(false);
        }
    }


}

