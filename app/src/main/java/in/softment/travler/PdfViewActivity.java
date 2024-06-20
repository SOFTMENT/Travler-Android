package in.softment.travler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.softment.travler.Model.UserModel;
import in.softment.travler.Model.Video;
import in.softment.travler.Utils.Constants;
import in.softment.travler.Utils.ProgressHud;
import in.softment.travler.Utils.Services;

public class PdfViewActivity extends AppCompatActivity {
    private WebView myWebView;
    private FrameLayout frameLayout;
    private ProgressBar progressBar;
    private ArrayList<Video> pdfs = new ArrayList<>();
    private boolean hasMembership = false;
    public static final String SUBSCRIBE_KEY= "subscribe";
    public static final String PREF_FILE= "MyPref";
    private TextView number1,number2,number3;
    private ImageView imageView1;
    private int count = 0;
    private String cat_id;
    private ImageView lastBtn, doneBtn;
    private RelativeLayout thirdView;
    private LinearLayout stack1;
    private TextView month1, month2, month3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        cat_id = getIntent().getStringExtra("cat_id");
        boolean isCatFree = getIntent().getBooleanExtra("isCatFree", false);
        ImageView lockBtn = findViewById(R.id.lock);
        doneBtn = findViewById(R.id.done);
        thirdView = findViewById(R.id.thirdView);
        lastBtn = findViewById(R.id.thirdBtn);
        number1 = findViewById(R.id.number1);
        number2 = findViewById(R.id.number2);
        number3 = findViewById(R.id.number3);

        stack1 = findViewById(R.id.stack1);

        month1 = findViewById(R.id.month1);
        month2 = findViewById(R.id.month2);
        month3 = findViewById(R.id.month3);

        imageView1 = findViewById(R.id.image1);

