package view;

import java.util.ArrayList;
import java.util.Calendar;

import model.DatabaseAdapter;
import model.Group;
import model.Task;

import com.example.todoapp.R;
import com.example.todoapp.R.layout;
import com.example.todoapp.R.menu;

import controller.ConfirmCancelDialogHandler;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

// This activity is used for 2 purposes: Creating new Task and Editing existing Task
// Every time it's loaded, the program should check whether the Task object in the bundle is exist
// If it's exist, this means the activity is going to edit that task
// Otherwise, the activity is going to create a new task
public class ModifyTaskActivity extends GeneralActivity {

	// VARIABLES DEFINED HERE

	// a Task object to hold the data about the current task being processed
	// If the activity is creating a new task, this object will be initialized
	// Otherwise, it will be retrieved from the bundle
	private Task task = null;

	// store the current job of this activity, Edit or Add task
	// value will be set from those 2 static variables below
	private int currentJob;
	private final int CURRENT_JOB_EDIT = 1;
	private final int CURRENT_JOB_ADD = 2;
	
	// DatabaseAdapter for interacting with database
	private DatabaseAdapter databaseAdapter;

	// The cursor for query all groups from database
	private Cursor allGroupsCursor;

	// Adapter for All Groups List View
	private SimpleCursorAdapter allGroupsSpinnerAdapter;

	// Group spinner
	private Spinner allGroupsSpinner;


	// OVERIDE FUNCTIONS

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent resultIntent;
		Bundle resultBundle;

		switch (item.getItemId()){

		// When user select Cancel or Home button, show a confirmation dialog
		case R.id.activity_modify_task_Menu_actionbar_Item_cancel:
		case android.R.id.home:
			// set the result for the previous activity
			resultIntent = new Intent();
			resultBundle = new Bundle();
			resultBundle.putSerializable(Task.TASK_BUNDLE_KEY, this.task);
			resultIntent.putExtras(resultBundle);
			setResult(ViewTaskDetailActivity.EDIT_TASK_REQUEST_CODE, resultIntent);

			// Show the confirmation dialog
			// The rest (leave activity or not), the function ApplicationDialogHandler.showConfirmCancelDialog() will handle
			ConfirmCancelDialogHandler.showConfirmCancelDialog(this);
			return true;

			// When user select Save
		case R.id.activity_modify_task_Menu_actionbar_Item_save:
			// validate form

			// check whether the current job is add new or edit
			if(this.currentJob == CURRENT_JOB_ADD){
				// add new task to database
				addNewTask();
			} else {
				// edit the current task
				editExistingTask();

				// set the result for the previous activity
				resultIntent = new Intent();
				resultBundle = new Bundle();
				resultBundle.putSerializable(Task.TASK_BUNDLE_KEY, this.task);
				resultIntent.putExtras(resultBundle);
				setResult(ViewTaskDetailActivity.EDIT_TASK_REQUEST_CODE, resultIntent);

			}

			// close this activity
			finish();
			return true;

			// default case, return the base class function
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		// set the result for the previous activity
		Intent resultIntent = new Intent();
		Bundle resultBundle = new Bundle();
		resultBundle.putSerializable(Task.TASK_BUNDLE_KEY, this.task);
		resultIntent.putExtras(resultBundle);
		setResult(ViewTaskDetailActivity.EDIT_TASK_REQUEST_CODE, resultIntent);

		// Show the confirmation dialog
		// The rest (leave activity or not), the function ApplicationDialogHandler.showConfirmCancelDialog() will handle
		ConfirmCancelDialogHandler.showConfirmCancelDialog(this);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_task);

		// Retrieve the group spinner
		allGroupsSpinner = (Spinner) findViewById(R.id.activity_modify_task_Spinner_group);

		// Open the connection to database
		databaseAdapter = new DatabaseAdapter(this);
		databaseAdapter.open();

		// init the group
		this.initGroup();

		// Check whether this activity is going to create a new Task or edit an existing one
		// First, retrieve the Task object from the bundle
		Bundle modifyTaskBundle = this.getIntent().getExtras();
		try {
			this.task = (Task) modifyTaskBundle.getSerializable(Task.TASK_BUNDLE_KEY);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		// Next, check if it is null
		if (task != null){
			// Change the currentJob to edit task
			this.currentJob = this.CURRENT_JOB_EDIT;
			// If the Task object exist, load data from it and put to the form
			putDataToForm();
			// After that, change the title of the activity to "Edit task " + task name
		} else {
			// Init the new Task object
			this.task = new Task();
			// Change the currentJob to add new task
			this.currentJob = this.CURRENT_JOB_ADD;
			// If the Task object not exist, change the title of the activity to "Create new Task"
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.modify_task, menu);
		return true;
	}


	// UTILITY FUNCTIONS

	// This function is used to edit the current task
	private void editExistingTask(){
		// Load data from form to this.task object
		updateTaskObject();
		// Call the database adapter to update the task
		databaseAdapter.editExistingTask(this.task);
	}

