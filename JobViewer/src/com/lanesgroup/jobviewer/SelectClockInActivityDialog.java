package com.lanesgroup.jobviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectClockInActivityDialog extends Activity {

    private final String SHIFT = "Shift";
    private final String ON_CALL = "OnCall";
    private final ArrayList<HashMap<String, Object>> m_data = new ArrayList<HashMap<String, Object>>();
    private Button start, cancel;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        setContentView(R.layout.select_dialog);

        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("maintext", R.drawable.shift_clock_icon);
        map1.put("subtext", "Shift");
        m_data.add(map1);

        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put("maintext", R.drawable.oncall_phone_icon);
        map2.put("subtext", "On-call");// no small text of this item!
        m_data.add(map2);

        for (HashMap<String, Object> m : m_data)
            // make data of this view should not be null (hide )
            m.put("checked", false);
        // end init data

        final ListView lv = (ListView) findViewById(R.id.listview);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final SimpleAdapter adapter = new SimpleAdapter(this, m_data,
                R.layout.dialog_list_layout, new String[]{"maintext",
                "subtext", "checked"}, new int[]{R.id.oncall_image,
                R.id.oncall_text, R.id.checkBox2});

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                RadioButton rb = (RadioButton) view
                        .findViewById(R.id.checkBox2);
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
        findViewById(R.id.dialog_ok)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int r = -1;
                        for (int i = 0; i < m_data.size(); i++) // clean
                        // previous
                        // selected
                        {
                            HashMap<String, Object> m = m_data.get(i);
                            Boolean x = (Boolean) m.get("checked");
                            if (x == true) {
                                r = i;
                                break; // break, since it's a single choice list
                            }
                        }
                        String result = "";
                        if (r == -1)
                            return;
                        else if (r == 0)
                            result = SHIFT;
                        else if (r == 1)
                            result = ON_CALL;
                        Intent intent = new Intent();
                        intent.putExtra("Selected", result);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

        findViewById(R.id.dialog_cancel)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }
}