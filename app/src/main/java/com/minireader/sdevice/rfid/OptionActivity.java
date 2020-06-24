package com.minireader.sdevice.rfid;
import java.util.ArrayList;
import com.asreader.component.SegmentedRadioGroup;
import com.asreader.sdevice.dongle.custom.CellData;
import com.asreader.sdevice.dongle.custom.CustomSettingAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.app.Activity;
import android.content.Intent;

public class OptionActivity extends Activity  
{

	private Button back;
	private CustomSettingAdapter    m_Adapter;
	private ListView                m_OptionListView;
	public ArrayList<CellData>       mListData   = new ArrayList<CellData>();
	ArrayList<String> arrayListName   = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option);

		back =(Button) findViewById(R.id.option_navigation_back_button);
		back.setOnClickListener(new OnClickListener()
		{	    
			@Override
			public void onClick(View v)
			{	
				moveTaskToBack(false);
				finish();
			}
		});		
		
		for(int i=0; i<arrayListName.size();i++)
		{
			CellData data =new CellData();
			data.setName(arrayListName.get(i));
			mListData.add(data);
		}

		m_Adapter     = new CustomSettingAdapter(this,R.layout.cell_setting, mListData);
		m_OptionListView = (ListView) findViewById(R.id.listOption);
		m_OptionListView.setAdapter(m_Adapter);

		m_OptionListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				CellData data = mListData.get(position);
				if(data.getName().equals("Setting"))
				{
					Intent intent = new Intent(getBaseContext(),
							PopSettingActivity.class);
					startActivity(intent);	
				}

				if(data.getName().equals("Hopping"))
				{
					Intent intent = new Intent(getBaseContext(),
							HoppingView.class);
					startActivity(intent);	
				}

				if(data.getName().equals("Region"))
				{
					Intent intent = new Intent(getBaseContext(),
							RegionActivity.class);
					startActivity(intent);	
				}

				if(data.getName().equals("Info"))
				{
					Intent intent = new Intent(getBaseContext(),
							InfoActivity.class);
					startActivity(intent);
				}

				if(data.getName().equals("Reader Setting On/Off"))
				{
					Intent intent = new Intent(getBaseContext(),
							SettingOnOffActivity.class);
					startActivity(intent);
				}

			}
		});

		arrayListName.clear();
		m_Adapter.clear();

		arrayListName.add("Setting");
		arrayListName.add("Hopping");
		arrayListName.add("Region");
		arrayListName.add("Info");
		arrayListName.add("Reader Setting On/Off");

		for(int i=0; i<arrayListName.size();i++)
		{
			CellData data =new CellData();
			data.setName(arrayListName.get(i));
			mListData.add(data);
		}

		m_Adapter.notifyDataSetChanged();

	}

}
