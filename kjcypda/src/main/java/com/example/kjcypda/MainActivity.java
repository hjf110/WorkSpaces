package com.example.kjcypda;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;
import com.google.zxing.util.QrCodeGenerator;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {


    private static final String TAG = MainActivity.class.getSimpleName();
    private WebView mWebView;
    private String TMP_URL = "http://115.238.154.91:8080/pad/pda/a_Pda_login.html";
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    private Uri imageUri;
    File file;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);



        setContentView(R.layout.activity_main);



        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#E8ECF0"));
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }


        mWebView = (WebView) findViewById(R.id.web);

        mWebView.loadUrl(TMP_URL);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        WebSettings settings = mWebView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        settings.setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        settings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        settings.setAllowUniversalAccessFromFileURLs(false);
        // 设置允许JS弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置与Js交互的权限
        settings.setJavaScriptEnabled(true);

        //启动XMLHttpRequest, Ajax
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
        }
        settings.setSupportZoom(true);
        // 通过addJavascriptInterface()将Java对象映射到JS对象
        //参数1：Javascript对象名
        //参数2：Java对象名
        mWebView.addJavascriptInterface(new AndroidtoJs(), "test");//AndroidtoJS类对象映射到js的test对象


        //检查权限
        checkAppPermission();
        mWebView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
                //返回true，说明你自己想根据url，做新的跳转，比如在判断url符合条件的情况下，我想让webView加载http://ask.csdn.net/questions/178242
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (request.getUrl().toString().contains("sina.cn")) {
                        view.loadUrl("http://ask.csdn.net/questions/178242");
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
        mWebView.setWebChromeClient(new WebChromeClient() {
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

        //android 7.0系统解决拍照的问题
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        builder.detectFileUriExposure();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果按下的是回退键且历史记录里确实还有页面
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * js调用android方法
     */
    class AndroidtoJs extends Object {

        // 定义JS需要调用的方法
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        public void showtw(String msg) {
//            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            startQrCode();
        }
    }


    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .CAMERA)) {
                Toast.makeText(this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_SHORT).show();
            }
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    /**
     * 生成二维码
     */
    private void generateQrCode() {
//        if (etContent.getText().toString().equals("")) {
//            Toast.makeText(this, "请输入二维码内容", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Bitmap bitmap = QrCodeGenerator.getQrCodeImage(etContent.getText().toString(), imgQrcode.getWidth(), imgQrcode.getHeight());
//        if (bitmap == null) {
//            Toast.makeText(this, "生成二维码出错", Toast.LENGTH_SHORT).show();
//            imgQrcode.setImageBitmap(null);
//        } else {
//            imgQrcode.setImageBitmap(bitmap);
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            final String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    // 注意调用的JS方法名要对应上
                    mWebView.loadUrl("javascript:sendMsg('" + scanResult + "')");

                }
            });

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
//            case Constant.REQ_PERM_EXTERNAL_STORAGE:
//                // 文件读写权限申请
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // 获得授权
//                    startQrCode();
//                } else {
//                    // 被禁止授权
//                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
//                }
//                break;
        }
    }


//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == FILECHOOSER_RESULTCODE) {
//            updatePhotos();
//            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
//            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
//            if (mUploadCallbackAboveL != null) {
//                onActivityResultAboveL(requestCode, resultCode, data);
//            } else if (mUploadMessage != null) {
//                Log.e("result", result + "");
//                if (result == null) {
////                        mUploadMessage.onReceiveValue(imageUri);
//                    mUploadMessage.onReceiveValue(imageUri);
//                    mUploadMessage = null;
//
//                    Log.e("imageUri", imageUri + "");
//                } else {
//                    mUploadMessage.onReceiveValue(result);
//                    mUploadMessage = null;
//                }
//
//
//            }
//        }
//    }


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
        Log.i(TAG, "" + Environment.getExternalStorageDirectory().getPath());
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
            Log.e("currentapiVersion", "currentapiVersion====>" + currentapiVersion);
            if (currentapiVersion < 24) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                //startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            } else {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
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
    private void checkAppPermission() {
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