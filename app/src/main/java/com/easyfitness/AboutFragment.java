package com.easyfitness;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DatabaseHelper;
import com.easyfitness.licenses.CustomLicense;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;

public class AboutFragment extends Fragment {

    private String name;
    private int id;
    private MainActivity mActivity = null;

    private View.OnClickListener clickLicense = v -> {

        String name = null;
        String url = null;
        String copyright = null;
        License license = null;

        int viewId = v.getId();

        if (viewId == R.id.MPAndroidChart) {
            name = "MPAndroidChart";
            url = "https://github.com/PhilJay/MPAndroidChart";
            copyright = "Copyright 2019 Philipp Jahoda";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.javaCSV) {
            name = "JavaCSV";
            url = "https://sourceforge.net/projects/javacsv/";
            copyright = "";
            license = new GnuLesserGeneralPublicLicense21();

        } else if (viewId == R.id.antoniomChronometer) {
            name = "Millisecond-Chronometer";
            url = "https://github.com/antoniom/Millisecond-Chronometer";
            copyright = "";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.LicensesDialog) {
            name = "LicensesDialog";
            url = "https://github.com/PSDev/LicensesDialog";
            copyright = "Copyright 2013 Philip Schiffer";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.PagerSlidingTabStrip) {
            name = "PagerSlidingTabStrip";
            url = "https://github.com/astuetz/PagerSlidingTabStrip";
            copyright = "Andreas Stuetz - andreas.stuetz@gmail.com";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.SmartTabLayout) {
            name = "SmartTabLayout";
            url = "https://github.com/ogaclejapan/SmartTabLayout";
            copyright = "Copyright (C) 2015 ogaclejapan";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.flaticonCredits) {
            name = "Flaticon";
            url = "https://www.flaticon.com";
            copyright = "Copyright © 2013-2019 Freepik Company S.L.";
            license = new CustomLicense(
                "Free License (with attribution)",
                "https://profile.flaticon.com/license/free"
            );

        } else if (viewId == R.id.freepikCredits) {
            name = "Freepik";
            url = "https://www.freepik.com";
            copyright = "Copyright © 2010-2019 Freepik Company S.L.";
            license = new CustomLicense(
                "Free License (with attribution)",
                "https://profile.freepik.com/license/free"
            );

        } else if (viewId == R.id.CircleProgress) {
            name = "CircleProgress";
            url = "https://github.com/lzyzsd/CircleProgress";
            copyright = "Copyright (C) 2014 Bruce Lee <bruceinpeking#gmail.com>";
            license = new CustomLicense(
                "WTFPL License",
                "http://www.wtfpl.net/txt/copying/"
            );

        } else if (viewId == R.id.CircularImageView) {
            name = "CircularImageView";
            url = "https://github.com/lopspower/CircularImageView";
            copyright = "Lopez Mikhael";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.ktoast) {
            name = "KToast";
            url = "https://github.com/onurkagan/KToast";
            copyright = "";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.SweetAlertDialog) {
            name = "SweetAlertDialog";
            url = "https://github.com/F0RIS/sweet-alert-dialog";
            copyright = "Pedant (http://pedant.cn)";
            license = new MITLicense();

        } else if (viewId == R.id.AndroidImageCropper) {
            name = "Android-Image-Cropper";
            url = "https://github.com/ArthurHub/Android-Image-Cropper";
            copyright = "Copyright 2016, Arthur Teplitzki, 2013, Edmodo, Inc.";
            license = new ApacheSoftwareLicense20();

        } else if (viewId == R.id.MaterialFavoriteButton) {
            name = "Material Favorite Button";
            url = "https://github.com/IvBaranov/MaterialFavoriteButton";
            copyright = "Copyright 2015 Ivan Baranov";
            license = new ApacheSoftwareLicense20();
        }

        if (name != null && license != null) {
            final Notice notice = new Notice(name, url, copyright, license);
            new LicensesDialog.Builder(getMainActivity())
                .setNotices(notice)
                .build()
                .show();
        }
    };

    public static AboutFragment newInstance(String name, int id) {
        AboutFragment f = new AboutFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_about, container, false);

        TextView mpDBVersionTextView = view.findViewById(R.id.database_version);
        mpDBVersionTextView.setText(Integer.toString(DatabaseHelper.DATABASE_VERSION));

        int[] clickableIds = {
            R.id.MPAndroidChart,
            R.id.javaCSV,
            R.id.LicensesDialog,
            R.id.antoniomChronometer,
            R.id.PagerSlidingTabStrip,
            R.id.SmartTabLayout,
            R.id.flaticonCredits,
            R.id.freepikCredits,
            R.id.CircleProgress,
            R.id.CircularImageView,
            R.id.ktoast,
            R.id.SweetAlertDialog,
            R.id.AndroidImageCropper,
            R.id.MaterialFavoriteButton
        };

        for (int id : clickableIds) {
            view.findViewById(id).setOnClickListener(clickLicense);
        }

        return view;
    }

    public MainActivity getMainActivity() {
        return this.mActivity;
    }
}
