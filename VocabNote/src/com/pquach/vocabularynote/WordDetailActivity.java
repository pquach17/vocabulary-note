package com.pquach.vocabularynote;



import java.util.Locale;

import com.pquach.vocabularynote.R;

import android.app.AlertDialog;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WordDetailActivity extends ActionBarActivity{
	
	long mId;
	TextView tv_word ;
	TextView tv_word_type;
	TextView tv_definition ;
	TextView tv_example  ;
	TextView label_definition;
	TextView label_example;
	ImageView btn_pronounce;
	private TextToSpeech mTts; 
	private static final  int MY_DATA_CHECK_CODE = 1;
	private static boolean MISSING_LANGUAGE = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_word_detail);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//
		//------Check Text-To-Speech engine availability
		checkTTtsAvailability();
		
		// -----Enable navigation arrow on action bar-----
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
			
		//------Get word id from previous activity-----------
		mId = getIntent().getLongExtra("id", -1); // id = -1 if there is no value passed to id from previous activity
		
		//-------Bind data to controls------------
		Word word = new Word();
		WordDataSource wordds = new WordDataSource(this);
		word = wordds.getWord((int)mId);
		if(mId != -1 && word != null){
			// Get controls' reference
			tv_word = (TextView) findViewById(R.id.tv_word);
			tv_word_type = (TextView) findViewById(R.id.tv_word_type);
			tv_definition = (TextView) findViewById(R.id.tv_definition);
			tv_example = (TextView) findViewById(R.id.tv_example);
			label_definition = (TextView) findViewById(R.id.label_definition);
			label_example = (TextView) findViewById(R.id.label_example);
			
		   // Bind data into UI
			tv_word.setText(word.getWord());
			tv_word_type.setText(word.getType());
			tv_definition.setText(word.getDefinition());
			tv_example.setText(word.getExample());
			
			// set labels' visibility
			if(word.getDefinition().length() > 0)
				label_definition.setVisibility(View.VISIBLE);
			if(word.getExample().length() > 0)
				label_example.setVisibility(View.VISIBLE);
			
			//-------Handle pronounce button-----------
			btn_pronounce = (ImageButton) findViewById(R.id.btn_pronounce);
			btn_pronounce.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//------Check Text-To-Speech engine availability
					pronounce(tv_word.getText().toString());
				}
			});
		}
		else{
			Toast.makeText(this, "No detail available", Toast.LENGTH_LONG)
			     .show();
		}
		wordds.close();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mTts.shutdown();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_word_detail_action, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_bar_btn_edit:
	        	Intent intent = new Intent();
				intent.putExtra("id", mId);
				intent.setClassName("com.pquach.vocabularynote", "com.pquach.vocabularynote.EditDetailActivity");
				startActivity(intent);
	            return true;
	        case R.id.action_bar_btn_delete:
	        	showDeleteAlerDialog(this); // show an alert dialog and delete the word (delete function is called inside showAlertDialog())
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
	}
	
	public void delete(int wordId){
		
		WordDataSource wordds = new WordDataSource(this);
		wordds.delete(String.valueOf(wordId));
		wordds.close();
	}
	
	public void showDeleteAlerDialog(Context context){
		AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.setMessage("Do you want to delete this word?");
		dlg.setTitle("Delete");
		dlg.setCancelable(true);
		DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which){
				case DialogInterface.BUTTON_POSITIVE:
					delete((int) mId);
					// start main activity
		    		Intent intent = new Intent();
		    		intent.setClassName("com.pquach.vocabularynote", "com.pquach.vocabularynote.MainActivity");
		    		startActivity(intent);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", dialogOnClickListener );
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "No", dialogOnClickListener);
		dlg.show();
	}
	
	/**
	 * check if Text-To-Speech engine is installed on current device
	 */
	public void checkTTtsAvailability(){
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	//	super.onActivityResult(requestCode, resultCode, data);
		int i=0;
	    if (requestCode == MY_DATA_CHECK_CODE) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            // success, create the TTS instance
	            mTts = new TextToSpeech(this, new OnInitListener() {
					@Override
					public void onInit(int status) {
						// TODO Auto-generated method stub
						Locale defaultLocale = Locale.getDefault();
						Locale locale = Locale.US;
						if(defaultLocale == Locale.CANADA){
							locale = Locale.CANADA;
						}
						if(defaultLocale == Locale.UK){
							locale = Locale.UK;
						}
						// isLanguageAvailable(locale)>0 means this language is available
						if(mTts.isLanguageAvailable(locale)>0){
							mTts.setLanguage(locale);
							mTts.setSpeechRate((float) 0.7);
						}else{
							MISSING_LANGUAGE = true;
						}
					}
				});
	        } else {
	            // missing data, install it
	            Intent installIntent = new Intent();
	            installIntent.setAction(
	                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        }
	    }
	}
	
	/**
	 * pronounce word
	 */
	protected void pronounce(String word){
		
		//checkTTtsAvailability();// check if Text-To-Speech engine is available
		int i=0;
		if(!MISSING_LANGUAGE){
			mTts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
		}else{
			Toast.makeText(getApplicationContext(), "The language is not supported", Toast.LENGTH_LONG)
		     .show();
		}
	}
}