package com.easyfitness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.Cardio;
import com.easyfitness.DAO.DAOCardio;
import com.easyfitness.DAO.IRecord;
import com.easyfitness.DAO.Profile;
import com.easyfitness.fonte.RecordCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

@SuppressLint("ValidFragment")
public class CardioFragment extends Fragment {

    DatePickerDialogFragment mDateFrag = null;
    TimePickerDialogFragment mDurationFrag = null;
    MainActivity mActivity = null;

    EditText dateEdit;
    AutoCompleteTextView exerciceEdit;
    EditText distanceEdit;
    EditText durationEdit;
    Button addButton;
    ExpandedListView recordList;
    ImageButton exerciceListButton;

    private DAOCardio mDb = null;

    public static CardioFragment newInstance(String name, int id) {
        CardioFragment f = new CardioFragment();
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

        View view = inflater.inflate(R.layout.tab_cardio, container, false);

        dateEdit = view.findViewById(R.id.editCardioDate);
        exerciceEdit = view.findViewById(R.id.editExercice);
        distanceEdit = view.findViewById(R.id.editDistance);
        durationEdit = view.findViewById(R.id.editDuration);
        recordList = view.findViewById(R.id.listCardioRecord);
        exerciceListButton = view.findViewById(R.id.buttonListExercice);
        addButton = view.findViewById(R.id.addExercice);

        mDb = new DAOCardio(view.getContext());

        addButton.setOnClickListener(clickAddButton);
        exerciceListButton.setOnClickListener(onClickMachineList);

        dateEdit.setOnClickListener(clickDateEdit);
        durationEdit.setOnClickListener(clickDateEdit);

        dateEdit.setOnFocusChangeListener(touchRazEdit);
        durationEdit.setOnFocusChangeListener(touchRazEdit);
        distanceEdit.setOnFocusChangeListener(touchRazEdit);
        exerciceEdit.setOnFocusChangeListener(touchRazEdit);

        exerciceEdit.setOnItemClickListener(onItemClickFilterList);
        recordList.setOnItemLongClickListener(itemLongClickDeleteRecord);

        return view;
    }

    private final OnClickListener clickDateEdit = v -> {
        if (v.getId() == R.id.editCardioDate) {
            showDatePicker();
        } else if (v.getId() == R.id.editDuration) {
            showTimePicker();
        }
    };

    private final OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            if (v.getId() == R.id.editCardioDate) {
                showDatePicker();
            } else if (v.getId() == R.id.editDuration) {
                showTimePicker();
            } else if (v.getId() == R.id.editDistance) {
                distanceEdit.setText("");
            } else if (v.getId() == R.id.editExercice) {
                exerciceEdit.setText("");
            }
        } else {
            if (v.getId() == R.id.editExercice) {
                FillRecordTable(exerciceEdit.getText().toString());
            }
        }
    };

    private final OnClickListener clickAddButton = v -> {

        if (dateEdit.getText().toString().isEmpty()
            || exerciceEdit.getText().toString().isEmpty()
            || (distanceEdit.getText().toString().isEmpty()
            && durationEdit.getText().toString().isEmpty())) {
            return;
        }

        Date date;
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = df.parse(dateEdit.getText().toString());
        } catch (ParseException e) {
            date = new Date();
        }

        long duration = 0;
        try {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            Date d = df.parse(durationEdit.getText().toString());
            duration = d.getTime();
        } catch (Exception ignored) {
        }

        float distance = 0;
        if (!distanceEdit.getText().toString().isEmpty()) {
            distance = Float.parseFloat(distanceEdit.getText().toString());
        }

        mDb.addCardioRecord(
            date,
            "00:00",
            exerciceEdit.getText().toString(),
            distance,
            duration,
            getProfile()
        );

        FillRecordTable(exerciceEdit.getText().toString());
    };

    private final OnClickListener onClickMachineList = v -> {
        String[] machines = mDb.getAllMachines(getProfile());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Machine");
        builder.setItems(machines, (dialog, which) -> {
            exerciceEdit.setText(machines[which]);
            FillRecordTable(machines[which]);
        });
        builder.show();
    };

    private final OnItemLongClickListener itemLongClickDeleteRecord = (parent, view, position, id) -> {
        showDeleteDialog(id);
        return true;
    };

    private final OnItemClickListener onItemClickFilterList =
        (parent, view, position, id) -> FillRecordTable(exerciceEdit.getText().toString());

    private void FillRecordTable(String machine) {
        List<Cardio> records;

        if (machine == null || machine.isEmpty()) {
            records = mDb.getAllCardioRecordsByProfile(getProfile());
        } else {
            records = mDb.getAllCardioRecordByMachines(getProfile(), machine);
        }

        if (records.isEmpty()) {
            recordList.setAdapter(null);
        } else {
            RecordCursorAdapter adapter =
                new RecordCursorAdapter(getContext(), mDb.getCursor(), 0, this::showDeleteDialog, null);
            recordList.setAdapter(adapter);
        }
    }

    private void showDeleteDialog(long idToDelete) {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getString(R.string.areyousure))
            .setConfirmText(getString(R.string.global_yes))
            .setCancelText(getString(R.string.global_no))
            .setConfirmClickListener(dialog -> {
                mDb.deleteRecord(idToDelete);
                FillRecordTable(exerciceEdit.getText().toString());
                KToast.infoToast(getActivity(), getString(R.string.removedid),
                    Gravity.BOTTOM, KToast.LENGTH_SHORT);
                dialog.dismissWithAnimation();
            })
            .show();
    }

    private void showDatePicker() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(
                (view, year, month, day) ->
                    dateEdit.setText(DateConverter.dateToString(year, month + 1, day))
            );
        }
        mDateFrag.show(getActivity().getFragmentManager(), "datePicker");
    }

    private void showTimePicker() {
        if (mDurationFrag == null) {
            mDurationFrag = TimePickerDialogFragment.newInstance(
                (view, hour, minute) ->
                    durationEdit.setText(String.format("%02d:%02d", hour, minute))
            );
        }
        mDurationFrag.show(getActivity().getFragmentManager(), "timePicker");
    }

    private Profile getProfile() {
        return mActivity.getCurrentProfile();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
            (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
