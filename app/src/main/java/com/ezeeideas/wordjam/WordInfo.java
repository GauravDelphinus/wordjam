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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class WordInfo extends Activity
{
	private TextView mWordText;
	private TextView mTypeText;
	private TextView mMeaningText;
	private Long mRowID;
	private WordDbAdapter mWordDbAdapter;
	private WordJamApp mApp;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mApp = (WordJamApp)getApplication();
		mWordDbAdapter = mApp.getWordDbAdapter();
		
		setContentView(R.layout.wordinfo);
		
		mWordText = (TextView) findViewById(R.id.word_label);
		mTypeText = (TextView) findViewById(R.id.word_type);
		mMeaningText = (TextView) findViewById(R.id.word_meaning);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mRowID = extras.getLong(WordDbAdapter.KEY_ROWID);
		}

		populateFields();

	}
	
	private void populateFields()
	{
		if (mRowID != null)
		{
			Cursor wordCursor = mWordDbAdapter.fetchWord(Constants.PLAY_TYPE_ENGLISH_CLASSIC, mRowID);
			startManagingCursor(wordCursor);
			
			String word = wordCursor.getString(wordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_WORD));
			mWordText.setText(word);
			
			String type = wordCursor.getString(wordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_TYPE));
			mTypeText.setText(type);
			
			String meaning = wordCursor.getString(wordCursor.getColumnIndexOrThrow(WordDbAdapter.KEY_MEANING));
			mMeaningText.setText(meaning);
			
		}
	}
}