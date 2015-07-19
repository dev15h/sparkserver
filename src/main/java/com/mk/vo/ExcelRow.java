package com.mk.vo;

/**
 * Created by margish on 6/14/15.
 */
public class ExcelRow {
    private String reference = "";
    private String schemRef = "";
    private String mfrPartNum = "";
    private String qty;
    private String lotQty;
    private String totalQty;
    private String digiPrice;
    private String digiMinimumQty;
    private String mouserPrice;
    private String mouserMinimumQty;
    private String totalPrice;
    private String digiStock;
    private String mouserStock;
    private String digiPN = "";
    private String mouserPN = "";
    private String desc = "";
    private String mfr = "";
    private String pckging = "";
    private String pckg = "";
    private String category = "";
    private String family = "";
    private String rohs = "";
    private boolean mouserNotFound;
    private boolean digiNotFound;
    private String mouserLink;
    private String digikeyLink;
    private String bestPrice;
    private String mouserPckging;
    private String digiPckging;

    public static final String MOUSER = "MOUSER";

    public static final String DIGIKEY = "DIGIKEY";

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSchemRef() {
        return schemRef;
    }

    public void setSchemRef(String schemRef) {
        this.schemRef = schemRef;
    }

    public String getMfrPartNum() {
        return mfrPartNum;
    }

