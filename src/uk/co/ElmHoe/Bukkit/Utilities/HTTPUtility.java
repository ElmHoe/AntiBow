package uk.co.ElmHoe.Bukkit.Utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTTPUtility {
	private static String USER_AGENT = "ElmHoeABLogs/20.0";

	
	public static void sendPost(String urlParameters) throws Exception {

		try{
			String url = "http://server.elmhoe.co.uk/randomFileForLogging.php";
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
	
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
	
	
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
			//print result
			//Bukkit.getLogger().info("An error was thrown and sent to Josh to investigate.");
		}catch(Exception e){
/*			e.printStackTrace();
			Bukkit.getLogger().warning("Failed to send error logs, this got awkward...");
*/		
		}
	}
}
