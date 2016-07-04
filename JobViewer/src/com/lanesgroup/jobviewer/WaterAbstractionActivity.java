package com.lanesgroup.jobviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jobviewer.util.Constants;

/**
 * Created by system-local on 29-06-2016.
 */
public class WaterAbstractionActivity extends BaseActivity {

    private Context mContext;
    private RadioButton radio_yes;
    private RadioButton radio_no;
    private Button mNext;
    private Button mCancel;
    private RadioGroup radioGroup1;
    private String selectedFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.water_abstraction_layout);
        mContext = this;
        initView();
    }

    private void initView() {
        radio_yes = (RadioButton) findViewById(R.id.radio_yes);
        radio_no = (RadioButton) findViewById(R.id.radio_no);
        mNext = (Button) findViewById(R.id.button2);
        mCancel = (Button) findViewById(R.id.button1);
        radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
        setClickListener();
        enableDisableNextButton(false);
    }

    private void setClickListener() {
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes) {
                    selectedFlag = Constants.YES_CONSTANT;
                } else {
                    selectedFlag = Constants.NO_CONSTANT;
                }
                enableDisableNextButton(true);
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (selectedFlag.equals(Constants.YES_CONSTANT)) {
                Intent mCautionDialog = new Intent(mContext, CautionDialog.class);
                mCautionDialog.putExtra(Constants.KEY_ABSTRACTION_VALUE, selectedFlag);

                startActivity(mCautionDialog);
//                } else {
//                    finish();
//                }

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void enableDisableNextButton(boolean isEnable) {
        if (isEnable) {
            mNext.setEnabled(true);
            mNext.setBackgroundResource(R.drawable.red_background);
        } else {
            mNext.setEnabled(false);
            mNext.setBackgroundResource(R.drawable.dark_grey_background);
        }
    }
}
