package cn.easyar.samples.helloarwebview;

import java.io.File;
import java.io.FileOutputStream;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * This class is an Activity that shows a webview.
 *
 * It can be used as an Activity or just as an WebView Manager. This is the case, for this sample.
 *
 * @author derzu
 */
public class WebViewActivity extends Activity {
	protected static final String TAG = "webview";
	private static WebView webView;
    private static boolean stopped = true;
    private View b1, b2;
	private EditText et;
	private String siteName = "teste";
	private static Bitmap bm;
	private static boolean firstTime = true;
    private static Canvas c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		webView = prepareWebView(this);

		b1 = findViewById(R.id.button1);
		b2 = findViewById(R.id.button2);
		et = (EditText) findViewById(R.id.editText1);

		b1.setOnClickListener(click);
		b2.setOnClickListener(click);
	}

	public static WebView prepareWebView(final Activity activity) {
        webView = (WebView) activity.findViewById(R.id.webView1);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                //Log.d("TAG", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
                return true;
            }
        });


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setPluginState(PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        webView.post(new Runnable() {
            @Override
            public void run() {
                isStoragePermissionGranted(activity);
            }
        });

        // if you wanna run the WebViewActivity.java, set to VISIBLE. And change the LAUNCHER Activity at the Manifest.
        webView.setVisibility(View.INVISIBLE);
        //webView.setVisibility(View.VISIBLE);

        return webView;
    }


	private static void saveImage(WebView webView, String siteName ){
        printScreen(webView);

		if (bm != null) {
			try {
				String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() +
                        File.separator + "Camera/"+siteName+".jpg";

                /*Log.i(TAG, "FILE NAME: " + fileName);
                Log.i(TAG, "FILE NAME: " + fileName);
                Log.i(TAG, "FILE NAME: " + fileName);*/

				File file = new File(fileName);
				FileOutputStream fOut = new FileOutputStream(file);

                if (bm.getHeight()>2000)
                    bm=Bitmap.createBitmap(bm, 0,0,bm.getWidth(), 2000);

				bm.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
				fOut.close();

                //Log.i(TAG, "Salvo!!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void setStopped() {
        stopped = true;
    }

    public static void stop() {
        setStopped();
        webView.stopLoading();
        webView.clearCache(true);
        webView.destroyDrawingCache();

        if (bm!=null)
            bm.eraseColor(Color.RED);

        //Log.i(TAG, "Loading Blank");
        loadUrl("about:blank");

        //webView.clearView();
        //webView.loadData("<HTML><BODY><H3>Test</H3></BODY></HTML>","text/html","utf-8");

    }

	/**
	 * Must be called by the same thread that init the webview.
	 *
	 * @param url
	 */
	public static void loadUrl(String url) {
        stopped = false;
        Log.i(TAG, "Loading NEW URL: " + url);
		webView.loadUrl(url);
	}


    /**
     * Printscreen the webview.
     *
     * @return the bitmap of the print.
     */
    public static Bitmap printScreen() {
	    if (stopped)
	        return null;

		Bitmap b = printScreen(webView);
		if (b!=null && b.getHeight()>1500)
			b=Bitmap.createBitmap(b, 0,0,b.getWidth(), 1500);

        if (stopped)
            return null;
        return b;
    }

    /**
     * Printscreen the webview.
     *
     * @param webView the webview.
     * @return the bitmap of the print.
     */
	private static Bitmap printScreen(WebView webView) {
		if (firstTime) {
			webView.measure(MeasureSpec.makeMeasureSpec(
					MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
			webView.buildDrawingCache();
			if (webView.getMeasuredWidth()>0 && webView.getMeasuredHeight()>0) {
				bm = Bitmap.createBitmap(webView.getMeasuredWidth(), webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
				firstTime = false;
			}
		}

		if (bm!=null) {
			c = new Canvas(bm);

			webView.draw(c);
		}

		return bm;
	}

    /**
     * This method is not being used because capturePicture is deprecated.
     * @param webView
     * @return
     */
	public static Bitmap printScreen3(WebView webView) {
		if (firstTime) {
			Picture picture = webView.capturePicture();
			if (picture.getWidth()>0 && picture.getHeight()>0) {
				bm = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
				firstTime = false;
			}
		}

		c = new Canvas(bm);

		webView.draw(c);

		return bm;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	    if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
	        Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
	        //resume tasks needing this permission
	    }
	}

	public static boolean isStoragePermissionGranted(Activity activity) {
	    if (Build.VERSION.SDK_INT >= 23) {
	        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
	            Log.i(TAG,"Permission is granted");
	            return true;
	        } else {
	            Log.i(TAG,"Permission is revoked");
	            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
	            return false;
	        }
	    }
	    else { //permission is automatically granted on sdk<23 upon installation
	        Log.v(TAG,"Permission is granted");
	        return true;
	    }
	}


	OnClickListener click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button1:


					Log.i(TAG, "Click butao 1");
					Log.i(TAG, "tv.getText(): " + et.getText());

					String url = et.getText().toString();
					webView.loadUrl(url);

					siteName = url.replaceAll("http://", "")
								  .replaceAll("https://", "")
								  .replaceAll("www.", "")
								  .replaceAll(".com", "")
								  .replaceAll(".br", "")
								  .replaceAll("/", "");

					break;
				case R.id.button2:
					Log.i(TAG, "Click butao 2");
					saveImage(webView, siteName);
					break;
			}
		}
	};

}
