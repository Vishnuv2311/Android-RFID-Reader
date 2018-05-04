package com.example.server1.reader;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.rscja.deviceapi.entity.ISO15693Entity;
import com.rscja.deviceapi.entity.SimpleRFIDEntity;
import com.rscja.deviceapi.exception.RFIDNotFoundException;
import com.rscja.deviceapi.exception.RFIDReadFailureException;
import com.rscja.utility.StringUtility;



public class Auto extends Fragment {

    private MainActivity mContext;

    private RadioButton rRead;
    private RadioButton rWrite;
    private EditText et_between;
    private EditText etWriteData;
    private TableRow writebox;

    private Thread writeThread;

    private Spinner spBlock;

    private Button btnStart;
    private Button btnClear;

    private Thread readThread;

    private int writeSuccCount = 0;
    private int writeFailCount = 0;

    private int readSuccCount = 0;
    private int readFailCount = 0;

    private TextView tv_read_fail_count;
    private TextView tv_read_succ_count;
    private TextView tv_write_succ_count;
    private TextView tv_write_fail_count;
    private TextView tv_continuous_count;

    private TextView tvResult;

    private boolean threadStop = true;
    private boolean isContinuous = true;

    private ScrollView svResult;

    private RadioButton rReadWrite;

    TableRow intervalbox;

    MediaPlayer read,fail;

