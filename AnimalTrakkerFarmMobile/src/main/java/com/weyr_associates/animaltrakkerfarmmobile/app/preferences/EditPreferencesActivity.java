package com.weyr_associates.animaltrakkerfarmmobile.app.preferences;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EditPreferencesActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT_EDIT_PREFERENCES = "TAG_FRAGMENT_EDIT_PREFERENCES";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content,
                            new EditPreferencesFragment(),
                            TAG_FRAGMENT_EDIT_PREFERENCES)
                    .commit();
        }
    }
}
