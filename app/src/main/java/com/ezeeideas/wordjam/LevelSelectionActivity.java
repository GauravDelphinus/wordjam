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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class LevelSelectionActivity extends Activity implements OnClickListener, OnTouchListener
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_level_selection);
		
		mApp = (WordJamApp) getApplication();
		mWordDbAdapter = mApp.getWordDbAdapter();
		mGamesDbAdapter = mApp.getGamesDbAdapter();
		
		if (getIntent().getExtras() != null)
		{
			mPlayType = getIntent().getExtras().getInt(Constants.KEY_PLAY_TYPE);
		}
		
		setupViews();
		
		setupDynamicViews();
		
		//setupDialer();
		
		honorTheme();
	}
	
	private void setupDialer()
	{
		if (mDialerImageOriginal == null)
		{
			mDialerImageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.levels_wheel_button);
		}
		
		if (mDialerMatrix == null)
		{
			mDialerMatrix = new Matrix();
		}
		else
		{
			mDialerMatrix.reset();
		}
		
		//mDialerView = (ImageView) findViewById(R.id.levels_wheel_button);
		//mDialerView.setOnTouchListener(this);
		/*
		mDialerView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				if (mDialerWidth == 0 || mDialerHeight == 0)
				{
					mDialerWidth = mDialerView.getWidth();
					mDialerHeight = mDialerView.getHeight();
					

					
					Matrix resize = new Matrix();
					resize.postScale((float)Math.min(mDialerWidth, mDialerHeight) / (float)mDialerImageOriginal.getWidth(), (float)Math.min(mDialerWidth, mDialerHeight) / (float)mDialerImageOriginal.getHeight());
					mDialerImageScaled = Bitmap.createBitmap(mDialerImageOriginal, 0, 0, mDialerImageOriginal.getWidth(), mDialerImageOriginal.getHeight(), resize, false);
				}
			}
		});
		*/
	}
	
	private void setupViews()
	{
		TextView tv = (TextView) findViewById(R.id.level_selection_title_text_view);
		tv.setText(GameUtils.getPlayTypeName(mPlayType, this));
		Utils.Log("setupViews, mPlayType = " + mPlayType + ", title = " + GameUtils.getPlayTypeName(mPlayType, this));
		
		mLevelButtons = new Vector<Button>();
		
		
		Button button = (Button) findViewById(R.id.level1_button);
		button.setEnabled(true);
		button.setOnClickListener(this);
		mLevelButtons.add(button);
		
		button = (Button) findViewById(R.id.level2_button);
		button.setEnabled(true);
		button.setOnClickListener(this);
		mLevelButtons.add(button);
		
		button = (Button) findViewById(R.id.level3_button);
		button.setEnabled(true);
		button.setOnClickListener(this);
		mLevelButtons.add(button);
		
		button = (Button) findViewById(R.id.level4_button);
		button.setEnabled(true);
		button.setOnClickListener(this);
		mLevelButtons.add(button);
		
		button = (Button) findViewById(R.id.level5_button);
		button.setEnabled(true);
		button.setOnClickListener(this);
		mLevelButtons.add(button);
		
		button = (Button) findViewById(R.id.level6_button);
		button.setEnabled(true);
		button.setOnClickListener(this);
		mLevelButtons.add(button);
		
		
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mBackButton.setEnabled(true);
		mBackButton.setOnClickListener(this);
	}
	
	private void setupDynamicViews()
	{
		
		boolean remainingLevelsLocked = false;
		for (int i = 0; i < MAX_LEVELS; i++)
		{
			int totalPoints = GameUtils.sLevelValues[i][GameUtils.LEVEL_TOTAL_POINTS];
			int earnedPoints = 0;
			
			String buttonLabel = "Level " + (i+1);
			
			// Get the Level states
			Button button = mLevelButtons.elementAt(i);
			if (remainingLevelsLocked)
			{
				button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.reveal_button, 0, 0, 0);
			}
			else
			{
				int levelState = mGamesDbAdapter.getLevelState(Constants.PLAY_TYPE_ENGLISH_CLASSIC, i+1);
				if (levelState == Constants.LEVEL_STATE_COMPLETED)
				{
					button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.completed, 0, 0, 0);
					
					//get the total points and earned points for this level
					earnedPoints = mGamesDbAdapter.getEarnedPoints(Constants.PLAY_TYPE_ENGLISH_CLASSIC, i+1);
					
					buttonLabel += "\nEarned Points: " + earnedPoints;
				}
				else if (levelState == Constants.LEVEL_STATE_IN_PROGRESS)
				{
					button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.resume, 0, 0, 0);
					remainingLevelsLocked = true; // lock remaining levels
					
					//get the total points and earned points for this level
					earnedPoints = mGamesDbAdapter.getEarnedPoints(Constants.PLAY_TYPE_ENGLISH_CLASSIC, i+1);
					
					buttonLabel += "\nEarned Points: " + earnedPoints + " / " + totalPoints;
				}
				else
				{
					//not yet started
					button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.start, 0, 0, 0);
 	 				remainingLevelsLocked = true; // lock remaining levels
				}
			}
			
			//set the button label
			button.setText(buttonLabel);
			
		}
		
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
		case R.id.level1_button:
			startLevel(1);
			break;
		case R.id.level2_button:
			startLevel(2);
			break;
		case R.id.level3_button:
			startLevel(3);
			break;
		case R.id.level4_button:
			startLevel(4);
			break;
		case R.id.level5_button:
			startLevel(5);
			break;
		case R.id.level6_button:
			startLevel(6);
			break;
		}
		
	}
	
	private void startLevel(int level)
	{
    	Intent i = new Intent(this, LevelHomeActivity.class);
        i.putExtra(Constants.KEY_LEVEL, level);
        i.putExtra(Constants.KEY_PLAY_TYPE, Constants.PLAY_TYPE_ENGLISH_CLASSIC);
        
    	startActivity(i);
	}

	private Vector<Button> mLevelButtons;
	private ImageButton mBackButton;
	
	private WordJamApp mApp;
	private WordDbAdapter mWordDbAdapter;
	private GamesDbAdapter mGamesDbAdapter;
	private int mPlayType;
	
	private static final int MAX_LEVELS = 6;
	
	private static Bitmap mDialerImageOriginal, mDialerImageScaled;
	private static Matrix mDialerMatrix;
	private ImageView mDialerView;
	private int mDialerWidth, mDialerHeight;
	private double mDialerStartAngle;
	
	public boolean onTouch(View view, MotionEvent event) 
	{
		Utils.Log("onTouch, x = " + event.getX() + ", y = " + event.getY());
		Utils.Log("onTouch, mDialerWidth = " + mDialerWidth + ", mDialerHeight = " + mDialerHeight);
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mDialerStartAngle = getAngle(event.getX(), event.getY());
			break;
        case MotionEvent.ACTION_MOVE:
            double currentAngle = getAngle(event.getX(), event.getY());
            rotateDialer((float) (mDialerStartAngle - currentAngle));
            mDialerStartAngle = currentAngle;
            break;
        case MotionEvent.ACTION_UP:
            break;
		}
		return true;
	}
	
	/**
	 * @return The angle of the unit circle with the image view's center
	 */
	private double getAngle(double xTouch, double yTouch) {
	    double x = xTouch - (mDialerWidth / 2d);
	    double y = mDialerHeight - yTouch - (mDialerHeight / 2d);
	    switch (getQuadrant(x, y)) {
	        case 1:
	            return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
	        case 2:
	            return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
	        case 3:
	            return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
	        case 4:
	            return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
	        default:
	            return 0;
	    }
	}
	/**
	 * @return The selected quadrant.
	 */
	private static int getQuadrant(double x, double y) 
	{
		int quadrant = 0;
	    if (x >= 0) {
	        quadrant = y >= 0 ? 1 : 4;
	    } else {
	        quadrant = y >= 0 ? 2 : 3;
	    }
	    
	    Utils.Log("getQuadrant, x = " + x + ", y = " + y + ", quadrant = " + quadrant);
	    return quadrant;
	}
	
	/**
	 * Rotate the dialer.
	 *
	 * @param degrees The degrees, the dialer should get rotated.
	 */
	private void rotateDialer(float degrees) {
		
	    mDialerMatrix.postRotate(degrees);
	    float[] values = new float[9];
	    mDialerMatrix.getValues(values);
	    
	    mDialerMatrix.postScale(1/values[0], 1/values[4]);
	    Utils.Log("rotateDialer, degrees = " + degrees + ", scale x = " + values[0] + ", y = " + values[4]);
	    Bitmap bitmap = Bitmap.createBitmap(mDialerImageOriginal, 0, 0, mDialerImageOriginal.getWidth(), mDialerImageOriginal.getHeight(), mDialerMatrix, true);
	    mDialerView.setImageBitmap(bitmap);
	    //Utils.Log("bitmap width = " + bitmap.getWidth() + ", height = " + bitmap.getHeight());
	}
}

