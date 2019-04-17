package cesi.acrcloud.pkandre.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class ConfigsActivity extends Activity {
    private SharedPreferences Prefs;
    private SharedPreferences.Editor edit;

    private EditText host, key,secret;
    private Button btnSave;
    private Intent intencion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_configs);

        host = (EditText)findViewById(R.id.conf_hot);
        key = (EditText)findViewById(R.id.conf_key);
        secret = (EditText)findViewById(R.id.conf_secret);
        btnSave = (Button)findViewById(R.id.conf_save);

        Prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Prefs = getSharedPreferences("cesi.acrcloud.pkandre.project", Context.MODE_PRIVATE);
        edit = Prefs.edit();

        setFormValues();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _host = host.getText().toString();
                String _key = key.getText().toString();
                String _secret = secret.getText().toString();

                edit.putString("_host",_host);
                edit.putString("_key",_key);
                edit.putString("_secret",_secret);

                edit.commit();

                intencion = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intencion);

            }
        });
    }

    public void setFormValues(){
        host.setText(Prefs.getString("_host","identify-eu-west-1.acrcloud.com"));
        key.setText(Prefs.getString("_key","216b861e0af3b10325c13185e94aff76"));
        secret.setText(Prefs.getString("_secret","GXn47xGGKRpmM5LWbDUJV7JV0qdYO77LzEHG06K6"));
    }

}