	// This function is used to retrieve data from form and update the this.task object
	private void updateTaskObject(){
		// Retrieve data from form and put into this.task object
		// No need to set collaborators since the list view handles it automatically
		// set task title
		String taskTitle = ((EditText)findViewById(R.id.activity_modify_task_Edittext_task_title)).getText().toString();
		this.task.setTitle(taskTitle);
		// set due date
		DatePicker taskDueDatePicker = (DatePicker) findViewById(R.id.activity_modify_task_Datepicker_due_date);
		this.task.getDueDate().set(Calendar.DATE, taskDueDatePicker.getDayOfMonth());
		this.task.getDueDate().set(Calendar.MONTH, taskDueDatePicker.getMonth());
		this.task.getDueDate().set(Calendar.YEAR, taskDueDatePicker.getYear());
		// set note
		String taskNote = ((EditText)findViewById(R.id.activity_modify_task_EditText_note)).getText().toString();
		this.task.setNote(taskNote);
		// set priority level
		int priorityLevel = ((Spinner)findViewById(R.id.activity_modify_task_Spinner_priority_level)).getSelectedItemPosition();
		this.task.setPriorityLevel(priorityLevel);
		// set group
		Spinner groupSpinner = (Spinner) findViewById(R.id.activity_modify_task_Spinner_group);
		this.task.getGroup().setId(getGroupIdByPosition(allGroupsCursor, groupSpinner.getSelectedItemPosition()));
	}

	// This function is used to add new task to database
	private void addNewTask(){
		// Retrieve data from form and put into this.task object
		// No need to set collaborators since the list view handles it automatically
		// set task title
		String taskTitle = ((EditText)findViewById(R.id.activity_modify_task_Edittext_task_title)).getText().toString();
		this.task.setTitle(taskTitle);
		// set due date
		DatePicker taskDueDatePicker = (DatePicker) findViewById(R.id.activity_modify_task_Datepicker_due_date);
		this.task.getDueDate().set(Calendar.DATE, taskDueDatePicker.getDayOfMonth());
		this.task.getDueDate().set(Calendar.MONTH, taskDueDatePicker.getMonth());
		this.task.getDueDate().set(Calendar.YEAR, taskDueDatePicker.getYear());
		
		// set note
		String taskNote = ((EditText)findViewById(R.id.activity_modify_task_EditText_note)).getText().toString();
		this.task.setNote(taskNote);
		// set priority level
		int priorityLevel = ((Spinner)findViewById(R.id.activity_modify_task_Spinner_priority_level)).getSelectedItemPosition();
		this.task.setPriorityLevel(priorityLevel);
		// set group
		Spinner groupSpinner = (Spinner) findViewById(R.id.activity_modify_task_Spinner_group);
		this.task.getGroup().setId(getGroupIdByPosition(allGroupsCursor, groupSpinner.getSelectedItemPosition()));
		
		this.task.setCompletionStatus(0);
		// Since we add new task, need to get a new task id
		// set task id
		String taskId = databaseAdapter.getNewTaskId();
		this.task.setId(taskId);

		// Call the database adapter to add new task
		this.databaseAdapter.insertTask(this.task);
	}

	// This function is used to init group spinner
	// it loads all group from database and then put into group spinner
	// need to be called in the onCreate() method
	private void initGroup(){
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
			// Init the adapter for spinner
			// TODO replace the deprecated SimpleCursorAdapter with an alternative one
			this.allGroupsSpinnerAdapter = new SimpleCursorAdapter(this,
					R.layout.activity_view_all_groups_listview_all_groups_layout, allGroupsCursor, from, to);
			// Set the adapter for the spinner
			allGroupsSpinner.setAdapter(allGroupsSpinnerAdapter);
		} else {
			finish();
		}
	}
	// This function is used to load data from this.task object and put it to form
	private void putDataToForm(){
		// First check if the current job is edit task
		if (this.currentJob == this.CURRENT_JOB_EDIT){
			// Now retrieve data from this Task oject and put it into form components
			// no need to set Collaborator list since it's a special component and there is another function that handles that task

			// set task title
			EditText taskTitleEditText = (EditText) findViewById(R.id.activity_modify_task_Edittext_task_title);
			taskTitleEditText.setText(this.task.getTitle());

			// set task date
			DatePicker taskDueDatePicker = (DatePicker) findViewById(R.id.activity_modify_task_Datepicker_due_date);
			taskDueDatePicker.updateDate(this.task.getDueDate().get(Calendar.YEAR),
					this.task.getDueDate().get(Calendar.MONTH),
					this.task.getDueDate().get(Calendar.DATE));

			// set task note
			EditText taskNoteEditText = (EditText) findViewById(R.id.activity_modify_task_EditText_note);
			taskNoteEditText.setText(this.task.getNote());

			// set priority level
			Spinner taskPriorityLevelSpinner = (Spinner) findViewById(R.id.activity_modify_task_Spinner_priority_level);
			taskPriorityLevelSpinner.setSelection(this.task.getPriorityLevel());

			// set group
			allGroupsSpinner.setSelection(this.getGroupPositionInCursor(allGroupsCursor, this.task.getGroup().getId()));
		}
	}

	// get the id of the group with the input position
	private String getGroupIdByPosition(Cursor cursor, int position){
		String groupId = null;
		cursor.moveToFirst();
		cursor.move(position);
		groupId = cursor.getString(cursor.getColumnIndex(DatabaseAdapter.GROUP_TABLE_COLUMN_ID));
		return groupId;
	}

	// get the position of the group with id String groupId in the cursor
	private int getGroupPositionInCursor(Cursor cursor, String groupId){
		int position = -1;
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			String currentGroupId = cursor.getString(cursor.getColumnIndex(DatabaseAdapter.GROUP_TABLE_COLUMN_ID));
			if(currentGroupId.equals(groupId)){
				position = cursor.getPosition();
			}
			cursor.moveToNext();
		}
		return position;
	}

}
