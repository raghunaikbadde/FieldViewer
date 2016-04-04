package com.jobviewer.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.jobviewer.comms.CommsConstant;
import com.jobviewer.db.objects.CheckOutObject;
import com.jobviewer.db.objects.StartTrainingObject;
import com.jobviewer.exception.ExceptionHandler;
import com.jobviewer.exception.VehicleException;
import com.jobviewer.provider.JobViewerDBHandler;
import com.jobviewer.survey.object.util.GsonConverter;
import com.jobviewer.util.ConfirmDialog.ConfirmDialogCallback;
import com.jobviwer.request.object.TimeSheetRequest;
import com.jobviwer.response.object.User;
import com.lanesgroup.jobviewer.ActivityPageActivity;
import com.lanesgroup.jobviewer.NewWorkActivity;
import com.lanesgroup.jobviewer.R;
import com.lanesgroup.jobviewer.TravelToWorkSiteActivity;
import com.vehicle.communicator.HttpConnection;

public class SelectActivityDialog extends Activity implements ConfirmDialogCallback {

	private CheckBox mWork, mWorkNoPhotos, mTraining;
	private String selected;
	private OnCheckedChangeListener checkChangedListner;
	private Button start, cancel;
	private Context mContext;

	private final String WORK = "Work";
	private final String WORK_NO_PHOTOS = "WorkNoPhotos";
	private final String TRAINING = "Training";
	final ArrayList<HashMap<String, Object>> m_data = new ArrayList<HashMap<String, Object>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		setContentView(R.layout.select_dialog);
		
		

		HashMap<String, Object> map1 = new HashMap<String, Object>();
		map1.put("maintext", R.drawable.work_camera_icon);
		map1.put("subtext", "Work");
		m_data.add(map1);

		HashMap<String, Object> map2 = new HashMap<String, Object>();
		map2.put("maintext", R.drawable.work_nophotos_user_icon);
		map2.put("subtext", "Work (no photos or data)");// no small text of this item!
		m_data.add(map2);

		HashMap<String, Object> map3 = new HashMap<String, Object>();
		map3.put("maintext", R.drawable.training_book_icon);
		map3.put("subtext", "Training / Toolbox Talk");
		m_data.add(map3);

		for (HashMap<String, Object> m : m_data)
			// make data of this view should not be null (hide )
			m.put("checked", false);
		// end init data

		final ListView lv = (ListView) findViewById(R.id.listview);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		final SimpleAdapter adapter = new SimpleAdapter(this, m_data,
				R.layout.dialog_list_layout, new String[] { "maintext",
						"subtext", "checked" }, new int[] { R.id.oncall_image,
						R.id.oncall_text, R.id.checkBox2 });

