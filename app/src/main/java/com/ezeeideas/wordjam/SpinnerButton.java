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

import com.ezeeideas.wordjam.PieButton.PieButtonListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class SpinnerButton extends PieButton implements OnGlobalLayoutListener
{
	public SpinnerButton(Context context) {
		super(context);
		
		initialize(context);
	}
	
	public SpinnerButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
				
		initialize(context);
	}
	
	public SpinnerButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);		
				
		initialize(context);
	}
	
	private void initialize(Context context)
	{
		mSpinnerListener = (SpinnerButtonListener) context;
		
		setScaleType(ScaleType.MATRIX);
		
        // initialize the matrix only once
        if (mMatrix == null) {
        	mMatrix = new Matrix();
        } else {
        	// not needed, you can also post the matrix immediately to restore the old state
        	mMatrix.reset();
        }

        mGestureDetector = new GestureDetector(new MyGestureDetector());
        
        // there is no 0th quadrant, to keep it simple the first value gets ignored
        quadrantTouched = new boolean[] { false, false, false, false, false };
        
        allowRotating = true;
        
        setImageResource(R.drawable.game_options_button_normal);
        
        //setOnTouchListener(new MyOnTouchListener());
		getViewTreeObserver().addOnGlobalLayoutListener(this);
		
		//set up diameter for the central push button
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int dpi = metrics.densityDpi;
		
		mPushButtonRadius = (int)(SPINNER_PUSH_BUTTON_DIAMETER_INCHES * dpi / 2);
	}
	
	/**
	 * Set the attributes that define this SpinnerButton
	 * 
	 *              -----*  <---- Angle that needs to be passed as it's the first line above the horizontal, anticlockwise axis
	 *            / \ 0 / \
	 *           /   \_/   \
	 *           | 1 |s| 2  |  s = spin button location
	 *           \    -    /
	 *            \___|___/
	 *              
	 * 
	 * @param buttonResources The resource IDs corresponding to the buttons, starting with 0th to nth in anticlockwise order
	 * @param startAngle The starting angle of the 0th button (measured from horizontal, right axis measured anticlockwise)
	 */
	public void setAttributes(int startAngle, int buttonNormalResource, Vector<Integer> buttonPressedResources, int spinButtonPressedResource)
	{
		super.setAttributes(startAngle, buttonNormalResource, buttonPressedResources);
		
		mSpinButtonPressedResource = spinButtonPressedResource;
		
        // load the image only once
        if (mImageOriginal == null) {
        	mImageOriginal = BitmapFactory.decodeResource(getResources(), mButtonNormalResource);
        }
	}

	public void onGlobalLayout() 
	{
		//Utils.Log("onGlobalLayout - mDialerWidth = " + mDialerWidth + ", mDialerHeight = " + mDialerHeight + ", w = " + getWidth() + ", h = " + getHeight());
		// method called more than once, but the values only need to be initialized one time
		if (mDialerHeight == 0 || mDialerWidth == 0) {
			mDialerHeight = getHeight();
			mDialerWidth = getWidth();

			//Utils.Log("onGlobalLayout, mImageOriginal width = " + mImageOriginal.getWidth() + ", height = " + mImageOriginal.getHeight() + ", dialerWidth = " + mDialerWidth + ", dialerHeight = " + mDialerHeight);
			// resize
			//Matrix resize = new Matrix();
			//resize.postScale((float)Math.min(mDialerWidth, mDialerHeight) / (float)mImageOriginal.getWidth(), (float)Math.min(mDialerWidth, mDialerHeight) / (float)mImageOriginal.getHeight());
			//mImageScaled = Bitmap.createBitmap(mImageOriginal, 0, 0, mImageOriginal.getWidth(), mImageOriginal.getHeight(), resize, false);

			// translate to the image view's center
			float translateX = mDialerWidth / 2 - mImageOriginal.getWidth() / 2;
			float translateY = mDialerHeight / 2 - mImageOriginal.getHeight() / 2;
			mMatrix.postTranslate(translateX, translateY);

			setImageBitmap(mImageOriginal);
			setImageMatrix(mMatrix);
		}
	}
	
	/**
	 * Rotate the dialer.
	 * 
	 * @param degrees The degrees, the dialer should get rotated.
	 */
	private void rotateDialer(float degrees) {
		//Utils.Log("rotateDialer, degrees = " + degrees);
		mMatrix.postRotate(degrees, mDialerWidth / 2, mDialerHeight / 2);
		
		setImageMatrix(mMatrix);
		
		mRotation += degrees;
		if (mRotation < 0)
		{
			mRotation = (-mRotation % 360);
		}
		else
		{
			mRotation = mRotation % 360;
		}
		int index = getCurrentLeverIndex();
		Utils.Log("current rotation = " + mRotation + ", lever index = " + index);
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
			case 3:
				return 180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
			
			case 4:
				return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
			
			default:
				// ignore, does not happen
				return 0;
		}
	}
	
	private int getRadius(double xTouch, double yTouch)
	{
		double x = xTouch - (mDialerWidth / 2d);
		double y = mDialerHeight - yTouch - (mDialerHeight / 2d);
		
		return (int) (Math.sqrt(x * x + y * y));
	}
	
	/**
	 * @return The selected quadrant.
	 */
	private static int getQuadrant(double x, double y) {
		if (x >= 0) {
			return y >= 0 ? 1 : 4;
		} else {
			return y >= 0 ? 2 : 3;
		}
	}
	
	/**
	 * Return the currently selected pie index pointing to the lever
	 * 
	 * @return
	 */
	private int getCurrentLeverIndex()
	{
		int pieArc = 360 / mNumButtons;
		int startAngle = (int)mRotation + pieArc/2;
		int indexOfBottomPie = startAngle / pieArc;
		int index = (indexOfBottomPie + 4) % mNumButtons; 
		return index;
	}
	
	/**
	 * Simple implementation of a {@link SimpleOnGestureListener} for detecting a fling event. 
	 */
	private class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			
			// get the quadrant of the start and the end of the fling
			int q1 = getQuadrant(e1.getX() - (mDialerWidth / 2), mDialerHeight - e1.getY() - (mDialerHeight / 2));
			int q2 = getQuadrant(e2.getX() - (mDialerWidth / 2), mDialerHeight - e2.getY() - (mDialerHeight / 2));

			// the inversed rotations
			if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
					|| (q1 == 3 && q2 == 3)
					|| (q1 == 1 && q2 == 3)
					|| (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
					|| ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
					|| ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
					|| (q1 == 2 && q2 == 4 && quadrantTouched[3])
					|| (q1 == 4 && q2 == 2 && quadrantTouched[3])) {
			
				post(new FlingRunnable(-1 * (velocityX + velocityY)));
			} else {
				// the normal rotation
				post(new FlingRunnable(velocityX + velocityY));
			}

			return true;
		}
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			//Utils.Log("ACTION_DOWN");
			if (getRadius(x, y) < mPushButtonRadius)
			{
				setImageResource(mSpinButtonPressedResource);
				mPushButtonPressed = true;
				return true;
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			//Utils.Log("ACTION_UP, pressure = " + event.getPressure() + ", size = " + event.getSize());
			
			if (getRadius(x, y) < mPushButtonRadius)
			{
				if (mPushButtonPressed)
				{
					mPushButtonPressed = false;
					setImageResource(mButtonNormalResource);
					
					//start the fling
					mSpinnerListener.onSpinnerStartedRotating();
					int velocity = Utils.getRandomNumberInRange(FLING_VELOCITY_RANGE_START, FLING_VELOCITY_RANGE_END);
					post(new FlingRunnable(velocity));
					return true;
				}
			}		
		}
		
		return super.onTouchEvent(event);
	}
	
	/*
	public boolean onTouchEvent(MotionEvent event)
	{
		//Utils.Log("onTouchEvent, x = " + event.getX() + ", y = " + event.getY());
		
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			//Utils.Log("onTouch, ACTION_DOWN");

			// reset the touched quadrants
			for (int i = 0; i < quadrantTouched.length; i++) {
				quadrantTouched[i] = false;
			}

			allowRotating = false;

			mStartAngle = getAngle(event.getX(), event.getY());
			break;

		case MotionEvent.ACTION_MOVE:
			//Utils.Log("onTouch, ACTION_MOVE");
			double currentAngle = getAngle(event.getX(), event.getY());
			rotateDialer((float) (mStartAngle - currentAngle));
			mStartAngle = currentAngle;
			break;

		case MotionEvent.ACTION_UP:
			//Utils.Log("onTouch, ACTION_UP");
			allowRotating = true;
			break;
		}

		// set the touched quadrant to true
		quadrantTouched[getQuadrant(event.getX() - (mDialerWidth / 2), mDialerHeight - event.getY() - (mDialerHeight / 2))] = true;

		mGestureDetector.onTouchEvent(event);
		super.onTouchEvent(event);
		
		return true;
	}
	*/
	
	

	private class MyOnTouchListener implements OnTouchListener {
		
		private double startAngle;

		public boolean onTouch(View v, MotionEvent event) {

			//Utils.Log("onTouch, x = " + event.getX() + ", y = " + event.getY());
			
			switch (event.getAction()) {
				
				case MotionEvent.ACTION_DOWN:
					//Utils.Log("onTouch, ACTION_DOWN");
					
					// reset the touched quadrants
					for (int i = 0; i < quadrantTouched.length; i++) {
						quadrantTouched[i] = false;
					}
					
					allowRotating = false;
					
					startAngle = getAngle(event.getX(), event.getY());
					break;
					
				case MotionEvent.ACTION_MOVE:
					//Utils.Log("onTouch, ACTION_MOVE");
					double currentAngle = getAngle(event.getX(), event.getY());
					rotateDialer((float) (startAngle - currentAngle));
					startAngle = currentAngle;
					break;
					
				case MotionEvent.ACTION_UP:
					//Utils.Log("onTouch, ACTION_UP");
					allowRotating = true;
					break;
			}
			
			// set the touched quadrant to true
			quadrantTouched[getQuadrant(event.getX() - (mDialerWidth / 2), mDialerHeight - event.getY() - (mDialerHeight / 2))] = true;
			
			mGestureDetector.onTouchEvent(event);
			
			
			return true;
		}
	}
	
	
	/**
	 * A {@link Runnable} for animating the the dialer's fling.
	 */
	private class FlingRunnable implements Runnable {

		private float velocity;

		public FlingRunnable(float velocity) {
			this.velocity = velocity;
		}

		public void run() {
			if (Math.abs(velocity) > 5 && allowRotating) 
			{
				rotateDialer(velocity / 75);
				velocity /= 1.0666F;

				// post this instance again
				post(this);
			}
			else
			{
				//rotation has stopped
				mSpinnerListener.onSpinnerStoppedRotating(getCurrentLeverIndex());
			}
		}
	}
	
	/**
	 * The listener (typically the calling Activity) that needs to listen to
	 * events from the SpinnerButton (to know where the lever points, etc.)
	 */
	public interface SpinnerButtonListener
	{
		/**
		 * The spinner started rotating
		 */
		public void onSpinnerStartedRotating();
		
		/**
		 * The spinner stopped rotating
		 * @param index The index of the button, that the lever is now pointing at
		 */
		public void onSpinnerStoppedRotating(int leverIndex);
	}
	
	private int mDialerHeight, mDialerWidth;
	private static Bitmap mImageOriginal, mImageScaled;
	private static Matrix mMatrix;
	private GestureDetector mGestureDetector;
	// needed for detecting the inversed rotations
	private boolean[] quadrantTouched;
	private boolean allowRotating;
	double mStartAngle = 0;
	float mRotation = 0;
		
	private static final double SPINNER_PUSH_BUTTON_DIAMETER_INCHES = 0.5; //minimum size of central push button in inches for comfortable tapping
	private static int mPushButtonRadius; //diameter of push button in pixels, calculated from the inches
	private static final int FLING_VELOCITY_RANGE_START = 5000; //the fling velcoity with which to spin the wheel on pushing the button
	private static final int FLING_VELOCITY_RANGE_END = 10000; //the fling velcoity with which to spin the wheel on pushing the button
	private boolean mPushButtonPressed = false;
	private int mSpinButtonPressedResource; // resource ID of the buttons in normal condition (non-pressed)
	
	private SpinnerButtonListener mSpinnerListener; //listener to the spinner button press events, usually an Activity that shows the button
}