package com.easyfitness;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageView;
import com.easyfitness.DAO.CVSManager;
import com.easyfitness.DAO.DAOCardio;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DatabaseHelper;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.cardio.DAOOldCardio;
import com.easyfitness.DAO.cardio.OldCardio;
import com.easyfitness.bodymeasures.BodyPartListFragment;
import com.easyfitness.fonte.FontesPagerFragment;
import com.easyfitness.intro.MainIntroActivity;
import com.easyfitness.machines.MachineFragment;
import com.easyfitness.utils.CustomExceptionHandler;
import com.easyfitness.utils.FileChooserDialog;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.MusicController;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String FONTES = "Fontes";
    public static final String MACHINESDETAILS = "MachineDetails";
    public static final String GRAPHIC = "Graphic";
    public static final String HISTORY = "History";
    public static final String BODYTRACKINGDETAILS = "BodyTrackingDetails";
    private static final int TIME_INTERVAL = 2000;
    public static final String FONTESPAGER = "FontePager";
    public static final String CARDIO = "Cardio";
    public static final String WEIGHT = "Weight";
    public static final String PROFILE = "Profile";
    public static final String BODYTRACKING = "BodyTracking";
    public static final String ABOUT = "About";
    public static final String SETTINGS = "Settings";
    public static final String MACHINES = "Machines";

    private final int REQUEST_CODE_INTRO = 111;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CustomDrawerAdapter mDrawerAdapter;
    private List<DrawerItem> dataList;

    private FontesPagerFragment mpFontesPagerFrag;
    private CardioFragment mpCardioFrag;
    private WeightFragment mpWeightFrag;
    private ProfileFragment mpProfileFrag;
    private MachineFragment mpMachineFrag;
    private SettingsFragment mpSettingFrag;
    private AboutFragment mpAboutFrag;
    private BodyPartListFragment mpBodyPartListFrag;

    private DAOProfil mDbProfils;
    private Profile mCurrentProfile;
    private long mCurrentProfilID = -1;

    private Toolbar top_toolbar;
    private CircularImageView roundProfile;
    private MusicController musicController;

    private boolean mIntro014Launched = false;
    private boolean mMigrationBD15done = false;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        top_toolbar = findViewById(R.id.actionToolbar);
        setSupportActionBar(top_toolbar);

        mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);
        mpCardioFrag = CardioFragment.newInstance(CARDIO, 4);
        mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);
        mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);
        mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
        mpAboutFrag = AboutFragment.newInstance(ABOUT, 6);
        mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
        mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);

        loadPreferences();
        DatabaseHelper.renameOldDatabase(this);

        initDrawer();
        musicController = new MusicController(this);
        musicController.initView();

        if (!mIntro014Launched) {
            startActivityForResult(
                new Intent(this, MainIntroActivity.class),
                REQUEST_CODE_INTRO
            );
        } else {
            initActivity();
        }
    }

    private void initDrawer() {
        dataList = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        dataList.add(new DrawerItem("TITLE", R.drawable.ic_profile_black, true));
        dataList.add(new DrawerItem(getString(R.string.menu_Workout), R.drawable.ic_barbell, true));
        dataList.add(new DrawerItem(getString(R.string.MachinesLabel), R.drawable.ic_machine, true));
        dataList.add(new DrawerItem(getString(R.string.weightMenuLabel), R.drawable.ic_scale, true));
        dataList.add(new DrawerItem(getString(R.string.bodytracking), R.drawable.ic_measuring_tape, true));
        dataList.add(new DrawerItem(getString(R.string.SettingLabel), R.drawable.ic_params, true));
        dataList.add(new DrawerItem(getString(R.string.AboutLabel), R.drawable.ic_action_info_outline, true));

        mDrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);
        mDrawerList.setAdapter(mDrawerAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(
            this, mDrawerLayout, top_toolbar,
            R.string.drawer_open, R.string.drawer_close
        );

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void showFragment(String fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (FONTESPAGER.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpFontesPagerFrag);
        } else if (CARDIO.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpCardioFrag);
        } else if (WEIGHT.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpWeightFrag);
        } else if (PROFILE.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpProfileFrag);
        } else if (MACHINES.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpMachineFrag);
        } else if (SETTINGS.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpSettingFrag);
        } else if (ABOUT.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpAboutFrag);
        } else if (BODYTRACKING.equals(fragment)) {
            ft.replace(R.id.fragment_container, mpBodyPartListFrag);
        }

        ft.commit();
    }

    public Profile getCurrentProfile() {
        return mCurrentProfile;
    }

    public long getCurrentProfilID() {
        return mCurrentProfilID;
    }

    public void setCurrentProfil(Profile mProfile) {
        mCurrentProfile = mProfile;
    }

    public String getCurrentMachine() {
        return mpMachineFrag.getCurrentMachine();
    }

    public void setCurrentMachine(String mMachine) {
        mpMachineFrag.setCurrentMachine(mMachine);
    }

    public void showMP3Toolbar(Boolean boolVal) {
        if (boolVal) {
            musicController.show();
        } else {
            musicController.hide();
        }
    }

    public void setCurrentProfile(Profile mProfile) {
        mCurrentProfile = mProfile;
    }

    public View getActivityToolbar() {
        return top_toolbar;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position == 0) {
                showFragment(PROFILE);
            } else if (position == 1) {
                showFragment(FONTESPAGER);
            } else if (position == 2) {
                showFragment(MACHINES);
            } else if (position == 3) {
                showFragment(WEIGHT);
            } else if (position == 4) {
                showFragment(BODYTRACKING);
            } else if (position == 5) {
                showFragment(SETTINGS);
            } else if (position == 6) {
                showFragment(ABOUT);
            }

            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences("prefsfile", MODE_PRIVATE);
        mCurrentProfilID = prefs.getLong("currentProfil", -1);
        mIntro014Launched = prefs.getBoolean("intro014Launched", false);
        mMigrationBD15done = prefs.getBoolean("migrationBD15done", false);
    }

    private void initActivity() {
        mDbProfils = new DAOProfil(this);
        List<Profile> profiles = mDbProfils.getAllProfils();
        if (!profiles.isEmpty()) {
            mCurrentProfile = profiles.get(0);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.pressBackAgain, Toast.LENGTH_SHORT).show();
            mBackPressed = System.currentTimeMillis();
        }
    }
}
