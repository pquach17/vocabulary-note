package com.pquach.vocabularynote;



import com.pquach.vocabularynote.R;

import android.app.AlertDialog;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_word_detail);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
		}
		else{
			Toast t = Toast.makeText(this, "No detail available", Toast.LENGTH_LONG);
			t.show();
			return;
		}
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
}