package in.softment.travler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import in.softment.travler.Utils.ProgressHud;
import in.softment.travler.Utils.Services;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_sign_up);

        EditText name = findViewById(R.id.fullName);
        EditText email = findViewById(R.id.emailAddress);
        EditText password = findViewById(R.id.password);
        TextView versionCode = findViewById(R.id.version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionCode.setText("Version - "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        findViewById(R.id.createAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sFullName = name.getText().toString().trim();
                String sEmail = email.getText().toString().trim();
                String sPassword = password.getText().toString().trim();

                if (sFullName.isEmpty()) {
                    Services.showCenterToast(SignUpActivity.this,"Enter Full Name");
                }
                else if (sEmail.isEmpty()) {
                    Services.showCenterToast(SignUpActivity.this,"Enter Email Address");
                }
                else if (sPassword.isEmpty()) {
                    Services.showCenterToast(SignUpActivity.this,"Enter Password");
                }
                else {
                    ProgressHud.show(SignUpActivity.this,"Creating Account...");
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            ProgressHud.dialog.dismiss();
                            if (task.isSuccessful()){
                                String profilePic = "https://firebasestorage.googleapis.com/v0/b/travlr-61577.appspot.com/o/png-4.png?alt=media&token=a2dbdc8f-747f-4045-907f-4dfa200cacfe";
                                Map<String, Object> map = new HashMap<>();
                                map.put("uid" ,FirebaseAuth.getInstance().getCurrentUser().getUid());
                                map.put("profilePic",profilePic);
                                map.put("email",sEmail);
                                map.put("name", sFullName);
                                map.put("hasMembership",false);
                                map.put("registredAt",FirebaseAuth.getInstance().getCurrentUser().getMetadata().getCreationTimestamp());
                                map.put("regiType","password");
                                Services.addUserDataOnServer(SignUpActivity.this,FirebaseAuth.getInstance().getCurrentUser().getUid(),"password", map);
                            }
                            else {
                                Services.showDialog(SignUpActivity.this,"Error",task.getException().getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}