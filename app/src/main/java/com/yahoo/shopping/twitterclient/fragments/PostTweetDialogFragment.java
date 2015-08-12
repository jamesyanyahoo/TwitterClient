package com.yahoo.shopping.twitterclient.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yahoo.shopping.twitterclient.R;

/**
 * Created by jamesyan on 8/11/15.
 */
public class PostTweetDialogFragment extends DialogFragment {

    private static PostTweetDialogFragment sDialogFragment;

    public static PostTweetDialogFragment newInstance() {
        if (sDialogFragment == null) {
            sDialogFragment = new PostTweetDialogFragment();
        }

        return sDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("WRITE YOUR TWEET");

        View view = inflater.inflate(R.layout.dialog_post_tweet, container);
        final EditText txtTweet = (EditText) view.findViewById(R.id.dialog_tweet_txt_tweet);
        final TextView tvCounter = (TextView) view.findViewById(R.id.dialog_tweet_tv_tweet_counts);
        Button btnApply = (Button) view.findViewById(R.id.dialog_tweet_btn_apply);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnFinishEditing)getActivity()).onFinishEditing(txtTweet.getText().toString());

                PostTweetDialogFragment.this.dismiss();
            }
        });

        txtTweet.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                tvCounter.setText((140 - txtTweet.getText().length()) + " Character(s) Left");
                return false;
            }
        });

        return view;
    }

    public interface OnFinishEditing {
        void onFinishEditing(String tweet);
    }
}
