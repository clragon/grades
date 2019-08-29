package com.gradestat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.evernote.android.state.StateSaver;
import com.google.android.material.navigation.NavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends AestheticActivity {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    private tableSpinner adapter;
    private SharedPreferences preferences;
    private DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    public Table table;
    public File tables_dir;

    private boolean isHomeAsUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        tables_dir = new File(getFilesDir(), "tables");

        if (savedInstanceState == null) {

            // initialize date library
            AndroidThreeTen.init(this);

            // check if table directory exists
            if (!tables_dir.exists()) {
                tables_dir.mkdir();
            }

            // check if settings exist
            if (!preferences.contains("boot_table")) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("boot_table", "grades.json");
                editor.putBoolean("dark", true);
                editor.putInt("minGrade", 1);
                editor.putInt("maxGrade", 6);
                editor.putBoolean("useWeight", true);
                editor.putBoolean("compensate", true);
                editor.putBoolean("useLimits", true);
                editor.putBoolean("advanced", false);
                editor.apply();
                recreate();
            }

            // initialize theme from settings
            changeTheme(preferences.getBoolean("dark", true));

            // get current table
            table = getTable();
        } else {
            // get current table
            table = (Table) savedInstanceState.getSerializable("table");
        }

        // Activating the view
        setContentView(R.layout.activity_main);

        // Setup drawer and toolbar
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer) {
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                }
                updateDrawer();
            }
        };
        // update drawer first time to adjust for recreation
        updateDrawer();
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // set toolbar as activity toolbar
        setSupportActionBar(toolbar);
        fragmentManager.addOnBackStackChangedListener(this::checkToolbar);
        // update toolbar first time
        checkToolbar();

        // Setting up the Navigation view inside the drawer
        final NavigationView navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(this::selectDrawerItem);

        // get nav header view to access views in the header
        View navHeader = navigation.getHeaderView(0);

        Spinner spinner = navHeader.findViewById(R.id.table_dropdown);
        // get new custom adapter
        adapter = new tableSpinner(this, getSpinnerList());
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // check if the current entry isn't the add new item button
                if (parent.getItemAtPosition(position) != null) {
                    // replace the current table with the selected one
                    table = (Table) parent.getItemAtPosition(position);
                    preferences.edit().putString("boot_table", new File(table.saveFile).getName()).apply();
                    navigation.getMenu().performIdentifierAction(navigation.getCheckedItem().getItemId(), 0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // set selected item to the loaded table
        spinner.setSelection(getTableIndex(table), false);

        // call edit table dialog when edit button is clicked
        navHeader.findViewById(R.id.table_edit).setOnClickListener(v -> new TableEditor.Builder(getSupportFragmentManager(), (Table) spinner.getSelectedItem())
                .setPositiveButton(v1 -> {
                    adapter.clear();
                    ArrayList<Table> new_tables = getSpinnerList();
                    adapter.addAll(new_tables);

                    if (spinner.getSelectedItem() == null) {
                        if (spinner.getSelectedItemPosition() == 0 || getTableList().size() == 0) {
                            getTable();
                            adapter.clear();
                            adapter.addAll(getSpinnerList());
                            adapter.notifyDataSetChanged();
                        }
                        spinner.setSelection(spinner.getSelectedItemPosition() - 1);
                    }
                    try {
                        navigation.getMenu().performIdentifierAction(navigation.getCheckedItem().getItemId(), 0);
                    } catch (Exception ex) {

                    }

                }).show());


        if (savedInstanceState == null) {
            // Activating the default menu item in the drawer
            navigation.getMenu().performIdentifierAction(R.id.grades, 0);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save table on state change
        outState.putSerializable("table", table);
        super.onSaveInstanceState(outState);
    }

    public Table getTable() {
        File table_data = new File(tables_dir, preferences.getString("boot_table", "grades.json"));
        // get the current table and try to load it
        if (table_data.exists()) {
            try {
                table = Table.read(table_data.getPath());
            } catch (Exception ex) {
                Toast.makeText(this, "can't read table", Toast.LENGTH_LONG).show();
            }
        } else {
            // if the table doesn't exist, make it
            table = new Table(getResources().getString(R.string.subjects));
            table.saveFile = table_data.getPath();
            table.minGrade = (double) preferences.getInt("minGrade", 1);
            table.maxGrade = (double) preferences.getInt("maxGrade", 6);
            table.useWeight = preferences.getBoolean("useWeight", true);
            try {
                table.write();
            } catch (IOException ex) {
                Toast.makeText(this, "can't write table", Toast.LENGTH_LONG).show();
            }
        }
        return table;
    }

    public ArrayList<Table> getTableList() {
        File[] table_files = tables_dir.listFiles(pathname -> (pathname.getName().startsWith("grades") && pathname.getName().endsWith(".json")));

        ArrayList<Table> tables = new ArrayList<>();
        for (File table_file : table_files) {
            try {
                tables.add(Table.read(table_file.getPath()));
            } catch (IOException ex) {
                // oh no
            }
        }

        return tables;
    }

    public ArrayList<Table> getSpinnerList() {
        ArrayList<Table> t = getTableList();
        t.add(null);
        return t;
    }

    public int getTableIndex(Table t) {
        int index = 0;
        ArrayList<Table> temp = getSpinnerList();
        for (Table current : temp) {
            if (current != null) {
                if (new File(current.saveFile).getName().equals(new File(t.saveFile).getName())) {
                    index = temp.indexOf(current);
                }
            }
        }
        return index;
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

    public void updateDrawer() {
        FrameLayout navHeader = (FrameLayout) ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);

        ((TextView) navHeader.findViewById(R.id.value_value)).setText(doubleFormat.format(table.getAverage()));
        // ((TextView) navHeader.findViewById(R.id.table_text1)).setText(String.format("%s: %d", getString(R.string.subjects), table.getSubjects().size()));
        int grades = 0;
        for (Table.Subject s : table.getSubjects()) {
            grades += s.getGrades().size();
        }
        ((TextView) navHeader.findViewById(R.id.table_text1)).setText(String.format("%s: %d", getString(R.string.grades), grades));
        ((TextView) navHeader.findViewById(R.id.table_text2)).setText(dateFormat.format(table.getLatest()));
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
                args.putSerializable("table", table);
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

            navigation.getMenu().performIdentifierAction(navigation.getCheckedItem().getItemId(), 0);
            // navigation.getMenu().performIdentifierAction(R.id.grades, 0);

            if (fragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
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
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fragment, fragment).commit();
            }
            // Highlight the selected item
            navigation.setCheckedItem(item);
        }

        drawer.closeDrawers();
        return true;
    }

    // call this method for animation between hamburger and arrow
    private void setHomeAsUp(boolean isHomeAsUp) {
        if (this.isHomeAsUp != isHomeAsUp) {
            this.isHomeAsUp = isHomeAsUp;

            ValueAnimator anim = isHomeAsUp ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
            anim.addUpdateListener(valueAnimator -> {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                toggle.onDrawerSlide(drawer, slideOffset);
            });
            anim.setInterpolator(new DecelerateInterpolator());
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    public class tableSpinner extends ArrayAdapter<Table> {

        private tableSpinner(Context context, ArrayList<Table> tables) {
            super(context, 0, tables);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        private View initView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.part_spinner, parent, false);
            }
            TextView drop_text = convertView.findViewById(R.id.drop_text);

            if (getItem(position) == null) {

                convertView = getLayoutInflater().inflate(R.layout.part_spinner, parent, false);
                drop_text = convertView.findViewById(R.id.drop_text);
                drop_text.setText(getString(R.string.add_table));
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                drop_text.setBackgroundResource(outValue.resourceId);
                drop_text.setOnClickListener(v -> {
                    File file;
                    File def = new File(tables_dir.getPath(), "grades.json");
                    if (def.exists()) {
                        for (int i = 2; true; i++) {
                            file = new File(tables_dir.getPath(), String.format("%s_%d%s", "grades", i, ".json"));
                            if (!file.exists()) {
                                break;
                            }
                        }
                    } else {
                        file = def;
                    }

                    new TableEditor.Builder(getSupportFragmentManager(), file)
                            .setPositiveButton(v1 -> {
                                adapter.clear();
                                ArrayList<Table> tables = getSpinnerList();
                                adapter.addAll(tables);
                                adapter.notifyDataSetChanged();
                            }).show();
                });
            } else {
                if (((TextView) convertView.findViewById(R.id.drop_text)).getText().toString().equals(getString(R.string.add_table))) {
                    convertView = getLayoutInflater().inflate(R.layout.part_spinner, parent, false);
                    drop_text = convertView.findViewById(R.id.drop_text);
                }
                Table current = getItem(position);
                if (current != null) {
                    drop_text.setText(current.name);
                }
            }

            return convertView;
        }
    }

    public int getAttr(int id) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(id, value, true);
        return obtainStyledAttributes(value.data, new int[]{id}).getColor(0, 1);
    }

    public void changeTheme(boolean dark) {

        int themeID;

        // decide which theme is active
        if (dark) {
            themeID = R.style.AppTheme;
        } else {
            themeID = R.style.AppTheme_Light;
        }

        // set theme to update colors
        setTheme(themeID);

        // get new background and text colors
        int background = getAttr(android.R.attr.colorBackground);
        int alertTextColor = getAttr(android.R.attr.textColorPrimary);

        // update the theme with aesthetic
        Aesthetic.get()
                .activityTheme(themeID)
                .isDark(dark)
                .colorStatusBar(background, background)
                .colorNavigationBar(background, background)
                .toolbarIconColor(alertTextColor, alertTextColor)
                .toolbarTitleColor(alertTextColor, alertTextColor)
                .apply();
    }
}