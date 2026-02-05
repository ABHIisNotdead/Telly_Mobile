package com.example.tellymobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {

    private Context context;
    private static final String TAG = "PdfGenerator";
    
    // Page Config (A4)
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 20;
    
    private Paint textPaint;
    private Paint linePaint;

    public PdfGenerator(Context context) {
        this.context = context;
        initPaints();
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(10f); // Default font size
        textPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(0.5f);
        linePaint.setStyle(Paint.Style.STROKE);
    }
    
    private void showToast(final String message) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    public void generateAndOpenPdf(Invoice invoice) {
        new Thread(() -> {
            try {
                // 1. Get Workbook from ExcelGenerator
                ExcelGenerator excelGen = new ExcelGenerator(context);
                Workbook workbook = excelGen.createWorkbook(invoice);
                
                // 2. Create PDF
                PdfDocument document = new PdfDocument();
                
                // 3. Render Workbook to PDF
                renderWorkbookToPdf(workbook, document);
                
                // 4. Save
                String fileName = "Invoice_" + invoice.getInvoiceNumber() + ".pdf";
                File file = savePdf(document, fileName);
                
                document.close();
                workbook.close();
                
                // 5. Open
                showToast("PDF Saved: " + file.getAbsolutePath());
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> openPdf(file));

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error generating PDF: " + e.getMessage());
                Log.e(TAG, "Error generating PDF", e);
            }
        }).start();
    }

    private void renderWorkbookToPdf(Workbook workbook, PdfDocument document) {
        Sheet sheet = workbook.getSheetAt(0);
        
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        float currentY = MARGIN;
        float currentX = MARGIN;
        
        // Calculate Column Widths (simple conversion)
        // POI width is 1/256th of a char width. Lets approximate char width as 6pts?
        // A better way is to scale "Total Excel Width" to "Page Width - Margins"
        
        List<Float> colWidths = new ArrayList<>();
        int maxCol = 0;
        // Find max columns
        for(Row r : sheet) maxCol = Math.max(maxCol, r.getLastCellNum());
        
        // Calculate raw widths
        float totalExcelWidth = 0;
        for (int i = 0; i < maxCol; i++) {
            float w = sheet.getColumnWidth(i); // Units of 1/256 char
            colWidths.add(w);
            totalExcelWidth += w;
        }
        
        // Scale factor to fit page
        float printableWidth = PAGE_WIDTH - (2 * MARGIN);
        float scale = totalExcelWidth > 0 ? printableWidth / totalExcelWidth : 1f;
        
        // Convert widths to points
        List<Float> pdfColWidths = new ArrayList<>();
        for (float w : colWidths) {
            pdfColWidths.add(w * scale);
        }

        // Check for merged regions to skip rendering underlying cells
        // (Simplified: We just draw the first cell of a merge region with the total dimensions)
        
        for (Row row : sheet) {
            float rowHeight = row.getHeightInPoints();
            if (rowHeight  == -1) rowHeight = 15f; // Default if undefined
            
            // Check Page Break
            if (currentY + rowHeight > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                currentY = MARGIN;
            }
            
            currentX = MARGIN;
            
            for (int i = 0; i < maxCol; i++) {
                Cell cell = row.getCell(i);
                float cellWidth = (i < pdfColWidths.size()) ? pdfColWidths.get(i) : 0;
                
                if (cellWidth == 0) continue;

                // Check if this cell is part of a merge
                CellRangeAddress merge = getMergeRange(sheet, row.getRowNum(), i);
                
                if (merge != null) {
                    // Only draw if we are the top-left cell of the merge
                    if (merge.getFirstRow() == row.getRowNum() && merge.getFirstColumn() == i) {
                        float mergedWidth = 0;
                        float mergedHeight = 0; // Not calculating merged height across rows well here, assuming row-bound for simplicity or just calc width
                        
                        // Calculate total width of merge
                        for (int c = merge.getFirstColumn(); c <= merge.getLastColumn(); c++) {
                             if(c < pdfColWidths.size()) mergedWidth += pdfColWidths.get(c);
                        }
                        
                        // Note: Merged height across rows is hard without peeking ahead. 
                        // For simple forms, usually merges are within a row or we just draw the content in the first row's rect.
                        // Lets basically draw the content with the merged width.
                        
                        drawCell(canvas, cell, currentX, currentY, mergedWidth, rowHeight);
                        
                    }
                    // If not top-left, we skip drawing (placeholder)
                } else {
                    drawCell(canvas, cell, currentX, currentY, cellWidth, rowHeight);
                }
                
                currentX += cellWidth;
            }
            
            currentY += rowHeight;
        }
        
        document.finishPage(page);
    }
    
    private CellRangeAddress getMergeRange(Sheet sheet, int row, int col) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(row, col)) {
                return range;
            }
        }
        return null;
    }
    
    private void drawCell(Canvas canvas, Cell cell, float x, float y, float w, float h) {
        if (cell == null) return;
        
        String text = "";
        if (cell.getCellType() == CellType.STRING) text = cell.getStringCellValue();
        else if (cell.getCellType() == CellType.NUMERIC) text = String.valueOf(cell.getNumericCellValue());
        else if (cell.getCellType() == CellType.BOOLEAN) text = String.valueOf(cell.getBooleanCellValue());
        else if (cell.getCellType() == CellType.FORMULA) {
             try { text = cell.getStringCellValue(); } catch (Exception e) { text = String.valueOf(cell.getNumericCellValue()); }
        }
        
        if (!text.isEmpty()) {
            // Style
            CellStyle style = cell.getCellStyle();
            HorizontalAlignment align = style.getAlignment();
            
            float textX = x + 2; // Default Left padding
            float textY = y + h - 5; // Bottom padding approx
            
            // Measure text
            float textWidth = textPaint.measureText(text);
            
            if (align == HorizontalAlignment.CENTER) {
                textX = x + (w - textWidth) / 2;
            } else if (align == HorizontalAlignment.RIGHT) {
                textX = x + w - textWidth - 2;
            }
            
            // Draw
            canvas.drawText(text, textX, textY, textPaint);
        }
        
        // Borders (Simple box for now if borders exist - checking every border is expensive, lets just draw light grid for non-empty cells or all?)
        // To allow 'clean' look like print layout, maybe only draw if style says so.
        // For now, drawing all cell bounds helps debugging, but for production, rely on style.
        // style.getBorderBottom() != BorderStyle.NONE
        
        if (cell.getCellStyle().getBorderBottom() != org.apache.poi.ss.usermodel.BorderStyle.NONE) {
             canvas.drawRect(x, y, x + w, y + h, linePaint);
        }
    }
    
    private File savePdf(PdfDocument document, String fileName) throws IOException {
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
        if (!directory.exists()) directory.mkdirs();
        File file = new File(directory, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        fos.close();
        return file;
    }
    
    private void openPdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            Intent chooser = Intent.createChooser(intent, "Open PDF with");
            try {
                context.startActivity(chooser);
            } catch (android.content.ActivityNotFoundException ex) {
                showToast("No app found to open PDF");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error opening PDF");
        }
    }
}
