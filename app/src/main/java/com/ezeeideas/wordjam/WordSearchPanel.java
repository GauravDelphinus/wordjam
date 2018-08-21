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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.view.MotionEvent;
import android.view.View;

public class WordSearchPanel extends LinearLayout
{
	//private static final boolean SHOW_WORDS = false;
	private Paint mCellBackgroundPaint, mTextPaint, mSelectionPaint, mBoardBackgroundPaint, mLoopFillPaint, mCurrentWordOverlayPaint, mOverlayTextPaint;
	private static final int TEXT_SIZE = 20;
	private static final int PADDING = 0;
	private static final int CELL_PADDING = 1;
	
	private static final int OVERLAY_PADDING = 5 ;
	private static final int OVERLAY_CORNER_RADIUS = 5;
	
	private static final float RADIUS_FACTOR = 0.4f;
	
	private float mScaleFactor;
	
	private char[][] mCurrentGrid;
	private boolean[][] mFoundGrid; // true for a cell if hte letter is among the 'found words', false otherwise
	private Vector<int[]> mWordMappingList; //list of all words. 0 = index into word list, 1 = direction, 2 = x, 3 = y
	private Vector<String> mWords;
	private Vector<Integer> mFoundWordsList; //indeces into the mWordMappingList for words that were found by user
	private Point[] mCurrentDragPoints; //points for current drag
	private int mDragStartRow = -1, mDragStartCol = -1; //drag start row & col
	
	private int mRadius; // radius of the loop that is drawn for the currently dragged word
	private int mCellCornerRadius; // corner radius of the cell background
	private int mRadiusChoice; //choice of radius, chosen randomly between 3 options.
	
	private int mOrder;
	public WordSearchPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WordSearchPanel(Context context) {
		super(context);
		init();
	}

