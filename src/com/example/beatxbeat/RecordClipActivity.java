package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

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
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
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
	private File clip1;
	
	private static String audioFilePath;
	private static String fileName="";
	private static String filePath="";
	private static String result = "";
	private String randomName = "";
	private Chronometer chrono;
	
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
		
		/**
		 * Hide "start recording" button
		 * Enable "stop recording" button
		 * Setup recording and beat detection
		 */
		startRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				beatList = new ArrayList<Double>();
				listen();
			}
		});
		
		stopRecording.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopRecording.setEnabled(false);
				playRecording.setEnabled(true);
				if (isRecording){
					isRecording = false;
					resetRecorder();
					showAlertRecordNaming();
				} else {
					startRecording.setEnabled(false);
					resetRecorder();
				}
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
		
		audioFilePath = this.getFilesDir().getPath() + "/";

		
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
		mPercussionOnsetDetector = new PercussionOnsetDetector(SAMPLE_RATE, minBufferSize/2, this, 80, 10);
		// END STEP 2
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record_clip, menu);
		return true;
	}
	
	
	/**
	 * Set UI elements for recording mode
	 * 
	 */
	private void setupRecordingUI() {
		stopRecording.setEnabled(true);
		playRecording.setEnabled(false);
		startRecording.setEnabled(false);
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.setVisibility(View.VISIBLE);
		chrono.start();
	}

	private void setupReadytoRecordUI() {
		stopRecording.setEnabled(false);
		playRecording.setEnabled(true);
		startRecording.setEnabled(false);
		chrono.setVisibility(View.INVISIBLE);
		chrono.stop();
		chrono.setBase(SystemClock.elapsedRealtime());
	}
	
	public void playAudio () throws IOException
	{
		playRecording.setEnabled(false);
		startRecording.setEnabled(false);
		stopRecording.setEnabled(true);
		if (filePath==null){
			Log.d("plaback", "file not found for playback in RecordClipActivity");
			return;
		}

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
	
	public void showAlertRecordNaming()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Beat x Beat");
		alert.setMessage("Name the clip as");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(randomName);
		input.setSelection(input.getText().length());
		input.setSelectAllOnFocus(true);
		alert.setView(input);
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
		
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (input.getText().toString() != randomName) {
					fileName = input.getText().toString();
					if (!fileName.endsWith(".pcm")){
						fileName = fileName + ".pcm";
					}
					filePath = audioFilePath+fileName;
					File f = new File(filePath);
					clip1.renameTo(f);
				} else {
					fileName = randomName;
				}
				setupReadytoRecordUI();
				File clip = new File(filePath);
				result = generateBeatTime();
				project.addClip(clip, result);
			}
		});
		

		
		AlertDialog alertToShow = alert.create();
		alertToShow.getWindow().setSoftInputMode(
		    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alertToShow.show();
	}
	
	/**
	 * Call method to setup UI state
	 * Start recording on new thread
	 */
	public void listen() {
		setupRecordingUI();
		recorder.startRecording();
		randomName = getRandomName() + ".pcm";
		filePath = audioFilePath+randomName;
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
				clip1 = new File(filePath);
			    try {
			    	 os = new FileOutputStream(clip1);
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
		            Log.d("RecordClipActivity", "Audio file written at path: " + filePath);
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			}
			
		});
		
		listeningThread.start();
	}
	
	private void resetRecorder(){
		if (null != recorder) {
	        isRecording = false;

	        recorder.stop();
	        recorder.release();

	        recorder = null;
	        listeningThread = null;
	    }
		setupReadytoRecordUI();
	}

	@Override
	public void handleOnset(double time, double salience) {
		beatList.add(time);
		final TextView beatDetector = (TextView) findViewById(R.id.beatDetector);
		runOnUiThread(new Runnable() {
			  public void run() {
				  new CountDownTimer(500, 250) {

			     	     public void onTick(long millisUntilFinished) {
			     	    	 beatDetector.setText("Beat!");
			     	     }

			     	     public void onFinish() {
			     	    	 beatDetector.setText("");
			     	     }
					}.start();
			  }
			});
	}
	
	private String generateBeatTime(){
		StringBuilder stringBuilder = new StringBuilder();
		double difference;
		int length;
		int measurePosition = 0;
		for (int i = 1; i < beatList.size(); i++){
			difference = beatList.get(i) - beatList.get(i-1);
			length = (int) (difference * 4);
			stringBuilder.append("C");
			measurePosition++;
			if(measurePosition==4){
				stringBuilder.append("|");
				measurePosition = 0;
			}
			//if rest is longer than 4 time units, add rests and bars
			while(length>=4){
				stringBuilder.append("z" + (4 - measurePosition));
				length -= (4 - measurePosition);
				stringBuilder.append("|");
				measurePosition = 0;
			}
			//if rest is less than 4 time units
			if(length>0){
				//if rest fits inside current measure
				if(length+measurePosition<=4){
					stringBuilder.append("z"+length);
					measurePosition+=length;
				}
				//fill up measure, add bar, then add rest
				else{
					stringBuilder.append("z" + (4 - measurePosition));
					length -= (4 - measurePosition);
					stringBuilder.append("|");
					stringBuilder.append("z"+ length);
					measurePosition = length;
					
				}
			}
		}
		stringBuilder.append("C");
		return stringBuilder.toString();
	}
	
	private String getRandomName() {
		Scanner scanner = null;
		ArrayList<String> dict = new ArrayList<String>();
		try {
			scanner = new Scanner(getAssets().open("dictionary/dict.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(scanner.hasNext()){
			dict.add(scanner.nextLine());
		}
		scanner.close();
		Collections.shuffle(dict);
		return dict.get(0);
	}
}
