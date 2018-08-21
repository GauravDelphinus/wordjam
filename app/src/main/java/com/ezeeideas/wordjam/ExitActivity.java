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

import java.util.HashSet;
import java.util.Set;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class ExitActivity extends Activity implements OnClickListener, AdListener
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exit);
        
		mExitButton = (Button) findViewById(R.id.exit_button);
		mExitButton.setOnClickListener(this);
		
		mAboutButton = (Button) findViewById(R.id.rate_button);
		mAboutButton.setOnClickListener(this);
		
		mFeedbackButton = (Button) findViewById(R.id.feedback_button);
		mFeedbackButton.setOnClickListener(this);
		
		mReenterGameButton = (Button) findViewById(R.id.reenter_game_button);
		mReenterGameButton.setOnClickListener(this);
		
        //set up the ads
        mBannerAdView = (AdView) findViewById(R.id.adView);
        mBannerAdView.setAdListener(this);
        
        loadBannerAd();
    }
	
	public void onClick(View arg0) 
	{
		if (arg0.getId() == R.id.exit_button)
		{
			this.finish();
		}
		else if (arg0.getId() == R.id.rate_button)
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.APP_PNAME)));
		}
		else if (arg0.getId() == R.id.reenter_game_button)
		{
			startActivity(new Intent(this, WordJam.class));
			this.finish();
		}
		else if (arg0.getId() == R.id.feedback_button)
		{
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
					"mailto",Constants.SUPPORT_EMAIL, null));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.support_email_subject));
			startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.support_email_title)));
		}
	}
	
	/**
	 * Load the banner ad.  This should be done after
	 * the game data has been loaded so we can target
	 * ads based on game content
	 */
	private void loadBannerAd()
	{
		AdRequest adRequest = new AdRequest();
		Set<String> keywords = new HashSet<String>();
		Utils.addKeywords(keywords);
		adRequest.setKeywords(keywords);
		
		Utils.Log("gauravad: loadBannerAd with keywords");
		mBannerAdView.loadAd(adRequest);
	}
	
	public void onDismissScreen(Ad arg0) 
	{		
	}

	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) 
	{
		Utils.Log("gauravad: ExitActivity: onFailedToReceiveAd");
	}

	public void onLeaveApplication(Ad arg0) 
	{		
	}

	public void onPresentScreen(Ad arg0) 
	{
	}

	public void onReceiveAd(Ad arg0) 
	{
		Utils.Log("gauravad: ExitActivity: onReceiveAd");
	}

	private Button mExitButton, mAboutButton, mFeedbackButton, mReenterGameButton;
	private AdView mBannerAdView; // the banner ad view

}