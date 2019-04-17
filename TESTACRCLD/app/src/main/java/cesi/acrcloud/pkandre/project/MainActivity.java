package cesi.acrcloud.pkandre.project;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.IACRCloudListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;




public class MainActivity extends Activity implements IACRCloudListener {

	private ACRCloudClient mClient;
	private ACRCloudConfig mConfig;
	
	private TextView mResult,tv_time;

	private Button startBtn;
	private ImageButton configBtn;
	private ImageView iconRunning;
	
	private boolean mProcessing = false;
	private boolean initState = false;

	private String path = "";

	private long startTime = 0;
	private long stopTime = 0;

    private Intent intencion;

    private SharedPreferences Prefs;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		path = Environment.getExternalStorageDirectory().toString()
				+ "/acrcloud/model";
		
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}		
			
		mResult = (TextView) findViewById(R.id.result);
		tv_time = (TextView) findViewById(R.id.time);

		startBtn = (Button) findViewById(R.id.action_speak);
		configBtn = (ImageButton) findViewById(R.id.action_config);

        iconRunning = (ImageView) findViewById(R.id.running);

        mResult.setVisibility(View.INVISIBLE);
        tv_time.setVisibility(View.INVISIBLE);
        iconRunning.setVisibility(View.INVISIBLE);

		startBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				start();
            }
		});

        configBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                intencion = new Intent(getApplicationContext(),ConfigsActivity.class);
                startActivity(intencion);
            }
        });


        this.mConfig = new ACRCloudConfig();
        this.mConfig.acrcloudListener = this;

        Prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Prefs = getSharedPreferences("cesi.acrcloud.pkandre.project", Context.MODE_PRIVATE);

        String host = Prefs.getString("_host","identify-eu-west-1.acrcloud.com");
        String accessKey = Prefs.getString("_key","216b861e0af3b10325c13185e94aff76");
        String accessSecret = Prefs.getString("_secret","GXn47xGGKRpmM5LWbDUJV7JV0qdYO77LzEHG06K6");

        this.mConfig.context = this;
        this.mConfig.host = host;//"identify-eu-west-1.acrcloud.com";
        this.mConfig.dbPath = path;
        this.mConfig.accessKey = accessKey;
        this.mConfig.accessSecret = accessSecret;
        this.mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP; // PROTOCOL_HTTP
        this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
        //this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;

        this.mClient = new ACRCloudClient();
        this.initState = this.mClient.initWithConfig(this.mConfig);
        if (this.initState) {
//            this.mClient.startPreRecord(3000); //start prerecord, you can call "this.mClient.stopPreRecord()" to stop prerecord.
        }
	}

	public void start() {
        if (!this.initState) {
            Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
            return;
        }
		
		if (!mProcessing) {
			mProcessing = true;
			mResult.setText("");

			if (this.mClient == null || !this.mClient.startRecognize()) {
				mProcessing = false;
                Toast.makeText(getApplicationContext(), "start error!", Toast.LENGTH_LONG).show();
			}

            startTime = System.currentTimeMillis();

            startBtn.setVisibility(View.INVISIBLE);
            tv_time.setVisibility(View.VISIBLE);
            iconRunning.setVisibility(View.VISIBLE);
			Toast.makeText(getApplicationContext(), "Start Okay", Toast.LENGTH_LONG).show();

		}
	}

	protected void stop() {

        startBtn.setVisibility(View.VISIBLE);
        tv_time.setVisibility(View.INVISIBLE);

		if (mProcessing && this.mClient != null) {
			this.mClient.stopRecordToRecognize();
		}
		mProcessing = false;
        iconRunning.setVisibility(View.INVISIBLE);
		stopTime = System.currentTimeMillis();
	}

	protected void cancel() {
		if (mProcessing && this.mClient != null) {
			mProcessing = false;
			this.mClient.cancel();
			tv_time.setText("--:--");
			mResult.setText("");
            iconRunning.setVisibility(View.INVISIBLE);
			Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_LONG).show();
		} 		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        //mResult.setText(getResources().getString(R.string.volume) + volume + "\n\nRecord Time: " + time + " s");
    }

    // Old api
	@Override
	public void onResult(String result) {	
		if (this.mClient != null) {
			this.mClient.cancel();
			mProcessing = false;
		} 
		
		String tres = "\n";
		String header = "";
		
		try {
		    JSONObject j = new JSONObject(result);
		    JSONObject j1 = j.getJSONObject("status");
		    int j2 = j1.getInt("code");
		    if(j2 == 0){
		    	JSONObject metadata = j.getJSONObject("metadata");
		    	//
		    	if (metadata.has("humming")) {
		    		JSONArray hummings = metadata.getJSONArray("humming");
		    		for(int i=0; i<hummings.length(); i++) {
		    			JSONObject tt = (JSONObject) hummings.get(i); 
		    			String title = tt.getString("title");
		    			JSONArray artistt = tt.getJSONArray("artists");
		    			JSONObject art = (JSONObject) artistt.get(0);
		    			String artist = art.getString("name");
		    			tres = tres + (i+1) + ".  " + title + "\n";
		    		}
		    	}
		    	if (metadata.has("music")) {
		    		JSONArray musics = metadata.getJSONArray("music");
		    		for(int i=0; i<musics.length(); i++) {
		    			JSONObject tt = (JSONObject) musics.get(i); 
		    			String title = tt.getString("title");
		    			JSONArray artistt = tt.getJSONArray("artists");
		    			JSONObject art = (JSONObject) artistt.get(0);
		    			String artist = art.getString("name");
		    			tres = tres + (i+1) + ".  Title: " + title + "    Artist: " + artist + "\n";
		    			header = title;
		    		}
		    	}
		    	if (metadata.has("streams")) {
		    		JSONArray musics = metadata.getJSONArray("streams");
		    		for(int i=0; i<musics.length(); i++) {
		    			JSONObject tt = (JSONObject) musics.get(i); 
		    			String title = tt.getString("title");
		    			String channelId = tt.getString("channel_id");
		    			tres = tres + (i+1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
		    		}
		    	}
		    	if (metadata.has("custom_files")) {
		    		JSONArray musics = metadata.getJSONArray("custom_files");
		    		for(int i=0; i<musics.length(); i++) {
		    			JSONObject tt = (JSONObject) musics.get(i); 
		    			String title = tt.getString("title");
		    			tres = tres + (i+1) + ".  Title: " + title + "\n";
		    		}
		    	}
		    	tres = tres + "\n\n" ;//+ result;
		    }else{
		    	tres = result;
		    }
		} catch (JSONException e) {
			tres = result;
		    e.printStackTrace();
		}

        stop();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(header);
        builder.setMessage(tres);
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
		//mResult.setText(tres);
	}
	
	@Override  
    protected void onDestroy() {  
        super.onDestroy();
        Log.e("MainActivity", "release");
        if (this.mClient != null) {
        	this.mClient.release();
        	this.initState = false;
        	this.mClient = null;
        }
    } 
}
