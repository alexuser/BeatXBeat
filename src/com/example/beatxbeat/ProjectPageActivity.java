package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ProjectPageActivity extends Activity {

	private Button recordBtn, importBtn, transcribeBtn;
	private TextView projectNameTextView;
	private String projectName;

	private ProjectFile project;
	public final static String PROJECT_PATH = "com.example.beatxbeat.PROJECT_PATH";
	public final static String IMPORT_CLIP_PATH = "com.example.beatxbeat.IMPORT_CLIP_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_page);

		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);

		recordBtn = (Button) findViewById(R.id.newClip);
		importBtn = (Button) findViewById(R.id.importClip);
		transcribeBtn = (Button) findViewById(R.id.transcribeBtn);

		Bundle extras = this.getIntent().getExtras();

		projectNameTextView = (EditText) findViewById(R.id.projectName);

		if (!extras.containsKey(PROJECT_PATH)) {
			Log.e("ProjectPageActivity", "No project path found. How did you get to this page?");
		}

		try {
			if (extras.getString(PROJECT_PATH).length() > 0) {
				Log.d("ProjectPageActivity", "Opening project file at path: " + extras.getString(PROJECT_PATH));
				project = new ProjectFile(this, new File(extras.getString(PROJECT_PATH)), null);
				projectNameTextView.setText(project.getName());
				//TODO need to figure out how to calculate result string for beat


				if (extras.containsKey(IMPORT_CLIP_PATH)) {
					File importedClip = new File(extras.getString(IMPORT_CLIP_PATH));
					BeatTranscriber ct = new BeatTranscriber(importedClip);
					project.addClip(importedClip, ct.getResults());
				}				
			} else {
				showNamingAlert();
				//				while (projectName == null) {
				//					this.wait(1000);
				//				}
				project = new ProjectFile(this, null, projectName);
			}
			project.save();
			projectNameTextView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					projectName = s.toString();
					project.setProjectName(projectName);
				}
			});

			setupClips();
		} catch (Exception e1) {
			//all purpose exception catcher
			e1.printStackTrace();
		}

		/**
		 * Try to transcribe the clips listed in the View, show alert dialog on failure
		 */
		transcribeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(ProjectPageActivity.this, TranscribePageActivity.class);
				try {
					intent.putExtra(PROJECT_PATH, project.getProjectPath());
					startActivity(intent);
				} catch (Exception e) {

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							ProjectPageActivity.this);

					// set title
					alertDialogBuilder.setTitle("Error... :(");

					// set dialog message
					alertDialogBuilder
					.setMessage("Something went wrong, make sure you have clips in your project to transcribe")
					.setCancelable(false)
					.setNeutralButton("Okay",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							//return to the project page
						}
					});

					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();

					// show it
					alertDialog.show();
				}
			}
		});

		importBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProjectPageActivity.this, ImportActivity.class);
				intent.putExtra(ImportActivity.FILE_TYPE, ImportActivity.PCM);
				intent.putExtra(PROJECT_PATH, project.getProjectPath());
				startActivity(intent);
			}
		});

		recordBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProjectPageActivity.this, RecordClipActivity.class);
				intent.putExtra(PROJECT_PATH, project.getProjectPath());
				startActivity(intent);
			}
		});

		
		//Setup the method to allow the keyboard to be hidden when clicking elswhere on screen
		ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content).getParent();

		for (int i = 0; i < viewGroup.getChildCount(); i++){
			setupUI(viewGroup.getChildAt(i));
		}
		
//		projectNameTextView.setOnEditorActionListener(new OnEditorActionListener() {
//
//	        @Override
//	        public boolean onEditorAction(TextView v, int actionId,
//	                KeyEvent event) {
//	            if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//
//	                // NOTE: In the author's example, he uses an identifier
//	                // called searchBar. If setting this code on your EditText
//	                // then use v.getWindowToken() as a reference to your 
//	                // EditText is passed into this callback as a TextView
//
//	                hideSoftKeyboard();
//	               // Must return true here to consume event
//	               return true;
//
//	            }
//	            return false;
//	        }
//	    });
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
			e.printStackTrace();
		}
		// Set and push to audio track..
		int intSize = android.media.AudioTrack.getMinBufferSize(
				44100, 
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT); 
		AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 
				44100, 
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
	}

	/**
	 * Prompts the user to name the project when starting a new project.
	 * Generates a preset name for convenience
	 */
	public void showNamingAlert() {
		AlertDialog.Builder namingAlert = new AlertDialog.Builder(this);

		namingAlert.setTitle("Beat x Beat");
		namingAlert.setMessage("Name your project as");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setSelectAllOnFocus(true);

		Scanner scanner = null;
		ArrayList<String> dict = new ArrayList<String>();
		try {
			scanner = new Scanner(getAssets().open("dictionary/dict.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(scanner.hasNext()){
			dict.add(scanner.nextLine());
		}
		Collections.shuffle(dict);

		input.setText(dict.get(0));
		input.setSelection(input.getText().length());
		namingAlert.setView(input);

		namingAlert.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				projectName = input.getText().toString();
				projectNameTextView.setText(projectName);
			}
		});
		namingAlert.show();
	}

	/**
	 * Sets up the ScrollView in the middle of the page, which should contain
	 * all the clips associated with the current project. If the project does 
	 * not contain any clips, it should display a text box saying "Clips go
	 * here". 
	 */
	private void setupClips() {
		ScrollView clipNames = (ScrollView) findViewById(R.id.clipName);
		ScrollView playButtons = (ScrollView) findViewById(R.id.play_buttons);
		LinearLayout clipLayout = new LinearLayout(this);
		LinearLayout playButtonLayout = new LinearLayout(this);
		clipLayout.setOrientation(LinearLayout.VERTICAL);
		playButtonLayout.setOrientation(LinearLayout.VERTICAL);

		if (!project.getClips().isEmpty()) {
			int index = 0;
			for (final String filepath : project.getClips()) {
				Button clip = new Button(this);
				Button playButton = new Button(this);

				clip.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				playButton.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				String filename = filepath.substring(filepath.lastIndexOf("/")+1);

				Pattern pattern = Pattern.compile("\\S+[.]pcm(\\w+[.]pcm)");
				Matcher matcher = pattern.matcher(filename);
				matcher.find();
				try {
					clip.setText(matcher.group(1));
				} catch (Exception e1) {
					pattern = Pattern.compile("\\/(\\S*.pcm)");
					matcher = pattern.matcher(filename);
					try {
						clip.setText(matcher.group(1));
					} catch (Exception e) {
						e.printStackTrace();
						clip.setText(filename);
					}
					clip.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(ProjectPageActivity.this, EditClipActivity.class);
							intent.putExtra("filepath", filepath);
							startActivity(intent);
						}
					});
				}
				playButton.setText("Play");

				playButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							playAudio(filepath);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

				clipLayout.addView(clip, index);
				playButtonLayout.addView(playButton, index++);
			}
			clipNames.removeAllViews();
			clipNames.addView(clipLayout);
			playButtons.removeAllViews();
			playButtons.addView(playButtonLayout);

		}
	}

	public void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}

	public void setupUI(View view) {

		//Set up touch listener for non-text box views to hide keyboard.
		if(!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard();
					return false;
				}

			});
		}

		//If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

				View innerView = ((ViewGroup) view).getChildAt(i);

				setupUI(innerView);
			}
		}
	}

}
