package com.example.rostest;

import static rx.schedulers.Schedulers.start;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean isRecordDialogShow = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mdirection;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    String addr = "http://192.168.0.5:5000";
    Integer is_start ,is_end,flag;
    String current_result = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        if (getArguments() != null) {
            mdirection = getArguments().getString("direction");
//            TextView tv = (TextView)view.findViewById(R.id.textview);
//            tv.setText(mParam1);
            //控制方向运行
            new Thread() {
                @Override
                public void run() {
                    try {
                        Toast toast = null;//单例的toast
                        OkHttpClient client = new OkHttpClient();
                        FormBody formBody = new FormBody.Builder()
                                .add("direction", mdirection)
                                .build();
                        Request req = new Request.Builder()
                                .url(addr + ":5000/move")
                                .post(formBody)
                                .build();
                        // 使用execute会导致当前线程阻塞，不过反正已经开新线程了，无所谓
                        Response resp = client.newCall(req).execute();

                        if (!resp.isSuccessful()) {
                            Log.i("Failed", "请求失败");
                            // 一般会在这抛个异常
                        }

                        resp.body().close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }


        // 初始化控件
        Button up =  view.findViewById(R.id.up);
        Button left =  view.findViewById(R.id.left);
        Button right = view.findViewById(R.id.right);
        Button down = view.findViewById(R.id.down);
        Button voice = view.findViewById(R.id.voice);
        TextView tv = (TextView) view.findViewById(R.id.tv_coordinate);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        Button start_slam = view.findViewById(R.id.autoMap);
        Button stop_slam = view.findViewById(R.id.stop);
        flag =1;
        final int COMPLETED = 0;
        // 处理ui显示
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == COMPLETED) {
                    tv.setText(msg.getData().getString("text"));
                }
            }
        };

        /**
         * 定义若干个线程
         */
        class T1 extends Thread{
            @Override
            public void run(){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                Request request = new Request.Builder()
                        .url("http://172.16.1.102:5000/start_slam")
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        class T2 extends Thread{
            @Override
            public void run(){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                flag = 0;
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder().url(addr + ":5000/position").get().build();
                do{
                    try {
                        Response sponse = client.newCall(request).execute();
                        current_result = sponse.body().string();
                        String cord = tv.getText().toString();
                        String x, y, now_cord,is_slam_end;

                        if (current_result!=null)
                        {

                            JSONObject jsonObject = new JSONObject(current_result);
                            x = jsonObject.getString("x");
                            y = jsonObject.getString("y");
                            is_slam_end = jsonObject.getString("is_slam_end");
                            if(is_slam_end.equals("true")){
                                is_end = 1;
                            }
                            now_cord = "x：" + x + ", y：" + y + " \n";
                            cord = now_cord + cord;
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("text", cord);
                            msg.what = COMPLETED;
                            msg.setData(bundle);
                            handler.sendMessage(msg);


                        }
                        Thread.sleep(2500);

                    }catch (IOException | JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }while(is_end==0);
            }
        }



        //获得控件
        WebView webView = (WebView) view.findViewById(R.id.wv_webview);
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        //访问网页
        webView.loadUrl(addr+":8001/rvizweb/www/index.html");


        start_slam.setOnClickListener (new View.OnClickListener() {
            public void onClick (View v) {
                is_start = 1;
                is_end = 0;
                try {
                    new T1().start();
                    new T1().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    new T2().start();
                    new T2().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



                }

        });

        stop_slam.setOnClickListener (new View.OnClickListener() {
            public void onClick (View v) {
                String httpUrl=addr + ":5000/stop_slam";  // post请求
                String resultData ="";
                new Thread(){
                    @Override
                    public void run() {
//                        networkRequest(httpUrl);

                        OkHttpClient client = new OkHttpClient().newBuilder()
                                .build();
                        MediaType mediaType = MediaType.parse("text/plain");
                        Request request = new Request.Builder()
                                .url("http://172.16.1.102:5000/stop_slam")
                                .get()
                                .build();
                        try {
                            Response response = client.newCall(request).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });

        // 上下左右的函数
        //上
        up.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
                  new Thread() {
                      @Override
                      public void run() {
                          try {
                              Toast toast = null;//单例的toast
                              OkHttpClient client = new OkHttpClient();
                              FormBody formBody = new FormBody.Builder()
                                      .add("direction", "0")
                                      .build();
                              Request req = new Request.Builder()
                                      .url(addr + ":5000/move")
                                      .post(formBody)
                                      .build();
                              // 使用execute会导致当前线程阻塞，不过反正已经开新线程了，无所谓
                              Response resp = client.newCall(req).execute();

                              if (!resp.isSuccessful()) {
                                  Log.i("Failed", "请求失败");
                                  // 一般会在这抛个异常
                              }

                              resp.body().close();

                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                      }
                  }.start();
              }
          });

        //下
        down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Toast toast = null;//单例的toast
                            OkHttpClient client = new OkHttpClient();
                            FormBody formBody = new FormBody.Builder()
                                    .add("direction", "2")
                                    .build();
                            Request req = new Request.Builder()
                                    .url(addr+":5000/move")
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
        });

        //左
        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Toast toast = null;//单例的toast
                            OkHttpClient client = new OkHttpClient();
                            FormBody formBody = new FormBody.Builder()
                                    .add("direction", "1")
                                    .build();
                            Request req = new Request.Builder()
                                    .url(addr+":5000/move")
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
        });


        //右
        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Toast toast = null;//单例的toast
                            OkHttpClient client = new OkHttpClient();
                            FormBody formBody = new FormBody.Builder()
                                    .add("direction", "3")
                                    .build();
                            Request req = new Request.Builder()
                                    .url(addr+":5000/move")
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
        });

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecordDialogShow){
                    return;
                }
                RecordDialogFragment dialogFragment = RecordDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), "dialog");
                isRecordDialogShow = true;
                dialogFragment.setOnCancelListener(new RecordDialogFragment.OnCancelInterface() {
                    @Override
                    public void onCancel() {
                        isRecordDialogShow = false;
                    }
                });
            }
        });

        return view;
    }

    private void networkRequest(String httpurl) {
        HttpURLConnection connection=null;
        try {
            URL url = new URL(httpurl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            //设置请求方式 GET / POST 一定要大小
            connection.setRequestMethod("GET");
            connection.setDoInput(false);
            connection.setDoOutput(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code" + responseCode);
            }
            String result = getStringByStream(connection.getInputStream());
            if (result == null) {
                Log.d("Fail", "失败了");
            }else{
                Log.d("succuss", "成功了 ");
                Log.d("succuss", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStringByStream(InputStream inputStream){
        Reader reader;
        try {
            reader=new InputStreamReader(inputStream,"UTF-8");
            char[] rawBuffer=new char[512];
            StringBuffer buffer=new StringBuffer();
            int length;
            while ((length=reader.read(rawBuffer))!=-1){
                buffer.append(rawBuffer,0,length);
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MainFragment newInstance(String text) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("direction", text);
        fragment.setArguments(args);
        return fragment;
    }


}