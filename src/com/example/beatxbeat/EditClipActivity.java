package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;
import com.musicg.wave.WaveHeader;

import simplesound.pcm.PcmAudioHelper;
import simplesound.pcm.WavAudioFormat;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.os.Build;

public class EditClipActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_clip);
		
		Intent intent = getIntent();
		String filepath = intent.getStringExtra("filepath");
		File pcm = new File(filepath);
		String resultPath = filepath.substring(0,filepath.length()-4)+".txt";
		File result = new File(resultPath);
		
		String tempPCM = this.getFilesDir().getPath().toString() + "/temporary.pcm";
		File trimmedPCM = new File(tempPCM);
		String tempResult = this.getFilesDir().getPath().toString() + "/temporary.txt";
		File trimmedResult = new File(tempResult);
		
		byte[] byteData = null;
		byteData = new byte[(int) pcm.length()];
		FileInputStream in = null;
		FileOutputStream out = null;
		
		char[] charData = null;
		charData = new char[(int) result.length()];
		FileReader read = null;
		FileWriter write = null;
		
		try {
			in = new FileInputStream(pcm);
			in.read(byteData);
			in.close(); 
			out = new FileOutputStream(trimmedPCM);
			out.write(byteData, 0, byteData.length/2);
			out.flush();
			out.close();
			trimmedPCM.renameTo(pcm);
			
			read = new FileReader(result);
			read.read(charData);
			read.close();
			write = new FileWriter(trimmedResult);
			write.write(charData, 0, charData.length/2);
			write.flush();
			write.close();
			trimmedResult.renameTo(result);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Button done = (Button) findViewById(R.id.doneEdit);
		done.setOnClickListener(new View.OnClickListener() {         
		    @Override
		    public void onClick(View v)
		    {
		      // some code
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


}
