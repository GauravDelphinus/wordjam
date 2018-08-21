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
import android.view.MotionEvent;
import android.widget.ImageView;

class PieButton extends ImageView
{
	public PieButton(Context context) {
		super(context);

		initialize(context);
	}
	
	public PieButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		initialize(context);
	}
	
	public PieButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		initialize(context);
	}
	
	private void initialize(Context context)
	{
		mListener = (PieButtonListener) context;
	}
	
	/**
	 * Set the attributes that define this PieButton
	 * 
	 *              -----*  <---- Angle that needs to be passed as it's the first line above the horizontal, anticlockwise axis
	 *            / \ 0 / \
	 *           /   \ /   \
	 *           | 1  *  2 |
	 *           \    |    /
	 *            \___|___/
	 *              
	 * 
	 * @param buttonResources The resource IDs corresponding to the buttons, starting with 0th to nth in anticlockwise order
	 * @param startAngle The starting angle of the 0th button (measured from horizontal, right axis measured anticlockwise)
	 */
	public void setAttributes(int startAngle, int buttonNormalResource, Vector<Integer> buttonPressedResources)
	{
		mStartingAngle = startAngle;
		mButtonNormalResource = buttonNormalResource;
		mButtonPressedResources = buttonPressedResources;
		mPressedButtonIndex = -1;
		mNumButtons = mButtonPressedResources.size();
		
		//calculate button angles
		int arcSizeForEachButton = 360 / mButtonPressedResources.size();
		mButtonAngles = new Vector<int[]>();
		for (int i = 0; i < mButtonPressedResources.size(); i++)
		{
			int startingAngle = (mStartingAngle + arcSizeForEachButton * i) % 360;
			int endAngle = (mStartingAngle + arcSizeForEachButton * (i+1)) % 360;
			mButtonAngles.add(new int[]{startingAngle, endAngle});
		}
	}
	
	/**
	 * Return the pie (slice/button) index that contains
	 * the given point.  The coordinates start at the top
	 * left of the PieButton view.
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @return Index of Pie Slice, or -1 if point doesn't fall anywhere inside the PieButton
	 */
	private int getPieIndexForPoint(int x, int y)
	{
		int w = getWidth();
		int h = getHeight();
		int cx = w/2;
		int cy = h/2;
		int dx = x - cx;
		int dy = cy - y;
		int opp = dy;
		int adj = dx;
		double tan = 1.0 * opp / adj;
		double radians = Math.atan(tan);
		int degrees = (int)Math.toDegrees(radians);
		int angle = 0;
		
		if (dx >= 0 && dy >= 0) //first quadrant
		{
			angle = degrees;
		}
		else if (dx < 0 && dy >= 0) //second quadrant
		{
			angle = 180 + degrees;
		}
		else if (dx < 0 && dy < 0) //third quadrant
		{
			angle = 180 + degrees;
		}
		else if (dx >= 0 && dy < 0) //fourth quadrant
		{
			angle = 360 + degrees;
		}
		
		int foundIndex = -1;
		for (int i = 0; i < mButtonAngles.size(); i++)
		{
			int vals[] = mButtonAngles.get(i);
			Utils.Log("button angles " + i + ": " + vals[0] + ", " + vals[1] + ", angle = " + angle);
			if ((vals[0] <= vals[1]) && (angle >= vals[0] && angle <= vals[1]))
			{
				//found the button
				foundIndex = i;
				break;
			}
			else if ((vals[1] < vals[0]) && ((angle <= 360 && angle >= vals[0]) || (angle >= 0 && angle <= vals[1])))
			{
				//found the button
				foundIndex = i;
				break;
			}
		}
		
		return foundIndex;
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		
		//Utils.Log("PieButton.onTouchevent w = " + w + ", h = " + h + ", cx = " + cx + ", cy = " + cy + ", x = " + x + ", y = " + y + ", opp = " + opp + ", adj = " + adj + ", tan = " + tan + ", radians = " + radians + ", degrees = " + degrees);
		//Utils.Log("PieButton.onTouchEvent, angle = " + angle);
		
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			Utils.Log("ACTION_DOWN");
			int pieIndex = getPieIndexForPoint(x, y);
			Utils.Log("pieIndex = " + pieIndex);
			if (pieIndex != -1)
			{
				setImageResource(mButtonPressedResources.get(pieIndex));
				mPressedButtonIndex = pieIndex;
			}
			else
			{
				setImageResource(mButtonNormalResource);
				mPressedButtonIndex = -1;
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			Utils.Log("ACTION_UP");
			setImageResource(mButtonNormalResource);
			
			int pieIndex = getPieIndexForPoint(x, y);
			if (pieIndex != -1 && pieIndex == mPressedButtonIndex)
			{
				mListener.onPieButtonSelected(this, pieIndex);
			}
			
			mPressedButtonIndex = -1;
		}
		return true;
	}
	
	/**
	 * The listener (typically the calling Activity) that needs to listen to
	 * events from the PieButton (to know which button was clicked)
	 */
	public interface PieButtonListener
	{
		/**
		 * Let the listener know that a button was selected from the pie.
		 * @param button The button class
		 * @param index The index of the button, starting with 0
		 */
		public void onPieButtonSelected(PieButton button, int index);
	}
	
	/**
	 * Member variables
	 */
	protected int mNumButtons; // number of buttons in the pie, assuming equal sized
	protected int mStartingAngle; // angle of 0th slice/button in anti clockwise direction, in degrees
	protected int mButtonNormalResource; // resource ID of the buttons in normal condition (non-pressed)
	protected Vector<Integer> mButtonPressedResources; // list of resource IDs corresponding to the pressed buttons, in anticlockwise order
	private Vector<int[]> mButtonAngles; // list of button angle ranges corresponding to each button, in anticlockwise order
	private int mPressedButtonIndex; //index of the button currently pressed, or -1 if none pressed
	
	private PieButtonListener mListener; //listener to the pie button press events, usually an Activity that shows the button
}