	private void init()
	{		
		mScaleFactor = getContext().getResources().getDisplayMetrics().scaledDensity;
		
		mCellBackgroundPaint = new Paint();
		//mCellBackgroundPaint.setColor(Color.WHITE);
		mCellBackgroundPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_CELL_BACKGROUND));
		mCellBackgroundPaint.setAntiAlias(true);
		mCellBackgroundPaint.setStyle(Style.FILL);

		mBoardBackgroundPaint = new Paint();
		//mBoardBackgroundPaint.setColor(Color.BLACK);
		mBoardBackgroundPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_BOARD_BACKGROUND));
		mBoardBackgroundPaint.setAntiAlias(true);
		mBoardBackgroundPaint.setStyle(Style.FILL);

		mTextPaint = new Paint();
		//mTextPaint.setColor(Color.BLACK);
		mTextPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_LETTER));
		mTextPaint.setAntiAlias(true);
		mTextPaint.setStyle(Style.FILL);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(TEXT_SIZE);
		
		mOverlayTextPaint = new Paint();
		//mOverlayTextPaint.setColor(Color.WHITE);
		mOverlayTextPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_OVERLAY_TEXT));
		mOverlayTextPaint.setAntiAlias(true);
		mOverlayTextPaint.setStyle(Style.FILL);
		mOverlayTextPaint.setTextAlign(Paint.Align.CENTER);
		float minTextSize = Themes.getMinTextSize() * mScaleFactor;
		mOverlayTextPaint.setTextSize(minTextSize);
		
		mSelectionPaint = new Paint();
		mSelectionPaint.setAntiAlias(true);
		mSelectionPaint.setStyle(Style.STROKE);
		mSelectionPaint.setTextAlign(Paint.Align.CENTER);
		mSelectionPaint.setStrokeWidth(3);
		//mSelectionPaint.setColor(Color.RED);
		mSelectionPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_LOOP));
		
		mLoopFillPaint = new Paint();
		mLoopFillPaint.setAntiAlias(true);
		mLoopFillPaint.setStyle(Style.FILL);
		//mLoopFillPaint.setARGB(50, 255, 0, 0);
		mLoopFillPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_LOOP_FILL));

		mCurrentWordOverlayPaint = new Paint();
		mCurrentWordOverlayPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_OVERLAY_BACKGROUND));
		//mCurrentWordOverlayPaint.setColor(Color.GRAY);
		mCurrentWordOverlayPaint.setAntiAlias(true);
		mCurrentWordOverlayPaint.setStyle(Style.FILL);
		
		mWords = new Vector<String>();
		
		//radius choice
		//let's do this randomly, so user gets to see a 'fresh' pattern every time.
		//We'll choose from 3 options: 0 = no radius (square cells), 1 = small-radius (colwidth/10), 2 = full-radius (circular cells)
		/*
		 * Gaurav: I don't like the circular or the pure square versions, so I'll stick to the small circle one for now.
		Random rand = new Random();
		mRadiusChoice = rand.nextInt(3);
		*/
		mRadiusChoice = 1;
	}
	
	public void setData(char[][] currentGrid, Vector<int[]> wordMappingList, Vector<Integer> foundWordsList, Vector<String> words, boolean[][] foundGrid)
	{
		mCurrentGrid = currentGrid;
		mWordMappingList = wordMappingList;
		mFoundWordsList = foundWordsList;
		mWords = words;
		mFoundGrid = foundGrid;
	}
	
	public void updateCurrentDragPoints(Point[] dragPoints, int startRow, int startCol)
	{
		mCurrentDragPoints = dragPoints;
		
		mDragStartRow = startRow;
		mDragStartCol = startCol;
	}
		
	
	//P's origin is the entire screen (not just the panel)
	public int[] getSelectedCell(Point p, boolean panelOffsets)
	{
		int[] cell = {-1, -1};
		
		if (p.x == -1 || p.y == -1)
		{
			return cell;
		}
		
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
		//Log.d(null, "### Point is (" + p.x + ", " + p.y +"), Rect is (" + r.left + ", " + r.top + "), (" + r.right + ", " + r.bottom + ")");
		
		//if panel offsets are provided, convert to screen
		if (panelOffsets)
		{
			p.x += r.left;
			p.y += r.top;
		}
		
		int panelWidth = r.right - r.left;
		int panelHeight = r.bottom - r.top;
		
		int wordSearchWidth = panelWidth - PADDING * 2;
		int wordSearchHeight = panelHeight - PADDING * 2;
		wordSearchWidth = wordSearchHeight = Math.min(wordSearchWidth,  wordSearchHeight);
		
		float colWidth = ((float) wordSearchWidth) / mOrder;
		float rowHeight = ((float) wordSearchHeight) / mOrder;
		
		int cX = r.left + (panelWidth - wordSearchWidth)/2;
		int cY = r.top + (panelHeight - wordSearchHeight)/2;
		
		if ((p.x >= r.left && p.x < (cX + wordSearchWidth)) && (p.y >= r.top && p.y <= (cY + wordSearchHeight)))
		{	
			//Log.d(null, "################ p.x = " + p.x + ", p.y = " + p.y + ", cX = " + cX + ", cY = " + cY);
			cell[0] = (int) ((p.y - cY) / rowHeight);
			cell[1] = (int) ((p.x - cX) / colWidth);
		}
		
		//normalize in case we were running out of the grid
		cell[0] = Math.max(cell[0], 0);
		cell[1] = Math.max(cell[1], 0);
		cell[0] = Math.min(cell[0], mOrder - 1);
		cell[1] = Math.min(cell[1], mOrder - 1);
		
		return cell;
	}
	
	//returned Point's origin is the WordSearchPanel, not the screen
	public Point getCenterPointForCell(int row, int col)
	{
		Point center = new Point();
		
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
		//Log.d(null, "### Point is (" + p.x + ", " + p.y +"), Rect is (" + r.left + ", " + r.top + "), (" + r.right + ", " + r.bottom + ")");
		
		int panelWidth = r.right - r.left;
		int panelHeight = r.bottom - r.top;
		
		int wordSearchWidth = panelWidth - PADDING * 2;
		int wordSearchHeight = panelHeight - PADDING * 2;
		wordSearchWidth = wordSearchHeight = Math.min(wordSearchWidth,  wordSearchHeight);
		
		float colWidth = ((float) wordSearchWidth) / mOrder;
		float rowHeight = ((float) wordSearchHeight) / mOrder;
		
		int cX = (panelWidth - wordSearchWidth)/2;
		int cY = (panelHeight - wordSearchHeight)/2;
		
		center.x = cX + (int)(col * colWidth + colWidth/2);
		center.y = cY + (int)(row * rowHeight + rowHeight/2);
		
		return center;
	}
	
    @Override
    protected void dispatchDraw(Canvas canvas)
    {
    	if (mOrder <= 0)
    	{
    		return;
    	}
    	
    	//update the radii based on the order
    	updateRadius();
    	//Log.d(null, "$$$$$$$$$$$$$$$$$$ dispatchDraw....");
    	RectF drawRect = new RectF();
    	drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
    	
		int[] l = {0, 0};
		this.getLocationOnScreen(l);
	
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
		
		int panelWidth = r.right - r.left;
		int panelHeight = r.bottom - r.top;
		
		int wordSearchWidth = panelWidth - PADDING * 2;
		int wordSearchHeight = panelHeight - PADDING * 2;
		wordSearchWidth = wordSearchHeight = Math.min(wordSearchWidth,  wordSearchHeight);
		
		int colWidth = wordSearchWidth / mOrder;
		int rowHeight = wordSearchHeight / mOrder;
		
		//recalulate the width and height to remove truncation errors
		wordSearchWidth = colWidth * mOrder;
		wordSearchHeight = rowHeight * mOrder;
		
		float minTextSize = Themes.getMinTextSize() * mScaleFactor;
		float textSize = Math.max(rowHeight/2 * mScaleFactor - 2 * CELL_PADDING, minTextSize);
		mTextPaint.setTextSize(textSize);
		
		int wordSearchLeft = r.left + (panelWidth - wordSearchWidth)/2 - l[0];
		int wordSearchTop = r.top + (panelHeight - wordSearchHeight)/2 - l[1];
		
		//draw background of whole board first
		canvas.drawRect(wordSearchLeft - CELL_PADDING, wordSearchTop - CELL_PADDING, wordSearchLeft + wordSearchWidth + CELL_PADDING, wordSearchTop + wordSearchHeight + CELL_PADDING, mBoardBackgroundPaint);
		
		//draw the word search grid
		int i, j;
		for (i = 0; i < mOrder; i++)
		{
			for (j = 0; j < mOrder; j++)
			{
				if (mCurrentGrid[i][j] != '#')
				{
					//fillable cell
					int left = wordSearchLeft + j * colWidth + CELL_PADDING;
					int top = wordSearchTop + i * rowHeight + CELL_PADDING;
					int right = left + colWidth - CELL_PADDING * 2;
					int bottom = top + rowHeight - CELL_PADDING * 2;
					RectF rect = new RectF(left, top, right, bottom);
					
					//try and give feedback to user by highlighting text/cell under the currently dragged word.
					//check to see if the current i,j cell is under the drag
					int[] currentCell = {-1, -1};
					if (mCurrentDragPoints != null)
					{
						//the point passed to getSelectedCell must be in screencoordinates, so we offset the current drag
						//points by the origin of the panel
						Point currentDragPoint = new Point(mCurrentDragPoints[1].x + r.left, mCurrentDragPoints[1].y + r.top); 
						currentCell = getSelectedCell(currentDragPoint, false);
					}
					
					//draw the transparent background for this cell
					canvas.drawRoundRect(rect, mCellCornerRadius, mCellCornerRadius, mCellBackgroundPaint);
					
					//now draw the current character in this cell
					Rect bounds = new Rect();
					String ch = Character.toString(mCurrentGrid[i][j]);
					mTextPaint.getTextBounds(ch, 0, ch.length(), bounds);
					int textHeight = bounds.bottom - bounds.top;
					
					//change look of 'under-drag' text, or for text for already found words
					if (mFoundGrid[i][j] || cellFallsInLine(i, j, mDragStartRow, mDragStartCol, currentCell[0], currentCell[1]))
					{
						mTextPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_HOVER_LETTER));
					}
					else
					{
						mTextPaint.setColor(Themes.getColorFor(Themes.COLOR_WORDSEARCH_LETTER));
					}
					canvas.drawText(ch, left + colWidth/2, top + rowHeight/2 + textHeight/2, mTextPaint);
				}
			}
		}
		
		//now, draw the already found words
		if (mFoundWordsList != null)
		{
			for (i = 0; i < mFoundWordsList.size(); i++)
			{
				//fetch the end points
				int mappingIndex = mFoundWordsList.elementAt(i);
				Point[] points = getEndPointsForWord(mappingIndex);
			
				//now draw the loop!
				drawLoopForPoints(canvas, points[0], points[1], i, true);
			}
		}
		
		//now draw the current word, if somethign is being dragged at the moment
		if (mCurrentDragPoints != null)
		{
			if (mCurrentDragPoints[0].x >= 0 && mCurrentDragPoints[0].y >= 0 && mCurrentDragPoints[1].x >= 0 && mCurrentDragPoints[1].y >= 0)
			{
				drawLoopForPoints(canvas, mCurrentDragPoints[0], mCurrentDragPoints[1], mFoundWordsList.size(), false);
				drawCurrentWord(canvas, mCurrentDragPoints[0], mCurrentDragPoints[1], wordSearchLeft, wordSearchTop, wordSearchWidth, wordSearchHeight);
			}
		}
    }
    
    private void drawCurrentWord(Canvas canvas, Point start, Point end, int canvasLeft, int canvasTop, int canvasWidth, int canvasHeight)
    {
    	int starts[] = getSelectedCell(start, true);
    	int ends[] = getSelectedCell(end, true);
    	int startRow = starts[0], startCol = starts[1];
    	int endRow = ends[0], endCol = ends[1];
    	String currentWord = new String();
    	
    	if (startRow < 0 || startCol < 0 || endRow < 0 || endCol < 0)
    	{
    		return;
    	}
    	
    	if (startRow == endRow && startCol == endCol)
    	{
    		//need a word, not a character
    		return;
    	}
    	
    	int i;
    	if (startRow == endRow)
		{
			//horizontal word
    		if (endCol > startCol)
    		{
    			int cols = endCol - startCol + 1;
    			for (i = 0; i < cols; i++)
    			{
    				currentWord = currentWord + mCurrentGrid[startRow][startCol + i];
    			}
    		}
    		else
    		{
    			int cols = startCol - endCol + 1;
    			for (i = 0; i < cols; i++)
    			{
    				currentWord = currentWord + mCurrentGrid[startRow][startCol - i];
    			}
    		}
		}
		else if (startCol == endCol)
		{
			//vertical word
			if (endRow > startRow)
			{
				int rows = endRow - startRow + 1;
				for (i = 0; i < rows; i++)
				{
					currentWord = currentWord + mCurrentGrid[startRow + i][startCol];
				}
			}
			else
			{
				int rows = startRow - endRow + 1;
				for (i = 0; i < rows; i++)
				{
					currentWord = currentWord + mCurrentGrid[startRow - i][startCol];
				}
			}
		}
		else if (Math.abs(startRow - endRow) == Math.abs(startCol - endCol))
    	{
			int rows = Math.abs(endRow - startRow) + 1;
	    	//diagonal word
			if (endRow > startRow && endCol > startCol)
			{
				for (i = 0; i < rows; i++)
				{
					currentWord = currentWord + mCurrentGrid[startRow + i][startCol + i];
				}
			}
			else if (endRow > startRow && endCol < startCol)
			{
				for (i = 0; i < rows; i++)
				{
					currentWord = currentWord + mCurrentGrid[startRow + i][startCol - i];
				}
			}
			else if (endRow < startRow && endCol > startCol)
			{
				for (i = 0; i < rows; i++)
				{
					currentWord = currentWord + mCurrentGrid[startRow - i][startCol + i];
				}
			}
			else if (endRow < startRow && endCol < startCol)
			{
				for (i = 0; i < rows; i++)
				{
					currentWord = currentWord + mCurrentGrid[startRow - i][startCol - i];
				}
			}
		}
    	
    	if (currentWord.length() > 0)
    	{
    		Rect bounds = new Rect();
			mOverlayTextPaint.getTextBounds(currentWord, 0, currentWord.length(), bounds);
			int textHeight = bounds.bottom - bounds.top;
			int textWidth = bounds.right - bounds.left;
	
			//now draw the current word text
			if (endRow > 0)
			{
				//draw the background overlay
				RectF rect = new RectF(canvasLeft + canvasWidth/2 - textWidth/2 - OVERLAY_PADDING, canvasTop, canvasLeft + canvasWidth/2 + textWidth/2 + OVERLAY_PADDING, canvasTop + textHeight + OVERLAY_PADDING * 3);
				canvas.drawRoundRect(rect, OVERLAY_CORNER_RADIUS, OVERLAY_CORNER_RADIUS, mCurrentWordOverlayPaint);
				canvas.drawText(currentWord, canvasLeft + canvasWidth/2, canvasTop + CELL_PADDING + textHeight + OVERLAY_PADDING, mOverlayTextPaint);
			}
			else
			{
				//draw the background overlay
				RectF rect = new RectF(canvasLeft + canvasWidth/2 - textWidth/2 - OVERLAY_PADDING, canvasTop + canvasHeight - textHeight - OVERLAY_PADDING * 3, canvasLeft + canvasWidth/2 + textWidth/2 + OVERLAY_PADDING, canvasTop + canvasHeight);
				canvas.drawRoundRect(rect, OVERLAY_CORNER_RADIUS, OVERLAY_CORNER_RADIUS, mCurrentWordOverlayPaint);
				canvas.drawText(currentWord, canvasLeft + canvasWidth/2, canvasTop + canvasHeight - CELL_PADDING - OVERLAY_PADDING, mOverlayTextPaint);
			}
    	}
    }
    
    private boolean cellFallsInLine(int curRow, int curCol, int startRow, int startCol, int dragRow, int dragCol)
    {
    	if (startRow == -1 || startCol == -1 || dragRow == -1 || dragCol == -1)
    	{
    		return false;
    	}
    	
    	//always highlight the starting cell
    	if (curRow == startRow && curCol == startCol)
    	{
    		return true;
    	}
    	
    	//always highlight the drag (end) cell
    	if (curRow == dragRow && curCol == dragCol)
    	{
    		return true;
    	}
    	
    	int startDragColDiff = Math.abs(dragCol - startCol);
    	int startDragRowDiff = Math.abs(dragRow - startRow);
    	int startCurColDiff = Math.abs(curCol - startCol);
    	int startCurRowDiff = Math.abs(curRow - startRow);
    	int curDragColDiff = Math.abs(dragCol - curCol);
    	int curDragRowDiff = Math.abs(dragRow - curRow);
    	int startDragColDir = 0;
    	if (startDragColDiff > 0)
    	{
    		startDragColDir = (dragCol - startCol)/startDragColDiff;
    	}
    	int startCurColDir = 0;
    	if (startCurColDiff > 0)
    	{
    		startCurColDir = (curCol - startCol)/startCurColDiff;
    	}
    	
    	//check if we're on the same row or col (horizontal or vertical)
    	if (startRow == dragRow && startRow == curRow && startCurColDiff <= startDragColDiff && curDragColDiff <= startDragColDiff)
    	{
    		//same row
    		return true;
    	}
    	if (startCol == dragCol && startCol == curCol && startCurRowDiff <= startDragRowDiff && curDragRowDiff <= startDragRowDiff)
    	{
    		//same col
    		return true;
    	}
    	
    	//check for diagonal
    	if (startDragRowDiff == startDragColDiff && startCurRowDiff == startCurColDiff && startCurRowDiff <= startDragRowDiff && curDragRowDiff <= startDragRowDiff && startDragColDir == startCurColDir)
    	{
    		//it will fall on the diagonal only if the triangle's base and height are equal
    		return true;
    	}
    	
    	return false;
    }
    
    private void drawLoopForPoints(Canvas canvas, Point startIn, Point endIn, int colorIndex, boolean drawFill)
    {
		/*
		 * we need to draw three shapes:
		 *     A_______C
		 *  2 (|_______|) 3
		 *     B   1   D
		 * 1. Inner rectangle (ACDB)
		 * 2. Semi-circle on one side (A2B)
		 * 3. Semi-circle on the other side (C3D)
		 */
		//We start with two points 'start' and 'end'.  In the above illustration, 'start' is the mid-point of AB and 
		//'end' is the mid-point of CD.
		
		//Let 'radius' be the radius of the semi-circles A2B and C3D.  So radius = AB/2 (or CD/2)
		
		//points1 represents AC, and points2 represents DB
		//below are all the variables we need to be able to draw the full shape
		Point[] points1 = new Point[2], points2 = new Point[2];
		RectF ovalRect1 = new RectF(), ovalRect2 = new RectF(); // oval rect (circumbscribing rect) for the arc
		float startAngle1 = 0, sweepAngle1 = 0, startAngle2 = 0, sweepAngle2 = 0;
		
		/*
		 * Find the angle of the line connecting the two points from the horizonal x axis.  Let's call it theta.
		 * This could be positive or negative (top-right or bottom-right).
		 * For top-left and bottom-left, we reverse the start and end points.
		 */
		Point start = new Point(startIn);
		Point end = new Point(endIn);
		
		if (start.x > end.x)
		{
			Point p = new Point(start);
			start.x = end.x;
			start.y = end.y;
			end.x = p.x;
			end.y = p.y;
		}
		
		//set the constants
		float opposite = end.y - start.y;
		float adjacent = end.x - start.x;
		
		double hypotenuse = Math.sqrt(adjacent * adjacent + opposite * opposite);
		double sinTheta = opposite / hypotenuse;
		double cosTheta = adjacent / hypotenuse;
		double tanTheta = opposite / adjacent;
		int sinVar = (int)(mRadius * sinTheta);
		int cosVar = (int)(mRadius * cosTheta);
		
		points1[0] = new Point(start.x + sinVar, start.y - cosVar);
		points1[1] = new Point(end.x + sinVar, end.y - cosVar);
		points2[0] = new Point(start.x - sinVar, start.y + cosVar);
		points2[1] = new Point(end.x - sinVar, end.y + cosVar);
		
		ovalRect1.left = start.x - mRadius;
		ovalRect1.top = start.y - mRadius;
		ovalRect1.right = start.x + mRadius;
		ovalRect1.bottom = start.y + mRadius;
		
		ovalRect2.left = end.x - mRadius;
		ovalRect2.top = end.y - mRadius;
		ovalRect2.right = end.x + mRadius;
		ovalRect2.bottom = end.y + mRadius;
		
		//double thetaRadians = Math.asin(sinTheta);
		double thetaRadians = Math.atan(tanTheta);
		float theta = (int)Math.toDegrees(thetaRadians);
		
		startAngle1 = 90 + theta;
		sweepAngle1 = 180;
		startAngle2 = 270 + theta;
		sweepAngle2 = 180;
		
		/*
		//now, let's get ready to draw it
		canvas.drawLine(points1[0].x, points1[0].y, points1[1].x, points1[1].y, mSelectionPaint);
		canvas.drawLine(points2[0].x, points2[0].y, points2[1].x, points2[1].y, mSelectionPaint);
		
		canvas.drawArc(ovalRect1, startAngle1, sweepAngle1, false, mSelectionPaint);
		canvas.drawArc(ovalRect2, startAngle2, sweepAngle2, false, mSelectionPaint);
    	*/
		
		//draw the loop fill
		
		if (drawFill)
		{
			//the pivot is the start point
			RectF horizontalRect = new RectF(start.x, start.y - mRadius, (float) (start.x + hypotenuse), start.y + mRadius);
			canvas.save();
			canvas.rotate(theta, start.x, start.y);
			canvas.drawRect(horizontalRect, mLoopFillPaint);
			canvas.restore();
			canvas.drawArc(ovalRect1, startAngle1, sweepAngle1, false, mLoopFillPaint);
			canvas.drawArc(ovalRect2, startAngle2, sweepAngle2, false, mLoopFillPaint);
		}
		
		//draw the loop boundary
		canvas.drawLine(points1[0].x, points1[0].y, points1[1].x, points1[1].y, mSelectionPaint);
		canvas.drawLine(points2[0].x, points2[0].y, points2[1].x, points2[1].y, mSelectionPaint);
		
		canvas.drawArc(ovalRect1, startAngle1, sweepAngle1, false, mSelectionPaint);
		canvas.drawArc(ovalRect2, startAngle2, sweepAngle2, false, mSelectionPaint);
		
    }
    
    private void updateRadius()
    {
		Rect r = new Rect(0, 0, 0, 0);
		getGlobalVisibleRect(r, null);
		
		int panelWidth = r.right - r.left;
		int panelHeight = r.bottom - r.top;
		
		int wordSearchWidth = panelWidth - PADDING * 2;
		int wordSearchHeight = panelHeight - PADDING * 2;
		wordSearchWidth = wordSearchHeight = Math.min(wordSearchWidth,  wordSearchHeight);
		
		float colWidth = ((float) wordSearchWidth) / mOrder;
		mRadius = (int)(colWidth * RADIUS_FACTOR);
		
		//update the corner radius for each cell

		if (mRadiusChoice == 0)
		{
			mCellCornerRadius = 0;
		}
		else if (mRadiusChoice == 1)
		{
			mCellCornerRadius = (int)(colWidth/8);
		}
		else
		{
			mCellCornerRadius = (int)(colWidth/2);
		}
    }
    
    //get the end center points of a word, given teh index into the maping list.
    private Point[] getEndPointsForWord(int mappingIndex)
    {
    	Point[] points = new Point[2];
    	
    	int val[] = mWordMappingList.elementAt(mappingIndex);
    	int dir = val[1];
    	int startRow = val[2], startCol = val[3];
    	//start point
    	points[0] = getCenterPointForCell(startRow, startCol);
    	int wordLen = mWords.elementAt(val[0]).length();
    	
    	int endRow = startRow, endCol = startCol;
    	switch (dir)
    	{
    	case WordSearch.DIR_TOP_RIGHT:
    		endRow -= (wordLen - 1);
    		endCol += (wordLen - 1);
    		break;
    	case WordSearch.DIR_RIGHT:
    		endCol += (wordLen - 1);
    		break;
    	case WordSearch.DIR_BOTTOM_RIGHT:
    		endRow += (wordLen - 1);
    		endCol += (wordLen - 1);
    		break;
    	case WordSearch.DIR_BOTTOM:
    		endRow += (wordLen - 1);
    		break;
    	case WordSearch.DIR_BOTTOM_LEFT:
    		endRow += (wordLen - 1);
    		endCol -= (wordLen - 1);
    		break;
    	case WordSearch.DIR_LEFT:
    		endCol -= (wordLen - 1);
    		break;
    	case WordSearch.DIR_TOP_LEFT:
    		endRow -= (wordLen - 1);
    		endCol -= (wordLen - 1);
    		break;
    	case WordSearch.DIR_TOP:
    		endRow -= (wordLen - 1);
    		break;
    	}
    	
    	//end point
    	points[1] = getCenterPointForCell(endRow, endCol);
    	
    	return points;
    }
    public void setOrder(int order)
    {
    	mOrder = order;
    }

	public boolean onTouchEvent(View v, MotionEvent event) {
		return false;
	}

}