package com.yahoo.shopping.twitterclient;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jamesyan on 8/11/15.
 */
public class PostTweetDialogFragment extends DialogFragment {

    private static PostTweetDialogFragment sDialogFragment;

    public static PostTweetDialogFragment newInstance(String title) {
        if (sDialogFragment == null) {
            sDialogFragment = new PostTweetDialogFragment();
        }

        // write code for title

        return sDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post_tweet, container);

        return view;
    }
}
