/*
 * Copyright (C) 2017 Noe Fernandez
 */
package io.github.nfdz.savedio;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.AppIntro;

import io.github.nfdz.savedio.data.PreferencesUtils;

public class IntroActivity extends AppIntro {

    private static final String USER_API_KEY_WEB = "http://saved.io/key";
    private static final String REGISTER_WEB = "http://saved.io/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDoneText(getString(R.string.intro_done_button));
        addSlide(IntroWarningFragment.newInstance());
        addSlide(IntroInstructionsFragment.newInstance());
        setBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        onDonePressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // back to main activity
        PreferencesUtils.setFinishedIntro(this, true);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // do nothing
    }

    public static class IntroWarningFragment extends Fragment {

        public static IntroWarningFragment newInstance() {
            return new IntroWarningFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_intro_warning, container, false);
            return view;
        }
    }

    public static class IntroInstructionsFragment extends Fragment {

        public static IntroInstructionsFragment newInstance() {
            return new IntroInstructionsFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_intro_instructions, container, false);
            // register button
            view.findViewById(R.id.bt_intro_web_register).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(REGISTER_WEB));
                    PackageManager pm = IntroInstructionsFragment.this.getContext().getPackageManager();
                    if (openIntent.resolveActivity(pm) != null) {
                        startActivity(openIntent);
                    } else {
                        Snackbar.make(view,
                                String.format(getString(R.string.intro_instructions_web_button_unable_format), REGISTER_WEB),
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            });
            // get api key button
            view.findViewById(R.id.bt_intro_web_api).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(USER_API_KEY_WEB));
                    PackageManager pm = IntroInstructionsFragment.this.getContext().getPackageManager();
                    if (openIntent.resolveActivity(pm) != null) {
                        startActivity(openIntent);
                    } else {
                        Snackbar.make(view,
                                String.format(getString(R.string.intro_instructions_web_button_unable_format), USER_API_KEY_WEB),
                                Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            });
            // set api key button
            view.findViewById(R.id.bt_intro_set_api).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent settingsIntent = new Intent(IntroInstructionsFragment.this.getContext(),
                            SettingsActivity.class);
                    startActivity(settingsIntent);
                }
            });
            return view;
        }
    }
}
