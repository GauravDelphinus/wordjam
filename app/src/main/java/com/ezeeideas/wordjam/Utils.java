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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.content.SharedPreferences;
import android.app.Dialog;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View.OnClickListener;

public final class Utils
{
	private static Toast mToast = null;
	
	private static void cancelAndShowToast(Context context, String text, int duration)
	{
		if (mToast != null)
		{
			mToast.cancel();
		}
		
		mToast = Toast.makeText(context, text, duration);
		mToast.show();
	}
	
	private Utils()
	{
		
	}
	
	/**
	 * Encode the string such that it becomes possible to store in a DB without
	 * conflicts when it is part of an SQL statement.  E.g., remove quotes, etc.
	 * 
	 * @param str The string to be encoded
	 * @return The encoded string
	 */
	static String encodeStringForDB(String str)
	{
		String out = str.replaceAll("'", "&");
		return out;
	}
	
	/**
	 * Decode a string from the DB to its original format.  Basically, undo
	 * whatever was done in encodeStringForDB
	 * 
	 * @param str The string to be decoded
	 * @return The decoded string
	 */
	static String decodeStringFromDB(String str)
	{
		String out = str.replaceAll("&",  "'");
		return out;
	}
	
	static String sanitizeString(String str, boolean replaceUnderscores, boolean removeHyphens, boolean removeBrackets, boolean lettersOnly, boolean removeTrailingCharacters)
	{
		String sanitizedWord = str.trim();
		if (replaceUnderscores)
		{
			sanitizedWord = sanitizedWord.replaceAll("_", " ");
		}
		
		if (removeHyphens)
		{
			sanitizedWord = sanitizedWord.replaceAll("-", "");
		}
		
		if (removeBrackets)
		{
			sanitizedWord = sanitizedWord.replaceAll("\\(.*\\)$", "");
		}
		
		if (lettersOnly)
		{
			sanitizedWord = sanitizedWord.replaceAll("[^A-Za-z]", "");
		}
		
		if (removeTrailingCharacters)
		{
			sanitizedWord = sanitizedWord.replaceAll(";$", "");
		}
		
		return sanitizedWord;
	}
	
	/*
	static void showTestSummary(Context context, int numTests, int numPassed, int numHints)
	{
		int duration = Toast.LENGTH_LONG;

		String text = String.format(context.getResources().getString(R.string.test_summary), numTests, numPassed, numHints);
		
		cancelAndShowToast(context, text, duration);
	}
	*/
	
	static void showTestResultPass(Context context)
	{
		/*
		int duration = Toast.LENGTH_SHORT;

		String text = context.getResources().getString(R.string.test_success);
		cancelAndShowToast(context, text, duration);
		*/
		/*
		
		TestResultDialog dialog = new TestResultDialog(context);

		dialog.setContentView(R.layout.test_pass);
		dialog.setTitle("Success!");
		

		String text = context.getResources().getString(R.string.test_success);
		TextView textView = (TextView) dialog.findViewById(R.id.test_pass_text);
		textView.setText(text);
		
		dialog.show();
		*/
		/*
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.test_pass, null);

		String text = context.getResources().getString(R.string.test_success);
		TextView textView = (TextView) layout.findViewById(R.id.test_pass_text);
		textView.setOnClickListener(new OnClickListener())
		textView.setText(text);

		builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		alertDialog = builder.create();
		
		alertDialog.show();
		*/
	}
	
	static void showTestResultFailed(Context context, String correctAnswer)
	{
		int duration = Toast.LENGTH_LONG;

		String text = String.format(context.getResources().getString(R.string.test_fail), correctAnswer);
		cancelAndShowToast(context, text, duration);
	}
	
	static void showTestResultFailedEx(Context context, String message, String correctAnswer)
	{
		int duration = Toast.LENGTH_LONG;

		String text = String.format(message, correctAnswer);
		cancelAndShowToast(context, text, duration);
	}
	
	static void showGenericToast(Context context, String message)
	{
		int duration = Toast.LENGTH_SHORT;

		cancelAndShowToast(context, message, duration);
	}
	/*
	static boolean showYesNoAlert(Context context, String title, String message)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(message);
		dialogBuilder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	return true;
            }
        });
		dialogBuilder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	return false;
            }
        });
		
	}
	*/
	
