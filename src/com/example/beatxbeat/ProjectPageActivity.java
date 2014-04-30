package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ActionBar;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ProjectPageActivity extends Activity {

	private Button recordBtn, importBtn, transcribeBtn, play;
	private TextView clipName, projectNameTextView;
	private String projectName;

	private ProjectFile project;
	public final static String PROJECT_PATH = "com.example.beatxbeat.PROJECT_PATH";

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
		//clipName = (TextView) findViewById(R.id.clipName);


		Bundle extras = this.getIntent().getExtras();

		// File that user recorded and the path
		String fileName = extras.getString("fileName");
		final String filePath = extras.getString("filePath");
		final String result = extras.getString("result");

		//		if (result == null || result.isEmpty()){
		//			transcribeBtn.setVisibility(View.GONE);
		//		}
		//		
		//		
		//		if (filePath != null && !filePath.isEmpty()){
		//			clipName.setText(fileName);
		//		}

		projectNameTextView = (TextView) findViewById(R.id.projectName);

		try {
			if (extras.containsKey(PROJECT_PATH)) {
				project = new ProjectFile(this, new File(extras.getString(PROJECT_PATH)), null);
				projectNameTextView.setText(project.getName());
			} else {
				showNamingAlert();
//				while (projectName == null) {
//					this.wait(1000);
//				}
				project = new ProjectFile(this, null, projectName);
				project.save();
			}
			
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
				intent.putExtra(PROJECT_PATH, project.getProjectPath());
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

	/**
	 * Prompts the user to name the project when starting a new project.
	 */
	public void showNamingAlert() {
		AlertDialog.Builder namingAlert = new AlertDialog.Builder(this);

		namingAlert.setTitle("Beat x Beat");
		namingAlert.setMessage("Name your project as");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
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
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);

		if (!project.getClips().isEmpty()) {
			int index = 0;
			for (String filename : project.getClips()) {
				TextView a = new TextView(this);
				a.setTextAppearance(this, android.R.style.TextAppearance_Medium);
				a.setText(filename);
				ll.addView(a, index++);
			}
			clipNames.removeAllViews();
			clipNames.addView(ll);
		}
	}
	
	

}
