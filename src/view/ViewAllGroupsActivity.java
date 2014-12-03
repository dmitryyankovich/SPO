package view;

import model.DatabaseAdapter;
import model.Group;
import model.Task;

import com.example.todoapp.R;
import com.example.todoapp.R.id;
import com.example.todoapp.R.layout;
import com.example.todoapp.R.menu;

import controller.ApplicationNavigationHandler;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ViewAllGroupsActivity extends GeneralActivity {
	
	// VARIABLES DEFINED HERE
	
	// DatabaseAdapter for interacting with database
	private DatabaseAdapter databaseAdapter;
	
	// The cursor for query all groups from database
	private Cursor allGroupsCursor;
	
	// Adapter for All Groups List View
	private SimpleCursorAdapter allGroupsListViewAdapter;
	
	// All Groups List View
	private ListView allGroupsListView;
	
	// 
	public static final int ADD_NEW_GROUP_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_all_groups);
		
		// Retrieve the all groups list view
		this.allGroupsListView = (ListView) findViewById(R.id.activity_view_all_groups_Listview_all_groups);
		allGroupsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				allGroupsListViewItemClickHandler(arg0, arg1, arg2);
			}
		});
		// Open the connection to database
		databaseAdapter = new DatabaseAdapter(this);
		databaseAdapter.open();
		// Retrieve all data and put it to 
		initAllGroupsListView();
		
	}
	
	// UTILITY METHODS
	
	// Get all groups from database and then put it to list view
	private void initAllGroupsListView(){
		// Check if the databaseAdapter is not null
		if(this.databaseAdapter != null){
			// Get all groups
			allGroupsCursor = databaseAdapter.getAllGroups();
			// TODO replace the deprecated startManagingCursor method with an alternative one
			startManagingCursor(allGroupsCursor);
			// Get data from which column
			String[] from = new String[]{DatabaseAdapter.GROUP_TABLE_COULMN_TITLE};
			// Put data to which components in layout
			int[] to = new int[]{R.id.activity_view_all_groups_listview_all_groups_layout_textview_group_title};
			// Init the adapter for list view
			// TODO replace the deprecated SimpleCursorAdapter with an alternative one
			allGroupsListViewAdapter = new SimpleCursorAdapter(this,
					R.layout.activity_view_all_groups_listview_all_groups_layout, allGroupsCursor, from, to);
			// Set the adapter for the All Groups List View
			this.allGroupsListView.setAdapter(allGroupsListViewAdapter);
		}
	}
	
	private void allGroupsListViewItemClickHandler(AdapterView<?> adapterView, View listView, int selectedItemId){
		// Create a new Task object and init the data
		// After that pass it to the next activity to display detail
		Group selectedGroup = new Group();
		// move the cursor to the right position
		allGroupsCursor.moveToFirst();
		allGroupsCursor.move(selectedItemId);
		// set the data for selectedTask
		// set id
		selectedGroup.setId(allGroupsCursor.getString(allGroupsCursor.getColumnIndex(DatabaseAdapter.GROUP_TABLE_COLUMN_ID)));
		// set title
		selectedGroup.setTitle(allGroupsCursor.getString(allGroupsCursor.getColumnIndex(DatabaseAdapter.GROUP_TABLE_COULMN_TITLE)));
		
		// start the activity
		ApplicationNavigationHandler.showGroupTasks(this, selectedGroup);
	}
	
	
	// OVERRIDE METHODS

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_all_groups, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		// Check if the requestCode is ADD_NEW_GROUP_RESULT_CODE
		if(requestCode == ViewAllGroupsActivity.ADD_NEW_GROUP_REQUEST_CODE){
			// re-init the list view
			initAllGroupsListView();
		}
	}

}
