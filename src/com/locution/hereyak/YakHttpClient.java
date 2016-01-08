package com.locution.hereyak;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.location.Location;
import android.util.Log;

public class YakHttpClient {
	private static final Map<String,String> api_urls = new HashMap<String, String>();
	//private static final String host = "http://192.168.0.100:3000";
	private static final String host = "https://hereyak.com";
	private static final String client_id = "qQfANcIoYYW6AnvR360Mt07nwuSjm7oQuzgNDNtV";
	static {
		api_urls.put("sign_in", host+"/users/sign_in.json");
		api_urls.put("sign_up", host+"/users.json");
		api_urls.put("get_posts", host+"/posts.json");
		api_urls.put("single_post_action", host+"/posts/");
		api_urls.put("send_post", host+"/posts.json");
    }
	private HttpURLConnection conn = null;
	private URL url;
	
	public YakHttpClient(String api_call) throws MalformedURLException,IOException  {
		//empty
	}
	
	private void initConnection(String url_string) throws MalformedURLException,IOException {
		
		try {
			Constants.logMessage(1,"yakhttpclient initConnection","api_url: " +url_string);
			url = new URL(url_string);
		} catch (MalformedURLException e) {
			Constants.logMessage(1,"yakhttpclient initConnection", "malformedurl exception");
		}
		
		conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(15000);
		conn.setConnectTimeout(30000);
		conn.setDoInput(true);
		
		//HTTP JSON header
		conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
	}
	
	/*
	 * Returns {status=>0, data=>{"access_token"=>"xxxx","device_key"=>"yyyyy" // success
	 *         {status=>1, data=>null //failed server side validation
	 *         {status=>2, data=>"error message" //generic failure 
	 */
	public Map<String,Object> sign_in(String email, String password) throws IOException {

		Map <String,Object> returnValues = new HashMap<String,Object>();
		UUID random = UUID.randomUUID();
	    String device_key = random.toString();
	    
	    try {
	    	JSONObject user_json = new JSONObject();
	    	user_json.put("email", email);
		    user_json.put("password", password);
		    
		    JSONObject request_params = new JSONObject();
		    request_params.put("user", user_json);
		    request_params.put("device_key", device_key);
		    request_params.put("client_id", client_id);
		    
		    int tries = 0;
			String data[] = null;
			while (tries < 2) {
				initConnection(api_urls.get("sign_in"));
				conn.setDoOutput(true);

				send_message(request_params.toString());
				Constants.logMessage(1,"sign_in","done sending message, about to read message");
				data = read_data();
				Constants.logMessage(1,"sign_in","received data back from read_data: "+data[0]+ "  "+data[1]);
				
				if (data[0].contains("200") || data[0].contains("201")) {
			    	returnValues.put("status","0");
			    	
			    	JSONObject jObject = new JSONObject(data[1]);
			    	jObject.put("device_key", device_key);
	  	
			    	returnValues.put("data", jObject);
			    	return returnValues;
			    }
			    tries++;
			}
				
	    	if (data != null){
		    	if (data[0].contains("401")) {
		    		returnValues.put("status","1");
		    		returnValues.put("data", null);
		    		Constants.logMessage(1,"sign_in","received 401");
		    	}
		    	else {
		    		returnValues.put("status","2");
		    		returnValues.put("data", data[1]);
		    		Constants.logMessage(1,"sign_in","received some other error");
		    	}
	    	} else {
	    		returnValues.put("status","2");
	    		returnValues.put("data", "some other data issues");
	    		Constants.logMessage(1,"sign_in","some other data issues");
	    	}
	    	return returnValues;
			
	    } catch (JSONException e) {
	    	returnValues.put("status","2");
	    	returnValues.put("data","JSON Exception");
	    	Constants.logMessage(1,"sign_in","JSONException Exception: "+e.getMessage());
	    	return returnValues;
	    } catch (IOException e) {
	    	returnValues.put("status","2");
	    	returnValues.put("data","IO Exception");
	    	Constants.logMessage(1,"sign_in","IOException Exception: "+e.getMessage());
	    	return returnValues;
	    } finally {
	    	conn.disconnect();
	    }
	}
	
