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

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.widget.LinearLayout;
import android.graphics.Point;

public class HangmanPanel extends LinearLayout
{
	private Paint	innerPaint, borderPaint, mHangmanPodColor, mHangmanColor, mLettersColor, mDashesColor;
	private static final int GAP_BETWEEN_LETTER_AND_DASH = 5;
	
	/**
	 * The Hangman is drawn with the help of various variables and parameters, as described below.
	 * 
	 *                      D               E
	 * 						-----------------
	 * 						|               |
	 *                      |               |F
	 * 						|              ---
	 * 						|             / G \
	 *                      |             \   /
	 *                      |              ---
	 *                      |               |H
	 *                      |              J|
	 *                      |           K---|---L
	 *                      |               |
	 *                      |             M |I
	 *                      |              / \
	 *                      |             /   \ 
	 *                      |            N     O
	 *                  ____|_____
	 *                 A    C     B
	 *                 
	 * mHangmanPaths - set of 7 paths
	 * Path Level 0: A-B, C-D, D-E, E-F
	 * Path Level 1: Circle around mid-point G
	 * Path Level 2: H-I
	 * Path Level 3: J-K
	 * Path Level 4: J-L
	 * Path Level 5: M-N
	 * Path Level 6: M-O
	 */
	private Point[][][] mHangmanPaths; //set of 7 paths as described above
	private int mHangmanHeadRadius; //the radius of the hangman's head
	private Point[][] mWordDashPaths; //the postions of the dash'es for each letter in the word
	private Point[] mCharPositions; //the top-left poition of each character in the word drawn on the dashes
	private int mCurrentPathLevel; //the current path level of 7 (see above illustration)
	private String mCurrentChars; //the current set of characters
	private String mWord; // the word to be drawn
	private float mScaleFactor; // scale factor for density
	
	public HangmanPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HangmanPanel(Context context) {
		super(context);
		init();
	}

	private void init() {
		innerPaint = new Paint();
		innerPaint.setARGB(225, 75, 75, 75); //gray
		innerPaint.setAntiAlias(true);

		mScaleFactor = getContext().getResources().getDisplayMetrics().scaledDensity;
		
		borderPaint = new Paint();
		borderPaint.setARGB(255, 255, 255, 255);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(2 * mScaleFactor);
		borderPaint.setTextAlign(Paint.Align.CENTER);
		borderPaint.setTextSize(25 * mScaleFactor);
		

		mDashesColor = new Paint();
		mDashesColor.setColor(Themes.getColorFor(Themes.COLOR_HANGMAN_DASHES));
		mDashesColor.setAntiAlias(true);
		mDashesColor.setStyle(Style.STROKE);
		mDashesColor.setStrokeWidth(2 * mScaleFactor);
		mDashesColor.setTextAlign(Paint.Align.CENTER);
		mDashesColor.setTextSize(25 * mScaleFactor);
		
		mHangmanPodColor = new Paint();
		mHangmanPodColor.setColor(Themes.getColorFor(Themes.COLOR_HANGMAN_POD));
		mHangmanPodColor.setAntiAlias(true);
		mHangmanPodColor.setStyle(Style.STROKE);
		mHangmanPodColor.setStrokeWidth(6 * mScaleFactor);
		mHangmanPodColor.setTextAlign(Paint.Align.CENTER);
		mHangmanPodColor.setTextSize(25 * mScaleFactor);
		
		mHangmanColor = new Paint();
		mHangmanColor.setColor(Themes.getColorFor(Themes.COLOR_HANGMAN_MAN));
		mHangmanColor.setAntiAlias(true);
		mHangmanColor.setStyle(Style.STROKE);
		mHangmanColor.setStrokeWidth(6 * mScaleFactor);
		mHangmanColor.setTextAlign(Paint.Align.CENTER);
		mHangmanColor.setTextSize(25 * mScaleFactor);
		
		mLettersColor = new Paint();
		mLettersColor.setColor(Themes.getColorFor(Themes.COLOR_HANGMAN_LETTERS));
		mLettersColor.setAntiAlias(true);
		mLettersColor.setStyle(Style.FILL_AND_STROKE);
		mLettersColor.setStrokeWidth(2 * mScaleFactor);
		mLettersColor.setTextAlign(Paint.Align.CENTER);
		float minTextSize = Themes.getMinTextSize() * mScaleFactor;
		float textSize = Math.max(20 * mScaleFactor, minTextSize);
		mLettersColor.setTextSize(textSize);
	
		mHangmanPaths = new Point[7][][];
		mHangmanPaths[0] = new Point[4][2];
		mHangmanPaths[1] = new Point[1][1];
		mHangmanPaths[2] = new Point[1][2];
		mHangmanPaths[3] = new Point[1][2];
		mHangmanPaths[4] = new Point[1][2];
		mHangmanPaths[5] = new Point[1][2];
		mHangmanPaths[6] = new Point[1][2];
		
		mCurrentPathLevel = 0;
		mCurrentChars = new String();
	}
	
