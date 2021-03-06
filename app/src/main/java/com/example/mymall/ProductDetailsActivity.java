package com.example.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.app.admin.DelegatedAdminReceiver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mymall.MainActivity.showCart;
import static com.example.mymall.RegisterActivity.setSignUpFragment;

public class ProductDetailsActivity extends AppCompatActivity {

    private ViewPager productImagesViewPager;
    private TabLayout viewpagerIndicator;
    public static FloatingActionButton addToWishListBtn;
    private Button coupenRedeemBtn;
    private FirebaseUser currentUser;
    public static boolean ALREADY_ADDED_TO_WISHLIST = false;
    public static boolean ALREADY_ADDED_TO_CART = false;
    public static MenuItem cartItem;
    List<String> productImages;

    /////////////variable for setting ADDD_TO_WISHLIST_BTN enable or disable
    public static boolean running_wishlist_query = false;
    public static boolean running_rated_query = false;
    public static boolean running_cart_query = false;
    /////////////variable for setting ADDD_TO_WISHLIST_BTN enable or disable

    private FirebaseFirestore firebaseFirestore;

    private Button buyNowBtn;
    private LinearLayout addToCartButton;
    private TextView badgeCount;


    //////////////////////coupenDialog
    public static TextView coupenTitle, tvCodIndicator, rewardTitle, rewardBody;
    public static TextView coupenBody, productPrice, cuttedPrice;
    private ImageView codIndicator;
    public static TextView coupenExpiryDate, productTitle, averageRatingMiniview, totalRatingMiniView;
    private static RecyclerView coupensRecyclerView;
    private static LinearLayout selectedCoupen;
    //////////////////////coupenDialog

    public static String productID;

    ///////////////////////////product description
    private ConstraintLayout productDetailsOnlyContainer;
    private TextView productOnlyDescriptionBody;
    private ConstraintLayout productDetailsTabsContainer;
    private ViewPager productDetailsViewpager;
    private TabLayout productDetailsTabLayout;
    private String productDescription, productOtherDetails;
    private int tabPosition = -1;

    private List<ProductSpecificationModel> productSpecificationModelList = new ArrayList<>();

    ///////////////////////////product description


    /////////////////rating layout
    public static int initialRating;
    public static LinearLayout rateNowContainer;
    private TextView totalRatings, totalRatingsFigure, averageRating;
    private LinearLayout ratingsNoContainer;
    private LinearLayout ratingsPrgressBarContainer;
    /////////////////rating layout

    private Dialog signInDialog;
    private Dialog loadingDialog;
    private LinearLayout coupenRedemptionLayout;

    private DocumentSnapshot documentSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialRating = -1; // initial rating isliye 0 set nhi kiya kyuki fir zero matlab 1 star ho jayega

        productImagesViewPager = findViewById(R.id.product_images_viewpager);
        viewpagerIndicator = findViewById(R.id.viewpager_indicator);
        addToWishListBtn = findViewById(R.id.add_to_wishlist_btn);
        buyNowBtn = findViewById(R.id.buy_now_btn);
        coupenRedeemBtn = findViewById(R.id.coupen_redemption_btn);
        productTitle = findViewById(R.id.product_title);
        averageRatingMiniview = findViewById(R.id.tv_product_rating_miniview);
        totalRatingMiniView = findViewById(R.id.total_ratings_miniview);
        productPrice = findViewById(R.id.product_price);
        cuttedPrice = findViewById(R.id.cutted_price);
        codIndicator = findViewById(R.id.cod_indicator_imageview);
        tvCodIndicator = findViewById(R.id.tv_cod_indicator);
        rewardTitle = findViewById(R.id.reward_title);
        rewardBody = findViewById(R.id.reward_body);
        coupenRedemptionLayout = findViewById(R.id.coupon_redemption_layout);

        productDetailsTabsContainer = findViewById(R.id.product_details_tabs_container);
        productDetailsOnlyContainer = findViewById(R.id.product_details_container);
        productOnlyDescriptionBody = findViewById(R.id.product_details_body);

        firebaseFirestore = FirebaseFirestore.getInstance();

