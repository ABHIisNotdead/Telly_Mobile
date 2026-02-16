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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
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

    public PdfGenerator(Context context) {
        this.context = context;
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
        
        // --- Render Logo if exists ---
        String logoPath = fetchCompanyLogo();
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                android.net.Uri logoUri = android.net.Uri.parse(logoPath);
                java.io.InputStream is = context.getContentResolver().openInputStream(logoUri);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                if (bitmap != null) {
                    float logoH = 40;
                    float logoW = (float) bitmap.getWidth() * logoH / bitmap.getHeight();
                    canvas.drawBitmap(bitmap, null, new RectF(MARGIN, currentY, MARGIN + logoW, currentY + logoH), null);
                }
                is.close();
            } catch (Exception e) {
                Log.e(TAG, "Logo error: " + e.getMessage());
            }
        }

        // Calculate Column Widths
        List<Float> colWidths = new ArrayList<>();
        int maxCol = 0;
        for(Row r : sheet) maxCol = Math.max(maxCol, r.getLastCellNum());
        
        float totalExcelWidth = 0;
        for (int i = 0; i < maxCol; i++) {
            float w = sheet.getColumnWidth(i); 
            colWidths.add(w);
            totalExcelWidth += w;
        }
        
        float printableWidth = PAGE_WIDTH - (2 * MARGIN);
        float scale = totalExcelWidth > 0 ? printableWidth / totalExcelWidth : 1f;
        
        List<Float> pdfColWidths = new ArrayList<>();
        for (float w : colWidths) {
            pdfColWidths.add(w * scale);
        }

        for (Row row : sheet) {
            float rowHeight = row.getHeightInPoints();
            if (rowHeight <= 0) rowHeight = 15f; 
            
            if (currentY + rowHeight > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                currentY = MARGIN;
            }
            
            float currentX = MARGIN;
            
            for (int i = 0; i < maxCol; i++) {
                Cell cell = row.getCell(i);
                float cellWidth = (i < pdfColWidths.size()) ? pdfColWidths.get(i) : 0;
                
                if (cellWidth <= 0) continue;

                CellRangeAddress merge = getMergeRange(sheet, row.getRowNum(), i);
                
                if (merge != null) {
                    if (merge.getFirstRow() == row.getRowNum() && merge.getFirstColumn() == i) {
                        float mergedWidth = 0;
                        for (int c = merge.getFirstColumn(); c <= merge.getLastColumn(); c++) {
                             if(c < pdfColWidths.size()) mergedWidth += pdfColWidths.get(c);
                        }
                        float mergedHeight = 0;
                        for (int r = merge.getFirstRow(); r <= merge.getLastRow(); r++) {
                            Row mRow = sheet.getRow(r);
                            float rh = (mRow != null) ? mRow.getHeightInPoints() : 15f;
                            if (rh <= 0) rh = 15f;
                            mergedHeight += rh;
                        }
                        drawCell(canvas, cell, currentX, currentY, mergedWidth, mergedHeight);
                    }
                } else {
                    drawCell(canvas, cell, currentX, currentY, cellWidth, rowHeight);
                }
                
                currentX += cellWidth;
            }
            
            currentY += rowHeight;
        }
        
        document.finishPage(page);
    }
    
    private String fetchCompanyLogo() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("TellyPrefs", Context.MODE_PRIVATE);
        int companyId = prefs.getInt("selected_company_id", 0);
        DatabaseHelper db = new DatabaseHelper(context);
        android.database.Cursor cursor = db.getCompany(companyId);
        String logo = null;
        if (cursor != null && cursor.moveToFirst()) {
            logo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_LOGO));
            cursor.close();
        }
        return logo;
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
        
        CellStyle style = cell.getCellStyle();
        
        // Background
        if (style.getFillForegroundColor() != 64 || style.getFillPattern() != FillPatternType.NO_FILL) { 
             Paint bgPaint = new Paint();
             bgPaint.setColor(0xFFF2F2F2); // Default to light gray for headers/filled areas
             bgPaint.setStyle(Paint.Style.FILL);
             canvas.drawRect(x, y, x + w, y + h, bgPaint);
        }

        // Borders - Use local paint for thread safety and dynamic weight
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);

        if (style.getBorderTop() != BorderStyle.NONE) {
            borderPaint.setStrokeWidth(getBorderWidth(style.getBorderTop()));
            canvas.drawLine(x, y, x + w, y, borderPaint);
        }
        if (style.getBorderBottom() != BorderStyle.NONE) {
            borderPaint.setStrokeWidth(getBorderWidth(style.getBorderBottom()));
            canvas.drawLine(x, y + h, x + w, y + h, borderPaint);
        }
        if (style.getBorderLeft() != BorderStyle.NONE) {
            borderPaint.setStrokeWidth(getBorderWidth(style.getBorderLeft()));
            canvas.drawLine(x, y, x, y + h, borderPaint);
        }
        if (style.getBorderRight() != BorderStyle.NONE) {
            borderPaint.setStrokeWidth(getBorderWidth(style.getBorderRight()));
            canvas.drawLine(x + w, y, x + w, y + h, borderPaint);
        }

        // Text
        String text = "";
        try {
            switch (cell.getCellType()) {
                case STRING: text = cell.getStringCellValue(); break;
                case NUMERIC: text = String.valueOf(cell.getNumericCellValue()); break;
                case BOOLEAN: text = String.valueOf(cell.getBooleanCellValue()); break;
                case FORMULA: 
                    try { text = cell.getStringCellValue(); } catch (Exception e) { text = String.valueOf(cell.getNumericCellValue()); }
                    break;
            }
        } catch (Exception e) {}

        if (!text.isEmpty()) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setAntiAlias(true);

            HorizontalAlignment align = style.getAlignment();
            int fontIndex = style.getFontIndex();
            org.apache.poi.ss.usermodel.Font font = cell.getSheet().getWorkbook().getFontAt(fontIndex);
            textPaint.setFakeBoldText(font.getBold());
            float fontSize = font.getFontHeightInPoints() > 0 ? font.getFontHeightInPoints() : 10f;
            textPaint.setTextSize(fontSize);
            
            if (style.getWrapText()) {
                android.text.TextPaint tp = new android.text.TextPaint(textPaint);
                android.text.Layout.Alignment layoutAlign = android.text.Layout.Alignment.ALIGN_NORMAL;
                if (align == HorizontalAlignment.CENTER) layoutAlign = android.text.Layout.Alignment.ALIGN_CENTER;
                else if (align == HorizontalAlignment.RIGHT) layoutAlign = android.text.Layout.Alignment.ALIGN_OPPOSITE;

                android.text.StaticLayout layout = android.text.StaticLayout.Builder.obtain(text, 0, text.length(), tp, (int) (w - 4))
                        .setAlignment(layoutAlign)
                        .setLineSpacing(0, 1)
                        .setIncludePad(false)
                        .build();
                
                canvas.save();
                canvas.translate(x + 2, y + 2);
                layout.draw(canvas);
                canvas.restore();
            } else {
                float textWidth = textPaint.measureText(text);
                float textX = x + 2;
                if (align == HorizontalAlignment.CENTER) textX = x + (w - textWidth) / 2;
                else if (align == HorizontalAlignment.RIGHT) textX = x + w - textWidth - 2;
                
                float textY = y + (h / 2) + (fontSize / 2) - 1;
                canvas.drawText(text, textX, textY, textPaint);
            }
        }
    }

    private float getBorderWidth(BorderStyle style) {
        switch (style) {
            case THIN: return 0.5f;
            case MEDIUM: return 1.2f;
            case THICK: return 2.0f;
            case DOUBLE: return 1.5f;
            default: return 0.5f;
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
