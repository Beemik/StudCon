package com.example.studcon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BrowserActivity extends Activity {

	private TextView userTextView;
	private ListView categoriesListView;
	private ArrayAdapter<String> categoriesAdapter;
	private String mobile_id = "";

	public static final String CLICKED_CATEGORY = "clicked_category";
	public static final String MOBILE_ID = "beacon_id";
	
	public static final String CATEGORIES_URL = "http://ondraszek.ds.polsl.pl/748v0et2c1/getCategories.php";
	public static final String MOBILE_URL = "http://ondraszek.ds.polsl.pl/748v0et2c1/getMobileID.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);
		userTextView = (TextView) findViewById(R.id.textView1);
		categoriesListView = (ListView) findViewById(R.id.listView1);
		userTextView.setText(String.format("Welcome %s!", getIntent()
				.getExtras().getString(MainActivity.USERNAME)));
		new GetCategoriesFromDB().execute();
		categoriesListView.setOnItemClickListener(createOnItemClickListener());
		new GetMobileID().execute();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Intent intent;
		switch (id) {
		case R.id.action_about:
			intent = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(intent);
			break;
		case R.id.action_logout:
			intent = new Intent(getApplicationContext(), MainActivity.class);
			finish();
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// when user clicked on category
	private AdapterView.OnItemClickListener createOnItemClickListener() {
		return new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						ContentActivity.class);
				intent.putExtra(CLICKED_CATEGORY, categoriesAdapter.getItem(position));
				intent.putExtra(MOBILE_ID, mobile_id);
				startActivity(intent);
			}
		
		};
	}

	class GetCategoriesFromDB extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			DefaultHttpClient httpClient = new DefaultHttpClient();
			InputStream is = null;
			String stringJSON = "";
			try {
				HttpPost httpPost = new HttpPost(CATEGORIES_URL);
				httpPost.setEntity(new StringEntity(""));
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				is.close();
				stringJSON = sb.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (stringJSON.equals("") == false) {
				stringJSON = stringJSON.replace("[", "");
				stringJSON = stringJSON.replace("]", "");
				stringJSON = stringJSON.replace("{", "");
				stringJSON = stringJSON.replace("}", "");
				stringJSON = stringJSON.replace("\"", "");
				String[] tmp = stringJSON.split(",");
				stringJSON = tmp[0];
				for (int i = 1; i < tmp.length; i++) {
					stringJSON += String.format(":%s", tmp[i]);
				}
			}
			return stringJSON;
		}

		protected void onPostExecute(String file_url) {
			// display list with categories
			if (file_url.equals("") == false) {
				ArrayList<String> categories = new ArrayList<String>();
				String[] tmp = file_url.split(":");
				for (int i = 1; i < tmp.length; i += 2) {
					categories.add(tmp[i]);
				}
				categoriesAdapter = new ArrayAdapter<String>(
						getApplicationContext(),
						android.R.layout.simple_list_item_1, categories) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						View view = super
								.getView(position, convertView, parent);
						TextView textView = (TextView) view
								.findViewById(android.R.id.text1);
						textView.setTextColor(Color.BLACK);
						return view;
					}
				};
				categoriesListView.setAdapter(categoriesAdapter);
			}
		}
	}

	class GetMobileID extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			DefaultHttpClient httpClient = new DefaultHttpClient();
			JSONObject json = new JSONObject();
			InputStream is = null;
			String stringJSON = "";
			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = manager.getConnectionInfo();

			try {
				json.put("user_id",
						getIntent().getExtras().getString(MainActivity.USER_ID));
				json.put("manufacturer", android.os.Build.MANUFACTURER);
				json.put("model", android.os.Build.MODEL);
				json.put("mac_address", info.getMacAddress());
				json.put("operating_system", "Android");
				json.put("version", android.os.Build.VERSION.RELEASE);

				HttpPost httpPost = new HttpPost(MOBILE_URL);
				httpPost.setEntity(new StringEntity(json.toString()));
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				is.close();
				stringJSON = sb.toString();
				if (stringJSON.equals("false") == false) {
					stringJSON = stringJSON.replace("[", "");
					stringJSON = stringJSON.replace("]", "");
					stringJSON = stringJSON.replace("{", "");
					stringJSON = stringJSON.replace("}", "");
					stringJSON = stringJSON.replace("\"", "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return stringJSON;
		}

		protected void onPostExecute(String file_url) {
			if (file_url.equals("false") == false) {
				String[] tmp = file_url.split(":");
				mobile_id = tmp[1];
			}
		}
	}
}