        productDetailsViewpager = findViewById(R.id.product_details_viewpager);
        productDetailsTabLayout = findViewById(R.id.product_details_tablayout);
        totalRatings = findViewById(R.id.total_ratings);
        ratingsNoContainer = findViewById(R.id.ratings_numbers_container);
        totalRatingsFigure = findViewById(R.id.total_ratings_figure);
        ratingsPrgressBarContainer = findViewById(R.id.ratings_progressbar_container);
        averageRating = findViewById(R.id.average_rating);
        addToCartButton = findViewById(R.id.add_to_cart_btn);

        ///////////////loading dialog
        loadingDialog = new Dialog(ProductDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        ///////////////loading dialog

        productImages = new ArrayList<>();
        productID = getIntent().getStringExtra("PRODUCT_ID");

        firebaseFirestore.collection("PRODUCTS").document(productID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    documentSnapshot = task.getResult();

                    for (long x = 1; x < (long) documentSnapshot.get("no_of_product_images") + 1; x++) {

                        productImages.add(documentSnapshot.get("product_image_" + x).toString());
                    }
                    ProductImagesAdapter productImagesAdapter = new ProductImagesAdapter(productImages);
                    productImagesViewPager.setAdapter(productImagesAdapter);

                    productTitle.setText(documentSnapshot.get("product_title").toString());
                    averageRatingMiniview.setText(documentSnapshot.get("average_rating").toString());
                    totalRatingMiniView.setText("(" + (long) documentSnapshot.get("total_ratings") + ") ratings");
                    productPrice.setText("Rs." + documentSnapshot.get("product_price").toString() + "/-");
                    cuttedPrice.setText("Rs." + documentSnapshot.get("cutted_price").toString() + "/-");

                    rewardTitle.setText(documentSnapshot.get("free_coupens") + " " + documentSnapshot.get("free_coupen_title").toString());
                    rewardBody.setText(documentSnapshot.get("free_coupen_body").toString());

                    if ((boolean) documentSnapshot.get("use_tab_layout")) {
                        productDetailsTabsContainer.setVisibility(View.VISIBLE);
                        productDetailsOnlyContainer.setVisibility(View.GONE);

                        productDescription = documentSnapshot.get("product_description").toString();
                        productOtherDetails = documentSnapshot.get("product_other_details").toString();

                        for (long x = 1; x < (long) documentSnapshot.get("total_specification_titles") + 1; x++) {

                            productSpecificationModelList.add(new ProductSpecificationModel(0,
                                    documentSnapshot.get("specification_title_" + x).toString()));


                            for (long m = 1; m < (long) documentSnapshot.get("specification_title_" + x + "_total_fields") + 1; m++) {
                                productSpecificationModelList.add(new ProductSpecificationModel(1,
                                        documentSnapshot.get("specification_title_" + x + "_field_" + m + "_name").toString(),
                                        documentSnapshot.get("specification_title_" + x + "_field_" + m + "_value").toString()));
                            }
                        }
                    } else {
                        productDetailsTabsContainer.setVisibility(View.GONE);
                        productDetailsOnlyContainer.setVisibility(View.VISIBLE);
                        productOnlyDescriptionBody.setText(documentSnapshot.get("product_description").toString());
                    }
                    totalRatings.setText((long) documentSnapshot.get("total_ratings") + " ratings");

                    for (int x = 0; x < 5; x++) {
                        TextView rating = (TextView) ratingsNoContainer.getChildAt(x);

                        // to yaha pr ye rating actually wo ratings ke main container, parent samajh le,
                        // usko access krta h and uske 5 children ko access krke unka text set krta h..... :)
                        rating.setText(String.valueOf((long) documentSnapshot.get((5 - x) + "_star")));

                        ProgressBar progressBar = (ProgressBar) ratingsPrgressBarContainer.getChildAt(x);
                        int maxProgress = Integer.parseInt(String.valueOf((long) documentSnapshot.get("total_ratings")));
                        progressBar.setMax(maxProgress);
                        progressBar.setProgress(Integer.parseInt(String.valueOf((long) documentSnapshot.get((5 - x) + "_star"))));

                    }
                    totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_ratings")));
                    averageRating.setText(documentSnapshot.get("average_rating").toString());
                    productDetailsViewpager.setAdapter(new ProductDetailsAdapter(getSupportFragmentManager(), productDetailsTabLayout.getTabCount(), productDescription, productOtherDetails, productSpecificationModelList));

                    if ((boolean) documentSnapshot.get("COD")) {
                        codIndicator.setVisibility(View.VISIBLE);
                        tvCodIndicator.setVisibility(View.VISIBLE);
                    } else {
                        codIndicator.setVisibility(View.INVISIBLE);
                        tvCodIndicator.setVisibility(View.INVISIBLE);
                    }

                    if (currentUser != null) {

                        if (DBqueries.myRating.size() == 0) {
                            DBqueries.loadRatingList(ProductDetailsActivity.this);
                        }
                        if (DBqueries.wishList.size() == 0) {
                            DBqueries.loadWishList(ProductDetailsActivity.this, loadingDialog, false);
                        }
                        if (DBqueries.cartList.size() == 0) {
                            DBqueries.loadCartList(ProductDetailsActivity.this, loadingDialog, false,badgeCount,new TextView(ProductDetailsActivity.this));
                        } else {
                            loadingDialog.dismiss();
                        }

                    } else {
                        loadingDialog.dismiss();
                    }

                    if (DBqueries.myRatedIds.contains(productID)) {
                        int index = DBqueries.myRatedIds.indexOf(productID);
                        initialRating = Integer.parseInt(String.valueOf(DBqueries.myRating.get(index))) - 1;
                        setRating(initialRating);
                    }
                    if (DBqueries.cartList.contains(productID)) {
                        ALREADY_ADDED_TO_CART = true;
                    } else {
                        ALREADY_ADDED_TO_CART = false;
                    }

                    if (DBqueries.wishList.contains(productID)) {
                        ALREADY_ADDED_TO_WISHLIST = true;
                        addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                    } else {
                        ALREADY_ADDED_TO_WISHLIST = false;
                    }
                    if ((boolean) documentSnapshot.get("in_stock")){
                        /////////////////////////////////////click listener
                        addToCartButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (currentUser == null) {
                                    signInDialog.show();
                                } else {
                                    if (!running_cart_query) {
                                        running_cart_query = true;
                                        if (ALREADY_ADDED_TO_CART) {
                                            running_cart_query = false;
                                            Toast.makeText(ProductDetailsActivity.this, "Already added to cart", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Map<String, Object> addProduct = new HashMap<>();
                                            addProduct.put("product_ID_" + String.valueOf(DBqueries.cartList.size()), productID);
                                            addProduct.put("list_size", (long) DBqueries.cartList.size() + 1);

                                            firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA").document("MY_CART")
                                                    .update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        // abhi agar dekhega to humne  """"DBqueries.cartItemModelList.add(0,""" aisa likha h just 2 line neeche
                                                        // waha index 0 isliye diya h
                                                        // taki jo bhi product add kare hum
                                                        // wo index value 0 ya top ya first position pr hi aaye.......
                                                        if (DBqueries.cartItemModelList.size() != 0) {
                                                            DBqueries.cartItemModelList.add(0,new CartItemModel(CartItemModel.CART_ITEM, documentSnapshot.get("product_image_1").toString(),
                                                                    documentSnapshot.get("product_title").toString(),
                                                                    (long) documentSnapshot.get("free_coupens"),
                                                                    documentSnapshot.get("product_price").toString(),
                                                                    documentSnapshot.get("cutted_price").toString(),
                                                                    (long) 1,
                                                                    (long) 0,
                                                                    (long) 0,
                                                                    productID,
                                                                    (boolean) documentSnapshot.get("in_stock")));
                                                        }

                                                        ALREADY_ADDED_TO_CART = true;
                                                        DBqueries.cartList.add(productID);
                                                        Toast.makeText(ProductDetailsActivity.this, "Product added to cart successfully", Toast.LENGTH_SHORT).show();
                                                        //  addToWishListBtn.setEnabled(true);
                                                        invalidateOptionsMenu();
                                                        running_cart_query = false;
                                                    }

                                                    ////////////////////////
                                                    else {
                                                        //addToWishListBtn.setEnabled(true);
                                                        running_cart_query = false;
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                }
                            }
                        });
                        /////////////////////////////////////click listener
                    }
                    else{
                        buyNowBtn.setVisibility(View.GONE);
                        TextView outOfStock = (TextView) addToCartButton.getChildAt(0);
                        outOfStock.setText("Out of Stock");
                        outOfStock.setTextColor(getResources().getColor(R.color.colorPrimary));
                        outOfStock.setCompoundDrawables(null,null,null,null);
                    }

                } else {
                    loadingDialog.dismiss();
                    String error = task.getException().getMessage();
                    Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });


