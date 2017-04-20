package com.example.caijingwei.devecocontact;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Random;

public class TextListView extends ListView {
    private ArrayAdapter<CharSequence> mAdapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_expandable_list_item_1);
    private static final String BUNDLE_KEY = "data";

    public TextListView(Context context) {
        super(context);
    }

    public TextListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            addMessage(message.getData().getString(BUNDLE_KEY));
            return false;
        }
    });

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setAdapter(mAdapter);
    }

    private void addMessageFromChildThread(CharSequence text) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY, text.toString());
        Message msg = Message.obtain();
        msg.setData(bundle);
        mUIHandler.sendMessage(msg);
    }

    public void addMessage(CharSequence text) {
        if (!Helper.isMainThread()) {
            addMessageFromChildThread(text);
            return;
        }
        mAdapter.insert(text, 0);
        mAdapter.notifyDataSetChanged();
    }
}
