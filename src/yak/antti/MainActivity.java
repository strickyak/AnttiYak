package yak.antti;

import java.net.URI;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;

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
		} else if (verb.equals("web")) {
			displayWeb((String)extras.get("html"));
		} else {
			displayDefault();
		}
	}

	private void displayDefault() {
		String[] numbers = new String[] {"One", "Two", "Three"};
		DemoListView v = new DemoListView(mainContext, numbers);
		setContentView(v);
	}

	private void displayWeb(String html) {
		DemoWebView v = new DemoWebView(mainContext, html);
		setContentView(v);
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
			String html = "GOT {" + label + "}.";
			startWeb(html);
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
