package com.example.beatxbeat;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ImportActivity extends Activity {

	public static final String XML = "xml";
	public static final String PCM = "pcm";
	public static final String FILE_TYPE = "fileType";

	private String mFileType;
	private String mProjectPath;
	private String mClipPath;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);

		mFileType = this.getIntent().getStringExtra(FILE_TYPE);
		mProjectPath = this.getIntent().getStringExtra(ProjectPageActivity.PROJECT_PATH);

		setupSearchBar();

		listFiles("", mFileType);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.import_project, menu);
		return true;
	}

	private void listFiles(CharSequence pName, String fileType) {
		ScrollView fileList = (ScrollView) findViewById(R.id.file_select_scrollview);
		ScrollView deleteButtons = (ScrollView) findViewById(R.id.delete_buttons);
		LinearLayout deleteButtonLayout = new LinearLayout(this);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		deleteButtonLayout.setOrientation(LinearLayout.VERTICAL);

		File[] files = this.getFilesDir().listFiles();
		int numFiles = 0;
		if (files != null) {
			for (File file : files) {
				Log.d("ImportActivity", "file found at path: " + file.getPath());
				final String path = file.getPath();
				final String filetype = path.substring(path.length()-3);
				String filename = "";
				if (filetype.equals(mFileType)) {
					if (filetype.equals(XML)) {
						mProjectPath = path;
						filename = path.substring(path.indexOf("_")+1, path.length()-4);
					} else {
						filename = path.substring(path.lastIndexOf("/")+1, path.length()-4);
					}

					if ((pName.length() == 0 || filename.contains(pName)) && filename.length() > 0) {
						final String projectPath = mProjectPath;

						Button fileButton = new Button(this);
						Button deleteButton = new Button(this);
						fileButton.setTextAppearance(this, android.R.style.TextAppearance_Medium);
						fileButton.setText(filename);
						//fileButton.setWidth(LinearLayout.LayoutParams.FILL_PARENT);
						fileButton.setWidth(300);
						fileButton.setHeight(7);
						fileButton.setOnClickListener (new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Intent intent;
								intent = new Intent(ImportActivity.this, ProjectPageActivity.class);
								intent.putExtra(ProjectPageActivity.PROJECT_PATH, projectPath);
								if (filetype.equals(PCM))
									intent.putExtra(ProjectPageActivity.IMPORT_CLIP_PATH, path);
								startActivity(intent);
							}
						});

						deleteButton.setBackgroundResource(R.drawable.ic_action_discard);
						deleteButton.setHeight(8);
						deleteButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								showAlertBeforeDelete(new File(path));
							}
						});
						//Log.d("ImportActivity", "adding file to linear layout with index " + numFiles);
						ll.addView(fileButton, numFiles);
						deleteButtonLayout.addView(deleteButton, numFiles++);
					}

					// so far only implemented project selection. 
					// to implement multiple selection for recorded clips, 
					// checkout this link: 
					// http://theopentutorials.com/tutorials/android/listview/android-multiple-selection-listview/
				}

			}
		}
		if (files == null || numFiles == 0) {
			Button b = new Button(this);
			b.setTextAppearance(this, android.R.style.TextAppearance_Medium);
			b.setText("Click here to start a new project");
			b.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					Intent intent = new Intent(ImportActivity.this, ProjectPageActivity.class);
					intent.putExtra(ProjectPageActivity.PROJECT_PATH, "");
					startActivity(intent);
				}
			});
			ll.addView(b, 0);
		}
		fileList.removeAllViews();
		fileList.addView(ll);
		deleteButtons.removeAllViews();
		deleteButtons.addView(deleteButtonLayout);
	}

	private void setupSearchBar() {
		EditText searchBar = (EditText) findViewById(R.id.import_search_bar);
		String hint = "Search ";
		if (mFileType.equals(XML)) {
			hint = hint + "project ";
		} else {
			hint = hint + "clip ";
		}
		hint = hint + "name";
		searchBar.setHint(hint);
		ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content).getParent();

		for (int i = 0; i < viewGroup.getChildCount(); i++){
			setupUI(viewGroup.getChildAt(i));
		}
		searchBar.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				listFiles(s, mFileType);
			}

		});

	}

	public void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}

	public void setupUI(View view) {

		//Set up touch listener for non-text box views to hide keyboard.
		if(!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard();
					return false;
				}

			});
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

				View innerView = ((ViewGroup) view).getChildAt(i);

				setupUI(innerView);
			}
		}
	}

	private void showAlertBeforeDelete(final File pFile) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Delete Clip");
		alert.setMessage("Warning: File delete cannot be undone! Type 'yes' to confirm.");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (input.getText().toString().equals("yes")||input.getText().toString().equals("Yes")){
					pFile.delete();
					listFiles("", mFileType);
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		alert.show();
	}

}


