package com.example.beatxbeat;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;

public class RecordClipActivity extends Activity {
	
	static final int SAMPLE_RATE = 16000;
	private Button startRecording;
	private Button stopRecording;
	private Button playRecording;
	private Button backbtn;
	private boolean isRecording = false;
	
	private static MediaRecorder mediaRecorder;
	private static MediaPlayer mediaPlayer;
	private static String audioFilePath;
	private static String fileName="";
	private static String filePath="";
	private Chronometer chrono;
	private ImageView miclogo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_clip);
		
		startRecording = (Button) findViewById(R.id.startButton);
		stopRecording = (Button) findViewById(R.id.stopButton);
		playRecording = (Button) findViewById(R.id.playButton);
		backbtn = (Button) findViewById(R.id.backButton);
		chrono = (Chronometer) findViewById(R.id.chronometer);
		
		startRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showAlertBeforeRecord();
			}
		});
		
		stopRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopRecording();
			}
		});
		
		playRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					playAudio();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		backbtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(RecordClipActivity.this, ProjectPageActivity.class);
                intent.putExtra("filePath", filePath);
                intent.putExtra("fileName", fileName);
				startActivity(intent);
			}
		});
		
		audioFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()
				+ "/Beat X Beat/";
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record_clip, menu);
		return true;
	}
	
	
	private void startRecording() {
		isRecording = true;
		stopRecording.setEnabled(true);
		playRecording.setEnabled(false);
		startRecording.setEnabled(false);
		miclogo = (ImageView) findViewById(R.id.imageView1);
		miclogo.setVisibility(View.GONE);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.setVisibility(View.VISIBLE);
		chrono.start();
		try {
		     mediaRecorder = new MediaRecorder();
		     mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		     mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		     mediaRecorder.setOutputFile(filePath);
		     mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		     mediaRecorder.prepare();
		} catch (Exception e) {
			   e.printStackTrace();
		}

		mediaRecorder.start();
	   
	}

	private void stopRecording() {
	    // stops the recording activity
		stopRecording.setEnabled(false);
		playRecording.setEnabled(true);
		ImageView miclogo = (ImageView) findViewById(R.id.imageView1);
		miclogo.setVisibility(View.VISIBLE);
		chrono.setVisibility(View.INVISIBLE);
		chrono.stop();
		chrono.setBase(SystemClock.elapsedRealtime());
			
		if (isRecording)
		{	startRecording.setEnabled(false);
			mediaRecorder.stop();
			mediaRecorder.release();
			mediaRecorder = null;
			isRecording = false;
			miclogo = (ImageView) findViewById(R.id.imageView1);
			miclogo.setVisibility(View.VISIBLE);
		} else {
			mediaPlayer.release();
		    mediaPlayer = null;
			startRecording.setEnabled(true);
		}
	}
	
	public void playAudio () throws IOException
	{
		playRecording.setEnabled(false);
		startRecording.setEnabled(false);
		stopRecording.setEnabled(true);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(filePath);
		mediaPlayer.prepare();
		mediaPlayer.start();
	}
	
	public void showAlertBeforeRecord()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Beat x Beat");
		alert.setMessage("Name the clip as");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				fileName = input.getText().toString();
				if (!fileName.endsWith(".amr")){
					fileName = fileName + ".amr";
				}
				filePath = audioFilePath+fileName;
				startRecording();
				
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
