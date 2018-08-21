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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
//import android.util.Log;
import android.widget.LinearLayout;

public class CrosswordPanel extends LinearLayout
{
	//private static final boolean SHOW_WORDS = false;
	private Paint mInnerPaint, mBorderPaint, mBlackPaint, mBlackBorderPaint, mSmallTextBlackPaint, mTextPaint, mSelectedPaint, mSelectedWordPaint, mFoundTextPaint;
	private static final int TEXT_SIZE = 25;
	private static final int PADDING = 0;
	private static final int CELL_PADDING = 1;
	private static final int INDEX_TOP_PADDING = 2;
	private static final int INDEX_LEFT_PADDING = 2;
	
	private float mScaleFactor;
	
	private Vector<int[]> mAcrossList;
	private Vector<int[]> mDownList;
	private Vector<String> mWords;

	private int[][][] mCrosswordMap; // third dim: 0 = index on across list, 1 = index into word for this char, 2 = index on down list, 3 = index into word for this char
	
	private int mSelectedRow;
	private int mSelectedCol;
	
	private char[][] mCurrentMatrix;
	private boolean[][] mCurrentFoundMatrix; // found matrix
	
	private int mOrder;
	private int[] mSelectedWordData; // 0th index: 0 = across list, 1 = down list, 1st index: position into the list
	
	public CrosswordPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CrosswordPanel(Context context) {
		super(context);
		init();
	}

	private void init()
	{
		mInnerPaint = new Paint();
		mInnerPaint.setARGB(255, 255, 255, 255); //white
		mInnerPaint.setAntiAlias(true);
		mInnerPaint.setStyle(Style.FILL);
		
		mSelectedPaint = new Paint();
		mSelectedPaint.setAntiAlias(true);
		mSelectedPaint.setStyle(Style.FILL);
		mSelectedPaint.setColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
		mSelectedPaint.setAlpha(50);

		mBorderPaint = new Paint();
		mBorderPaint.setARGB(255, 255, 255, 255);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setStyle(Style.STROKE);
		mBorderPaint.setStrokeWidth(2);
		mBorderPaint.setTextAlign(Paint.Align.CENTER);
		mBorderPaint.setTextSize(25);

		mSelectedWordPaint = new Paint();
		mSelectedWordPaint.setColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
		mSelectedWordPaint.setAntiAlias(true);
		mSelectedWordPaint.setStyle(Style.STROKE);
		mSelectedWordPaint.setStrokeWidth(3);
		mSelectedWordPaint.setTextAlign(Paint.Align.CENTER);
		mSelectedWordPaint.setTextSize(25);
		
		mBlackPaint = new Paint();
		mBlackPaint.setARGB(255, 0, 0, 0);
		mBlackPaint.setAntiAlias(true);
		mBlackPaint.setStyle(Style.FILL);
		mBlackPaint.setTextAlign(Paint.Align.CENTER);
		//mBlackPaint.setTextSize(25);

		mTextPaint = new Paint();
		mTextPaint.setColor(Themes.getColorFor(Themes.COLOR_CROSSWORD_LETTER));
		mTextPaint.setAntiAlias(true);
		mTextPaint.setStyle(Style.FILL);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(TEXT_SIZE);
		
		mFoundTextPaint = new Paint();
		//mFoundTextPaint.setARGB(50, 128, 128, 128);
		mFoundTextPaint.setColor(Themes.getColorFor(Themes.COLOR_CROSSWORD_HIT_CELL));
		mFoundTextPaint.setStyle(Style.FILL);
		mFoundTextPaint.setAntiAlias(true);

		mSmallTextBlackPaint = new Paint();
		mSmallTextBlackPaint.setARGB(255, 0, 0, 0);
		mSmallTextBlackPaint.setAntiAlias(true);
		mSmallTextBlackPaint.setStyle(Style.FILL);
		mSmallTextBlackPaint.setTextAlign(Paint.Align.LEFT);
		mSmallTextBlackPaint.setTextSize(10);
		
		mBlackBorderPaint = new Paint();
		mBlackBorderPaint.setARGB(255, 0, 0, 0);
		mBlackBorderPaint.setAntiAlias(true);
		mBlackBorderPaint.setStyle(Style.STROKE);
		
		mAcrossList = new Vector<int[]>();
		mDownList = new Vector<int[]>();
		mWords = new Vector<String>();
		mCurrentMatrix = null; //will be allocated once we know the order
		mCurrentFoundMatrix = null;
		mOrder = 0;
		
		mSelectedWordData = new int[] {-1, -1};
		
		mScaleFactor = getContext().getResources().getDisplayMetrics().scaledDensity;
		
		mSelectedRow = -1;
		mSelectedCol = -1;
	}
	
