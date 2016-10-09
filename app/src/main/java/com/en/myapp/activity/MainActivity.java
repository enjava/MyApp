package com.en.myapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.en.myapp.R;
import com.en.myapp.utils.BaiduPushUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;

;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private String downloadUrl="";
    private TextView tvVerName;
    private TextView tvVerCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        // 请将AndroidManifest.xml 139行 api_key 字段值修改为自己的 api_key 方可使用 ！！
        // ATTENTION：You need to modify the value of api_key to your own at row 139 in AndroidManifest.xml to use this Demo !!
        // 启动百度push
        PushManager.startWork(this, PushConstants.LOGIN_TYPE_API_KEY, BaiduPushUtils.getMetaValue(this, "api_key"));

        // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
        // PushManager.enableLbs(getApplicationContext());

        // Push: 设置自定义的通知样式，具体API介绍见用户手册，如果想使用系统默认的可以不加这段代码
        // 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
        // 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应

        // 推送高级设置，通知栏样式设置为下面的ID
        // PushManager.setNotificationBuilder(this, 1, cBuilder);
        tvVerName = (TextView) findViewById(R.id.tv_name);
        tvVerCode = (TextView) findViewById(R.id.tv_vercode);

        initData();
    }

    private void initData() {
        tvVerName.setText("版本名:"+getVerName(this));
        tvVerCode.setText("版本号:"+getVerCode(this));

    }



    public  int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verCode;
    }
    public  String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return verName;
    }
    /**
     * 下载APK
     */
    private void downLoadApk() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //获取sd卡路径
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "updateMyApp.apk";

            //发送请求，获取apk,并放置到指定的路径
            HttpUtils httpUtils = new HttpUtils();

            httpUtils.download(downloadUrl, path, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    Log.i(TAG, "下载成功");
                    File file = responseInfo.result;
                    // 提示用户安装k
                   installApk(file);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Log.i(TAG, "下载失败");
                }



                @Override
                public void onStart() {
                    super.onStart();
                    Log.i(TAG, "刚刚开始下载");
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);

                    Log.i(TAG, "下载中........");
                    Log.i(TAG, "total = " + total);
                    Log.i(TAG, "current = " + current);
                }
            });
        }
    }

//    private void installApk(File file) {
//        // 系统应用界面,源码,安装apk入口
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
//        /*
//         * //文件作为数据源 intent.setData(Uri.fromFile(file)); //设置安装的类型
//		 * intent.setType("application/vnd.android.package-archive");
//		 */
//        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        // startActivity(intent);
//        startActivityForResult(intent, 0);
//    }

    private void installApk(File file) {
        File apkfile = file;
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
