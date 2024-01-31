package com.esandinfo.esfaceid.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.esandinfo.esfaceid.EsFaceIDManager;
import com.esandinfo.esfaceid.EsOnVerifyCallback;
import com.esandinfo.esfaceid.bean.EsFaceIDConfig;
import com.esandinfo.esfaceid.bean.EsFaceIDProvider;
import com.esandinfo.esfaceid.bean.EsFaceIDResult;
import com.esandinfo.esfaceid.constants.EsFaceIDErrorCode;
import com.esandinfo.esfaceid.db.Feature;
import com.esandinfo.esfaceid.demo.util.AssetsUtil;
import com.esandinfo.esfaceid.demo.util.HTTPClient;
import com.esandinfo.esfaceid.utils.AppExecutors;
import com.esandinfo.esfaceid.utils.ImageUtils;
import com.esandinfo.esfaceid.utils.MyLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_home_initDrawable;
    Button btn_home_add_face;
    Button btn_home_verify;
    ScrollView scrollView;
    LinearLayout main_layout;
    // 显示执行结果
    protected TextView tvShowInfos = null;

    private boolean isCreate = false;
    //默认数据库名称
    public static final String DB_NAME = "DB1";
    public static final int request_code_check_face = 1;
    public static final int request_code_add_face = 2;
    EsFaceIDManager manager;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home);
        scrollView = findViewById(R.id.scrollView);
        main_layout = findViewById(R.id.main_layout);
        btn_home_initDrawable = findViewById(R.id.btn_home_initDrawable);
        btn_home_initDrawable.setOnClickListener(this);
        btn_home_add_face = findViewById(R.id.btn_home_add_face);
        btn_home_add_face.setOnClickListener(this);
        btn_home_verify = findViewById(R.id.btn_home_verify);
        btn_home_verify.setOnClickListener(this);
        tvShowInfos = (TextView) findViewById(R.id.tvShowInfos);
        manager = EsFaceIDManager.getInstance(MainActivity.this);
        // SDK初始化 (授权)
        initEsFaceIDManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 初始化算法引擎全局调用一次即可
     */
    public void initEsFaceIDManager(){
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                if (isCreate) {
                    showDiaLog("实例已创建");
                    return;
                }

                EsFaceIDResult esFaceIDResult = manager.checkCertificate();
                showDiaLog("检查是否有缓存证书："+esFaceIDResult.toString());
                String certificate="";
                if (esFaceIDResult.getErrorCode()!=EsFaceIDErrorCode.EF_SUCCESS) {
                    // 检查是否已经授权
                    EsFaceIDResult result = manager.checkCertificate();
                    if (result.getErrorCode() != EsFaceIDErrorCode.EF_SUCCESS) { // 尚未授权
                        HTTPClient httpClient = new HTTPClient();
                        String initMSG = manager.getCertificateInfo(this.getApplicationContext(), "ANDROID@RS@V20231010");
                        String rspMsg = httpClient.getCertificate(initMSG);
                        if (rspMsg == null) {
                            showDiaLog("获取授权文件失败");
                            return;
                        }

                        JSONObject biz = JSONObject.parseObject(rspMsg);
                        certificate = biz.getString("certificate");
                        if (certificate == null || certificate.length() == 0) {
                            showDiaLog("获取授权文件失败 :" + rspMsg);
                            return;
                        }
                    }
                }

                EsFaceIDConfig config = new EsFaceIDConfig();
                config.setThreshold(70);
                config.setProvider(EsFaceIDProvider.RS);
                config.setShowFaceRect(true);
                EsFaceIDResult result = manager.esCreate(this, certificate, config);
                if (result.getErrorCode() != EsFaceIDErrorCode.EF_SUCCESS) {
                    showDiaLog("引擎初始化失败" + result.getErrMsg());
                }
            } catch (Exception e) {
                showDiaLog("引擎初始化失败，发生异常：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_home_initDrawable:
                //批量添加资源文件中的pic_开头的照片数据  用于做测试数据
                AppExecutors.getInstance().diskIO().execute(() -> {
                    String[] assetsFileNameList = AssetsUtil.getFilesFromAssets(this.getApplicationContext(), "");
                    for (String fileName : assetsFileNameList) {
                        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                            //转换成map
                            Bitmap bitmap = AssetsUtil.getImageFromAssetsFile(this.getApplicationContext(), fileName);
                            long testGetFeature = System.currentTimeMillis();
                            //获取图片特征数据
                            EsFaceIDResult faceFeature = manager.getFaceFeature(bitmap);
                            testGetFeature = System.currentTimeMillis() - testGetFeature;
                            if (faceFeature.getErrorCode() == EsFaceIDErrorCode.EF_SUCCESS) {
                                //提取特征数据成功
                                Feature feature = new Feature();
                                //设置特征数据
                                feature.setFeatureData(faceFeature.getFeature().getFeatureData());
                                //设置算法
                                feature.setProvider(faceFeature.getFeature().getProvider());
                                //设置特征唯一id 应该跟用户绑定
                                feature.setPersionID(fileName);
                                //将特征数据添加到数据库
                                manager.addFeatureToDb(feature, DB_NAME);
                                showDiaLog(fileName +" 已经入库");
                            } else {
                                //提取特征数据失败
                                showDiaLog(fileName +" " + faceFeature.getErrMsg());
                            }
                        }
                    }
                    showDiaLog("批量添加完成");
                });
                break;
            case R.id.btn_home_add_face:
                //在这里跳转到手机系统相册里面 添加人脸
                Intent intent2 = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent2, request_code_add_face);
                break;
            case R.id.btn_home_verify:
                //开始人脸检测流程
                manager.startVerify(new EsOnVerifyCallback() {
                    @Override
                    public void onVerifyResult(EsFaceIDResult esFaceIDResult) {
                        Bitmap bitmap = null;
                        if (esFaceIDResult.getFeature() != null) {
                            Pattern pattern = Pattern.compile("^data:image/w+;base64,");
                            String image = esFaceIDResult.getFeature().getImage();
                            Matcher matcher = pattern.matcher(image);
                            image = matcher.replaceAll("").trim();
                            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
                            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        }

                        if (esFaceIDResult.getErrorCode() == EsFaceIDErrorCode.EF_SUCCESS) {
                            manager.addPromptBox(bitmap, "签到成功", esFaceIDResult.getFeature().getPersionID(), esFaceIDResult.getFeature().getPersionID());
                        }

                        if (esFaceIDResult.getErrorCode() == EsFaceIDErrorCode.EF_FACE_UNREGISTERED) {
                            //人脸未注册
                            manager.addPromptBox(bitmap, "人脸未注册", esFaceIDResult.getFeature().getPersionID(), esFaceIDResult.getFeature().getPersionID());
                        }
                    }

                    @Override
                    public void onMatchResult(EsFaceIDResult esFaceIDResult) {

                    }

                    @Override
                    public void onError(EsFaceIDResult esFaceIDResult) {
                        showDiaLog(esFaceIDResult.getErrMsg());
                    }
                }, DB_NAME);
                break;
            default: {
                break;
            }
        }
    }

    EsOnVerifyCallback callback = new EsOnVerifyCallback() {
        @Override
        public void onVerifyResult(EsFaceIDResult esFaceIDResult) {
            if (esFaceIDResult.getErrorCode() == EsFaceIDErrorCode.EF_SUCCESS){
                if (esFaceIDResult.getFeature() != null){
                    showDiaLog(">>>>>>>>>>认证结果:" + esFaceIDResult.getFeature().getPersionID());
                }
            }else if (esFaceIDResult.getErrorCode()!= EsFaceIDErrorCode.EF_FACE_RECOGNITION_END){
                showDiaLog(esFaceIDResult.getErrMsg());
            }
        }

        @Override
        public void onMatchResult(EsFaceIDResult esFaceIDResult) {
            MyLog.debug("onMatchResult:");
        }

        @Override
        public void onError(EsFaceIDResult esFaceIDResult) {
            showDiaLog(">>>>>>>>>>错误结果" + esFaceIDResult.getErrMsg());
        }
    };
    //从相册界面回来的回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //获取照片成功了
            if (data == null){
                return;
            }
            Uri selectedImage = data.getData();
            Bitmap userBitmap = null;
            userBitmap = ImageUtils.getBitmapFormUri(MainActivity.this, selectedImage);
            if (requestCode == request_code_check_face && userBitmap != null) {
                //人脸比对
                manager.recognitionFace(userBitmap, DB_NAME, callback);
            }

            if (requestCode == request_code_add_face && userBitmap != null) {
                //添加人脸
                EsFaceIDResult faceFeature = manager.getFaceFeature(userBitmap);
                if (faceFeature.getErrorCode() == EsFaceIDErrorCode.EF_SUCCESS) {
                    final EditText inputServer = new EditText(this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("输入名字").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                            .setNegativeButton("Cancel", null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //提取特征数据成功
                            showDiaLog("特征数据长度" + faceFeature.getFeature().getFeatureData().length);
                            Log.i("特征数据base64:", Base64.encodeToString(faceFeature.getFeature().getFeatureData(), Base64.DEFAULT));
                            Feature feature = new Feature();
                            feature.setFeatureData(faceFeature.getFeature().getFeatureData());
                            feature.setPersionID(inputServer.getText().toString());
                            feature.setProvider(faceFeature.getFeature().getProvider());
                            manager.addFeatureToDb(feature, DB_NAME);
                        }
                    });
                    builder.show();
                } else {
                    showDiaLog(faceFeature.getErrMsg());
                }
            }
        }
    }

    /**
     * 弹出提示框
     * @param msg
     */
    public void showDiaLog(String msg) {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                String s = tvShowInfos.getText().toString();
                tvShowInfos.setText(s + "\n" + msg);
                mHandler.post(ScrollRunnable);
            }
        });
    }

    /**
     * 滚屏的线程
     */
    private Runnable ScrollRunnable = new Runnable() {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            int off = main_layout.getMeasuredHeight() - scrollView.getHeight();// 判断高度
            if (off > 0) {
                scrollView.scrollBy(0, 50);
                if (scrollView.getScaleY() == off) {
                    Thread.currentThread().interrupt();
                } else {
                    mHandler.postDelayed(this, 1000);
                }
            }
        }
    };
}

