package com.easyfitness;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.bodymeasures.BodyPartDetailsFragment;
import com.easyfitness.utils.EditableInputView.EditableInputView;
import com.easyfitness.utils.EditableInputView.EditableInputViewWithDate;
import com.easyfitness.utils.Gender;
import com.onurkaganaldemir.ktoastlib.KToast;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class WeightFragment extends Fragment {

    MainActivity mActivity = null;

    private EditableInputViewWithDate weightEdit;
    private EditableInputView fatEdit;
    private EditableInputView musclesEdit;
    private EditableInputView waterEdit;

    private TextView imcText;
    private TextView imcRank;
    private TextView ffmiText;
    private TextView ffmiRank;
    private TextView rfmText;
    private TextView rfmRank;

    private DAOWeight mWeightDb;
    private DAOBodyMeasure mDbBodyMeasure;
    private DAOProfil mDb;

    private final AdapterView.OnClickListener showDetailsFragment = v -> {
        int bodyPartID = BodyPart.WEIGHT;

        if (v.getId() == R.id.weightDetailsButton) {
            bodyPartID = BodyPart.WEIGHT;
        } else if (v.getId() == R.id.fatDetailsButton) {
            bodyPartID = BodyPart.FAT;
        } else if (v.getId() == R.id.musclesDetailsButton) {
            bodyPartID = BodyPart.MUSCLES;
        } else if (v.getId() == R.id.waterDetailsButton) {
            bodyPartID = BodyPart.WATER;
        }

        BodyPartDetailsFragment fragment =
            BodyPartDetailsFragment.newInstance(bodyPartID, false);

        FragmentTransaction transaction =
            getActivity().getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, fragment,
            MainActivity.BODYTRACKINGDETAILS);
        transaction.addToBackStack(null);
        transaction.commit();
    };

    private final EditableInputView.OnTextChangedListener itemOnTextChange = view -> {
        EditableInputViewWithDate v = (EditableInputViewWithDate) view;

        try {
            if (view.getId() == R.id.weightInput) {
                float value = Float.parseFloat(v.getText());
                mDbBodyMeasure.addBodyMeasure(
                    v.getDate(), BodyPart.WEIGHT, value, getProfil().getId());

            } else if (view.getId() == R.id.fatInput) {
                float value = Float.parseFloat(v.getText());
                mDbBodyMeasure.addBodyMeasure(
                    v.getDate(), BodyPart.FAT, value, getProfil().getId());

            } else if (view.getId() == R.id.musclesInput) {
                float value = Float.parseFloat(v.getText());
                mDbBodyMeasure.addBodyMeasure(
                    v.getDate(), BodyPart.MUSCLES, value, getProfil().getId());

            } else if (view.getId() == R.id.waterInput) {
                float value = Float.parseFloat(v.getText());
                mDbBodyMeasure.addBodyMeasure(
                    v.getDate(), BodyPart.WATER, value, getProfil().getId());
            }

        } catch (NumberFormatException ignored) {
        }

        refreshData();
    };

    private final OnClickListener showHelp = v -> {

        if (v.getId() == R.id.imcHelp) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(R.string.BMI_dialog_title)
                .setContentText(getString(R.string.BMI_formula))
                .setConfirmText(getString(R.string.global_ok))
                .show();

        } else if (v.getId() == R.id.ffmiHelp) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(R.string.FFMI_dialog_title)
                .setContentText(getString(R.string.FFMI_formula))
                .setConfirmText(getString(R.string.global_ok))
                .show();

        } else if (v.getId() == R.id.rfmHelp) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(R.string.RFM_dialog_title)
                .setContentText(
                    getString(R.string.RFM_female_formula) +
                        getString(R.string.RFM_male_formula))
                .setConfirmText(getString(R.string.global_ok))
                .show();
        }
    };

    public static WeightFragment newInstance(String name, int id) {
        WeightFragment f = new WeightFragment();
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

        View view = inflater.inflate(R.layout.tab_weight, container, false);

        weightEdit = view.findViewById(R.id.weightInput);
        fatEdit = view.findViewById(R.id.fatInput);
        musclesEdit = view.findViewById(R.id.musclesInput);
        waterEdit = view.findViewById(R.id.waterInput);

        ImageButton weightDetails = view.findViewById(R.id.weightDetailsButton);
        ImageButton fatDetails = view.findViewById(R.id.fatDetailsButton);
        ImageButton musclesDetails = view.findViewById(R.id.musclesDetailsButton);
        ImageButton waterDetails = view.findViewById(R.id.waterDetailsButton);

        imcText = view.findViewById(R.id.imcValue);
        imcRank = view.findViewById(R.id.imcViewText);
        ffmiText = view.findViewById(R.id.ffmiValue);
        ffmiRank = view.findViewById(R.id.ffmiViewText);
        rfmText = view.findViewById(R.id.rfmValue);
        rfmRank = view.findViewById(R.id.rfmViewText);

        ImageButton imcHelp = view.findViewById(R.id.imcHelp);
        ImageButton ffmiHelp = view.findViewById(R.id.ffmiHelp);
        ImageButton rfmHelp = view.findViewById(R.id.rfmHelp);

        weightEdit.setOnTextChangeListener(itemOnTextChange);
        fatEdit.setOnTextChangeListener(itemOnTextChange);
        musclesEdit.setOnTextChangeListener(itemOnTextChange);
        waterEdit.setOnTextChangeListener(itemOnTextChange);

        imcHelp.setOnClickListener(showHelp);
        ffmiHelp.setOnClickListener(showHelp);
        rfmHelp.setOnClickListener(showHelp);

        weightDetails.setOnClickListener(showDetailsFragment);
        fatDetails.setOnClickListener(showDetailsFragment);
        musclesDetails.setOnClickListener(showDetailsFragment);
        waterDetails.setOnClickListener(showDetailsFragment);

        mWeightDb = new DAOWeight(view.getContext());
        mDbBodyMeasure = new DAOBodyMeasure(view.getContext());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    private float calculateImc(float weight, int size) {
        if (size == 0) return 0;
        return (float) (weight / Math.pow(size / 100.0, 2));
    }

    private String getImcText(float imc) {
        if (imc < 18.5) return getString(R.string.underweight);
        else if (imc < 25) return getString(R.string.normal);
        else if (imc < 30) return getString(R.string.overweight);
        else return getString(R.string.obese);
    }

    private double calculateFfmi(float weight, int size, float fat) {
        if (fat == 0) return 0;
        return weight * (1 - fat / 100) / Math.pow(size / 100.0, 2);
    }

    private String getFfmiTextForMen(double ffmi) {
        if (ffmi < 17) return "below average";
        else if (ffmi < 19) return "average";
        else if (ffmi < 21) return "above average";
        else if (ffmi < 23) return "excellent";
        else if (ffmi < 25) return "superior";
        else if (ffmi < 27) return "suspicious";
        else return "very suspicious";
    }

    private String getFfmiTextForWomen(double ffmi) {
        if (ffmi < 14) return "below average";
        else if (ffmi < 16) return "average";
        else if (ffmi < 18) return "above average";
        else if (ffmi < 20) return "excellent";
        else if (ffmi < 22) return "superior";
        else if (ffmi < 24) return "suspicious";
        else return "very suspicious";
    }

    private void refreshData() {
        if (getView() == null || getProfil() == null) return;

        BodyMeasure weight = mDbBodyMeasure.getLastBodyMeasures(BodyPart.WEIGHT, getProfil());
        BodyMeasure fat = mDbBodyMeasure.getLastBodyMeasures(BodyPart.FAT, getProfil());
        BodyMeasure water = mDbBodyMeasure.getLastBodyMeasures(BodyPart.WATER, getProfil());
        BodyMeasure muscles = mDbBodyMeasure.getLastBodyMeasures(BodyPart.MUSCLES, getProfil());

        if (weight != null) {
            weightEdit.setText(String.valueOf(weight.getBodyMeasure()));
            int size = getProfil().getSize();

            if (size > 0) {
                float imc = calculateImc(weight.getBodyMeasure(), size);
                imcText.setText(String.format("%.1f", imc));
                imcRank.setText(getImcText(imc));

                if (fat != null) {
                    double ffmi = calculateFfmi(weight.getBodyMeasure(), size, fat.getBodyMeasure());
                    ffmiText.setText(String.format("%.1f", ffmi));
                    ffmiRank.setText(
                        getProfil().getGender() == Gender.FEMALE ?
                            getFfmiTextForWomen(ffmi) :
                            getFfmiTextForMen(ffmi));
                } else {
                    ffmiText.setText("-");
                    ffmiRank.setText(R.string.no_fat_available);
                }
            }
        }

        fatEdit.setText(fat != null ? String.valueOf(fat.getBodyMeasure()) : "-");
        waterEdit.setText(water != null ? String.valueOf(water.getBodyMeasure()) : "-");
        musclesEdit.setText(muscles != null ? String.valueOf(muscles.getBodyMeasure()) : "-");
    }

    private void showDeleteDialog(long id) {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getString(R.string.areyousure))
            .setConfirmText(getString(R.string.global_yes))
            .setCancelText(getString(R.string.global_no))
            .setConfirmClickListener(d -> {
                mDbBodyMeasure.deleteMeasure(id);
                refreshData();
                KToast.infoToast(getActivity(), getString(R.string.removedid),
                    Gravity.BOTTOM, KToast.LENGTH_LONG);
                d.dismissWithAnimation();
            })
            .show();
    }

    private Profile getProfil() {
        return ((MainActivity) getActivity()).getCurrentProfile();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }
}
