package com.mk.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by margish on 6/1/15.
 */
public class DigikeyProductInfo {
    private String partNum;
    private Integer qtyAvail;
    private String mfr;
    private String mfrPartNumber;
    private String desc;
    private String rohs;
    private String category;
    private String family;
    private String packaging;
    private String pkgCase;
    private List<PriceBreaker> priceBreakers;
    private PriceBreaker bestPrice;
    private Integer userQty;
    private String errorMessage;
    private String partLink;

    public List<PriceBreaker> getPriceBreakers() {
        return priceBreakers;
    }

    public void setPriceBreakers(List<PriceBreaker> priceBreakers) {
        this.priceBreakers = priceBreakers;
    }



    public DigikeyProductInfo() {
    }

    public DigikeyProductInfo(String partNum, Integer qtyAvail, String mfr, String mfrPartNumber, String desc, String rohs, String category, String family, String packaging, String pkgCase,
                              String partLink) {
        this.partNum = partNum;
        this.qtyAvail = qtyAvail;
        this.mfr = mfr;
        this.mfrPartNumber = mfrPartNumber;
        this.desc = desc;
        this.rohs = rohs;
        this.category = category;
        this.family = family;
        this.packaging = packaging;
        this.pkgCase = pkgCase;
        this.partLink = partLink;
    }

    public String getPartNum() {
        return partNum;
    }

    public void setPartNum(String partNum) {
        this.partNum = partNum;
    }

    public Integer getQtyAvail() {
        return qtyAvail;
    }

    public void setQtyAvail(Integer qtyAvail) {
        this.qtyAvail = qtyAvail;
    }

    public String getMfr() {
        return mfr;
    }

    public void setMfr(String mfr) {
        this.mfr = mfr;
    }

    public String getMfrPartNumber() {
        return mfrPartNumber;
    }

