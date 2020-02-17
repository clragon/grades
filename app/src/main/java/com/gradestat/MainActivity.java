package com.gradestat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.preference.PreferenceManager;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static java.lang.Math.round;


public class MainActivity extends AestheticActivity {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private tableSpinner adapter;
    private SharedPreferences preferences;
    private final DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

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
            // ensure table directory exists
            if (!tables_dir.exists()) {
                if (!tables_dir.mkdir()) {
                    Toasty.error(this, R.string.table_no_write, Toast.LENGTH_LONG, true).show();
                    finish();
                }
            } else if (!tables_dir.canWrite()) {
                Toasty.error(this, R.string.table_no_write, Toast.LENGTH_LONG, true).show();
                finish();
            }

            // create default settings on first run
            // not really needed since shared preference calls need a default value.

            Map<String, Object> defaultPreferences = new HashMap<>();
            {
                defaultPreferences.put("boot_table", "grades.json");
                defaultPreferences.put("dark", true);
                defaultPreferences.put("minGrade", 1);
                defaultPreferences.put("maxGrade", 6);
                defaultPreferences.put("useWeight", true);
                defaultPreferences.put("compensate", true);
                defaultPreferences.put("compensateDouble", true);
                defaultPreferences.put("useLimits", true);
                defaultPreferences.put("advanced", false);
                defaultPreferences.put("sorting", getString(R.string.sort_by_custom));
                defaultPreferences.put("sorting_invert", false);
            }

            SharedPreferences.Editor editor = preferences.edit();

            for (Map.Entry<String, Object> entry : defaultPreferences.entrySet()) {
                if (!preferences.contains(entry.getKey())) {
                    if (entry.getValue() instanceof String) {
                        editor.putString(entry.getKey(), (String) entry.getValue());
                    }
                    if (entry.getValue() instanceof Integer) {
                        editor.putInt(entry.getKey(), (Integer) entry.getValue());
                    }
                    if (entry.getValue() instanceof Boolean) {
                        editor.putBoolean(entry.getKey(), (Boolean) entry.getValue());
                    }
                }
            }

            editor.apply();

