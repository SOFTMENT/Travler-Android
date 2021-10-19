package in.softment.travler.Utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import in.softment.travler.MainActivity;
import in.softment.travler.Model.UserModel;
import in.softment.travler.R;
import in.softment.travler.SignInActivity;
import in.softment.travler.SignUpActivity;


public class Services {


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getTimeAgo(Date date) {
        long time = date.getTime();
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = currentDate().getTime();
        if (time > now || time <= 0) {
            return "in the future";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "moments ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 60 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 2 * HOUR_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static void handleFirebaseERROR(Context context, String errorCode){
        switch (errorCode) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.token_format_incorrect));
                break;

       
            case "ERROR_INVALID_CREDENTIAL":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.supploed_auth_credntial_malformed));

                break;

            case "ERROR_INVALID_EMAIL":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.email_address_is_badly));
                break;

            case "ERROR_WRONG_PASSWORD":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.password_is_invalid));

                break;

            case "ERROR_USER_MISMATCH":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.previously_singed_in_user));

                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.login_again_before_retrying));
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.account_already_exist));
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.email_already_in_use_another_account));
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.credential_already_associated));

                break;

            case "ERROR_USER_DISABLED":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.account_has_been_disabled));

                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.credential_is_no_longer_valid));
                break;

            case "ERROR_USER_NOT_FOUND":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.no_record_corresponding));
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.credential_is_no_longer_valid));

                break;

           
            case "ERROR_WEAK_PASSWORD":
                Services.showDialog(context,context.getString(R.string.error),context.getString(R.string.given_passoword_invalid));
                break;

        }

    }

    public static void loadLocale(Context context){
        String code = getLocateCode(context);
        setLocale(context,code);
    }





    public static String getLocateCode(Context context) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("lang", MODE_PRIVATE);
            return sharedPreferences.getString("mylang", "pt");
        }

        return "pt";
    }

    private static  void setLocale(Context context,String code){
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration,context.getResources().getDisplayMetrics());

    }

    public static  String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            return json;
        } catch (IOException e) {
            return null;
        }
    }
    public static boolean isPromoting(Date date){
        Date currentDate = new Date();
        if (currentDate.compareTo(date) < 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public static  Date getServerDate() throws Exception {
        String url = "https://time.is/Unix_time_now";
        Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
        String[] tags = new String[] {
                "div[id=time_section]",
                "div[id=clock0_bg]"
        };
        Elements elements= doc.select(tags[0]);
        for (int i = 0; i <tags.length; i++) {
            elements = elements.select(tags[i]);
        }
        return convertTimeToDate(Long.parseLong(elements.text()));
    }

    public static Date convertTimeToDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        return cal.getTime();
    }

//    public static boolean isSellerMode(Context context){
//        SharedPreferences sharedPreferences = context.getSharedPreferences("ecdeRoom",MODE_PRIVATE);
//        return sharedPreferences.getBoolean("isSellerMode",false);
//    }
//    public static void setSellerMode(Context context,boolean isSeller) {
//        context.getSharedPreferences("ecdeRoom",MODE_PRIVATE).edit().putBoolean("isSellerMode",isSeller).apply();
//    }

    public static void sentPushNotification(Context context,String title, String message, String token) {
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + "AAAAVSW-r_Y:APA91bEgQtUiZbIhBHk8Egu3iX688vG945Jh0df_EbR19S01eUC8En22xEUWacVTDJz07s9be2Detoq-Xrd0yYYYducv4JfyCpaC-ATPxeeJMWbmGkT-UrRPZ2x1F9hlmQOo_qtU45BP";
        final String contentType = "application/json";
        String NOTIFICATION_TITLE;
        String NOTIFICATION_MESSAGE;

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", title);
            notifcationBody.put("message", message);
            notification.put("to", token);
            notification.put("data", notifcationBody);
        } catch (JSONException ignored) {

        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

    public static  String convertDateToString(Date date) {
        if (date == null) {
            date = new Date();
        }
        date.setTime(date.getTime());
        String pattern = "dd-MMM-yyyy";
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        return  df.format(date);
    }

    public static  String convertDateToTimeString(Date date) {
        if (date == null) {
            date = new Date();
        }
        date.setTime(date.getTime());
        String pattern = "dd-MMM-yyyy, hh:mm a";
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        return  df.format(date);
    }

    public static void showCenterToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();
    }

    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }


    public static String toUpperCase(String str) {
        if (str.isEmpty()){
            return "";
        }
        String[] names = str.trim().split(" ");
        str = "";
        for (String name : names) {
            try {
                str += name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() + " ";
            }
            catch (Exception ignored){

            }
        }
     return str;
    }
    public static void showDialog(Context context,String title,String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        Activity activity = (Activity) context;
        View view = activity.getLayoutInflater().inflate(R.layout.error_message_layout, null);
        TextView titleView = view.findViewById(R.id.title);
        TextView msg = view.findViewById(R.id.message);
        titleView.setText(title);
        msg.setText(message);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if (title.equalsIgnoreCase(context.getString(R.string.verify_your_email))) {
                    if (context instanceof SignUpActivity) {
                        ((SignUpActivity) context).finish();
                    }

                }
                else if (title.equalsIgnoreCase(context.getString(R.string.updated))) {
                    ((Activity) context).finish();
                }
            }
        });

        if(!((Activity) context).isFinishing())
        {
            alertDialog.show();

        }

    }

    public static void sentEmailVerificationLink(Context context){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            ProgressHud.show(context,"");
            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    ProgressHud.dialog.dismiss();

                    if (task.isSuccessful()) {
                        showDialog(context,context.getString(R.string.verify_your_email),context.getString(R.string.we_have_sent_verification_link));
                    }
                    else {
                        showDialog(context,context.getString(R.string.error),task.getException().getLocalizedMessage());
                    }
                }
            });
        }
        else {
            ProgressHud.dialog.dismiss();
        }
    }



    public static void addUserDataOnServer(Context context,String uid, String type,Map user){


        ProgressHud.show(context,"");
        FirebaseFirestore.getInstance().collection("Users").document(uid).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ProgressHud.dialog.dismiss();
                if (task.isSuccessful()) {
                    if (type.equalsIgnoreCase("password")) {
                        sentEmailVerificationLink(context);
                    }
                    else {
                        Services.getCurrentUserData(context,FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                    }
                }
                else {
                    Services.showDialog(context,"Error",task.getException().getLocalizedMessage());
                }
            }
        });
    }

    public static void getCurrentUserData(Context context,String uid, Boolean showProgress) {

        if (showProgress) {
            ProgressHud.show(context,"");
        }

        FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (showProgress) {
                    ProgressHud.dialog.dismiss();
                }

                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        documentSnapshot.toObject(UserModel.class);

                        if (UserModel.data != null) {
                                Intent intent = null;
                                intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);

                        }
                        else  {
                            Services.logout(context);
                           showCenterToast(context,"Something Went Wrong. Code - 101");
                        }
                    }
                    else {

                        Services.logout(context);
                        showCenterToast(context,"Something Went Wrong. Code - 103");
                    }
                }
                else {
                    showDialog(context,"Error",task.getException().getLocalizedMessage());
                }

            }
        });
    }




}
