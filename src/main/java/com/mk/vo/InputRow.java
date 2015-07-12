package com.mk.vo;

/**
 * Created by margish on 6/13/15.
 */
public class InputRow {
    private String mfrPartNum;
    private String schemRef;
    private Integer qty;
    private String vendor;
    private String mfrPn2;
    private String mfrPn3;

    public InputRow(String mfrPartNum, String schemRef, Integer qty, String vendor, String mfrPn2, String mfrPn3) {
        this.mfrPartNum = mfrPartNum;
        this.schemRef = schemRef;
        this.qty = qty;
        this.vendor = vendor;
        this.mfrPn2 = mfrPn2;
        this.mfrPn3 = mfrPn3;
    }

    public String getMfrPartNum() {
        return mfrPartNum;
    }

    public void setMfrPartNum(String mfrPartNum) {
        this.mfrPartNum = mfrPartNum;
    }

    public String getSchemRef() {
        return schemRef;
    }

    public void setSchemRef(String schemRef) {
        this.schemRef = schemRef;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getMfrPn2() {
        return mfrPn2;
    }

    public void setMfrPn2(String mfrPn2) {
        this.mfrPn2 = mfrPn2;
    }

    public String getMfrPn3() {
        return mfrPn3;
    }

    public void setMfrPn3(String mfrPn3) {
        this.mfrPn3 = mfrPn3;
    }
}
