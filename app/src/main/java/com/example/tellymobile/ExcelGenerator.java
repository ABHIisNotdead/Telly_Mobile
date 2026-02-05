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
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
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
        // Check if template exists
        boolean hasTemplate = false;
        try {
             InputStream is = context.getAssets().open(TEMPLATE_FILENAME);
             is.close();
             hasTemplate = true;
        } catch (IOException e) {
            hasTemplate = false;
        }

        if (hasTemplate) {
            return processTemplate(invoice);
        } else {
            return processManual(invoice);
        }
    }

    private Workbook processTemplate(Invoice invoice) throws IOException {
        InputStream is = context.getAssets().open(TEMPLATE_FILENAME);
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        
        // A4 Paper Size
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);

        // 1. Prepare Data Map
        Map<String, String> data = new HashMap<>();
        data.put("{{INVOICE_NO}}", invoice.getInvoiceNumber());
        data.put("{{DATE}}", invoice.getDate());
        data.put("{{CUSTOMER_NAME}}", invoice.getCustomerName());
        data.put("{{SUBTOTAL}}", String.format("%.2f", invoice.getTotalAmount()));
        data.put("{{TOTAL_TAX}}", String.format("%.2f", invoice.getTotalTaxAmount()));
        // Note: Delivery Charges used for "Other Charges"
        data.put("{{OTHER_CHARGES}}", String.format("%.2f", invoice.getDeliveryCharges()));

        // Dispatch Details
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

        // Buyer Details
        data.put("{{BUYER_ADDRESS}}", checkNull(invoice.getBuyerAddress()));
        data.put("{{BUYER_GST}}", checkNull(invoice.getBuyerGst()));
        data.put("{{BUYER_STATE}}", checkNull(invoice.getBuyerState()));

        // Consignee Details
        data.put("{{CONSIGNEE_NAME}}", checkNull(invoice.getConsigneeName()));
        data.put("{{CONSIGNEE_ADDRESS}}", checkNull(invoice.getConsigneeAddress()));
        data.put("{{CONSIGNEE_GST}}", checkNull(invoice.getConsigneeGst()));
        data.put("{{CONSIGNEE_STATE}}", checkNull(invoice.getConsigneeState()));
        data.put("{{GRAND_TOTAL}}", String.format("%.2f", invoice.getGrandTotal()));
        data.put("{{AMOUNT_WORDS}}", "INR " + String.format("%.2f", invoice.getGrandTotal()) + " Only"); 

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
             
             // Map columns
             for (Cell cell : sourceRow) {
                 if (cell.getCellType() == CellType.STRING) {
                     String text = cell.getStringCellValue();
                     if (text.contains("{{")) {
                        colMap.put(text, cell.getColumnIndex());
                     }
                 }
             }
             
             int itemCount = invoice.getItems() != null ? invoice.getItems().size() : 0;
             if (itemCount > 0) {
                  // Shift rows
                  if (itemCount > 1) {
                      sheet.shiftRows(itemRowIndex + 1, sheet.getLastRowNum(), itemCount - 1);
                  }
                  
                  for (int i = 0; i < itemCount; i++) {
                      InvoiceItem item = invoice.getItems().get(i);
                      Row currentRow = (i == 0) ? sourceRow : sheet.createRow(itemRowIndex + i);
                      
                      // Copy styles if new row
                      if (i > 0) {
                          for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
                              Cell src = sourceRow.getCell(j);
                              if(src != null) {
                                  Cell newC = currentRow.createCell(j);
                                  newC.setCellStyle(src.getCellStyle());
                              }
                          }
                      }
                      
                      // Fill Data using Map
                      fillCell(currentRow, colMap.get("{{ITEM_NAME}}"), item.getItemName());
                      fillCell(currentRow, colMap.get("{{HSN}}"), item.getHsn());
                      fillCell(currentRow, colMap.get("{{GST}}"), item.getGstRate() + "%");
                      fillCell(currentRow, colMap.get("{{QTY}}"), String.valueOf(item.getQuantity()));
                      fillCell(currentRow, colMap.get("{{RATE}}"), String.format("%.2f", item.getRate()));
                      fillCell(currentRow, colMap.get("{{AMOUNT}}"), String.format("%.2f", item.getAmount()));
                  }
             } else {
                 sheet.removeRow(sourceRow);
             }
        }

        // 3. Global Replace (excluding items if needed layout, but global works for rest)
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String text = cell.getStringCellValue();
                    if (data.containsKey(text)) {
                        cell.setCellValue(data.get(text));
                    }
                }
            }
        }
        
        // Recalculate Formulas
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

        return workbook;
    }
    
    private void fillCell(Row row, Integer colIndex, String value) {
        if (colIndex != null) {
            Cell cell = row.getCell(colIndex);
            if (cell == null) cell = row.createCell(colIndex);
            cell.setCellValue(value);
    }
    }

    private String checkNull(String s) {
        return s == null ? "" : s;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createTotalRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(4);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        
        Cell valueCell = row.createCell(5);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private Workbook processManual(Invoice invoice) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoice");
        
        // A4 Paper Size
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);

        // Styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TAX INVOICE");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5)); 

        // Company Info
        Row companyRow = sheet.createRow(rowNum++);
        createCell(companyRow, 0, "SHREE AMBEY PACKAGING", titleStyle); 
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
        
        Row addrRow = sheet.createRow(rowNum++);
        createCell(addrRow, 0, "Vasai East, Mumbai", dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));
        
        rowNum++; 

        // Customer Info
        Row infoRow1 = sheet.createRow(rowNum++);
        createCell(infoRow1, 0, "Customer Name:", headerStyle);
        createCell(infoRow1, 1, invoice.getCustomerName(), dataStyle);
        createCell(infoRow1, 3, "Invoice No:", headerStyle);
        createCell(infoRow1, 4, invoice.getInvoiceNumber(), dataStyle);

        Row infoRow2 = sheet.createRow(rowNum++);
        createCell(infoRow2, 0, "", dataStyle); 
        createCell(infoRow2, 1, "", dataStyle); 
        createCell(infoRow2, 3, "Date:", headerStyle);
        createCell(infoRow2, 4, invoice.getDate(), dataStyle);
        
        Row infoRow3 = sheet.createRow(rowNum++);
        createCell(infoRow3, 0, "Vehicle No:", headerStyle);
        createCell(infoRow3, 1, invoice.getMotorVehicleNo(), dataStyle);
        createCell(infoRow3, 3, "Bill of Lading:", headerStyle);
        createCell(infoRow3, 4, invoice.getBillOfLading(), dataStyle);
        
        rowNum++;

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] columns = {"Description", "HSN/SAC", "GST %", "Qty", "Rate", "Amount"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Items
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                Row row = sheet.createRow(rowNum++);
                
                createCell(row, 0, item.getItemName(), dataStyle);
                createCell(row, 1, item.getHsn(), dataStyle);
                createCell(row, 2, item.getGstRate() + "%", dataStyle);
                createCell(row, 3, String.valueOf(item.getQuantity()), dataStyle);
                createCell(row, 4, String.format("%.2f", item.getRate()), dataStyle);
                createCell(row, 5, String.format("%.2f", item.getAmount()), dataStyle);
            }
        }

        // Totals
        rowNum++;
        createTotalRow(sheet, rowNum++, "Subtotal:", String.format("%.2f", invoice.getTotalAmount()), dataStyle);
        createTotalRow(sheet, rowNum++, "Tax:", String.format("%.2f", invoice.getTotalTaxAmount()), dataStyle);
        createTotalRow(sheet, rowNum++, "Grand Total:", String.format("%.2f", invoice.getGrandTotal()), headerStyle);

        // Auto Size
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

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
        showToast("Opening Excel...");
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "No Excel Viewer found.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error opening Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
