package com.example.beatxbeat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class TranscribePageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcribe_page);
		Bundle extras = this.getIntent().getExtras();
		
		// File that user recorded and the path
		String result = extras.getString("result");
		TextView output = (TextView) findViewById(R.id.output);
		output.setText(result);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transcribe_page, menu);
		return true;
	}

}
