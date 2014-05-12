package com.example.beatxbeat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import android.graphics.Color;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ProjectPageActivity extends Activity {

	private Button recordBtn, importBtn, transcribeBtn;
	private TextView projectNameTextView;
	private String projectName;
	static final int SAMPLE_RATE = 32000;

	private ProjectFile project;
	public final static String PROJECT_PATH = "com.example.beatxbeat.PROJECT_PATH";
	public final static String IMPORT_CLIP_PATH = "com.example.beatxbeat.IMPORT_CLIP_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_page);

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
				if (extras.containsKey(IMPORT_CLIP_PATH)) {
					File importedClip = new File(extras.getString(IMPORT_CLIP_PATH));
					String textPath = importedClip.getPath();
					textPath = textPath.substring(0, textPath.length()-4) + ".txt";
					Log.d("ProjectPageActivity", "Importing clip at path: " + extras.getString(IMPORT_CLIP_PATH));
					Log.d("ProjectPageActivity", "Importing result string at path: " + textPath);
					project.addClip(importedClip, textPath);
				}				
			} else {
				showNamingAlert();
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

				if(!project.getClips().isEmpty()){
					intent.putExtra(PROJECT_PATH, project.getProjectPath());
					startActivity(intent);
					}
				else{
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProjectPageActivity.this);
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

	/**
	 * Plays the audio file at the given file path.
	 * 
	 * @param filePath Path of the audio file.
	 * @throws IOException
	 */
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

		//Forces the soft keyboard to show up while naming
		AlertDialog alertToShow = namingAlert.create();
		alertToShow.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alertToShow.show();
	}

	/**
	 * Sets up the ScrollView in the middle of the page, which should contain
	 * all the clips associated with the current project. If the project does 
	 * not contain any clips, it should display a text box saying "Clips go
	 * here". 
	 */
	private void setupClips() {
		ScrollView clipNames = (ScrollView) findViewById(R.id.clipName);
		RelativeLayout options = (RelativeLayout) findViewById(R.id.scroll_linear_layout);

		if (!project.getClips().isEmpty()) {
			options.removeAllViews();
			int index = 0;
			ArrayList<Button> clips = new ArrayList<Button>();
			ArrayList<Button> playButtons = new ArrayList<Button>();
			for (final String filepath : project.getClips()) {
				Button clip = new Button(this);
				Button playButton = new Button(this);
				Button deleteButton = new Button(this);

				clip.setId(index+1);
				clip.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				clip.setWidth(300);
				//clip.setHeight(7);
				clip.setBackgroundColor(R.drawable.button_border);
				//clip.setTextColor(Color.WHITE);
				//playButton.setText("Play");
				playButton.setId(playButton.hashCode());
				playButton.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				playButton.setBackgroundResource(R.drawable.ic_action_play);
				playButton.setTextColor(Color.WHITE);
				String filename = filepath.substring(filepath.lastIndexOf("/")+1);
				deleteButton.setBackgroundResource(R.drawable.ic_action_discard);
				//deleteButton.setHeight(8);

				Pattern pattern = Pattern.compile("(?i)([\\s\\w]+).pcm");
				Matcher matcher = pattern.matcher(filename);
				matcher.find();
				try {
					clip.setText(matcher.group(1));
				} 
				catch (Exception e1) {
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
				RelativeLayout.LayoutParams clipLP = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				if (index > 0) {
					clipLP.addRule(RelativeLayout.BELOW, clips.get(index-1).getId());
					options.addView(clip, clipLP);
				} else {
					options.addView(clip);
				}
				clips.add(clip);
								
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

				RelativeLayout.LayoutParams playLP = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				playLP.addRule(RelativeLayout.RIGHT_OF, clip.getId());
				if (index > 0) {
					playLP.addRule(RelativeLayout.BELOW, clips.get(index-1).getId());
				} 
				options.addView(playButton, playLP);
				playButtons.add(playButton);
				
				deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showAlertBeforeDelete(new File(filepath));
					}
				});
				//Log.d("ImportActivity", "adding file to linear layout with index " + numFiles);
				RelativeLayout.LayoutParams deleteLP = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				deleteLP.addRule(RelativeLayout.RIGHT_OF, playButton.getId());
				if (index > 0) {
					deleteLP.addRule(RelativeLayout.BELOW, clips.get(index-1).getId());
				} 
				index++;
				options.addView(deleteButton, deleteLP);
			}
			clipNames.removeAllViews();
			clipNames.addView(options);
		} else {
			clipNames.removeAllViews();
			clipNames.addView(createFillerTextView());
		}
	}
	
	/**
	 * Instantiates and sets up the filler TextView that appears when
	 * the project has no clips in it.
	 * @return TextView that says "Recordings Go Here"
	 */
	private TextView createFillerTextView() {
		TextView tv = new TextView(this);
		tv.setId(R.id.recording_filler);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 38, 0, 0);
		tv.setLayoutParams(params);
		tv.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		tv.setText("Recordings Go Here");
		return tv;
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


	private void showAlertBeforeDelete(final File pFile) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Delete Clip From Project");
		alert.setMessage("Warning: Delete cannot be undone! Press 'Continue' to confirm.");

		alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				project.removeClip(pFile, false);
				project.save();
				setupClips();
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
