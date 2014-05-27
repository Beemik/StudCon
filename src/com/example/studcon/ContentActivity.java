package com.example.studcon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

public class ContentActivity extends Activity {

	private TextView categoryTextView;
	private TextView beaconsTextView;
	private String beacon_id = "";
	private String url = "http://www.google.com";
	private WebView webView;
	private int beaconCount;
	private ConnectivityManager connectivityManager;
	private NetworkInfo networkInfo;
	private ProgressBar progressBar;

	private Region beaconsRegion = new Region("region_id", null, null, null);
	private BeaconManager beaconManager;
	private List<Beacon> beaconList;

	private static final int REQUEST_ENABLE_BT = 0;
	public static final String BEACON_URL = "http://ondraszek.ds.polsl.pl/748v0et2c1/getBeaconID.php";
	public static final String GET_URL = "http://ondraszek.ds.polsl.pl/748v0et2c1/getUrl.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		categoryTextView = (TextView) findViewById(R.id.categoryTextView);
		categoryTextView.setText(getIntent().getExtras().getString(
				BrowserActivity.CLICKED_CATEGORY));
		beaconsTextView = (TextView) findViewById(R.id.beaconTextView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setMax(100);
		progressBar.setVisibility(View.GONE);
		webView = (WebView) findViewById(R.id.webView1);
		webView.setWebViewClient(new MyBrowserViewClient());
		webView.setWebChromeClient(new MyBrowserChromeClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.loadUrl(url);

		beaconCount = 0;
		beaconList = new ArrayList<Beacon>();
		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region arg0, final List<Beacon> arg1) {
				// TODO Auto-generated method stub
				beaconList = arg1;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						beaconsTextView.setText(String.format("%d connected.",
								beaconList.size()));
						if (beaconCount != beaconList.size()
								&& beaconList.size() > 0) {
							beaconCount = beaconList.size();
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							new GetBeaconID().execute();
							new GetURL().execute();
						}
					}
				});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.content, menu);
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
		case android.R.id.home:
			finish();
			break;
		case R.id.action_about:
			intent = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(intent);
			break;
		case R.id.action_previous:
			if (webView.canGoBack())
				webView.goBack();
			break;
		case R.id.action_next:
			if (webView.canGoForward())
				webView.goForward();
			break;
		case R.id.action_refresh:
			webView.reload();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// connect to BeaconService, start ranging and monitoring
	void connectWithBeacon() {
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				// TODO Auto-generated method stub
				try {
					beaconManager.startRanging(beaconsRegion);
				} catch (RemoteException e) {
					Toast.makeText(getApplicationContext(),
							"Cannot start ranging, something wrong.",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	// when restarting this activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK)
				connectWithBeacon();
			else
				Toast.makeText(this, "Bluetooth not enabled.",
						Toast.LENGTH_LONG).show();
		}
	}

	// when activity starts app check if user has possibility to use this app
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// if device cannot support BT Low Energy
		if (!beaconManager.hasBluetooth()) {
			Toast.makeText(this, "Device doesn't support Bluetooth Low Energy",
					Toast.LENGTH_LONG).show();
			return;
		}
		// if BT isn't enabled, enable it
		if (!beaconManager.isBluetoothEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			connectWithBeacon();
		}
	}

	// stop ranging beacons
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		try {
			beaconManager.stopRanging(beaconsRegion);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Cannot stop ranging.", Toast.LENGTH_LONG)
					.show();
		}
		super.onStop();
	}

	// stop ranging and monitoring beacons
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		beaconManager.disconnect();
		super.onDestroy();
	}

	private class MyBrowserChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			setValue(newProgress);
		}

	}

	private class MyBrowserViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			progressBar.setVisibility(View.GONE);
			progressBar.setProgress(100);
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(0);
			super.onPageStarted(view, url, favicon);
		}
		
	}

	private void setValue(int progress) {
		progressBar.setProgress(progress);
	}

	class GetBeaconID extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub

			DefaultHttpClient httpClient = new DefaultHttpClient();
			JSONObject json = new JSONObject();
			InputStream is = null;
			String stringJSON = "";

			try {
				json.put(
						"mobile_id",
						getIntent().getExtras().getString(
								BrowserActivity.MOBILE_ID));
				json.put("proximityUUID", beaconList.get(0).getProximityUUID());
				json.put("major", beaconList.get(0).getMajor());
				json.put("minor", beaconList.get(0).getMinor());
				json.put(
						"category",
						getIntent().getExtras().getString(
								BrowserActivity.CLICKED_CATEGORY));

				HttpPost httpPost = new HttpPost(BEACON_URL);
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
			if ((file_url.equals("false") || file_url.equals("")) == false) {
				String[] tmp = file_url.split(":");
				beacon_id = tmp[1];
			} else {
				beacon_id = "";
			}
		}
	}

	class GetURL extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			DefaultHttpClient httpClient = new DefaultHttpClient();
			JSONObject json = new JSONObject();
			InputStream is = null;
			String stringJSON = "";

			try {
				json.put("beacon_id", beacon_id);

				HttpPost httpPost = new HttpPost(GET_URL);
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
					stringJSON = stringJSON.replace("/", "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return stringJSON;
		}

		protected void onPostExecute(String file_url) {
			boolean theSameURL = false;
			if ((file_url.equals("false") || file_url.equals("")) == false) {
				String[] tmp = file_url.split(":");
				if (url.equals(tmp[1] + ":" + tmp[2]) == true)
					theSameURL = true;
				else
					url = tmp[1] + ":" + tmp[2];
			} else {
				url = "http://www.google.com";
			}
			connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			networkInfo = connectivityManager.getActiveNetworkInfo();
			if ((networkInfo == null) || (!networkInfo.isConnected())
					|| (!networkInfo.isAvailable()))
				Toast.makeText(getApplicationContext(),
						"No internet connection!", Toast.LENGTH_LONG).show();
			if (theSameURL == false) {
				progressBar.setProgress(0);
				progressBar.setVisibility(View.VISIBLE);
				webView.loadUrl(url);
			}
		}
	}

}
