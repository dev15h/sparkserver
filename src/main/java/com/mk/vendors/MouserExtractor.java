package com.mk.vendors;

import com.mk.utils.ConnectionUtils;
import com.mk.utils.Errors;
import com.mk.utils.MkStrintUtils;
import com.mk.vo.MouserProductInfo;
import com.mk.vo.PriceBreaker;
import com.sun.deploy.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Created by margish on 5/31/15.
 */
public class MouserExtractor {

    private static final String PACKAGING = "Packaging";
    private static final String PKG = "Package / Case";
    private static final String TYPE = "Type";
    private static final String ROHS = "RoHS";

    private static final String CUT_TAPE = ">Cut Tape<";
    private static final String MOUSE_REEL = ">MouseReel";
    private static final String FULL_REEL = ">Full Reels<";
    private static final String REEL_OF = "> Reel of";


    private Document document;

    public MouserProductInfo getMouseProductDetails(String partNum, String vendor, String mfrPn2, String mfrPn3) {
        String preUrl = "http://www.mouser.com/Search/Refine.aspx?Keyword=";

        String url = preUrl+partNum;
        System.out.println(url);
        document = ConnectionUtils.connectionRetry(url, 1);
        if (document == null)
            return new MouserProductInfo(Errors.PART_NOT_FOUND, url, partNum);
        Elements html = document.getElementsByTag("html");

        String partNumFound = partNum;

        if (html.get(0).childNode(2).outerHtml().indexOf("NRSearchMsg") > -1) {
            System.out.println(partNum + " Not Found on Mouser");

            String newUrl;
            // try part num 2
            boolean found = false;
            if (mfrPn2 != null) {
                newUrl = preUrl + mfrPn2;
                document = ConnectionUtils.connectionRetry(newUrl, 1);
                html = document.getElementsByTag("html");

                if (html.get(0).childNode(2).outerHtml().indexOf("NRSearchMsg") > -1) {
                    System.out.println(mfrPn2 + " Not Found on Mouser");

                    // try part num 3
                    if (mfrPn2 != null) {
                        newUrl = preUrl + mfrPn3;
                        document = ConnectionUtils.connectionRetry(newUrl, 1);
                        html = document.getElementsByTag("html");

                        if (html.get(0).childNode(2).outerHtml().indexOf("NRSearchMsg") > -1) {
                            System.out.println(mfrPn3 + " Not Found on Mouser");
                        } else {
                            //mfr pn 3 found
                            partNumFound = mfrPn3;
                            url = newUrl;
                            found = true;
                        }
                    }
                } else {
                    //mfr pn 2 found
                    partNumFound = mfrPn2;
                    url = newUrl;
                    found = true;
                }

            }
            if (!found)
                return new MouserProductInfo(Errors.PART_NOT_FOUND, url, partNum);
        }

        partNum = partNumFound;

        System.out.println(partNum + " Found on Mouser. Getting info...");

        MouserProductInfo mouserProductInfo = mouserExtraction(partNum, vendor);
        if (mouserProductInfo == null) {
            return new MouserProductInfo(Errors.PART_NOT_FOUND, url, partNum);
        }

        if (mouserProductInfo.getPriceBreakers() == null || mouserProductInfo.getPriceBreakers().isEmpty()) {
            List<PriceBreaker> priceBreakers = mouserPriceBreaker(mouserProductInfo.getPkging());
            mouserProductInfo.setPriceBreakers(priceBreakers);
        }

        return mouserProductInfo;

   }



