package com.example.beatxbeat;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class TranscribePageActivity extends Activity {
	
	ProjectFile project;

	private String title = "Sample Beat";
	private String beats = "[V:one] CDC \\n"
			+ "[V:two] CDC";
	private String voices = "V:one clef=perc name = \"Samp1\"  \\n V:two clef = perc name = \"Samp2\" \\n";
	private String abcString = "X:1 \\n"
			+ "T: " + title + " \\n"
			+ "Q:60\\n "
			+ "L:1/4\\n"
			+ voices
			+ beats;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcribe_page);
		Bundle extras = this.getIntent().getExtras();
		
		
		try {
			project = new ProjectFile(this, new File(extras.getString(ProjectPageActivity.PROJECT_PATH)), null);
		} catch (Exception e) {
			//all purpose exception catcher
			Log.e("TranscribePageActivity", "project failed to open!");
			e.printStackTrace();
		}
		
		final WebView webView = (WebView)findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() 
        {
        	public void onPageFinished(WebView view, String url)
            {
        		generateTranscript();
        		
                webView.loadUrl("javascript:ABCJS.renderAbc(\"canvas\","
                		+ "' " + abcString +  " ', "
                		+ "{},"
                		+ " {'editable': false, 'staffwidth': 400, 'scale': 0.7 },"
                		+ " {});");
            }
        }
            );
		
		webView.loadUrl("file:///android_asset/www/index.html");
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transcribe_page, menu);
		return true;
	}
	
	
	/**
	 * Generates the appropriate string to pass to the ABCJS rendering engine from project Clips
	 * Precondition: class variables title, beats, voices, abcString exist
	 * Postcondition: abcString is generated
	 */
	private void generateTranscript() {
		HashMap<String, String> results = project.getClipResults();

		int index = 0;
		String tempBeats ="";
		String tempVoices = "";
		for (String clipName : results.keySet()) {

			tempBeats = tempBeats + "[V:" + index + "]" + results.get(clipName) + "\\n";
			tempVoices = tempVoices + "V:" + index + " clef=perc name = \"" + clipName + "\" \\n";
			index++;
		}
		
		if(index>0){
			tempBeats = tempBeats.substring(0,tempBeats.length()-3);
		}
		title = project.getName();
		beats = tempBeats;
		voices = tempVoices;
		abcString = "X:1 \\n"
				+ "T: " + title + " \\n"
				+ "Q:60\\n "
				+ "L:1/4\\n"
				+ voices
				+ beats;

	}

}
