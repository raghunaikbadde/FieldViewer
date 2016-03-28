package com.lanesgroup.jobviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.jobviewer.util.Constants;
import com.lanesgroup.jobviewer.fragment.WorkCompleteFragment;

public class AddPhotosActivity extends BaseActivity implements OnClickListener{
	
	private ProgressBar mProgress;
	private TextView mProgressStep;
	private ImageButton mAddInfo, mStop, mUser, mClickPhoto;
	private Button mSave, mLeaveSite;
	private ListView mListView;
	private View mRootView;
	private ArrayList<HashMap<String, Object>> mPhotoList; 
	private SimpleAdapter mAdapter;
	private Context mContext;
	static File file;
	
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.add_photos_screen);
			initUI();
		}
		
		private void initUI() {
			mContext = this;
			mPhotoList = new ArrayList<HashMap<String,Object>>();
			mAdapter = new SimpleAdapter(this, mPhotoList, R.layout.add_photo_list, new String[] { "picture",
							"time"}, new int[] { R.id.captured_image1, R.id.date_time_text1});
			mAdapter.setViewBinder(new ViewBinder() {
				public boolean setViewValue(View view, Object data,
						String textRepresentation) {
					if (data == null) {
						view.setVisibility(View.GONE);
						return true;
					}
					view.setVisibility(View.VISIBLE);
					return false;
				}
			});
			
			mListView = (ListView) findViewById(R.id.listview);
			mListView.setAdapter(mAdapter);

			mProgress = (ProgressBar) findViewById(R.id.progressBar);
			mProgressStep = (TextView) findViewById(R.id.progress_step_text);
			mAddInfo = (ImageButton) findViewById(R.id.detail_imageButton);
			mStop = (ImageButton) findViewById(R.id.video_imageButton);
			mUser = (ImageButton) findViewById(R.id.user_imageButton);
			mClickPhoto = (ImageButton) findViewById(R.id.capture_imageButton);
			mClickPhoto.setOnClickListener(this);
			mSave = (Button) findViewById(R.id.button1);
			mLeaveSite = (Button) findViewById(R.id.button2);
			mLeaveSite.setOnClickListener(this);
		}
		@Override
		public void onClick(View view) {
			if(view == mSave){
				
			}else if(view == mLeaveSite){
				getFragmentManager().beginTransaction().add(android.R.id.content, new WorkCompleteFragment()).commit();
			}else if(view == mClickPhoto){
				Intent intent = new Intent(Constants.IMAGE_CAPTURE_ACTION);
				startActivityForResult(intent, Constants.RESULT_CODE);
			}
		}
		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
	    	if (requestCode == 500  && resultCode == RESULT_OK){
				Bitmap rotateBitmap = ((Bitmap) data.getExtras().get("data"));

	    		HashMap<String, Object> map1 = new HashMap<String, Object>();
	    		map1.put("picture", rotateBitmap);
	    		map1.put("time", Calendar.getInstance().getTime());
	    		mPhotoList.add(map1);
	    		mAdapter.notifyDataSetChanged();
	    	}
		}
}
