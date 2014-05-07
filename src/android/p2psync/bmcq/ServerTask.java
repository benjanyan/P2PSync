package android.p2psync.bmcq;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class ServerTask extends AsyncTask<String, Void, String> {
	
	Button syncButton;
	Context context;
	
	protected void setVars(Button syncButton, Context context) {
		this.syncButton = syncButton;
		this.context = context;
	}

	@Override
	protected String doInBackground(String... arg0) {
		Path localSyncDirectory = new Path(Environment.getExternalStorageDirectory().getPath() + File.separator + "test");	//Without trailing slash
		String hostName = arg0[0];
		ServerDiscover serverDiscover = new ServerDiscover();	

		ServerIP host = serverDiscover.getServerIP();

		ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory);
		controlClient.setKey("jd874jks893ka");
		ControlServer server = new ControlServer(5555, localSyncDirectory);
		server.setKey("jd874jks893ka");
		controlClient.run();

		controlClient = null;
		server.run();
		server.exportFileInfo();
		server.close();
		server = null;
		
//		RelativePath localSyncDirectory = new RelativePath(Environment.getExternalStorageDirectory().getPath() + File.separator + "test");	//Without trailing slash
//		String hostName = arg0[0];
//		
//		
//		InetAddress host;
//		try {
//			host = InetAddress.getByName(hostName);
//			ControlClient controlClient = new ControlClient(host, 5555, localSyncDirectory);
//			controlClient.setKey("jd874jks893ka");
//			controlClient.connect();
//			controlClient.control();
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		
//		ControlServer server = new ControlServer(5555, localSyncDirectory);
//		server.setKey("jd874jks893ka");
//		server.run();
		
		return "done";
	}
	
	@Override
	protected void onPreExecute() {
		
	}
	
    @Override
    protected void onPostExecute(String result) {
        Log.d("p2psync.bmcq", "ServerTask executed");
        syncButton.setEnabled(true);
        Toast.makeText(context, "Sync completed", 3).show();
        
   }

}
