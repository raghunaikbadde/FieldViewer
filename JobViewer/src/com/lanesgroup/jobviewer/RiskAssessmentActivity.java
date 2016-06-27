package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.util.ActivityConstants;
import com.jobviewer.util.Utils;
import com.lanesgroup.jobviewer.fragment.QuestionsActivity;

public class RiskAssessmentActivity extends BaseActivity implements
        OnClickListener, android.widget.CompoundButton.OnCheckedChangeListener {

    private ProgressBar mProgress;
    private TextView mProgressStep, number_text;
    private CheckBox mRememberSelection;
    private RadioButton mExcavation, mNonExcavation;
    private RadioGroup riskAssessmentOption;
    private Button mSave, mNext;
    private View mRootView;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_risk_assessment_screen);
        initUI();
        updateData();
    }

    private void updateData() {
        CheckOutObject checkOutRemember = JobViewerDBHandler
                .getCheckOutRemember(this);
        if (!Utils.isNullOrEmpty(checkOutRemember.getIsPollutionSelected())) {
            mProgressStep.setText(getResources().getString(
                    R.string.progress_step_assessment_pollution));
            progress = (100 / 6) * 3;
        } else {
            mProgressStep.setText(getResources().getString(
                    R.string.progress_step_assessment));
            progress = (100 / 5) * 3;
        }
        mProgress.setProgress(progress);
        number_text.setText(checkOutRemember.getVistecId());

        mRememberSelection
                .setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        CheckOutObject checkOutRemember2 = JobViewerDBHandler
                                .getCheckOutRemember(buttonView.getContext());
                        if (isChecked) {
                            checkOutRemember2.setIsAssessmentRemember("true");
                        } else {
                            checkOutRemember2.setIsAssessmentRemember("");
                        }
                        JobViewerDBHandler.saveCheckOutRemember(
                                buttonView.getContext(), checkOutRemember2);
                    }
                });

    }

    private void initUI() {
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgressStep = (TextView) findViewById(R.id.progress_step_text);
        mRememberSelection = (CheckBox) findViewById(R.id.checkBox3);
        riskAssessmentOption = (RadioGroup) findViewById(R.id.riskAssessmentOption);
        mExcavation = (RadioButton) findViewById(R.id.checkBox2);
        mExcavation.setOnCheckedChangeListener(this);
        mNonExcavation = (RadioButton) findViewById(R.id.checkBox1);
        mNonExcavation.setOnCheckedChangeListener(this);
        number_text = (TextView) findViewById(R.id.number_text);
        mSave = (Button) findViewById(R.id.button1);
        mSave.setOnClickListener(this);
        mNext = (Button) findViewById(R.id.button2);
        // mNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mNext) {
            Intent intent = new Intent(RiskAssessmentActivity.this,
                    QuestionsActivity.class);
            int checkedRadioButtonId = riskAssessmentOption
                    .getCheckedRadioButtonId();
            CheckOutObject checkOutRemember = JobViewerDBHandler
                    .getCheckOutRemember(view.getContext());
            if (checkedRadioButtonId == mExcavation.getId()) {
                intent.putExtra(ActivityConstants.ASSESMENT_TYPE,
                        ActivityConstants.EXCAVATION);
                checkOutRemember
                        .setAssessmentSelected(ActivityConstants.EXCAVATION);

            } else {
                intent.putExtra(ActivityConstants.ASSESMENT_TYPE,
                        ActivityConstants.NON_EXCAVATION);
                checkOutRemember
                        .setAssessmentSelected(ActivityConstants.NON_EXCAVATION);
            }
            JobViewerDBHandler.saveCheckOutRemember(view.getContext(),
                    checkOutRemember);
            startActivity(intent);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mNext.setBackgroundResource(R.drawable.red_background);
        mNext.setOnClickListener(this);
    }

}