package com.pquach.vocabularynote;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import com.pquach.vocabularynote.WordDataSource;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;


public class FileProccessor {
	
	final char WORD = '@';
	final char TYPE = '%';
	final char DEFINITION = '$';
	final char EXAMPLE = '&';
	final char DIVIDER = '^';
	Context mContext;
	
	public FileProccessor(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	public boolean exportData(String fileName){
			//--Load data
			WordDataSource wordds = new WordDataSource(mContext);
			Cursor cur = wordds.getAll();
			return writeFile(fileName, cur);
			
	}
	
	public boolean importData(String fileName){
		// 1. Read file's content => return an array list of words
		// 2. Insert data into database
		Word[] arrWords = readFile(fileName);
		WordDataSource wordds = new WordDataSource(mContext);
		if(arrWords!=null && arrWords.length>0){
			for(int i=0;i<arrWords.length; i++){
				wordds.insert(arrWords[i]);
			}
			return true;
		}else{
			return false;
		}
	}
	
	private boolean writeFile(String fileName, Cursor content){
		boolean isDone = false;
		try{
			if(!isExternalStorageWritable())
				return false;
			File dir = new File(mContext.getExternalFilesDir(null), "Exports");
			dir.mkdirs();
			File file = new File(dir, fileName);
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fOut); 
			
			// Write the string to the file
			String word, type, def, ex, buffer;
			osw.write(String.valueOf(content.getCount())+"\n");
			while(content.moveToNext()){
				word = WORD + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD))+"\n";
				type = TYPE + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE))+"\n";
				def = DEFINITION + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION))+"\n";
				ex = EXAMPLE + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE))+"\n";
				buffer = word+type+def+ex+DIVIDER+"\n";
				
				osw.write(buffer);
				isDone = true;
			}
			
			if(isDone==false){
				mContext.deleteFile(fileName);
			}
			/* ensure that everything is
			* really written out and close */
			osw.flush();
			osw.close();
			return isDone;
			
		}catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
	      }
	}
	private Word[] readFile(String fileName){
		int rowCount = 0, i=0;
		String readLine, word=null, type=null, def=null, ex=null;
		Word[] arrWords;
		
		try{
			if(!isExternalStorageReadable())
				return null;
			File dir = new File(mContext.getExternalFilesDir(null), "Exports");
			File file = new File(dir, fileName);
			FileInputStream fIn = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fIn);
			BufferedReader buff = new BufferedReader(isr);
			try{
				rowCount = Integer.parseInt(buff.readLine());
			}catch (NumberFormatException nfe){
				nfe.printStackTrace();
				buff.close();
				return null;
			}
			arrWords = new Word[rowCount];
			while((readLine=buff.readLine())!=null){
				Word newWord = new Word();
				switch(readLine.charAt(0)){
				case WORD:
					word=readLine.substring(1);
					break;
				case TYPE:
					type=readLine.substring(1);
					break;
				case DEFINITION:
					def=readLine.substring(1);
					break;
				case EXAMPLE:
					ex=readLine.substring(1);
					break;
				case DIVIDER:
					newWord.setWord(word);
					newWord.setType(type);
					newWord.setDefinition(def);
					newWord.setExample(ex);
					arrWords[i++] = newWord;
				default:
					break;
				};
			}
			buff.close();
			return arrWords;
			
		}catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
	      }
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
}
