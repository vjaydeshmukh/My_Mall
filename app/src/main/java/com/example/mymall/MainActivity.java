package com.example.mymall;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.FrameLayout;


// top right side ke 3 items tune menu->main.xml me set kiya h (bell,notification,cart ke icons ).........

// nav_header_main.xml me tune place holder ka icon set kiya h, jab top left ke 3 dots wale icon ko click karega
// tab jo navigation bar khulta h uski baat ho rhi h yaha.........


// menu->activity_main_drawer.xml me jo left side ke navbar ke neeche dikhte h wo tune waha pr daale h,
// jaise amazon ke app jab left navbar kholte h to jo options dikhte h neeche wo waha pr h...........









public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private NavigationView navigationView;

    private FrameLayout frameLayout;
    private static final int HOME_FRAGMENT = 0;
    private static final int CART_FRAGMENT = 1;

    private static int currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

         navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
//
        navigationView.getMenu().getItem(0).setChecked(true);

        frameLayout = findViewById(R.id.main_framelayout);
        setFragment(new HomeFragment(),HOME_FRAGMENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ye if condition ye check krta h ki agar humara home page pr h user..
        // to sirf uss time usko upar ke icons dikhne chahiye,
        // nhi to baki sare pages me usko nhi dikhna chahiye
        if(currentFragment == HOME_FRAGMENT){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // ye h top right side ke 3 items ke liye, unme se koi ek bhi selecct hua to uska code yaha h......

        int id = item.getItemId();
        if(id == R.id.main_search_icon){

            return true;
        }
        else if(id == R.id.main_notification_icon){
            return true;
        }
        else if(id == R.id.main_cart_icon){
            mycart();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void mycart(){
        // isse kya hota h ki humara sare menu bar ke options hat jayenge...coz user ko abhi next page ke usme ye nhi dikhna chahiye
        // isse exactly onCreateOptionMenu wala function jo upar h wo fir se run hota and abhi humara waha if wala statement run hoga.....
        invalidateOptionsMenu();

        setFragment(new MyCartFragment(),CART_FRAGMENT);
        // delh abhi yaha pr humne neeche 3 kyu pass kiya na
        // coz jab app ke left me navbar open karega tab tujhe jo options dekhenge usme se 3 rd wala option my cart ka h
        // and user ko ye bhata rhe h apan ki agar usne 3rd wala option select kiya to ye sab hoga.....
        navigationView.getMenu().getItem(3).setChecked(true);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item){

        int id = item.getItemId();

        if(id == R.id.nav_my_mall){
            setFragment(new HomeFragment(),HOME_FRAGMENT);
        }
        else if(id == R.id.nav_my_orders){

        }
        else if(id == R.id.nav_my_rewards){

        }
        else if(id == R.id.nav_my_cart){

            mycart();
        }
        else if(id == R.id.nav_my_wishlist){

        }
        else if(id == R.id.nav_my_account){

        }
        else if(id == R.id.nav_sign_out){

        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setFragment(Fragment fragment, int fragmentNo){
        currentFragment = fragmentNo;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }


}





















