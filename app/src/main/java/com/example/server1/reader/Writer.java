package com.example.server1.reader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.deviceapi.entity.SimpleRFIDEntity;
import com.rscja.deviceapi.exception.RFIDNotFoundException;
import com.rscja.utility.StringUtility;


public class Writer extends Fragment {

    private MainActivity mContext;

    private LinearLayout llMultiple;
    private LinearLayout llSingle;

    private Button btnWrite;
    private EditText etWriteData;
    private Button btnWriteAFI;
    private Button btnWriteDSFID;
    private Button btnLockAFI;
    private Button btnLockDSFID;

    private EditText etAFI;
    private EditText etDSFID;

    private Button btnBack;

    private int writeSuccCount = 0;
    private int writeFailCount = 0;

    private boolean threadStop = true;

    private TextView tvResult;

    private Spinner spBlock;

    MediaPlayer read,fail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = (View) inflater.inflate(R.layout.fragment_writer, container, false);
        return  view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        llMultiple = (LinearLayout)mContext.findViewById(R.id.llMultiple);
        llSingle = (LinearLayout)mContext. findViewById(R.id.llSingle);
        btnWrite = (Button)mContext.findViewById(R.id.btnWrite);
        btnWriteAFI = (Button)mContext.findViewById(R.id.btnWriteAFI);
        btnWriteDSFID = (Button)mContext.findViewById(R.id.btnWriteDSFID);
        btnLockAFI = (Button)mContext.findViewById(R.id.btnLockAFI);
        btnLockDSFID = (Button)mContext.findViewById(R.id.btnLockDSFID);
        etWriteData = (EditText)mContext.findViewById(R.id.etWriteData);
        etAFI = (EditText)mContext.findViewById(R.id.etAFI);
        etDSFID = (EditText)mContext.findViewById(R.id.etDSFID);
        spBlock = (Spinner)mContext.findViewById(R.id.spBlock);
        tvResult = (TextView)mContext.findViewById(R.id.tvResultWrite );

        read = MediaPlayer.create(mContext, R.raw.beep);
        fail=MediaPlayer.create(mContext,R.raw.serror);

        btnWrite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                writeTag();

            }
        });
        btnWriteAFI.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                writeAFI();

            }
        });
        btnWriteDSFID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                writeDSFID();

            }
        });

        btnLockAFI.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                lockAFI();

            }
        });

        btnLockDSFID.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                lockDSFID();

            }
        });

    }


    public void writeAFI() {
        String afi = etAFI.getText().toString();

        if (afi.length() != 2) {
            tvResult.setText(R.string.rfid_msg_1byte_fail);
            return;
        } else if (!vailHexInput(afi)) {

            tvResult.setText(R.string.rfid_mgs_error_nohex);
            return;
        }

        boolean bResult = false;
        try {
            bResult = mContext.mRFID.writeAFI(Integer.parseInt(afi));
        } catch (NumberFormatException e) {
            tvResult.setText(R.string.rfid_msg_1byte_fail);
            return;
        } catch (RFIDNotFoundException e) {
            tvResult.setText(R.string.rfid_mgs_error_not_found);
            return;
        }

        if (bResult) {

            tvResult.setText(R.string.rfid_msg_write_succ);
            read.start();
        } else {
            tvResult.setText(R.string.rfid_msg_write_fail);
            fail.start();
        }
    }

    public void lockAFI() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.rfid_msg_confirm_title)
                .setMessage(R.string.rfid_msg_confirm_afi)
                .setPositiveButton(R.string.rfid_msg_confirm_true,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                boolean bResult = false;
                                try {
                                    bResult = mContext.mRFID.lockAFI();

                                } catch (NumberFormatException e) {
                                    tvResult.setText(R.string.rfid_msg_1byte_fail);
                                    return;
                                } catch (RFIDNotFoundException e) {
                                    tvResult.setText(R.string.rfid_mgs_error_not_found);
                                    return;
                                }

                                if (bResult) {

                                    tvResult.setText(R.string.rfid_msg_lock_succ);

                                } else {
                                    tvResult.setText(R.string.rfid_msg_lock_fail);
                                }

                            }
                        })
                .setNegativeButton(R.string.rfid_msg_confirm_flase,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }
                        }).show();
    }

    public void writeDSFID() {
        String dsfid = etDSFID.getText().toString();

        if (dsfid.length() != 2) {
            tvResult.setText(R.string.rfid_msg_1byte_fail);
            fail.start();
            return;
        } else if (!vailHexInput(dsfid)) {

            tvResult.setText(R.string.rfid_mgs_error_nohex);
            fail.start();
            return;
        }

        boolean bResult = false;
        try {
            bResult = mContext.mRFID.writeDSFID(Integer.parseInt(dsfid));
        } catch (NumberFormatException e) {
            tvResult.setText(R.string.rfid_msg_1byte_fail);
            fail.start();
            return;
        } catch (RFIDNotFoundException e) {
            tvResult.setText(R.string.rfid_mgs_error_not_found);
            fail.start();
            return;
        }

        if (bResult) {

            tvResult.setText(R.string.rfid_msg_write_succ);
            read.start();

        } else {
            tvResult.setText(R.string.rfid_msg_write_fail);
            fail.start();
        }
    }

    public void lockDSFID() {

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.rfid_msg_confirm_title)
                .setMessage(R.string.rfid_msg_confirm_dsfid)
                .setPositiveButton(R.string.rfid_msg_confirm_true,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                boolean bResult = false;
                                try {
                                    bResult = mContext.mRFID.lockDSFID();

                                } catch (NumberFormatException e) {
                                    tvResult.setText(R.string.rfid_msg_1byte_fail);
                                    return;
                                } catch (RFIDNotFoundException e) {
                                    tvResult.setText(R.string.rfid_mgs_error_not_found);

                                    return;
                                }

                                if (bResult) {

                                    tvResult.setText(R.string.rfid_msg_lock_succ);


                                } else {
                                    tvResult.setText(R.string.rfid_msg_lock_fail);


                                }

                            }
                        })
                .setNegativeButton(R.string.rfid_msg_confirm_flase,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }
                        }).show();

    }

    private void writeTag() {
        String strData = etWriteData.getText().toString();

        if (strData.length() == 0) {

            tvResult.setText(R.string.rfid_mgs_error_not_write_null);
            fail.start();
            return;
        } else if (!vailHexInput(strData)) {

            tvResult.setText(R.string.rfid_mgs_error_nohex);
            fail.start();
            return;
        }

        int block = spBlock.getSelectedItemPosition();

        boolean bResult = false;

        try {
            bResult = mContext.mRFID.write(block, strData);
        } catch (RFIDNotFoundException e) {
            tvResult.setText(R.string.rfid_mgs_error_not_found);
            fail.start();
            return;
        }

        if (bResult) {

            tvResult.setText(R.string.rfid_msg_write_succ);
            read.start();


        } else {
            tvResult.setText(R.string.rfid_msg_write_fail);
            fail.start();
        }

    }

    private void resetContinuous() {

        threadStop = true;
        tvResult.setText("");

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



}