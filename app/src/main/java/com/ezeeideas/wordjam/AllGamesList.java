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

import com.ezeeideas.wordjam.db.GamesDbAdapter;

import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.os.AsyncTask;
import android.view.Window;

public class AllGamesList extends Activity implements OnClickListener
{	
	private int mTestType;
	private int mLevel;
	
	private WordJamApp mApp;
	private GamesDbAdapter mGamesDbAdapter;
	private ArrayList<GamesDbAdapter.GameShortInfo> mAllGamesList;
	
	private ListView mAllGamesListView;
	
	private static final int ALL_GAMES_BUTTONS_INDEX_BASE = 21444232;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
   
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	requestWindowFeature(Window.FEATURE_PROGRESS);
    	
		setContentView(R.layout.allgameslist);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mTestType = extras.getInt(Constants.KEY_GAME_TYPE);
			mLevel = extras.getInt(Constants.KEY_LEVEL);
		}
		
		mAllGamesListView = (ListView) findViewById(R.id.allgameslist);
		
		TextView gamesListHeading = (TextView) findViewById(R.id.allgameslist_heading);
		//gamesListHeading.setText(GameSelectionActivity.getTestName(this,  mTestType) + ", Level: " + mLevel);
		
		//open databases
		mApp = (WordJamApp) getApplication();
		mGamesDbAdapter = mApp.getGamesDbAdapter();

    	QueryDatabaseTask task = new QueryDatabaseTask(this);
    	task.execute(new Integer[] {0});
    	
    	//button click listeners
    	ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
    	backButton.setOnClickListener(this);		
    			
		honorTheme();
	}
	
	private void honorTheme()
    {
    	LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
    	ll.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    	
    	TextView headingText = (TextView) findViewById(R.id.allgameslist_heading);
    	headingText.setTextSize(Themes.getTextSizeFor(Themes.TEXT_SIZE_HEADINGS));
    }
	
	public void onClick(View v) {

    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }

	private class QueryDatabaseTask extends AsyncTask<Integer, Integer, Integer>
	{
		Context mContext;
		
		QueryDatabaseTask(Context context)
		{
			super();
			mContext = context;
		}
		protected void onPreExecute()
		{
			setProgressBarIndeterminateVisibility(true);	
		}
		
		protected Integer doInBackground(Integer... types)
		{
			//Log.d(null, "doInBackground");
			mAllGamesList = mGamesDbAdapter.fetchAllSavedGames(mLevel);

	    	//Log.d(null, "number of rows is " + numRows);
	    	return (Integer)mAllGamesList.size();
		}
		
		protected void onProgressUpdate(Integer... progress)
		{
			//Log.d(null, "onProgressUpdate");
			
		}

		protected void onPostExecute(Integer result)
		{
			//Log.d(null, "onPostExecute");
			/*
			mAllGamesListView.setAdapter(new SimpleCursorAdapter(getBaseContext(), R.layout.wordlistitem, mCursor, new String[] {WordDbAdapter.KEY_WORD, WordDbAdapter.KEY_MEANING},
					new int[] {R.id.wordtextrow, R.id.meaningtextrow}));
					*/
			
			GamesListAdapter mListAdapter = new GamesListAdapter(mContext, mAllGamesList);
			mAllGamesListView.setAdapter(mListAdapter);
			setProgressBarIndeterminateVisibility(false);
		}
	}

	public class GamesListAdapter extends ArrayAdapter<GamesDbAdapter.GameShortInfo> implements OnClickListener
	{	
		int mItemsPerRow;
		ArrayList<GamesDbAdapter.GameShortInfo> mList;
		Context mContext;
		
		public GamesListAdapter(Context context, ArrayList<GamesDbAdapter.GameShortInfo> list)
		{
			super(context, R.layout.gameslistitem, list);
			mContext = context;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				mItemsPerRow = 2;
			}
			else
			{
				mItemsPerRow = 3;
			}
			mList = list;
		}
		
		  @Override
		  public int getCount() {
		      
			  if ((mList.size() % mItemsPerRow) == 0)
			  {
				  return (mList.size() / mItemsPerRow);
			  }
			  
		      return (mList.size() / mItemsPerRow + 1);
		  }
		  
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Utils.Log("################ getView called with position " + position + ", list size = " + mList.size() + ", mItemsPerRow = " + mItemsPerRow);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.gameslistitem, null);
            }

            int itemsInThisRow = 0;
            if (position < mList.size() / mItemsPerRow)
            {
            	itemsInThisRow = mItemsPerRow;
            }
            else
            {
            	itemsInThisRow = mList.size() % mItemsPerRow;
            }
            
            int index = position * mItemsPerRow;
            
            Button game1button = (Button) v.findViewById(R.id.game_1_row_button);
            Button game2button = (Button) v.findViewById(R.id.game_2_row_button);
            Button game3button = (Button) v.findViewById(R.id.game_3_row_button);
            
            game1button.setVisibility(View.VISIBLE);
            String button1Text = String.format(getString(R.string.recent_game_label), mList.get(index).gameNum);
        	game1button.setText(button1Text);
        	game1button.setId(ALL_GAMES_BUTTONS_INDEX_BASE + index);
        	game1button.setOnClickListener(this);
            
            if (itemsInThisRow > 1)
            {
            	game2button.setVisibility(View.VISIBLE);
            	String button2Text = String.format(getString(R.string.recent_game_label), mList.get(index + 1).gameNum);
            	game2button.setText(button2Text);
            	game2button.setId(ALL_GAMES_BUTTONS_INDEX_BASE + index + 1);
            	game2button.setOnClickListener(this);
            }
            if (itemsInThisRow > 2)
            {
            	game3button.setVisibility(View.VISIBLE);
            	String button3Text = String.format(getString(R.string.recent_game_label), mList.get(index + 2).gameNum);
            	game3button.setText(button3Text);
            	game3button.setId(ALL_GAMES_BUTTONS_INDEX_BASE + index + 2);
            	game3button.setOnClickListener(this);
            }	
             
            return v;
		}

		public void onClick(View v) {
		
			Button button = (Button)v;
			int index = button.getId() - ALL_GAMES_BUTTONS_INDEX_BASE;
			
			
			Utils.Log("gaurav - onClick, button.id = " + button.getId() + ", index = " + index + ", gameNum = " + mList.get(index).gameNum);
			//GameSelectionOld.startGame(mContext, mTestType, mLevel, mList.get(index).gameNum);
		}
	}
}