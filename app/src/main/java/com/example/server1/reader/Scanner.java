package com.example.server1.reader;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.entity.ISO15693Entity;
import com.rscja.deviceapi.exception.RFIDReadFailureException;
import com.rscja.utility.StringUtility;

import java.util.ArrayList;


public class Scanner extends Fragment {

    private MainActivity mContext;
    private Spinner spBlock;

    private RadioButton rReadWrite;

    private TextView tvResult;



    private ScrollView svResult;
    private LinearLayout llMultiple;
    private LinearLayout llSingle;

    private Button btnRead;
    private Button btnReadId;

    private int readSuccCount = 0;
    private int readFailCount = 0;

    private TextView tv_continuous_count;

    MediaPlayer read,fail;


    private ArrayAdapter adapterBlock;
    private ArrayList<String> arrBlock = new ArrayList<String>();



    private void statContinuous() {
        int total = readSuccCount + readFailCount;

        if (total % 1000 == 0) {
            tvResult.setText("");
        }

        tv_continuous_count.setText(String.valueOf(total));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = (View) inflater.inflate(R.layout.fragment_scanner, container, false);
         return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        btnRead = (Button)mContext.findViewById(R.id.btnRead);
        btnReadId = (Button)mContext.findViewById(R.id.btnReadId);
        tvResult = (TextView)mContext.findViewById(R.id.tvResult);
        svResult = (ScrollView)mContext.findViewById(R.id.svResult);
        llMultiple = (LinearLayout)mContext.findViewById(R.id.llMultiple);
        llSingle = (LinearLayout)mContext. findViewById(R.id.llSingle);
        spBlock = (Spinner)mContext.findViewById(R.id.spBlock);

        btnRead.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                readTag();

            }
        });

        btnReadId.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                scan();

            }
        });

        read = MediaPlayer.create(mContext, R.raw.beep);
        fail=MediaPlayer.create(mContext,R.raw.serror);
        initData();

    }

    public void readTag() {
        ISO15693Entity entity = null;

        try {
            entity = mContext.mRFID.read(spBlock.getSelectedItemPosition());

            if (entity == null) {
                tvResult.append(getString(R.string.rfid_mgs_error_not_found)
                        + "\n");
                fail.start();
                return;
            }

        } catch (RFIDReadFailureException e) {
            tvResult.append(getString(R.string.rfid_msg_read_fail) + "\n");

            return;
        }

        tvResult.setText("");
        tvResult.append(getString(R.string.rfid_msg_uid) + " " + entity.getId());
        if (entity.getType().length() > 0) {
            tvResult.append(getString(R.string.rfid_msg_type) + " "
                    + entity.getType());
        }

        tvResult.append(getString(R.string.rfid_msg_data) + " "
                + entity.getData());
        read.start();


    }

    private void scan() {
        ISO15693Entity entity = null;

        entity = mContext.mRFID.inventory();

        if (entity == null) {
            tvResult.append(getString(R.string.rfid_mgs_error_not_found));
            tvResult.append("\n============\n");
            scrollToBottom(svResult, tvResult);
            fail.start();
            return;
        }

        tvResult.append(getString(R.string.rfid_msg_uid) + " " + entity.getId());
        if (entity.getType().length() > 0) {
            tvResult.append(getString(R.string.rfid_msg_type) + " "
                    + entity.getType());
        }

        if (entity.getAFI().length() > 0) {
            tvResult.append("\nAFI:" + entity.getAFI());
        }

        if (entity.getDESFID().length() > 0) {
            tvResult.append("\nDESFID:" + entity.getDESFID());
        }

        tvResult.append("\n============\n");

        read.start();

        scrollToBottom(svResult, tvResult);
    }

    private void initData() {

        // adapterBlock
        arrBlock.clear();
        arrBlock.addAll(builNum(28));

        adapterBlock = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, arrBlock);
        adapterBlock
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBlock.setAdapter(adapterBlock);
    }

    public ArrayList<String> builNum(int count) {
        if (count < 1) {
            return null;

        }

        ArrayList<String> arrStr = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            arrStr.add(String.valueOf(i));

        }
        return arrStr;
    }

    public void scrollToBottom(final View scroll, final View inner) {

        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }

                scroll.scrollTo(0, offset);
            }
        });
    }

    public int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }


}