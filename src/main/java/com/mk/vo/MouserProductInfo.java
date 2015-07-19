package com.mk.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by margish on 6/1/15.
 */
public class MouserProductInfo {
    private String mouserPartNumber;
    private String mfrPartNumber;
    private String mfr;
    private String desc;
    private Integer qntyAvailable;
    private List<PriceBreaker> priceBreakers;
    private Integer userQty;
    private PriceBreaker bestPrice;
    private String errorMessage;
    private String partLink;
    private String pkging = null;
    private String pkg = null;
    private String family = null;
    private String rohs = null;

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
        if (priceBreakers == null || priceBreakers.isEmpty())
            return null;
        PriceBreaker minPriceBreaker = priceBreakers.get(0);
        for (PriceBreaker priceBreaker:priceBreakers){
            if (qty < priceBreaker.getQty()) {
                break;
            }
            lastPriceBreaker = priceBreaker;
        }
        PriceBreaker result;
        if (lastPriceBreaker != null) {
            result = new PriceBreaker(qty, lastPriceBreaker.getUnitPrice(), null);
        } else {
            result = new PriceBreaker(minPriceBreaker.getQty(), minPriceBreaker.getUnitPrice(), null);
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
            if (priceBreaker.getPkging().equals(PriceBreaker.FULL_REEL)) {
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
        PriceBreaker previousPrice = null;
        if (minReelQty == null) {
            for (PriceBreaker cutTapePrice:priceBreakers) {
                previousPrice = cutTapePrice;
                if (remaining >= cutTapePrice.getQty()) {
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
                return finalPrice;
            } else {
                if (bestCutPrice == null) {
                    return previousPrice;
                }
                if (nextPrice.getExtendedPrice() < (bestCutPrice.getUnitPrice() * remaining)) {
                    return nextPrice;
                }
            }
            PriceBreaker returnBreaker = bestCutPrice;
            returnBreaker.setQty((returnBreaker.getQty() > totalQty)?returnBreaker.getQty():totalQty);
            return returnBreaker;
        }

        while (remaining >= minReelQty) {
            for (PriceBreaker reelPriceBreaker:descendingOrderPriceBreaker) {
                if (reelPriceBreaker.getPkging().equals(PriceBreaker.FULL_REEL)) {
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

        if (remaining != 0) {

            for (PriceBreaker cutPriceBreaker : priceBreakers) {
                if (cutPriceBreaker.getPkging().equals(PriceBreaker.CUT_TAPE)) {
                    if (remaining >= cutPriceBreaker.getQty()) {
                        bestCutPrice = cutPriceBreaker;
                    }
                }
            }

            //look for better cut tape price
            PriceBreaker betterCutPrice = bestCutPrice;
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

            if (betterCutPrice != null)
                bestCutPrice = betterCutPrice;
        }

        // no cut tape price, only reel prices
        if (bestCutPrice == null && !priceBreakers.isEmpty()) {
            for (PriceBreaker priceBreaker:priceBreakers) {
                if (bestCutPrice == null || totalQty > priceBreaker.getQty()) {
                    bestCutPrice = priceBreaker;
                } else {
                    break;
                }
            }
            return bestCutPrice;

        }
        remaining = (remaining < bestCutPrice.getQty()) ? bestCutPrice.getQty() : remaining;

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
            pkg += "/" + PriceBreaker.FULL_REEL;
        } else {
            if (cutTapeTotalPrice > 0)
                pkg += PriceBreaker.CUT_TAPE;

            if (reelTotalQty > 0)
                pkg += PriceBreaker.FULL_REEL;
        }

        if (pkg.equals("")) {
            if (bestCutPrice != null) {
                pkg = bestCutPrice.getPkging();
            } else if (bestReelPrice != null) {
                pkg = bestReelPrice.getPkging();
            }
        }

        String errorMsg = null;
        if (bestReelPrice != null && bestReelPrice.getErrorMessage() != null) {
            errorMsg = bestReelPrice.getErrorMessage();
        }

        PriceBreaker betterPrice = new PriceBreaker(finalQty, finalUnitPrice, finalUnitPrice * finalQty, pkg, errorMsg);

        //check for better price
        for (PriceBreaker priceBreaker:this.priceBreakers) {
            if (priceBreaker.getQty() > finalQty
                    && (priceBreaker.getUnitPrice() * priceBreaker.getQty()) < (finalUnitPrice * finalQty)
                    && (betterPrice.getUnitPrice() * betterPrice.getQty()) < (finalUnitPrice * finalQty)) {
                betterPrice = priceBreaker;
            }
        }

        return betterPrice;
    }

    private PriceBreaker searchForBetterExtendedPrice(PriceBreaker result) {
        for (PriceBreaker priceBreaker:priceBreakers) {
            if (priceBreaker.getQty() != null) {
                if (result.getQty() < priceBreaker.getQty()
                        &&  (result.getExtendedPrice() > priceBreaker.getExtendedPrice())
                        && result.getUnitPrice() != 0) {
                    return priceBreaker;
                }
            }
        }
        return result;
    }


    public MouserProductInfo() {
    }

    public MouserProductInfo(String mouserPartNumber, String mfrPartNumber, String mfr, String desc, Integer qntyAvailable, String partLink) {
        this.mouserPartNumber = mouserPartNumber;
        this.mfrPartNumber = mfrPartNumber;
        this.mfr = mfr;
        this.desc = desc;
        this.qntyAvailable = qntyAvailable;
        this.partLink = partLink;
    }

    public Integer getQntyAvailable() {
        return qntyAvailable;
    }

    public void setQntyAvailable(Integer qntyAvailable) {
        this.qntyAvailable = qntyAvailable;
    }

    public String getMouserPartNumber() {
        return mouserPartNumber;
    }

    public void setMouserPartNumber(String mouserPartNumber) {
        this.mouserPartNumber = mouserPartNumber;
    }

    public String getMfrPartNumber() {
        return mfrPartNumber;
    }

    public void setMfrPartNumber(String mfrPartNumber) {
        this.mfrPartNumber = mfrPartNumber;
    }

    public String getMfr() {
        return mfr;
    }

    public void setMfr(String mfr) {
        this.mfr = mfr;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<PriceBreaker> getPriceBreakers() {
        return priceBreakers;
    }

    public void setPriceBreakers(List<PriceBreaker> priceBreakers) {
        this.priceBreakers = priceBreakers;
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

    public String getPkging() {
        return pkging;
    }

    public void setPkging(String pkging) {
        this.pkging = pkging;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getRohs() {
        return rohs;
    }

    public void setRohs(String rohs) {
        this.rohs = rohs;
    }

    public MouserProductInfo(String errorMessage, String partLink, String mfrPartNumber) {
        this.errorMessage = errorMessage;
        this.partLink = partLink;
        this.mfrPartNumber = mfrPartNumber;
    }

    @Override
    public String toString() {
        return "MouserProductInfo{" +
                "mouserPartNumber='" + mouserPartNumber + '\'' +
                ", mfrPartNumber='" + mfrPartNumber + '\'' +
                ", mfr='" + mfr + '\'' +
                ", desc='" + desc + '\'' +
                ", qntyAvailable=" + qntyAvailable +
                ", priceBreakers=" + priceBreakers +
                '}';
    }
}
