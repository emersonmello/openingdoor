package br.edu.ifsc.mello.openingdoor;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends AppCompatActivity {

    private final int REG = 1;
    private final int AUTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
    }

    public void createAccount(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REG);
    }

    public void authentication(View view) {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivityForResult(intent, AUTH);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extra = data.getExtras();
        Toast toast;
        switch (requestCode) {
            case REG:
                //TODO Do something instead of toast message!
                toast = Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case AUTH:
                String result = extra.getString("result");
                String[] status = result.split("#ServerResponse\n");
                String response = status[1].trim();
                response = response.substring(1,response.length()-1);
                Gson gson = new Gson();
                ServerResponse serverResponse = gson.fromJson(response,ServerResponse.class);
                //TODO Do something instead of toast message!
                toast = Toast.makeText(getApplicationContext(), serverResponse.getStatus(), Toast.LENGTH_SHORT);
                toast.show();
                break;
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
