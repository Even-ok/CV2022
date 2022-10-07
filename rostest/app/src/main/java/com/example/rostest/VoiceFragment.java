package com.example.rostest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hanks.htextview.base.HTextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VoiceFragment extends BaseActivity  {

    private boolean isRecordDialogShow = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VoiceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VoiceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VoiceFragment newInstance(String param1, String param2) {
        VoiceFragment fragment = new VoiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        View view = inflater.inflate(R.layout.fragment_voice, container, false);
        TextView tv = (TextView) view.findViewById(R.id.startRecording);

//        textView2.animateText("Hello！请问您需要什么帮助？");
////        textView2.setOnClickListener(new ClickListener());
//        textView2.setAnimationListener(new SimpleAnimationListener(this.getActivity()));
//        Bundle myBundle = this.getActivity().getIntent().getExtras();
//        if (myBundle!=null) {
//            String myText = myBundle.getString("showText");
//            textView2.animateText(myText);
//        }

        tv.setOnClickListener(new View.OnClickListener() {
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
        // Inflate the layout for this fragment
        return view;
    }

}