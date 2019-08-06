package com.gradecalc;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    public Table table = new Table();
    public File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean dark = true;

        if (!dark) {
            this.setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);
        }


        File tables = new File(getFilesDir(), "tables");
        tables.mkdir();
        file = new File(tables, "grades.json");

        table.saveFile = file.getPath();

        if (file.exists()) {
            try {
                table = Table.read(file.getPath());
            } catch (Exception ex) {
                Toast.makeText(this, "can't read table",
                        Toast.LENGTH_LONG).show();
            }
        }


        // Activating the view
        setContentView(R.layout.activity_main);

        // Setting a custom toolbar to have a drawer button
        toolbar = findViewById(R.id.toolbar);
        setDrawerToolbar();

        // Setting the drawer variable so this activity can manage it
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);

        // Setting up the Navigation view inside the drawer
        final NavigationView navigation = findViewById(R.id.nav_view);
        setupDrawerContent(navigation);

        // Activating the default menu item in the drawer
        navigation.getMenu().performIdentifierAction(R.id.grades, 0);


        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        // window.setStatusBarColor(ContextCompat.getColor(this, ));


    }

    public void setDrawerToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        fragmentManager.addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                toggle.setDrawerIndicatorEnabled(false);
                actionbar.setDisplayHomeAsUpEnabled(true);
                actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
            } else {
                //show hamburger
                toggle.setDrawerIndicatorEnabled(true);
                actionbar.setDisplayHomeAsUpEnabled(false);
                actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
                toggle.syncState();
                toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
            }
        });
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        Bundle args = new Bundle();
        switch (menuItem.getItemId()) {
            // assigning menu items to fragments
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
                fragmentClass = Settings.class;
                break;
            case R.id.info:
                fragmentClass = Info.class;
                break;
            // if no fragment is assigned, remove the displayed one
            default:
                fragment = fragmentManager.findFragmentById(R.id.framelayout);
                if (fragment != null) {
                    fragmentManager.beginTransaction().remove(fragment).commit();
                }
                setTitle(menuItem.getTitle());
                menuItem.setChecked(true);
                drawer.closeDrawers();
                return;
        }

        // For whatever reason, there is a try statement here. I'll leave it alone.
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(args);
        } catch (Exception e) {
            // e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        if (fragment != null) {
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.framelayout, fragment).commit();
            setDrawerToolbar();
        }

        // Highlight the selected item
        menuItem.setChecked(true);
        drawer.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}