    private List<PriceBreaker> mouserPriceBreaker(String pckging) {
        List<PriceBreaker> priceBreakerList = new ArrayList<PriceBreaker>();
        if (document == null)
            return priceBreakerList;
//        Elements priceQtyElements = document.getElementsByAttributeValue("class", "PriceBreakQuantity");
//        Elements priceBrkElements = document.getElementsByAttributeValue("class", "PriceBreakPrice");

        Elements priceBreaks;
        try {
            priceBreaks = document.getElementsByClass("PriceBreaks").get(0).getElementsByTag("tr");
        } catch (Exception e) {
            System.out.println("Prices not listed");
            return priceBreakerList;
        }

        String pkgType = "";
        List<PriceBreaker> cutTapePriceList = new ArrayList<PriceBreaker>();
        List<PriceBreaker> reelPriceList = new ArrayList<PriceBreaker>();
        for (Element priceBrkElement:priceBreaks) {
            Integer qty;
            Double price = 0.0;
            if (priceBrkElement.toString().contains("PriceBreakQuantity")) {
                if (pkgType.equals("")) {
                    pkgType = pckging;
                }
                if (pkgType.equals(CUT_TAPE)) {
                    try {
                        String qtyStr = priceBrkElement.child(1).childNode(1).childNode(0).outerHtml().replace(",", "");
                        qty = Integer.parseInt(qtyStr);

                        String priceStr = priceBrkElement.child(2).childNode(1).childNode(0).outerHtml().replace("$", "");
                        try {
                            price = Double.parseDouble(priceStr);
                            cutTapePriceList.add(new PriceBreaker(qty, price, null, CUT_TAPE.replace(">", "").replace("<", ""), null));
                        } catch (NumberFormatException nfe) {
                            cutTapePriceList.add(new PriceBreaker(qty, price, null, CUT_TAPE.replace(">", "").replace("<", ""), priceBrkElement.child(2).childNode(1).childNode(0).childNode(0).outerHtml()));
                        }
                    } catch (Exception e) {
                        System.out.println(priceBrkElement);
                    }
                } else if (pkgType.equals(FULL_REEL)) {
                    try {
                        String qtyStr = priceBrkElement.child(1).childNode(1).childNode(0).outerHtml().replace(",", "");
                        qty = Integer.parseInt(qtyStr);

                        String priceStr = priceBrkElement.child(2).childNode(1).childNode(0).outerHtml().replace("$", "");
                        try {
                            price = Double.parseDouble(priceStr);
                            reelPriceList.add(new PriceBreaker(qty, price, null, FULL_REEL.replace(">", "").replace("<", ""), null));
                        } catch (NumberFormatException nfe) {
                            reelPriceList.add(new PriceBreaker(qty, price, null, FULL_REEL.replace(">", "").replace("<", ""), priceBrkElement.child(2).childNode(1).childNode(0).childNode(0).outerHtml()));
                        }
                    } catch (Exception e) {
                        System.out.println(priceBrkElement);
                    }
                } else if (pkgType.equals(pckging)){
                    try {
                        String qtyStr = priceBrkElement.child(1).childNode(1).childNode(0).outerHtml().replace(",", "");
                        qty = Integer.parseInt(qtyStr);

                        String priceStr = priceBrkElement.child(2).childNode(1).childNode(0).outerHtml().replace("$", "");
                        try {
                            price = Double.parseDouble(priceStr);
                            cutTapePriceList.add(new PriceBreaker(qty, price, null, pckging, null));
                        } catch (NumberFormatException nfe) {
                            cutTapePriceList.add(new PriceBreaker(qty, price, null, pckging, priceBrkElement.child(2).childNode(1).childNode(0).childNode(0).outerHtml()));
                        }
                    } catch (Exception e) {
                        System.out.println(priceBrkElement);
                    }
                }
            } else {

                }
                if (priceBrkElement.toString().contains(CUT_TAPE)) {
                    pkgType = CUT_TAPE;
                    continue;
                } else if (priceBrkElement.toString().contains(MOUSE_REEL)) {
                    pkgType = MOUSE_REEL;
                    continue;
                } else if (priceBrkElement.toString().contains(FULL_REEL) || priceBrkElement.toString().contains(REEL_OF) ) {
                    pkgType = FULL_REEL;
                    continue;
                } else if (pkgType.equals(MOUSE_REEL)) {
                    continue;
                } else {
                    pkgType = pckging;
            }

        }

        /**

        for (int i = 0; i < priceBrkElements.size() - 1;  i++) {
            Element priceElement = priceBrkElements.get(i);
            Double price;
            if (priceElement.childNode(1).childNodeSize() > 0) {
                String priceStr = priceElement.childNode(1).childNode(0).outerHtml().replace("$", "");
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    price = null;
                }
            } else {
                price = null;
            }

            Element qtyElement = priceQtyElements.get(i + 1);
            if (qtyElement.childNode(1).childNodeSize() > 0
                    && price != null) {
                String qtyString = qtyElement.childNode(1).childNode(0).outerHtml().trim().replace(",", "").replace(":", "");
                Integer qty = Integer.parseInt(qtyString);
                priceBreakerList.add(new PriceBreaker(qty, price, null));
            }
        }*/

        priceBreakerList.addAll(cutTapePriceList);
        priceBreakerList.addAll(reelPriceList);
        return priceBreakerList;
    }