	public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + mContext.getResources().getString(R.string.app_name));

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using " + mContext.getResources().getString(R.string.app_name) + ", please take a moment to rate it. Thanks for your support!");
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);
        
        Button b1 = new Button(mContext);
        b1.setText("Rate " + mContext.getResources().getString(R.string.app_name));
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.APP_PNAME)));
                dialog.dismiss();
            }
        });        
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setText("No, thanks");
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);        
        dialog.show();        
    }
	
	public static void Log(String message)
	{
		
		boolean enableLogging = false;
		if (enableLogging)
		{
			Log.d("gaurav", message);
		}
		
	}
	
	// Implementing Fisher Yates shuffle
	public static void shuffleArray(int[] ar, int length)
	{
		Random rnd = new Random();
	    for (int i = length - 1; i >= 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	}
	
	public static int[] getRandList(int length)
	{
		int[] list = new int[length];
		int i;
		for (i = 0; i < length; i++)
		{
			list[i] = i;
		}
		shuffleArray(list, length);
		
		return list;
	}
	
	/**
	 * Copy bytes from one file to another.
	 * 
	 * @param fromFile source file
	 * @param toFile target file
	 * @throws IOException
	 */
	public static void copyFile(InputStream fromFile, OutputStream toFile) throws IOException {
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = fromFile.read(buffer)) > 0) {
                toFile.write(buffer, 0, length);
                //Log.d(null, "#### written " + length + " bytes to the output file....");
            }
        }
        // Close the streams
        finally 
        {
            try 
            {
                if (toFile != null) 
                {
                    try 
                    {
                        toFile.flush();
                    } finally 
                    {
                        toFile.close();
                    }
                }
            } finally 
            {
                if (fromFile != null) 
                {
                    fromFile.close();
                }
            }
        }
    }
	
	public static class RandomSelection {
		public static List<Integer> getRandomList(int requiredCount, int low, int high)
		{
			List<Integer> list = new ArrayList<Integer>();
			if (requiredCount > (high - low + 1))
			{
				return list;
			}
			int totalCount = high - low + 1;
			Random random = new Random();
			while (list.size() < requiredCount)
			{
				Integer next = random.nextInt(totalCount) + low;
				if (!list.contains(next))
				{
					list.add(next);
				}
			}
			
			return list;
		}
	}
	
	public static String convertTime(long time)
	{
		long secs = (long)(time/1000); 
		long mins = (long)((time/1000)/60); 
		long hrs = (long)(((time/1000)/60)/60);
		
		/* Convert the seconds to String * and format to ensure it has * a leading zero when required */ 
		secs = secs % 60; 
		String seconds = String.valueOf(secs); 
		if(secs == 0)
		{ 
			seconds = "00"; 
		} 
		if (secs < 10 && secs > 0)
		{ 
			seconds = "0"+seconds; 
		} 
		
		/* Convert the minutes to String and format the String */ 
		mins = mins % 60; 
		String minutes = String.valueOf(mins); 
		if (mins == 0)
		{ 
			 minutes = "00"; 
		} 
		if(mins < 10 && mins > 0)
		{ 
			minutes = "0" + minutes; 
		} 
		
		/* Convert the hours to String and format the String */ 
		String hours = String.valueOf(hrs); 
		if (hrs == 0)
		{ 
			hours = "00"; 
		} 
		if(hrs < 10 && hrs > 0)
		{ 
			hours = "0"+hours; 
		} 

		/* Setting the timer text to the elapsed time */ 
		//String out = hours + ":" + minutes + ":" + seconds; 
		String out = minutes + ":" + seconds; 
		
		return out;
	}
	
	public static int getRandomNumberInRange(int minInclusive, int maxInclusive)
	{
		Random random = new Random();
		int randomNum = random.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
		return randomNum;
	}
	
	public static void addKeywords(Set<String> keywords)
	{
		keywords.add("Vocabulary");
		keywords.add("GRE");
		keywords.add("SAT");
		keywords.add("TOEFL");
		keywords.add("ELTS");
		keywords.add("English");
		keywords.add("grammar");
		keywords.add("spelling");
		keywords.add("jumble");
		keywords.add("crossword");
		keywords.add("dictionary");
		keywords.add("thesaurus");
		keywords.add("hangman");
		keywords.add("word search");
		keywords.add("word");
		keywords.add("word games");
		keywords.add("Math");
		keywords.add("study");
		keywords.add("meaning");
		keywords.add("school");
		keywords.add("college");
		keywords.add("university");
		keywords.add("language");
	}
	
	/**
	 * Create a Bitmap from the given view.
	 * @param view The given view
	 * @return A bitmap with representing the view
	 */
	public static Bitmap getBitmapFromView(View view) 
	{
		//Define a bitmap with the same size as the view
		Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		//Bind a canvas to it
		Canvas canvas = new Canvas(returnedBitmap);
		//Get the view's background
		Drawable bgDrawable =view.getBackground();
		if (bgDrawable!=null) 
			//has background drawable, then draw it on the canvas
			bgDrawable.draw(canvas);
		else 
			//does not have background drawable, then draw white background on the canvas
			canvas.drawColor(Color.WHITE);
		// draw the view on the canvas
		view.draw(canvas);
		//return the bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        returnedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return returnedBitmap;
    }

}