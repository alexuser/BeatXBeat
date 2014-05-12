package com.example.beatxbeat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class TranscribePageActivity extends Activity {
	
	ProjectFile project;

	private String title = "Sample Beat";
	private String beats = "[V:one] CDC \\n"
			+ "[V:two] CDC";
	private String voices = "V:one clef=perc name = \"Samp1\"  \\n V:two clef = perc name = \"Samp2\" \\n";
	private String abcString = "X:1 \\n"
			+ "T: " + title + " \\n"
			+ "Q:240\\n "
			+ "L:1/4\\n"
			+ voices
			+ beats;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcribe_page);
		Bundle extras = this.getIntent().getExtras();
		
		Button share = (Button) findViewById(R.id.share);
		
		share.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				shareTranscript((WebView)findViewById(R.id.webView1));
			}
		});
		
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
		Pattern pattern;
		Matcher matcher;
		for (String clipName : results.keySet()) {
			pattern = Pattern.compile("(?i)([\\s\\w]+).pcm");
			matcher = pattern.matcher(clipName);
			matcher.find();
			tempBeats = tempBeats + "[V:" + index + "]" + results.get(clipName) + "\\n";
			
			try {
				tempVoices = tempVoices + "V:" + index + " clef=perc name = \"" + matcher.group(1) + "\" \\n";
			} 
			catch (Exception e) {
				tempVoices = tempVoices + "V:" + index + " clef=perc name = \"" + clipName + "\" \\n";
			}
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
				+ "Q:240\\n "
				+ "L:1/4\\n"
				+ voices
				+ beats;

	}

	
	/**
	 * Starts the share intent to share webview as Bitmap
	 */
	@SuppressWarnings("deprecation")
	private void shareTranscript(WebView webView){
		Picture picture = webView.capturePicture();
		Bitmap bitMap = pictureDrawable2Bitmap(picture);
		String pathofBmp = Images.Media.insertImage(getContentResolver(), bitMap,"Transcript", null);
	    Uri bmpUri = Uri.parse(pathofBmp);
	    final Intent emailIntent1 = new Intent(     android.content.Intent.ACTION_SEND);
	    emailIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    emailIntent1.putExtra(Intent.EXTRA_STREAM, bmpUri);
	    emailIntent1.setType("image/png");
	    startActivity(emailIntent1);
	}
	
	/**
	 * Converts picture to bitmap
	 * @param picture
	 * @return bitmap
	 */
	private static Bitmap pictureDrawable2Bitmap(Picture picture) {
	    PictureDrawable pd = new PictureDrawable(picture);
	    Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    canvas.drawPicture(pd.getPicture());
	    return bitmap;
	}
}
