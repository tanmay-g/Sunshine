package com.example.android.sunshine.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by tanmay.godbole on 30-08-2016
 */
public class LocationEditTextPreference extends EditTextPreference{


    private static final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    private static final int DEFAULT_MIN_LENGTH = 2;
    private int mMinLen;

    public LocationEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.locationEditTextPreference,
                0, 0);

        try {
            mMinLen = a.getInteger(R.styleable.locationEditTextPreference_minLength, DEFAULT_MIN_LENGTH);
        } finally {
            a.recycle();
        }
    }

    public LocationEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        Log.i(LOG_TAG, String.valueOf(mMinLen));
        return super.onCreateView(parent);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        EditText innerText = getEditText();
        innerText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();
                if (d instanceof AlertDialog){
                    ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() >= mMinLen);
                }

            }
        });

    }

    public int getmMinLen() {
        return mMinLen;
    }

    public void setmMinLen(int mMinLen) {
        if (mMinLen >= 0)
            this.mMinLen = mMinLen;
//        else
//            throw new Exception("Can't set negative min length");
    }
}