    private Handler writeHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg != null && !threadStop) {

                if ((writeSuccCount + writeFailCount) % 500 == 0) {
                    tvResult.setText("");
                }

                switch (msg.arg1) {
                    case 1: {
                        writeSuccCount++;

                        tvResult.append(getString(R.string.rfid_msg_write_succ));
                        tvResult.append("\n==============================\n");
                        read.start();
                    }
                    break;
                    case 0: {
                        writeFailCount++;
                        tvResult.append(msg.obj.toString());
                        tvResult.append("\n==============================\n");
                        fail.start();
                    }
                    break;
                }

                statContinuous();

                scrollToBottom(svResult, tvResult);

            }

        }
    };

    private Handler readHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg != null && !threadStop) {

                if ((readSuccCount + readFailCount) % 500 == 0) {
                    tvResult.setText("");
                }

                switch (msg.arg1) {
                    case 1: {
                        readSuccCount++;
                        final ISO15693Entity entity = (ISO15693Entity) msg.obj;

                        tvResult.append(getString(R.string.rfid_msg_uid) + " "
                                + entity.getId());


                        if (entity.getType().length() > 0) {
                            tvResult.append(getString(R.string.rfid_msg_type) + " "
                                    + entity.getType());

                        }

                        tvResult.append(getString(R.string.rfid_msg_data) + " "
                                + entity.getData());

                        tvResult.append("\n==============================\n");
                        read.start();
                    }
                    break;
                    case 0: {
                        readFailCount++;
                        tvResult.append(msg.obj.toString());
                        tvResult.append("\n==============================\n");
                        fail.start();
                    }
                    break;
                }

                statContinuous();
                scrollToBottom(svResult, tvResult);
            }

        }

    };

    class ReadRunnable implements Runnable {
        private boolean isContinuous = false;
        private long sleepTime = 1000;
        private int block;
        Message msg = null;

        public ReadRunnable(boolean isContinuous, int sleep, int block) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
            this.block = block;
        }

        @Override
        public void run() {
            ISO15693Entity entity = null;

            do {

                msg = new Message();

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    entity = mContext.mRFID.read(block);

                    if (entity == null) {
                        msg.arg1 = 0;
                        msg.obj = getText(R.string.rfid_mgs_error_not_found);

                        readHandler.sendMessage(msg);
                        continue;
                    } else {
                        msg.arg1 = 1;
                        msg.obj = entity;

                        readHandler.sendMessage(msg);
                    }

                } catch (RFIDReadFailureException e) {
                    msg.arg1 = 0;
                    msg.obj = getText(R.string.rfid_msg_read_fail);

                    readHandler.sendMessage(msg);
                    continue;
                }

            } while (isContinuous && !threadStop);
        }

    }

    class WriteRunnable implements Runnable {

        private boolean isContinuous = false;
        private long sleepTime = 1000;
        private int block;
        String strData;
        Message msg = null;

        public WriteRunnable(boolean isContinuous, int sleep, int block,
                             String data) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
            this.block = block;
            this.strData = data;
        }

        @Override
        public void run() {

            SimpleRFIDEntity entity = null;

            do {
                msg = new Message();
                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                boolean bResult = false;

                try {
                    bResult = mContext.mRFID.write(block, strData);
                } catch (RFIDNotFoundException e) {
                    msg.arg1 = 0;
                    msg.obj = getText(R.string.rfid_mgs_error_not_found);

                    writeHandler.sendMessage(msg);
                    continue;
                }

                if (bResult) {
                    msg.arg1 = 1;
                    msg.obj = getText(R.string.rfid_msg_write_succ);

                    writeHandler.sendMessage(msg);
                    continue;
                } else {
                    msg.arg1 = 0;
                    msg.obj = getText(R.string.rfid_msg_write_fail);

                    writeHandler.sendMessage(msg);
                    continue;
                }

            } while (isContinuous && !threadStop);

        }

    }

    private void statContinuous() {
        int total = readSuccCount + readFailCount + writeSuccCount
                + writeFailCount;

        if (total % 1000 == 0) {
            tvResult.setText("");
        }

        tv_continuous_count.setText(String.valueOf(total));
        tv_read_succ_count.setText(String.valueOf(readSuccCount));
        tv_read_fail_count.setText(String.valueOf(readFailCount));
        tv_write_succ_count.setText(String.valueOf(writeSuccCount));
        tv_write_fail_count.setText(String.valueOf(writeFailCount));
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_auto, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = (MainActivity) getActivity();

        rRead = (RadioButton)mContext.findViewById(R.id.rRead);
        rWrite = (RadioButton)mContext.findViewById(R.id.rWrite);
        et_between = (EditText)mContext.findViewById(R.id.et_between);
        btnStart = (Button)mContext.findViewById(R.id.btnStart);
        btnClear = (Button)mContext.findViewById(R.id.btnClear);
        tv_read_fail_count = (TextView)mContext. findViewById(R.id.tv_read_fail_count);
        tv_write_succ_count = (TextView)mContext.findViewById(R.id.tv_write_succ_count);
        tv_write_fail_count = (TextView)mContext.findViewById(R.id.tv_write_fail_count);
        tv_read_succ_count = (TextView)mContext. findViewById(R.id.tv_read_succ_count);
        tv_continuous_count = (TextView)mContext.findViewById(R.id.tv_continuous_count);
       tvResult = (TextView)mContext.findViewById(R.id.tvResultAuto);
        spBlock = (Spinner)mContext.findViewById(R.id.spBlock);
        svResult = (ScrollView) mContext.findViewById(R.id.svResultauto);
        rReadWrite = (RadioButton)mContext.findViewById(R.id.rReadWrite);
        etWriteData = (EditText)mContext.findViewById(R.id.etWriteDataa);
        writebox=(TableRow)mContext.findViewById(R.id.writeboxs);
        intervalbox=(TableRow)mContext.findViewById(R.id.intervallayout);

        writeSuccCount = 0;
        writeFailCount = 0;

        readSuccCount = 0;
        readFailCount = 0;

        read = MediaPlayer.create(mContext, R.raw.beep);
        fail=MediaPlayer.create(mContext,R.raw.serror);


        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                btnClear.setEnabled(!threadStop);

                if (threadStop) {
                    writebox.setVisibility(View.GONE);
                    intervalbox.setVisibility(View.GONE);

                    threadStop = false;

                    btnStart.setText(R.string.title_stop);

                    int sleep = 0;

                    String strBetween = et_between.getText().toString().trim();
                    if (StringUtility.isEmpty(strBetween)) {

                    } else {
                        sleep =toInt(strBetween,0);
                    }

                    int block = Integer.parseInt(spBlock.getSelectedItem()
                            .toString());


                    if (rRead.isChecked()) {
                        readThread = new Thread(new ReadRunnable(isContinuous,
                                sleep, block));

                        readThread.start();

                    } else {

                        String strData = etWriteData.getText().toString();

                        if (strData.length() == 0) {

                            tvResult.setText(R.string.rfid_mgs_error_not_write_null);
                            return;
                        } else if (!vailHexInput(strData)) {

                            tvResult.setText(R.string.rfid_mgs_error_nohex);
                            return;
                        }

                        if (rWrite.isChecked()) {

                            writeThread = new Thread(new WriteRunnable(
                                    isContinuous, sleep, block, strData));

                            writeThread.start();

                        } else if (rReadWrite.isChecked()) {
                            readThread = new Thread(new ReadRunnable(
                                    isContinuous, sleep, block));

                            writeThread = new Thread(new WriteRunnable(
                                    isContinuous, sleep, block, strData));

                            readThread.start();
                            writeThread.start();

                        }
                    }

                } else {
                    writebox.setVisibility(View.VISIBLE);
                    intervalbox.setVisibility(View.VISIBLE);
                    threadStop = true;
                    btnStart.setText(R.string.title_start);
                }

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                resetContinuous();

            }
        });

    }
    private void resetContinuous() {
        readSuccCount = 0;
        readFailCount = 0;

        threadStop = true;
        tvResult.setText("");

        tv_continuous_count.setText("0");
        tv_read_fail_count.setText("0");
        tv_read_succ_count.setText("0");

    }

    public int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    public boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }


        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
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

}