        viewpagerIndicator.setupWithViewPager(productImagesViewPager, true);

        addToWishListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    //    addToWishListBtn.setEnabled(false); // taki user multiple times btn pr click na kare......
                    if (!running_wishlist_query) {
                        running_wishlist_query = true;
                        if (ALREADY_ADDED_TO_WISHLIST) {

                            int index = DBqueries.wishList.indexOf(productID);
                            DBqueries.removeFromWishlist(index, ProductDetailsActivity.this);
                            addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                        } else {
                            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                            Map<String, Object> addProduct = new HashMap<>();
                            addProduct.put("product_ID_" + String.valueOf(DBqueries.wishList.size()), productID);
                            addProduct.put("list_size", (long) DBqueries.wishList.size() + 1);


                            firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA").document("MY_WISHLIST")
                                    .update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        if (DBqueries.wishlistModelList.size() != 0) {

                                            DBqueries.wishlistModelList.add(new WishlistModel(productID, documentSnapshot.get("product_image_1").toString(),
                                                    documentSnapshot.get("product_title_").toString(),
                                                    (long) documentSnapshot.get("free_coupens_"),
                                                    documentSnapshot.get("average_rating").toString(),
                                                    (long) documentSnapshot.get("total_ratings"),
                                                    documentSnapshot.get("product_price").toString(),
                                                    documentSnapshot.get("cutted_price").toString(),
                                                    (boolean) documentSnapshot.get("COD")));
                                        }

                                        ALREADY_ADDED_TO_WISHLIST = true;
                                        addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                                        DBqueries.wishList.add(productID);
                                        Toast.makeText(ProductDetailsActivity.this, "Product added to wishlist successfully", Toast.LENGTH_SHORT).show();

                                    } else {
                                        addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                                        //addToWishListBtn.setEnabled(true);
                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                    running_wishlist_query = false;
                                }
                            });

                        }
                    }
                }
            }
        });

        productDetailsViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(productDetailsTabLayout));

        productDetailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                productDetailsViewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        /////////////////rating layout
        rateNowContainer = findViewById(R.id.rate_now_container);

        for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
            final int starPosition = x;
            rateNowContainer.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUser == null) {
                        signInDialog.show();
                    } else {
                        if (starPosition != initialRating) {
                            if (!running_rated_query) {

                                running_rated_query = true;
                                setRating(starPosition);

                                Map<String, Object> updateRating = new HashMap<>();
                                if (DBqueries.myRatedIds.contains(productID)) {
                                    TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                    TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                    // to agar abhi myrateids ke list me koi bhi product ka id h
                                    // to matlab user ne already kabhi to raings diya tha warna nhi

                                    // so abhi apan user ki rating ko update karenge agar user ne say pehle
                                    // 4 star diye the and abhi wo 3 dena chahta h

                                    // to sabse pehle hum user ki pehli di hui rating ka count ghira denge and abhi wali ka count badha denge

                                    // matlab agar pehle 3 tha so 3 stars ka total count decrease krdenge
                                    updateRating.put(initialRating + 1 + "_star", Long.parseLong(oldRating.getText().toString()) - 1);

                                    // and abhi say 4 h to uska count badha denge
                                    updateRating.put(starPosition + 1 + "_star", Long.parseLong(finalRating.getText().toString()) + 1);

                                    updateRating.put("average_rating", String.valueOf(calculateAverageRating((long) starPosition - initialRating, true)));

                                    Toast.makeText(ProductDetailsActivity.this, "stage 1 bancho", Toast.LENGTH_SHORT).show();

                                } else {
                                    //abhi ye updation Products wale collection me krne wale h
                                    updateRating.put((starPosition + 1) + "_star", (long) documentSnapshot.get(starPosition + 1 + "_star") + 1);
                                    updateRating.put("average_rating", String.valueOf(calculateAverageRating((long) starPosition + 1, false))); //user agar pehli baar rating de rha h to update false hoga
                                    updateRating.put("total_ratings", (long) documentSnapshot.get("total_ratings") + 1);
                                }
                                firebaseFirestore.collection("PRODUCTS")
                                        .document(productID)
                                        .update(updateRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProductDetailsActivity.this, "haa haa aagaya bancho andar :)", Toast.LENGTH_SHORT).show();

                                            Map<String, Object> myRating = new HashMap<>();

                                            if (DBqueries.myRatedIds.contains(productID)) {
                                                // hum star position +1 krte h coz kyuki yaha pr apan usko zero index se access krte h but wo actually me 1 based h index
                                                myRating.put("rating_" + DBqueries.myRatedIds.indexOf(productID), (long) starPosition + 1);
                                            } else {
                                                myRating.put("list_size", (long) DBqueries.myRatedIds.size() + 1);
                                                myRating.put("product_ID_" + DBqueries.myRatedIds.size(), productID);
                                                myRating.put("rating_" + DBqueries.myRatedIds.size(), (long) (starPosition + 1));
                                            }
                                            // ye neeche wali condition tab ki h jab user first time rating deta h product ko
                                            firebaseFirestore.collection("USERS")
                                                    .document(currentUser.getUid())
                                                    .collection("USER_DATA")
                                                    .document("MY_RATINGS")
                                                    .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        if (DBqueries.myRatedIds.contains(productID)) {

                                                            DBqueries.myRating.set(DBqueries.myRatedIds.indexOf(productID), (long) starPosition + 1);

                                                            TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                                            TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                                            // to yaha pr ye rating actually wo ratings ke main container, parent samajh le,
                                                            // usko access krta h and uske 5 children ko access krke unka text set krta h..... :)
                                                            finalRating.setText(String.valueOf(Integer.parseInt(finalRating.getText().toString()) + 1));
                                                            oldRating.setText(String.valueOf(Integer.parseInt(oldRating.getText().toString()) - 1));
                                                        } else {
                                                            DBqueries.myRatedIds.add(productID);
                                                            DBqueries.myRating.add((long) (starPosition + 1));
                                                            TextView rating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                                            // to yaha pr ye rating actually wo ratings ke main container, parent samajh le,
                                                            // usko access krta h and uske 5 children ko access krke unka text set krta h..... :)
                                                            rating.setText(String.valueOf(Integer.parseInt(rating.getText().toString()) + 1));

                                                            totalRatingMiniView.setText("(" + ((long) documentSnapshot.get("total_ratings") + 1) + ")ratings");
                                                            totalRatings.setText((long) documentSnapshot.get("total_ratings") + 1 + " ratings");
                                                            totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_ratings") + 1));

                                                            Toast.makeText(ProductDetailsActivity.this, "Thanx for feedback", Toast.LENGTH_SHORT).show();
                                                        }

                                                        for (int x = 0; x < 5; x++) {
                                                            TextView ratingfigures = (TextView) ratingsNoContainer.getChildAt(x);

                                                            ProgressBar progressBar = (ProgressBar) ratingsPrgressBarContainer.getChildAt(x);
                                                            int maxProgress = Integer.parseInt(totalRatingsFigure.getText().toString());
                                                            progressBar.setMax(maxProgress);

                                                            progressBar.setProgress(Integer.parseInt(ratingfigures.getText().toString()));
                                                        }
                                                        initialRating = starPosition;
                                                        averageRating.setText(String.valueOf(calculateAverageRating(0, true)));
                                                        averageRatingMiniview.setText(String.valueOf(calculateAverageRating(0, true)));

                                                        if (DBqueries.wishList.contains(productID) && DBqueries.wishlistModelList.size() != 0) {

                                                            int index = DBqueries.wishList.indexOf(productID);
                                                            DBqueries.wishlistModelList.get(index).setRating(averageRating.getText().toString());
                                                            DBqueries.wishlistModelList.get(index).setTotalRatings(Long.parseLong(totalRatingsFigure.getText().toString()));

                                                        }

                                                    } else {
                                                        setRating(initialRating);// agar kuchh process me gadbad ho gayi to initial rating hi wapas set hojayega
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(ProductDetailsActivity.this, "yaha hag rha h tu", Toast.LENGTH_SHORT).show();
                                                    }
                                                    running_rated_query = false;
                                                }
                                            });
                                        } else {
                                            Toast.makeText(ProductDetailsActivity.this, "kyu bancho :):):)", Toast.LENGTH_SHORT).show();
                                            running_rated_query = false;
                                            setRating(initialRating);// agar kuchh process me gadbad ho gayi to initial rating hi wapas set hojayega
                                            String error = task.getException().getMessage();
                                            Toast.makeText(ProductDetailsActivity.this, "next wale me hag rhs h tu", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
        /////////////////rating layout

        buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    DeliveryActivity.cartItemModelList = new ArrayList<>();
                    DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.CART_ITEM, documentSnapshot.get("product_image_1").toString(),
                            documentSnapshot.get("product_title").toString(),
                            (long) documentSnapshot.get("free_coupens"),
                            documentSnapshot.get("product_price").toString(),
                            documentSnapshot.get("cutted_price").toString(),
                            (long) 1,
                            (long) 0,
                            (long) 0,
                            productID,
                            (boolean) documentSnapshot.get("in_stock")));

                    DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));

                    if (DBqueries.addressesModelList.size() == 0) {
                        DBqueries.loadAddress(ProductDetailsActivity.this, loadingDialog);
                    }
                    else{
                        loadingDialog.dismiss();
                        Intent deliveryIntent = new Intent(ProductDetailsActivity.this, DeliveryActivity.class);
                        startActivity(deliveryIntent);
                    }
                }
            }
        });


        ///////////////////////////////coupen dialog
        final Dialog checkCoupenPriceDialog = new Dialog(ProductDetailsActivity.this);
        checkCoupenPriceDialog.setContentView(R.layout.coupen_reedem_dialog);
        checkCoupenPriceDialog.setCancelable(true);
        checkCoupenPriceDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView toggleRecyclerView = checkCoupenPriceDialog.findViewById(R.id.toggle_recyclerview);
        coupensRecyclerView = checkCoupenPriceDialog.findViewById(R.id.coupens_recyclerview);
        selectedCoupen = checkCoupenPriceDialog.findViewById(R.id.selected_coupen);

        TextView originalPrice = checkCoupenPriceDialog.findViewById(R.id.original_price);
        TextView discountedPrice = checkCoupenPriceDialog.findViewById(R.id.discounted_price);

        coupenTitle = checkCoupenPriceDialog.findViewById(R.id.coupen_title);
        coupenExpiryDate = checkCoupenPriceDialog.findViewById(R.id.coupen_validity);
        coupenBody = checkCoupenPriceDialog.findViewById(R.id.coupen_body);


        LinearLayoutManager layoutManager = new LinearLayoutManager(ProductDetailsActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        coupensRecyclerView.setLayoutManager(layoutManager);

        List<RewardModel> rewardModelList = new ArrayList<>();
        rewardModelList.add(new RewardModel("Cashback", "till 2nd,June 2020", "GET 30% OFF on all the elctronic products through Paytm"));
        rewardModelList.add(new RewardModel("Discount", "till 2nd,June 2020", "GET 30% OFF on all the elctronic products through Paytm"));
        rewardModelList.add(new RewardModel("Buy One get one free", "till 2nd,June 2020", "GET 30% OFF on all the elctronic products through Paytm"));
        rewardModelList.add(new RewardModel("Casback", "till 2nd,June 2020", "GET 30% OFF on all the elctronic products through Paytm"));
        rewardModelList.add(new RewardModel("Casback", "till 2nd,June 2020", "GET 30% OFF on all the elctronic products through Paytm"));

        MyRewardsAdapter myRewardsAdapter = new MyRewardsAdapter(rewardModelList, true);
        coupensRecyclerView.setAdapter(myRewardsAdapter);
        myRewardsAdapter.notifyDataSetChanged();

        toggleRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRecyclerView();
            }
        });
        ////////////////////////////////////////////// coupen dialog

        coupenRedeemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCoupenPriceDialog.show();
            }
        });


        signInDialog = new Dialog(ProductDetailsActivity.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button dialogSignInBtn = signInDialog.findViewById(R.id.sign_in_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.sign_up_btn);
        final Intent registerIntent = new Intent(ProductDetailsActivity.this, RegisterActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = false;
                startActivity(registerIntent);
            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpFragment.disableCloseBtn = true;
                SignInFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = true;
                startActivity(registerIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            coupenRedemptionLayout.setVisibility(View.GONE);
        } else {
            coupenRedemptionLayout.setVisibility(View.VISIBLE);
        }

        if (currentUser != null) {

            if (DBqueries.myRating.size() == 0) {
                DBqueries.loadRatingList(ProductDetailsActivity.this);
            }
            if (DBqueries.wishList.size() == 0) {
                DBqueries.loadWishList(ProductDetailsActivity.this, loadingDialog, false);
            } else {
                loadingDialog.dismiss();
            }

        } else {
            loadingDialog.dismiss();
        }
        if (DBqueries.myRatedIds.contains(productID)) {
            int index = DBqueries.myRatedIds.indexOf(productID);
            initialRating = Integer.parseInt(String.valueOf(DBqueries.myRating.get(index))) - 1;
            setRating(initialRating);
        }
        if (DBqueries.cartList.contains(productID)) {
            ALREADY_ADDED_TO_CART = true;
        } else {
            ALREADY_ADDED_TO_CART = false;
        }

        if (DBqueries.wishList.contains(productID)) {
            ALREADY_ADDED_TO_WISHLIST = true;
            addToWishListBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
        } else {
            addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
            ALREADY_ADDED_TO_WISHLIST = false;
        }
        invalidateOptionsMenu();

    }

    public static void showDialogRecyclerView() {

        if (coupensRecyclerView.getVisibility() == View.GONE) {
            coupensRecyclerView.setVisibility(View.VISIBLE);
            selectedCoupen.setVisibility(View.GONE);
        } else {
            coupensRecyclerView.setVisibility(View.GONE);
            selectedCoupen.setVisibility(View.VISIBLE);
        }
    }

    public static void setRating(int starPosition) {
        for (int x = 0; x < rateNowContainer.getChildCount(); x++) {

            ImageView starBtn = (ImageView) rateNowContainer.getChildAt(x);
            starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#bebebe")));

            if (x <= starPosition) {
                starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffbb00")));
            }
        }
    }


    private String calculateAverageRating(long currentUserRating, boolean update) {
        Double totalStars = Double.valueOf(0);
        for (int x = 1; x < 6; x++) {
            TextView ratingNo = (TextView) ratingsNoContainer.getChildAt(5 - x); // multiply by x kiya taki humko pata chale ki kitne users ne stars diya h
            totalStars = totalStars + (Long.parseLong(ratingNo.getText().toString()) * x);
            // matlab 1 star multiply by uske no.of users, 2 stars multiply by it's users and so on
        }
        totalStars = totalStars + currentUserRating;
        if (update) {
            return String.valueOf(totalStars / Long.parseLong(totalRatingsFigure.getText().toString())).substring(0, 3);
        } else {
            return String.valueOf(totalStars / (Long.parseLong(totalRatingsFigure.getText().toString()) + 1)).substring(0, 3);
        }
        // abhi ye if statements isliye banaya h kyuki say jab user first time koi bhi product ko rating de rha h to uss time apan total rating ka value
        // plus (+1) 1 karenge
        // coz agar user sirf change kr rha h
        // say 3 star se 4 star kiya to uss time total ratings to same hi rahegi na
        // islliye uss time plus one (+1) nhi karenge
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_and_cart_icon, menu);
        cartItem = menu.findItem(R.id.main_cart_icon);

        cartItem.setActionView(R.layout.badge_layout);
        //to yaha pr humne wo main home page me upar ka 3rd icon jo ki h cart icon usko find kiya h

        ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
        badgeIcon.setImageResource(R.mipmap.cart_white);

        badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);
        if (currentUser != null) {
            if (DBqueries.cartList.size() == 0) {
                DBqueries.loadCartList(ProductDetailsActivity.this, loadingDialog, false,badgeCount,new TextView(ProductDetailsActivity.this));
            }
            else{
                badgeCount.setVisibility(View.VISIBLE);
                if( DBqueries.cartList.size() < 99) {
                    badgeCount.setText(String.valueOf(DBqueries.cartList.size()));
                }
                else{
                    badgeCount.setText("99");
                }
            }
        }
        cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    Intent cartIntent = new Intent(ProductDetailsActivity.this, MainActivity.class);
                    showCart = true;
                    startActivity(cartIntent);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // ye h top right side ke 3 items ke liye, unme se koi ek bhi selecct hua to uska code yaha h......

        int id = item.getItemId();
        if (id == R.id.main_search_icon) {
            return true;
        } else if (id == R.id.main_cart_icon) {

            if (currentUser == null) {
                signInDialog.show();
            } else {
                Intent cartIntent = new Intent(ProductDetailsActivity.this, MainActivity.class);
                showCart = true;
                startActivity(cartIntent);
                return true;
            }
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}



