        findViewById(R.id.firstBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstBtnClicked();
            }
        });

        findViewById(R.id.thirdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastBtnClicked();
            }
        });

        if (getSubscribeValueFromPref() || (UserModel.data.expireDate.compareTo(Constants.currentDate) > 0 && UserModel.data.subscription_status.equalsIgnoreCase("active")) || isCatFree){
            hasMembership = true;

            lockBtn.setVisibility(View.GONE);
        }
        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            View view = getLayoutInflater().inflate(R.layout.error_message_layout, null);
            TextView titleView = view.findViewById(R.id.title);
            TextView msg = view.findViewById(R.id.message);
            titleView.setText("Required Premium Membership");
            msg.setText("Please purchase premium membership and unlock all contents.");
            builder.setView(view);
            AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    finish();
                }
            });
            alertDialog.show();
            lockBtn.setVisibility(View.VISIBLE);

        }




        myWebView = (WebView) findViewById(R.id.webpage);
        myWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView webView, String url) {

                webView.loadUrl(url);

                frameLayout.setVisibility(View.VISIBLE);
                return  false;
            }

        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                setTitle("Loading...");
                if (progress == 100) {
                    frameLayout.setVisibility(View.GONE);
                    setTitle(webView.getTitle());

                    //   lottieAnimationView.setVisibility(View.GONE);


                }
                super.onProgressChanged(webView, progress);
            }
        });

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setAllowContentAccess(true);
        myWebView.setScrollbarFadingEnabled(false);
        myWebView.setScrollbarFadingEnabled(false);
        frameLayout = findViewById(R.id.frame);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PdfViewActivity.this,AllVideosViewController.class);
                intent.putExtra("cat_id","KZBFOnWEiklt48D9XjNs");
                intent.putExtra("title","MOVEMENT LIBRARY");
                startActivity(intent);
            }
        });

        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        getPdfs(cat_id);


    }

    public  void firstBtnClicked(){
        if (count == 0) {
            finish();
        }
        else {
            count = count - 1;
            if (count == 0) {

                stack1.setVisibility(View.GONE);
                imageView1.setVisibility(View.VISIBLE);
                imageView1.setImageResource(R.drawable.group17);

                number2.setText(Services.convertDateToDay(pdfs.get(0).getDate()));
                month2.setText(Services.convertDateToMonth(pdfs.get(0).getDate()));

                number3.setText(Services.convertDateToDay(pdfs.get(1).getDate()));
                month3.setText(Services.convertDateToMonth(pdfs.get(1).getDate()));



            }
            else {
                stack1.setVisibility(View.VISIBLE);
                number1.setText(Services.convertDateToDay(pdfs.get(count - 1).getDate()));
                month1.setText(Services.convertDateToMonth(pdfs.get(count - 1).getDate()));
                imageView1.setVisibility(View.GONE);


                number2.setText(Services.convertDateToDay(pdfs.get(count).getDate()));
                month2.setText(Services.convertDateToMonth(pdfs.get(count).getDate()));

                try {
                    number3.setText(Services.convertDateToDay(pdfs.get(count + 1).getDate()));
                    month3.setText(Services.convertDateToMonth(pdfs.get(count + 1).getDate()));
                }
                catch (Exception ignored) {

                }


            }
            loadPdf();
        }

        thirdView.setVisibility(View.VISIBLE);
        doneBtn.setVisibility(View.VISIBLE);

    }

    public void lastBtnClicked(){
        if (hasMembership) {

            stack1.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.GONE);
            number1.setText(Services.convertDateToDay(pdfs.get(count).getDate()));
            month1.setText(Services.convertDateToMonth(pdfs.get(count).getDate()));


            number2.setText(Services.convertDateToDay(pdfs.get(count + 1).getDate()));
            month2.setText(Services.convertDateToMonth(pdfs.get(count + 1).getDate()));

            count = count + 1;

            if (count == pdfs.size() - 1) {
                thirdView.setVisibility(View.GONE);
                doneBtn.setVisibility(View.GONE);
            }
            else {

                number3.setText(Services.convertDateToDay(pdfs.get(count + 1).getDate()));
                month3.setText(Services.convertDateToMonth(pdfs.get(count + 1).getDate()));

                thirdView.setVisibility(View.VISIBLE);
                doneBtn.setVisibility(View.VISIBLE);
            }
            loadPdf();
        }
        else {
            Services.showDialog(PdfViewActivity.this,"Required Premium Membership","Please purchase premium membership and unlock all contents.");
        }
    }

    private boolean getSubscribeValueFromPref(){
        return getPreferenceObject().getBoolean( SUBSCRIBE_KEY,false);
    }

    private SharedPreferences getPreferenceObject() {
        return getSharedPreferences(PREF_FILE, 0);
    }
    public void loadPdf(){

        if (count <= (pdfs.size() - 1)) {
            WriteBatch writeBatch =  FirebaseFirestore.getInstance().batch();
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            Map<String, Object> map = new HashMap();

                map.put("count", FieldValue.increment(1));
                writeBatch.set(documentReference.collection("WatchedCount").document(cat_id),map, SetOptions.merge());
                Map<String, String> map1 = new HashMap();
                map1.put("videoId", pdfs.get(count).id);
                writeBatch.set(documentReference.collection("Watched").document(pdfs.get(count).id),map1);


            try {

                String url= URLEncoder.encode(pdfs.get(count).pdfLink,"UTF-8");
                myWebView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    public void getPdfs(String cat_id){
        ProgressHud.show(this,"Loading...");
        FirebaseFirestore.getInstance().collection("Categories").document(cat_id).collection("Videos").orderBy("date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    pdfs.clear();
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            Video video = documentSnapshot.toObject(Video.class);
                            if (video.type.equalsIgnoreCase("pdf")) {
                                pdfs.add(video);
                            }
                        }

                        int  Mindex = 0;

                        for (int i = 0; i< pdfs.size(); i++) {
                            if (Services.convertDateToDay(pdfs.get(i).getDate()).equals(Services.convertDateToDay(new Date()))
                            && Services.convertDateToMonth(pdfs.get(i).getDate()).equals(Services.convertDateToMonth(new Date()))
                            ) {
                                Mindex = i;
                                break;
                            }
                        }

                        count = Mindex + 1;

                        Map<String, String> map = new HashMap<>();
                        map.put("cat_id",cat_id);
                        map.put("Location","GetPDF Function");
                        FirebaseFirestore.getInstance().collection("ERROR").document().set(map);
                        firstBtnClicked();

                    }
                }
                else {
                    Services.showDialog(PdfViewActivity.this,"Error",task.getException().getLocalizedMessage());
                }
            }
        });



    }
}