		lv.setAdapter(adapter);

lv.setOnItemClickListener(new OnItemClickListener() {

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RadioButton rb = (RadioButton) view.findViewById(R.id.checkBox2);
		if (!rb.isChecked()) // OFF->ON
		{
			for (HashMap<String, Object> m : m_data)
				// clean previous selected
				m.put("checked", false);

			m_data.get(position).put("checked", true);
			adapter.notifyDataSetChanged();
		}
	}
});

		// show result
		((Button) findViewById(R.id.dialog_ok))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int selected = -1;
						Intent intent = new Intent();
						for (int i = 0; i < m_data.size(); i++) // clean
																// previous
																// selected
						{
							HashMap<String, Object> m = m_data.get(i);
							Boolean x = (Boolean) m.get("checked");
							if (x == true) {
								selected = i;
								break; // break, since it's a single choice list
							}
						}
						String result = "";
						if(selected==-1)
							return;
						else if(selected==0){
							CheckOutObject checkOutRemember = JobViewerDBHandler
									.getCheckOutRemember(v.getContext());
							if (checkOutRemember != null
									&& ActivityConstants.TRUE
											.equalsIgnoreCase(checkOutRemember
													.getIsTravelEnd())) {
								intent.setClass(SelectActivityDialog.this,
										NewWorkActivity.class);
							} else {
								intent.setClass(SelectActivityDialog.this,
										TravelToWorkSiteActivity.class);
							}
							result = WORK;
							startActivity(intent);
						}
						else if (selected==1){
							result = WORK_NO_PHOTOS;
						}
							
						else if (selected==2){
							new ConfirmDialog(v.getContext(), SelectActivityDialog.this, Constants.START_TRAINING).show();
							result = TRAINING;
							return;
						}
						/*intent.putExtra("Selected", result);
						setResult(RESULT_OK, intent);*/
						finish();
					}
				});
		
		((Button) findViewById(R.id.dialog_cancel)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onConfirmStartTraining() {
		
		ContentValues data = new ContentValues();
		Utils.timeSheetRequest = new TimeSheetRequest();
		
		Utils.timeSheetRequest.setStarted_at(new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
		.format(Calendar.getInstance().getTime())); 
				
		data.put("started_at", new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
		.format(Calendar.getInstance().getTime()));
		
		
		User userProfile = JobViewerDBHandler.getUserProfile(this);
		if(userProfile!=null){
			Utils.timeSheetRequest.setRecord_for(userProfile.getEmail());
			data.put("record_for", userProfile.getEmail());
		}else{
			Utils.timeSheetRequest.setRecord_for("fsa@lancegroup.com");
			data.put("record_for", "fsa@lancegroup.com");
		}
		
		Utils.timeSheetRequest.setIs_inactive("");
		data.put("is_inactive", "");
		
		Utils.timeSheetRequest.setIs_overriden("");
		data.put("is_overriden", "");
		
		Utils.timeSheetRequest.setOverride_reason("");
		data.put("override_reason", "");
		
		Utils.timeSheetRequest.setOverride_comment("");
		data.put("override_comment","");
		
		Utils.timeSheetRequest.setOverride_timestamp("");
		data.put("override_timestamp","");
		
		CheckOutObject checkOutObject = JobViewerDBHandler.getCheckOutRemember(this);
		if(checkOutObject.getVistecId() != null){
			Utils.timeSheetRequest.setReference_id(checkOutObject.getVistecId());
			data.put("reference_id", checkOutObject.getVistecId());
		} else {
			Utils.timeSheetRequest.setReference_id("");
			data.put("reference_id", "");
		}
		if(userProfile!=null)
			data.put("user_id", userProfile.getEmail());
		else 
			data.put("user_id", "fsa@lancegroup.com");
		String time = new SimpleDateFormat("HH:mm:ss dd MMM yyyy")
		.format(Calendar.getInstance().getTime());

		if (Utils.isInternetAvailable(this)){
			Utils.SendHTTPRequest(this, CommsConstant.HOST
					+ CommsConstant.START_TRAINING_API, data,
					getStartTrainingHandler(time));
		} else {
			Utils.saveTimeSheetInBackLogTable(
					SelectActivityDialog.this, Utils.timeSheetRequest,
					CommsConstant.START_TRAINING_API,
					Utils.REQUEST_TYPE_WORK);
			saveTrainingTimeSheet(Utils.timeSheetRequest);
		}
		setResult(RESULT_OK);
		finish();
	}
	
	private void saveTrainingTimeSheet(TimeSheetRequest timeSheetRequest) {
		StartTrainingObject startTraining=new StartTrainingObject();
		startTraining.setIsTrainingStarted("true");
		startTraining.setStartTime(timeSheetRequest.getStarted_at());
		JobViewerDBHandler.saveStartTraining(this, startTraining);
		
	}

	private Handler getStartTrainingHandler(final String time) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HttpConnection.DID_SUCCEED:
					Utils.StopProgress();
					saveTrainingTimeSheet(Utils.timeSheetRequest);
					break;
				case HttpConnection.DID_ERROR:
					Utils.StopProgress();
					String error = (String) msg.obj;
					VehicleException exception = GsonConverter
							.getInstance()
							.decodeFromJsonString(error, VehicleException.class);
					ExceptionHandler.showException(mContext, exception, "Info");
					Utils.saveTimeSheetInBackLogTable(
							SelectActivityDialog.this, Utils.timeSheetRequest,
							CommsConstant.START_TRAINING_API,
							Utils.REQUEST_TYPE_WORK);
					saveTrainingTimeSheet(Utils.timeSheetRequest);
					break;
				default:
					break;
				}
			}
		};
		return handler;
	}

	@Override
	public void onConfirmDismiss() {
		// TODO Auto-generated method stub
		
	}
}
