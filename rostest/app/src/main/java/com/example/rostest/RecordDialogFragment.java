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
    private long currentMilliseconds = 0;//????????????????????????
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
        //Recorder??????????????? ????????? ????????????
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
                    // ??????????????????????????????
                    flag = 1;
                } else {
                    stopRecord();
                    // ???????????????buttont?????????????????????
                    flag = 0;
                }
            }
        });

        //????????????
        llCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onCancelInterface.onCancel();
                FileUtils.deleteFile(FileUtils.getFilePath());
                dismiss();
                Toast.makeText(mContext, "??????????????????", Toast.LENGTH_LONG).show();
            }
        });
        llSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seconds < 1){
                    Toast.makeText(mContext, "????????????????????????1???", Toast.LENGTH_LONG).show();
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
                Toast.makeText(mContext, "????????????????????????", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(mContext, "?????????,??????????????????", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
    }

    /**
     * ????????????
     */
    private void readyRecord() {
//        FileUtils.deleteFile(FileUtils.getFilePath());
        setClickable(false);
        record();

    }

    /**
     * ????????????
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
     * ????????????
     */
    private void record() {
        fileName = "myRecode" + ".mp3";
        //??????????????????????????????  ???????????????????????????????????????  ???????????????onRecordData ??????????????????  ????????? ??????????????????
        idealRecorder.setRecordFilePath("/sdcard/Music/"+fileName);
        //?????????????????? ?????????????????? ?????????????????????????????????
        idealRecorder.setRecordConfig(recordConfig).setMaxRecordTime(Integer.MAX_VALUE).setVolumeInterval(200);
        //????????????????????????????????????
        idealRecorder.setStatusListener(statusListener);
        idealRecorder.start(); //????????????
    }


    /**
     * ?????????????????????
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
     * ????????????????????????
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
            //????????????60s?????????????????????????????????????????????????????????????????????????????????????????????
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
            Toast.makeText(getActivity(), "??????????????????", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFileSaveSuccess(String fileUri) {
            Toast.makeText(getActivity(), "??????????????????,?????????" + fileUri, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStopRecording() {
            seconds = (int) (currentMilliseconds /1000);
            currentMilliseconds = 0;

            tvStartRecording.setTextColor(getResources().getColor(R.color.white));
            //rx??????????????????
            if (!subscribe.isUnsubscribed()) {
                subscribe.unsubscribe();
            }
        }
    };
    /**
     *
     * ????????????????????????????????????????????????????????????????????????
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
                    Toast toast = null;//?????????toast
                    OkHttpClient client = new OkHttpClient();
                    MultipartBody body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("audio/mpeg"), file))
                            .build();
                    Request req = new Request.Builder()
                            .url(ownPC+":7000/speechProcess")
                            .post(body)
                            .build();
                    // ??????execute???????????????????????????????????????????????????????????????????????????
                    Response resp = client.newCall(req).execute();
                    result[0] = resp.body().string();
                    Log.i("?????????????????????????????????????????????",result[0]);
                    //result[0] = resp.body().string();

                    if ( !resp.isSuccessful()){
                        Looper.prepare();
                        Toast.makeText(mContext,"??????????????????????????????", Toast.LENGTH_LONG).show();
                        Looper.loop();
                        //notifyServerFails("?????????????????????");
                    }else {
                        Looper.prepare();
                        JSONObject jsonObject = new JSONObject(result[0]);
                        speechResult[0] = jsonObject.getString("result");
                        status[0] = jsonObject.getString("status");
                        type[0] = jsonObject.getString("type");
                        if (type[0].equals("3"))  // ???????????????????????????????????????
                        {
                             showText[0] = speechResult[0];
                            Intent intent01=new Intent();
                            Bundle bundle=new Bundle();
                            bundle.putString("showText", showText[0]);
                            intent01.setClass(getActivity(), SpeechResultActivity.class);
                            intent01.putExtras(bundle);

                            startActivity(intent01);

                        }
                        else if(type[0].equals("1")) //????????????speechResult[0]???????????????
                        {
                            Intent intent = new Intent(

                            Intent.ACTION_CALL, Uri.parse("tel:"+speechResult[0]));
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();

                            startActivity(intent);
                        }

                        else if(type[0].equals("2")) //????????????speechResult[0]???????????????
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
                               RecordDialogFragment.newInstance().getActivity(), "???????????????", Toast.LENGTH_SHORT ).show();
                            }

                        else if(type[0].equals("4")) //???????????????
                        {
                            // ?????????????????????????????????????????????????????????????????????
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            startActivity(intent);
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("5")){  //??????
                            controlMove("0");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("6")){  //??????
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
                        else if(type[0].equals("7")){  //??????
                            controlMove("1");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else if(type[0].equals("8")){  //??????
                            controlMove("3");
                            onCancelInterface.onCancel();
                            FileUtils.deleteFile(FileUtils.getFilePath());
                            dismiss();
                        }
                        else{
                            Toast.makeText(mContext, "???????????????????????????????????????", Toast.LENGTH_LONG).show();
                        }

                        //Toast.makeText(mContext, "?????????????????????????????????->" + FileUtils.getFilePath(), Toast.LENGTH_LONG).show();
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
     * ????????????dialog?????????
     */
    public interface OnCancelInterface {
        void onCancel();
    }

    private OnCancelInterface onCancelInterface;

    /**
     * ??????dialog?????????????????????
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
     * ????????????????????????
     */
    private void notifyServerSuccess() {
        FileUtils.deleteFile(FileUtils.getFilePath());
        changeGray();
        renderCardView("??????????????????????????????",R.mipmap.broadcast_success);
    }
    /**
     * ????????????????????????
     */
    private void notifyServerFails(String message) {
        changeGray();
        renderCardView("??????????????????",R.mipmap.broadcast_fail);
    }

    /**
     * ???????????????????????????????????????????????????
     * @param msg
     * @param imgRes
     */
    private void renderCardView(String msg, int imgRes) {
        cardBroadcast.setVisibility(View.VISIBLE);
        waveView.setVisibility(View.GONE);
        tvRecordTime.setVisibility(View.INVISIBLE);
        tvCardRecordStatus.setText(msg);
        tvCardRecordDate.setText(DateUtils.getDate());
        tvCardRecordDuration.setText("?????? " + DateUtils.secondToTime(seconds));
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
                    Toast toast = null;//?????????toast
                    OkHttpClient client = new OkHttpClient();
                    FormBody formBody = new FormBody.Builder()
                            .add("direction", dir)
                            .build();
                    Request req = new Request.Builder()
                            .url(rosAddr+":5000"+"/move")
                            .post(formBody)
                            .build();
                    // ??????execute???????????????????????????????????????????????????????????????????????????
                    Response resp = client.newCall(req).execute();
                    if (!resp.isSuccessful()) {
                        Log.i("Failed", "????????????");
                        // ???????????????????????????
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