	public void setInnerPaint(Paint innerPaint) {
		this.innerPaint = innerPaint;
	}

	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	Utils.Log("dispatchDraw with canvas = " + canvas);
    	updateHangmanData();
    	
    	////Log.d(null, "$$$$$$$$$$$$$$$$$$ dispatchDraw....");
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
    	//canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
		//canvas.drawRoundRect(drawRect, 5, 5, borderPaint);

		int[] l = {0, 0};
		this.getLocationOnScreen(l);
		int i;
		Utils.Log("$$$ dispatchDraw, mCurrentPathLevel = " + mCurrentPathLevel);
		for (i = 0; i < mCurrentPathLevel; i++)
		{
			int j;
			for (j = 0; j < mHangmanPaths[i].length; j++)
			{
				if (i == 1) // head
				{
					//Log.d(null, "############################# drawing circle at point (" + mHangmanPaths[i][j][0].x + ", " + mHangmanPaths[i][j][0].y + ") with radius " + mHangmanHeadRadius);
					canvas.drawCircle(mHangmanPaths[i][j][0].x - l[0], mHangmanPaths[i][j][0].y - l[1], mHangmanHeadRadius, mHangmanColor);
				}
				else
				{
					Paint paint;
					if (i < 1)
					{
						paint = mHangmanPodColor;
					}
					else
					{
						paint = mHangmanColor;
					}
					//draw the lines
					//Log.d(null, "#### about to access mHangmanPaths[" + i + "][" + j + "][0] and [1]");
					canvas.drawLine(mHangmanPaths[i][j][0].x - l[0], mHangmanPaths[i][j][0].y - l[1], mHangmanPaths[i][j][1].x - l[0], mHangmanPaths[i][j][1].y - l[1], paint);
				}
			}
		}
		
