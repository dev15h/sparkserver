package com.mk;

import com.mk.utils.Errors;
import com.mk.utils.FileUtils;
import com.mk.vo.InputRow;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static spark.Spark.get;

/**
 * Created by margish on 5/23/15.
 */
public class Controller {
    public static void main(String[] args) {
        get(new Route("/getFormData") {
            public Object handle(Request request, Response response) {
                String fileName = request.queryMap("fileName").value();
                return FileUtils.getCSVFromExcel(fileName);
            }
        });
        get(new Route("/submitSelection") {
            public Object handle(Request request, Response response) {

                //TEMP. REMOVE IN FINAL VERSION
                //if (new Date().getYear() != 2015 && new Date().getMonth() > 8){
                //    return "";
                //}

                String tsvData = request.queryMap("selection").value();
                String lines[] = tsvData.split("\n");

                List<InputRow> inputRowList = new ArrayList<InputRow>();
                for (String line : lines) {
                    try {
                        String cols[] = line.split("\t");
                        Integer qty = Integer.parseInt(cols[0]);
                        if (qty == 0)
                            qty = 1;
                        String mfr = cols[1];
                        String schRef = cols[2];
                        String vendor = null;
                        String mfrPn2 = null;
                        String mfrPn3 = null;
                        if (cols.length > 3 && !cols[3].equals(Errors.BLANK))
                            vendor = cols[3];
                        if (cols.length > 4 && !cols[4].equals(Errors.BLANK))
                            mfrPn2 = cols[4];
                        if (cols.length > 5 && !cols[5].equals(Errors.BLANK))
                            mfrPn3 = cols[5];
                        InputRow inputRow = new InputRow(mfr, schRef, qty, vendor, mfrPn2, mfrPn3);
                        inputRowList.add(inputRow);
                    } catch (Exception e) {
                        System.out.println("**Ignoring invalid row :\t" + line + "**");
                    }
                }

                String lotQtyCsv = request.queryMap("lotQtyList").value();
                List<Integer> lotQtyList = new ArrayList<Integer>();
                String qtySplit[] = lotQtyCsv.split(",");
                for (String qtyStr:qtySplit) {
                    try {
                        Integer lotQty = Integer.parseInt(qtyStr);
                        if (lotQty > 0) lotQtyList.add(lotQty);
                    } catch (Exception e) {

                    }
                }
                if (lotQtyList.isEmpty()) lotQtyList.add(1);
                return FileUtils.generateDataForExcel(inputRowList, lotQtyList);
            }
        });
    }
}