    public void initWithOrder(int order)
    {
    	mOrder = order;
    	mCurrentMatrix = new char[mOrder][mOrder];
    	mCurrentFoundMatrix = new boolean[mOrder][mOrder];
    	Utils.Log("initWithOrder, mOrder = " + mOrder + ", mCurrentMatrix = " + mCurrentMatrix);
    }
	
	public void updateSelectedWordData(int[] data)
	{
		mSelectedWordData = data;
	}
	
	public void updateCurrentMatrix(char[][] matrix, boolean[][] foundMatrix)
	{
		mCurrentMatrix = matrix;
		mCurrentFoundMatrix = foundMatrix;
	}
	
	public void updateSelectedCell(int row, int col)
	{
		mSelectedRow = row;
		mSelectedCol = col;
	}
	
	public void setCrosswordData(Vector<int[]> acrossList, Vector<int[]> downList, Vector<String> wordList, Vector<String> meaningList, int[][][] crosswordMap)
	{
		mAcrossList = acrossList;
		mDownList = downList;
		mWords = wordList;
		mCrosswordMap = crosswordMap;
	}
	
	public int[] getSelectedCell(Point p)
	{
		int[] cell = {-1, -1};
		
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
		//Log.d(null, "### Point is (" + p.x + ", " + p.y +"), Rect is (" + r.left + ", " + r.top + "), (" + r.right + ", " + r.bottom + ")");
		
		int panelWidth = r.right - r.left;
		int panelHeight = r.bottom - r.top;
		
		int crosswordWidth = panelWidth - PADDING * 2;
		int crosswordHeight = panelHeight - PADDING * 2;
		crosswordWidth = crosswordHeight = Math.min(crosswordWidth,  crosswordHeight);
		
		float colWidth = ((float) crosswordWidth) / mOrder;
		float rowHeight = ((float) crosswordHeight) / mOrder;
		//Log.d(null, "crosswordWidth = " + crosswordWidth + ", crosswordHeight = " + crosswordHeight + ", colWidth = " + colWidth + ", rowHeight = " + rowHeight);
		
		int cX = r.left + (panelWidth - crosswordWidth)/2;
		int cY = r.top + (panelHeight - crosswordHeight)/2;
		
		if ((p.x >= r.left && p.x < (cX + crosswordWidth)) && (p.y >= r.top && p.y <= (cY + crosswordHeight)))
		{	
			//Log.d(null, "################ p.x = " + p.x + ", p.y = " + p.y + ", cX = " + cX + ", cY = " + cY);
			cell[0] = (int) ((p.y - cY) / rowHeight);
			cell[1] = (int) ((p.x - cX) / colWidth);
		}
		
		/*
		if (cell[0] >= mOrder)
		{
			cell[0] --;
		}
		if (cell[1] >= mOrder)
		{
			cell[1] --;
		}
		*/
		
		return cell;
	}
	
