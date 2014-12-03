package view;

import model.DatabaseAdapter;

import com.example.todoapp.R;
import com.example.todoapp.R.id;
import com.example.todoapp.R.layout;
import com.example.todoapp.R.menu;
import com.example.todoapp.R.string;

import controller.ApplicationNavigationHandler;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
	private DatabaseAdapter databaseAdapter;
	private ListView mainListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		databaseAdapter = new DatabaseAdapter(this);
		databaseAdapter.open();
		if(this.databaseAdapter != null){
			if(databaseAdapter.getGroupByTitle("Home").getCount() == 0){
				databaseAdapter.insertGroup(databaseAdapter.getNewGroupId(), "Home");
			}
			if(databaseAdapter.getGroupByTitle("Work").getCount() == 0){
				databaseAdapter.insertGroup(databaseAdapter.getNewGroupId(), "Work");
			}
			if(databaseAdapter.getGroupByTitle("Study").getCount() == 0){
				databaseAdapter.insertGroup(databaseAdapter.getNewGroupId(), "Study");
			}
		}
		// set action listener for mainListView
		mainListView = (ListView) findViewById(R.id.activity_main_Listview_main_option);
		mainListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View listView, int selectedItemId,
					long arg4) {
				// TODO Auto-generated method stub
				mainListViewOnItemClickHandler(adapterView, listView, selectedItemId, arg4);
			}
		});
	}
	
	// handler for mainListView item click event
	private void mainListViewOnItemClickHandler(AdapterView<?> adapterView, View listView, int selectedItemId,
			long arg4) {
		//get selected item
		String selectedItem = (String) mainListView.getItemAtPosition(selectedItemId);
		
		// check which one the user want to navigate to
		if(selectedItem.equals(getString(R.string.activity_main_Listview_main_option_Item_show_all_groups_String_title))){
			ApplicationNavigationHandler.showAllGroups(this);
		}
		if(selectedItem.equals(getString(R.string.activity_main_Listview_main_option_Item_show_all_tasks_String_title))){
			ApplicationNavigationHandler.showAllTasks(this);
		}
		if(selectedItem.equals(getString(R.string.activity_main_Listview_main_option_Item_show_daily_tasks_String_title))){
			ApplicationNavigationHandler.showDailyTasks(this);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
