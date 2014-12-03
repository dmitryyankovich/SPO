package view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ViewDailyTasksActivity extends GeneralActivity {

	// The List View that shows all Tasks
	private ListView dailyTasksListView;

	// DatabaseAdapter for interacting with database
	private DatabaseAdapter databaseAdapter;

	// The cursor for query all groups from database
	private Cursor dailyTasksCursor;

	// Adapter for All Tasks List View
	private SimpleCursorAdapter dailyTasksListViewAdapter;

	// The Add New Task request code
	public static final int ADD_NEW_TASK_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_daily_tasks);

		// set action listener for allTasksListView
		dailyTasksListView = (ListView) findViewById(R.id.activity_view_daily_tasks_Listview_all_tasks);
		dailyTasksListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				dailyTaskListViewItemClickHandler(arg0, arg1, arg2);
			}
		});

		// Open the connection to database
		databaseAdapter = new DatabaseAdapter(this);
		databaseAdapter.open();
		
		// Init all tasks and add them to list view
		initDailyTasksListView();
	}

	private Calendar getDateWithOutTime(Date targetDate) {
	    Calendar newDate = Calendar.getInstance();
	    newDate.setLenient(false);
	    newDate.setTime(targetDate);
	    newDate.set(Calendar.HOUR_OF_DAY, 0);
	    newDate.set(Calendar.MINUTE,0);
	    newDate.set(Calendar.SECOND,0);
	    newDate.set(Calendar.MILLISECOND,0);

	    return newDate;

	}
	
	// Init the Tasks List View
	// Load all Tasks from database and put them to list view
	public void initDailyTasksListView(){
		// Check if the databaseAdapter is not null
		if(this.databaseAdapter != null){
			// Get all Tasks
			dailyTasksCursor = databaseAdapter.getDailyTasks(getDateWithOutTime(Calendar.getInstance().getTime()).getTimeInMillis());
			// TODO replace the deprecated startManagingCursor method with an alternative one
			startManagingCursor(dailyTasksCursor);
			// Get data from which column
			String[] from = new String[]{DatabaseAdapter.TASK_TABLE_COLUMN_TITLE};
			// Put data to which components in layout
			int[] to = new int[]{R.id.activity_view_all_groups_listview_all_groups_layout_textview_group_title};
			// Init the adapter for list view
			// TODO replace the deprecated SimpleCursorAdapter with an alternative one
			dailyTasksListViewAdapter = new SimpleCursorAdapter(this,
					R.layout.activity_view_all_groups_listview_all_groups_layout, dailyTasksCursor, from, to);
			// Set the adapter for the list view
			this.dailyTasksListView.setAdapter(dailyTasksListViewAdapter);
		}
	}

	// Handle the item clicked event of allTasksListView
	private void dailyTaskListViewItemClickHandler(AdapterView<?> adapterView, View listView, int selectedItemId){
		// Create a new Task object and init the data
		// After that pass it to the next activity to display detail
		Task selectedTask = new Task();
		// move the cursor to the right position
		dailyTasksCursor.moveToFirst();
		dailyTasksCursor.move(selectedItemId);
		// set the data for selectedTask
		// set id
		selectedTask.setId(dailyTasksCursor.getString(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_ID)));
		// set title
		selectedTask.setTitle(dailyTasksCursor.getString(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_TITLE)));
		// set due date
		selectedTask.getDueDate().setTimeInMillis(dailyTasksCursor.getLong(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_DUE_DATE)));
		// set note
		selectedTask.setNote(dailyTasksCursor.getString(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_NOTE)));
		// set priority level
		selectedTask.setPriorityLevel(dailyTasksCursor.getInt(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_PRIORITY)));
		// set completion status
		selectedTask.setCompletionStatus(dailyTasksCursor.getInt(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_COMPLETION_STATUS)));
		// set the group
		selectedTask.setGroup(this.getGroupByTask(dailyTasksCursor.getString(dailyTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_GROUP))));
		// set the collaborators
		
		// start the activity
		ApplicationNavigationHandler.viewTaskDetail(this, selectedTask);
	}
	
	// Get the group by the input Id
	private Group getGroupByTask(String groupId){
		Group group = new Group();
		
		// query from database
		Cursor groupCursor = this.databaseAdapter.getGroupById(groupId);
		groupCursor.moveToFirst();
		group.setId(groupCursor.getString(groupCursor.getColumnIndex(DatabaseAdapter.GROUP_TABLE_COLUMN_ID)));
		group.setTitle(groupCursor.getString(groupCursor.getColumnIndex(DatabaseAdapter.GROUP_TABLE_COULMN_TITLE)));
		
		return group;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.activity_view_daily_tasks_Menu_actionbar_Item_add_task:
			ApplicationNavigationHandler.addNewTask(this, this.databaseAdapter);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_daily_tasks, menu);
		return true;
	}

}
