package in.softment.travler.Fragments;

import static com.android.billingclient.api.BillingClient.SkuType.SUBS;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.softment.travler.Adapters.CategoriesAdapter;
import in.softment.travler.BuildConfig;
import in.softment.travler.MainActivity;
import in.softment.travler.Model.Category;
import in.softment.travler.Model.UserModel;
import in.softment.travler.R;
import in.softment.travler.Utils.Constants;
import in.softment.travler.Utils.ProgressHud;
import in.softment.travler.Utils.Security;
import in.softment.travler.Utils.Services;


public class WorkoutFragment extends Fragment implements PurchasesUpdatedListener {
    private RecyclerView recyclerView;
    private CategoriesAdapter categoriesAdapter;
    private Context context;
    private ArrayList<Category> categories;
    private BottomSheetDialog sheetDialog;
    private BottomSheetDialog membershipDialog;
    private BillingClient billingClient;
    public static final String PREF_FILE= "MyPref";
    public static final String SUBSCRIBE_KEY= "subscribe";
    public static final String ITEM_SKU_SUBSCRIBE= "travler_premium_sub";
    public WorkoutFragment(Context context) {
      this.context = context;
    }
    public WorkoutFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        categories = new ArrayList<>();
        categoriesAdapter = new CategoriesAdapter(context, categories);
        recyclerView.setAdapter(categoriesAdapter);

        if (!UserModel.data.subscription_id.isEmpty()) {
            checkStripeSubscriptionStatus(UserModel.data.getSubscription_id());
        }


