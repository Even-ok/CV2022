package com.example.rostest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    TabLayout Tablayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        Tablayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        //Initial the list
        ArrayList<String> arrayList = new ArrayList<>();

        //add title in array list

        arrayList.add("Home");
        arrayList.add("Other");
        
        //Prepare view pager
        MainAdapter adapter = new MainAdapter(getSupportFragmentManager());

        MainFragment fragment = new MainFragment();

        adapter.addFragment(fragment, arrayList.get(0));
        VoiceFragment fragment_1 = new VoiceFragment();
        adapter.addFragment(fragment_1, arrayList.get(1));

        viewPager.setAdapter(adapter);

        //prepareViewPager(viewPager,arrayList);

        Tablayout.setupWithViewPager(viewPager);

        requestPermissions();
        SpeechUtility.createUtility(this.getApplicationContext(), SpeechConstant.APPID +"=5eade2d5");


    }


    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_SMS
                }, 0x0010);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void prepareViewPager(ViewPager viewPager, ArrayList<String> arrayList) {
        //Initialize min adapter
        MainAdapter adapter = new MainAdapter(getSupportFragmentManager());

        MainFragment fragment = new MainFragment();
        adapter.addFragment(fragment, arrayList.get(0));
//        MainFragment fragment_1 = new MainFragment();
//        adapter.addFragment(fragment_1, arrayList.get(1));

        viewPager.setAdapter(adapter);

    }

    private class MainAdapter extends FragmentPagerAdapter{

        //Initialize array list
        ArrayList<String> arrayList = new ArrayList<>();
        List<Fragment> fragmentList = new ArrayList<>();

        //create constructor
        public void addFragment(Fragment fragment,String title){
            //add title
            arrayList.add(title);
            fragmentList.add(fragment);
        }
        public MainAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return arrayList.get(position);
        }
    }

    private void changeTv(String current_result,TextView tv) {

        String cord = tv.getText().toString();
        String x,y,now_cord;

        try{
            JSONArray jsonArray = new JSONArray(current_result);
            for(int i = 0;i < jsonArray.length();i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                x = jsonObject.getString("x");
                y = jsonObject.getString("y");
                now_cord = "x："+x+", y："+y+" \n";
                cord = now_cord + cord;
                tv.setText(cord);

            }
        }catch (Exception e){e.printStackTrace();}
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

}