package com.easyfitness;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.Profile;
import com.easyfitness.utils.EditableInputView.EditableInputView;
import com.easyfitness.utils.Gender;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.RealPathUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;

import com.onurkaganaldemir.ktoastlib.KToast;

import java.io.File;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileFragment extends Fragment {

    EditableInputView sizeEdit, birthdayEdit, nameEdit, genderEdit;
    CircularImageView roundProfile;
    FloatingActionButton photoButton;

    String mCurrentPhotoPath;
    private DAOProfil mDb;
    private Profile mProfile;
    private ImageUtil imgUtil;
    MainActivity mActivity;

    /* -------------------- CROP IMAGE LAUNCHER (CORRECTED) -------------------- */

    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
        registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                // The library automatically handles file saving and provides a Uri.
                Uri uri = result.getUriContent();
                if (uri != null) {
                    String realPath = RealPathUtil.getRealPath(getContext(), uri);
                    if (realPath == null) {
                        // Fallback for some devices, get path directly from Uri
                        realPath = uri.getPath();
                    }

                    ImageUtil.setPic(roundProfile, realPath);
                    ImageUtil.saveThumb(realPath); // This might be redundant if the cropper already saved it
                    mCurrentPhotoPath = realPath;
                    mProfile.setPhoto(mCurrentPhotoPath); // Make sure to save the path to the profile object
                    requestForSave(roundProfile);
                }
            } else {
                // An error occurred.
                final Exception e = result.getError();
                if (e != null) {
                    Log.e("ProfileFragment", "Image Cropping error", e);
                    Toast.makeText(getContext(), "Cropping failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    /* ------------------------------------------------------------ */

    public static ProfileFragment newInstance(String name, int id) {
        ProfileFragment f = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile, container, false);

        sizeEdit = view.findViewById(R.id.size);
        birthdayEdit = view.findViewById(R.id.birthday);
        nameEdit = view.findViewById(R.id.name);
        genderEdit = view.findViewById(R.id.gender);
        roundProfile = view.findViewById(R.id.photo);
        photoButton = view.findViewById(R.id.actionCamera);

        mDb = new DAOProfil(view.getContext());
        mProfile = getProfile();
        imgUtil = new ImageUtil();

        // Load existing profile data
        if (mProfile != null) {
            nameEdit.setText(mProfile.getName());
            // ... load other profile data into your EditableInputViews ...
            if (mProfile.getPhoto() != null && !mProfile.getPhoto().isEmpty()) {
                ImageUtil.setPic(roundProfile, mProfile.getPhoto());
            }
        }

        genderEdit.setCustomDialogBuilder(v ->
            new SweetAlertDialog(v.getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.edit_value))
                .setNeutralText(getString(R.string.maleGender))
                .setCancelText(getString(R.string.femaleGender))
                .setConfirmText(getString(R.string.otherGender))
                .setNeutralClickListener(d -> setGender(Gender.MALE, d))
                .setCancelClickListener(d -> setGender(Gender.FEMALE, d))
                .setConfirmClickListener(d -> setGender(Gender.OTHER, d))
        );

        photoButton.setOnClickListener(v -> {
            CropImageOptions cropOptions = new CropImageOptions();
            cropOptions.guidelines = CropImageView.Guidelines.ON;
            cropOptions.imageSourceIncludeCamera = true;
            cropOptions.imageSourceIncludeGallery = true;
            CropImageContractOptions launchOptions = new CropImageContractOptions(null, cropOptions);
            cropImage.launch(launchOptions);
        });

        return view;
    }

    private void setGender(int gender, SweetAlertDialog d) {
        mProfile.setGender(gender);
        requestForSave(genderEdit);
        d.dismissWithAnimation();
    }

    private void requestForSave(View view) {
        // Update profile object from editable fields before saving
        mProfile.setName(nameEdit.getText());
        // ... get other values from your EditableInputViews ...

        mDb.updateProfile(mProfile);
        com.onurkaganaldemir.ktoastlib.KToast KToast = null;
        com.onurkaganaldemir.ktoastlib.KToast.infoToast(
            getActivity(),
            mProfile.getName() + " updated",
            Gravity.BOTTOM,
            KToast.LENGTH_SHORT
        );

        // Update the profile in the MainActivity as well
        if (mActivity != null) {
            mActivity.setCurrentProfile(mProfile);
        }
    }

    private Profile getProfile() {
        if (mActivity == null) {
            mActivity = (MainActivity) getActivity();
        }
        return mActivity.getCurrentProfile();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }
}
