package in.softment.travler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

import in.softment.travler.Model.UserModel;
import in.softment.travler.Utils.ProgressHud;

public class DisclaimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressHud.show(DisclaimerActivity.this,"Wait...");
                Map map = new HashMap();
                map.put("isDiscAccepted",true);
                FirebaseFirestore.getInstance().collection("Users").document(UserModel.data.getUid()).set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ProgressHud.dialog.dismiss();
                        Intent intent = null;
                        intent = new Intent(DisclaimerActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}