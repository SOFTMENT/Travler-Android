package in.softment.travler.Model;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class UserModel {
    public Date getExiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -1);
        Log.d("HELLOVIJAYSOFTMENT",calendar.getTime().toString());
        return calendar.getTime();

    }
    public String name = "";
    public String email = "";
    public String uid = "";
    public String profilePic = "";
    public Date expireDate = getExiryDate();
    public String subscription_id = "";
    public String subscription_status = "";
    public boolean isDiscAccepted = false;

    public static UserModel data  = new UserModel();

    public UserModel() {
        data = this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getSubscription_id() {
        return subscription_id;
    }

    public void setSubscription_id(String subscription_id) {
        this.subscription_id = subscription_id;
    }

    public boolean isDiscAccepted() {
        return isDiscAccepted;
    }

    public void setDiscAccepted(boolean discAccepted) {
        isDiscAccepted = discAccepted;
    }
}
