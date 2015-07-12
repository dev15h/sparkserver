package com.mk.vo;

/**
 * Created by margish on 5/31/15.
 */
public class PriceBreaker implements Comparable{

    public static final String CUT_TAPE = "Cut Tape";
    public static final String FULL_REEL = "Full Reels";
    public static final String TAPE_AND_REEL = "Tape & Reels";
    public static final String TUBE = "Tube";
    public static final String BULK = "Bulk";

    private Integer qty = 0;
    private Double unitPrice = 0.0;
    private Double extendedPrice;
    private String pkging;
    private String errorMessage;
    private String vendor;

    public PriceBreaker(Integer qty, Double unitPrice, Double extendedPrice) {
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.extendedPrice = extendedPrice;
    }

    public PriceBreaker(Integer qty, Double unitPrice, Double extendedPrice, String pkging, String errorMessage) {
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.extendedPrice = extendedPrice;
        this.pkging = pkging;
        this.errorMessage = errorMessage;
    }



    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Double getExtendedPrice() {
        if (unitPrice == null)
            unitPrice = 0.0;
        if (qty == null)
            qty = 0;
        if (extendedPrice == null)
        extendedPrice = unitPrice * qty;
        return extendedPrice;
    }

    public void setExtendedPrice(Double extendedPrice) {
        this.extendedPrice = extendedPrice;
    }

    @Override
    public String toString() {
        if (unitPrice == null || qty == null)
            return "Price breaker has null values";
        extendedPrice = extendedPrice!=null?extendedPrice:(qty*unitPrice);
        return "PriceBreaker{" +
                "qty=" + qty +
                ", unitPrice=" + unitPrice +
                ", extendedPrice=" + extendedPrice +
                ", pkging='" + pkging + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", vendor='" + vendor + '\'' +
                '}';
    }

    public int compareTo(Object o) {
        if (this.qty == null) return 1;
        if (((PriceBreaker)o).qty == null) return -1;
        if(this.qty > ((PriceBreaker)o).qty) return 1;
        else return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PriceBreaker that = (PriceBreaker) o;

        if (qty != null ? !qty.equals(that.qty) : that.qty != null) return false;
        if (unitPrice != null ? !unitPrice.equals(that.unitPrice) : that.unitPrice != null) return false;
        return !(extendedPrice != null ? !extendedPrice.equals(that.extendedPrice) : that.extendedPrice != null);

    }

    @Override
    public int hashCode() {
        int result = qty != null ? qty.hashCode() : 0;
        result = 31 * result + (unitPrice != null ? unitPrice.hashCode() : 0);
        result = 31 * result + (extendedPrice != null ? extendedPrice.hashCode() : 0);
        return result;
    }

    public String getPkging() {
        return pkging;
    }

    public void setPkging(String pkging) {
        this.pkging = pkging;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
