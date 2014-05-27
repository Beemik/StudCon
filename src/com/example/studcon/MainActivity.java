package com.example.studcon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String USERNAME = "username";
	public static final String USER_ID = "user_id";

	private EditText user;
	private EditText pass;
	private Button login;
	private ProgressDialog progressDialog;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;

	private static final String LOGIN_URL = "http://ondraszek.ds.polsl.pl/748v0et2c1/getUser.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		user = (EditText) findViewById(R.id.loginEditText);
		pass = (EditText) findViewById(R.id.passEditText);
		login = (Button) findViewById(R.id.loginButton);

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				networkInfo = connectivityManager.getActiveNetworkInfo();
				if ((networkInfo == null) || (!networkInfo.isConnected())
						|| (!networkInfo.isAvailable()))
					Toast.makeText(getApplicationContext(),
							"No internet connection!", Toast.LENGTH_LONG)
							.show();
				else
					new AttemptLogin().execute();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

	class AttemptLogin extends AsyncTask<String, String, String> {

		boolean failure = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("Please wait...");
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			String username = user.getText().toString();
			String password = pass.getText().toString();

			DefaultHttpClient httpClient = new DefaultHttpClient();
			JSONObject json = new JSONObject();
			InputStream is = null;
			String stringJSON = "";

			try {
				json.put("login", username);
				json.put("password", password);

				HttpPost httpPost = new HttpPost(LOGIN_URL);
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
				if (stringJSON.equals("false") == true) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(getApplicationContext(),
									"Ivalid login or password!",
									Toast.LENGTH_LONG).show();
						}
					});
					return null;
				} else {
					stringJSON = stringJSON.replace("[", "");
					stringJSON = stringJSON.replace("]", "");
					stringJSON = stringJSON.replace("{", "");
					stringJSON = stringJSON.replace("}", "");
					stringJSON = stringJSON.replace("\"", "");
					String[] tmp = stringJSON.split(":");
					Intent intent = new Intent(getApplicationContext(),
							BrowserActivity.class);
					finish();
					intent.putExtra(USERNAME, username);
					intent.putExtra(USER_ID, tmp[1]);
					startActivity(intent);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(String file_url) {
			progressDialog.dismiss();
			if (file_url != null)
				Toast.makeText(MainActivity.this, file_url, Toast.LENGTH_LONG)
						.show();
		}
	}

}
