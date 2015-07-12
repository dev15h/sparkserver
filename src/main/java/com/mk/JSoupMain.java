package com.mk;

import com.mk.vendors.DigiKeyProductExtractor;
import com.mk.vendors.MouserExtractor;
import com.mk.vo.DigikeyProductInfo;
import com.mk.vo.MouserProductInfo;

import java.util.List;

/**
 * Created by margish on 5/30/15.
 */
public class JSoupMain {


    public static void main(String[] args) {
        String partNums[] = {"ERJ-3GEY0R00V", "ERJ-3EKF10R0V",  "ERJ-3EKF52R3V",  "ERJ-3EKF1000V",  "ERJ-3EKF1500V",  "MCR03EZPFX1800",  "MCR03EZPFX2700",  "MCR03EZPFX3000",  "MCR03EZPFX3300",  "MCR03EZPFX5100",  "RC0603FR-07620RL",  "ERJ-3EKF1001V",  "ERJ-3EKF1501V",  "CRCW06032K20FKEA"};
        for (String partNum:partNums) {
            System.out.println("\n\n" + partNum);
            DigiKeyProductExtractor digiKeyProductExtractor = new DigiKeyProductExtractor();
            List<String> linksForProduct = digiKeyProductExtractor.getLinksForProduct(partNum);
            for (String link : linksForProduct) {
                System.out.println("**"+link);
                DigikeyProductInfo digikeyProductInfo = digiKeyProductExtractor.getDigikeyProductDetails(link);
                System.out.println(digikeyProductInfo);
            }

            MouserExtractor mouserExtractor = new MouserExtractor();
            MouserProductInfo mouserProductInfo = mouserExtractor.getMouseProductDetails(partNum, null, null, null);
            if (mouserProductInfo != null)
                System.out.println(mouserProductInfo.toString());
        }
    }

}
