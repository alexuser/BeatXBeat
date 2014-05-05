package com.example.beatxbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
		
		//Convert pcm to wav
		File pcm = new File(filepath);
		System.out.println(pcm);
//		byte[] data = new byte[(int) pcm.length()];
//		FileInputStream fileInputStream;
//		try {
//			fileInputStream = new FileInputStream(pcm);
//			fileInputStream.read(data);
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		File wav = null;
		try {
			PcmAudioHelper.convertRawToWav(new WavAudioFormat(44100, 16, 1, true), pcm, wav);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(wav);
		
		//Draw waveform 
		Wave wave = new Wave();
		System.out.println(wave);
		wave.leftTrim(1);
		wave.rightTrim(0.5F);
		System.out.println(wave);
		WaveFileManager waveFileManager=new WaveFileManager(wave);
		String wavPath = this.getExternalFilesDir(null).getPath() + "/waveform.wav";
		waveFileManager.saveWaveAsFile(wavPath);
//		GraphicRender render=new GraphicRender();
//		render.setHorizontalMarker(1);
//		render.setVerticalMarker(1);
//		String jpgPath = this.getExternalFilesDir(null).getPath() + "/waveform.jpg";
//		render.renderWaveform(wave, jpgPath);
//		ImageView waveform = (ImageView) findViewById(R.id.waveform);
//		File jpg = new File(jpgPath);
//		Bitmap myBitmap = BitmapFactory.decodeFile(jpg.getAbsolutePath());
//	    waveform.setImageBitmap(myBitmap);
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
