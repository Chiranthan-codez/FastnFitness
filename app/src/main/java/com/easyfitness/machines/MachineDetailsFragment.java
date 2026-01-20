package com.easyfitness.machines;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAORecord;
import com.easyfitness.DAO.Machine;
import com.easyfitness.R;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.RealPathUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MachineDetailsFragment extends Fragment {

    public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

    protected CharSequence[] _muscles = {
        "Biceps", "Triceps", "Pectoraux", "Dorseaux",
        "Abdominaux", "Quadriceps", "Ischio-jambiers",
        "Adducteurs", "Mollets", "Deltoids"
    };
    protected boolean[] _selections = new boolean[_muscles.length];

    Spinner typeList;
    EditText musclesList, machineName, machineDescription;
    ImageView machinePhoto;
    FloatingActionButton machineAction;
    LinearLayout machinePhotoLayout;

    LinearLayout exerciseTypeSelectorLayout;
    TextView bodybuildingSelector, cardioSelector;

    int selectedType = DAOMachine.TYPE_FONTE;
    long machineIdArg, machineProfilIdArg;
    boolean isImageFitToScreen = false;

    DAOMachine mDbMachine;
    DAORecord mDbRecord;
    Machine mMachine;

    ImageUtil imgUtil;
    String mCurrentPhotoPath;
    boolean toBeSaved = false;

    ArrayList<Integer> selectMuscleList = new ArrayList<>();

    View fragmentView;

    public static MachineDetailsFragment newInstance(long machineId, long machineProfile) {
        MachineDetailsFragment f = new MachineDetailsFragment();
        Bundle args = new Bundle();
        args.putLong("machineID", machineId);
        args.putLong("machineProfile", machineProfile);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.machine_details, container, false);
        fragmentView = view;

        mDbMachine = new DAOMachine(view.getContext());
        mDbRecord = new DAORecord(view.getContext());

        machineName = view.findViewById(R.id.machine_name);
        machineDescription = view.findViewById(R.id.machine_description);
        musclesList = view.findViewById(R.id.machine_muscles);
        machinePhoto = view.findViewById(R.id.machine_photo);
        machinePhotoLayout = view.findViewById(R.id.machine_photo_layout);
        machineAction = view.findViewById(R.id.actionCamera);

        imgUtil = new ImageUtil(machinePhoto);

        Bundle args = getArguments();
        machineIdArg = args.getLong("machineID");
        machineProfilIdArg = args.getLong("machineProfile");

        mMachine = mDbMachine.getMachine(machineIdArg);

        machineName.setText(mMachine.getName());
        machineDescription.setText(mMachine.getDescription());
        musclesList.setText(getInputFromDBString(mMachine.getBodyParts()));
        mCurrentPhotoPath = mMachine.getPicture();

        machinePhoto.setOnClickListener(v -> CreatePhotoSourceDialog());
        machinePhoto.setOnLongClickListener(v -> CreatePhotoSourceDialog());

        machineAction.setOnClickListener(v -> CreatePhotoSourceDialog());

        machineName.addTextChangedListener(watcher);
        machineDescription.addTextChangedListener(watcher);
        musclesList.addTextChangedListener(watcher);

        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
                ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
            } else {
                machinePhoto.setImageResource(R.drawable.ic_machine);
            }
        });

        return view;
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            requestForSave();
        }
    };

    private boolean CreatePhotoSourceDialog() {
        return imgUtil.CreatePhotoSourceDialog(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == ImageUtil.REQUEST_TAKE_PHOTO) {
            mCurrentPhotoPath = imgUtil.getFilePath();

        } else if (requestCode == ImageUtil.REQUEST_PICK_GALERY_PHOTO && data != null) {
            Uri uri = data.getData();
            mCurrentPhotoPath = RealPathUtil.getRealPath(requireContext(), uri);
        }

        if (mCurrentPhotoPath != null) {
            ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
            ImageUtil.saveThumb(mCurrentPhotoPath);
            requestForSave();
        }
    }

    private void requestForSave() {
        toBeSaved = true;
    }

    public boolean toBeSaved() {
        return toBeSaved;
    }

    public void machineSaved() {
        toBeSaved = false;
    }

    public Machine getMachine() {
        mMachine.setName(machineName.getText().toString());
        mMachine.setDescription(machineDescription.getText().toString());
        mMachine.setBodyParts(getDBStringFromInput(musclesList.getText().toString()));
        mMachine.setPicture(mCurrentPhotoPath);
        mMachine.setType(selectedType);
        return mMachine;
    }

    private String getDBStringFromInput(String pInput) {
        if (pInput == null || pInput.isEmpty()) return "";
        String[] data = pInput.split(";");
        StringBuilder sb = new StringBuilder();
        for (String s : data) {
            sb.append(s).append(";");
        }
        return sb.toString();
    }

    private String getInputFromDBString(String db) {
        return db == null ? "" : db.replace(";", "; ");
    }
}