    @Override
    protected void dispatchDraw(Canvas canvas)
    {
    	Utils.Log("dispatchDraw, mOrder = " + mOrder + ", mCurrentMatrix = " + mCurrentMatrix);
    	if (mOrder <= 0)
    	{
    		return;
    	}
    	//Log.d(null, "$$$$$$$$$$$$$$$$$$ dispatchDraw....");
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
		int[] l = {0, 0};
		this.getLocationOnScreen(l);
	
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
		
		int panelWidth = r.right - r.left;
		int panelHeight = r.bottom - r.top;
		
		int crosswordWidth = panelWidth - PADDING * 2;
		int crosswordHeight = panelHeight - PADDING * 2;
		crosswordWidth = crosswordHeight = Math.min(crosswordWidth,  crosswordHeight);
		
		int colWidth = crosswordWidth / mOrder;
		int rowHeight = crosswordHeight / mOrder;
		
		//recalulate the width and height to remove truncation errors
		crosswordWidth = colWidth * mOrder;
		crosswordHeight = rowHeight * mOrder;
		
		float minTextSize = Themes.getMinTextSize() * mScaleFactor;
		float minNumSize = minTextSize * 0.7f;
		float textSize = Math.max(rowHeight/1.5f - 2 * CELL_PADDING, minTextSize);
		float numSize = Math.max(rowHeight/3f - 2 * CELL_PADDING, minNumSize);
		Utils.Log("## rowHeight = " + rowHeight + ", mScaleFactor = " + mScaleFactor + ", textSize = " + textSize + ", numSize + " + numSize);
		
		mTextPaint.setTextSize(textSize);
		mSmallTextBlackPaint.setTextSize(numSize);
		
		int crosswordLeft = r.left + (panelWidth - crosswordWidth)/2 - l[0];
		int crosswordTop = r.top + (panelHeight - crosswordHeight)/2 - l[1];
		
		//draw background of whole board blcak first
		canvas.drawRect(crosswordLeft - CELL_PADDING, crosswordTop - CELL_PADDING, crosswordLeft + crosswordWidth + CELL_PADDING, crosswordTop + crosswordHeight + CELL_PADDING, mBlackPaint);
		
		//draw the crossword
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				if (mCurrentMatrix[i][j] != '%')
				{
					//fillable cell
					int left = crosswordLeft + j * colWidth + CELL_PADDING;
					int top = crosswordTop + i * rowHeight + CELL_PADDING;
					int right = left + colWidth - CELL_PADDING * 2;
					int bottom = top + rowHeight - CELL_PADDING * 2;
					
					//draw the white background for this cell
					if (mSelectedRow == i && mSelectedCol == j)
					{
						canvas.drawRect(left, top, right, bottom, mInnerPaint);
						canvas.drawRect(left, top, right, bottom, mSelectedPaint);
					}
					else
					{
						canvas.drawRect(left, top, right, bottom, mInnerPaint);
						if (mCurrentFoundMatrix[i][j] == true)
						{
							canvas.drawRect(left, top, right, bottom, mFoundTextPaint);
						}
					}
					
					//now draw the number, if any
					int index = -1;
					int acrossIndex = mCrosswordMap[i][j][0];
					int downIndex = mCrosswordMap[i][j][2];
					if (acrossIndex != -1 && mCrosswordMap[i][j][1] == 0) // show index only if it's the first character
					{
						int val[] = mAcrossList.elementAt(acrossIndex);
						index = val[0];
					}
					else if (downIndex != -1 && mCrosswordMap[i][j][3] == 0) // show index only if it's the first character
					{
						int val[] = mDownList.elementAt(downIndex);
						index = val[0];
					}
					if (index != -1)
					{
						/*
						 * draw the small index text, left aligned
						 * ___________
						 * |    ^
						 * |    |
						 * |    V____
						 * |<-> | 
						 * |    |
						 */
						Rect bounds = new Rect();
					    mSmallTextBlackPaint.getTextBounds(String.valueOf(index), 0, String.valueOf(index).length(), bounds);
						canvas.drawText(String.valueOf(index), left + INDEX_LEFT_PADDING, top + INDEX_TOP_PADDING + (bounds.bottom - bounds.top), mSmallTextBlackPaint);
					}
					
					//now draw the current character in this cell
					if (mCurrentMatrix[i][j] != '*')
					{
						/*
						 * draw the letter, center aligned
						 */
						Rect bounds = new Rect();
					    mTextPaint.getTextBounds(Character.toString(mCurrentMatrix[i][j]), 0, Character.toString(mCurrentMatrix[i][j]).length(), bounds);
						//fillable cell that has been filled with something by user
						canvas.drawText(Character.toString(mCurrentMatrix[i][j]), left + colWidth/2, top + rowHeight/2 + (bounds.bottom - bounds.top)/2, mTextPaint);
					}
				}
			}
		}
		
		//now draw any selected word, if user tapped on an item in the hints lists
		//Log.d(null, "################################ going to check for selected word to draw");
		if (mSelectedWordData[0] > -1 && mSelectedWordData[1] > -1)
		{
			if (mSelectedWordData[0] == 0) // across list
			{
				/*
				for (i = 0; i < mAcrossList.size(); i++)
				{
					if (mAcrossList.elementAt(i)[0] == mSelectedWordData[1])
					{
						break;
					}
				}
			
				int[] val = mAcrossList.get(i);
				*/
				int[] val = mAcrossList.get(mSelectedWordData[1]);
				int row = val[1];
				int startCol = val[2];
				String word = mWords.elementAt(val[3]);
				int endCol = startCol + word.length() - 1;
				//Log.d(null, "row = " + row + ", startCol = " + startCol + ", word = " + word + ", endCol = " + endCol);
			
				// find the extremes of the bounding rect of the entire word
				int left = crosswordLeft + startCol * colWidth + CELL_PADDING;
				int top = crosswordTop + row * rowHeight + CELL_PADDING;
				int right = crosswordLeft + endCol * colWidth + colWidth - CELL_PADDING;
				int bottom = top + rowHeight - CELL_PADDING * 2;
			
				//Log.d(null, "calculated left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
				canvas.drawRect(left, top, right, bottom, mSelectedWordPaint);
			}
			else if (mSelectedWordData[0] == 1) // down list
			{
				/*
				for (i = 0; i < mDownList.size(); i++)
				{
					if (mDownList.elementAt(i)[0] == mSelectedWordData[1])
					{
						break;
					}
				}
				int[] val = mDownList.get(i);
				*/
				int[] val = mDownList.get(mSelectedWordData[1]);
				int startRow = val[1];
				int col = val[2];
				String word = mWords.elementAt(val[3]);
				int endRow = startRow + word.length() - 1;
				//Log.d(null, "row = " + row + ", startCol = " + startCol + ", word = " + word + ", endCol = " + endCol);
			
				// find the extremes of the bounding rect of the entire word
				int left = crosswordLeft + col * colWidth + CELL_PADDING;
				int top = crosswordTop + startRow * rowHeight + CELL_PADDING;
				int right = left + colWidth - CELL_PADDING * 2;
				int bottom = crosswordTop + endRow * rowHeight + rowHeight - CELL_PADDING;
	
				//Log.d(null, "calculated left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
				canvas.drawRect(left, top, right, bottom, mSelectedWordPaint);
			}
		}
		
		/*
		
		//now draw the white squares
		if (mAcrossList.size() > 0)
		{
			Iterator it = mAcrossList.iterator();
			while (it.hasNext())
			{
				int[] val = (int [])it.next();
				int row = val[1];
				int col = val[2];

				String word = mWords.elementAt(val[3]);
			
				int i;
				for (i = col; i < col + word.length(); i++)
				{					
					canvas.drawRect(10 + i * colWidth + 1, 10 + row * rowHeight + 1, 10 + i * colWidth + colWidth - 1, 10 + row * rowHeight + rowHeight - 1, mInnerPaint);
				}
			}
		}
		if (mDownList.size() > 0)
		{
			Iterator it = mDownList.iterator();
			while (it.hasNext())
			{
				int[] val = (int [])it.next();
				int row = val[1];
				int col = val[2];

				String word = mWords.elementAt(val[3]);
			
				int i;
				for (i = row; i < row + word.length(); i++)
				{
					
					canvas.drawRect(10 + col * colWidth + 1, 10 + i * rowHeight + 1, 10 + col * colWidth + colWidth - 1, 10 + i * rowHeight + rowHeight - 1, mInnerPaint);
				}
			}
		}

		//draw the numbers next to word starts		
		if (mAcrossList.size() > 0)
		{
			Iterator it = mAcrossList.iterator();
			while (it.hasNext())
			{
				int[] val = (int [])it.next();
				int number = val[0];
				int row = val[1];
				int col = val[2];

				canvas.drawText(String.valueOf(number), 10 + col * colWidth + colWidth/4, 10 + row * rowHeight + rowHeight/3, mSmallTextBlackPaint);
			}
		}
		
		if (mDownList.size() > 0)
		{
			Iterator it = mDownList.iterator();
			while (it.hasNext())
			{
				int[] val = (int [])it.next();
				int number = val[0];
				int row = val[1];
				int col = val[2];

				canvas.drawText(String.valueOf(number), 10 + col * colWidth + colWidth/4, 10 + row * rowHeight + rowHeight/3, mSmallTextBlackPaint);
			}
		}

		if (!SHOW_WORDS)
		{
		
		// draw the current matrix
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				if (mCurrentMatrix[i][j] != '#' && mCurrentMatrix[i][j] != '*')
				{
					canvas.drawText(Character.toString(mCurrentMatrix[i][j]), 10 + j * colWidth + colWidth/2, 10 + i * rowHeight + rowHeight/2 + TEXT_SIZE/2, mTextBluePaint);
				}
			}
		}
		}
		else
		{
			int k, i;
			// SHOW WORDS (TEMPORARY FOR TESTING)
			for (k = 0; k < mAcrossList.size(); k++)
			{
				int[] val = (int [])mAcrossList.elementAt(k);
				int number = val[0];
				int row = val[1];
				int col = val[2];
				String word = mWords.elementAt(val[3]);
				//Log.d(null, "drawing text for across Index = " + index + ", row = " + row + ", col = " + col);
				for (i = col; i < col + word.length(); i++)
				{
					canvas.drawText(Character.toString(word.charAt(i - col)), 10 + i * colWidth + colWidth/2, 10 + row * rowHeight + rowHeight/2 + TEXT_SIZE/2, mTextBlackPaint);
				}
			}
			for (k = 0; k < mDownList.size(); k++)
			{				
				int[] val = (int [])mDownList.elementAt(k);
				int number = val[0];
				int row = val[1];
				int col = val[2];
				String word = mWords.elementAt(val[3]);
				//Log.d(null, "drawing text for down Index = " + index + ", row = " + row + ", col = " + col);
				for (i = row; i < row + word.length(); i++)
				{
					canvas.drawText(Character.toString(word.charAt(i - row)), 10 + col * colWidth + colWidth/2, 10 + i * rowHeight + rowHeight/2 + TEXT_SIZE/2, mTextBlackPaint);
				}
			}
		}
		*/
    }
    

}