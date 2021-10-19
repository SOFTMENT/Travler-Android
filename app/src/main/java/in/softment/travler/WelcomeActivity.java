package in.softment.travler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import in.softment.travler.Utils.Services;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FirebaseMessaging.getInstance().subscribeToTopic("travlr");
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String providerId = FirebaseAuth.getInstance().getCurrentUser().getProviderData().get(0).getProviderId();
            if (providerId.equals("password")) {
                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                    Services.getCurrentUserData(WelcomeActivity.this,FirebaseAuth.getInstance().getCurrentUser().getUid(),false);
                }
                else {
                   gotoSignInPage();
                }
            }
            else {
                Services.getCurrentUserData(WelcomeActivity.this,FirebaseAuth.getInstance().getCurrentUser().getUid(),false);
            }


        }
        else {
            gotoSignInPage();
        }


    }

    public void gotoSignInPage(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        },2000);

    }
}