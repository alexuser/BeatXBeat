package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.example.beatxbeat.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EditClipActivity extends Activity {
	
	private File pcm;
	private File result;
	private int min = 0;
	private int max = 0;
	private int size = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_clip);		
		
		Intent intent = getIntent();
		String filepath = intent.getStringExtra("filepath");
		pcm = new File(filepath);
		size = (int) pcm.length();
		
		String resultPath = filepath.substring(0,filepath.length()-4)+".txt";
		result = new File(resultPath);
		
		RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, (int) size, this);
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
		        @Override
		        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
		              min = minValue;
		              max = maxValue;
		        }
		});
		
		ViewGroup layout = (ViewGroup) findViewById(R.id.editLayout);
		layout.addView(seekBar);
		
		Button done = (Button) findViewById(R.id.doneEdit);
		done.setOnClickListener(new View.OnClickListener() {         
		    @Override
		    public void onClick(View v)
		    {
		      // some code
		    	trim(pcm, result, min, max);
		      EditClipActivity.this.finish();
		    }
		});
		
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
	
	private void trim(File pFile, File rFile, int beginning, int end) {
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
			int beg = (int) ((double)min/size * charData.length);
			int en = (int) ((double)(max-min)/size * charData.length);
			write.write(charData, beg, en);
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


}
