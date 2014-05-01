package com.example.beatxbeat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomePageActivity extends Activity {

	private Button newProject;
	private Button continueProject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_page);

		newProject = (Button) findViewById(R.id.newProjectBtn);
		continueProject = (Button) findViewById(R.id.continueProjectBtn);

		newProject.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HomePageActivity.this, ProjectPageActivity.class);
				intent.putExtra(ProjectPageActivity.PROJECT_PATH, "");
				startActivity(intent);
			}
		});

		continueProject.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HomePageActivity.this, ImportActivity.class);
				intent.putExtra(ImportActivity.FILE_TYPE, ImportActivity.XML);
				startActivity(intent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_page, menu);
		return true;
	}

}
