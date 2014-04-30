package com.example.beatxbeat;

import java.io.File;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ImportProjectActivity extends Activity {

	private static final String XML = "xml";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_project);
		
		listFiles();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_select, menu);
		return true;
	}
	
	
	private void listFiles() {
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
					// TODO Auto-generated method stub
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

}
