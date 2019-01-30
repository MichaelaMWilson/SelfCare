package com.example.mzw5443.selfcare;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * This class performs an http request on the Iodine API to access its information
 *
 * Resources referenced:
 *       JSON and Http requests - Lecture Notes 19: Access to Remote Services
 *                                Used the class given at the end of the notes
 *
 * Iodine API Documentation:
 *       https://www.iodine.com/api/docs
 *
 **/

public class JSONViaHttp {
    static final String TAG = "JSONViaHttp";

    /**
     * Make an HTTP request that returns a JSON object, or null if there was an error
     *
     * @param method the request method in all caps ("GET", "POST", "PUT", or "DELETE")
     * @param sURL the URL including the scheme e.g. ("http://www.example.com/path")
     * @param queryString the key value pairs for the query string
     * @param payload a String containing the body of the request Note that this payload
     *                should be URL-encoded
     * @return a url connection that can be used to get the response
     */

    public static JSONObject get(String method, String sURL, QueryStringParams queryString, String payload){

        HttpURLConnection urlConnection = makeRequest(method, sURL, queryString, payload);
        if(urlConnection == null) return null;

        return getResponse(urlConnection);
    }


    /**
     * Set up the header and body of the HTTP request
     */
    private static HttpURLConnection makeRequest(String method, String sURL,
                                                 QueryStringParams queryString, String payload){
        if(queryString != null) sURL = sURL + queryString;

        URL url;
        try{
            url = new URL(sURL);
        }
        catch(MalformedURLException e){
            Log.e(TAG, "Error creating URL: " + e.getMessage());
            return null;
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        catch (IOException e){
            Log.e(TAG, "Error creating URL: " + e.getMessage());
            return null;
        }

        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");

        //API key
        urlConnection.setRequestProperty("x-api-key", "Your API key here");

        if(payload != null && method.equals("POST")){
            OutputStreamWriter osWriter;
            try{
                urlConnection.setRequestMethod(method);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                osWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                osWriter.write(payload);
                osWriter.flush();
                osWriter.close();
            }
            catch (IOException e){
                Log.e(TAG, "Error setting body for request: " + e.getMessage());
            }
        }
        else{
            try{
                urlConnection.setRequestMethod(method);
            }
            catch (ProtocolException e){
                Log.e(TAG, "Error setting method (" + method + ") for request: " + e.getMessage());
                return null;
            }
        }
        return urlConnection;
    }

    private static JSONObject getResponse(HttpURLConnection urlConnection){
        try{
            int responseCode = urlConnection.getResponseCode();
            if(responseCode != 200) {
                Log.e(TAG, "Request for '" + urlConnection.getURL() + "' returned request code: " + responseCode);
                return null;
            }
        }
        catch(IOException e){
            Log.e(TAG, "Error making http request to '" + urlConnection.getURL() + ": " + e.getMessage());
            return null;
        }
        StringBuilder json = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
            String line;
            while((line = reader.readLine()) != null){
                json.append(line);
            }
            reader.close();
        }
        catch (Exception e){
            Log.e(TAG, "Error converting result " + e.toString());
            return null;
        }
        try{
            return new JSONObject(json.toString());
        }
        catch(JSONException e){
            Log.e(TAG, "Error parsing data " + e.toString());
            return null;
        }
    }
    static class QueryStringParams{
        private ArrayList<String> keys = new ArrayList<>();
        private ArrayList<String> values = new ArrayList<>();

        /**
         * Add a new key value pair for the query string. Both the key and value will
         * be URL encoded before output in the query string.
         *
         * @param key The query string key
         * @param value The query string parameter
         */
        public void add(String key, String value){
            try{
                keys.add(URLEncoder.encode(key, "UTF-8"));
                values.add(URLEncoder.encode(value, "UTF-8"));
            }
            catch(UnsupportedEncodingException e){
                Log.wtf(TAG, "Cannot encode as UTF-8 " + e.getMessage());
            }
        }

        @Override
        public String toString(){
            StringBuilder retVal = new StringBuilder();
            for(int i = 0; i < keys.size(); i++){
                if(i == 0){
                    retVal.append("?");
                }
                else{
                    retVal.append("&");
                }
                retVal.append(keys.get(i));
                if(!values.get(i).equals("")){
                    retVal.append("=");
                    retVal.append(values.get(i));
                }
            }
            return retVal.toString();
        }
    }
}
