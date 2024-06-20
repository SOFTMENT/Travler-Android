package in.softment.travler.Model;

public class Category {

    public String title = "";
    public String image = "";
    public String id = "";
    public String desc = "";
    public int totalVideos = 0;
    public String type = "";
    public boolean isCatFree = false;
    public boolean isPremium = false;
    public  Category(){

    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public boolean isCatFree() {
        return isCatFree;
    }

    public void setCatFree(boolean catFree) {
        isCatFree = catFree;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(int totalVideos) {
        this.totalVideos = totalVideos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
