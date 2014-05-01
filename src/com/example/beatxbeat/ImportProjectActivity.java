package com.example.beatxbeat;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ImportProjectActivity extends Activity {

	private static final String XML = "xml";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_project);
		
		setupSearchBar();
		listFiles("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.import_project, menu);
		return true;
	}
	
	private void listFiles(CharSequence pName) {
		ScrollView fileList = (ScrollView) findViewById(R.id.file_select_scrollview);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);

		File[] files = this.getFilesDir().listFiles();
		int numFiles = 0;
		if (files != null) {
			for (File file : files) {
				final String path = file.getAbsolutePath();
				String filetype = path.substring(path.length()-3);
				String filename = "";
				if (filetype.equals(XML)) { //Project file (xml)
					filename = path.substring(path.indexOf("_")+1);
					if (pName.length() == 0 || filename.contains(pName)) {
						Button b = new Button(this);
						b.setTextAppearance(this, android.R.style.TextAppearance_Medium);
						b.setText(filename);
						b.setWidth(1000);
						b.setHeight(8);
						b.setOnClickListener (new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Intent intent;
								intent = new Intent(ImportProjectActivity.this, ProjectPageActivity.class);
								intent.putExtra(ProjectPageActivity.PROJECT_PATH, path);
								startActivity(intent);
							}
						});
						ll.addView(b, numFiles++);
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
					Intent intent = new Intent(ImportProjectActivity.this, ProjectPageActivity.class);
					intent.putExtra(ProjectPageActivity.PROJECT_PATH, "");
					startActivity(intent);
				}
			});
			ll.addView(b, numFiles++);
		}
		fileList.removeAllViews();
		fileList.addView(ll);
	}

	private void setupSearchBar() {
		EditText searchBar = (EditText) findViewById(R.id.import_proj_search_bar);
		searchBar.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				listFiles(s);
			}
			
		});
	}
}
