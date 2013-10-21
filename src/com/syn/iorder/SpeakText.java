package com.syn.iorder;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class SpeakText implements TextToSpeech.OnInitListener {
	private TextToSpeech tts;
	private String textToSpeech;
	
	public SpeakText(Context c, String textToSpeech){
		tts = new TextToSpeech(c, this);
		this.textToSpeech = textToSpeech;
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			 
            int result = tts.setLanguage(Locale.US);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speak();
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
	}

	public void speak(){
        tts.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null);
	}
}
