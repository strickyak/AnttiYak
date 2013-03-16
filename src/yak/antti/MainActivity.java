package yak.antti;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Context mainContext;
	Store store = new Store("http://yak.net:30332/");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mainContext = this;
		
//		String[] numbers = new String[] {"One", "Two", "Three"};
//		DemoListView v = new DemoListView(mainContext, numbers);
//		setContentView(v);
		
		// setContentView(R.layout.activity_main);
		

		Intent intent = getIntent();
		Uri uri = intent.getData();
		Bundle extras = intent.getExtras();
		String path = uri == null ? "/" : uri.getPath();
		String query = uri == null ? "" : uri.getQuery();

		display(path, query, extras, savedInstanceState); 
	}

	private void display(String path, String query, Bundle extras,
			Bundle savedInstanceState) {
		Log.i("antti", "PATH=" + path);
		String[] words = path.split("/");
		Log.i("antti", "words.LEN=" + words.length);
		String verb = "";
		if (words.length > 1) {
			verb = words[1];
		}

		Log.i("antti", "=============== VERB =" + verb);
		if (verb.equals("list")) {
			String[] labels = extras.getString("items").split(";");
			displayList(labels);
		} else if (verb.equals("channel")) {
			displayChannel(words[2]);
		} else if (verb.equals("create")) {
			displayCreateInode();
		} else if (verb.equals("web")) {
			displayWeb((String)extras.get("html"));
		} else {
			displayDefault();
		}
	}

	private void displayDefault() {
		String[] numbers = new String[] {"One", "Two", "Three", "Channel"};
		DemoListView v = new DemoListView(mainContext, numbers);
		setContentView(v);
	}

	private void displayWeb(String html) {
		DemoWebView v = new DemoWebView(mainContext, html);
		setContentView(v);
	}
	
	private void displayChannel(String chanKey) {
		StringBuilder sb = new StringBuilder();
		String[] inodes = null;
		try {
			inodes = this.store.fetchInodes(chanKey);
		} catch (ClientProtocolException e) {
			Log.i("antti", e.getMessage());
		} catch (IOException e) {
			Log.i("antti", e.getMessage());
		}
		sb.append(this.renderInodes(inodes));
		sb.append("<p><a href=\"/create\">Create</a></p>");
		
		DemoWebView v = new DemoWebView(mainContext, sb.toString());
		setContentView(v);
	}
	
	private String renderInodes(String[] inodes) {
		StringBuilder sb = new StringBuilder();
		for (String inode : inodes) {
			sb.append("<p>");
			sb.append(inode);
			sb.append("</p>");
		}
		return sb.toString();
	}

	public void displayCreateInode() {
		final EditText ed = new EditText(this);

		ed.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				| InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
		final LayoutParams widgetParams = new LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
		
		ed.setLayoutParams(widgetParams);
		ed.setTextAppearance(this, R.style.teletype);
		ed.setBackgroundColor(Color.BLACK);
		ed.setGravity(Gravity.TOP);
		ed.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
		ed.setVerticalFadingEdgeEnabled(true);
		ed.setVerticalScrollBarEnabled(true);
		ed.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter"
				// button
				// if ((event.getAction() == KeyEvent.ACTION_DOWN)
				// &&
				// (keyCode == KeyEvent.KEYCODE_ENTER)) {
				// // Perform action on key press
				// Toast.makeText(TerseActivity.this, ed.getText(),
				// Toast.LENGTH_SHORT).show();
				// return true;
				// }
				return false;
			}
		});

		Button btn = new Button(this);
		btn.setText("Send");
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				return;
			}
		});

		LinearLayout linear = new LinearLayout(this);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.addView(btn);
		linear.addView(ed);
		setContentView(linear);
	}
	
	// Provides access to the storage.
	public class Store {
		
		String baseUrl;
		
		public Store(String baseUrl) {
			this.baseUrl = baseUrl;
		}
		
		// Fetch the inodes from storage.
		public String[] fetchInodes(String chanKey) throws ClientProtocolException, IOException {
			String scanUrl = this.baseUrl + "?f=scan&u=0&c=" + chanKey;
			String scan = null;
			
			Log.i("antti", "~~~Store Scan Url: " + scanUrl);
			scan = this.getUrl(scanUrl);
			Log.i("antti", "~~~Store Scan Reply: " + scan);
			
			String[] inodeKeys = scan.split("\n");
			int n = inodeKeys.length;
			String[] inodes = new String[n];
			
			Log.i("antti", "~~~Store found " + n + " number of inode keys to fetch.");
			for (int i = 0; i < n; i++) {
				String fetchUrl = this.baseUrl + "?f=fetch&u=0&c=" + chanKey + "&i=" + inodeKeys[i];
				
				Log.i("antti", "~~~Store Fetch Url: " + fetchUrl);
				inodes[i] = this.getUrl(fetchUrl);
				Log.i("antti", "~~~Store Fetch Reply: " + inodes[i]);
			}
			
			return inodes;
		}
		
		private void createInode(String chanKey, String inode, String value, String user) throws ClientProtocolException, IOException {
			String createUrl = this.baseUrl + "?f=create&u=0&c=" + chanKey + "&i=" + inode + "&value=" + value + "&u=" + user;
			getUrl(createUrl);
		}
		
		private String getUrl(String url) throws ClientProtocolException, IOException {
			HttpClient httpclient = new DefaultHttpClient();
		    HttpResponse response = httpclient.execute(new HttpGet(url));
		    StatusLine statusLine = response.getStatusLine();
		    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        response.getEntity().writeTo(out);
		        out.close();
		        String responseString = out.toString();
		        return responseString;
		    } else {
		        //Closes the connection.
		        response.getEntity().getContent().close();
		        throw new IOException(statusLine.getReasonPhrase());
		    }
		}
	}

	private void displayList(String[] labels) {
		DemoListView v = new DemoListView(mainContext, labels);
		setContentView(v);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public abstract class AListView extends ListView {

		Context context;
		String[] labels;

		public AListView(Context context, final String[] labels) {
			super(context);
			this.context = context;
			this.labels = labels;
			
			this.setAdapter(new ArrayAdapter<String>(context,
					R.layout.list_item, labels));

			this.setLayoutParams(FILL);
			this.setTextFilterEnabled(true);
			
			this.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					onClick(arg2, labels[arg2]);
				}
			});
		}
		
		protected abstract void onClick(int index, String label);
	}
	
	public class DemoListView extends AListView {

		public DemoListView(Context context, String[] labels) {
			super(context, labels);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onClick(int index, String label) {
			if (label == "Channel") {
				startChannel("555");
			} else {
			
				String html = "GOT {" + label + "}.";
				startWeb(html);
			}
//			DemoWebView v = new DemoWebView(context, html);
//			setContentView(v);
		}
	
	}
	
	public abstract class AWebView extends WebView {

		public AWebView(Context context, String html) {
			super(context);

			this.loadDataWithBaseURL("terse://terse",
					html, "text/html", "UTF-8", null);
			

			// this.setWebChromeClient(new WebChromeClient());
			this.getSettings().setBuiltInZoomControls(true);
			// this.getSettings().setJavaScriptEnabled(true);
			this.getSettings().setDefaultFontSize(18);
			this.getSettings().setNeedInitialFocus(true);
			this.getSettings().setSupportZoom(true);
			this.getSettings().setSaveFormData(true);
			

			this.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view,
						String url) {
					return onClickLink(url);
				}
			});
		}
		protected abstract boolean onClickLink(String url);
	}
	public class DemoWebView extends AWebView {

		public DemoWebView(Context context, String html) {
			super(context, html);
		}

		protected boolean onClickLink(String url) {
			URI uri = URI.create("" + url);
			String path = uri.getPath();
			String query = uri.getQuery();
			
			startMain(path, query);
			
			return true;
		}	
	}

	void startList(String[] labels) {
		String z = "";
		for (String s : labels) {
			z = z + labels + ";";
		}
		startMain("/list", null, "items", z);
	}
	void startWeb(String html) {
		startMain("/web", null, "html", html);
	}
	
	void startChannel(String chanKey) {
		startMain("/channel/" + chanKey, null);
	}

	void startMain(String actPath, String actQuery, String... extrasKV) {
		Uri uri = new Uri.Builder().scheme("terse").path(actPath)
				.encodedQuery(actQuery).build();
		Intent intent = new Intent("android.intent.action.MAIN", uri);
		intent.setClass(getApplicationContext(), MainActivity.class);
		for (int i = 0; i < extrasKV.length; i += 2) {
			intent.putExtra((String)extrasKV[i], extrasKV[i + 1]);
		}
		startActivity(intent);
	}

	LayoutParams FILL = new LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT);
}
