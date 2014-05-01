package com.example.beatxbeat;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class TranscribePageActivity extends Activity {
	
	ProjectFile project;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcribe_page);
		Bundle extras = this.getIntent().getExtras();
		
		// File that user recorded and the path
//		String result = extras.getString("result");
//		TextView output = (TextView) findViewById(R.id.output);
//		output.setText(result);
		
		try {
			project = new ProjectFile(this, new File(extras.getString(ProjectPageActivity.PROJECT_PATH)), null);
		} catch (Exception e) {
			//all purpose exception catcher
			Log.e("TranscribePageActivity", "project failed to open!");
			e.printStackTrace();
		}
		
		createTranscript();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transcribe_page, menu);
		return true;
	}
	
	private void createTranscript() {
		HashMap<String, String> results = project.getClipResults();
		ScrollView sv = (ScrollView) findViewById(R.id.output);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		int index = 0;
		for (String clipName : results.keySet()) {
			TextView result = new TextView(this);
			result.setText(clipName + ": " + results.get(clipName));
			ll.addView(result, index++);
		}
		sv.removeAllViews();
		sv.addView(ll);
	}

}
