package com.example.beatxbeat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ProjectPageActivity extends Activity {
	
	private Button recordBtn, importBtn, transcribeBtn;
	private TextView clipName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_page);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		recordBtn = (Button) findViewById(R.id.newClip);
		importBtn = (Button) findViewById(R.id.importClip);
		transcribeBtn = (Button) findViewById(R.id.transcribe);
		clipName = (TextView) findViewById(R.id.clipName);
		
		Bundle extras = this.getIntent().getExtras();
		
		// File that user recorded and the path
		String fileName = extras.getString("fileName");
		String filePath = extras.getString("filePath");
		
		
		if (filePath != null && !filePath.isEmpty()){
			clipName.setText(fileName);
		}
		
		transcribeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ProjectPageActivity.this, TranscribePageActivity.class);
                startActivity(intent);
			}
		});
		
		importBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ProjectPageActivity.this, ImportProjectActivity.class);
                startActivity(intent);
			}
		});
		
		recordBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ProjectPageActivity.this, RecordClipActivity.class);
                startActivity(intent);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_page, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    Intent myIntent = new Intent(getApplicationContext(), HomePageActivity.class);
	    startActivityForResult(myIntent, 0);
	    return true;

	}

}
