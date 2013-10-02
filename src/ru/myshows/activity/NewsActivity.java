package ru.myshows.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import ru.myshows.fragments.NewsFragment;
import ru.myshows.fragments.ShowsFragment;
import ru.myshows.tasks.GetShowsTask;

/**
 * @Author: Georgy Gobozov
 * @Date: 12.05.2011
 */
public class NewsActivity extends MenuActivity {

    private ViewPager pager;

    @Override
    protected int getContentViewId() {
        return R.layout.main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setVisibility(View.GONE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.main, new NewsFragment()).commitAllowingStateLoss();
        setupDrawer();
    }


//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(R.id.action_search).setVisible(MyShows.isLoggedIn);
//        menu.findItem(R.id.action_refresh).setVisible(MyShows.isLoggedIn);
//        return super.onPrepareOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//        switch (item.getItemId()) {
//            case 1:
//                int position = pager.getCurrentItem();
//                if (!MyShows.isLoggedIn)
//                    break;
//                Fragment currentFragment = getFragment(position);
//                if (currentFragment != null)
//                    ((Taskable) currentFragment).executeUpdateTask();
//
//                break;
//
//        }
//        return true;
//    }

//    private TextWatcher filterTextWatcher = new TextWatcher() {
//        public void afterTextChanged(Editable s) {
//        }
//
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        }
//
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Fragment fragment = getFragment(pager.getCurrentItem());
//            if (fragment instanceof Searchable) {
//                ((Searchable) fragment).getAdapter().getFilter().filter(s);
//            }
//        }
//
//    };
//

    private Fragment getFragment(int position) {
        return  getSupportFragmentManager().findFragmentByTag(getFragmentTag(position));
    }
//
    private String getFragmentTag(int position) {
        return "android:switcher:" + R.id.pager + ":" + position;
    }




}
