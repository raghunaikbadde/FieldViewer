package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.util.Constants;
import com.lanesgroup.jobviewer.CaptureVistecActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.RiskAssessmentActivity;

public class WorkCompleteFragment extends Fragment implements OnClickListener{
	
	private ProgressBar mProgress;
	private TextView mProgressStep;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto;
	private Button mSave, mLeaveSite;
	private View mRootView;
	
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mRootView = inflater.inflate(R.layout.work_complete_screen, container,
					false); 
			//initUI();
			return mRootView;
		}
		private void initUI() {
			mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
			mProgressStep = (TextView) mRootView.findViewById(R.id.progress_step_text);
			mAddInfo = (ImageButton) mRootView.findViewById(R.id.detail_imageButton);
			mStop = (ImageButton) mRootView.findViewById(R.id.video_imageButton);
			mUser = (ImageButton) mRootView.findViewById(R.id.user_imageButton);
			mClickPhoto = (ImageButton) mRootView.findViewById(R.id.capture_imageButton);
			mClickPhoto.setOnClickListener(this);
			mSave = (Button) mRootView.findViewById(R.id.button1);
			mLeaveSite = (Button) mRootView.findViewById(R.id.button2);
			mLeaveSite.setOnClickListener(this);
		}
		@Override
		public void onClick(View view) {
			if(view == mSave){
				
			}else if(view == mLeaveSite){
				
			}else if(view == mClickPhoto){
				Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
				startActivityForResult(intent, Constants.RESULT_CODE);
			}
		}
		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
	    	if (requestCode == 500  && resultCode == getActivity().RESULT_OK){
	    		
	    	}
		}
}
