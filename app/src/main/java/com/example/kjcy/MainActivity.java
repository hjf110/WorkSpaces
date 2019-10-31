package com.example.kjcy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final String host = "115.238.154.91:8080/pad/DDLogin.html";
    private final String urlAddress = "http://" + host;

    private static final String TAG = MainActivity.class.getSimpleName();
    private WebView web;
    private String TMP_URL = "http://221.2.169.102:50001/LBM";
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    private Uri imageUri;
    File file;





    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

        WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);





        web = (WebView) findViewById(R.id.web);
        WebSettings webSettings = web.getSettings();
       // settings.setJavaScriptEnabled(true);


        //支持js
        web.getSettings().setJavaScriptEnabled(true);
        // 解决图片不显示
        web.getSettings().setBlockNetworkImage(false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }



        //设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        //设置支持缩放
        webSettings.setBuiltInZoomControls(true);

        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.supportMultipleWindows();
        webSettings.setAllowContentAccess(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);

        web.loadUrl(urlAddress);

        web.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {
                //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
                //返回true，说明你自己想根据url，做新的跳转，比如在判断url符合条件的情况下，我想让webView加载http://ask.csdn.net/questions/178242
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (request.getUrl().toString().contains("sina.cn")){
                       // view.loadUrl("http://ask.csdn.net/questions/178242");
                        return true;
                    }
                }

                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            // onShowFileChooser详解 http://teachcourse.cn/2224.html
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                take();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                take();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                take();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                take();
            }
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            updatePhotos();
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                Log.e("result", result + "");
                if (result == null) {
//                        mUploadMessage.onReceiveValue(imageUri);
                    mUploadMessage.onReceiveValue(imageUri);
                    mUploadMessage = null;

                    Log.e("imageUri", imageUri + "");
                } else {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }


            }
        }
    }

    @SuppressWarnings("null")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE
                || mUploadCallbackAboveL == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{imageUri};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }

        return;
    }


    private void take() {
//        int hasCameraContactsPermission = ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
//        if (hasCameraContactsPermission != PackageManager.PERMISSION_GRANTED) {
//            String[] PERMISSIONS_STORAGE = {
//                    Manifest.permission.CAMERA};
//
//            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
//                    1);
//        }
//
//        int hasWriteContactsPermission = ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
//            String[] PERMISSIONS_STORAGE = {
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
//
//            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
//                    1);
//        }
        Log.i(TAG,""+Environment.getExternalStorageDirectory().getPath());
        // 指定拍照存储位置的方式调起相机
        String filePath = Environment.getExternalStorageDirectory() + File.separator
                + Environment.DIRECTORY_PICTURES + File.separator;
        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        file = new File(filePath + fileName);
        imageUri = Uri.fromFile(file);

        //调用照相机和浏览图片库代码
//        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//
//        Intent Photo = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        Intent chooserIntent = Intent.createChooser(Photo, "Image Chooser");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
//        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

        //调用系统相机，此代码在android 7.0以上有问题，需要在onCreate方法加入StrictMode.VmPolicy.Builder解决办法
//        Intent intentCamera = new Intent();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
//        }
//        intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//        //将拍照结果保存至photo_file的Uri中，不保留在相册中
//        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(intentCamera, FILECHOOSER_RESULTCODE);

        //Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);


        //兼容android 7.0+版本的照相机调用代码
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            /*获取当前系统的android版本号*/
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            Log.e("currentapiVersion","currentapiVersion====>"+currentapiVersion);
            if (currentapiVersion<24){
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            }else {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                //startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            }
        } else {
            Toast.makeText(this, "照相机不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent Photo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooserIntent = Intent.createChooser(Photo, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    private void updatePhotos() {
        // 该广播即使多发（即选取照片成功时也发送）也没有关系，只是唤醒系统刷新媒体文件
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageUri);
        sendBroadcast(intent);
    }

    //这里检查了存储权限，如果有需要，可以先检查下相机权限。  此方法用户对权限禁止或允许 未做判断，需要进一步完善
    private void checkAppPermission(){
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            final int REQUEST_EXTERNAL_STORAGE = 1;
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }







}
