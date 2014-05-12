package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import android.widget.Toast;
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
	private static String txtPath="";
	private static String result = "";
	private static int minBufferSize = -1;
	private String randomName = "";
	private Chronometer chrono;
	Thread playingThread;

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
		stopRecording.setEnabled(true);
		playRecording.setEnabled(true);
		startRecording.setEnabled(true);

		Intent intent = getIntent();
		String message = intent.getStringExtra(ProjectPageActivity.PROJECT_PATH);
		try {
			project = new ProjectFile(this, new File(message), null);
		} catch (Exception e1) {
			//all purpose exception catcher
			e1.printStackTrace();
		}

		prepareRecordingUI();
		
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

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				if (isRecording){
					isRecording = false;
					resetRecorder();
					showAlertRecordNaming();
				} else {
					resetRecorder();
				}
				setupAfterRecordUI();
			}
		});

		playRecording.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startRecording.setVisibility(View.INVISIBLE);
				stopRecording.setVisibility(View.VISIBLE);
				stopRecording.setText("Stop Playback");
				playRecording.setVisibility(View.INVISIBLE);
				
				playingThread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							playAudio();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							runOnUiThread(new Runnable() {

		                        @Override
		                        public void run() {
		                        	playRecording.setVisibility(View.VISIBLE);
		                        	stopRecording.setVisibility(View.INVISIBLE);
		                        	startRecording.setVisibility(View.VISIBLE);
		                        	stopChrono();
		                        }
		                    });
						}
					}

				});
				playingThread.start();
				stopRecording.setText("Stop Recording");
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
		setupRecorder();
		// END STEP 1

		// STEP 2: create percussion detector
		mPercussionOnsetDetector = new PercussionOnsetDetector(SAMPLE_RATE, minBufferSize/2, this, 80, 10);
		// END STEP 2
		
		Toast.makeText(getApplicationContext(), "Press \"Start Recording\" to record a beat!", Toast.LENGTH_LONG ).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record_clip, menu);
		return true;
	}
	
	private void startChrono(){
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.setVisibility(View.VISIBLE);
		chrono.start();
	}
	
	private void stopChrono(){
		chrono.setVisibility(View.INVISIBLE);
		chrono.stop();
	}

	private void prepareRecordingUI(){
		startRecording.setVisibility(View.VISIBLE);
		stopRecording.setVisibility(View.INVISIBLE);
		playRecording.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Set UI elements for recording mode
	 * 
	 */
	private void setupRecordingUI() {
		startRecording.setVisibility(View.INVISIBLE);
		stopRecording.setVisibility(View.VISIBLE);
		playRecording.setVisibility(View.INVISIBLE);
		startChrono();
	}

	
	private void setupAfterRecordUI(){
		startRecording.setVisibility(View.VISIBLE);
		startRecording.setText("Record Another");
		stopRecording.setVisibility(View.INVISIBLE);
		playRecording.setVisibility(View.VISIBLE);
		playRecording.setText("Play " + fileName);
		stopChrono();
	}

	public void playAudio () throws IOException
	{

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
			runOnUiThread(new Runnable() {

                @Override
                public void run() {
                	startChrono();
                }
			});
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
		RecordClipActivity.fileName = input.getText().toString();
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (input.getText().toString() != randomName) {
					fileName = input.getText().toString();
					if (!fileName.endsWith(".pcm")){
						fileName = fileName + ".pcm";
					}
					filePath = audioFilePath+fileName;
					txtPath = audioFilePath+fileName.substring(0,fileName.length()-4)+".txt";
					File f = new File(filePath);
					clip1.renameTo(f);
				} else {
					fileName = randomName;
				}
				File clip = new File(filePath);
				result = generateBeatTime();
				project.addClip(clip, txtPath);
				PrintWriter writer;
				try {
					writer = new PrintWriter(txtPath, "UTF-8");
					writer.println(result);
					writer.close();
					Log.d("RecordClipActivity", "result written at path: " + txtPath);
				} catch (Exception e) {
					//all purpose exception catcher
					e.printStackTrace();
				} 
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
			setupRecorder();
			
			listeningThread = null;
		}
	}
	
	private void setupRecorder(){
		RecordClipActivity.minBufferSize = AudioRecord.getMinBufferSize(
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
		//add the starting rest
		int start = (int) (beatList.get(0) * 4);
		while (start >= 4) {
			stringBuilder.append("z4|");
			start -= 4;
		}
		if (start != 0) {
			stringBuilder.append("z" + start);
			measurePosition = start;
		}
		//transcribe the rest starting from the first beat
		for (int i = 1; i < beatList.size(); i++){
			difference = beatList.get(i) - beatList.get(i-1);
			length = (int) (difference * 4);
			stringBuilder.append("C");
			measurePosition++;
			//if the measure is finished, add a bar
			if (measurePosition == 4) {
				stringBuilder.append("|");
				measurePosition = 0;
			}
			//if there is enough rest to complete the measure, add rest and then a bar
			if (length + measurePosition >= 4) {
				stringBuilder.append("z" + (4 - measurePosition));
				length -= (4 - measurePosition);
				stringBuilder.append("|");
				measurePosition = 0;
			}
			//if there are full measures of rest, add them
			while (length >= 4) {
				stringBuilder.append("z4|");
				length -= 4;
				measurePosition = 0;
			}
			//add the leftover rest
			if (length > 0) {
				stringBuilder.append("z" + length);
				measurePosition += length;
				if (measurePosition == 4) {
					stringBuilder.append("|");
					measurePosition = 0;
				}
			}	
		}
		stringBuilder.append("C");
		if (measurePosition != 3) {
			stringBuilder.append("z" + (3 - measurePosition));
		}
		stringBuilder.append("|");
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
