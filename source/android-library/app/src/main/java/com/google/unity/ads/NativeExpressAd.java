/*
 * Copyright (C) 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.unity.ads;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Native express ad implementation for the Google Mobile Ads Unity plugin.
 */
public class NativeExpressAd {

    /**
     * The {@link NativeExpressAdView} to display to the user.
     */
    private NativeExpressAdView mAdView;

    /**
     * The {@link Activity} that the native express ad will be displayed in.
     */
    private Activity mUnityPlayerActivity;

    /**
     * A listener implemented in Unity via {@code AndroidJavaProxy} to receive ad events.
     */
    private UnityAdListener mUnityListener;

    /**
     * Creates an instance of {@code NativeExpressAd}.
     *
     * @param activity The {@link Activity} that will contain an ad.
     * @param listener The {@link UnityAdListener} used to receive ad events in Unity.
     */
    public NativeExpressAd(Activity activity, UnityAdListener listener) {
        this.mUnityPlayerActivity = activity;
        this.mUnityListener = listener;
    }

    /**
     * Creates a {@link NativeExpressAdView} to hold native express ads.
     *
     * @param publisherId  Your ad unit ID.
     * @param adSize       The size of the native express ad.
     * @param positionCode A code indicating where to place the ad.
     */
    public void create(final String publisherId, final AdSize adSize, final int positionCode) {
        mUnityPlayerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdView = new NativeExpressAdView(mUnityPlayerActivity);
                // Setting the background color works around an issue where the first ad isn't
                // visible.
                mAdView.setBackgroundColor(Color.TRANSPARENT);
                mAdView.setAdUnitId(publisherId);
                mAdView.setAdSize(adSize);
                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (mUnityListener != null) {
                            mUnityListener.onAdLoaded();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        if (mUnityListener != null) {
                            mUnityListener.onAdFailedToLoad(PluginUtils.getErrorReason(errorCode));
                        }
                    }

                    @Override
                    public void onAdOpened() {
                        if (mUnityListener != null) {
                            mUnityListener.onAdOpened();
                        }
                    }

                    @Override
                    public void onAdClosed() {
                        if (mUnityListener != null) {
                            mUnityListener.onAdClosed();
                        }
                    }

                    @Override
                    public void onAdLeftApplication() {
                        if (mUnityListener != null) {
                            mUnityListener.onAdLeftApplication();
                        }
                    }
                });
                FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams
                        .WRAP_CONTENT);
                adParams.gravity = PluginUtils.getLayoutGravityForPositionCode(positionCode);
                mUnityPlayerActivity.addContentView(mAdView, adParams);
            }
        });
    }

    /**
     * Loads an ad on a background thread.
     *
     * @param request The {@link AdRequest} object with targeting parameters.
     */
    public void loadAd(final AdRequest request) {
        mUnityPlayerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(PluginUtils.LOGTAG, "Calling loadAd() on NativeExpressAdView");
                mAdView.loadAd(request);
            }
        });
    }

    /**
     * Sets the ad size for the {@link NativeExpressAdView}.
     */
    public void setAdSize(final AdSize adSize) {
        mUnityPlayerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdView.setAdSize(adSize);
            }
        });
    }

    /**
     * Sets the {@link NativeExpressAdView} to be visible.
     */
    public void show() {
        mUnityPlayerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(PluginUtils.LOGTAG, "Calling show() on NativeExpressAdView");
                mAdView.setVisibility(View.VISIBLE);
                mAdView.resume();
            }
        });
    }

    /**
     * Sets the {@link NativeExpressAdView} to be gone.
     */
    public void hide() {
        mUnityPlayerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(PluginUtils.LOGTAG, "Calling hide() on NativeExpressAdView");
                mAdView.setVisibility(View.GONE);
                mAdView.pause();
            }
        });
    }

    /**
     * Destroys the {@link NativeExpressAdView}.
     */
    public void destroy() {
        mUnityPlayerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(PluginUtils.LOGTAG, "Calling destroy() on NativeExpressAdView");
                mAdView.destroy();
                ViewParent parentView = mAdView.getParent();
                if (parentView != null && parentView instanceof ViewGroup) {
                    ((ViewGroup) parentView).removeView(mAdView);
                }
            }
        });
    }
}

