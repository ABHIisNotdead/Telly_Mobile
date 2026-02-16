package com.example.tellymobile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelGenerator {

    private Context context;
    private static final String TEMPLATE_FILENAME = "invoice_template.xlsx";

    public ExcelGenerator(Context context) {
        this.context = context;
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }

    public void generateAndOpenExcel(Invoice invoice) {
        new Thread(() -> {
            try {
                Workbook workbook = createWorkbook(invoice);
                saveAndOpen(workbook, invoice.getInvoiceNumber());
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error generating Excel: " + e.getMessage());
            }
        }).start();
    }

    public Workbook createWorkbook(Invoice invoice) throws IOException {
        // Determine Template
        String templateName = TEMPLATE_FILENAME;
        
        boolean hasPaymentTemplate = false;
         try {
             InputStream is = context.getAssets().open("PaymentVoucher.xlsx");
             is.close();
             hasPaymentTemplate = true;
        } catch (IOException e) {
            hasPaymentTemplate = false;
        }
        
        boolean isPayment = "PAYMENT".equals(invoice.getBuyersOrderNo());
        boolean isJournal = "JOURNAL".equals(invoice.getBuyersOrderNo()) || "CONTRA".equals(invoice.getBuyersOrderNo());
        
        if (hasPaymentTemplate && isPayment) {
             templateName = "PaymentVoucher.xlsx";
        } else if (isJournal) {
             // If we had JournalVoucher.xlsx, we would use it here
             // For now we'll use processManualJournal or similar
        }

        boolean hasTemplate = false;
        try {
             InputStream is = context.getAssets().open(templateName);
             is.close();
             hasTemplate = true;
        } catch (IOException e) {
            hasTemplate = false;
        }

        if (hasTemplate) {
            return processTemplate(invoice, templateName);
        } else {
            if (isJournal) return processManualJournal(invoice);
            return processManual(invoice);
        }
    }

    private Workbook processTemplate(Invoice invoice, String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 1);

        // 1. Detect Tax Type
        String taxType = "CGST/SGST";
        boolean isIgst = false;
        boolean isUtgst = false;
        if (invoice.getExtraCharges() != null) {
            for (InvoiceCharge charge : invoice.getExtraCharges()) {
                String name = charge.getChargeName().toUpperCase();
                if (name.contains("IGST")) { isIgst = true; taxType = "IGST"; break; }
                if (name.contains("UTGST")) { isUtgst = true; taxType = "UTGST"; }
            }
        }

        Map<String, String> data = new HashMap<>();
        data.put("{{INVOICE_NO}}", invoice.getInvoiceNumber());
        data.put("{{DATE}}", invoice.getDate());
        data.put("{{CUSTOMER_NAME}}", invoice.getCustomerName());
        data.put("{{SUBTOTAL}}", String.format("%.2f", invoice.getTotalAmount()));
        
        double totalTax = invoice.getTotalTaxAmount();
        if (totalTax <= 0 && invoice.getExtraCharges() != null) {
            for (InvoiceCharge charge : invoice.getExtraCharges()) {
                String name = charge.getChargeName().toUpperCase();
                if (name.contains("GST") || name.contains("TAX") || name.contains("DUTY") || name.contains("CESS") || 
                    name.contains("IGST") || name.contains("CGST") || name.contains("SGST") || name.contains("UTGST")) {
                    totalTax += charge.getAmount();
                }
            }
        }
        data.put("{{TOTAL_TAX}}", String.format("%.2f", totalTax));

        data.put("{{DELIVERY_NOTE}}", checkNull(invoice.getDeliveryNote()));
        data.put("{{MODE_PAYMENT}}", checkNull(invoice.getModeOfPayment()));
        data.put("{{REF_NO}}", checkNull(invoice.getReferenceNo()));
        data.put("{{OTHER_REF}}", checkNull(invoice.getOtherReferences()));
        data.put("{{BUYER_ORDER_NO}}", checkNull(invoice.getBuyersOrderNo()));
        data.put("{{DISPATCH_DOC_NO}}", checkNull(invoice.getDispatchDocNo()));
        data.put("{{DELIVERY_NOTE_DATE}}", checkNull(invoice.getDeliveryNoteDate()));
        data.put("{{DISPATCH_THROUGH}}", checkNull(invoice.getDispatchThrough()));
        data.put("{{DESTINATION}}", checkNull(invoice.getDestination()));
        data.put("{{TERMS_DELIVERY}}", checkNull(invoice.getTermsOfDelivery()));
        data.put("{{BILL_OF_LADING}}", checkNull(invoice.getBillOfLading()));
        data.put("{{MOTOR_VEHICLE_NO}}", checkNull(invoice.getMotorVehicleNo()));
        
        data.put("{{THROUGH}}", checkNull(invoice.getDispatchThrough()));
        data.put("{{ACCOUNT}}", checkNull(invoice.getCustomerName()));    
        data.put("{{AMOUNT}}", String.format("%.2f", invoice.getGrandTotal())); 
        data.put("{{TOTAL_AMOUNT}}", String.format("%.2f", invoice.getGrandTotal())); 
        data.put("{{DR}}", "");     
        data.put("{{PARTICULARS}}", checkNull(invoice.getCustomerName()));      

        data.put("{{BUYER_ADDRESS}}", checkNull(invoice.getBuyerAddress()));
        data.put("{{BUYER_GST}}", checkNull(invoice.getBuyerGst()));
        data.put("{{BUYER_STATE}}", checkNull(invoice.getBuyerState()));

        data.put("{{CONSIGNEE_NAME}}", checkNull(invoice.getConsigneeName()));
        data.put("{{CONSIGNEE_ADDRESS}}", checkNull(invoice.getConsigneeAddress()));
        data.put("{{CONSIGNEE_GST}}", checkNull(invoice.getConsigneeGst()));
        data.put("{{CONSIGNEE_STATE}}", checkNull(invoice.getConsigneeState()));
        
        // Calculate Totals for Key Mapping
        double totalTaxableVal = 0;
        double totalCgstVal = 0;
        double totalSgstVal = 0;
        double totalIgstVal = 0;
        Map<String, TaxSummary> summaryMap = calculateTaxSummary(invoice);
        for (TaxSummary s : summaryMap.values()) {
            totalTaxableVal += s.taxableValue;
            totalCgstVal += s.cgstAmount;
            totalSgstVal += s.sgstAmount;
            totalIgstVal += s.igstAmount;
        }
        
        data.put("{{TOTAL_TAXABLE_VAL}}", String.format("%.2f", totalTaxableVal));
        if (isIgst) {
            data.put("{{TOTAL_CGST_TOTAL}}", String.format("%.2f", totalIgstVal));
            data.put("{{TOTAL_SGST_TOTAL}}", "");
        } else {
            data.put("{{TOTAL_CGST_TOTAL}}", String.format("%.2f", totalCgstVal));
            data.put("{{TOTAL_SGST_TOTAL}}", String.format("%.2f", totalSgstVal));
        }
        
        double grandTotal = invoice.getGrandTotal();
        data.put("{{GRAND_TOTAL}}", String.format("%.2f", grandTotal));
        data.put("{{AMOUNT_WORDS}}", "INR " + convertToWords(grandTotal) + " Only"); 
        data.put("{{AMOUNT_WORDS_TAX}}", "INR " + convertToWords(totalTax) + " Only"); 
        
        // Optional: Provide {{ROUND_OFF}} for templates if needed, 
        // derived from the manual charge if present
        double manualRoundOff = 0;
        if (invoice.getExtraCharges() != null) {
            for (InvoiceCharge charge : invoice.getExtraCharges()) {
                if (charge.getChargeName().equalsIgnoreCase("Round Off")) {
                    manualRoundOff = charge.getAmount();
                    break;
                }
            }
        }
        data.put("{{ROUND_OFF}}", String.format("%.2f", manualRoundOff));

        if (invoice.getBankLedgerId() > 0) {
            Map<String, String> bankDetails = fetchBankDetails(invoice.getBankLedgerId());
            data.put("{{BANK_NAME}}", bankDetails.getOrDefault("NAME", ""));
            data.put("{{BANK_AC}}", bankDetails.getOrDefault("ACCOUNT_NO", ""));
            data.put("{{BANK_IFSC}}", bankDetails.getOrDefault("IFSC", ""));
            data.put("{{BANK_BRANCH}}", bankDetails.getOrDefault("BRANCH", ""));
        } else {
             data.put("{{BANK_NAME}}", ""); data.put("{{BANK_AC}}", ""); data.put("{{BANK_IFSC}}", ""); data.put("{{BANK_BRANCH}}", "");
        }
        
        Map<String, String> companyData = fetchCompanyData();
        data.put("{{COMPANY_PAN}}", companyData.getOrDefault("PAN", ""));
        
        data.put("{{BUYER_EMAIL}}", checkNull(invoice.getBuyerEmail()));
        data.put("{{BUYER_MOBILE}}", checkNull(invoice.getBuyerMobile()));
        data.put("{{CONSIGNEE_EMAIL}}", checkNull(invoice.getConsigneeEmail()));
        data.put("{{CONSIGNEE_MOBILE}}", checkNull(invoice.getConsigneeMobile()));
        data.put("{{BS_ORDER_DATE}}", checkNull(invoice.getBuyersOrderDate())); 

        double totalQty = 0;
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                totalQty += item.getQuantity();
            }
        }
        data.put("{{TOTAL_QTY}}", String.valueOf(totalQty));


        // Search and replace headers for CGST/SGST
        if (isIgst || isUtgst) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String val = cell.getStringCellValue();
                        if (isIgst) {
                            if (val.equals("CGST")) cell.setCellValue("IGST");
                            if (val.equals("SGST/UTGST")) cell.setCellValue("");
                        } else if (isUtgst) {
                            if (val.contains("SGST")) cell.setCellValue("UTGST");
                        }
                    }
                }
            }
        }

        // 2. Handle Items Row
        int itemRowIndex = -1;
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("{{ITEM_NAME}}")) {
                    itemRowIndex = row.getRowNum();
                    break;
                }
            }
            if (itemRowIndex != -1) break;
        }

        if (itemRowIndex != -1) {
             Row sourceRow = sheet.getRow(itemRowIndex);
             Map<String, Integer> colMap = new HashMap<>();
             for (Cell cell : sourceRow) {
                 if (cell.getCellType() == CellType.STRING) {
                     String text = cell.getStringCellValue();
                     if (text.contains("{{ITEM_NAME}}")) colMap.put("{{ITEM_NAME}}", cell.getColumnIndex());
                     if (text.contains("{{SL_NO}}")) colMap.put("{{SL_NO}}", cell.getColumnIndex());
                     if (text.contains("{{HSN}}")) colMap.put("{{HSN}}", cell.getColumnIndex());
                     if (text.contains("{{GST}}")) colMap.put("{{GST}}", cell.getColumnIndex());
                     if (text.contains("{{QTY}}")) colMap.put("{{QTY}}", cell.getColumnIndex());
                     if (text.contains("{{UNIT}}")) colMap.put("{{UNIT}}", cell.getColumnIndex());
                     if (text.contains("{{RATE}}")) colMap.put("{{RATE}}", cell.getColumnIndex());
                     if (text.contains("{{AMOUNT}}")) colMap.put("{{AMOUNT}}", cell.getColumnIndex());
                 }
             }
             
             int itemCount = invoice.getItems() != null ? invoice.getItems().size() : 0;
             if (itemCount > 0) {
                  if (itemCount > 1) {
                      sheet.shiftRows(itemRowIndex + 1, sheet.getLastRowNum(), itemCount - 1);
                  }
                  
                  for (int i = 0; i < itemCount; i++) {
                      InvoiceItem item = invoice.getItems().get(i);
                      Row currentRow = (i == 0) ? sourceRow : sheet.createRow(itemRowIndex + i);
                      
                      if (i > 0) {
                          for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                              Cell src = sourceRow.getCell(j);
                              Cell newC = currentRow.createCell(j);
                              if(src != null) {
                                  newC.setCellStyle(src.getCellStyle());
                              }
                          }
                          copyMergedRegions(sheet, itemRowIndex, currentRow.getRowNum());
                      }
                      
                      fillCell(currentRow, colMap.get("{{ITEM_NAME}}"), item.getItemName());
                      setAlignment(currentRow, colMap.get("{{ITEM_NAME}}"), HorizontalAlignment.LEFT);
                      fillCell(currentRow, colMap.get("{{SL_NO}}"), String.valueOf(i + 1));
                      setAlignment(currentRow, colMap.get("{{SL_NO}}"), HorizontalAlignment.CENTER);
                      fillCell(currentRow, colMap.get("{{HSN}}"), item.getHsn());
                      setAlignment(currentRow, colMap.get("{{HSN}}"), HorizontalAlignment.CENTER);
                      fillCell(currentRow, colMap.get("{{GST}}"), item.getGstRate() + "%");
                      setAlignment(currentRow, colMap.get("{{GST}}"), HorizontalAlignment.CENTER);
                      fillCell(currentRow, colMap.get("{{QTY}}"), String.valueOf(item.getQuantity()));
                      setAlignment(currentRow, colMap.get("{{QTY}}"), HorizontalAlignment.CENTER);
                      fillCell(currentRow, colMap.get("{{UNIT}}"), shortenUnit(item.getUnit()));
                      setAlignment(currentRow, colMap.get("{{UNIT}}"), HorizontalAlignment.CENTER);
                      fillCell(currentRow, colMap.get("{{RATE}}"), String.format("%.2f", item.getRate()));
                      setAlignment(currentRow, colMap.get("{{RATE}}"), HorizontalAlignment.CENTER);
                      fillCell(currentRow, colMap.get("{{AMOUNT}}"), String.format("%.2f", item.getAmount()));
                      setAlignment(currentRow, colMap.get("{{AMOUNT}}"), HorizontalAlignment.CENTER);
                  }
             } else {
                 sheet.removeRow(sourceRow);
             }
        }

        // 2.1 Handle Charges Row
        int chargeRowIndex = -1;
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String val = cell.getStringCellValue();
                    if (val.contains("{{CHARGE_NAME}}") || val.contains("{{OTHER_CHARGES}}") || val.contains("{{PARTICULARS}}") || val.contains("{{ACCOUNT}}")) {
                        chargeRowIndex = row.getRowNum();
                        break;
                    }
                }
            }
            if (chargeRowIndex != -1) break;
        }

        if (chargeRowIndex != -1) {
             Row sourceRow = sheet.getRow(chargeRowIndex);
             Map<String, Integer> colMap = new HashMap<>();
             for (Cell cell : sourceRow) {
                 if (cell.getCellType() == CellType.STRING) {
                     String text = cell.getStringCellValue();
                     if (text.contains("{{CHARGE_NAME}}") || text.contains("{{OTHER_CHARGES}}") || text.contains("{{PARTICULARS}}") || text.contains("{{ACCOUNT}}")) colMap.put("{{CHARGE_NAME}}", cell.getColumnIndex());
                     if (text.contains("{{CHARGE_AMOUNT}}") || text.contains("{{AMOUNT}}")) colMap.put("{{CHARGE_AMOUNT}}", cell.getColumnIndex());
                     if (text.contains("{{CHARGE_RATE}}") || text.contains("{{RATE}}")) colMap.put("{{CHARGE_RATE}}", cell.getColumnIndex());
                 }
             }
             
             java.util.List<InvoiceCharge> charges = invoice.getExtraCharges();
             int chargeCount = charges != null ? charges.size() : 0;
             if (chargeCount > 0) {
                  if (chargeCount > 1) {
                      sheet.shiftRows(chargeRowIndex + 1, sheet.getLastRowNum(), chargeCount - 1);
                  }
                  for (int i = 0; i < chargeCount; i++) {
                      InvoiceCharge charge = charges.get(i);
                      Row currentRow = (i == 0) ? sourceRow : sheet.createRow(chargeRowIndex + i);
                      if (i > 0) {
                          for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                              Cell src = sourceRow.getCell(j);
                              Cell newC = currentRow.createCell(j);
                              if(src != null) {
                                  newC.setCellStyle(src.getCellStyle());
                              }
                          }
                          copyMergedRegions(sheet, chargeRowIndex, currentRow.getRowNum());
                      }
                      fillCell(currentRow, colMap.get("{{CHARGE_NAME}}"), charge.getChargeName());
                      setAlignment(currentRow, colMap.get("{{CHARGE_NAME}}"), HorizontalAlignment.RIGHT);
                      fillCell(currentRow, colMap.get("{{CHARGE_AMOUNT}}"), String.format("%.2f", charge.getAmount()));
                      setAlignment(currentRow, colMap.get("{{CHARGE_AMOUNT}}"), HorizontalAlignment.CENTER);
                      String rateStr = charge.isPercentage() ? charge.getRate() + "%" : "";
                      fillCell(currentRow, colMap.get("{{CHARGE_RATE}}"), rateStr);
                      setAlignment(currentRow, colMap.get("{{CHARGE_RATE}}"), HorizontalAlignment.RIGHT);
                  }
             } else {
                 sheet.removeRow(sourceRow);
             }
        }

        // 3. Global Replace
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String text = cell.getStringCellValue();
                    if (data.containsKey(text)) {
                        cell.setCellValue(data.get(text));
                        if (text.contains("SUBTOTAL") || text.contains("TOTAL_TAX") || text.contains("GRAND_TOTAL") || text.contains("TOTAL_QTY")) {
                            setAlignment(row, cell.getColumnIndex(), HorizontalAlignment.CENTER);
                        }
                    }
                }
            }
        }
        
        // 4. Tax Breakdown Table
        int taxRowIndex = -1;
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && (cell.getStringCellValue().contains("{{TAX_SAC}}") || cell.getStringCellValue().contains("{{TAX_HSN}}"))) {
                    taxRowIndex = row.getRowNum();
                    break;
                }
            }
            if (taxRowIndex != -1) break;
        }
        
        if (taxRowIndex != -1) {
             Row sourceRow = sheet.getRow(taxRowIndex);
             Map<String, Integer> colMap = new HashMap<>();
             for (Cell cell : sourceRow) {
                 if (cell.getCellType() == CellType.STRING) {
                     String text = cell.getStringCellValue();
                     if (text.contains("{{")) colMap.put(text, cell.getColumnIndex());
                 }
             }
             Map<String, TaxSummary> taxSummary = calculateTaxSummary(invoice);
             int count = taxSummary.size();
             if (count > 0) {
                  if (count > 1) {
                      sheet.shiftRows(taxRowIndex + 1, sheet.getLastRowNum(), count - 1);
                  }
                  int i = 0;
                  for (Map.Entry<String, TaxSummary> entry : taxSummary.entrySet()) {
                       TaxSummary summary = entry.getValue();
                       Row currentRow = (i == 0) ? sourceRow : sheet.createRow(taxRowIndex + i);
                       if (i > 0) {
                           for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                                Cell src = sourceRow.getCell(j);
                                Cell newC = currentRow.createCell(j);
                                if(src != null) {
                                    newC.setCellStyle(src.getCellStyle());
                                }
                           }
                           copyMergedRegions(sheet, taxRowIndex, currentRow.getRowNum());
                       }
                       fillCell(currentRow, colMap.get("{{TAX_SAC}}"), summary.hsn);
                       fillCell(currentRow, colMap.get("{{TAX_HSN}}"), summary.hsn);
                       fillCell(currentRow, colMap.get("{{TAX_VAL}}"), String.format("%.2f", summary.taxableValue));
                       fillCell(currentRow, colMap.get("{{TAX_RATE}}"), summary.rate + "%");
                       
                       if (isIgst) {
                           fillCell(currentRow, colMap.get("{{TAX_CGST_RATE}}"), summary.rate + "%");
                           fillCell(currentRow, colMap.get("{{TAX_CGST_AMT}}"), String.format("%.2f", summary.totalTax));
                           fillCell(currentRow, colMap.get("{{TAX_SGST_RATE}}"), "");
                           fillCell(currentRow, colMap.get("{{TAX_SGST_AMT}}"), "");
                       } else {
                           fillCell(currentRow, colMap.get("{{TAX_CGST_RATE}}"), (summary.rate/2) + "%");
                           fillCell(currentRow, colMap.get("{{TAX_CGST_AMT}}"), String.format("%.2f", summary.cgstAmount));
                           fillCell(currentRow, colMap.get("{{TAX_SGST_RATE}}"), (summary.rate/2) + "%");
                           fillCell(currentRow, colMap.get("{{TAX_SGST_AMT}}"), String.format("%.2f", summary.sgstAmount));
                       }
                       fillCell(currentRow, colMap.get("{{TAX_TOTAL}}"), String.format("%.2f", summary.totalTax));
                       i++;
                  }
             } else {
                  for (Cell cell : sourceRow) cell.setCellValue("");
             }
        }
        
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        return workbook;
    }
    
    private void copyMergedRegions(Sheet sheet, int sourceRowNum, int targetRowNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == sourceRowNum && mergedRegion.getLastRow() == sourceRowNum) {
                CellRangeAddress newMergedRegion = new CellRangeAddress(
                        targetRowNum,
                        targetRowNum,
                        mergedRegion.getFirstColumn(),
                        mergedRegion.getLastColumn()
                );
                sheet.addMergedRegion(newMergedRegion);
            }
        }
    }

    private void fillCell(Row row, Integer colIndex, String value) {
        if (colIndex != null) {
            Cell cell = row.getCell(colIndex);
            if (cell == null) cell = row.createCell(colIndex);
            cell.setCellValue(value);
        }
    }

    private void setAlignment(Row row, Integer colIndex, HorizontalAlignment alignment) {
        if (colIndex != null) {
            Cell cell = row.getCell(colIndex);
            if (cell == null) cell = row.createCell(colIndex);
            CellStyle oldStyle = cell.getCellStyle();
            CellStyle newStyle = row.getSheet().getWorkbook().createCellStyle();
            newStyle.cloneStyleFrom(oldStyle);
            newStyle.setAlignment(alignment);
            cell.setCellStyle(newStyle);
        }
    }

    private String shortenUnit(String unit) {
        if (unit == null || unit.isEmpty()) return "";
        String s = unit.trim().toLowerCase();
        if (s.equals("piece") || s.equals("pieces") || s.equals("each") || s.equals("pcs")) return "pc";
        if (s.equals("numbers") || s.equals("number") || s.equals("nos")) return "nos";
        if (s.equals("kilogram") || s.equals("kilograms") || s.equals("kg")) return "kg";
        if (s.equals("gram") || s.equals("grams") || s.equals("gm")) return "gm";
        if (s.equals("meter") || s.equals("meters") || s.equals("mtr")) return "mtr";
        return unit; 
    }

    private String checkNull(String s) {
        return s == null ? "" : s;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private void createTotalRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(5);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        Cell valueCell = row.createCell(6);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private Map<String, String> fetchCompanyData() {
        Map<String, String> map = new HashMap<>();
        android.content.SharedPreferences prefs = context.getSharedPreferences("TellyPrefs", Context.MODE_PRIVATE);
        int companyId = prefs.getInt("selected_company_id", 0);
        DatabaseHelper db = new DatabaseHelper(context);
        android.database.Cursor cursor = db.getCompany(companyId);
        if (cursor != null && cursor.moveToFirst()) {
            map.put("NAME", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_NAME))));
            map.put("ADDRESS", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_ADDRESS))));
            map.put("GST", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_GST))));
            map.put("LOGO", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_LOGO))));
            map.put("MOBILE", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_MOBILE))));
            map.put("EMAIL", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_EMAIL))));
            map.put("STATE", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_STATE))));
            try {
                 int panIdx = cursor.getColumnIndex("company_pan");
                 if (panIdx != -1) map.put("PAN", checkNull(cursor.getString(panIdx)));
            } catch (Exception e) {}
            cursor.close();
        }
        return map;
    }
    
    private Map<String, String> fetchBankDetails(int ledgerId) {
        Map<String, String> map = new HashMap<>();
        DatabaseHelper db = new DatabaseHelper(context);
        android.database.Cursor cursor = db.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, null, "_id=?", new String[]{String.valueOf(ledgerId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            map.put("NAME", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BANK_NAME))));
            map.put("ACCOUNT_NO", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BANK_ACCOUNT_NO))));
            map.put("IFSC", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BANK_IFSC))));
            map.put("BRANCH", checkNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BANK_BRANCH))));
        }
        if (cursor != null) cursor.close();
        return map;
    }

    private static class TaxSummary {
        double rate;
        double taxableValue;
        double cgstAmount;
        double sgstAmount;
        double igstAmount;
        double totalTax;
        String hsn;
        TaxSummary(double rate, String hsn) { this.rate = rate; this.hsn = hsn; }
    }
    
    private Map<String, TaxSummary> calculateTaxSummary(Invoice invoice) {
        Map<String, TaxSummary> map = new HashMap<>(); // Key: rate_hsn
        double totalTaxableValue = 0;
        
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                double rate = item.getGstRate();
                String hsn = item.getHsn() != null ? item.getHsn() : "";
                String key = rate + "_" + hsn;
                
                TaxSummary summary = map.get(key);
                if (summary == null) {
                    summary = new TaxSummary(rate, hsn);
                    map.put(key, summary);
                }
                summary.taxableValue += item.getAmount();
                summary.cgstAmount += item.getCgstAmount();
                summary.sgstAmount += item.getSgstAmount();
                summary.totalTax += (item.getCgstAmount() + item.getSgstAmount() + item.getIgstAmount());
                totalTaxableValue += item.getAmount();
            }
        }
        
        // Distribution of tax from charges if items have no tax
        boolean itemTaxFound = false;
        for (TaxSummary s : map.values()) { if (s.totalTax > 0) { itemTaxFound = true; break; } }
        
        if (!itemTaxFound && invoice.getExtraCharges() != null && totalTaxableValue > 0) {
            double cIgst = 0, cCgst = 0, cSgst = 0, cUtgst = 0;
            double rIgst = 0, rCgst = 0, rSgst = 0, rUtgst = 0;
            
            // Re-detect tax type for this local scope
            boolean igstMode = false;
            for (InvoiceCharge charge : invoice.getExtraCharges()) {
                if (charge.getChargeName().toUpperCase().contains("IGST")) { igstMode = true; break; }
            }

            for (InvoiceCharge charge : invoice.getExtraCharges()) {
                String name = charge.getChargeName().toUpperCase();
                double amt = charge.getAmount();
                double rate = charge.getRate();
                
                if (name.contains("IGST")) { cIgst += amt; rIgst = Math.max(rIgst, rate); }
                else if (name.contains("CGST")) { cCgst += amt; rCgst = Math.max(rCgst, rate); }
                else if (name.contains("SGST")) { cSgst += amt; rSgst = Math.max(rSgst, rate); }
                else if (name.contains("UTGST")) { cUtgst += amt; rUtgst = Math.max(rUtgst, rate); }
                else if (name.contains("GST") || name.contains("TAX") || name.contains("DUTY")) {
                    if (igstMode) { cIgst += amt; rIgst = Math.max(rIgst, rate); }
                    else {
                        // Default to split tax if not explicitly IGST
                        cCgst += amt / 2;
                        cSgst += amt / 2;
                        rCgst = Math.max(rCgst, rate / 2);
                        rSgst = Math.max(rSgst, rate / 2);
                    }
                }
            }
            
            if (cIgst > 0 || cCgst > 0 || cSgst > 0 || cUtgst > 0) {
                for (TaxSummary summary : map.values()) {
                    double ratio = summary.taxableValue / totalTaxableValue;
                    summary.igstAmount = cIgst * ratio;
                    summary.cgstAmount = cCgst * ratio;
                    summary.sgstAmount = (cSgst + cUtgst) * ratio;
                    summary.totalTax = summary.igstAmount + summary.cgstAmount + summary.sgstAmount;
                    if (summary.rate == 0) summary.rate = rIgst + rCgst + rSgst + rUtgst;
                }
            }
        }
        return map;
    }
    
    private String convertToWords(double doubleValue) {
        long amount = (long) doubleValue;
        long paise = Math.round((doubleValue - amount) * 100);
        String words = numToWords(amount);
        if (paise > 0) words += " and " + numToWords(paise) + " Paise";
        return words;
    }
    
    private static final String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen" };
    private static final String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

    private String numToWords(long n) {
        if (n < 0) return "Minus " + numToWords(-n);
        if (n == 0) return "Zero";
        if (n < 20) return units[(int)n];
        if (n < 100) return tens[(int)n / 10] + ((n % 10 != 0) ? " " : "") + units[(int)n % 10];
        if (n < 1000) return units[(int)n / 100] + " Hundred" + ((n % 100 != 0) ? " " : "") + numToWords(n % 100);
        if (n < 100000) return numToWords(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "") + numToWords(n % 1000);
        if (n < 10000000) return numToWords(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + numToWords(n % 100000);
        return numToWords(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + numToWords(n % 10000000);
    }

    public Workbook processManualJournal(Invoice invoice) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Journal Voucher");
        PrintSetup ps = sheet.getPrintSetup();
        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 1);

        sheet.setColumnWidth(0, 1500); // Sl
        sheet.setColumnWidth(1, 8000); // Particulars
        sheet.setColumnWidth(2, 4000); // Debit
        sheet.setColumnWidth(3, 4000); // Credit

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.cloneStyleFrom(dataStyle);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle rightStyle = workbook.createCellStyle();
        rightStyle.cloneStyleFrom(dataStyle);
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);

        int rowNum = 0;
        Map<String, String> companyData = fetchCompanyData();

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("JOURNAL VOUCHER");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        titleCell.setCellStyle(headerStyle);

        Row compNameRow = sheet.createRow(rowNum++);
        createCell(compNameRow, 0, companyData.getOrDefault("NAME", "Company Name"), null);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));
        
        Row invRow = sheet.createRow(rowNum++);
        createCell(invRow, 0, "Voucher No: " + invoice.getInvoiceNumber(), null);
        createCell(invRow, 2, "Date: " + invoice.getDate(), null);
        rowNum++;

        Row header = sheet.createRow(rowNum++);
        String[] cols = {"Sl.", "Particulars", "Debit", "Credit"};
        for(int i=0; i<cols.length; i++) createCell(header, i, cols[i], headerStyle);

        double totalDr = 0, totalCr = 0;
        if (invoice.getExtraCharges() != null) {
            int sl = 1;
            for (InvoiceCharge charge : invoice.getExtraCharges()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, String.valueOf(sl++), centerStyle);
                
                // We use InvoiceCharge as a proxy for VoucherCharge here
                // Logic based on naming conventions or amount sign could work, 
                // but let's assume chargeName contains Dr/Cr if possible or just use side logic.
                // For Journal, we usually have a way to distinguish.
                boolean isDr = charge.getChargeName().startsWith("By ");
                String name = charge.getChargeName();
                
                createCell(row, 1, name, dataStyle);
                if (isDr) {
                    createCell(row, 2, String.format("%.2f", charge.getAmount()), rightStyle);
                    createCell(row, 3, "", dataStyle);
                    totalDr += charge.getAmount();
                } else {
                    createCell(row, 2, "", dataStyle);
                    createCell(row, 3, String.format("%.2f", charge.getAmount()), rightStyle);
                    totalCr += charge.getAmount();
                }
            }
        }
        
        Row footer = sheet.createRow(rowNum++);
        createCell(footer, 1, "Total:", headerStyle);
        createCell(footer, 2, String.format("%.2f", totalDr), headerStyle);
        createCell(footer, 3, String.format("%.2f", totalCr), headerStyle);

        rowNum += 2;
        Row narRow = sheet.createRow(rowNum++);
        createCell(narRow, 0, "Narration: " + checkNull(invoice.getDeliveryNote()), null);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        return workbook;
    }

    public Workbook processManual(Invoice invoice) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoice");
        PrintSetup ps = sheet.getPrintSetup();
        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 1);

        sheet.setColumnWidth(0, 1500); 
        sheet.setColumnWidth(1, 6000); 
        sheet.setColumnWidth(2, 3000); 
        sheet.setColumnWidth(3, 2000); 
        sheet.setColumnWidth(4, 3000); 
        sheet.setColumnWidth(5, 3000); 
        sheet.setColumnWidth(6, 4000); 

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setWrapText(true);
        
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.cloneStyleFrom(dataStyle);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle rightStyle = workbook.createCellStyle();
        rightStyle.cloneStyleFrom(dataStyle);
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle labelStyle = workbook.createCellStyle();
        labelStyle.cloneStyleFrom(dataStyle);
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelStyle.setFont(labelFont);

        int rowNum = 0;
        Map<String, String> companyData = fetchCompanyData();

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TAX INVOICE");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6)); 
        rowNum++; 

        Row compNameRow = sheet.createRow(rowNum++);
        createCell(compNameRow, 0, companyData.getOrDefault("NAME", "Company Name"), null);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));
        
        Row invRow = sheet.createRow(rowNum++);
        createCell(invRow, 0, "Invoice No: " + invoice.getInvoiceNumber(), null);
        createCell(invRow, 3, "Date: " + invoice.getDate(), null);
        rowNum++;

        Row header = sheet.createRow(rowNum++);
        String[] cols = {"Sl.", "Description", "HSN", "GST", "Qty", "Rate", "Amount"};
        for(int i=0; i<cols.length; i++) createCell(header, i, cols[i], headerStyle);

        double totalQty = 0;
        if (invoice.getItems() != null) {
            int sl = 1;
            for (InvoiceItem item : invoice.getItems()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, String.valueOf(sl++), centerStyle);
                createCell(row, 1, item.getItemName(), dataStyle);
                createCell(row, 2, item.getHsn(), centerStyle);
                createCell(row, 3, item.getGstRate() + "%", centerStyle);
                createCell(row, 4, String.valueOf(item.getQuantity()) + " " + shortenUnit(item.getUnit()), centerStyle);
                createCell(row, 5, String.format("%.2f", item.getRate()), centerStyle);
                createCell(row, 6, String.format("%.2f", item.getAmount()), centerStyle);
                totalQty += item.getQuantity();
            }
        }
        
        Row tqRow = sheet.createRow(rowNum++);
        createCell(tqRow, 3, "Total Qty:", labelStyle);
        createCell(tqRow, 4, String.valueOf(totalQty), centerStyle);
        setAlignment(tqRow, 3, HorizontalAlignment.RIGHT);
        
        rowNum++;
        createTotalRow(sheet, rowNum++, "Subtotal:", String.format("%.2f", invoice.getTotalAmount()), dataStyle);
        createTotalRow(sheet, rowNum++, "Total Tax:", String.format("%.2f", invoice.getTotalTaxAmount()), dataStyle);
        createTotalRow(sheet, rowNum++, "Grand Total:", String.format("%.2f", invoice.getGrandTotal()), headerStyle);

        return workbook;
    }
    
    private void saveAndOpen(Workbook workbook, String invoiceNo) throws IOException {
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
        if (!directory.exists()) directory.mkdirs();
        File file = new File(directory, "Excel_Invoice_" + invoiceNo + ".xlsx");
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        workbook.close();
        fos.close();
        showToast("Excel Saved: " + file.getAbsolutePath());
        openExcel(file);
    }
    
    private void openExcel(File file) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Open Excel with"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