    private List<PriceBreaker> mouserPriceBreaker(String vendor, String pckging) {
        List<PriceBreaker> priceBreakerList = new ArrayList<PriceBreaker>();
        if (document == null)
            return priceBreakerList;
        /**
        Elements priceQtyElements = document.getElementsByAttributeValue("class", "PriceBreakQuantity");
        Elements priceBrkElements = document.getElementsByAttributeValue("class", "PriceBreakPrice");


        for (int i = 0; i < priceBrkElements.size() - 1;  i++) {
            Element priceElement = priceBrkElements.get(i);
            Double price;
            if (priceElement.childNode(1).childNodeSize() > 0) {
                String priceStr = priceElement.childNode(1).childNode(0).outerHtml().replace("$", "");
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    price = null;
                }
            } else {
                price = null;
            }

            Element qtyElement = priceQtyElements.get(i + 1);
            if (qtyElement.childNode(1).childNodeSize() > 0
                    && price != null) {
                String qtyString = qtyElement.childNode(1).childNode(0).outerHtml().trim().replace(",", "").replace(":", "");
                Integer qty = Integer.parseInt(qtyString);
                PriceBreaker priceBreaker = new PriceBreaker(qty, price, null);
                priceBreaker.setVendor(vendor);
                priceBreakerList.add(priceBreaker);
            }
        }
         */

        Elements priceBreaks;
        try {
            priceBreaks = document.getElementsByClass("PriceBreaks").get(0).getElementsByTag("tr");
        } catch (Exception e) {
            System.out.println("Prices not listed");
            return priceBreakerList;
        }

        String pkgType = "";
        List<PriceBreaker> cutTapePriceList = new ArrayList<PriceBreaker>();
        List<PriceBreaker> reelPriceList = new ArrayList<PriceBreaker>();
        for (Element priceBrkElement:priceBreaks) {
            Integer qty;
            Double price = 0.0;
            if (priceBrkElement.toString().contains("PriceBreakQuantity")) {
                if (pkgType.equals("")) {
                    pkgType = pckging;
                }
                if (pkgType.equals(CUT_TAPE)) {
                    try {
                        String qtyStr = priceBrkElement.child(1).childNode(1).childNode(0).outerHtml().replace(",", "");
                        qty = Integer.parseInt(qtyStr);

                        String priceStr = priceBrkElement.child(2).childNode(1).childNode(0).outerHtml().replace("$", "");
                        PriceBreaker priceBreaker;
                        try {
                            price = Double.parseDouble(priceStr);
                            priceBreaker = new PriceBreaker(qty, price, null, CUT_TAPE.replace(">", "").replace("<", ""), null);
                        } catch (NumberFormatException nfe) {
                            priceBreaker = new PriceBreaker(qty, price, null, CUT_TAPE.replace(">", "").replace("<", ""), priceBrkElement.child(2).childNode(1).childNode(0).childNode(0).outerHtml());
                        }

                        priceBreaker.setVendor(vendor);
                        cutTapePriceList.add(priceBreaker);
                    } catch (Exception e) {
                        System.out.println(priceBrkElement);
                    }
                } else if (pkgType.equals(FULL_REEL)) {
                    try {
                        String qtyStr = priceBrkElement.child(1).childNode(1).childNode(0).outerHtml().replace(",", "");
                        qty = Integer.parseInt(qtyStr);

                        String priceStr = priceBrkElement.child(2).childNode(1).childNode(0).outerHtml().replace("$", "");
                        PriceBreaker priceBreaker;
                        try {
                            price = Double.parseDouble(priceStr);
                            priceBreaker = new PriceBreaker(qty, price, null, FULL_REEL.replace(">", "").replace("<", ""), null);
                        } catch (NumberFormatException nfe) {
                            priceBreaker = new PriceBreaker(qty, price, null, FULL_REEL.replace(">", "").replace("<", ""), priceBrkElement.child(2).childNode(1).childNode(0).childNode(0).outerHtml());
                        }
                        priceBreaker.setVendor(vendor);
                        reelPriceList.add(priceBreaker);
                    } catch (Exception e) {
                        System.out.println(priceBrkElement);
                    }
                } else if (pkgType.equals(pckging)){
                    try {
                        String qtyStr = priceBrkElement.child(1).childNode(1).childNode(0).outerHtml().replace(",", "");
                        qty = Integer.parseInt(qtyStr);

                        String priceStr = priceBrkElement.child(2).childNode(1).childNode(0).outerHtml().replace("$", "");
                        PriceBreaker priceBreaker;
                        try {
                            price = Double.parseDouble(priceStr);
                            priceBreaker = new PriceBreaker(qty, price, null, pckging, null);
                        } catch (NumberFormatException nfe) {
                            priceBreaker = new PriceBreaker(qty, price, null, pckging, priceBrkElement.child(2).childNode(1).childNode(0).childNode(0).outerHtml());
                        }
                        priceBreaker.setVendor(vendor);
                        cutTapePriceList.add(priceBreaker);
                    } catch (Exception e) {
                        System.out.println(priceBrkElement);
                    }
                }
            } else {

            }
            if (priceBrkElement.toString().contains(CUT_TAPE)) {
                pkgType = CUT_TAPE;
                continue;
            } else if (priceBrkElement.toString().contains(MOUSE_REEL)) {
                pkgType = MOUSE_REEL;
                continue;
            } else if (priceBrkElement.toString().contains(FULL_REEL) || priceBrkElement.toString().contains(REEL_OF) ) {
                pkgType = FULL_REEL;
                continue;
            } else if (pkgType.equals(MOUSE_REEL)) {
                pkgType = pckging;
            }

        }

        priceBreakerList.addAll(cutTapePriceList);
        priceBreakerList.addAll(reelPriceList);
        return priceBreakerList;
    }



