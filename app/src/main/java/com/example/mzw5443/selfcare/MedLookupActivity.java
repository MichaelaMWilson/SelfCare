package com.example.mzw5443.selfcare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class searches the IODINE API medicine database and brings up results for the desired
 * medication.
 *
 * Resources referenced:
 *       Link URL to button click -
 *          https://stackoverflow.com/questions/24261224/android-open-url-onclick-certain-button
 *       Hyperlink in TextView -
 *          https://stackoverflow.com/questions/30839300/make-hyperlink-in-textview-android
 *       JSON and Http requests - Lecture Notes 19: Access to Remote Services
 *
 * Iodine API Documentation:
 *       https://www.iodine.com/api/docs
 **/

public class MedLookupActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    static final String SERVER = "https://api.iodine.com/drug/";
    static final String TAG = "SettingsActivity";

    AsyncHttpRequest asyncHttpRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the colors based on the app theme
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = sharedPref.getInt("theme", R.style.AppTheme);
        setTheme(theme);

        setContentView(R.layout.activity_med_lookup);
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(!((TextView)findViewById(R.id.tvSideEffects)).getText().equals("")){
            (findViewById(R.id.tvDescTitle)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tvSideEffTitle)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tvTipsTitle)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tvLinkTitle)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tvNoSearch)).setVisibility(View.GONE);
        }
        if(!((TextView)findViewById(R.id.tvDescription)).getText().equals("")){
            (findViewById(R.id.tvNoSearch)).setVisibility(View.GONE);
        }
    }

    //btnSearch onClick is set to this
    public void onClick(View view){
        if(asyncHttpRequest != null){
            asyncHttpRequest.cancel(true);
        }

        String search = ((EditText) findViewById(R.id.etSearch)).getText().toString().trim();
        if(search.isEmpty()){
            Toast.makeText(this, "Search cannot be blank", Toast.LENGTH_SHORT).show();
        }
        else {
            asyncHttpRequest = new AsyncHttpRequest("GET", SERVER + search + ".json", null, null);
            asyncHttpRequest.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(asyncHttpRequest != null){
            asyncHttpRequest.cancel(true);
        }
    }

    //imgbtnIodine onClick is set to this
    public void onIodineClick(View view){
        //Refer the user to the Iodine website
        Uri uri = Uri.parse("https://www.iodine.com/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncHttpRequest extends AsyncTask<Void, Void, JSONObject>{
        String method;
        String url;
        JSONViaHttp.QueryStringParams queryStringParams;
        String payload;

        AsyncHttpRequest(String method, String url, JSONViaHttp.QueryStringParams queryStringParams, String payload){
            this.method = method;
            this.url = url;
            this.queryStringParams = queryStringParams;
            this.payload = payload;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return JSONViaHttp.get(method, url, queryStringParams, payload);
        }

        @Override
        protected void onPostExecute(JSONObject result){
            super.onPostExecute(result);

            (findViewById(R.id.tvNoSearch)).setVisibility(View.GONE);

            if(result != null){
                try{
                    String common = result.getJSONObject("name").getString("common");
                    String generic = result.getJSONObject("name").getString("generic");
                    String name;

                    //The medication searched does not exist/is not in the api
                    if(common.equals("null")){
                        ((TextView) findViewById(R.id.tvPillName)).setText(R.string.results);
                        ((TextView) findViewById(R.id.tvDescription)).setText(R.string.spelling);
                        ((TextView) findViewById(R.id.tvTips)).setText("");
                        ((TextView) findViewById(R.id.tvSideEffects)).setText("");
                        ((TextView) findViewById(R.id.tvLink)).setText("");

                        (findViewById(R.id.tvDescTitle)).setVisibility(View.GONE);
                        (findViewById(R.id.tvSideEffTitle)).setVisibility(View.INVISIBLE);
                        (findViewById(R.id.tvTipsTitle)).setVisibility(View.INVISIBLE);
                        (findViewById(R.id.tvLinkTitle)).setVisibility(View.INVISIBLE);
                    }
                    //Fill TextViews with the appropriate text from the api
                    else {
                        //For medications where the common and generic name are the same,
                        //the full name would be something like, ibuprofen (ibuprofen),
                        //as opposed to, Advil (ibuprofen). This code sets the name as the
                        //first brand name listed followed by the generic name
                        if(!common.equals(generic))
                            name = result.getJSONObject("name").getString("full");
                        else {
                            name = result.getJSONObject("name").getJSONArray("brand").getString(0);
                            name += " (" + generic + ")";
                        }

                        String description = result.getJSONObject("basics").getString("moa");

                        //Sometimes the API does not have any side effects listed
                        JSONArray sideEffectsArr;
                        try {
                            sideEffectsArr = result.getJSONArray("whatToExpect").getJSONObject(0).getJSONArray("side_effects");
                        }
                        catch(JSONException e){
                            sideEffectsArr = null;
                        }

                        StringBuilder sideEffects = new StringBuilder();
                        if(sideEffectsArr != null) {
                            for (int i = 0; i < sideEffectsArr.length(); i++) {
                                if (i == sideEffectsArr.length() - 1) {
                                    sideEffects.append("• ");
                                    sideEffects.append(sideEffectsArr.getJSONObject(i).getString("name"));
                                } else {
                                    sideEffects.append("• ");
                                    sideEffects.append(sideEffectsArr.getJSONObject(i).getString("name"));
                                    sideEffects.append(" \n");
                                }
                            }
                        }
                        else sideEffects.append("Information unavailable");


                        //Sometimes the api does not have any tips listed
                        JSONArray tipsArr;
                        try{
                            tipsArr = result.getJSONArray("pharmacistTips");
                        }
                        catch (JSONException e){
                            tipsArr = null;
                        }
                        StringBuilder tips = new StringBuilder();
                        if(tipsArr != null) {
                            for (int i = 0; i < tipsArr.length(); i++) {
                                if (i == tipsArr.length() - 1) {
                                    tips.append("• ");
                                    tips.append(tipsArr.getString(i));
                                } else {
                                    tips.append("• ");
                                    tips.append(tipsArr.getString(i));
                                    tips.append(" \n\n");
                                }
                            }
                        }
                        else tips.append("Information unavailable");

                        String link = result.getJSONObject("link").getString("html");

                        //Create a hyperlink
                        TextView tvLink = findViewById(R.id.tvLink);
                        tvLink.setMovementMethod(LinkMovementMethod.getInstance());

                        ((TextView) findViewById(R.id.tvPillName)).setText(name);
                        ((TextView) findViewById(R.id.tvDescription)).setText(description);
                        ((TextView) findViewById(R.id.tvTips)).setText(tips);
                        ((TextView) findViewById(R.id.tvSideEffects)).setText(sideEffects);

                        //Set Iodine link for medication (as per their crediting rules)
                        tvLink.setText(Html.fromHtml(link));

                        (findViewById(R.id.tvDescTitle)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.tvSideEffTitle)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.tvTipsTitle)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.tvLinkTitle)).setVisibility(View.VISIBLE);
                    }
                }
                catch(JSONException e){
                    Log.e(TAG, "Error retrieving list: " + e.getMessage());
                }
            }
            else {
                ((TextView) findViewById(R.id.tvPillName)).setText(R.string.badRequest);
            }
            asyncHttpRequest = null;
        }
    }
}
