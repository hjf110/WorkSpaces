package com.example.kjcy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;

public class MyWebChromeClientUtil extends WebChromeClient {
    public static ValueCallback<Uri> uriValueCallback;
    public static ValueCallback<Uri[]> valueCallbacks;
    private Activity activity;

    public static final int FILECHOOSER_RESULTCODE = 1314;

    public static Uri myImageUri=null;

    @SuppressWarnings("deprecation")
    public MyWebChromeClientUtil(Activity activity) {
        this.activity = activity;
    }

    //5.0+
    @Override
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        // TODO 自动生成的方法存根
        valueCallbacks = filePathCallback;
        this.activity.startActivityForResult(createCameraIntent(),
                this.FILECHOOSER_RESULTCODE);
        return true;
    }

    //4.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType, String capture) {
        uriValueCallback = uploadMsg;
        this.activity.startActivityForResult(createCameraIntent(),
                this.FILECHOOSER_RESULTCODE);

    }

    // 3.0 +
    @SuppressWarnings("static-access")
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {

        uriValueCallback = uploadMsg;
        this.activity.startActivityForResult(createCameraIntent(),
                this.FILECHOOSER_RESULTCODE);
    }


    // Android < 3.0
    @SuppressWarnings("static-access")
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        uriValueCallback = uploadMsg;
        this.activity.startActivityForResult(createCameraIntent(),
                this.FILECHOOSER_RESULTCODE);
    }

    /**
     * 跳转选择界面
     * @return
     */
    private Intent createDefaultOpenableIntent() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        return i;

    }

    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "选择图片");
        return chooser;
    }

    /**
     * 调用系统相机拍照
     * @return
     */
    @SuppressWarnings("static-access")
    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.myImageUri=getFileUri();
        cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,myImageUri);
        return cameraIntent;
    }

    private String getFilePath(){//图片 存储路径
        File externalDataDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath()
                + File.separator + "UPUPUP");
        cameraDataDir.mkdirs();//路径:DCIM/UPUPUP
        String mCameraFilePath = cameraDataDir.getAbsolutePath()
                + File.separator + "send_new_image.jpg";

        return mCameraFilePath;
    }
    //Uri获取 支持Android7.0
    private Uri getFileUri(){
        Uri imageUri = null;
        String path=getFilePath();
        File file = new File(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//Android版本>=7.0
            try {
                imageUri = FileProvider.getUriForFile(this.activity,
                        "com.zhs.fileprovider", file);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            imageUri = Uri.fromFile(file);
        }
        return imageUri;
    }

    public static void update(Uri[] uris) {//上传

        if ( valueCallbacks != null
                && uris[0] != null) {
            valueCallbacks.onReceiveValue(uris);
            valueCallbacks = null;
        }

        if(uriValueCallback != null
                && uris[0] != null){
            uriValueCallback.onReceiveValue(uris[0]);
            uriValueCallback = null;
        }
    }
}
