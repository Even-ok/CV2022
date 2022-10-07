package com.example.rostest;

import android.Manifest;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.rostest.BaseDialogFragment;
import com.example.rostest.utils.DateUtils;
import com.example.rostest.utils.FileUtils;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import tech.oom.idealrecorder.IdealRecorder;
import tech.oom.idealrecorder.StatusListener;
import www.linwg.org.lib.LCardView;

/**
 * author: rivenlee
 * date: 2018/10/30
 * email: rivenlee0@gmail.com
 */
public class RecordDialogFragment extends BaseDialogFragment {

    TextView tvRecording;
    TextView tvRecordTime;
    TextView tvStartRecording ;
    ImageView imgVolic;
    TextView tvCardRecordStatus;
    TextView tvCardRecordDuration;
    TextView tvCardRecordDate;
    LCardView cardBroadcast;
    WaveView waveView;
    LinearLayout llSend;
    LinearLayout llCancel;
    ImageView imgCancel;
    ImageView imgSend;
    TextView tvCancel;
    TextView tvSend;

    public String biaozhun;
    public static int flag = 0;
    private IdealRecorder idealRecorder;
    private IdealRecorder.RecordConfig recordConfig;
    private String fileName;
    private Subscription subscribe;
    private long currentMilliseconds = 0;//当前录音的毫秒数
    private int seconds = 0;
    private final String[] result = new String[1];
    private AVLoadingIndicatorView avi;
    private  int takeCode = 1;
    private String rosAddr = "http://192.168.0.5";
    private String ownPC = "http://192.168.0.2";

    public static RecordDialogFragment newInstance() {
        return new RecordDialogFragment();
    }

