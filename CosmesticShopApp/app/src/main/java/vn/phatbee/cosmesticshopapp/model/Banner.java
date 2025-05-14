package vn.phatbee.cosmesticshopapp.model;

public class Banner {
    private String imageUrl;
    private String title;
    private String actionUrl;

    public Banner(String imageUrl, String title, String actionUrl) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.actionUrl = actionUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}

