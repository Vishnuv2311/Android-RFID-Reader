package com.example.server1.reader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import  android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithISO15693;
import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    RFIDWithISO15693 mRFID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupWithViewPager(viewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


        try {
            mRFID = RFIDWithISO15693.getInstance();
        } catch (ConfigurationException e) {


            Toast.makeText(MainActivity.this,R.string.rfid_mgs_error_config,
                    Toast.LENGTH_SHORT).show();
            return;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
         new InitTask().execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == 139) {
            Toast.makeText(this, "Working", Toast.LENGTH_SHORT).show();
            Scanner scanner=new Scanner();

        }

        return super.onKeyDown(keyCode, event);

    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            return mRFID.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
//                Toast.makeText(MainActivity.this, "init fail",
//                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }


    public void setupWithViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new Scanner(), "Scanner");
        adapter.addFragment(new Writer(), "Writer");
        adapter.addFragment(new Auto(),"Auto R/W");
        viewPager.setAdapter(adapter);
    }

   static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> tiltleList = new ArrayList<>();


        private Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            tiltleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tiltleList.get(position);
        }
    }

}


