/******************************************************************************
*
* The MIT License (MIT)
*
* Copyright (c) 2013 - Present, Gaurav Jain
*
* (@gauravdelphinus, github.com/gauravdelphinus)
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*
*******************************************************************************/
package com.ezeeideas.wordjam;

import java.util.ArrayList;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.os.AsyncTask;
import android.view.Window;

public class WordList extends Activity implements OnClickListener
{	
	private int mType;
	private String mTypeStr;
	
	private WordJamApp mApp;
	private WordDbAdapter mWordDbAdapter;
	private Cursor mCursor;
	private QueryDatabaseTask mQueryDatabaseTask;
	
	private ListView mWordListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mApp = (WordJamApp)getApplication();
		mWordDbAdapter = mApp.getWordDbAdapter();
   
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	requestWindowFeature(Window.FEATURE_PROGRESS);
    	
		setContentView(R.layout.wordlist);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mType = extras.getInt(WordTypeList.KEY_WORD_TYPE);
			mTypeStr = extras.getString(WordTypeList.KEY_WORD_TYPE_STR);
		}
		
		mWordListView = (ListView) findViewById(R.id.wordlist);
		
		TextView wordListHeading = (TextView) findViewById(R.id.wordlist_heading);
		wordListHeading.setText(mTypeStr);


    	
    	//button click listeners
    	ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
    	backButton.setOnClickListener(this);		
    			
		honorTheme();
		
		//we don't care about storing the state are the search needs to be performed
		//afresh every time anyway.
		mQueryDatabaseTask = new QueryDatabaseTask();
		mQueryDatabaseTask.execute(new Integer[] {mType});
	}
	
	private void honorTheme()
    {
    	LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
    	ll.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    	
    	TextView headingText = (TextView) findViewById(R.id.wordlist_heading);
    	headingText.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_HEADINGS));
    }
	
	public void onClick(View v) {

    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }

	private class QueryDatabaseTask extends AsyncTask<Integer, Void, Cursor>
	{
		protected void onPreExecute()
		{
			setProgressBarIndeterminateVisibility(true);	
		}
		
		protected Cursor doInBackground(Integer... types)
		{
			Cursor cursor = mWordDbAdapter.fetchWordsOfType(Constants.PLAY_TYPE_ENGLISH_CLASSIC, types[0]);
	    	startManagingCursor(cursor);

	    	return cursor;
		}
		
		protected void onProgressUpdate(Void... progress)
		{			
		}

		protected void onPostExecute(Cursor cursor)
		{
			mWordListView.setAdapter(new SimpleCursorAdapter(getBaseContext(), R.layout.wordlistitem, cursor, new String[] {WordDbAdapter.KEY_WORD, WordDbAdapter.KEY_MEANING},
					new int[] {R.id.wordtextrow, R.id.meaningtextrow}));
			setProgressBarIndeterminateVisibility(false);
		}
		
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	public void onDestroy()
	{
		cancelAsyncTask();
		super.onDestroy();
	}
	
	private void cancelAsyncTask()
	{
		if (mQueryDatabaseTask != null && mQueryDatabaseTask.getStatus() != AsyncTask.Status.FINISHED)
		{
			mQueryDatabaseTask.cancel(true);
			mQueryDatabaseTask = null;
		}
	}
	
	public class WordTypeAdapter extends ArrayAdapter<Integer>
	{	
		public WordTypeAdapter(Context context, int textViewResourceId, ArrayList<Integer> words)
		{
			super(context, textViewResourceId, words);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Log.d(null, "################ getView called with position " + position);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.wordlistitem, null);
            }
            
            TextView wordTextView = (TextView) v.findViewById(R.id.wordtextrow);
        	TextView meaningTextView = (TextView) v.findViewById(R.id.meaningtextrow);
            
            //lookup the particular word of this type
            Cursor cursor = mWordDbAdapter.fetchWordOfType(Constants.PLAY_TYPE_ENGLISH_CLASSIC, mType, position);
            //Log.d(null, "numRows = " + cursor.getCount() + ", numColumns = " + cursor.getColumnCount());
            String[] columns = cursor.getColumnNames();
            int i;
            for (i = 0; i < columns.length; i++)
            {
            	//Log.d(null, "column " + i + " [" + columns[i] + "]");
            }
            //Log.d(null, "index = " + index);
            //Log.d(null, "value for this index = " + cursor.getString(index));
            String word = cursor.getString(cursor.getColumnIndexOrThrow(WordDbAdapter.KEY_WORD));
            String meaning = cursor.getString(cursor.getColumnIndexOrThrow(WordDbAdapter.KEY_MEANING));
            
            if(wordTextView != null){
               wordTextView.setText(word);
            }
  
            if(meaningTextView != null){
                meaningTextView.setText(meaning);
             }
             
            return v;
		}
	}
}