package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;


public class RecordClipActivity extends Activity implements OnsetHandler{
	
	static final int SAMPLE_RATE = 32000;
	private Button startRecording;
	private Button stopRecording;
	private Button playRecording;
	private Button backbtn;
	private boolean isRecording = false;
	
	private static String audioFilePath;
	private static String fileName="";
	private static String filePath="";
	private static String result = "";
	private Chronometer chrono;
	//private ImageView miclogo;
	
	Thread listeningThread;
	private AudioRecord recorder;
	private byte[] buffer;
	private PercussionOnsetDetector mPercussionOnsetDetector;
	private be.hogent.tarsos.dsp.AudioFormat tarsosFormat;
	private static ArrayList<Double> beatList = new ArrayList<Double>();
	
	private ProjectFile project;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_clip);

		startRecording = (Button) findViewById(R.id.startButton);
		stopRecording = (Button) findViewById(R.id.stopButton);
		playRecording = (Button) findViewById(R.id.playButton);
		backbtn = (Button) findViewById(R.id.backButton);
		chrono = (Chronometer) findViewById(R.id.chronometer);
		
		Intent intent = getIntent();
		String message = intent.getStringExtra(ProjectPageActivity.PROJECT_PATH);
		try {
			project = new ProjectFile(this, new File(message), null);
		} catch (Exception e1) {
			//all purpose exception catcher
			e1.printStackTrace();
		}
		
		startRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showAlertBeforeRecord();
				startRecording.setEnabled(false);
				stopRecording.setEnabled(true);
			}
		});
		
		stopRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopRecording.setEnabled(false);
				playRecording.setEnabled(true);
				if (isRecording){
					startRecording.setEnabled(true);
					isRecording = false;
					displayBeatTime();
					resetRecording();
					File clip = new File(filePath);
					project.addClip(clip, result);
				} else {
					startRecording.setEnabled(false);
					stop();
				}
				beatList = new ArrayList<Double>();
			}
		});
		
		playRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopRecording.setEnabled(true);
				startRecording.setEnabled(false);
				playRecording.setEnabled(false);
				try {
					playAudio();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		backbtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecordClipActivity.this, ProjectPageActivity.class);
                intent.putExtra("filePath", filePath);
                intent.putExtra("fileName", fileName);
                intent.putExtra("result", result);
                intent.putExtra(ProjectPageActivity.PROJECT_PATH, project.getProjectPath());
				startActivity(intent);
			}
		});
		
		audioFilePath = getBaseContext().getExternalFilesDir(null) + "/state.pcm";
				//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()
				//+ "/Beat X Beat/";
		
		// STEP 1: set up recorder... same as in loopback example
		int minBufferSize = AudioRecord.getMinBufferSize(
				SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		buffer = new byte[minBufferSize];
		recorder = new AudioRecord(
				MediaRecorder.AudioSource.MIC,
				SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				minBufferSize);
		// END STEP 1
				
		// STEP 2: create percussion detector
		mPercussionOnsetDetector = new PercussionOnsetDetector(SAMPLE_RATE, minBufferSize/2, this, 60, 10);
		// END STEP 2
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record_clip, menu);
		return true;
	}
	
	
	private void setupRecording() {
		isRecording = true;
		stopRecording.setEnabled(true);
		playRecording.setEnabled(false);
		startRecording.setEnabled(false);
//		miclogo = (ImageView) findViewById(R.id.imageView1);
//		miclogo.setVisibility(View.GONE);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.setVisibility(View.VISIBLE);
		chrono.start();
	}

	private void resetRecording() {
	    // stops the recording activity
		stopRecording.setEnabled(false);
		playRecording.setEnabled(true);
//		ImageView miclogo = (ImageView) findViewById(R.id.imageView1);
//		miclogo.setVisibility(View.VISIBLE);
		chrono.setVisibility(View.INVISIBLE);
		chrono.stop();
		chrono.setBase(SystemClock.elapsedRealtime());
	}
	
	public void playAudio () throws IOException
	{
		playRecording.setEnabled(false);
		startRecording.setEnabled(false);
		stopRecording.setEnabled(true);
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
			e.printStackTrace();
		}
		// Set and push to audio track..
		int intSize = android.media.AudioTrack.getMinBufferSize(
				SAMPLE_RATE, 
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT); 
		AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 
				SAMPLE_RATE, 
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
		} //else
//			Log.d("TCAudio", "audio track is not initialised ");
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
				fileName = input.getText().toString();
				if (!fileName.endsWith(".pcm")){
					fileName = fileName + ".pcm";
				}
				filePath = audioFilePath+fileName;
				listen();
			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
		alert.show();
	}
	
	public void listen() {
		setupRecording();
		recorder.startRecording();
		isRecording = true;
		
		tarsosFormat = new be.hogent.tarsos.dsp.AudioFormat(
						(float)SAMPLE_RATE, // sample rate
						16, // bit depth
						1, // channels
						true, // signed samples?
						false // big endian?
						);
		
		Thread listeningThread = new Thread(new Runnable() {

			@Override
			public void run() {
				FileOutputStream os = null;
				File f = new File(filePath);
			    try {
			    	 os = new FileOutputStream(f);
			    } catch (FileNotFoundException e) {
			        e.printStackTrace();
			    }
				while (isRecording) {
					int bufferReadResult =
							recorder.read(buffer, 0, buffer.length);
					AudioEvent audioEvent =
							new AudioEvent(
									tarsosFormat,
									bufferReadResult);
					audioEvent.setFloatBufferWithByteBuffer(buffer);
					mPercussionOnsetDetector.process(audioEvent);
					try {
			            os.write(buffer, 0, buffer.length);
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
				}
				try {
			        os.close();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			}
			
		});
		
		listeningThread.start();
	}
	
	private void stop(){
		if (null != recorder) {
	        isRecording = false;

	        recorder.stop();
	        recorder.release();

	        recorder = null;
	        listeningThread = null;
	    }
		resetRecording();
	}

	@Override
	public void handleOnset(double time, double salience) {
		//final String s = "Clap at " + String.valueOf(time) + " seconds";
		beatList.add(time);
	}
	
	private void displayBeatTime(){
		StringBuilder stringBuilder = new StringBuilder();
		double difference;
		int rest;
		for (int i = 1; i < beatList.size(); i++){
			difference = beatList.get(i) - beatList.get(i-1);
			difference = Math.round(difference*4)/4d;
			rest = (int)(difference / (double)0.25);
			for (int j = 0; j < rest; j++){
				stringBuilder.append(" -");
			}
			stringBuilder.append(" 0");
			}
		result = stringBuilder.toString();
	}

}
