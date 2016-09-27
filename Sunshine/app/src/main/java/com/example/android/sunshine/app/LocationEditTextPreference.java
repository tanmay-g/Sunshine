package com.example.android.sunshine.app;

import android.app.Activity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by tanmay.godbole on 30-08-2016
 */
public class LocationEditTextPreference extends EditTextPreference{


    private static final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    private static final int DEFAULT_MIN_LENGTH = 2;
    private int mMinLen;

    int PLACE_PICKER_REQUEST = 1;

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

        // Check to see if Google Play services is available. The Place Picker API is available
        // through Google Play services, so if this is false, we'll just carry on as though this
        // feature does not exist. If it is true, however, we can add a widget to our preference.
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            // Add the get current location widget to our location preference
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
        else
            Log.i(LOG_TAG, "couldn't start it");
    }

    public LocationEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        if (currentLocation != null) {
            currentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    // Launch the Place Picker so that the user can specify their location, and then
                    // return the result to SettingsActivity.
                    // TODO(student): Create a PlacePicker.IntentBuilder object here.
                    // We are in a view right now, not an activity. So we need to get ourselves
                    // an activity that we can use to start our Place Picker intent. By using
                    // SettingsActivity in this way, we can ensure the result of the Place Picker
                    // intent comes to the right place for us to process it.

                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    Activity settingsActivity = (SettingsActivity) context;
                    try {
                        // TODO(student): Launch the intent using your settingsActivity object to access
                        // startActivityForResult(). You'll need to build your builder object and use
                        // the request code we declared in SettingsActivity.
                        settingsActivity.startActivityForResult(builder.build(settingsActivity), SettingsActivity.PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesNotAvailableException
                            | GooglePlayServicesRepairableException e) {
                        // What did you do?? This is why we check Google Play services in onResume!!!
                        // The difference in these exception types is the difference between pausing
                        // for a moment to prompt the user to update/install/enable Play services vs
                        // complete and utter failure.
                        // If you prefer to manage Google Play services dynamically, then you can do so
                        // by responding to these exceptions in the right moment. But I prefer a cleaner
                        // user experience, which is why you check all of this when the app resumes,
                        // and then disable/enable features based on that availability.
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            });
        }
        return view;
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
