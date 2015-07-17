package com.mk.utils;

import com.mk.vendors.DigiKeyProductExtractor;
import com.mk.vendors.MouserExtractor;
import com.mk.vo.*;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by margish on 5/23/15.
 */
public class FileUtils {
    private static final int QUANTITY_COL_INDEX = 3;
    private static final int TOTAL_QTY_COL_INDEX = 5;
    private static final int DIGIKEY_PRICE_COL_INDEX = 5;
    private static final int TOTAL_PRICE_COL_INDEX = 10;

    public static String getCSVFromExcel(String fileName){
        StringBuilder csv = new StringBuilder();
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(fileName));
            //Get the workbook instance for XLS file
            Workbook workbook = WorkbookFactory.create(file);

            //Get first sheet from the workbook
            Sheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows from first sheet
            //Iterator<Row> rowIterator = sheet.iterator();
            //while(rowIterator.hasNext()) {
            int rowEnd = sheet.getLastRowNum();
            int rowStart = sheet.getFirstRowNum();
            for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null)
                    continue;
                int lastColumn = row.getLastCellNum();
                //For each row, iterate through each columns
                //Iterator<Cell> cellIterator = row.cellIterator();
                //while(cellIterator.hasNext()) {

                int cols = 0;
                for (int cn = 0; cn < lastColumn; cn++) {
                    Cell cell = row.getCell(cn, Row.RETURN_BLANK_AS_NULL);

                    //Cell cell = cellIterator.next();


                    if (cell == null) {
                        csv.append(" \t");
                        System.out.print(" \t");
                    } else {
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_BLANK:
                                csv.append(" \t");
                                System.out.print(" \t");
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                csv.append(cell.getBooleanCellValue() + "\t");
                                System.out.print(cell.getBooleanCellValue() + "\t");
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                Double num = cell.getNumericCellValue();
                                if ("0".equals(num.toString().split("\\.")[1])) {
                                    csv.append(num.toString().split("\\.")[0] + "\t");
                                    System.out.print(num.toString().split("\\.")[0] + "\t");
                                } else {
                                    if (num.toString().indexOf('E') > -1) {
                                        csv.append(num.longValue() + "\t");
                                        System.out.print(num.longValue() + "\t");
                                    } else {
                                        csv.append(num.toString() + "\t");
                                        System.out.print(num.toString() + "\t");
                                    }
                                }
                                break;
                            case Cell.CELL_TYPE_STRING:
                                csv.append(cell.getStringCellValue().replaceAll("\n", "zzzzz") + "\t");
                                System.out.print(cell.getStringCellValue() + "\t");
                                break;
                        }
                    }
                }
                csv.append("\n");
                System.out.println("");
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




        return "xxxxx" +  csv.toString();
    }

    public static String generateDataForExcel(List<InputRow> inputRows, List<Integer> qty) {
        File file = null;
        try {
            List<ProductInfo> productInfoList = getProductInfo(inputRows);

            Date now = new Date();
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy kk.mm.ss");
            file = new File("output_" + formatter.format(now) + ".xls");
            FileOutputStream outputStream = new FileOutputStream(file);

            HSSFWorkbook workbook = createWorkBook(productInfoList, qty);

            workbook.write(outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return file.getAbsolutePath();
    }

    private static List<ProductInfo> getProductInfo(List<InputRow> inputRows) {
        List<ProductInfo> productInfoList = new ArrayList<ProductInfo>();
        for (InputRow inputRow:inputRows) {

            ProductInfo productInfo = new ProductInfo();
            productInfo.setSchRef(inputRow.getSchemRef());
            productInfo.setPartNum(inputRow.getMfrPartNum());
            productInfo.setUserQty(inputRow.getQty());
            productInfo.setPreferredVendor(inputRow.getVendor());
            Set<DigikeyProductInfo> digikeyProductInfoSet = new HashSet<DigikeyProductInfo>();

            String partNum = inputRow.getMfrPartNum();
            DigiKeyProductExtractor digiKeyProductExtractor = new DigiKeyProductExtractor();
            List<String> linksForProduct = digiKeyProductExtractor.getLinksForProduct(partNum);

            DigikeyProductInfo digikeyProductInfo;
            String partNumFound = partNum;
            if (linksForProduct.isEmpty()) {
                if (inputRow.getMfrPn2() != null) {
                    System.out.println(partNum + "not found on Digikey. Trying for 2ndn mfr part num " + inputRow.getMfrPn2());
                    linksForProduct = digiKeyProductExtractor.getLinksForProduct(inputRow.getMfrPn2());
                    if (linksForProduct.isEmpty())
                        System.out.println(inputRow.getMfrPn2() + " not found on Digikey");
                    else {
                        partNumFound = inputRow.getMfrPn2();
                    }
                }

                if (linksForProduct.isEmpty() && inputRow.getMfrPn3() != null) {
                    System.out.println("Trying for 3rd mfr part num " + inputRow.getMfrPn3());
                    linksForProduct = digiKeyProductExtractor.getLinksForProduct(inputRow.getMfrPn3());
                } else {
                    partNumFound = inputRow.getMfrPn3();
                }

                if (linksForProduct.isEmpty()) {
                    String preUrl = "http://www.digikey.com/product-search/en?vendor=0&keywords=";
                    String url = preUrl + partNum;
                    digikeyProductInfo = new DigikeyProductInfo(Errors.PART_NOT_FOUND, url, partNum);
                }

            }
            if (!linksForProduct.isEmpty()){
                productInfo.setPartNum(partNumFound);
                for (String link : linksForProduct) {
                    digikeyProductInfo = digiKeyProductExtractor.getDigikeyProductDetails(link);
                    digikeyProductInfo.setUserQty(inputRow.getQty());
                    if (digikeyProductInfo.getPriceBreakers() != null && !digikeyProductInfo.getPriceBreakers().isEmpty()) {
                        System.out.println(digikeyProductInfo);
                        digikeyProductInfoSet.add(digikeyProductInfo);
                    }
                }
            }
            productInfo.setDigiProductSet(digikeyProductInfoSet);

            MouserExtractor mouserExtractor = new MouserExtractor();
            MouserProductInfo mouserProductInfo = mouserExtractor.getMouseProductDetails(partNum, inputRow.getVendor(), inputRow.getMfrPn2(), inputRow.getMfrPn3());
            if (mouserProductInfo != null) {
                mouserProductInfo.setUserQty(inputRow.getQty());
                productInfo.setMouserProductInfo(mouserProductInfo);
                System.out.println(mouserProductInfo.toString());
            }
            System.out.println("\n");
            productInfoList.add(productInfo);

        }
        return productInfoList;
    }

    private static HSSFWorkbook createWorkBook(List<ProductInfo> productInfoList, List<Integer> qtyList) {
        HSSFWorkbook workbook = new HSSFWorkbook();

        for (Integer qty:qtyList) {
            Sheet sheet = workbook.createSheet("Lot_Qty_" + qty);

            //HEADER
            Row headerRow = sheet.createRow(0);
            String headers[] = {"Reference", "Schematics Ref", "Manufacturer P/N", "Qty", "Lot Qty", "Total Qty", "DigiKey Price", "DigiKey Qty", "Mouser Price", "Mouser Qty", "Total Price", "Digikey Stock", "Mouser Stock", "Digi-Key P/N", "Mouser P/N", "Description", "Manufacturer", "Packaging", "Package", "Category", "Family", "RoHS"};
            CellStyle headerCellStyle = getHeaderStyle(workbook);
            int columnIndex = 0;
            for (String header : headers) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(header);
                sheet.setColumnWidth(columnIndex++, 6000);
            }

            List<ExcelRow> excelRows = getExcelRows(productInfoList, qty);

            //ROWS
            int rowIndex = 1;
            int ref = 1;
            for (ExcelRow excelRow : excelRows) {
                columnIndex = 0;
                Row row = sheet.createRow(rowIndex++);

                Cell cell = row.createCell(columnIndex++);
                cell.setCellValue(ref++);//excelRow.getReference());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getSchemRef());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getMfrPartNum());

                cell = row.createCell(columnIndex++);
                cell.setCellValue(Integer.parseInt(excelRow.getQty()));

                cell = row.createCell(columnIndex++);
                cell.setCellValue(Integer.parseInt(excelRow.getLotQty()));

                cell = row.createCell(columnIndex++);
                cell.setCellValue(Integer.parseInt(excelRow.getTotalQty()));

                cell = row.createCell(columnIndex++);
                if (excelRow.getDigiPrice() == null)
                    cell.setCellValue("N/A");
                else {
                    if (excelRow.getBestPrice().equals(ExcelRow.DIGIKEY))
                        cell.setCellStyle(getGreenHighLightCellStyle(workbook));
                    try {
                        cell.setCellValue(Double.parseDouble(excelRow.getDigiPrice()));
                    } catch (Exception e) {
                        cell.setCellValue(excelRow.getDigiPrice());
                    }
                }

                cell = row.createCell(columnIndex++);
                if (excelRow.getDigiMinimumQty() == null)
                    cell.setCellValue("N/A");
                else {
                    try {
                        cell.setCellValue(Integer.parseInt(excelRow.getDigiMinimumQty()));
                    } catch (Exception e) {
                        cell.setCellValue(excelRow.getDigiMinimumQty());
                    }
                }

                cell = row.createCell(columnIndex++);
                if (excelRow.isMouserNotFound() || excelRow.getMouserPrice() == null)
                    cell.setCellValue("");
                else {
                    if (excelRow.getBestPrice().equals(ExcelRow.MOUSER))
                        cell.setCellStyle(getGreenHighLightCellStyle(workbook));
                    try {
                        cell.setCellValue(Double.parseDouble(excelRow.getMouserPrice()));
                    } catch (Exception e) {
                        cell.setCellValue(excelRow.getMouserPrice());
                    }
                }

                cell = row.createCell(columnIndex++);
                if (excelRow.isMouserNotFound())
                    cell.setCellValue("");
                else {
                    try {
                        cell.setCellValue(Integer.parseInt(excelRow.getMouserMinimumQty()));
                    } catch (Exception e) {
                        cell.setCellValue(excelRow.getMouserMinimumQty());
                    }
                }

                cell = row.createCell(columnIndex++);
                cell.setCellValue(excelRow.getTotalPrice());

                cell = row.createCell(columnIndex++);
                if (excelRow.getDigiStock() == null)
                    cell.setCellValue("N/A");
                else
                    cell.setCellValue(Integer.parseInt(excelRow.getDigiStock()));

                cell = row.createCell(columnIndex++);
                if (excelRow.isMouserNotFound())
                    cell.setCellValue("");
                else
                    cell.setCellValue(Integer.parseInt(excelRow.getMouserStock()));


                Hyperlink digi_link = workbook.getCreationHelper().createHyperlink(Hyperlink.LINK_URL);
                cell = row.createCell(columnIndex++);
//                System.out.println("** " + excelRow.getDigikeyLink() + " ** " + excelRow.getDigiPN());
                digi_link.setAddress(excelRow.getDigikeyLink());
                cell.setCellStyle(getPartNumLinkStyle(workbook, excelRow.getDigiPN().equals(Errors.PART_NOT_FOUND)));
                cell.setHyperlink(digi_link);
                cell.setCellValue(excelRow.getDigiPN());

                Hyperlink mouser_link = workbook.getCreationHelper().createHyperlink(Hyperlink.LINK_URL);
                cell = row.createCell(columnIndex++);
//                System.out.println("**" + excelRow.getMouserLink() + " ** " + excelRow.getMouserPN());
                mouser_link.setAddress(excelRow.getMouserLink());
                cell.setCellStyle(getPartNumLinkStyle(workbook, (excelRow.getMouserPN().equals(Errors.PART_NOT_FOUND))));
                cell.setHyperlink(mouser_link);
                cell.setCellValue(excelRow.getMouserPN());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getDesc());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getMfr());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getPckging());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getPckg());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getCategory());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getFamily());

                cell = row.createCell(columnIndex++);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(excelRow.getRohs());

            }

            addCostCalculations(sheet, rowIndex, qty);

        }//Lot Qty Loop


        return workbook;
    }

    private static void addCostCalculations(Sheet sheet, int rowIndex, Integer lotQty) {
        Font totalFont = sheet.getWorkbook().createFont();

        CellStyle sumTextCellStyle = sheet.getWorkbook().createCellStyle();
        totalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        sumTextCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        sumTextCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        sumTextCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        sumTextCellStyle.setFont(totalFont);

        CellStyle sumFormulaCellStyle = sheet.getWorkbook().createCellStyle();
        sumFormulaCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        sumFormulaCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        sumFormulaCellStyle.setBorderRight(CellStyle.BORDER_THIN);
        sumFormulaCellStyle.setFont(totalFont);

        CellStyle yellowHighlightCellStyle = sheet.getWorkbook().createCellStyle();
        yellowHighlightCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        yellowHighlightCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        yellowHighlightCellStyle.setBorderRight(CellStyle.BORDER_THIN);
        yellowHighlightCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        yellowHighlightCellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
        yellowHighlightCellStyle.setFont(totalFont);

        CellStyle greenHighlightCellStyle = sheet.getWorkbook().createCellStyle();
        greenHighlightCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        greenHighlightCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        greenHighlightCellStyle.setBorderRight(CellStyle.BORDER_THIN);
        greenHighlightCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        greenHighlightCellStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        greenHighlightCellStyle.setFont(totalFont);

        Row sumRow = sheet.createRow(rowIndex + 1);
        Cell totalQtyCell = sumRow.createCell(QUANTITY_COL_INDEX - 1);
        totalQtyCell.setCellStyle(sumTextCellStyle);
        totalQtyCell.setCellValue("Total Qty");
        
        Cell qtyTotalCell = sumRow.createCell(QUANTITY_COL_INDEX);
        qtyTotalCell.setCellStyle(sumFormulaCellStyle);
        qtyTotalCell.setCellType(Cell.CELL_TYPE_FORMULA);
        String qtySumFormula = getSumFormula(1, rowIndex - 1, QUANTITY_COL_INDEX - 1);
        qtyTotalCell.setCellFormula(qtySumFormula);

        Cell totalTextCell = sumRow.createCell(TOTAL_PRICE_COL_INDEX - 1);
        totalTextCell.setCellStyle(sumTextCellStyle);
        totalTextCell.setCellValue("Total Part Cost");

        Cell totalSumCell = sumRow.createCell(TOTAL_PRICE_COL_INDEX);
        totalSumCell.setCellStyle(sumFormulaCellStyle);
        totalSumCell.setCellType(Cell.CELL_TYPE_FORMULA);
        String totalSumFormula = getSumFormula(1, rowIndex - 1, TOTAL_PRICE_COL_INDEX - 1);
        totalSumCell.setCellFormula(totalSumFormula);

        rowIndex += 2;

        Row markupRow = sheet.createRow(rowIndex);

        //mark up
        Cell markupPercentTextCell = markupRow.createCell(TOTAL_QTY_COL_INDEX - 1);
        markupPercentTextCell.setCellStyle(sumTextCellStyle);
        markupPercentTextCell.setCellValue("Markup");

        Cell markupPercentCell = markupRow.createCell(DIGIKEY_PRICE_COL_INDEX);
        markupPercentCell.setCellStyle(yellowHighlightCellStyle);
        markupPercentCell.setCellType(Cell.CELL_TYPE_FORMULA);
        markupPercentCell.setCellFormula("20%");

        Cell markupCostTextCell = markupRow.createCell(TOTAL_PRICE_COL_INDEX - 1);
        markupCostTextCell.setCellStyle(sumTextCellStyle);
        markupCostTextCell.setCellValue("Parts cost with Markup");

        Cell markupCostCell = markupRow.createCell(TOTAL_PRICE_COL_INDEX);
        markupCostCell.setCellStyle(sumFormulaCellStyle);
        markupCostCell.setCellType(Cell.CELL_TYPE_FORMULA);
        String markupSumFormula = getCellRef(totalSumCell) + "*(1+" + getCellRef(markupPercentCell) + ")";
        markupCostCell.setCellFormula(markupSumFormula);
        

        rowIndex++;
        //labor cent
        Row laborCentRow = sheet.createRow(rowIndex);

        Cell laborCentTextCell = laborCentRow.createCell(TOTAL_QTY_COL_INDEX - 1);
        laborCentTextCell.setCellStyle(sumTextCellStyle);
        laborCentTextCell.setCellValue("Labor Cents Each");

        Cell laborCentCell = laborCentRow.createCell(DIGIKEY_PRICE_COL_INDEX);
        laborCentCell.setCellStyle(yellowHighlightCellStyle);
        laborCentCell.setCellType(Cell.CELL_TYPE_NUMERIC);
        laborCentCell.setCellValue(0.15);


        //unit part cost
        Cell unitPartCostTextCell = laborCentRow.createCell(TOTAL_PRICE_COL_INDEX - 1);
        unitPartCostTextCell.setCellStyle(sumTextCellStyle);
        unitPartCostTextCell.setCellValue("Unit Part Cost");

        Cell unitPartCostCell = laborCentRow.createCell(TOTAL_PRICE_COL_INDEX);
        unitPartCostCell.setCellStyle(sumFormulaCellStyle);
        unitPartCostCell.setCellType(Cell.CELL_TYPE_FORMULA);
        String unitPartSumFormula = getCellRef(markupCostCell) + "/" + lotQty ;
        unitPartCostCell.setCellFormula(unitPartSumFormula);


        rowIndex++;
        Row pcbCostRow = sheet.createRow(rowIndex);

        //pcb cost
        Cell pcbCostTextCell = pcbCostRow.createCell(TOTAL_QTY_COL_INDEX - 1);
        pcbCostTextCell.setCellStyle(sumTextCellStyle);
        pcbCostTextCell.setCellValue("PCB Cost");

        Cell pcbCostCell = pcbCostRow.createCell(DIGIKEY_PRICE_COL_INDEX);
        pcbCostCell.setCellStyle(yellowHighlightCellStyle);
        pcbCostCell.setCellType(Cell.CELL_TYPE_NUMERIC);
        pcbCostCell.setCellValue(5);


        //unit labor cost
        Cell unitLaborCostTextCell = pcbCostRow.createCell(TOTAL_PRICE_COL_INDEX - 1);
        unitLaborCostTextCell.setCellStyle(sumTextCellStyle);
        unitLaborCostTextCell.setCellValue("Unit Labor Cost");

        Cell unitLaborCostCell = pcbCostRow.createCell(TOTAL_PRICE_COL_INDEX);
        unitLaborCostCell.setCellStyle(sumFormulaCellStyle);
        unitLaborCostCell.setCellType(Cell.CELL_TYPE_FORMULA);
        String unitLaborSumFormula = getCellRef(qtyTotalCell) + "*" + getCellRef(laborCentCell) ;
        unitLaborCostCell.setCellFormula(unitLaborSumFormula);

        //pcb cost 2
        rowIndex++;
        Row pcbCostRow2 = sheet.createRow(rowIndex);

        //unit labor cost
        Cell pcbCost2TextCell = pcbCostRow2.createCell(TOTAL_PRICE_COL_INDEX - 1);
        pcbCost2TextCell.setCellStyle(sumTextCellStyle);
        pcbCost2TextCell.setCellValue("PCB Cost");

        Cell pcbCost2Cell = pcbCostRow2.createCell(TOTAL_PRICE_COL_INDEX);
        pcbCost2Cell.setCellStyle(sumFormulaCellStyle);
        pcbCost2Cell.setCellType(Cell.CELL_TYPE_FORMULA);
        String pcbCostFormula = getCellRef(pcbCostCell);
        pcbCost2Cell.setCellFormula(pcbCostFormula);

        //total cost each
        rowIndex++;
        Row totalCostEachRow = sheet.createRow(rowIndex);

        //unit labor cost
        Cell totalCostEach2TextCell = totalCostEachRow.createCell(TOTAL_PRICE_COL_INDEX - 1);
        totalCostEach2TextCell.setCellStyle(sumTextCellStyle);
        totalCostEach2TextCell.setCellValue("Total Cost Each");

        Cell totalCostEach2Cell = totalCostEachRow.createCell(TOTAL_PRICE_COL_INDEX);
        totalCostEach2Cell.setCellStyle(greenHighlightCellStyle);
        totalCostEach2Cell.setCellType(Cell.CELL_TYPE_FORMULA);
        String totalCostEachFormula = getCellRef(unitPartCostCell) + "+" + getCellRef(unitLaborCostCell) + "+" + getCellRef(pcbCost2Cell);
        totalCostEach2Cell.setCellFormula(totalCostEachFormula);





    }

    private static String getSumFormula(int firstRowIndex, int lastRowIndex, int colIndex) {
        String firstCellRef = CellReference.convertNumToColString(colIndex + 1) + (firstRowIndex + 1);
        String lastCellRef = CellReference.convertNumToColString(colIndex + 1) + (lastRowIndex + 1);
        String sumFormula = "SUM(" + firstCellRef + ":" + lastCellRef + ")";
        return sumFormula;
    }

    private static String getCellRef(Cell cell){
        return CellReference.convertNumToColString(cell.getColumnIndex()) + (cell.getRowIndex() + 1);
    }

    private static CellStyle getGreenHighLightCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        return cellStyle;
    }

    private static CellStyle getPartNumLinkStyle(Workbook workbook, boolean error) {
        CellStyle linkStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor((error) ? IndexedColors.RED.getIndex() : IndexedColors.BLUE.getIndex());
        linkStyle.setFont(font);
        return linkStyle;
    }

    private static List<ExcelRow> getExcelRows(List<ProductInfo> productInfoList, Integer qty) {
        List<ExcelRow> excelRows = new ArrayList<ExcelRow>();
        for (ProductInfo productInfo:productInfoList) {
            try {
                DigikeyProductInfo digikeyProductInfo = productInfo.getAggregatedDigikeyInfo();
                MouserProductInfo mouserProductInfo = productInfo.getMouserProductInfo();

                ExcelRow excelRow = new ExcelRow();
                excelRow.setLotQty(qty.toString());
                excelRow.setQty(productInfo.getUserQty().toString());
                excelRow.setSchemRef(productInfo.getSchRef());
                excelRow.setMfrPartNum(productInfo.getPartNum());

                excelRow.setDigikeyLink(digikeyProductInfo.getPartLink());
                if (digikeyProductInfo != null && digikeyProductInfo.getErrorMessage() == null) {
                    digikeyProductInfo.setBestPrice(digikeyProductInfo.getQtyPrice(qty));
                    PriceBreaker digiBestPrice = digikeyProductInfo.getBestPrice();

                    System.out.println(digikeyProductInfo.getMfrPartNumber());
                    excelRow.setDigiMinimumQty(digiBestPrice.getQty() != null ? digiBestPrice.getQty().toString() : digiBestPrice.getErrorMessage());
                    excelRow.setDigiPrice(digiBestPrice.getUnitPrice() != null ? digiBestPrice.getUnitPrice().toString() : digiBestPrice.getErrorMessage());
                    excelRow.setDigiStock(digikeyProductInfo.getQtyAvail().toString());
                    excelRow.setDigiPN(digikeyProductInfo.getPartNum());
                    excelRow.setDesc(digikeyProductInfo.getDesc());
                    excelRow.setMfr(digikeyProductInfo.getMfr());
                    excelRow.setPckging(digiBestPrice.getPkging());
                    excelRow.setPckg(digikeyProductInfo.getPkgCase());
                    excelRow.setCategory(digikeyProductInfo.getCategory());
                    excelRow.setFamily(digikeyProductInfo.getFamily());
                    excelRow.setRohs(digikeyProductInfo.getRohs());
//                        excelRow.setQty(digikeyProductInfo.getUserQty());
                }else {
                    excelRow.setDigiNotFound(true);
                    excelRow.setDigiMinimumQty(null);
                    excelRow.setDigiPrice(null);
                    excelRow.setDigiStock(null);
                    excelRow.setDigiPN(digikeyProductInfo.getErrorMessage());
                }

                if ((digikeyProductInfo == null || digikeyProductInfo.getErrorMessage() != null) && mouserProductInfo != null && mouserProductInfo.getErrorMessage() == null){
                    excelRow.setDesc(mouserProductInfo.getDesc());
                    excelRow.setMfr(mouserProductInfo.getMfr());
                    excelRow.setPckg(mouserProductInfo.getPkg());
                    excelRow.setCategory("N/A");
                    excelRow.setFamily(mouserProductInfo.getFamily());
                    excelRow.setRohs(mouserProductInfo.getRohs());
//                        excelRow.setQty(mouserProductInfo.getUserQty());
                }

                excelRow.setMouserLink(mouserProductInfo.getPartLink());
                if (mouserProductInfo.getErrorMessage() == null) {
                    mouserProductInfo.setBestPrice(mouserProductInfo.getQtyPrice(qty));
                    excelRow.setMouserStock(mouserProductInfo.getQntyAvailable().toString());
                    excelRow.setMouserPN(mouserProductInfo.getMouserPartNumber());
                    PriceBreaker mouserBestPrice = mouserProductInfo.getBestPrice();
                    if (mouserBestPrice != null){
                        excelRow.setMouserPrice((mouserBestPrice.getUnitPrice() != null) ? mouserBestPrice.getUnitPrice().toString() : mouserBestPrice.getErrorMessage());
                        mouserProductInfo.setBestPrice(mouserProductInfo.getQtyPrice(qty));
                        excelRow.setMouserMinimumQty((mouserBestPrice.getQty() != null) ? mouserBestPrice.getQty().toString() : mouserBestPrice.getErrorMessage());
                        excelRow.setMouserPrice((mouserBestPrice.getUnitPrice() != null && mouserBestPrice.getUnitPrice() != 0.0 && mouserBestPrice.getErrorMessage() == null) ? mouserBestPrice.getUnitPrice().toString() : mouserBestPrice.getErrorMessage());
                        excelRow.setPckging(mouserBestPrice.getPkging());
                    } else {
                        excelRow.setMouserPrice("");
                        excelRow.setMouserMinimumQty("");
                        excelRow.setMouserPrice("");
                    }
                } else {
                    excelRow.setMouserNotFound(true);
                    excelRow.setMouserPrice(null);
                    excelRow.setMouserStock(null);
                    excelRow.setMouserPN(mouserProductInfo.getErrorMessage());
                    excelRow.setMouserMinimumQty(null);
                    excelRow.setMouserPrice(null);
                }
                Integer tot = Integer.parseInt(excelRow.getQty()) * Integer.parseInt(excelRow.getLotQty());
                excelRow.setTotalQty(tot.toString());
                excelRows.add(excelRow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return excelRows;
    }

    private static CellStyle getHeaderStyle(HSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);


        return cellStyle;
    }

}