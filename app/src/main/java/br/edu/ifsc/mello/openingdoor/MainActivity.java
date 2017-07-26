package br.edu.ifsc.mello.openingdoor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;


public class MainActivity extends AppCompatActivity {

    private final int REG = 1;
    private final int AUTH = 2;
    private final int DEREG = 3;
    private SharedPreferences mSharedPreferences;
    private WhitelistingTask mWhiteListingTask;
    private ApplicationContextDoorLock mApplicationContextDoorLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(ApplicationContextDoorLock.getContext(), R.xml.pref_general, false);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationContextDoorLock.getContext());
        mApplicationContextDoorLock = ApplicationContextDoorLock.getInstance();
        init();
    }

    private void init() {

        String username = mSharedPreferences.getString("usernameMain", "");
        View cardReg = findViewById(R.id.card_register);
        View cardDetails = findViewById(R.id.card_userdetails);
        if (username.isEmpty()) {
            cardReg.setVisibility(View.VISIBLE);
            cardDetails.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.username);
            textView.setText(username);
            TextView keyid = (TextView) findViewById(R.id.keyid);
            keyid.setText(mSharedPreferences.getString("keyid", ""));
            TextView pubkey = (TextView) findViewById(R.id.publickey);
            pubkey.setText(mSharedPreferences.getString("pubkey", ""));
            TextView server = (TextView) findViewById(R.id.server);
            server.setText(mSharedPreferences.getString("server", ""));
            cardReg.setVisibility(View.GONE);
            cardDetails.setVisibility(View.VISIBLE);
        }
    }

    public void deregisterAuthenticator(View view) {
        Intent intent = new Intent(this, DeRegisterActivity.class);
        startActivityForResult(intent, DEREG);
    }

    public void createAccount(View view) {
        String username = mSharedPreferences.getString("username", "");

        if (username.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.emptyusername)
                    .setMessage(R.string.emptyusernamemessage)
                    .setPositiveButton(R.string.button_go_to_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        }
                    }).show();
        }else {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivityForResult(intent, REG);
        }
    }

    public void authentication(View view) {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivityForResult(intent, AUTH);
    }

    public void dereg() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove("usernameMain");
        editor.remove("aaid");
        editor.remove("keyid");
        editor.remove("pubkey");
        editor.remove("server");
        editor.apply();
        init();
        //TODO Invoke FIDO UAF Client to do DeReg
    }

    public void showPubKey(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.pubkey_title)
                .setMessage(mSharedPreferences.getString("pubkey", ""))
                .setNeutralButton(R.string.button_done, null).show();

    }

    private void updateMainLayout(ServerResponseReg serverResponseReg) {
        if (serverResponseReg.getStatus().equals("SUCCESS")) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("usernameMain", serverResponseReg.getUsername());
            editor.putString("keyid", serverResponseReg.getAuthenticator().getKeyID());
            editor.putString("aaid", serverResponseReg.getAuthenticator().getAAID());
            editor.putString("pubkey", serverResponseReg.getPublicKey());
            editor.putString("server", mSharedPreferences.getString("fido_server_endpoint", ""));
            editor.apply();
            View cardReg = findViewById(R.id.card_register);
            cardReg.setVisibility(View.GONE);
            View cardDetails = findViewById(R.id.card_userdetails);

            TextView username = (TextView) findViewById(R.id.username);
            username.setText(serverResponseReg.getUsername());
            TextView keyid = (TextView) findViewById(R.id.keyid);
            keyid.setText(serverResponseReg.getAuthenticator().getKeyID());
            TextView pubkey = (TextView) findViewById(R.id.publickey);
            pubkey.setText(serverResponseReg.getPublicKey());
            TextView server = (TextView) findViewById(R.id.server);
            server.setText(mSharedPreferences.getString("fido_server_endpoint", ""));

            cardDetails.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle extra = data.getExtras();
            Toast toast;
            String result;
            String[] status;
            String response;
            Gson gson = new Gson();
            switch (requestCode) {
                case REG:
                    result = extra.getString("result");
                    if (!result.isEmpty()) {
                        // **************************************************
                        // It is specific for eBay RP Server Response - github.com/eBay/UAF
                        status = result.split("#ServerResponse\n");
                        if (status.length > 1) {
                            response = status[1].trim();
                            response = response.substring(1, response.length() - 1);
                            ServerResponseReg serverResponseReg = gson.fromJson(response, ServerResponseReg.class);
                            this.updateMainLayout(serverResponseReg);
                            //TODO Do something instead of snackbar message!
                            Snackbar.make(findViewById(R.id.layout_main), serverResponseReg.getStatus(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
                        }
                        // **************************************************
                    } else {
                        Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case AUTH:
                    result = extra.getString("result");
                    if (!result.isEmpty()) {
                        // **************************************************
                        // It is specific for eBay RP Server Response - github.com/eBay/UAF
                        status = result.split("#ServerResponse\n");
                        if (status.length > 1) {
                            response = status[1].trim();
                            response = response.substring(1, response.length() - 1);
                            ServerResponse serverResponse = gson.fromJson(response, ServerResponse.class);
                            //TODO Do something instead of toast message!
                            Toast.makeText(getApplicationContext(), serverResponse.getStatus(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
                        }
                        // **************************************************
                    } else {
                        Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DEREG:
                    result = extra.getString("result");
                    if (!result.isEmpty()) {
                        dereg();
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_appsign:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_sign_dialog)
                        .setMessage(getFacetId())
                        .setNeutralButton(R.string.button_done, null).show();

                break;
            case R.id.action_whitelisting_facetid:
                // Developing purpose only, it should not be used in a real scenario.
                this.attemptWhitelistings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getFacetId() {
        StringBuffer ret = new StringBuffer();
        String comma = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature sign : packageInfo.signatures) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
                messageDigest.update(sign.toByteArray());
                String currentSignature = Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT);
                ret.append("android:apk-key-hash:");
                ret.append(currentSignature.substring(0, currentSignature.length() - 2));
                ret.append(comma);
                comma = ",";
            }
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.init();
    }



    /**
     * Whitelisting facetID of this Android RP APP. Certified FIDO Client should
     * only trusts on Android RP App registered at FIDO UAF Server
     *
     * It only works with FIDO UAF Demo Server (https://github.com/emersonmello/UAF or https://github.com/eBay/UAF)
     *
     * Developing purpose only, it should not be used in a real scenario.
     *
     */
    private void attemptWhitelistings() {
        if (mWhiteListingTask != null) {
            return;
        }
        mWhiteListingTask = new MainActivity.WhitelistingTask(this);
        String url = mSharedPreferences.getString("fido_server_endpoint", "") + mSharedPreferences.getString("fido_whitelisting_facetid", "");
        String facetId = null;
        try {
            facetId = URLEncoder.encode(this.getFacetId(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mWhiteListingTask.execute(url + facetId);
    }


    /**
     * Represents an asynchronous task used to whitelisting facetID. Developing purpose only, it should not be used in a real scenario.
     */
    public class WhitelistingTask extends AsyncTask<String, Integer, String> {

        private String result;
        private boolean done;
        private MainActivity mMainActivity;

        public WhitelistingTask(MainActivity mainActivity) {
            mMainActivity = mainActivity;
            result = null;
            done = false;
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
                mWhiteListingTask = null;
                this.result = result;
                this.done = true;

                if (result.contains(mMainActivity.getFacetId())){
                    Toast.makeText(mMainActivity.getApplicationContext(), R.string.whitelisting_ok, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mMainActivity.getApplicationContext(), R.string.whitelisting_error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(mMainActivity.getApplicationContext(), R.string.connection_error, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mWhiteListingTask = null;
        }
    }
}
