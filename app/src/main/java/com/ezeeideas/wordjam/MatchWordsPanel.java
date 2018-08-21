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

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.widget.LinearLayout;
import android.graphics.Point;

public class MatchWordsPanel extends LinearLayout
{
	public MatchWordsPanel(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = (MatchWordsGameActivity)context;
		init();
	}

	public MatchWordsPanel(Context context) 
	{
		super(context);
		mContext = (MatchWordsGameActivity)context;
		init();
	}

	/**
	 * Initialize all values and members
	 */
	private void init() 
	{
		/**
		 * Initialize Paints
		 */
		innerPaint = new Paint();
		innerPaint.setARGB(225, 75, 75, 75); //gray
		innerPaint.setAntiAlias(true);

		mLinePaint = new Paint();
		mLinePaint.setColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_LINE));
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(5);
		
		mFoundLinePaint = new Paint();
		mFoundLinePaint.setARGB(255, 0, 255, 0);
		mFoundLinePaint.setAntiAlias(true);
		mFoundLinePaint.setStyle(Style.STROKE);
		mFoundLinePaint.setStrokeWidth(5);
	
		mButtonPaint = new Paint();
		mButtonPaint.setColor(Themes.getColorFor(Themes.COLOR_MATCH_WORDS_BUTTON));
		mButtonPaint.setAntiAlias(true);
		mButtonPaint.setStyle(Style.FILL);
		mButtonPaint.setStrokeWidth(2);

		/**
		 * initialize data structures
		 */
		mCurrentStartPoint = new Point(0, 0);
		mCurrentEndPoint = new Point(0, 0);
		
		mChosenPairs = new Vector<int[]>();
		mFoundWords = new Vector<Integer>();
	}

	/**
	 * Util function that returns true if the given word is correctly found (matched)
	 * @param wordButtonIndex The index of the word button
	 * @return true if correctly matched, false otherwise
	 */
	boolean isWordFound(int wordButtonIndex)
	{
		for (int i = 0; i < mFoundWords.size(); i++)
		{
			if (wordButtonIndex == mFoundWords.get(i))
			{
				return true;
			}
		}
		
		return false;
	}
	
    @Override
    protected void dispatchDraw(Canvas canvas) 
    {
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
		int[] l = {0, 0};
		this.getLocationOnScreen(l);
		
		/**
		 * Draw the "chosen pairs" which could include both matched
		 * and not correctly matched pairs
		 */
		Paint linePaint = null;
    	for (int i = 0; i < mChosenPairs.size(); i++)
    	{
    		//if (isWordFound(mChosenPairs.get(i)[0])) //matched pair
    		//{
    			//linePaint = mFoundLinePaint; 
    		//}
    		//else //not correctly matched pair
    		{
    			linePaint = mLinePaint; 
    		}
    		
    		Point[] linePoints = mContext.getAnchorPoints(mChosenPairs.get(i));
			if (linePoints[0].x > 0 && linePoints[0].y > 0 && linePoints[1].x > 0 && linePoints[1].y > 0)
			{
				//draw the anchor points
				canvas.drawCircle(linePoints[0].x - l[0], linePoints[0].y - l[1], ANCHOR_RADIUS, linePaint);
				canvas.drawCircle(linePoints[1].x - l[0], linePoints[1].y - l[1], ANCHOR_RADIUS, linePaint);
				
				//draw the lines
				canvas.drawLine(linePoints[0].x - l[0], linePoints[0].y - l[1], linePoints[1].x - l[0], linePoints[1].y - l[1], linePaint);
			}
    	}
		
    	/**
    	 * Draw the "current" drag
    	 */
		if (mCurrentStartPoint.x > 0 && mCurrentStartPoint.y > 0 && mCurrentEndPoint.x > 0 && mCurrentEndPoint.y > 0)
		{
			canvas.drawCircle(mCurrentStartPoint.x - l[0], mCurrentStartPoint.y - l[1], ANCHOR_RADIUS, mLinePaint);
			canvas.drawCircle(mCurrentEndPoint.x - l[0], mCurrentEndPoint.y - l[1], ANCHOR_RADIUS, mLinePaint);
			
			canvas.drawLine(mCurrentStartPoint.x - l[0], mCurrentStartPoint.y - l[1], mCurrentEndPoint.x - l[0], mCurrentEndPoint.y - l[1], mLinePaint);
		}
		
		super.dispatchDraw(canvas);
    }
    
    /**
     * Update the various data values that are used to draw the lines
     * and anchor points
     * @param currentStartPoint The start point of the current drag
     * @param currentEndPoint The end point of the current drag
     * @param chosenPairs The current set of chosen pairs, some of which may be wrong matches
     * @param foundWords The actual found words, indexes of the word buttons that have been correctly matched so far
     */
    public void updateData(Point currentStartPoint, Point currentEndPoint, Vector<int[]> chosenPairs, Vector<Integer> foundWords)
    {
    	mCurrentStartPoint = currentStartPoint;
    	mCurrentEndPoint = currentEndPoint;
    	
    	mChosenPairs = chosenPairs;
    	mFoundWords = foundWords;
    }
    
    /**************** Members Variables *******************/
    
	private Paint	innerPaint, mLinePaint, mButtonPaint, mFoundLinePaint;

	private Point mCurrentStartPoint;
	private Point mCurrentEndPoint;
	private MatchWordsGameActivity mContext;
	private Vector<int[]> mChosenPairs; 
	private Vector<Integer> mFoundWords;
	
	/**
	 * Constants
	 */
	public static final int ANCHOR_RADIUS = 5; //the radius of the anchor points next to buttons
}