    public void setMfrPartNumber(String mfrPartNumber) {
        this.mfrPartNumber = mfrPartNumber;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRohs() {
        return rohs;
    }

    public void setRohs(String rohs) {
        this.rohs = rohs;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getPkgCase() {
        return pkgCase;
    }

    public void setPkgCase(String pkgCase) {
        this.pkgCase = pkgCase;
    }

    public Integer getUserQty() {
        return userQty;
    }

    public void setUserQty(Integer userQty) {
        this.userQty = userQty;
    }

    public PriceBreaker getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(PriceBreaker bestPrice) {
        this.bestPrice = bestPrice;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPartLink() {
        return partLink;
    }

    public void setPartLink(String partLink) {
        this.partLink = partLink;
    }

    public DigikeyProductInfo(String errorMessage, String partLink, String mfrPartNumber) {
        this.errorMessage = errorMessage;
        this.partLink = partLink;
        this.mfrPartNumber = mfrPartNumber;
    }

    @Override
    public String toString() {
        return "DigikeyProductInfo{" +
                "partNum='" + partNum + '\'' +
                ", qtyAvail=" + qtyAvail +
                ", mfr='" + mfr + '\'' +
                ", mfrPartNumber='" + mfrPartNumber + '\'' +
                ", desc='" + desc + '\'' +
                ", rohs='" + rohs + '\'' +
                ", category='" + category + '\'' +
                ", family='" + family + '\'' +
                ", packaging='" + packaging + '\'' +
                ", pkgCase='" + pkgCase + '\'' +
                ", priceBreakers=" + priceBreakers +
                '}';
    }

    public PriceBreaker getQtyPrice(Integer lotQty){
        if (this.priceBreakers.isEmpty()) {
            return null;
        }
        try {
            if (true)
                return getNewPrice(lotQty);
        } catch (Exception e) {
            System.out.println("Error in price breaker for part num " + mfrPartNumber);
            e.printStackTrace();
        }
        Double price = 0.0;
        Integer qty = lotQty * userQty;
        PriceBreaker lastPriceBreaker = null;
        PriceBreaker minPriceBreaker = null;
        for (PriceBreaker priceBreaker:priceBreakers){
            if (minPriceBreaker == null)
                minPriceBreaker = priceBreaker;
            if (priceBreaker.getQty() != null) {
                if (qty < priceBreaker.getQty()) {
                    break;
                }
            }
            lastPriceBreaker = priceBreaker;
        }
        PriceBreaker result;
        if (lastPriceBreaker != null && lastPriceBreaker.getQty() != null) {
            result = new PriceBreaker(qty, lastPriceBreaker.getUnitPrice(), null, lastPriceBreaker.getPkging(), minPriceBreaker.getErrorMessage());
        } else {
            if (minPriceBreaker != null)
                result = new PriceBreaker(minPriceBreaker.getQty(), minPriceBreaker.getUnitPrice(), null, minPriceBreaker.getPkging(), minPriceBreaker.getErrorMessage());
            else
                return new PriceBreaker(0, 0.0, null, packaging, errorMessage);
        }
        result = searchForBetterExtendedPrice(result);
        return result;
    }

    public PriceBreaker getNewPrice(Integer qty) {
        Collections.sort(priceBreakers);
        List<PriceBreaker> descendingOrderPriceBreaker = new ArrayList<PriceBreaker>();
        Integer minReelQty = null;

        for (int i = priceBreakers.size() - 1; i >= 0 ; i--) {
            PriceBreaker priceBreaker = priceBreakers.get(i);
            if (priceBreaker.getPkging().equals(PriceBreaker.TAPE_AND_REEL)) {
                if (minReelQty == null || minReelQty > priceBreaker.getQty())
                    minReelQty = priceBreaker.getQty();
            }
            descendingOrderPriceBreaker.add(priceBreaker);
        }

        Integer totalQty = qty * this.userQty;
        Integer remaining = totalQty;

        List<PriceBreaker> reelPrices = new ArrayList<PriceBreaker>();
        PriceBreaker bestReelPrice = null;

        //only cut tape, no reel
        PriceBreaker bestCutPrice = null;
        PriceBreaker nextPrice = null;

        //if only one price breaker
        if (priceBreakers.size() == 1) {
            PriceBreaker breaker = priceBreakers.get(0);
            if (breaker.getQty() != null) {
                if (breaker.getQty() > remaining)
                    return breaker;
                else {
                    breaker.setQty(remaining);
                    return breaker;
                }
            }
        }
        if (minReelQty == null) {
            for (PriceBreaker cutTapePrice:priceBreakers) {
                if (priceBreakers.size() == 1 && (remaining == null || cutTapePrice == null || cutTapePrice.getQty() == null)) {
                    return priceBreakers.get(0);
                }
                if (cutTapePrice == null || cutTapePrice.getQty() == null) {
                    System.out.println("***null");
                }
                if (cutTapePrice.getQty() == null) {
                    System.out.println(partNum + " has price breaker with null qty");
                } else if (remaining >= cutTapePrice.getQty()) {
                    bestCutPrice = cutTapePrice;
                } else {
                    nextPrice = cutTapePrice;
                    break;
                }
            }
            PriceBreaker finalPrice;
            if (nextPrice == null) {
                finalPrice = bestCutPrice;
                finalPrice.setQty(remaining);
                return bestCutPrice;
            } else {
                if (nextPrice.getExtendedPrice() < (bestCutPrice.getUnitPrice() * remaining)) {
                    return nextPrice;
                }
            }
            return bestCutPrice;
        }

        while (remaining >= minReelQty) {
            for (PriceBreaker reelPriceBreaker:descendingOrderPriceBreaker) {
                if (reelPriceBreaker.getPkging().equals(PriceBreaker.TAPE_AND_REEL)) {
                    if (remaining >= reelPriceBreaker.getQty()) {
                        bestReelPrice = reelPriceBreaker;
                        reelPrices.add(reelPriceBreaker);
                        break;
                    }
                }
            }
            if (bestReelPrice != null)
                remaining = remaining - bestReelPrice.getQty();
        }


        for (PriceBreaker cutPriceBreaker:priceBreakers) {
            if (remaining == null || cutPriceBreaker == null || cutPriceBreaker.getQty() == null) {
                return cutPriceBreaker;
            }
            if (cutPriceBreaker.getPkging().equals(PriceBreaker.CUT_TAPE)) {
                if (cutPriceBreaker.getQty() == null && remaining >= cutPriceBreaker.getQty()) {
                    bestCutPrice = cutPriceBreaker;
                }
            }
        }

        //look for better cut tape price
        PriceBreaker betterCutPrice = bestCutPrice;
        if (bestCutPrice != null) {
            for (PriceBreaker cutPriceBreaker : priceBreakers) {
                if (cutPriceBreaker.getPkging().equals(PriceBreaker.CUT_TAPE)) {
                    Integer temp = (betterCutPrice.getQty() > remaining) ? betterCutPrice.getQty() : remaining;
                    if (remaining < cutPriceBreaker.getQty()
                            && cutPriceBreaker.getExtendedPrice() < temp * bestCutPrice.getUnitPrice()
                            && cutPriceBreaker.getExtendedPrice() < temp * betterCutPrice.getUnitPrice()) {
                        betterCutPrice = cutPriceBreaker;
                    }
                }
            }
        }

        if (bestCutPrice != null) {
            remaining = (remaining < bestCutPrice.getQty()) ? bestCutPrice.getQty() : remaining;
        }

        Integer reelTotalQty = 0;
        Double reelTotalPrice = 0.0;
        for (PriceBreaker reelPriceBreaker:reelPrices) {
            reelTotalQty += reelPriceBreaker.getQty();
            reelTotalPrice += reelPriceBreaker.getUnitPrice() * reelPriceBreaker.getQty();
        }

        Double cutTapeTotalPrice = 0.0;
        if (remaining != 0 && bestCutPrice != null) {
            cutTapeTotalPrice = remaining * bestCutPrice.getUnitPrice();
        }

        Integer finalQty = remaining + reelTotalQty;
        Double finalUnitPrice = (reelTotalPrice + cutTapeTotalPrice)/finalQty;

        String pkg = "";
        if (cutTapeTotalPrice > 0 && reelTotalQty > 0) {
            pkg += PriceBreaker.CUT_TAPE;
            pkg += "/" + PriceBreaker.TAPE_AND_REEL;
        } else {
            if (cutTapeTotalPrice > 0)
                pkg += PriceBreaker.CUT_TAPE;

            if (reelTotalQty > 0)
                pkg += PriceBreaker.TAPE_AND_REEL;
        }
        if (pkg.equals("")) {
            if (bestCutPrice != null) {
                pkg = bestCutPrice.getPkging();
            } else if (bestReelPrice != null) {
                pkg = bestReelPrice.getPkging();
            }
        }

        PriceBreaker betterPrice = new PriceBreaker(finalQty, finalUnitPrice, finalUnitPrice * finalQty, pkg, null);

        //check for better price
        for (PriceBreaker priceBreaker:this.priceBreakers) {
            if (priceBreaker.getQty() > finalQty && (priceBreaker.getUnitPrice() * priceBreaker.getQty()) < (finalUnitPrice * finalQty)) {
                betterPrice = priceBreaker;
            }
        }

        return betterPrice;
    }


    private PriceBreaker searchForBetterExtendedPrice(PriceBreaker result) {
        for (PriceBreaker priceBreaker:priceBreakers) {
            if (priceBreaker.getQty() != null) {
                if (result.getQty() < priceBreaker.getQty() &&  (result.getExtendedPrice() > priceBreaker.getExtendedPrice())) {
                    return priceBreaker;
                }
            }
        }
        return result;
    }
}