    public void setMfrPartNum(String mfrPartNum) {
        this.mfrPartNum = mfrPartNum;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getLotQty() {
        return lotQty;
    }

    public void setLotQty(String lotQty) {
        this.lotQty = lotQty;
    }

    public String getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(String totalQty) {
        this.totalQty = totalQty;
    }

    public String getDigiPrice() {
        return digiPrice;
    }

    public void setDigiPrice(String digiPrice) {
        this.digiPrice = digiPrice;
    }

    public String getMouserPrice() {
        return mouserPrice;
    }

    public void setMouserPrice(String mouserPrice) {
        this.mouserPrice = mouserPrice;
    }

    public Double getTotalPrice() {
        Double mouserTotal;
        Double digiTotal;

        boolean noDigiPrice = false;
        boolean noMouserPrice = false;
        try {
            if (digiPrice == null)
                noDigiPrice = true;
            else
                Double.parseDouble(digiPrice);
        } catch (NumberFormatException nfe) {
            noDigiPrice = true;
        }
        try {
            if (mouserPrice == null)
                noMouserPrice = true;
            else
                Double.parseDouble(mouserPrice);
        } catch (NumberFormatException nfe) {
            noMouserPrice = true;
        }

        try {
            if (mouserMinimumQty == null || noMouserPrice)
                mouserTotal = 0.0;
            else
                mouserTotal = Double.parseDouble(mouserPrice) * Integer.parseInt(mouserMinimumQty);

            if (digiMinimumQty == null || noDigiPrice)
                digiTotal = 0.0;
            else
                digiTotal = Double.parseDouble(digiPrice) * Integer.parseInt(digiMinimumQty);
        } catch (Exception e) {
            return 0.0;
        }

        if (mouserTotal == 0.0) return digiTotal;
        if (digiTotal == 0.0) return mouserTotal;
        return (digiTotal < mouserTotal)?digiTotal:mouserTotal;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDigiStock() {
        return digiStock;
    }

    public void setDigiStock(String digiStock) {
        this.digiStock = digiStock;
    }

    public String getMouserStock() {
        return mouserStock;
    }

    public void setMouserStock(String mouserStock) {
        this.mouserStock = mouserStock;
    }

    public String getDigiPN() {
        return digiPN;
    }

    public void setDigiPN(String digiPN) {
        this.digiPN = digiPN;
    }

    public String getMouserPN() {
        return mouserPN;
    }

    public void setMouserPN(String mouserPN) {
        this.mouserPN = mouserPN;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMfr() {
        return mfr;
    }

    public void setMfr(String mfr) {
        this.mfr = mfr;
    }

    public String getPckging() {
        Double mouserTotal;
        Double digiTotal;

        boolean noDigiPrice = false;
        boolean noMouserPrice = false;
        try {
            if (digiPrice == null)
                noDigiPrice = true;
            else
                Double.parseDouble(digiPrice);
        } catch (NumberFormatException nfe) {
            noDigiPrice = true;
        }
        try {
            if (mouserPrice == null)
                noMouserPrice = true;
            else
                Double.parseDouble(mouserPrice);
        } catch (NumberFormatException nfe) {
            noMouserPrice = true;
        }

        try {
            if (mouserMinimumQty == null || noMouserPrice)
                mouserTotal = 0.0;
            else
                mouserTotal = Double.parseDouble(mouserPrice) * Integer.parseInt(mouserMinimumQty);

            if (digiMinimumQty == null || noDigiPrice)
                digiTotal = 0.0;
            else
                digiTotal = Double.parseDouble(digiPrice) * Integer.parseInt(digiMinimumQty);
        } catch (Exception e) {
            return "";
        }

        if (mouserTotal == 0.0) return digiPckging;
        if (digiTotal == 0.0) return mouserPckging;
        return (digiTotal <= mouserTotal)?digiPckging:mouserPckging;
    }

    public void setPckging(String pckging) {
        this.pckging = pckging;
    }

    public String getPckg() {
        return pckg;
    }

    public void setPckg(String pckg) {
        this.pckg = pckg;
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

    public String getRohs() {
        return rohs;
    }

    public void setRohs(String rohs) {
        this.rohs = rohs;
    }

    public String getDigiMinimumQty() {
        return digiMinimumQty;
    }

    public void setDigiMinimumQty(String digiMinimumQty) {
        this.digiMinimumQty = digiMinimumQty;
    }

    public String getMouserMinimumQty() {
        return mouserMinimumQty;
    }

    public void setMouserMinimumQty(String mouserMinimumQty) {
        this.mouserMinimumQty = mouserMinimumQty;
    }

    public boolean isMouserNotFound() {
        return mouserNotFound;
    }

    public void setMouserNotFound(boolean mouserNotFound) {
        this.mouserNotFound = mouserNotFound;
    }

    public boolean isDigiNotFound() {
        return digiNotFound;
    }

    public void setDigiNotFound(boolean digiNotFound) {
        this.digiNotFound = digiNotFound;
    }

    public String getMouserLink() {
        return mouserLink;
    }

    public void setMouserLink(String mouserLink) {
        this.mouserLink = mouserLink;
    }

    public String getDigikeyLink() {
        return digikeyLink;
    }

    public void setDigikeyLink(String digikeyLink) {
        this.digikeyLink = digikeyLink;
    }

    public String getMouserPckging() {
        return mouserPckging;
    }

    public void setMouserPckging(String mouserPckging) {
        this.mouserPckging = mouserPckging;
    }

    public String getDigiPckging() {
        return digiPckging;
    }

    public void setDigiPckging(String digiPckging) {
        this.digiPckging = digiPckging;
    }

    public String getBestPrice() {
        Double mouserTotal;
        Double digiTotal;

        boolean noDigiPrice = false;
        boolean noMouserPrice = false;
        try {
            if (digiPrice == null)
                noDigiPrice = true;
            else
                Double.parseDouble(digiPrice);
        } catch (NumberFormatException nfe) {
            noDigiPrice = true;
        }
        try {
            if (mouserPrice == null)
                noMouserPrice = true;
            else
                Double.parseDouble(mouserPrice);
        } catch (NumberFormatException nfe) {
            noMouserPrice = true;
        }

        try {
            if (mouserMinimumQty == null || noMouserPrice)
                mouserTotal = 0.0;
            else
                mouserTotal = Double.parseDouble(mouserPrice) * Integer.parseInt(mouserMinimumQty);

            if (digiMinimumQty == null || noDigiPrice)
                digiTotal = 0.0;
            else
                digiTotal = Double.parseDouble(digiPrice) * Integer.parseInt(digiMinimumQty);
        } catch (Exception e) {
            return "";
        }

        if (mouserTotal == 0.0) return DIGIKEY;
        if (digiTotal == 0.0) return MOUSER;
        return (digiTotal <= mouserTotal)?DIGIKEY:MOUSER;
    }

    public void setBestPrice(String bestPrice) {
        this.bestPrice = bestPrice;
    }
}
