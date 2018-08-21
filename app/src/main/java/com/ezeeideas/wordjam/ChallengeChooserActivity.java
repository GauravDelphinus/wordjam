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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

public class ChallengeChooserActivity extends Activity implements OnClickListener, SwipeTouchListener.OnTouchListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_play_chooser);
		
		setupViews();
		
		honorTheme();
	}
	
	private void setupViews()
	{
		mEnglishChallengeButton = (Button) findViewById(R.id.english_challenge_button);
		mEnglishChallengeButton.setEnabled(true);
		mEnglishChallengeButton.setOnClickListener(this);
		mEnglishChallengeButton.setOnTouchListener(new SwipeTouchListener(this));
		
		mGREChallengeButton = (Button) findViewById(R.id.gre_challenge_button);
		mGREChallengeButton.setEnabled(true);
		mGREChallengeButton.setOnClickListener(this);
		mGREChallengeButton.setOnTouchListener(new SwipeTouchListener(this));
		
		mSATChallengeButton = (Button) findViewById(R.id.sat_challenge_button);
		mSATChallengeButton.setEnabled(true);
		mSATChallengeButton.setOnClickListener(this);
		mSATChallengeButton.setOnTouchListener(new SwipeTouchListener(this));
		
		mKidsChallengeButton = (Button) findViewById(R.id.kids_challenge_button);
		mKidsChallengeButton.setEnabled(true);
		mKidsChallengeButton.setOnClickListener(this);
		mKidsChallengeButton.setOnTouchListener(new SwipeTouchListener(this));
		
		mChallengeChooserViewFlipper = (ViewFlipper) findViewById(R.id.challenge_chooser_view_flipper);
		mChallengeChooserViewFlipper.setOnTouchListener(new SwipeTouchListener(this));
		
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mBackButton.setEnabled(true);
		mBackButton.setOnClickListener(this);
	}
	
	private void honorTheme()
	{
		//TBD
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else
    	{
    		startPlay(v.getId());
    	}
	}
	
	private void startPlay(int Id)
	{
		switch (Id)
		{
		case R.id.english_challenge_button:
        	Intent i = new Intent(this, ChallengeHomeActivity.class);
        	i.putExtra(Constants.KEY_PLAY_TYPE, Constants.PLAY_TYPE_ENGLISH_CLASSIC);
     
        	startActivity(i);
        default:
        		break;
		}
	}

	private Button mEnglishChallengeButton, mGREChallengeButton, mSATChallengeButton, mKidsChallengeButton;
	private ImageButton mBackButton;
	private ViewFlipper mChallengeChooserViewFlipper;
	public void onSwipeRight() {
		Utils.Log("onSwipeRight");
		if (mChallengeChooserViewFlipper.getDisplayedChild() > 0)
		{
			mChallengeChooserViewFlipper.setInAnimation(this, R.anim.in_from_left);
			mChallengeChooserViewFlipper.setOutAnimation(this, R.anim.out_to_right);
			
			// Show The Previous Screen
			mChallengeChooserViewFlipper.showPrevious();
		}
		
	}

	public void onSwipeLeft() {
		Utils.Log("onSwipeLeft");
		if (mChallengeChooserViewFlipper.getDisplayedChild() < (Constants.NUM_CHALLENGES - 1))
		{
			mChallengeChooserViewFlipper.setInAnimation(this, R.anim.in_from_right);
			mChallengeChooserViewFlipper.setOutAnimation(this, R.anim.out_to_left);
			
			// Show The Previous Screen
			mChallengeChooserViewFlipper.showNext();
		}
		
	}

	public void onSwipeTop() {
		// TODO Auto-generated method stub
		
	}

	public void onSwipeBottom() {
		// TODO Auto-generated method stub
		
	}
}
