package com.lanesgroup.jobviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jobviewer.util.Constants;
import com.jobviewer.util.SelectActivityDialog;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        
        Button button1 = (Button) findViewById(R.id.clock_in);
        button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openDialog();
			}
		});
    }
    
    public void openDialog() {
    	Intent intent = new Intent();
    	intent.setClass(MainActivity.this, SelectActivityDialog.class);
    	startActivityForResult(intent, Constants.RESULT_CODE_WELCOME);
        /*final Dialog dialog = new Dialog(this); // Context, this, etc.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_box2_test);
        dialog.show();*/
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == 1003 && resultCode == RESULT_OK) {
    		data.getExtras().getString("Selected");
    	}
    }
}
