package com.mk.vo;

import com.mk.utils.ConnectionUtils;
import com.mk.utils.Errors;
import com.mk.utils.MkStrintUtils;

import java.util.*;

/**
 * Created by margish on 6/14/15.
 */
public class ProductInfo {
    private String partNum;
    private String schRef;
    private Integer userQty;
    private Set<DigikeyProductInfo> digiProductSet;
    private MouserProductInfo mouserProductInfo;
    private String preferredVendor = "";

    public Double getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(Double bestPrice) {
        this.bestPrice = bestPrice;
    }

    private Double bestPrice;

    public String getPartNum() {
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public Set<DigikeyProductInfo> getDigiProductSet() {
        return digiProductSet;
    }

    public void setDigiProductSet(Set<DigikeyProductInfo> digiProductSet) {
        this.digiProductSet = digiProductSet;
    }

    public MouserProductInfo getMouserProductInfo() {
        return mouserProductInfo;
    }

    public void setMouserProductInfo(MouserProductInfo mouserProductInfo) {
        this.mouserProductInfo = mouserProductInfo;
    }

    public String getSchRef() {
        return schRef;
    }

    public Integer getUserQty() {
        return userQty;
    }

    public void setUserQty(Integer userQty) {
        this.userQty = userQty;
    }

    public void setSchRef(String schRef) {
        this.schRef = schRef;
    }

    public String getPreferredVendor() {
        return preferredVendor;
    }

    public void setPreferredVendor(String preferredVendor) {
        this.preferredVendor = preferredVendor;
    }

    public DigikeyProductInfo getAggregatedDigikeyInfo(){
        if (digiProductSet == null || digiProductSet.isEmpty()) {
            return new DigikeyProductInfo(Errors.PART_NOT_FOUND, "http://www.digikey.com/product-search/en?vendor=0&keywords="+partNum, partNum);
        }
        DigikeyProductInfo aggregatedDigikeyProductInfo = digiProductSet.iterator().next();
        List<PriceBreaker> priceBreakers = new ArrayList<PriceBreaker>();

        Set<PriceBreaker> allSet = new HashSet<PriceBreaker>();
        Set<PriceBreaker> preferedSet = new HashSet<PriceBreaker>();
        DigikeyProductInfo preferredDigikeyProductInfo = null;
        for (DigikeyProductInfo digikeyProductInfo:digiProductSet) {
            List<PriceBreaker> priceBreakerList = new ArrayList<PriceBreaker>();
            priceBreakerList.addAll(digikeyProductInfo.getPriceBreakers());

            if (preferredVendor != null && !preferredVendor.equals("")) {
                for (PriceBreaker priceBreaker : priceBreakerList) {
//                System.out.println("mfr: " + priceBreaker.getVendor() + "\nPrf: " + preferredVendor);
                    if (MkStrintUtils.removeSpecialChars(priceBreaker.getVendor()).toLowerCase().contains(MkStrintUtils.removeSpecialChars(preferredVendor).toLowerCase())) {
                        if (preferredDigikeyProductInfo == null) {
                            preferredDigikeyProductInfo = digikeyProductInfo;
                        }
                        preferedSet.addAll(digikeyProductInfo.getPriceBreakers());
                    }
                }
            }

            allSet.addAll(digikeyProductInfo.getPriceBreakers());
        }

        if (!preferedSet.isEmpty()) {
            priceBreakers.addAll(preferedSet);
        } else {
            priceBreakers.addAll(allSet);
        }

        Collections.sort(priceBreakers);

        if (preferredDigikeyProductInfo != null)
            aggregatedDigikeyProductInfo = preferredDigikeyProductInfo;

        aggregatedDigikeyProductInfo.setPriceBreakers(priceBreakers);
        return aggregatedDigikeyProductInfo;
    }
}
