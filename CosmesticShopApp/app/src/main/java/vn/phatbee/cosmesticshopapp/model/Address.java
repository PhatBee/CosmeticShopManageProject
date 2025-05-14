package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;

public class Address implements Serializable {
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String province;
    private String district;
    private String ward;
    private boolean defaultAddress;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }
    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean isDefault) {
        this.defaultAddress = isDefault;
    }
}
