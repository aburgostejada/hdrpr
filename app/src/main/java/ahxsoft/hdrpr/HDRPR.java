package ahxsoft.hdrpr;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

public class HDRPR extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String IMAGE_DIR = "HDR Preview";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 0:
                fragmentManager.beginTransaction().replace(R.id.container, Dashboard.newInstance(position)).commit();
                break;
            case 1:
                fragmentManager.beginTransaction().replace(R.id.container, NewImage.newInstance(position)).commit();
                break;
            case 2:
                fragmentManager.beginTransaction().replace(R.id.container, ImageViewer.newInstance(position)).commit();
                break;
            case 3:
                fragmentManager.beginTransaction().replace(R.id.container, Guide.newInstance(position)).commit();
                break;
            case 4:
                fragmentManager.beginTransaction().replace(R.id.container, About.newInstance(position)).commit();
                break;
            default:
                fragmentManager.beginTransaction().replace(R.id.container, Dashboard.newInstance(position)).commit();
                break;
        }
    }

    public void goToDashboard(){
        onNavigationDrawerItemSelected(0);
    }

    public void goToImages(){
        onNavigationDrawerItemSelected(2);
    }

    public void goToStartNewImage() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, NewImage.newInstance(1, true)).commit();
    }

    public void goToNewImage() {
        onNavigationDrawerItemSelected(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
            case 3:
                mTitle = getString(R.string.title_section5);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            default:
                mTitle = getTitle();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.dashboard, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
