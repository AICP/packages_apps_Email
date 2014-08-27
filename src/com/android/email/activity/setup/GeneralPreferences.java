/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.email.activity.setup;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.email.Preferences;
import com.android.email.R;
import com.android.email.provider.EmailProvider;
import com.android.mail.preferences.MailPrefs;
import com.android.mail.ui.settings.ClearPictureApprovalsDialogFragment;
import com.android.mail.utils.Utils;

public class GeneralPreferences extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREFERENCE_KEY_AUTO_ADVANCE = "auto_advance";
    private static final String PREFERENCE_KEY_CONFIRM_DELETE = "confirm_delete";
    private static final String PREFERENCE_KEY_CONFIRM_SEND = "confirm_send";
    private static final String PREFERENCE_KEY_CONV_LIST_ICON = "conversation_list_icon";
    private static final String PREFERENCE_KEY_CONFIRM_FORWARD = "confirm_forward";
    private static final String PREFERENCE_KEY_ADD_ATTACHMENT = "add_attachment";
    private static final String PREFERENCE_KEY_SELECT_RECIPIENTS = "select_recipients";

    private MailPrefs mMailPrefs;
    private Preferences mPreferences;
    private ListPreference mAutoAdvance;

    private CheckBoxPreference mConfirmDelete;
    private CheckBoxPreference mConfirmSend;
    //private CheckBoxPreference mConvListAttachmentPreviews;
    private CheckBoxPreference mSwipeDelete;

    private CheckBoxPreference mConfirmForward;
    private CheckBoxPreference mAddAttachment;
    private CheckBoxPreference mSelectRecipients;

    private boolean mSettingsChanged = false;

    CharSequence[] mSizeSummaries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mMailPrefs = MailPrefs.get(getActivity());
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFERENCES_FILE);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.general_preferences);
    }

    @Override
    public void onResume() {
        loadSettings();
        mSettingsChanged = false;
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSettingsChanged) {
            // Notify the account list that we have changes
            ContentResolver resolver = getActivity().getContentResolver();
            resolver.notifyChange(EmailProvider.UIPROVIDER_ALL_ACCOUNTS_NOTIFIER, null);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        // Indicate we need to send notifications to UI
        mSettingsChanged = true;
        if (PREFERENCE_KEY_AUTO_ADVANCE.equals(key)) {
            mPreferences.setAutoAdvanceDirection(mAutoAdvance.findIndexOfValue((String) newValue));
            return true;
        } else if (MailPrefs.PreferenceKeys.DEFAULT_REPLY_ALL.equals(key)) {
            mMailPrefs.setDefaultReplyAll((Boolean) newValue);
            return true;
        } else if (PREFERENCE_KEY_CONV_LIST_ICON.equals(key)) {
            mMailPrefs.setShowSenderImages((Boolean) newValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (getActivity() == null) {
            // Guard against monkeys.
            return false;
        }
        mSettingsChanged = true;
        String key = preference.getKey();
        if (PREFERENCE_KEY_CONFIRM_DELETE.equals(key)) {
            mPreferences.setConfirmDelete(mConfirmDelete.isChecked());
            return true;
        } else if (PREFERENCE_KEY_CONFIRM_SEND.equals(key)) {
            mPreferences.setConfirmSend(mConfirmSend.isChecked());
            return true;
        } else if (MailPrefs.PreferenceKeys.CONVERSATION_LIST_SWIPE.equals(key)) {
            mMailPrefs.setConversationListSwipeEnabled(mSwipeDelete.isChecked());
            return true;
        } else if (PREFERENCE_KEY_CONFIRM_FORWARD.equals(key)) {
            mPreferences.setConfirmForward(mConfirmForward.isChecked());
            return true;
        } else if (PREFERENCE_KEY_ADD_ATTACHMENT.equals(key)) {
            mPreferences.setAddAttachmentEnabled(mAddAttachment.isChecked());
            return true;
        } else if (PREFERENCE_KEY_SELECT_RECIPIENTS.equals(key)) {
            mPreferences.setSelectRecipientsEnabled(mSelectRecipients.isChecked());
            return true;
        }
        return false;
    }

    private void loadSettings() {
        mPreferences = Preferences.getPreferences(getActivity());
        mAutoAdvance = (ListPreference) findPreference(PREFERENCE_KEY_AUTO_ADVANCE);
        mAutoAdvance.setValueIndex(mPreferences.getAutoAdvanceDirection());
        mAutoAdvance.setOnPreferenceChangeListener(this);

        final CheckBoxPreference convListIcon =
                (CheckBoxPreference) findPreference(PREFERENCE_KEY_CONV_LIST_ICON);
        if (convListIcon != null) {
            final boolean showSenderImage = mMailPrefs.getShowSenderImages();
            convListIcon.setChecked(showSenderImage);
            convListIcon.setOnPreferenceChangeListener(this);
        }

        mConfirmDelete = (CheckBoxPreference) findPreference(PREFERENCE_KEY_CONFIRM_DELETE);
        mConfirmSend = (CheckBoxPreference) findPreference(PREFERENCE_KEY_CONFIRM_SEND);
        mSwipeDelete = (CheckBoxPreference)
                findPreference(MailPrefs.PreferenceKeys.CONVERSATION_LIST_SWIPE);
        mSwipeDelete.setChecked(mMailPrefs.getIsConversationListSwipeEnabled());

        final CheckBoxPreference replyAllPreference =
                (CheckBoxPreference) findPreference(MailPrefs.PreferenceKeys.DEFAULT_REPLY_ALL);
        replyAllPreference.setChecked(mMailPrefs.getDefaultReplyAll());
        replyAllPreference.setOnPreferenceChangeListener(this);

        mConfirmForward = (CheckBoxPreference) findPreference(PREFERENCE_KEY_CONFIRM_FORWARD);
        mConfirmForward.setChecked(mPreferences.getConfirmForward());
        mAddAttachment = (CheckBoxPreference) findPreference(PREFERENCE_KEY_ADD_ATTACHMENT);
        mAddAttachment.setChecked(mPreferences.getAddAttachmentEnabled());
        mSelectRecipients = (CheckBoxPreference) findPreference(PREFERENCE_KEY_SELECT_RECIPIENTS);
        mSelectRecipients.setChecked(mPreferences.getSelectRecipientsEnabled());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.general_prefs_fragment_menu, menu);

        MenuItem feedbackMenuItem = menu.findItem(R.id.feedback_menu_item);
        Uri feedbackUri = Utils.getValidUri(getString(R.string.email_feedback_uri));

        if (feedbackMenuItem != null) {
            feedbackMenuItem.setVisible(!Uri.EMPTY.equals(feedbackUri));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_picture_approvals_menu_item:
                clearDisplayImages();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearDisplayImages() {
        final ClearPictureApprovalsDialogFragment fragment =
                ClearPictureApprovalsDialogFragment.newInstance();
        fragment.show(getActivity().getFragmentManager(),
                ClearPictureApprovalsDialogFragment.FRAGMENT_TAG);
    }

}
