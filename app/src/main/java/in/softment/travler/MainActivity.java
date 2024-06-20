package in.softment.travler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import in.softment.travler.Fragments.NotificationsFragment;
import in.softment.travler.Fragments.ProfileFragment;
import in.softment.travler.Fragments.WorkoutFragment;
import in.softment.travler.Utils.NonSwipeAbleViewPager;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private NonSwipeAbleViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private int[] tabIcons = {
            R.drawable.notification,
            R.drawable.home5,
            R.drawable.menu,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);


        //ViewPager
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);


        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        viewPager.setCurrentItem(1);
    }

    public void gotoPdfActivity(String cat_id,boolean isCatFree) {
        Intent intent = new Intent(this, PdfViewActivity.class);
        intent.putExtra("cat_id",cat_id);
        intent.putExtra("isCatFree",isCatFree);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            tabLayout.getTabAt(2).select();
            viewPager.setCurrentItem(2);
        }
    }

    private void setupTabIcons() {

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);


    }

    public void changeBottomBarPosition(int position) {
        viewPager.setCurrentItem(position);
    }

    private void setupViewPager(ViewPager viewPager) {

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFrag(new NotificationsFragment(MainActivity.this),"Notifications");
        viewPagerAdapter.addFrag(new WorkoutFragment(MainActivity.this),"Home");
        viewPagerAdapter.addFrag(new ProfileFragment(MainActivity.this),"Profile");

        viewPager.setAdapter(viewPagerAdapter);

    }

    static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        @Override
        public int getItemPosition(@NonNull @NotNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {

            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment,String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }


    }


    @Override
    public void onBackPressed() {

        if (tabLayout.getSelectedTabPosition() == 1) {
            super.onBackPressed();
        }
        else {
            viewPager.setCurrentItem(1);
            tabLayout.getTabAt(1).select();
        }
    }
}