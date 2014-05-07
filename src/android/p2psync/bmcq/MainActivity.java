package android.p2psync.bmcq;

import uk.co.bmcq.p2psync.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void startServer(View view) {
		EditText etServerIp = (EditText) findViewById(R.id.editText_ServerIP);
		ServerTask serverTask = new ServerTask();
		Button btSync = (Button) findViewById(R.id.button_startServer);
		btSync.setEnabled(false);
		
		serverTask.setVars(btSync, getApplicationContext());
		serverTask.execute(etServerIp.getText().toString());
	}
	

}
