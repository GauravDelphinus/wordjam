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

import java.util.Vector;

import com.ezeeideas.wordjam.db.GamesDbAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageButton;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Button;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;

public class ChallengeHomeActivity extends Activity implements OnClickListener, PieButton.PieButtonListener {
  	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_challenge_home);
        
        if (getIntent().getExtras() != null)
        {
        	mPlayType = getIntent().getExtras().getInt(Constants.KEY_PLAY_TYPE);
        }
        // record the main activity in the global context
    	mApp = ((WordJamApp)getApplication());
    	
        //set up all the views in this activity
        setupViews();

    	honorTheme();
    }
    
    /* ------------------------- UI Setup ----------------------- */
    
    private void setupViews()
    {       
        mThreeWayButton = (PieButton) findViewById(R.id.challenge_choices_view);
        Vector<Integer> buttonPressedResources = new Vector<Integer>();
        buttonPressedResources.add(R.drawable.challenge_options_button_lookup);
        buttonPressedResources.add(R.drawable.challenge_options_button_practice);
        buttonPressedResources.add(R.drawable.challenge_options_button_play);
        mThreeWayButton.setAttributes(30, R.drawable.challenge_options_button_normal, buttonPressedResources);
        
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this);
        mBackButton.setEnabled(true);
    }
    

    
    private void honorTheme()
    {
    	RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
    	mainLayout.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));   	
     }
    
    /* ------------------------- UI Callbacks --------------------- */
    
    public void onClick(View v) 
    {
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    }
    
	public void onPieButtonSelected(PieButton button, int index) 
	{
		if (button == mThreeWayButton)
		{
			switch (index)
			{
			case 0:
				//Lookup
		        {
		        	Intent intent = new Intent(this, LookupWordsActivity.class);
		        	intent.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
		        	startActivity(intent);
		        }
		        break;
			case 1:
				//practice
		        {
		        	Intent intent = new Intent(this, PracticeLevelSelectionActivity.class);
		        	intent.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
		        	startActivity(intent);
		        }
		        break;
			case 2:
				//play
			    {
		        	Intent intent = new Intent(this, LevelSelectionActivity.class);
		        	intent.putExtra(Constants.KEY_PLAY_TYPE, mPlayType);
		        	startActivity(intent);
		        }
		        break;
			}
		}
	} 
    
    /* ------------------- Member Variables ----------------- */
    
	private WordDbAdapter mWordDbAdapter; //The Word DB Adapter
	private GamesDbAdapter mGamesDbAdapter; //The Games DB Adapter
	private WordJamApp mApp; // Application Context
	private int mPlayType;
	
	private ImageButton mBackButton;
	private PieButton mThreeWayButton;
	

}