	/*
	 * Returns {status=>0, data=>{"access_token"=>"xxxx","device_key"=>"yyyyy" // success
	 *         {status=>1, data=>JSONObject //failed server side validation
	 *         {status=>2, data=>"error message" //generic failure 
	 * 
	 */
	public Map<String,Object> sign_up(String username, String email, String password) {
		Map <String,Object> returnValues = new HashMap<String,Object>();
		UUID random = UUID.randomUUID();
		String device_key = random.toString();
		    
		try {

			JSONObject user_json = new JSONObject();
		    user_json.put("username", username);
		    user_json.put("email", email);
			user_json.put("password", password);
			user_json.put("password_confirmation", password);
			
			JSONObject request_params = new JSONObject();
			request_params.put("user", user_json);
			request_params.put("device_key", device_key);
			request_params.put("client_id", client_id);

			int tries = 0;
			String data[] = null;
			while (tries < 2) {
			
				initConnection(api_urls.get("sign_up"));
				conn.setDoOutput(true);
			    send_message(request_params.toString());
			    data = read_data();

			    if (data[0].contains("200") || data[0].contains("201")) {
			    	returnValues.put("status","0");
			    		
			    	JSONObject jObject = new JSONObject(data[1]);
			    	jObject.put("device_key", device_key);
			    	
			    	returnValues.put("data", jObject);
			    	return returnValues;
			    }
			    tries++;
			    
			}
		    
			if (data != null){
				if (data[0].contains("422")) {
			    	returnValues.put("status","1");
			    	JSONObject jObject = new JSONObject(data[1]);
			    	returnValues.put("data", jObject);
			    }
			    else {
			    	returnValues.put("status","2");
			    	returnValues.put("data", data[1]);
			    } 
			} else {
	    		returnValues.put("status","2");
	    		returnValues.put("data", "some other data issues");
	    		Constants.logMessage(1,"sign_up","some other data issues");
	    	}
		    return returnValues;
		    	
		} catch (JSONException e) {
			returnValues.put("status","2");
		    returnValues.put("data","JSON Exception");
		    return returnValues;
		} catch (IOException e) {
			returnValues.put("status","2");
		    returnValues.put("data","IO Exception");
		    return returnValues;
		} finally {
			conn.disconnect();
		}
	}
	
	public Map<String,Object> get_posts(String device_key, String access_token, Location location, String last_message_received) {
		Map <String,Object> returnValues = new HashMap<String,Object>();
		    
		//conn.setDoOutput(true);
		    
		try {
			
			String new_url = (String)(api_urls.get("get_posts"));
			new_url = new_url+"/?lat="+location.getLatitude();
			new_url = new_url+"&lon="+location.getLongitude();
			new_url = new_url+"&device_key="+device_key;
			new_url = new_url+"&last_message_received="+last_message_received;
			
			
						
			try {
				Constants.logMessage(1,"get_posts","api_url: " +new_url);
				initConnection(new_url);
			} catch (MalformedURLException e) {
				Constants.logMessage(1,"yakhttpclient get_posts", "malformed url exception for get posts");
			}
			
			conn.setRequestProperty("Authorization", "Bearer token="+access_token);
			
		    //send_message(request_params.toString());
		    String data[] = read_data();

		    	
		    if (data[0].contains("200") || data[0].contains("201")) {
		    	returnValues.put("status","0");
		    		
		    	JSONArray jObject = new JSONArray(data[1]);		    	
		    	returnValues.put("data", jObject);
		    }
		    else if (data[0].contains("422")) {
		    	returnValues.put("status","1");
		    	JSONObject jObject = new JSONObject(data[1]);
		    	returnValues.put("data", jObject);
		    }
		    else {
		    	returnValues.put("status","2");
		    	returnValues.put("data", data[1]);
		    } 
		    return returnValues;
		    	
		} catch (JSONException e) {
			returnValues.put("status","2");
		    returnValues.put("data","JSON Exception");
		    return returnValues;
		} catch (IOException e) {
			returnValues.put("status","2");
		    returnValues.put("data","IO Exception");
		    return returnValues;
		    
		} catch (Exception e) {
			Constants.logMessage(1,"get_posts","exception: "+e.getMessage());
			return null;
		} finally {
			conn.disconnect();
		}
	}
	
