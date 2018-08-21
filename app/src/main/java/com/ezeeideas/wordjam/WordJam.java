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

import com.ezeeideas.wordjam.db.GamesDbAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Button;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.content.Context;

public class WordJam extends Activity implements OnClickListener
{
  	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        // record the main activity in the global context
    	mApp = ((WordJamApp)getApplication());
    	
    	// Initialize Themes
    	Themes.initialize(mApp);
        
    	GameUtils.initialize(mApp);
    	
        //set up all the views in this activity
        setupViews();
        
        //initialize or set up the DB, as required
        if (!checkDBs())
        {
        	//DB not yet set up, so disable buttons and start setting up DB
        	enableButtons(false);
        	
        	setupDBs(); //start the SetupDBTask to set up the DB
    	}

    	honorTheme();
    }
    
    /**
     * Application Exit.
     */
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	cancelSetupDBTask();
    	
    	//finally, close the database
    	closeDBs();
    }
    
    /* ------------------------- UI Setup ----------------------- */
    
    private void setupViews()
    {
    	// Set up click listeners for all the buttons
        mGamesButton = (ImageButton) findViewById(R.id.games_button);
        mGamesButton.setOnClickListener(this);
        mGamesButton.setEnabled(true);
        
        mOptionsButton = (ImageButton) findViewById(R.id.options_button);
        mOptionsButton.setOnClickListener(this);
        mOptionsButton.setEnabled(true);
        
        mAboutButton = (ImageButton) findViewById(R.id.about_button);
        mAboutButton.setOnClickListener(this);
        mAboutButton.setEnabled(true);
        
        mWordsButton = (ImageButton) findViewById(R.id.words_button);
        mWordsButton.setOnClickListener(this);
        mWordsButton.setEnabled(true);

        mExitButton = (ImageButton) findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(this);
    	mExitButton.setEnabled(true);
    }
    
    /**
     * Enable/disable buttons.
     * @param enable true if buttons need to be enabled.  False otherwise.
     */
    public void enableButtons(boolean enable)
    {
		mGamesButton.setEnabled(enable);
	   	mOptionsButton.setEnabled(enable);
	   	mAboutButton.setEnabled(enable);
	   	mWordsButton.setEnabled(enable);
    }
    
    /* ----------------- DB Routines ------------------*/
    
    private boolean checkDBs()
    {
    	//do the one time database copy/setup
    	mWordDbAdapter = mApp.getWordDbAdapter();
    	mGamesDbAdapter = mApp.getGamesDbAdapter();
    		
	    return (mWordDbAdapter.isSetUp() && mGamesDbAdapter.isSetUp());
    }
    
    private void setupDBs()
    {
    	//do the one time database copy/setup
	    mSetupDBTask = new SetupDBTask(this);
		mSetupDBTask.execute((Void[])null);
    }
    
    private void closeDBs()
    {
    	mWordDbAdapter.close();
    	mGamesDbAdapter.close();
    }
    
    private void clearDBs()
    {
    	mWordDbAdapter.delete();
    }
    
    private void cancelSetupDBTask()
    {
    	if (mSetupDBTask != null && mSetupDBTask.getStatus() != AsyncTask.Status.FINISHED)
    	{
    		mSetupDBTask.cancel(true);
    		mSetupDBTask = null;
    	}
    }
    
    /* ------------------------- UI Callbacks --------------------- */
    
    public void onClick(View v) {
        switch (v.getId()) {
	        case R.id.games_button:
		        {
		        	
		        	//Intent intent = new Intent(WordJam.this, ChallengeChooserActivity.class);
		        	//startActivity(intent);
		        	
		        	Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_PLAY_BUTTON, null);
		        	
		        	/**
		        	 * start the practice session with word type as set in the preference
		        	 */
		        	Intent i = new Intent(this, GameSelectionActivity.class);
		        	
		            i.putExtra(Constants.KEY_IS_PLAY, false);
		            i.putExtra(Constants.KEY_PRACTICE_TYPE, GameUtils.getWordDifficultyPreference(this));
		            i.putExtra(Constants.KEY_PLAY_TYPE, Constants.PLAY_TYPE_ENGLISH_CLASSIC);
		            
		        	startActivity(i);
		        }
		        break;
	           
	        case R.id.options_button:
		        {
		        	Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_OPTIONS_BUTTON, null);
		        	
		        	Intent i = new Intent(this, OptionsActivity.class);
		        	startActivityForResult(i, Constants.KEY_SOURCE_ACTIVITY);
		        }
		        break;
	        
	        case R.id.about_button:
		        {
		        	Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_ABOUT_BUTTON, null);
		        	
		        	Intent intent = new Intent(WordJam.this, AboutDialog.class);
		        	startActivity(intent);
		        }
		        break;
	        	
	        case R.id.exit_button:
		        {
		        	Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_EXIT_BUTTON, null);
		        	
		        	this.finish();
		        }
		        break;
		        
	        case R.id.words_button:
	        {
	        	Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_BROWSE_BUTTON, null);
	        	
	        	Intent intent = new Intent(this, WordsActivity.class);
	        	startActivity(intent);
	        }
		        break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if (requestCode == Constants.KEY_SOURCE_ACTIVITY)
    	{	
    		honorTheme();
    	}
    }
    
    /* ---------------------- AsyncTask ---------------------*/
    
    /**
     * AyncTask that sets up the DB in the background
     *
     */
    public class SetupDBTask extends AsyncTask<Void, Void, Boolean>
  	{
      	private Context mContext;
      	
      	SetupDBTask(Context context)
      	{
      		mContext = context;
      	}
      	
  		protected void onPreExecute()
  		{
  			mDialog = ProgressDialog.show(mContext, "", getResources().getString(R.string.setupdb_progress_message));
  		}
  		
  		protected Boolean doInBackground(Void... types)
  		{	
  			boolean retval = false;
  			
  			//open/create the Word DB
  			retval = mWordDbAdapter.setup();
  			
  			//and now open/create the Games DB
  			if (retval)
  			{
  				retval = mGamesDbAdapter.setup();
  			}

  	    	return retval;
  		}
  		
  		protected void onProgressUpdate(Void... progress)
  		{
  			
  		}
  		
  		protected void onPostExecute(Boolean result)
  		{
  			if (mDialog != null && mDialog.isShowing()) 
  			{
  	            mDialog.dismiss();
  	            mDialog = null;
  	        }
  			
  			if (!result)
  			{
  				//enable only the exit button
  				enableButtons(false);
  				
  				//show an alert that there was an issue, and then exit
  				showErrorAlert();
  			}
  			else
  			{
  				//enable all buttons
  				enableButtons(true);
  			}
  		}
  		
  		protected void onCancelled()
  		{
  			//clear DBs for re-setup the next time we're up
  			clearDBs();
  		}
  	}
    
    /**
     * Something went wrong, because of which we can't continue, so let the user know
     * he can try and exit, and launch the app again.
     */
    private void showErrorAlert()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(getResources().getString(R.string.app_error_str))
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	       
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void honorTheme()
    {
    	View view = (View) findViewById(R.id.main_layout);
    	view.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    }
    
    /* ------------------- Member Variables ----------------- */
    
	private WordDbAdapter mWordDbAdapter; //The Word DB Adapter
	private GamesDbAdapter mGamesDbAdapter; //The Games DB Adapter
	private WordJamApp mApp; // Application Context
	
	private ImageButton mGamesButton;
	private ImageButton mOptionsButton, mAboutButton, mWordsButton, mExitButton;
	
  	private ProgressDialog mDialog;
  	private SetupDBTask mSetupDBTask;
  	
  	/*
	public boolean onTouch(View arg0, MotionEvent arg1) 
	{
		int x = (int)arg1.getX();
		int y = (int)arg1.getY();
		int w = arg0.getWidth();
		int h = arg0.getHeight();
		Utils.Log("onTouch, w = " + w + ", h = " + h + ", x = " + x + ", y = " + y);
		
		if (arg1.getAction() == MotionEvent.ACTION_DOWN)
		{
			Utils.Log("ACTION_DOWN");
			if (y < h/2)
			{
				//lookup
				mGamesView.setImageResource(R.drawable.challenge_options_button_lookup);
			}
			else
			{
				mGamesView.setImageResource(R.drawable.challenge_options_button_normal);
			}
			
			//mGamesView.invalidate();
		}
		else if (arg1.getAction() == MotionEvent.ACTION_UP)
		{
			Utils.Log("ACTION_UP");
			if (y < h/2)
			{
				//lookup
	        	Intent i = new Intent(this, OptionsActivity.class);
	        	startActivity(i);
			}
			mGamesView.setImageResource(R.drawable.challenge_options_button_normal);
			//mGamesView.invalidate();
		}
		return true;
	}
	*/
}