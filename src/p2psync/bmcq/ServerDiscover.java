package p2psync.bmcq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import com.google.gson.*;

public class ServerDiscover {
	
	public ServerIP getServerIP() {
		Gson gson = new Gson();
		return gson.fromJson(httpGet("http://stuff.bmcq.co.uk/p2psync/getServer.php?key=q5mAKKZdn6ke9ghVa7YbE7uM"), ServerIP.class);
	}
	
	public void setServerIP(ServerIP server) {
		Gson gson = new Gson();
		httpGet("http://stuff.bmcq.co.uk/p2psync/setServer.php?key=q5mAKKZdn6ke9ghVa7YbE7uM&ip=" + gson.toJson(server));
	}
	
	private String httpGet(String address) {
		HttpURLConnection httpCon;
		URL url = null;
		BufferedReader input;
		String data, contents;
		contents = null;
		try {
			url = new URL(address);
		} catch (MalformedURLException e) {
			Utils.logE("Malformed URL in getServerIP!");
			e.printStackTrace();
		}
		
		contents = "";
		
		try {
			httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestMethod("GET");
			input = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			while ((data = input.readLine()) != null) {
				contents += data;
			}
		} catch (ProtocolException e) {
			Utils.logE("HTTP error");
			e.printStackTrace();
		} catch (IOException e) {
			Utils.logE("I/O Error while reading from " + url.getHost());
			e.printStackTrace();
		}

		return contents;
	}
}
