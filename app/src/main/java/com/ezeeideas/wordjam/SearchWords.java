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

import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import android.view.inputmethod.EditorInfo;

public class SearchWords extends Activity implements OnClickListener, OnKeyListener
{	
	private String mKeyword;
	
	private WordJamApp mApp; // the Application Context
	private WordDbAdapter mWordDbAdapter;
	private Cursor mCursor;
	
	private ListView mWordListView;
	private Context mContext;
	private EditText mEditTextView;
	
	public static final String KEY_WORD_KEYWORD = "search_keyword";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		mContext = this;
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	requestWindowFeature(Window.FEATURE_PROGRESS);
    	
		setContentView(R.layout.searchlist);
		
		mWordListView = (ListView) findViewById(R.id.searchlist);
		
		mEditTextView = (EditText) findViewById(R.id.search_box);
		mEditTextView.setOnKeyListener(this);
		mEditTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		
		//open databases
		mApp = (WordJamApp) getApplication();
    	mWordDbAdapter = mApp.getWordDbAdapter();
    	
    	//button click listeners
    	ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
    	backButton.setOnClickListener(this);		
    			
		honorTheme();
	}
	
	private void honorTheme()
    {
    	LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
    	ll.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    	
    	TextView headingText = (TextView) findViewById(R.id.searchwords_heading);
    	headingText.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_HEADINGS));
    }
	
	public void onClick(View v) {

    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }

	private class QueryDatabaseTask extends AsyncTask<String, Integer, Integer>
	{
		private boolean mSomeResultsFound; // true if at least 1 result found
		
		protected void onPreExecute()
		{
			setProgressBarIndeterminateVisibility(true);
			
			mSomeResultsFound = false; //lets see what we get
		}
		
		protected Integer doInBackground(String... types)
		{
			//Log.d(null, "doInBackground");
			mCursor = mWordDbAdapter.searchWords(Constants.PLAY_TYPE_ENGLISH_CLASSIC, types[0]);
	    	startManagingCursor(mCursor);
	    	int numRows = mCursor.getCount();
	    	if (numRows > 0)
	    	{
	    		mSomeResultsFound = true;
	    	}
	    	else
	    	{
	    		mSomeResultsFound = false;
	    	}
	    	//Log.d(null, "number of rows is " + numRows);
	    	return (Integer)numRows;
		}
		
		protected void onProgressUpdate(Integer... progress)
		{
			//Log.d(null, "onProgressUpdate");
			
		}

		protected void onPostExecute(Integer result)
		{
			//Log.d(null, "onPostExecute");
			if (mSomeResultsFound)
			{
				mWordListView.setAdapter(new SimpleCursorAdapter(getBaseContext(), R.layout.searchlistitem, mCursor, new String[] {WordDbAdapter.KEY_WORD, WordDbAdapter.KEY_MEANING},
					new int[] {R.id.searchwordstextrow, R.id.searchmeaningstextrow}));
			}
			else
			{
				//show the no results view, and hide the list view
				mWordListView.setAdapter(null);
				Utils.showGenericToast(mContext, getResources().getString(R.string.search_no_results));
			}
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
		super.onDestroy();
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) 
	{
		// If the event is a key-down event on the "enter" button
		if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
				(keyCode == KeyEvent.KEYCODE_ENTER)) 
		{
			// Perform action on key press
	    	QueryDatabaseTask task = new QueryDatabaseTask();
	    	task.execute(mEditTextView.getText().toString().trim());
			return true;
		}
		return false;
	}
}