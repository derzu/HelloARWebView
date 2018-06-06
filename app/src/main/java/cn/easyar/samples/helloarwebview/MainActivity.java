//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarwebview;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.webkit.WebView;

import java.util.HashMap;

import cn.easyar.Engine;


public class MainActivity extends AppCompatActivity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARWebView
    *      Package Name: cn.easyar.samples.helloarwebview
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "yveKyv7oEnSXXhhhbxJCR8jWabdpVokzKaJY7PHG4EtSnhsgOPIFlpKFvLjS7GWtvomK65jjxEKxI77GrGS65JQImm96YEjpr7e6mGtGHvtEsmwA4S1vRK5SJv6Q9vvnpbe8pLgJFWik0fK9Xj20H7E8YOVYVViBn2EAZZSaygYm1TGKmxCHKfCnOSPpTkIHIyw29xct";
    private GLView glView;
    private WebView webView;
    private int index = 0;
    private static final String TAG = "MainActivity";

    public static int PRINT_WEB  = 1;
    public static int STOP_WEB   = 2;
    public static int RELOAD_URL = 3;

    private static Handler hand = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == PRINT_WEB) {
                //Log.i(TAG, "SETTING WEB TEXTURE");
                WebViewRenderer.setTextureWeb(WebViewActivity.printScreen());
            }
            else if (msg.what == STOP_WEB) {
                WebViewActivity.stop();
            }
            else if (msg.what == RELOAD_URL) {
                WebViewActivity.loadUrl((String) msg.obj);
            }
        }
    };

    public static Handler getHandler() {
        return hand;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = WebViewActivity.prepareWebView(this);

        if (!Engine.initialize(this, key)) {
            Log.e(TAG, "Initialization Failed.");
        }

        glView = new GLView(this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onFailure() {
            }
        });

        // starts loading the default site.
        glView.post(new Runnable() {
            @Override
            public void run() {
                WebViewActivity.loadUrl("http://www.manairashopping.com.br");
            }
        });

        glView.postDelayed(new Runnable() {
            @Override
            public void run() {
                t.start();
            }
        }, 1000);
    }


    /**
     * Each 1 second does a printscreens from the webview.
     */
    Thread t = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getHandler().sendEmptyMessage(PRINT_WEB);
            }
        }
    };



    private interface PermissionCallback
    {
        void onSuccess();
        void onFailure();
    }
    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;
    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (glView != null) { glView.onResume(); }
    }

    @Override
    protected void onPause()
    {
        if (glView != null) { glView.onPause(); }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