    @Override
    protected int setView() {
        return R.layout.dialog_fragment_record;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        hideAvi(view);
        idealRecorder = IdealRecorder.getInstance();
        //Recorder的配置信息 采样率 采样位数
        recordConfig = new IdealRecorder.RecordConfig(MediaRecorder.AudioSource.MIC,
                IdealRecorder.RecordConfig.SAMPLE_RATE_44K_HZ, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        tvStartRecording = view.findViewById(R.id.tv_start_recording);
        checkPermission();
        tvStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == 0) {
                    readyRecord();
                    // 第一次单击触发的事件
                    flag = 1;
                } else {
                    stopRecord();
                    // 第二次单击buttont改变触发的事件
                    flag = 0;
                }
            }
        });

        //检查权限
        llCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onCancelInterface.onCancel();
                FileUtils.deleteFile(FileUtils.getFilePath());
                dismiss();
                Toast.makeText(mContext, "取消录音成功", Toast.LENGTH_LONG).show();
            }
        });
        llSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seconds < 1){
                    Toast.makeText(mContext, "录音时间不得少于1秒", Toast.LENGTH_LONG).show();
                    return;
                }
                // notifyServerStatus();
                File mp3File = new File("/sdcard/Music/",  "myRecode.mp3");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        result[0] = sendRecord(mp3File,view);
                    }
                }).start();
                Toast.makeText(mContext, "已上传，请稍等！", Toast.LENGTH_LONG).show();
                showAvi(view);
            }
        });
        //Toast.makeText(mContext,result[0], Toast.LENGTH_LONG).show();
    }

    private void initView(View view){

        tvRecording = view.findViewById(R.id.tv_recording);
        tvRecordTime = view.findViewById(R.id.tv_record_time);
        tvStartRecording = view.findViewById(R.id.tv_start_recording);
        imgVolic = view.findViewById(R.id.img_volic);
        tvCardRecordStatus = view.findViewById(R.id.tv_card_record_status);
        tvCardRecordDuration = view.findViewById(R.id.tv_card_record_duration);
        tvCardRecordDate = view.findViewById(R.id.tv_card_record_date);
        cardBroadcast = view.findViewById(R.id.card_broadcast);
        waveView = view.findViewById(R.id.wave_view);
        llSend = view.findViewById(R.id.ll_send);
        llCancel = view.findViewById(R.id.ll_cancel);
        imgSend = view.findViewById(R.id.img_send);
        imgCancel = view.findViewById(R.id.img_cancel);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvSend = view.findViewById(R.id.tv_send);
        avi= (AVLoadingIndicatorView) view.findViewById(R.id.avi);


    }

    private void checkPermission() {
        Acp.getInstance(mContext).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(List permissions) {
                        Toast.makeText(mContext, "请授权,否则无法录音", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
    }

    /**
     * 准备录音
     */
    private void readyRecord() {
//        FileUtils.deleteFile(FileUtils.getFilePath());
        setClickable(false);
        record();

    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        setClickable(true);
        idealRecorder.stop();
    }

    private void setClickable(boolean clickable){
        llCancel.setClickable(clickable);
        llSend.setClickable(clickable);
    }

    /**
     * 开始录音
     */
    private void record() {
        fileName = "myRecode" + ".mp3";
        //如果需要保存录音文件  设置好保存路径就会自动保存  也可以通过onRecordData 回调自己保存  不设置 不会保存录音
        idealRecorder.setRecordFilePath("/sdcard/Music/"+fileName);
        //设置录音配置 最长录音时长 以及音量回调的时间间隔
        idealRecorder.setRecordConfig(recordConfig).setMaxRecordTime(Integer.MAX_VALUE).setVolumeInterval(200);
        //设置录音时各种状态的监听
        idealRecorder.setStatusListener(statusListener);
        idealRecorder.start(); //开始录音
    }


    /**
     * 启用计时器功能
     */
    private void countDownTimer() {
        Observable<Long> observable = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread());
        subscribe = observable.subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long aLong) {
                currentMilliseconds += 1000;
                String hms = DateUtils.getFormatHMS(currentMilliseconds);
                tvRecordTime.setText(hms);
            }
        });
    }

    /**
     * 录音状态监听回调
     */
    private StatusListener statusListener = new StatusListener() {
        @Override
        public void onStartRecording() {
            countDownTimer();
            changeRed();
            tvStartRecording.setTextColor(getResources().getColor(R.color.gray_7b7b7b));
            tvRecording.setVisibility(View.GONE);
            cardBroadcast.setVisibility(View.GONE);
            waveView.setVisibility(View.VISIBLE);
            tvRecordTime.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRecordData(short[] data, int length) {
            //只能录音60s？？注意！！在这里修改传输的长度！而且，这儿怎么降采样了。。。
            for (int i = 0; i < length; i += 60) {
                waveView.addData(data[i]);
            }
        }

        @Override
        public void onVoiceVolume(int volume) {

        }

        @Override
        public void onRecordError(int code, String errorMsg) {
        }

        @Override
        public void onFileSaveFailed(String error) {
            Toast.makeText(getActivity(), "文件保存失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFileSaveSuccess(String fileUri) {
            Toast.makeText(getActivity(), "文件保存成功,路径是" + fileUri, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStopRecording() {
            seconds = (int) (currentMilliseconds /1000);
            currentMilliseconds = 0;

            tvStartRecording.setTextColor(getResources().getColor(R.color.white));
            //rx取消订阅关联
            if (!subscribe.isUnsubscribed()) {
                subscribe.unsubscribe();
            }
        }
    };
    /**
     *
     * 此处由服务器返回结果，结合网络请求返回结果实现。
     */
    public String sendRecord(File file,View view) {
        final String[] speechResult = new String[1];
        final String[] status = new String[1];
        final String[] type = new String[1];
        final String[] showText = new String[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Toast toast = null;//单例的toast
                    OkHttpClient client = new OkHttpClient();
                    MultipartBody body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("audio/mpeg"), file))
                            .build();
                    Request req = new Request.Builder()
                            .url(ownPC+":7000/speechProcess")
                            .post(body)
                            .build();
                    // 使用execute会导致当前线程阻塞，不过反正已经开新线程了，无所谓
                    Response resp = client.newCall(req).execute();
                    result[0] = resp.body().string();
                    Log.i("我是从可爱的服务器传回来的数据",result[0]);
                    //result[0] = resp.body().string();

                    if ( !resp.isSuccessful()){
                        Looper.prepare();
                        Toast.makeText(mContext,"录音有误，请重新录制", Toast.LENGTH_LONG).show();
                        Looper.loop();
                        //notifyServerFails("录音发送失败！");
                    }else {
                        Looper.prepare();
                        JSONObject jsonObject = new JSONObject(result[0]);
                        speechResult[0] = jsonObject.getString("result");
                        status[0] = jsonObject.getString("status");
                        type[0] = jsonObject.getString("type");
                        if (type[0].equals("3"))  // 显示文本，语音输出的类型！
                        {
                             showText[0] = speechResult[0];
                            Intent intent01=new Intent();
                            Bundle bundle=new Bundle();
                            bundle.putString("showText", showText[0]);
                            intent01.setClass(getActivity(), SpeechResultActivity.class);
                            intent01.putExtras(bundle);

                            startActivity(intent01);

                        }
                        else if(type[0].equals("1")) //打电话，speechResult[0]是电话号码
                        {
                            Intent intent = new Intent(

                            Intent.ACTION_CALL, Uri.parse("tel:"+speechResult[0]));
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();

                            startActivity(intent);
                        }

                        else if(type[0].equals("2")) //发短信，speechResult[0]是电话号码
                        {
                         String smsContent = "SOS!";
                         // note: SMS must be divided before being sent
                            SmsManager sms = SmsManager.getDefault();
                            List<String> texts = sms.divideMessage(smsContent);
                            for (String text : texts) {
                            sms.sendTextMessage(speechResult[0], null, text, null, null); }
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                          // note: not checked success or failure yet
                              Toast.makeText(
                               RecordDialogFragment.newInstance().getActivity(), "短信已发送", Toast.LENGTH_SHORT ).show();
                            }

                        else if(type[0].equals("4")) //打开摄像头
                        {
                            // 直接启动照相机，照相机照片将会存在默认的文件中
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            startActivity(intent);
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("5")){  //直走
                            controlMove("0");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("6")){  //后退
//                            MainFragment fragment2 = MainFragment.newInstance("2");
//                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                            transaction.add(android.R.id.content, fragment2);
//                            transaction.addToBackStack(null);
//                            transaction.commit();
                            controlMove("2");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("7")){  //向左
                            controlMove("1");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("8")){  //向右
                            controlMove("3");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else{
                            Toast.makeText(mContext, "录制音频有误，请重新录制！", Toast.LENGTH_LONG).show();
                        }

                        //Toast.makeText(mContext, "录音发送成功，文件路径->" + FileUtils.getFilePath(), Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return result[0];
    }


    /**
     * 创建取消dialog的接口
     */
    public interface OnCancelInterface {
        void onCancel();
    }

    private OnCancelInterface onCancelInterface;

    /**
     * 取消dialog的接口回调方法
     *
     * @param onCancelInterface
     */
    public void setOnCancelListener(OnCancelInterface onCancelInterface) {
        this.onCancelInterface = onCancelInterface;
    }

    private void changeRed(){
        llSend.setEnabled(true);
        imgCancel.setImageResource(R.mipmap.cancel_red);
        imgSend.setImageResource(R.mipmap.send_red);
        tvCancel.setTextColor(getResources().getColor(R.color.color_333333));
        tvSend.setTextColor(getResources().getColor(R.color.color_333333));
    }
    private void changeGray(){
        llSend.setEnabled(false);
        imgCancel.setImageResource(R.mipmap.cancle);
        imgSend.setImageResource(R.mipmap.send);
        tvCancel.setTextColor(getResources().getColor(R.color.gray_9b9b9b));
        tvSend.setTextColor(getResources().getColor(R.color.gray_9b9b9b));
    }

    /**
     * 广播发送成功回调
     */
    private void notifyServerSuccess() {
        FileUtils.deleteFile(FileUtils.getFilePath());
        changeGray();
        renderCardView("您的录音已经发送成功",R.mipmap.broadcast_success);
    }
    /**
     * 广播发送失败回调
     */
    private void notifyServerFails(String message) {
        changeGray();
        renderCardView("出现网络错误",R.mipmap.broadcast_fail);
    }

    /**
     * 渲染广播发送成功或失败后的显示效果
     * @param msg
     * @param imgRes
     */
    private void renderCardView(String msg, int imgRes) {
        cardBroadcast.setVisibility(View.VISIBLE);
        waveView.setVisibility(View.GONE);
        tvRecordTime.setVisibility(View.INVISIBLE);
        tvCardRecordStatus.setText(msg);
        tvCardRecordDate.setText(DateUtils.getDate());
        tvCardRecordDuration.setText("时长 " + DateUtils.secondToTime(seconds));
        imgVolic.setImageResource(imgRes);
    }

    public void hideAvi(View view) {
        avi.hide();
        // or avi.smoothToHide();
    }

    public void showAvi(View view) {
        avi.show();
        // or avi.smoothToShow();
    }
    public void controlMove(String dir){
        new Thread(){
            @Override
            public void run() {
                try {
                    Toast toast = null;//单例的toast
                    OkHttpClient client = new OkHttpClient();
                    FormBody formBody = new FormBody.Builder()
                            .add("direction", dir)
                            .build();
                    Request req = new Request.Builder()
                            .url(rosAddr+":5000"+"/move")
                            .post(formBody)
                            .build();
                    // 使用execute会导致当前线程阻塞，不过反正已经开新线程了，无所谓
                    Response resp = client.newCall(req).execute();
                    if (!resp.isSuccessful()) {
                        Log.i("Failed", "请求失败");
                        // 一般会在这抛个异常
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
