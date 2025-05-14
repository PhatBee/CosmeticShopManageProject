package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;

public class ShippingAddress implements Serializable {
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String province;
    private String district;
    private String ward;

    // Constructors, getters, and setters
    public ShippingAddress() {}
    public ShippingAddress(String receiverName, String receiverPhone, String address, String province, String district, String ward) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.address = address;
        this.province = province;
        this.district = district;
        this.ward = ward;
    }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
}