		for (i = 0; i < mWord.length(); i++)
		{
			//Log.d(null, "################## drawing dash " + i + ", from (" + mWordDashPaths[i][0].x + ", " + mWordDashPaths[i][0].y + ") to (" + mWordDashPaths[i][1].x + ", " + mWordDashPaths[i][1].y + ")");
			canvas.drawLine(mWordDashPaths[i][0].x - l[0], mWordDashPaths[i][0].y - l[1], mWordDashPaths[i][1].x - l[0], mWordDashPaths[i][1].y - l[1], mDashesColor);
			char c = mCurrentChars.charAt(i);
			//Log.d(null, "################ checking char at " + i + ", it is " + c + ", isDigit = " + Character.isDigit(c));
			if (Character.isLetter(c))
			{
				canvas.drawText(Character.toString(c), mCharPositions[i].x - l[0], mCharPositions[i].y - l[1], mLettersColor);
			}
		}
		super.dispatchDraw(canvas);
    }
    
    public void updateHangmanData()
    {
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
    	Utils.Log("updateHangmanData, globalVisibleRect = " + r);

	
		// set up hangman
		int hangmanWidth = r.right - r.left;
		int hangmanHeight = (int)((r.bottom - r.top) * 0.7);
		hangmanWidth = hangmanHeight = Math.min(hangmanWidth, hangmanHeight);
		int midx = (r.left + r.right) / 2;
		int midy = (r.top + r.top + hangmanHeight) / 2;
		Rect hangmanRect = new Rect(midx - hangmanWidth/2, midy - hangmanHeight/2, midx + hangmanWidth/2, midy + hangmanHeight/2);
		
		//structure
		mHangmanPaths[0][0][0] = new Point(hangmanRect.left + hangmanWidth/8, hangmanRect.bottom - hangmanHeight/16); //A
		mHangmanPaths[0][0][1] = new Point(hangmanRect.left + hangmanWidth/2, mHangmanPaths[0][0][0].y); //B
		//Log.d(null, "##### allocated mHangmanPaths[0][0][0] and [0][0][1]");
		
		mHangmanPaths[0][1][0] = new Point((mHangmanPaths[0][0][0].x + mHangmanPaths[0][0][1].x)/2, mHangmanPaths[0][0][0].y); //C
		mHangmanPaths[0][1][1] = new Point(mHangmanPaths[0][1][0].x, hangmanRect.top + hangmanHeight/16); //D
		
		mHangmanPaths[0][2][0] = new Point(mHangmanPaths[0][1][1].x, mHangmanPaths[0][1][1].y); //D
		mHangmanPaths[0][2][1] = new Point(hangmanRect.right - hangmanWidth/3, mHangmanPaths[0][2][0].y); //E
		
		mHangmanPaths[0][3][0] = new Point(mHangmanPaths[0][2][1].x, mHangmanPaths[0][2][1].y); //E
		mHangmanPaths[0][3][1] = new Point(mHangmanPaths[0][3][0].x, mHangmanPaths[0][3][0].y + hangmanHeight/8); //F
		
		//head mid point
		mHangmanPaths[1][0][0] = new Point(mHangmanPaths[0][3][1].x, mHangmanPaths[0][3][1].y + hangmanHeight/16);  //G
		//head radius
		mHangmanHeadRadius = hangmanHeight/16;
		
		//neck + body
		mHangmanPaths[2][0][0] = new Point(mHangmanPaths[1][0][0].x, mHangmanPaths[1][0][0].y + hangmanHeight/16); //H
		mHangmanPaths[2][0][1] = new Point(mHangmanPaths[2][0][0].x, mHangmanPaths[2][0][0].y + (int)(2.5 * hangmanHeight/8));  //I
		
		//right hand
		mHangmanPaths[3][0][0] = new Point(mHangmanPaths[2][0][0].x, mHangmanPaths[2][0][0].y + hangmanHeight/16);  //J
		mHangmanPaths[3][0][1] = new Point(mHangmanPaths[3][0][0].x - hangmanWidth/10, mHangmanPaths[3][0][0].y + hangmanHeight/10); //K
		
		//left hand
		mHangmanPaths[4][0][0] = new Point(mHangmanPaths[3][0][0].x, mHangmanPaths[3][0][0].y); //J
		mHangmanPaths[4][0][1] = new Point(mHangmanPaths[4][0][0].x + hangmanWidth/10, mHangmanPaths[4][0][0].y + hangmanHeight/10); //L
		
		//right leg
		mHangmanPaths[5][0][0] = new Point(mHangmanPaths[4][0][0].x, mHangmanPaths[4][0][0].y + hangmanHeight/4); //M
		mHangmanPaths[5][0][1] = new Point(mHangmanPaths[5][0][0].x - hangmanWidth/10, mHangmanPaths[5][0][0].y + hangmanHeight/10); //N
		
		//left leg
		mHangmanPaths[6][0][0] = new Point(mHangmanPaths[5][0][0].x, mHangmanPaths[5][0][0].y); //M
		mHangmanPaths[6][0][1] = new Point(mHangmanPaths[6][0][0].x + hangmanWidth/10, mHangmanPaths[6][0][0].y + hangmanHeight/10); //O
		
		// set up word dashes
		int wordAreaWidth = r.right - r.left;
		int wordAreaHeight = r.bottom - r.top - hangmanHeight;
		Rect wordAreaRect = new Rect(r.left, r.bottom - wordAreaHeight, r.right, r.bottom);
		
		int numDashes = mWord.length();
		int dashWidth = wordAreaWidth/numDashes - 10;
		int dashY = wordAreaRect.bottom - wordAreaHeight/4;
		//Log.d(null, "numDashes = " + numDashes + ", dashWidth = " + dashWidth + ", dashY = " + dashY);
		int i;
		for (i = 0; i < numDashes; i++)
		{
			//Log.d(null, "Updating wordDashPath date for i = " + i);
			mWordDashPaths[i][0] = new Point(wordAreaRect.left + i * (dashWidth + 10), dashY);
			mWordDashPaths[i][1] = new Point(mWordDashPaths[i][0].x + dashWidth, dashY);
			mCharPositions[i] = new Point((mWordDashPaths[i][0].x + mWordDashPaths[i][1].x)/2, mWordDashPaths[i][0].y - GAP_BETWEEN_LETTER_AND_DASH);
		}
		
		// set up char positions
    }
    
    public void updatePathLevel(int level)
    {
    	mCurrentPathLevel = level;
    	invalidate();
    }
    
    public void setWord(String word)
    {
    	
    	mWord = word;
    	//Log.d(null, "Panel: setWord [" + mWord + "]");
    	mWordDashPaths = new Point[word.length()][];
    	int i;
    	for (i = 0; i < word.length(); i++)
    	{
    		mWordDashPaths[i] = new Point[2];
    		mCurrentChars = mCurrentChars.concat(Character.toString('#'));
    	}
    	
    	mCharPositions = new Point[word.length()];
    	//Log.d(null, "*** Allocated mCharPositions");
    }
    
    public void updateCurrentChars(String chars)
    {
    	mCurrentChars = chars;
    	invalidate();
    }
}