	public Map<String,Object> send_post(String device_key, String access_token, String message, Location location, Address address) {
		Map <String,Object> returnValues = new HashMap<String,Object>();
		    
		try {
			
			JSONObject post = new JSONObject();
			post.put("content", message);
			if (address != null) {
				post.put("city",address.getLocality());
				post.put("province",address.getAdminArea());
				post.put("country",address.getCountryCode());
				post.put("postal_code",address.getPostalCode());
			}
			
		    
		    JSONObject request_params = new JSONObject();
		    request_params.put("post", post);
			request_params.put("device_key", device_key);
			request_params.put("lat", Double.toString(location.getLatitude()));
			request_params.put("lon", Double.toString(location.getLongitude()));
			
			int tries = 0;
			String data[] = null;
			while (tries < 2) {
				initConnection(api_urls.get("send_post"));
				
				conn.setRequestProperty("Authorization", "Bearer token="+access_token);
				conn.setDoOutput(true);
			
				send_message(request_params.toString());
				Constants.logMessage(1,"send_post","sent message, now trying to read.");
			    data = read_data();
			    Constants.logMessage(1,"send_post","finish reading. Data: "+data.toString());
			    
			    if (data[0].contains("200") || data[0].contains("201")) {
			    	returnValues.put("status","0");
			    	returnValues.put("data", data[1]);
			    	return returnValues;
			    }
			    tries++;
			}
			    
		    if (data !=null) {
			    if (data[0].contains("422")) {
			    	returnValues.put("status","1");
			    	JSONObject jObject = new JSONObject(data[1]);
			    	returnValues.put("data", jObject);
			    }
			    else {
			    	returnValues.put("status","2");
			    	returnValues.put("data", data[1]);
			    } 
		    }
		    else {
		    	returnValues.put("status","2");
		    	returnValues.put("data", "other issues encountered");
		    }
		    return returnValues;
		    	
		} catch (JSONException e) {
			returnValues.put("status","2");
		    returnValues.put("data","JSON Exception");
		    return returnValues;
		} catch (IOException e) {
			returnValues.put("status","2");
		    returnValues.put("data","IO Exception");
		    Constants.logMessage(1,"send_post","IOException: "+ e.toString());
		    return returnValues;
		} finally {
			conn.disconnect();
		}
	}
	
	
	
	private String[] read_data() throws IOException {
		String[] ret = new String[2];
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			int status = conn.getResponseCode();
		    ret[0] = Integer.toString(status);

	        String line;
		    if (status <= 201 && status < 300) {
		    	br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    } else {
		    	br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		    }
		    
            while ((line = br.readLine()) != null) {
            	sb.append(line+"\n");
            }

	        ret[1] = sb.toString();
	        Constants.logMessage(1,"read_data","data Status: "+ret[0]+" message: "+ret[1]);
	        return ret;
		} catch (IOException e)	 {
			ret[0] = "401";
			ret[1] = "Invalid credentials";
			Constants.logMessage(1,"read_data","IOException: "+ e.toString());
			return ret; 
		} catch (Exception e){
			Constants.logMessage(1,"read_data","weird exeption: "+ e.toString());
			return null;
		} finally {
			if (br != null) {
				br.close();
			}
        }
	}
	
	private void send_message(String message) throws IOException{
		Constants.logMessage(1,"send_message","YakMessage: "+ message);
		conn.setFixedLengthStreamingMode(message.getBytes().length);
		conn.connect();
		Constants.logMessage(1,"send_message","after conn.connect ");
		OutputStream os = new BufferedOutputStream(conn.getOutputStream());
		try {
			os.write(message.getBytes());
			os.flush();
			Constants.logMessage(1,"send_message","finished os.write and os.flush");
		} finally {
			os.close();
		}
		
	}
}
