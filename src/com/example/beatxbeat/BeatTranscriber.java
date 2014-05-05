package com.example.beatxbeat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;

public class BeatTranscriber implements OnsetHandler{

	static final int SAMPLE_RATE = 44100;
	Thread listeningThread;
	private byte[] buffer;
	private PercussionOnsetDetector mPercussionOnsetDetector;
	private be.hogent.tarsos.dsp.AudioFormat tarsosFormat;
	private static ArrayList<Double> beatList = new ArrayList<Double>();
	File mClip;

	public BeatTranscriber(File pClip) {
		int minBufferSize = AudioRecord.getMinBufferSize(
				SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		buffer = new byte[minBufferSize];
		mPercussionOnsetDetector = new PercussionOnsetDetector(SAMPLE_RATE, minBufferSize/2, this, 60, 10);

		tarsosFormat = new be.hogent.tarsos.dsp.AudioFormat(
				(float)SAMPLE_RATE, // sample rate
				16, // bit depth
				1, // channels
				true, // signed samples?
				false // big endian?
				);
		mClip = pClip;

	}

	@Override
	public void handleOnset(double time, double salience) {
		beatList.add(time);
	}

	private String displayBeatTime(ArrayList<Double> pBeatList){
		StringBuilder stringBuilder = new StringBuilder();
		double difference;
		int length;
		int measurePosition = 0;
		for (int i = 1; i < pBeatList.size(); i++){
			difference = pBeatList.get(i) - pBeatList.get(i-1);
			length = (int) (difference * 4);
			stringBuilder.append("C");
			measurePosition++;
			if(measurePosition==4){
				stringBuilder.append("|");
				measurePosition = 0;
			}
			while(length>=4){
				stringBuilder.append("z" + (4 - measurePosition));
				length-=(4 - measurePosition);
				measurePosition = 0;
				stringBuilder.append("|");
			}
			stringBuilder.append("z"+length);
			measurePosition+=length;
		}
		return stringBuilder.toString();
	}

	public String getResults() {
		int size = (int) mClip.length();
		byte[] bytes = new byte[size*2];
		int bufferReadResult = 0;
		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(mClip));
			bufferReadResult = buf.read(bytes, 0, bytes.length);   
			buf.close();
		} catch (Exception e) {
			//all purpose exception catcher
			e.printStackTrace();
		}
		AudioEvent audioEvent =
				new AudioEvent(
						tarsosFormat,
						bufferReadResult);
		audioEvent.setFloatBufferWithByteBuffer(buffer);
		mPercussionOnsetDetector.process(audioEvent);
		return displayBeatTime(beatList);
	}
}