    public MouserProductInfo mouserExtraction(String partNum, String vendor) {
        if (document == null) {
            return null;
        }
        Document _doc = document.clone();
        Element partNumElement = document.getElementById("divMouserPartNum");
        String mouserPartNumber;
        String newUrl = "";
        try {
            mouserPartNumber = partNumElement.childNode(0).outerHtml().trim();
        } catch (NullPointerException e) {
            try {
                if (!newUrl.contains("..") && !document.getElementsByAttributeValueContaining("href", partNum).isEmpty()) {
                    newUrl = "http://www.mouser.com" + document.getElementsByAttributeValueContaining("href", partNum).get(0).attributes().get("href");
                    document = ConnectionUtils.connectionRetry(newUrl, 1);
                } else {
                    document = null;
                }
                if (document != null)
                    return mouserExtraction(partNum, vendor);
                else {
                    Set<String> allUrls = new HashSet<String>();
                    Set<String> preferredUrls = new HashSet<String>();
                    for (Element element:_doc.getElementsByAttributeValueContaining("href", "../ProductDetail")) {
                        if (MkStrintUtils.removeSpecialChars(element.toString(), "/").toLowerCase().contains("/" + MkStrintUtils.removeSpecialChars(partNum).toLowerCase() + "/")) {
                            newUrl = "http://www.mouser.com" + element.attributes().get("href").replace("..", "");
                            //document.getElementsByAttributeValueContaining("href", partNum).get(0).attributes().get("href");
                            if (vendor != null &&
                                    !vendor.equals("") &&
                                    MkStrintUtils.removeSpecialChars(newUrl).toLowerCase().contains(MkStrintUtils.removeSpecialChars(vendor).toLowerCase())){
                                preferredUrls.add(newUrl);
                            }
                            allUrls.add(newUrl);
                        }
                    }
//                    return new MouserProductInfo(Errors.PART_NOT_FOUND, newUrl, partNum);
                    if (preferredUrls.isEmpty())
                        return getMouserProductFromLinks(allUrls, partNum, _doc.baseUri(), vendor);
                    else
                        return getMouserProductFromLinks(preferredUrls, partNum, _doc.baseUri(), vendor);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Part sold by other dealer, info not listed on Mouser");
                return new MouserProductInfo(Errors.PART_NOT_FOUND, newUrl, partNum);
            }
        }

        Element mfrPartNumberElement = document.getElementById("divManufacturerPartNum");
        String mfrPartNumber = partNumElement.childNode(0).outerHtml().trim();

        Elements mfrElements = document.getElementsByAttributeValue("itemprop", "manufacturer");
        String mfr = mfrElements.get(0).childNode(3).childNode(1).childNode(0).outerHtml().trim();

        Element descElement = document.getElementById("divDes");
        String desc = descElement.childNode(0).outerHtml().trim();

        Map<String, String> info = new HashMap<String, String>();


        try {
            Elements elements = document.getElementsByClass("ProductDetailData");
            for (Element element:elements) {
                try {
                    Element parent = element.parent().parent();
                    String key = parent.child(0).child(0).child(0).childNode(0).outerHtml();
                    String value = parent.child(0).child(1).child(0).childNode(0).outerHtml();
                    info.put(key, value);
                } catch (Exception e) {
                    System.out.println(element);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get additional information for " + partNum);
        }

        String pkging = "";
        if (info.containsKey(PACKAGING)){
            pkging = info.get(PACKAGING);
        }

        String pkg = "";
        if (info.containsKey(PKG)){
            pkg = info.get(PKG);
        }

        String family = "";
        if (info.containsKey(TYPE)){
            family = info.get(TYPE);
        }

        String rohs = "";
        if (info.containsKey(ROHS)){
            try {
                rohs = info.get(ROHS).toString().split("alt=\"")[1].split("\" ")[0];
            } catch (Exception e) {
                System.out.println("Error while reading RoHS for " + partNum);
            }
        }


        Element qtyAvailElement = document.getElementById("availability");
        Integer qtyAvailable;
        if (qtyAvailElement != null) {
            String qtyAvailString = qtyAvailElement.getElementsByTag("table").get(0).getElementsByTag("td").get(2).childNode(0).outerHtml().replace(",", "");
            try {
                qtyAvailable = Integer.parseInt(qtyAvailString);
            } catch (NumberFormatException nfe) {
                qtyAvailable = 0;
            }
        } else {
            qtyAvailable = 0;
        }

        MouserProductInfo mouserProductInfo = new MouserProductInfo(mouserPartNumber, mfrPartNumber, mfr, desc, qtyAvailable, document.baseUri());
        mouserProductInfo.setPkging(pkging);
        mouserProductInfo.setPkg(pkg);
        mouserProductInfo.setFamily(family);
        mouserProductInfo.setRohs(rohs);


        return mouserProductInfo;
    }

    private MouserProductInfo getMouserProductFromLinks(Set<String> urls, String partNum, String originalUrl, String vendor) {
        List<MouserProductInfo> mouserProductInfoList = new ArrayList<MouserProductInfo>();
        MouserProductInfo mouserProductInfo = null;
        for (String url:urls) {
            document = ConnectionUtils.connectionRetry(url, 1);
            MouserProductInfo temp = mouserExtraction(partNum, vendor);
            if (temp == null) {
                return new MouserProductInfo(Errors.PART_NOT_FOUND, url, partNum);
            }
            temp.setPartLink(url);
            List<PriceBreaker> priceBreakers = mouserPriceBreaker(temp.getMfr(), temp.getPkging());
            temp.setPriceBreakers(priceBreakers);
            mouserProductInfoList.add(temp);
        }
        if (!mouserProductInfoList.isEmpty()) {
            mouserProductInfo = mouserProductInfoList.get(0);
            for (int i = 1; i < mouserProductInfoList.size(); i++) {
                MouserProductInfo temp = mouserProductInfoList.get(i);
                mouserProductInfo.getPriceBreakers().addAll(temp.getPriceBreakers());
            }
            Collections.sort(mouserProductInfo.getPriceBreakers());
        }
        return mouserProductInfo;
    }

}
