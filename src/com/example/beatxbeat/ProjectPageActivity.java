package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ProjectPageActivity extends Activity {
	
	private Button recordBtn, importBtn, transcribeBtn, play;
	private TextView clipName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_page);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		recordBtn = (Button) findViewById(R.id.newClip);
		importBtn = (Button) findViewById(R.id.importClip);
		transcribeBtn = (Button) findViewById(R.id.transcribeBtn);
		play = (Button) findViewById(R.id.play);
		clipName = (TextView) findViewById(R.id.clipName);
		
		Bundle extras = this.getIntent().getExtras();
		
		// File that user recorded and the path
		String fileName = extras.getString("fileName");
		final String filePath = extras.getString("filePath");
		final String result = extras.getString("result");
		
		if (result == null || result.isEmpty()){
			transcribeBtn.setVisibility(View.GONE);
		}
		
		
		if (filePath != null && !filePath.isEmpty()){
			clipName.setText(fileName);
		}

		
		transcribeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ProjectPageActivity.this, TranscribePageActivity.class);
				intent.putExtra("result", result);
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
		
		play.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					playAudio(filePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	public void playAudio (String filePath) throws IOException
	{
		if (filePath==null)
			return;

		//Reading the file..
		byte[] byteData = null; 
		File file = null; 
		file = new File(filePath); 
		byteData = new byte[(int) file.length()];
		FileInputStream in = null;
		try {
			in = new FileInputStream( file );
			in.read( byteData );
			in.close(); 

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Set and push to audio track..
		int intSize = android.media.AudioTrack.getMinBufferSize(
				32000, 
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT); 
		AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 
				32000, 
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, 
				intSize, 
				AudioTrack.MODE_STREAM); 
		if (at!=null) { 
			at.play();
			// Write the byte array to the track
			at.write(byteData, 0, byteData.length); 
			at.stop();
			at.release();
		}
		else
			Log.d("TCAudio", "audio track is not initialised ");
	}

}
