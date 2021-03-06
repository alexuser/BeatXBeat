package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beatxbeat.RangeSeekBar.OnRangeSeekBarChangeListener;

public class EditClipActivity extends Activity {
	
	static final int SAMPLE_RATE = 32000;
	private File pcm;
	private File result;
	private int min = 0;
	private int max = 0;
	private int size = 0;
	private static Button play;
	private static Button done;
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
	
	private static WebView webView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_clip);		
		
		Intent intent = getIntent();
		String filepath = intent.getStringExtra("filepath");
		TextView clipName = (TextView) findViewById(R.id.clipName);
		Pattern pattern = Pattern.compile("(?i)([\\s\\w]+).pcm");
		Matcher matcher = pattern.matcher(filepath);
		matcher.find();
		try {
			clipName.setText(matcher.group(1));
			title = matcher.group(1);
		} 
		catch (Exception e1) {
			clipName.setText(filepath);
			title = filepath;
		}
		
		
		pcm = new File(filepath);
		size = (int) pcm.length();
		max = size;
		
		String resultPath = filepath.substring(0,filepath.length()-4)+".txt";
		result = new File(resultPath);
		
		
		RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, size, this);
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
		        @Override
		        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
		            min = minValue;
		            max = maxValue;
		            EditClipActivity.play.setVisibility(View.VISIBLE);
		            EditClipActivity.done.setVisibility(View.VISIBLE);
		              
					refreshWebView();
		        }
		});
		
		play = (Button) findViewById(R.id.playTrimmed);
		ViewGroup layout = (ViewGroup) findViewById(R.id.editLayout);
		layout.addView(seekBar);
		
		;
		play.setOnClickListener(new View.OnClickListener() {         
		    @Override
		    public void onClick(View v)
		    {
		    	try {
					playAudio(pcm, min, max);
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		});
		
		done = (Button) findViewById(R.id.doneEdit);
		done.setOnClickListener(new View.OnClickListener() {         
		    @Override
		    public void onClick(View v)
		    {
		    	showAlertBeforeSave(pcm, result, min, max);
		    }
		});
		Toast.makeText(getApplicationContext(), "Press the Android back button to return without trimming.", Toast.LENGTH_LONG ).show();
		play.setVisibility(View.INVISIBLE);
		done.setVisibility(View.INVISIBLE);
		
		this.webView = (WebView)findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("file:///android_asset/www/index.html");
		refreshWebView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_clip, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void trim(File pFile, File rFile) {
		//create temporary files
		String tempPCM = this.getFilesDir().getPath().toString() + "/temporary.pcm";
		File trimmedPCM = new File(tempPCM);
		String tempResult = this.getFilesDir().getPath().toString() + "/temporary.txt";
		File trimmedResult = new File(tempResult);
		//set up the audio trimming
		byte[] byteData = null;
		byteData = new byte[(int) pFile.length()];
		FileInputStream in = null;
		FileOutputStream out = null;
		//set up the transcription trimming
		char[] charData = null;
		charData = new char[(int) rFile.length()];
		FileReader read = null;
		FileWriter write = null;
		
		try {
			//trim the audio file
			in = new FileInputStream(pFile);
			in.read(byteData);
			in.close(); 
			out = new FileOutputStream(trimmedPCM);
			out.write(byteData, 2*(min/2), 2*((max - min)/2));
			out.flush();
			out.close();
			trimmedPCM.renameTo(pFile);		
			//trim the transcription
			read = new FileReader(rFile);
			read.read(charData);
			read.close();
			trimmedResult.createNewFile();
			write = new FileWriter(trimmedResult);
			double beg = ((double)min)/size;
			double en = ((double)max)/size;
			String trimmed = getTrimmed(charData, beg, en);
			write.write(trimmed);
			write.flush();
			write.close();
			trimmedResult.renameTo(rFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getTrimmed(char[] charArray, double beg, double end) {
		ArrayList<Character> temp = new ArrayList<Character>();
		ArrayList<Character> trimmed = new ArrayList<Character>();
		String trimmedResult = "";
		//convert the result into notes and rests
		for (char c: charArray) {
			if (c == 'C') {
				temp.add('C');
			}
			else if (c == '1') {
				temp.add('z');
			}
			else if (c == '2') {
				temp.add('z');
				temp.add('z');
			}
			else if (c == '3') {
				temp.add('z');
				temp.add('z');
				temp.add('z');
			}
			else if (c == '4') {
				temp.add('z');
				temp.add('z');
				temp.add('z');
				temp.add('z');
			}
		}
		int pos = 0;
		int count = 0;
		//add the notes and rests and bars for the trimmed region
		for (int i = (int) (beg * temp.size()); i < (int) (end * temp.size()); i++) {
			System.out.println(i);
			if (temp.get(i) == 'z') {
				count++;
				pos++;
				if (pos == 4) {
					trimmed.add('z');
					trimmed.add((char) (((int) '0') + count));
					trimmed.add('|');
					pos = 0;
					count = 0;
				}
			} 
			else if (temp.get(i) == 'C') {
				if (count != 0) {
					trimmed.add('z');
					trimmed.add((char) (((int) '0') + count));
					count = 0;
				}
				trimmed.add('C');
				pos++;
				if (pos == 4) {
					trimmed.add('|');
					pos = 0;
				}
			}
		}
		if (pos != 0) {
			trimmed.add('z');
			trimmed.add((char) (((int) '0') + (4 - pos + count)));
			trimmed.add('|');
		}
		//convert the result to a string
		for (char c: trimmed) {
			trimmedResult += c;
		}
		return trimmedResult;
	}

	/**
	 * Plays the audio file at the given file path.
	 * 
	 * @param filePath Path of the audio file.
	 * @throws IOException
	 */
	public void playAudio (File pFile, int beg, int end) throws IOException
	{

		byte[] byteData = null; 
		byteData = new byte[(int) pFile.length()];
		FileInputStream in = null;
		try {
			in = new FileInputStream( pFile );
			in.read(byteData);
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
			at.write(byteData, 2 *(beg/2), 2*((end - beg)/2)); 
			at.stop();
			at.release();
		}
	}
	
	private void refreshWebView(){
		char[] charData = null;
		charData = new char[(int) pcm.length()];
		FileReader read;
		try {
			read = new FileReader(result);
			read.read(charData);
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		double beg = ((double)min)/size;
		double en = ((double)max)/size;
		beats = "[V:0]" + getTrimmed(charData,beg,en);
        voices = "V:0 clef=perc \\n";
		abcString = "X:1 \\n"
		+ "T: " + title + " \\n"
		+ "Q:240\\n "
		+ "L:1/4\\n"
		+ voices
		+ beats;
          
        webView.loadUrl("javascript:ABCJS.renderAbc(\"canvas\","
            		+ "' " + abcString +  " ', "
            		+ "{},"
            		+ " {'editable': false, 'staffwidth': 400, 'scale': 0.7 },"
            		+ " {});");
	}
	
	private void showAlertBeforeSave(final File pcm, final File result, final int min, final int max) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Delete Clip");
		alert.setMessage("Warning: Trim cannot be undone! Press 'Continue' to confirm.");


		alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				trim(pcm,result);
				EditClipActivity.this.finish();
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
