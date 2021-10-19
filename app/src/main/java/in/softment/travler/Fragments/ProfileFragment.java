package in.softment.travler.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import in.softment.travler.BuildConfig;
import in.softment.travler.MainActivity;
import in.softment.travler.Model.UserModel;
import in.softment.travler.R;
import in.softment.travler.Utils.Services;


public class ProfileFragment extends Fragment {

    private Context context;
    public ProfileFragment(Context context) {
        this.context = context;
    }

    public ProfileFragment(){

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        view.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
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
            }
        });

        //ProfilePicture
        CircleImageView profilePic = view.findViewById(R.id.profilePic);
        if (!UserModel.data.profilePic.isEmpty()) {
            Glide.with(context).load(UserModel.data.profilePic).placeholder(R.drawable.logo).into(profilePic);
        }

        //Name
        TextView name = view.findViewById(R.id.name);
        name.setText(UserModel.data.name);

        //Email
        TextView email = view.findViewById(R.id.emailAddress);
        email.setText(UserModel.data.email);

        //NotificationCentre
        view.findViewById(R.id.notificationCentre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)context).changeBottomBarPosition(0);
            }
        });

        //HelpCentre
        view.findViewById(R.id.helpCentre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"travelr.program@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "App feedback");
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Services.showCenterToast(context,"There are no email client installed on your device.");
                }
            }
        });

        //RateApp
        view.findViewById(R.id.rateApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(myAppLinkToMarket);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show();
                }
            }
        });

        //InviteFriends
        view.findViewById(R.id.inviteFriends).setOnClickListener(new View.OnClickListener() {
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
            }
        });

        //Version
        TextView version = view.findViewById(R.id.versionCode);
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = pInfo.versionName;
            version.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //PrivacyPolicy
        view.findViewById(R.id.privacyPolicy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/privacy-policy/"));
                startActivity(browserIntent);
            }
        });

        //TermsOfService
        view.findViewById(R.id.termsOfService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/terms-of-service/"));
                startActivity(browserIntent);
            }
        });

        //Developer
        view.findViewById(R.id.termsOfService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://softment.in/"));
                startActivity(browserIntent);
            }
        });
        return view;
    }
}