            // get current table
            table = getTable();
        } else {
            // get current table from saved instance
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
                // check if more than one fragment is inflated
                // if, replace hamburger with back button
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                }
                // update header whenever drawer is opened.
                updateDrawer();
            }
        };

        // update drawer header first time
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
        int background = getAttr(this, android.R.attr.colorBackground);
        navigation.setBackgroundColor(background);
        navigation.setNavigationItemSelectedListener(this::selectDrawerItem);

        // get nav header view to access views in the header
        View navHeader = navigation.getHeaderView(0);

        Spinner spinner = navHeader.findViewById(R.id.table_dropdown);
        // put tables into spinner
        adapter = new tableSpinner(this, getSpinnerList());
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // set selected item to the loaded table
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // check if the current entry isn't the add new item button
                if (parent.getItemAtPosition(position) != null) {
                    // prevent endless loop
                    if (position != 0) {
                        // replace the current table with the selected one
                        table = (Table) parent.getItemAtPosition(position);
                        // update the boot table to the current one
                        preferences.edit().putString("boot_table", new File(table.saveFile).getName()).apply();
                        // refresh subjects by reapplying the fragment
                        navigation.getMenu().performIdentifierAction(navigation.getCheckedItem().getItemId(), 0);
                        // update spinner so that the selected table is always on position 0
                        adapter.clear();
                        adapter.addAll(getSpinnerList());
                        adapter.notifyDataSetChanged();
                        // call set selection again
                        spinner.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(getTableIndex(getSpinnerList(), table), false);


        // call edit table dialog when edit button is clicked
        navHeader.findViewById(R.id.table_edit).setOnClickListener(v -> new TableEditor.Builder(getSupportFragmentManager(), (Table) spinner.getSelectedItem())
                .setPositiveButton(v1 -> {
                    // update adapter before null check
                    adapter.clear();
                    adapter.addAll(getSpinnerList());
                    adapter.notifyDataSetChanged();

                    // check if the add new item button is the only item left
                    if (spinner.getSelectedItem() == null) {
                        if (spinner.getSelectedItemPosition() == 0 || getTableList().size() == 0) {
                            // no tables left, recreate the default one
                            table = getTable();
                            preferences.edit().putString("boot_table", new File(table.saveFile).getName()).apply();

                            // update adapter to reflect changes
                            adapter.clear();
                            adapter.addAll(getSpinnerList());

                            adapter.notifyDataSetChanged();
                        }
                        spinner.setSelection(spinner.getSelectedItemPosition() - 1);
                    } else {
                        // update the app table with the edited one
                        table = (Table) spinner.getSelectedItem();
                        preferences.edit().putString("boot_table", new File(table.saveFile).getName()).apply();
                    }

                    try {
                        // update subject fragment
                        navigation.getMenu().performIdentifierAction(navigation.getCheckedItem().getItemId(), 0);
                    } catch (Exception ex) {
                        // this shouldn't happen.
                    }

                }).show());


        if (savedInstanceState == null) {
            // Activating the default menu item in the drawer
            navigation.getMenu().performIdentifierAction(R.id.grades, 0);

            // initialize theme from settings
            changeTheme(preferences.getBoolean("dark", true));
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // save table on state change
        outState.putSerializable("table", table);
        super.onSaveInstanceState(outState);
    }

    public Table getTable() {
        File table_data = new File(tables_dir, preferences.getString("boot_table", "grades.json"));
        // get the current table and try to load it
        try {
            if (table_data.exists()) {
                try {
                    table = Table.read(table_data.getPath());
                } catch (JsonParseException ex) {
                    Toasty.error(this, String.format(getString(R.string.table_read_failed), table_data.getPath()), Toast.LENGTH_LONG, true).show();
                    if (!getTableList().isEmpty()) {
                        table = Table.read(getTableList().get(0).saveFile);
                    } else {
                        table = createTable(findTable());
                    }
                }
            } else {
                table = createTable(table_data);
            }
        } catch (IOException ex) {
            Toasty.error(this, R.string.table_no_write, Toast.LENGTH_LONG, true).show();
            finish();
        }
        preferences.edit().putString("boot_table", new File(table.saveFile).getName()).apply();
        return table;
    }

    private Table createTable(@NonNull File table_data) throws IOException {
        // if the table doesn't exist, create it
        table = new Table(getResources().getString(R.string.subjects));
        table.saveFile = table_data.getPath();
        table.minGrade = (double) preferences.getInt("minGrade", 1);
        table.maxGrade = (double) preferences.getInt("maxGrade", 6);
        table.useWeight = preferences.getBoolean("useWeight", true);
        table.write();
        return table;
    }

    public File findTable() {
        // find next available file by incrementing until name doesn't exist
        File file;
        File def = new File(tables_dir.getPath(), "grades.json");
        if (def.exists()) {
            for (int i = 2; true; i++) {
                file = new File(tables_dir.getPath(), String.format(getResources().getConfiguration().locale, "%s_%d%s", "grades", i, ".json"));
                if (!file.exists()) {
                    break;
                }
            }
        } else {
            file = def;
        }
        return file;
    }

    public ArrayList<Table> getTableList() {
        // read all files that start with "grades" and end with "json"
        // naming scheme of tables is "grades_n.json" or for the very first one "grades.json"
        File[] table_files = tables_dir.listFiles(pathname -> (pathname.getName().startsWith("grades") && pathname.getName().endsWith(".json")));

        // put all tables into a list.
        ArrayList<Table> tables = new ArrayList<>();
        for (File table_file : table_files) {
            try {
                tables.add(Table.read(table_file.getPath()));
            } catch (IOException | JsonSyntaxException ex) {
                // unable to read the table, exclude it
            }
        }
        return tables;
    }

    public ArrayList<Table> getSpinnerList() {
        ArrayList<Table> t = getTableList();
        if (!t.isEmpty()) {
            // place current table on top
            int pos = getTableIndex(t, table);
            if (pos != -1) {
                t.remove(pos);
                t.add(0, table);
            }
        }
        // add null entry for the add new item button
        t.add(null);
        return t;
    }

    public int getTableIndex(@NonNull ArrayList<Table> target, @NonNull Table t) {
        // find index of a table by reading the list of tables
        // then finding the table we search by its safe-file in it.
        int index = -1;
        for (Table current : target) {
            if (current != null) {
                if (new File(current.saveFile).getName().equals(new File(t.saveFile).getName())) {
                    index = target.indexOf(current);
                }
            }
        }
        return index;
    }

    public void checkToolbar() {
        // play arrow to hamburger transition animation when switching from or to a second layer fragment
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
        ((TextView) navHeader.findViewById(R.id.table_text1)).setText(String.format(getResources().getConfiguration().locale, "%s: %d", getString(R.string.grades), grades));
        ((TextView) navHeader.findViewById(R.id.table_text2)).setText(dateFormat.format(table.getLast()));
        if (preferences.getBoolean("colorRings", true)) {
            ((GradientDrawable) (((ImageView) navHeader.findViewById(R.id.value_circle)).getDrawable()).mutate()).setColor(getGradeColor(this, table, table.getAverage()));
        }
    }

    public boolean selectDrawerItem(@NonNull MenuItem item) {
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
                fragmentClass = Settings.class;
                wrap = true;
                break;
            case R.id.info:
                fragmentClass = About.class;
                wrap = true;
                break;
            default:
                // no fragment is assigned, remove the displayed one
                // or maybe show empty?
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
            // this really shouldn't happen
        }

        if (fragment != null) {
            // basic transaction
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (wrap) {
                // treated like a new activity,
                // add previous to back stack and lock drawer.
                transaction.addToBackStack(null);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                // up slide in and down slide out animations
                transaction.setCustomAnimations(R.anim.slide_in_up, android.R.anim.fade_out, android.R.anim.fade_in, R.anim.slide_out_down);
            } else {
                // throw away all fragments in the back stack
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                    fragmentManager.popBackStack();
                }
                // Highlight the selected item
                navigation.setCheckedItem(item);
                // fade animations
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            transaction.replace(R.id.fragment, fragment);
            transaction.commit();
        }

        drawer.closeDrawers();
        return true;
    }

    private void setHomeAsUp(boolean isHomeAsUp) {
        // animation between hamburger and back arrow
        // isHomeAsUp == true == back arrow
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
                // create view for add new item button
                convertView = getLayoutInflater().inflate(R.layout.part_spinner, parent, false);
                drop_text = convertView.findViewById(R.id.drop_text);
                drop_text.setText(getString(R.string.add_table));
                // ripple effect on button
                // do not call getAttr
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                drop_text.setBackgroundResource(outValue.resourceId);
                // create table on click
                drop_text.setOnClickListener(v -> new TableEditor.Builder(getSupportFragmentManager(), findTable())
                        .setPositiveButton(v1 -> {
                            // refresh adapter after creation
                            adapter.clear();
                            adapter.addAll(getSpinnerList());
                            adapter.notifyDataSetChanged();
                        }).show());
            } else {
                if (((TextView) convertView.findViewById(R.id.drop_text)).getText().toString().equals(getString(R.string.add_table))) {
                    // spinner is trying to reuse add item button. insert new one instead
                    convertView = getLayoutInflater().inflate(R.layout.part_spinner, parent, false);
                    drop_text = convertView.findViewById(R.id.drop_text);
                }
                // update view text
                Table current = getItem(position);
                if (current != null) {
                    drop_text.setText(current.name);
                }
            }

            return convertView;
        }
    }

    public static int getAttr(Activity activity, int id) {
        // shortcut so I don't have to type the horrible garbage below everywhere
        TypedValue value = new TypedValue();
        activity.getTheme().resolveAttribute(id, value, true);
        TypedArray array = activity.obtainStyledAttributes(value.data, new int[]{id});
        int color = array.getColor(0, 1);
        array.recycle();
        return color;
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
        int background = getAttr(this, android.R.attr.colorBackground);
        int textColor = getAttr(this, android.R.attr.textColorPrimary);

        // update the theme with aesthetic
        Aesthetic.get()
                .activityTheme(themeID)
                .isDark(dark)
                .colorStatusBar(background, background)
                .colorNavigationBar(background, background)
                .toolbarIconColor(textColor, textColor)
                .toolbarTitleColor(textColor, textColor)
                .apply();
    }

    private static class GradeColor {

        GradeColor(int color, double anchor) {
            this.color = color;
            this.anchor = anchor;
            this.weight = 1;
        }

        final int color;
        final double anchor;
        final float weight;
    }

    public static Integer getGradeColor(Activity activity, ArrayList<GradeColor> colors, double value) {
        GradeColor lower = null;
        GradeColor greater = null;
        for (GradeColor color : colors) {
            if (color.anchor == value) {
                return color.color;
            } else if (color.anchor < value) {
                if (lower != null) {
                    if (color.anchor > lower.anchor) {
                        lower = color;
                    }
                } else {
                    lower = color;
                }
            } else if (color.anchor > value) {
                if (greater != null) {
                    if (color.anchor < greater.anchor) {
                        greater = color;
                    }
                } else {
                    greater = color;
                }
            }
        }

        if (lower == null || greater == null) {
            return getAttr(activity, android.R.attr.colorPrimary);
        }

        return getBlendColor(lower, greater, (1 / (greater.anchor - lower.anchor) * (value - lower.anchor)));
    }

    public static Integer getGradeColor(Activity activity, Table table, double value) {
        ArrayList<GradeColor> defaultColors = new ArrayList<>();

        defaultColors.add(new GradeColor(Color.rgb(59, 178, 115), table.maxGrade));
        defaultColors.add(new GradeColor(Color.rgb(225, 188, 41), round(table.maxGrade * 0.66)));
        defaultColors.add(new GradeColor(Color.rgb(225, 85, 84), table.minGrade));

        return getGradeColor(activity, defaultColors, value);
    }

    public static Integer getBlendColor(GradeColor from, GradeColor to, double percent) {
        return (Integer) new ArgbEvaluator().evaluate((float) percent, from.color, to.color);
    }
}