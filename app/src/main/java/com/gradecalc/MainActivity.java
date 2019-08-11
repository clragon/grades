package com.gradecalc;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AestheticActivity {


    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private SharedPreferences preferences;

    public Table table;
    public File tables;
    public File table_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        tables = new File(getFilesDir(), "tables");
        if (!tables.exists()) {
            tables.mkdir();
        }

        checkSettings();

        table_data = new File(tables, preferences.getString("boot_table", "grades.json"));
        table = getTable();

        // Activating the view
        setContentView(R.layout.activity_main);

        // Setting a custom toolbar
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        setSupportActionBar(toolbar);

        fragmentManager.addOnBackStackChangedListener(this::checkToolbar);

        // Setting up the Navigation view inside the drawer
        final NavigationView navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(this::selectDrawerItem);

        checkToolbar();

        if (savedInstanceState == null) {
            // Activating the default menu item in the drawer
            navigation.getMenu().performIdentifierAction(R.id.grades, 0);
            // if (!preferences.getBoolean("dark", true)) {  this.setTheme(R.style.AppThemeLight); }

            if (preferences.getBoolean("dark", true)) {
                Aesthetic.get()
                        .activityTheme(R.style.AppTheme)
                        .isDark(true)
                        .apply();
            } else {
                Aesthetic.get()
                        .activityTheme(R.style.AppThemeLight)
                        .isDark(false)
                        .apply();
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void checkSettings() {
        if (!preferences.contains("boot_table") && tables.list().length == 0) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("boot_table", "grades.json");
            editor.putBoolean("dark", true);
            editor.putInt("minGrade", 1);
            editor.putInt("maxGrade", 6);
            editor.putBoolean("useWeight", true);
            editor.putBoolean("compensate", true);
            editor.putBoolean("useLimits", true);
            editor.apply();
        }
    }

    public Table getTable() {
        table = new Table(getResources().getString(R.string.subjects));
        table.minGrade = (double) preferences.getInt("minGrade", 1);
        table.maxGrade = (double) preferences.getInt("maxGrade", 6);
        table.saveFile = table_data.getPath();
        if (table_data.exists()) {
            try {
                table = Table.read(table_data.getPath());
            } catch (Exception ex) {
                Toast.makeText(this, "can't read table", Toast.LENGTH_LONG).show();
            }
        }
        return table;
    }

    public void checkToolbar() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            setHomeAsUp(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
            toggle.syncState();
            setHomeAsUp(false);
        }
    }

    public boolean selectDrawerItem(MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        boolean wrap = false;
        Fragment fragment = null;
        Class fragmentClass;
        Bundle args = new Bundle();
        final NavigationView navigation = findViewById(R.id.nav_view);
        // assigning menu items to fragments
        switch (item.getItemId()) {
            case R.id.grades:
                fragmentClass = Subjects.class;
                args.putSerializable("table", table);
                break;
            case R.id.overview:
                fragmentClass = Overview.class;
                args.putSerializable("table", table);
                break;
            case R.id.history:
                fragmentClass = History.class;
                break;
            case R.id.settings:
                wrap = true;
                fragmentClass = Settings.class;
                break;
            case R.id.info:
                wrap = true;
                fragmentClass = About.class;
                break;
            default:
                // if no fragment is assigned, remove the displayed one
                fragment = fragmentManager.findFragmentById(R.id.fragment);
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                }
                setTitle(item.getTitle());
                navigation.setCheckedItem(item);
                drawer.closeDrawers();
                return true;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(args);
        } catch (Exception e) {
            // e.printStackTrace();
            // catastrophic failure.
        }

        if (wrap) {

            navigation.getMenu().performIdentifierAction(R.id.grades, 0);

            if (fragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            }
        } else {
            // Insert the fragment by replacing any existing fragment
            if (fragment != null) {
                // getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                    fragmentManager.popBackStack();
                }
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment, fragment).commit();
                // setDrawerToolbar();
            }
            // Highlight the selected item
            navigation.setCheckedItem(item);
        }

        drawer.closeDrawers();
        return true;
    }

    protected boolean isHomeAsUp = false;

    // call this method for animation between hamburger and arrow
    protected void setHomeAsUp(boolean isHomeAsUp) {
        if (this.isHomeAsUp != isHomeAsUp) {
            this.isHomeAsUp = isHomeAsUp;

            ValueAnimator anim = isHomeAsUp ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
            anim.addUpdateListener(valueAnimator -> {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                toggle.onDrawerSlide(drawer, slideOffset);
            });
            anim.setInterpolator(new DecelerateInterpolator());
            // You can change this duration to more closely match that of the default animation.
            anim.setDuration(400);
            anim.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        toggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }
}