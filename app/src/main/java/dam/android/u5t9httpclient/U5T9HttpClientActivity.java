package dam.android.u5t9httpclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class U5T9HttpClientActivity extends AppCompatActivity implements View.OnClickListener {

    private final String URL_GEONAMES = "http://api.geonames.org/wikipediaSearchJSON";
    private final String USER_NAME = "oalexis";
    private final int ROWS = 10;

    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ArrayList<String> listSearchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u5_t9_http_client);
        setUI();
    }

    private void setUI() {

        etPlaceName = (EditText) findViewById(R.id.etPlacename);
        btSearch = (Button) findViewById(R.id.btSearch);
        btSearch.setOnClickListener(this);
        listSearchResult = new ArrayList<>();
        lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);
        lvSearchResult.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listSearchResult));
    }

    @Override
    public void onClick(View v) {
        if (isNetworkAvailable()) {
            String place = etPlaceName.getText().toString();
            if (!place.isEmpty()) {
                URL url = null;
                try {
                    url = new URL(URL_GEONAMES + "?q=" + place + "&maxRows=" + ROWS + "&userName=" + USER_NAME);
                    GetHttpDataTask taskGetData = new GetHttpDataTask();
                    taskGetData.execute(url);
                } catch (MalformedURLException e) {
                    Log.i("URL", e.getMessage());
                }
            } else Toast.makeText(this, "Write a place to search", Toast.LENGTH_LONG).show();
        } else Toast.makeText(this, "Sorry,network is not available", Toast.LENGTH_LONG).show();
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        Boolean networkAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            networkAvailable = true;
        }
        return networkAvailable;
    }

    public class GetHttpDataTask extends AsyncTask<URL, Void, Boolean> {

        private final int CONNECTION_TIMEOUT = 15000;
        private final int READ_TIMEOUT = 10000;

        @Override
        protected Boolean doInBackground(URL... urls) {
            Boolean result = false;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(READ_TIMEOUT);
                if (urlConnection.getResponseCode() == 200) {
                    String resultStream = readStream(urlConnection.getInputStream());
                    JSONObject json = new JSONObject(resultStream);
                    JSONArray jArray = json.getJSONArray("geonames");
                    listSearchResult.clear();
                    if (jArray.length() > 0) {
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject item = jArray.getJSONObject(i);
                            listSearchResult.add(item.getString("summary"));
                        }
                    } else {
                        listSearchResult.add("No information found");
                    }
                } else {
                    Log.i("URL", "ErrorCode: " + urlConnection.getResponseCode());
                }
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return result;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResult.getAdapter();
                adapter.notifyDataSetChanged();

            } else {

                Toast.makeText(getApplicationContext(), "Request ends with error", Toast.LENGTH_LONG).show();
            }
        }

        private String readStream(InputStream in) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    sb.append(nextLine);

                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return sb.toString();
        }


    }


}
