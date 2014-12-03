package view;

import java.util.ArrayList;
import java.util.List;

import com.example.todoapp.R;
import com.example.todoapp.R.id;
import com.example.todoapp.R.layout;
import com.example.todoapp.R.menu;
import controller.ApplicationNavigationHandler;
import model.DatabaseAdapter;
import model.Group;
import model.Task;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ViewGroupTasksActivity extends GeneralActivity {

	Group group;
	// The List View that shows all Tasks
	private ListView allTasksListView;

	// DatabaseAdapter for interacting with database
	private DatabaseAdapter databaseAdapter;

	// The cursor for query all groups from database
	private Cursor allGroupTasksCursor;

	// Adapter for All Tasks List View
	private SimpleCursorAdapter allTasksListViewAdapter;

	// The Add New Task request code
	public static final int ADD_NEW_TASK_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_group_tasks);
		Bundle modifyGroupBundle = this.getIntent().getExtras();
		try {
			this.group = (Group) modifyGroupBundle.getSerializable(Group.GROUP_BUNDLE_KEY);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		// set action listener for allTasksListView
		allTasksListView = (ListView) findViewById(R.id.activity_view_group_tasks_Listview_all_tasks);
		allTasksListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				allGroupTasksListViewItemClickHandler(arg0, arg1, arg2);
			}
		});

		// Open the connection to database
		databaseAdapter = new DatabaseAdapter(this);
		databaseAdapter.open();
		
		// Init all tasks and add them to list view
		initAllTasksListView();
	}

	// Init the Tasks List View
	// Load all Tasks from database and put them to list view
	public void initAllTasksListView(){
		// Check if the databaseAdapter is not null
		if(this.databaseAdapter != null){
			// Get all Tasks
			allGroupTasksCursor = databaseAdapter.getGroupTasks(group.getId());
			// TODO replace the deprecated startManagingCursor method with an alternative one
			startManagingCursor(allGroupTasksCursor);
			// Get data from which column
			String[] from = new String[]{DatabaseAdapter.TASK_TABLE_COLUMN_TITLE};
			// Put data to which components in layout
			int[] to = new int[]{R.id.activity_view_all_groups_listview_all_groups_layout_textview_group_title};
			// Init the adapter for list view
			// TODO replace the deprecated SimpleCursorAdapter with an alternative one
			allTasksListViewAdapter = new SimpleCursorAdapter(this,
					R.layout.activity_view_all_groups_listview_all_groups_layout, allGroupTasksCursor, from, to);
			// Set the adapter for the list view
			this.allTasksListView.setAdapter(allTasksListViewAdapter);
		}
	}

	private void allGroupTasksListViewItemClickHandler(AdapterView<?> adapterView, View listView, int selectedItemId){
		// Create a new Task object and init the data
		// After that pass it to the next activity to display detail
		Task selectedTask = new Task();
		// move the cursor to the right position
		allGroupTasksCursor.moveToFirst();
		allGroupTasksCursor.move(selectedItemId);
		// set the data for selectedTask
		// set id
		selectedTask.setId(allGroupTasksCursor.getString(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_ID)));
		// set title
		selectedTask.setTitle(allGroupTasksCursor.getString(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_TITLE)));
		// set due date
		selectedTask.getDueDate().setTimeInMillis(allGroupTasksCursor.getLong(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_DUE_DATE)));
		// set note
		selectedTask.setNote(allGroupTasksCursor.getString(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_NOTE)));
		// set priority level
		selectedTask.setPriorityLevel(allGroupTasksCursor.getInt(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_PRIORITY)));
		// set completion status
		selectedTask.setCompletionStatus(allGroupTasksCursor.getInt(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_COMPLETION_STATUS)));
		// set the group
		selectedTask.setGroup(this.getGroupByTask(allGroupTasksCursor.getString(allGroupTasksCursor.getColumnIndex(DatabaseAdapter.TASK_TABLE_COLUMN_GROUP))));
		
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
		case R.id.activity_view_group_tasks_Menu_actionbar_Item_add_task:
			ApplicationNavigationHandler.addNewTask(this, this.databaseAdapter);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_group_tasks, menu);
		setTitle(this.group.getTitle());
		return true;
	}

}
