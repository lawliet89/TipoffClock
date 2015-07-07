package io.gitub.lawliet89.tipoffclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by yongwen on 29/06/15.
 */
public class TimezonePreference  extends ListPreference{
    public TimezonePreference(Context context) {
        super(context);
        initialize();
    }
    public TimezonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setEntries(TimeZone.getAvailableIDs());
        setEntryValues(getEntries());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return StringUtils.defaultIfBlank(a.getString(index),
                Calendar.getInstance().getTimeZone().getID());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, Calendar.getInstance().getTimeZone().getID());
    }


    @Override
    public CharSequence getSummary() {
        return StringUtils.defaultIfBlank(getValue(), "None");
    }
}
