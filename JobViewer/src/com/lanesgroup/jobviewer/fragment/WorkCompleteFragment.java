package com.lanesgroup.jobviewer.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jobviewer.survey.object.Screen;
import com.jobviewer.survey.object.util.QuestionManager;
import com.jobviewer.util.Constants;
import com.lanesgroup.jobviewer.CaptureVistecActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.RiskAssessmentActivity;
import com.lanesgroup.jobviewer.WorkSuccessActivity;

public class WorkCompleteFragment extends Fragment implements OnClickListener{
	
	private ProgressBar mProgress;
	private TextView mProgressStep;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto;
	private Button mSave, mLeaveSite;
	private LinearLayout mCaptureCallingCard;
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
			
			return mRootView;
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub=
			super.onActivityCreated(savedInstanceState);
			initUI();
		}
		private void initUI() {
			mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
			mProgressStep = (TextView) mRootView.findViewById(R.id.progress_step_text);
			mAddInfo = (ImageButton) mRootView.findViewById(R.id.detail_imageButton);
			mStop = (ImageButton) mRootView.findViewById(R.id.video_imageButton);
			mUser = (ImageButton) mRootView.findViewById(R.id.user_imageButton);
			mProgress.setMax(6);
			mProgress.setProgress(5);
			mProgressStep.setText("Step 5 of 6");
			mCaptureCallingCard = (LinearLayout) mRootView.findViewById(R.id.customer_calling_layout);
			mCaptureCallingCard.setClickable(true);
			mCaptureCallingCard.setOnClickListener(this);
			mSave = (Button) mRootView.findViewById(R.id.button1);
			mLeaveSite = (Button) mRootView.findViewById(R.id.button2);
			mLeaveSite.setOnClickListener(this);
		}
		@Override
		public void onClick(View view) {
			if(view == mSave){
				
			}else if(view == mLeaveSite){
				//Upload Photos here// if calling card available
				Intent workSuccessIntent = new Intent(getActivity(),WorkSuccessActivity.class);
				workSuccessIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(workSuccessIntent);				
			}else if(view == mCaptureCallingCard){
				Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
				startActivityForResult(intent, Constants.RESULT_CODE);
			}
		}
		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
	    	if (requestCode == 500  && resultCode == getActivity().RESULT_OK){
	    		
	    	}
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
		        ContextMenuInfo menuInfo) {

		    getActivity().getMenuInflater().inflate(R.menu.activity_type_menu, menu);
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			return super.onOptionsItemSelected(item);
		}
}