        view.findViewById(R.id.membership).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sheetDialog = new BottomSheetDialog(context, R.style.BottomSheetStyle);
                View view1 = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog,(LinearLayout)view.findViewById(R.id.sheet));

                TextView name = view1.findViewById(R.id.name);
                TextView email = view1.findViewById(R.id.email);
                ImageView lock = view1.findViewById(R.id.lock);
                ImageView goPremiumImg = view1.findViewById(R.id.img);
                TextView goPremiumText = view1.findViewById(R.id.goPremiumText);
                TextView accountType = view1.findViewById(R.id.accountType);
                name.setText(UserModel.data.name);
                email.setText(UserModel.data.email);



                if (getSubscribeValueFromPref() || (UserModel.data.expireDate.compareTo(Constants.currentDate) > 0)){


                    Log.d("SOFTMENT22",UserModel.data.getExpireDate().getTime()+"");

                    Log.d("SOFTMENT2222",Constants.currentDate+"");
                    long diff = UserModel.data.expireDate.getTime() - Constants.currentDate.getTime();

                    Log.d("SOFTMENT2222232",diff+"");
                    int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
                    if (numOfDays > 1) {
                        goPremiumText.setText((numOfDays + 1) +" Days left");
                    }
                    else {
                        goPremiumText.setText((numOfDays + 1)+" Day left");
                    }

                    accountType.setText("Premium");
                    lock.setImageResource(R.drawable.crown_yellow);
                    goPremiumImg.setImageResource(R.drawable.clock);


                }
                else {
                    view1.findViewById(R.id.goPremium).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            membershipDialog = new BottomSheetDialog(context, R.style.BottomSheetStyle);
                            View view2 = LayoutInflater.from(context).inflate(R.layout.membership_dialog,(LinearLayout)view.findViewById(R.id.membershipSheet));
                            view2.findViewById(R.id.payBtn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    membershipDialog.dismiss();
                                    if (billingClient.isReady()) {
                                        initiatePurchase();
                                    }
                                    //else reconnect service
                                    else{
                                        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(WorkoutFragment.this).build();
                                        billingClient.startConnection(new BillingClientStateListener() {
                                            @Override
                                            public void onBillingSetupFinished(BillingResult billingResult) {
                                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                                    initiatePurchase();
                                                } else {
                                                    Toast.makeText(context,"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            @Override
                                            public void onBillingServiceDisconnected() {
                                                Toast.makeText(context,"Service Disconnected ",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                            membershipDialog.setContentView(view2);
                            membershipDialog.show();
                            sheetDialog.dismiss();
                        }
                    });

                    accountType.setText("Free");
                    goPremiumText.setText("Go Premium");
                    lock.setImageResource(R.drawable.lock);
                    goPremiumImg.setImageResource(R.drawable.crown);

                }



                view1.findViewById(R.id.shareApp).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Travler");
                            String shareMessage= "\nLet me recommend you this application\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch(Exception e) {
                            //e.toString();
                        }
                        sheetDialog.dismiss();
                    }
                });

                view1.findViewById(R.id.rateUsOnAppStore).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(myAppLinkToMarket);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show();
                        }

                        sheetDialog.dismiss();

                    }
                });

                view1.findViewById(R.id.privacyPolicy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/privacy-policy/"));
                        startActivity(browserIntent);
                        sheetDialog.dismiss();
                    }
                });

                view1.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);

                        builder.setTitle("Logout");
                        builder.setMessage("Are you sure you want to logout?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Services.logout(context);
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                        builder.setCancelable(false);
                        builder.show();

                        sheetDialog.dismiss();
                    }
                });

                sheetDialog.setContentView(view1);
                sheetDialog.show();

            }
        });


        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(SUBS);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        handlePurchases(queryPurchases);
                    }
                    //if no item in purchase list means subscription is not subscribed
                    //Or subscription is cancelled and not renewed for next month
                    // so update pref in both cases
                    // so next time on app launch our premium content will be locked
                    else{
                        saveSubscribeValueToPref(false);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(context,"Service Disconnected",Toast.LENGTH_SHORT).show();
            }
        });




        ProgressHud.show(context,"Loading...");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://worldtimeapi.org/api/ip/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject parentObject = null;
                        try {
                            parentObject = new JSONObject(response);
                        } catch (JSONException e) {
                            getCategoryData();
                            e.printStackTrace();
                        }
                        try {
                            Constants.currentDate = new Date(Long.parseLong(parentObject.getString("unixtime")) * 1000);


                            getCategoryData();
                        } catch (JSONException e) {
                            getCategoryData();
                            e.printStackTrace();


                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        getCategoryData();

                    }
                }){

        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);





        return view;
    }



    private void initiatePurchase() {
        List<String> skuList = new ArrayList<>();
        skuList.add(ITEM_SKU_SUBSCRIBE);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(SUBS);
        BillingResult billingResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            billingClient.querySkuDetailsAsync(params.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                         List<SkuDetails> skuDetailsList) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetailsList.get(0))
                                            .build();
                                    billingClient.launchBillingFlow((Activity) context, flowParams);
                                } else {
                                    //try to add subscription item "sub_example" in google play console
                                    Toast.makeText(context, "Item not Found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context,
                                        " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(context,
                    "Sorry Subscription not Supported. Please Update Play Store", Toast.LENGTH_SHORT).show();
        }
    }
    public void getCategoryData() {
        FirebaseFirestore.getInstance().collection("Categories").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                ProgressHud.dialog.dismiss();
                if (error == null) {
                    categories.clear();
                    if (value != null && !value.isEmpty()) {

                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            Category category = documentSnapshot.toObject(Category.class);
                            categories.add(category);
                        }
                    }
                    categoriesAdapter.notifyDataSetChanged();
                }
                else {
                    Services.showDialog(context, "Error",error.getLocalizedMessage());
                }
            }
        });
    }

    public void checkStripeSubscriptionStatus(String subscriptionId) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://softment.in/Travler/stripe/vendor/retrieve_subscription.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {



                        try {
                            //JSONArray myJSON= new JSONArray(response);

                            JSONObject parentObject = new JSONObject(response);

                            UserModel.data.expireDate = new Date(Long.parseLong(parentObject.getString("current_period_end"))*1000);

                            Log.d("WOWSOFTMENT",UserModel.data.expireDate.toString());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            ProgressHud.dialog.dismiss();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {


                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<String,String>();
                map.put("subscription_id",subscriptionId);
                map.put("Content-Type" ,"application/x-www-form-urlencoded");
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        requestQueue.add(stringRequest);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item subscribed
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already subscribed then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(SUBS);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){
                handlePurchases(alreadyPurchases);
                if (Constants.currentDate.compareTo(UserModel.data.getExpireDate()) > 0) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(Constants.currentDate);
                    c.add(Calendar.DATE, 30);
                    UserModel.data.expireDate = c.getTime();
                    Map<String, Object> map = new HashMap<>();
                    map.put("expireDate", c.getTime());
                    FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());
                }
            }
        }
        //if Purchase canceled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //Toast.makeText(context,"Purchase Canceled",Toast.LENGTH_SHORT).show();
        }
        // Handle any other error msgs
        else {
            Toast.makeText(context,"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private SharedPreferences getPreferenceObject() {
        return context.getSharedPreferences(PREF_FILE, 0);
    }
    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = context.getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }
    private boolean getSubscribeValueFromPref(){
        return getPreferenceObject().getBoolean( SUBSCRIBE_KEY,false);
    }
    private void saveSubscribeValueToPref(boolean value){
        getPreferenceEditObject().putBoolean(SUBSCRIBE_KEY,value).commit();
    }

    void handlePurchases(List<Purchase>  purchases) {
        for(Purchase purchase:purchases) {
            //if item is purchased

            if (purchase.getSkus().contains(ITEM_SKU_SUBSCRIBE) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
            {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    // show error to user
                    Toast.makeText(context, "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if(!getSubscribeValueFromPref()){
                        saveSubscribeValueToPref(true);
                        Calendar c = Calendar.getInstance();
                        c.setTime(Constants.currentDate);
                        c.add(Calendar.DATE, 30);
                        UserModel.data.expireDate = c.getTime();
                        Map<String , Object> map = new HashMap<>();
                        map.put("expireDate",c.getTime());
                        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map, SetOptions.merge());
                        Toast.makeText(context, "Item Purchased", Toast.LENGTH_SHORT).show();
                        ((MainActivity)context).recreate();
                    }
                }
            }
            //if purchase is pending
            else if(purchase.getSkus().contains(ITEM_SKU_SUBSCRIBE) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING)
            {
                Toast.makeText(context,
                        "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
            }
            //if purchase is unknown mark false
            else if(purchase.getSkus().contains(ITEM_SKU_SUBSCRIBE) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE)
            {
                saveSubscribeValueToPref(false);
               // premiumContent.setVisibility(View.GONE);
               // subscribe.setVisibility(View.VISIBLE);
               // subscriptionStatus.setText("Subscription Status : Not Subscribed");
                Toast.makeText(context, "Purchase Status Unknown", Toast.LENGTH_SHORT).show();
            }
        }
    }
    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                saveSubscribeValueToPref(true);
                ((MainActivity)context).recreate();
            }
        }
    };

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8GS3G+4dqS7KPu804zRswQ3ZHVh3a48JjZGbkl/OzywogaqyFvD/euI+kLB9s3cSvMGSjpPKDrRu9ymk360klbw6ks9WfGFObm+4BK3a+8JWekGgtkl13GTipVNxWgj9VVYQRjbJnKbGa/UqG97rd/3r3jQ+pP/AzCJJthW3gIXQYCnB733BpfPyzcTN6QlGd8Ki8nLHlVjSxfRRJkiKrMn7h7d/ThD7afBQjaA5VbSTz7myRF1OA8GMnUH9wFt9RlCw5o/VBtfen7pdWp6f7r84qhg4+611BRW2+zI8k/sA9jEJz9WIDZkrlV6XCkDXiBLS/uvyCIVfv+LEiTDhBwIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }
}