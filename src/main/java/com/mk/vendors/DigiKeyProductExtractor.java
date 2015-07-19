package com.mk.vendors;

import com.mk.utils.ConnectionUtils;
import com.mk.vo.DigikeyProductInfo;
import com.mk.vo.PriceBreaker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by margish on 5/31/15.
 */
public class DigiKeyProductExtractor {
    public static final String RD_SEARCH_PARTS_PAGE = "rd-search-parts-page";
    public static final String RD_PRODUCT_CATEGORY_PAGE = "rd-product-category-page";
    public static final String RD_PRODUCT_DETAILS_PAGE = "rd-product-details-page";

    public List<String> getLinksForProduct(String partNum) {
        String preUrl = "http://www.digikey.com/product-search/en?vendor=0&keywords=";

        List<String> prodLinks = new ArrayList<String>();

        String url = preUrl+partNum;
        System.out.println(url);
        Document doc = null;
        doc = ConnectionUtils.connectionRetry(url, 1);
        if (doc == null) {
            return prodLinks;
        }
        Elements html = doc.getElementsByTag("html");
        Element pageId = html.get(0);
        String classValue = pageId.attributes().get("class");


        if (RD_SEARCH_PARTS_PAGE.equals(classValue)) {
            System.out.println("Not Found");
        }

        // Code for site down
        //Elements body = doc.getElementsByTag("body");
        //pageId = body.get(0);

        /** @TO-DO
         * rd-product-details-page
         */

        if (RD_PRODUCT_CATEGORY_PAGE.equals(classValue)) {
            Elements tables = doc.getElementsByTag("table");
            int productTableIndex = -1;
            for (int i = 0; i < tables.size(); i++){
                if ("productTable".equals(tables.get(i).attributes().get("id"))){
                    productTableIndex = i;
                    break;
                }
            }
            Elements tbody = tables.get(productTableIndex).getElementsByTag("tbody");
            Elements td = tbody.get(0).getElementsByTag("td");

            for (Element element:td){
                if(element.html().indexOf("/product-detail/en/") > -1) {
                    Elements meta = element.getElementsByTag("meta");
                    if (meta.size() > 0) {
                        String prodLink = element.getElementsByTag("a").get(0).attributes().get("href");
                        String partLink = "http://www.digikey.com" + prodLink;
                        try {
                            if (URLDecoder.decode(prodLink, "UTF-8").toLowerCase().contains("/" + partNum.toLowerCase() + "/"))
                                prodLinks.add(partLink);
                            else
                                System.out.println("Ignoring non part link: " + partLink);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (RD_PRODUCT_DETAILS_PAGE.equals(classValue)) {
            prodLinks.add(url);
        }
        return prodLinks;
    }

    public DigikeyProductInfo getDigikeyProductDetails(String link) {
        Document document = null;
        document = ConnectionUtils.connectionRetry(link, 1);
        Element productDetailsElement = null;
        try {
            productDetailsElement = document.getElementsByAttributeValue("class", "product-details").get(0);
        } catch (Exception e) {
            System.out.println("Bad page retruned for " + link);
            e.printStackTrace();
            System.out.println("Skipping the link");
        }

        String partNum = productDetailsElement.getElementById("reportpartnumber").childNode(1).outerHtml().trim();
        String qtyAvailString;
        Integer qtyAvailable;
        String mfr;
        String mfrPartNumber;
        String desc;
        String rohs;

        try {
            qtyAvailString = productDetailsElement.getElementById("quantityavailable").childNode(1).outerHtml().split(":")[1].trim().replace(",", "");
            qtyAvailable = Integer.parseInt(qtyAvailString);
        } catch (Exception e) {
            qtyAvailable = 0;
        }
        mfr = productDetailsElement.getElementsByAttributeValue("class", "seohtag").get(0).getElementsByAttributeValue("itemprop", "name").get(0).childNode(0).outerHtml().trim();
        mfrPartNumber = productDetailsElement.getElementsByAttributeValue("itemprop", "model").get(0).childNode(0).outerHtml().trim();
        desc = productDetailsElement.getElementsByAttributeValue("itemprop", "description").get(0).childNode(0).outerHtml();
        rohs = productDetailsElement.childNode(1).childNode(13).childNode(1).childNode(0).outerHtml().trim();
        String category = null;
        String family = null;
        String packaging = "";
        String pkgCase = null;

        Element additionalInfoElement = document.getElementsByAttributeValue("class", "attributes-table-main").get(0);
        Elements additionalInfoRows = additionalInfoElement.getElementsByTag("tr");
        for (int i = 1; i < additionalInfoRows.size(); i++) {
            String rowString = additionalInfoRows.get(i).outerHtml();
            Element row = additionalInfoRows.get(i);
            if (rowString.indexOf("Category") > 01) {
                category = row.childNode(1).childNode(0).childNode(1).childNode(0).outerHtml().trim();
            } else if (rowString.indexOf("Family") > -1) {
                try {
                    if (row.childNode(1).childNode(0).childNodeSize() > 0) {
                        family = row.childNode(1).childNode(0).childNode(0).childNode(0).outerHtml().trim();
                    } else {
                        family = row.childNode(1).childNode(1).childNode(0).outerHtml();
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                        family = "Not Found";
                }
            } else if (rowString.indexOf(">Packaging&nbsp;<") > -1) {
                packaging = Jsoup.parse(row.childNode(1).childNode(0).outerHtml().trim()).text();
            } else if (rowString.indexOf("Package / Case") > -1) {
                pkgCase = row.childNode(1).childNode(0).outerHtml().trim();
            }
        }

        DigikeyProductInfo digikeyProductInfo = new DigikeyProductInfo(partNum, qtyAvailable, mfr, mfrPartNumber, desc, rohs, category, family, packaging, pkgCase, link);

        List<PriceBreaker> priceBreakers = getDigiPriceBreakers(document, packaging, mfr);

        digikeyProductInfo.setPriceBreakers(priceBreakers);

        return digikeyProductInfo;

    }

    private List<PriceBreaker> getDigiPriceBreakers(Document document, String packaging, String mfr) {
        List<PriceBreaker> priceBreakers = new ArrayList<PriceBreaker>();
        Element table = document.getElementById("pricing");
        Elements rows = table.getElementsByTag("tr");
        for (int i = 1; i < rows.size(); i++) {
            String qtyString;
            String unitPriceString;
            String extdPriceString;
            Integer qty;
            Double unitPrice;
            Double extdPrice;
            String error = null;

            qtyString = rows.get(i).childNode(0).childNode(0).outerHtml().trim().replace(",", "");
            unitPriceString = rows.get(i).childNode(1).childNode(0).outerHtml().trim();
            extdPriceString = rows.get(i).childNode(2).childNode(0).outerHtml().trim();
            try {
                qty = Integer.parseInt(qtyString);
            } catch (NumberFormatException e) {
                qty = null;
                error = qtyString;
            }
            try {
                unitPrice = Double.parseDouble(unitPriceString);
            } catch (NumberFormatException e) {
                unitPrice = null;
                error = unitPriceString;
            }

            try {
                extdPrice = Double.parseDouble(extdPriceString);
            } catch (NumberFormatException e) {
                extdPrice = null;
                error = extdPriceString;
            }

            String comparePkg = packaging.toLowerCase();
            if (comparePkg.contains("cut") && comparePkg.contains("tape")) {
                packaging = PriceBreaker.CUT_TAPE;
            } else if (comparePkg.contains("tape") && comparePkg.contains("reel")) {
                packaging = PriceBreaker.TAPE_AND_REEL;
            } else if (comparePkg.contains("bulk")) {
                packaging = PriceBreaker.BULK;
            } else if (comparePkg.contains("tube")) {
                packaging = PriceBreaker.TUBE;
            } else if (!comparePkg.equals("")){
                // do nothing;
            }

            PriceBreaker priceBreaker = new PriceBreaker(qty, unitPrice, extdPrice, packaging, error);
            priceBreaker.setVendor(mfr);


            priceBreakers.add(priceBreaker);
        }

        return priceBreakers;
    }
}
