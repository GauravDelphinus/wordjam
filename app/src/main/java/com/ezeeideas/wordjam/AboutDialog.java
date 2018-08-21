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
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class AboutDialog extends Activity implements OnClickListener
{
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
   
		setContentView(R.layout.aboutdialog);
	
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);
		
		ImageButton rateAppButton = (ImageButton) findViewById(R.id.rate_app_button);
		rateAppButton.setOnClickListener(this);
		
		ImageButton feedbackButton = (ImageButton) findViewById(R.id.feedback_button);
		feedbackButton.setOnClickListener(this);
		
		ImageButton facebookButton = (ImageButton) findViewById(R.id.facebook_button);
		facebookButton.setOnClickListener(this);
		
		ImageButton twitterButton = (ImageButton) findViewById(R.id.twitter_button);
		twitterButton.setOnClickListener(this);
		
		TextView versionTextView = (TextView) findViewById(R.id.version_no_str);
		String text = String.format(getResources().getString(R.string.version_str), getResources().getString(R.string.version_no));
		versionTextView.setText(text);
		
		Utils.Log("########### about dialog, default text size is " + versionTextView.getTextSize());
		
		honorTheme();
	}
	
	private void honorTheme()
    {
    	LinearLayout ll = (LinearLayout) findViewById(R.id.main_layout);
    	ll.setBackgroundColor(Themes.getColorFor(Themes.COLOR_BACKGROUND));
    	
    	View ruleHeader = (View) findViewById(R.id.rule_header);
    	ruleHeader.setBackgroundColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
    	
    	View ruleHeader2 = (View) findViewById(R.id.rule_header2);
    	ruleHeader2.setBackgroundColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
    	
    	View ruleFooter = (View) findViewById(R.id.rule_footer);
    	ruleFooter.setBackgroundColor(Themes.getColorFor(Themes.COLOR_LINE_HEADING));
    }
	
    public void onClick(View v)
    {
    	if (v.getId() == R.id.back_button)
    	{
    		this.finish();
    	}
    	else if (v.getId() == R.id.rate_app_button)
    	{
    		Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_RATE_BUTTON, null);
    		
    		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.APP_PNAME)));
    	}
		else if (v.getId() == R.id.feedback_button)
		{
			Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_FEEDBACK_BUTTON, null);
    		
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
					"mailto",Constants.SUPPORT_EMAIL, null));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.support_email_subject));
			startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.support_email_title)));
		}
		else if (v.getId() == R.id.facebook_button)
		{
			Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_FACEBOOK_BUTTON, null);
    		
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FACEBOOK_PAGE_URL));
			startActivity(browserIntent);
		}
		else if (v.getId() == R.id.twitter_button)
		{
			Analytics.trackEvent(this, Analytics.ANALYTICS_CATEGORY_UI_ACTION, Analytics.ANALYTICS_ACTION_BUTTON_PRESS, Analytics.ANALYTICS_LABEL_TWITTER_BUTTON, null);
    		
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TWITTER_PAGE_URL));
			startActivity(browserIntent);
		}
    }
}