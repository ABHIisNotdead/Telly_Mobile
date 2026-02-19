package com.example.tellymobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "TellyMobile.db";
    private static final int DATABASE_VERSION = 37; // Added Item Cost and Low Stock Limit


    // ...

    // Ledgers Table
    public static final String TABLE_NAME = "ledgers";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_GROUP = "group_name";
    public static final String COLUMN_MOBILE = "mobile";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_GST = "gst";
    public static final String COLUMN_BALANCE = "balance";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TAX_RATE = "tax_rate"; // New
    public static final String COLUMN_IS_PERCENTAGE = "is_percentage"; // New
    public static final String COLUMN_BANK_NAME = "bank_name";
    public static final String COLUMN_BANK_ACCOUNT_NO = "bank_account_no";
    public static final String COLUMN_BANK_IFSC = "bank_ifsc";
    public static final String COLUMN_BANK_BRANCH = "bank_branch";
    public static final String COLUMN_COMPANY_ID = "company_id"; // Global Company ID

    // Companies Table
    public static final String TABLE_COMPANIES = "companies";
    public static final String COLUMN_COMPANY_NAME = "company_name";
    public static final String COLUMN_COMPANY_ADDRESS = "company_address";
    public static final String COLUMN_COMPANY_MOBILE = "company_mobile";
    public static final String COLUMN_COMPANY_PHONE2 = "company_phone2"; // New
    public static final String COLUMN_COMPANY_EMAIL = "company_email";
    public static final String COLUMN_COMPANY_STATE = "company_state";   // New
    public static final String COLUMN_COMPANY_LOGO = "company_logo";     // New Logo URI
    public static final String COLUMN_COMPANY_GST = "company_gst";
    public static final String COLUMN_COMPANY_TAGLINE = "company_tagline";
    public static final String COLUMN_COMPANY_CST = "company_cst";
    public static final String COLUMN_COMPANY_TIN = "company_tin";
    public static final String COLUMN_COMPANY_VAT_TIN = "company_vat_tin";

    // ...



    // ...

    public void addLedger(String name, String group, String mobile, String email, String address, String gst, double balance, String type, double taxRate, boolean isPercentage, String bankName, String accNo, String ifsc, String branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_GROUP, group);
        cv.put(COLUMN_MOBILE, mobile);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_ADDRESS, address);
        cv.put(COLUMN_GST, gst);
        cv.put(COLUMN_BALANCE, balance);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_TAX_RATE, taxRate);
        cv.put(COLUMN_IS_PERCENTAGE, isPercentage ? 1 : 0);
        cv.put(COLUMN_BANK_NAME, bankName);
        cv.put(COLUMN_BANK_ACCOUNT_NO, accNo);
        cv.put(COLUMN_BANK_IFSC, ifsc);
        cv.put(COLUMN_BANK_BRANCH, branch);
        db.insert(TABLE_NAME, null, cv);
    }
    
    // Kept for backward compatibility if needed, or update callers
    public void addLedger(String name, String group, String mobile, String email, String address, String gst, double balance, String type, double taxRate, boolean isPercentage) {
        addLedger(name, group, mobile, email, address, gst, balance, type, taxRate, isPercentage, "", "", "", "");
    }
    
    public void updateLedger(int id, String name, String group, String mobile, String email, String address, String gst, double balance, String type, double taxRate, boolean isPercentage, String bankName, String accNo, String ifsc, String branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_GROUP, group);
        cv.put(COLUMN_MOBILE, mobile);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_ADDRESS, address);
        cv.put(COLUMN_GST, gst);
        cv.put(COLUMN_BALANCE, balance);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_TAX_RATE, taxRate);
        cv.put(COLUMN_IS_PERCENTAGE, isPercentage ? 1 : 0);
        cv.put(COLUMN_BANK_NAME, bankName);
        cv.put(COLUMN_BANK_ACCOUNT_NO, accNo);
        cv.put(COLUMN_BANK_IFSC, ifsc);
        cv.put(COLUMN_BANK_BRANCH, branch);
        db.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public void updateLedger(int id, String name, String group, String mobile, String email, String address, String gst, double balance, String type, double taxRate, boolean isPercentage) {
        updateLedger(id, name, group, mobile, email, address, gst, balance, type, taxRate, isPercentage, "", "", "", "");
    }
    
    public Cursor getLedger(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_NAME + " as ledger_name, " +
                       COLUMN_GROUP + " as ledger_group, " +
                       COLUMN_MOBILE + ", " + COLUMN_EMAIL + ", " + COLUMN_ADDRESS + ", " + COLUMN_GST + ", " +
                       COLUMN_BALANCE + " as opening_balance, " + 
                       COLUMN_TYPE + ", " + COLUMN_TAX_RATE + ", " + COLUMN_IS_PERCENTAGE + ", " +
                       COLUMN_BANK_NAME + ", " + COLUMN_BANK_ACCOUNT_NO + ", " + COLUMN_BANK_IFSC + ", " + COLUMN_BANK_BRANCH +
                       " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Common Column Names
    public static final String COLUMN_ID = "_id";

    // Groups Table
    public static final String TABLE_GROUPS = "groups";
    public static final String COLUMN_GROUP_ID = "_id";
    public static final String COLUMN_GROUP_NAME = "name";
    public static final String COLUMN_GROUP_PARENT = "parent";

    // Stock Groups Table (Inventory)
    public static final String TABLE_STOCK_GROUPS = "stock_groups";
    public static final String COLUMN_STOCK_GROUP_ID = "_id";
    public static final String COLUMN_STOCK_GROUP_NAME = "name";
    public static final String COLUMN_STOCK_GROUP_PARENT = "parent";

    // Stock Categories Table (Inventory)
    public static final String TABLE_STOCK_CATEGORIES = "stock_categories";
    public static final String COLUMN_STOCK_CAT_ID = "_id";
    public static final String COLUMN_STOCK_CAT_NAME = "name";
    public static final String COLUMN_STOCK_CAT_PARENT = "parent";

    // Ledgers Table


    // Items Table
    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ITEM_ID = "_id";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_ITEM_RATE = "rate";
    public static final String COLUMN_ITEM_UNIT = "unit";
    public static final String COLUMN_ITEM_STOCK = "stock";
    public static final String COLUMN_ITEM_HSN = "hsn";
    public static final String COLUMN_ITEM_GROUP = "group_name";
    public static final String COLUMN_ITEM_CATEGORY = "category";
    public static final String COLUMN_ITEM_GST_RATE = "gst_rate"; // Added
    public static final String COLUMN_ITEM_GST_TYPE = "gst_type"; // Added (Percentage/Amount)
    public static final String COLUMN_ITEM_COST = "cost_price"; // New
    public static final String COLUMN_ITEM_LOW_STOCK_LIMIT = "low_stock_limit"; // New

    // Purchases Table
    public static final String TABLE_PURCHASES = "purchases";
    public static final String COLUMN_PURCHASE_ID = "_id";
    public static final String COLUMN_PURCHASE_INV_NO = "purchase_inv_no";
    public static final String COLUMN_PURCHASE_DATE = "purchase_date";
    public static final String COLUMN_SUPPLIER_INV_NO = "supplier_inv_no"; // New
    public static final String COLUMN_SUPPLIER_INV_DATE = "supplier_inv_date";
    public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
    public static final String COLUMN_SUPPLIER_CST = "supplier_cst";
    public static final String COLUMN_SUPPLIER_TIN = "supplier_tin";
    public static final String COLUMN_BUYER_VAT_TIN = "buyer_vat_tin";
    public static final String COLUMN_PURCHASE_TOTAL = "purchase_total";

    // Purchase Items Table
    public static final String TABLE_PURCHASE_ITEMS = "purchase_items";
    public static final String COLUMN_PUR_ITEM_ID = "_id";
    public static final String COLUMN_PUR_ID_FK = "purchase_id";
    public static final String COLUMN_PUR_ITEM_NAME = "item_name";
    public static final String COLUMN_PUR_QTY = "quantity";
    public static final String COLUMN_PUR_RATE = "rate";
    public static final String COLUMN_PUR_AMOUNT = "amount";
    public static final String COLUMN_PUR_UNIT = "unit"; // Added
    public static final String COLUMN_PUR_HSN = "hsn"; // Added

    // Invoices Table (Sales)
    public static final String TABLE_INVOICES = "invoices";
    public static final String COLUMN_INVOICE_ID = "_id";
    public static final String COLUMN_INVOICE_NO = "invoice_number";
    public static final String COLUMN_INVOICE_DATE = "date";
    public static final String COLUMN_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_TOTAL_AMOUNT = "total_amount"; // Taxable Subtotal
    public static final String COLUMN_DELIVERY_CHARGES = "delivery_charges"; // New
    public static final String COLUMN_TOTAL_TAX = "total_tax"; // New
    public static final String COLUMN_GRAND_TOTAL = "grand_total";
    
    // New Invoice Columns
    public static final String COLUMN_DELIVERY_NOTE = "delivery_note";
    public static final String COLUMN_MODE_PAYMENT = "mode_payment";
    public static final String COLUMN_REF_NO = "reference_no";
    public static final String COLUMN_OTHER_REF = "other_references";
    public static final String COLUMN_BUYER_ORDER_NO = "buyers_order_no";
    public static final String COLUMN_DISPATCH_DOC_NO = "dispatch_doc_no";
    public static final String COLUMN_DELIVERY_NOTE_DATE = "delivery_note_date";
    public static final String COLUMN_DISPATCH_THROUGH = "dispatch_through";
    public static final String COLUMN_DESTINATION = "destination";
    public static final String COLUMN_TERMS_DELIVERY = "terms_delivery";
    public static final String COLUMN_BILL_OF_LADING = "bill_of_lading";
    public static final String COLUMN_MOTOR_VEHICLE_NO = "motor_vehicle_no";
    
    public static final String COLUMN_CONSIGNEE_NAME = "consignee_name";
    public static final String COLUMN_CONSIGNEE_ADDR = "consignee_address";
    public static final String COLUMN_CONSIGNEE_GST = "consignee_gst";
    public static final String COLUMN_CONSIGNEE_STATE = "consignee_state";
    
    public static final String COLUMN_BUYER_ADDR = "buyer_address";
    public static final String COLUMN_BUYER_GST = "buyer_gst";
    public static final String COLUMN_BUYER_STATE = "buyer_state";

    // Invoice Items Table
    public static final String TABLE_INVOICE_ITEMS = "invoice_items";
    public static final String COLUMN_INV_ITEM_ID = "_id";
    public static final String COLUMN_INV_ID_FK = "invoice_id";
    public static final String COLUMN_INV_ITEM_NAME = "item_name";
    public static final String COLUMN_INV_QTY = "quantity";
    public static final String COLUMN_INV_RATE = "rate";
    public static final String COLUMN_INV_AMOUNT = "amount"; // Taxable Value
    public static final String COLUMN_INV_HSN = "hsn";
    public static final String COLUMN_INV_GST_RATE = "gst_rate"; // New
    public static final String COLUMN_INV_CGST = "cgst_amount"; // New
    public static final String COLUMN_INV_SGST = "sgst_amount"; // New
    public static final String COLUMN_UNIT = "unit"; // New Field
    
    // Voucher Charges Table
    public static final String TABLE_VOUCHER_CHARGES = "voucher_charges";
    public static final String COLUMN_CHARGE_ID = "_id";
    public static final String COLUMN_CHARGE_VOUCHER_ID = "voucher_id";
    public static final String COLUMN_CHARGE_VOUCHER_TYPE = "voucher_type";
    public static final String COLUMN_CHARGE_LEDGER_ID = "ledger_id"; // FK to ledgers
    public static final String COLUMN_CHARGE_LEDGER_NAME = "ledger_name"; // Store name for easier query
    public static final String COLUMN_CHARGE_AMOUNT = "amount";
    public static final String COLUMN_CHARGE_IS_PERCENTAGE = "is_percentage"; // 1 for true, 0 for false
    public static final String COLUMN_CHARGE_RATE = "rate"; // Percentage value
    public static final String COLUMN_CHARGE_IS_DEBIT = "is_debit"; // 1 for Dr, 0 for Cr
    public static final String COLUMN_CHARGE_PAYMENT_MODE = "payment_mode"; // New

    
    // Journal Table
    public static final String TABLE_JOURNALS = "journals";
    public static final String COLUMN_JOURNAL_ID = "_id";
    public static final String COLUMN_JOURNAL_NO = "journal_no";
    public static final String COLUMN_JOURNAL_DATE = "date";
    public static final String COLUMN_JOURNAL_NARRATION = "narration";
    public static final String COLUMN_JOURNAL_TYPE = "journal_type";
    
    // Receipt Table
    public static final String TABLE_RECEIPTS = "receipts";
    public static final String COLUMN_RECEIPT_ID = "_id";
    public static final String COLUMN_RECEIPT_NO = "receipt_no";
    public static final String COLUMN_RECEIPT_DATE = "date";
    public static final String COLUMN_RECEIPT_NARRATION = "narration";
    public static final String COLUMN_RECEIPT_THROUGH = "through_ledger"; // New
    public static final String COLUMN_RECEIPT_TOTAL = "total_amount"; // New
    public static final String COLUMN_RECEIPT_PAYMENT_MODE = "payment_mode"; // New for v35
    
    // Notifications Table
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIF_ID = "_id";
    public static final String COLUMN_NOTIF_TITLE = "title";
    public static final String COLUMN_NOTIF_MESSAGE = "message";
    public static final String COLUMN_NOTIF_TYPE = "type"; // Info, Success, Warning
    public static final String COLUMN_NOTIF_TIMESTAMP = "timestamp";
    public static final String COLUMN_NOTIF_IS_READ = "is_read";
    
    // Payments Table
    public static final String TABLE_PAYMENTS = "payments";
    public static final String COLUMN_PAYMENT_ID = "_id";
    public static final String COLUMN_PAYMENT_VOUCHER_NO = "voucher_no";
    public static final String COLUMN_PAYMENT_DATE = "date";
    public static final String COLUMN_PAYMENT_THROUGH_LEDGER = "through_ledger"; // The Bank/Cash Ledger
    public static final String COLUMN_PAYMENT_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_PAYMENT_NARRATION = "narration";
    
    // ...

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Companies Table
        String queryCompanies = "CREATE TABLE " + TABLE_COMPANIES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_COMPANY_NAME + " TEXT, " +
                COLUMN_COMPANY_ADDRESS + " TEXT, " +
                COLUMN_COMPANY_MOBILE + " TEXT, " +
                COLUMN_COMPANY_PHONE2 + " TEXT, " + // New
                COLUMN_COMPANY_EMAIL + " TEXT, " +
                COLUMN_COMPANY_STATE + " TEXT, " +   // New
                COLUMN_COMPANY_LOGO + " TEXT, " +    // New
                COLUMN_COMPANY_GST + " TEXT, " +
                COLUMN_COMPANY_TAGLINE + " TEXT, " +
                COLUMN_COMPANY_CST + " TEXT, " +
                COLUMN_COMPANY_TIN + " TEXT, " +
                COLUMN_COMPANY_VAT_TIN + " TEXT);";
        db.execSQL(queryCompanies);

        String queryGroups = "CREATE TABLE " + TABLE_GROUPS + " (" +
                COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GROUP_NAME + " TEXT, " +
                COLUMN_GROUP_PARENT + " TEXT, " + 
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
        db.execSQL(queryGroups);

        String queryStockGroups = "CREATE TABLE " + TABLE_STOCK_GROUPS + " (" +
                COLUMN_STOCK_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STOCK_GROUP_NAME + " TEXT, " +
                COLUMN_STOCK_GROUP_PARENT + " TEXT);";
        db.execSQL(queryStockGroups);

        String queryStockCats = "CREATE TABLE " + TABLE_STOCK_CATEGORIES + " (" +
                COLUMN_STOCK_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STOCK_CAT_NAME + " TEXT, " +
                COLUMN_STOCK_CAT_PARENT + " TEXT);";
        db.execSQL(queryStockCats);

        String queryLedgers = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_GROUP + " TEXT, " +
                COLUMN_MOBILE + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_GST + " TEXT, " +
                COLUMN_BALANCE + " REAL, " +
                COLUMN_TYPE + " TEXT, " + 
                COLUMN_TAX_RATE + " REAL, " +
                COLUMN_IS_PERCENTAGE + " INTEGER DEFAULT 0, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0, " +
                COLUMN_BANK_NAME + " TEXT, " +
                COLUMN_BANK_ACCOUNT_NO + " TEXT, " +
                COLUMN_BANK_IFSC + " TEXT, " +
                COLUMN_BANK_BRANCH + " TEXT);";
        db.execSQL(queryLedgers);

        String queryItems = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT, " +
                COLUMN_ITEM_RATE + " REAL, " +
                COLUMN_ITEM_UNIT + " TEXT, " +
                COLUMN_ITEM_STOCK + " REAL DEFAULT 0, " +
                COLUMN_ITEM_HSN + " TEXT, " +
                COLUMN_ITEM_GROUP + " TEXT, " +
                COLUMN_ITEM_CATEGORY + " TEXT, " +
                COLUMN_ITEM_GST_RATE + " REAL DEFAULT 0, " +
                COLUMN_ITEM_GST_TYPE + " TEXT DEFAULT 'Percentage', " +
                COLUMN_ITEM_COST + " REAL DEFAULT 0, " +
                COLUMN_ITEM_LOW_STOCK_LIMIT + " REAL DEFAULT 0, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);"; 
        db.execSQL(queryItems);

        String queryInvoices = "CREATE TABLE " + TABLE_INVOICES + " (" +
                COLUMN_INVOICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_INVOICE_NO + " TEXT, " +
                COLUMN_INVOICE_DATE + " TEXT, " +
                COLUMN_CUSTOMER_NAME + " TEXT, " +
                COLUMN_TOTAL_AMOUNT + " REAL, " +
                COLUMN_DELIVERY_CHARGES + " REAL, " +
                COLUMN_TOTAL_TAX + " REAL, " +
                COLUMN_GRAND_TOTAL + " REAL, " +
                COLUMN_DELIVERY_NOTE + " TEXT, " +
                COLUMN_MODE_PAYMENT + " TEXT, " +
                COLUMN_REF_NO + " TEXT, " +
                COLUMN_OTHER_REF + " TEXT, " +
                COLUMN_BUYER_ORDER_NO + " TEXT, " +
                COLUMN_DISPATCH_DOC_NO + " TEXT, " +
                COLUMN_DELIVERY_NOTE_DATE + " TEXT, " +
                COLUMN_DISPATCH_THROUGH + " TEXT, " +
                COLUMN_DESTINATION + " TEXT, " +
                COLUMN_TERMS_DELIVERY + " TEXT, " +
                COLUMN_BILL_OF_LADING + " TEXT, " +
                COLUMN_MOTOR_VEHICLE_NO + " TEXT, " +
                COLUMN_CONSIGNEE_NAME + " TEXT, " +
                COLUMN_CONSIGNEE_ADDR + " TEXT, " +
                COLUMN_CONSIGNEE_GST + " TEXT, " +
                COLUMN_CONSIGNEE_STATE + " TEXT, " +
                COLUMN_BUYER_ADDR + " TEXT, " +
                COLUMN_BUYER_GST + " TEXT, " +
                COLUMN_BUYER_STATE + " TEXT, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0, " +
                "bank_ledger_id INTEGER DEFAULT -1);"; // New Column for Bank Selection
        db.execSQL(queryInvoices);

        String queryInvoiceItems = "CREATE TABLE " + TABLE_INVOICE_ITEMS + " (" +
                COLUMN_INV_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_INV_ID_FK + " INTEGER, " +
                COLUMN_INV_ITEM_NAME + " TEXT, " +
                COLUMN_INV_QTY + " REAL, " +
                COLUMN_INV_RATE + " REAL, " +
                COLUMN_INV_AMOUNT + " REAL, " +
                COLUMN_INV_HSN + " TEXT, " +
                COLUMN_INV_GST_RATE + " REAL, " +
                COLUMN_INV_CGST + " REAL, " +
                COLUMN_INV_SGST + " REAL, " +
                COLUMN_UNIT + " TEXT);"; // Added
        db.execSQL(queryInvoiceItems);
        
        String queryPurchases = "CREATE TABLE " + TABLE_PURCHASES + " (" +
                COLUMN_PURCHASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PURCHASE_INV_NO + " TEXT, " +
                COLUMN_PURCHASE_DATE + " TEXT, " +
                COLUMN_SUPPLIER_NAME + " TEXT, " +
                COLUMN_PURCHASE_TOTAL + " REAL, " +
                COLUMN_SUPPLIER_INV_DATE + " TEXT, " +
                COLUMN_SUPPLIER_CST + " TEXT, " +
                COLUMN_SUPPLIER_TIN + " TEXT, " +
                COLUMN_BUYER_VAT_TIN + " TEXT, " +
                COLUMN_SUPPLIER_INV_NO + " TEXT, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
        db.execSQL(queryPurchases);

        String queryPurchaseItems = "CREATE TABLE " + TABLE_PURCHASE_ITEMS + " (" +
                COLUMN_PUR_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PUR_ID_FK + " INTEGER, " +
                COLUMN_PUR_ITEM_NAME + " TEXT, " +
                COLUMN_PUR_QTY + " REAL, " +
                COLUMN_PUR_RATE + " REAL, " +
                COLUMN_PUR_AMOUNT + " REAL, " +
                COLUMN_PUR_UNIT + " TEXT, " +
                COLUMN_PUR_HSN + " TEXT);"; 
        db.execSQL(queryPurchaseItems);
        
        String queryVoucherCharges = "CREATE TABLE " + TABLE_VOUCHER_CHARGES + " (" +
                COLUMN_CHARGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CHARGE_VOUCHER_ID + " INTEGER, " +
                COLUMN_CHARGE_VOUCHER_TYPE + " TEXT, " +
                COLUMN_CHARGE_LEDGER_ID + " INTEGER, " +
                COLUMN_CHARGE_LEDGER_NAME + " TEXT, " +
                COLUMN_CHARGE_AMOUNT + " REAL, " +
                COLUMN_CHARGE_IS_PERCENTAGE + " INTEGER, " +
                COLUMN_CHARGE_RATE + " REAL, " +
                COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0, " +
                COLUMN_CHARGE_PAYMENT_MODE + " TEXT);";

        db.execSQL(queryVoucherCharges);

        String queryNotifications = "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                COLUMN_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTIF_TITLE + " TEXT, " +
                COLUMN_NOTIF_MESSAGE + " TEXT, " +
                COLUMN_NOTIF_TYPE + " TEXT, " +
                COLUMN_NOTIF_TIMESTAMP + " INTEGER, " +
                COLUMN_NOTIF_IS_READ + " INTEGER DEFAULT 0);";
        db.execSQL(queryNotifications);
        
        String queryPayments = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                COLUMN_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PAYMENT_VOUCHER_NO + " TEXT, " +
                COLUMN_PAYMENT_DATE + " TEXT, " +
                COLUMN_PAYMENT_THROUGH_LEDGER + " TEXT, " + 
                COLUMN_PAYMENT_TOTAL_AMOUNT + " REAL, " +
                COLUMN_PAYMENT_NARRATION + " TEXT, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
        db.execSQL(queryPayments);
        
        String queryJournals = "CREATE TABLE " + TABLE_JOURNALS + " (" +
                COLUMN_JOURNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_JOURNAL_NO + " TEXT, " +
                COLUMN_JOURNAL_DATE + " TEXT, " +
                COLUMN_JOURNAL_NARRATION + " TEXT, " +
                COLUMN_JOURNAL_TYPE + " TEXT, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
        db.execSQL(queryJournals);

        String queryReceipts = "CREATE TABLE " + TABLE_RECEIPTS + " (" +
                COLUMN_RECEIPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECEIPT_NO + " TEXT, " +
                COLUMN_RECEIPT_DATE + " TEXT, " +
                COLUMN_RECEIPT_NARRATION + " TEXT, " +
                COLUMN_RECEIPT_THROUGH + " TEXT, " + 
                COLUMN_RECEIPT_TOTAL + " REAL, " +
                COLUMN_RECEIPT_PAYMENT_MODE + " TEXT, " +
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
        db.execSQL(queryReceipts);
        
        // Update queryVoucherCharges to include is_debit
        db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0");
        
        insertDefaultGroups(db);
    }

    private void insertDefaultGroups(SQLiteDatabase db) {
        String[] defaults = {
            "Sundry Debtors", "Sundry Creditors", "Duties & Taxes", "Bank Accounts", 
            "Cash-in-hand", "Sales Accounts", "Purchase Accounts", 
            "Direct Expenses", "Indirect Expenses", "Indirect Incomes", "Round Off"
        };
        for (String group : defaults) {
            String groupToUse = group;
            String parent = "Primary";
            
            if (group.equals("Round Off")) {
                groupToUse = "Duties & Taxes";
            }

            // Check if name already exists in ledgers or groups to avoid duplicates
            // For groups
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_GROUPS + " WHERE " + COLUMN_GROUP_NAME + "=?", new String[]{groupToUse});
            if (c.getCount() == 0 && !group.equals("Round Off")) {
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_GROUP_NAME, groupToUse);
                cv.put(COLUMN_GROUP_PARENT, parent);
                db.insert(TABLE_GROUPS, null, cv);
            }
            c.close();

            if (group.equals("Round Off")) {
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_NAME, "Round Off");
                cv.put(COLUMN_GROUP, "Duties & Taxes");
                cv.put(COLUMN_BALANCE, 0.0);
                cv.put(COLUMN_TYPE, "Dr");
                db.insert(TABLE_NAME, null, cv);
            }
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 9) {
             // Wipe for older versions if struct changed drastically
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK_GROUPS);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK_CATEGORIES);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICES);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVOICE_ITEMS);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASES);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE_ITEMS);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOUCHER_CHARGES);
             db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
             onCreate(db);
        } else if (oldVersion == 9 && newVersion >= 10) {
             // Incremental Upgrade for version 10
             String[] newCols = {
                COLUMN_DELIVERY_NOTE, COLUMN_MODE_PAYMENT, COLUMN_REF_NO, COLUMN_OTHER_REF, 
                COLUMN_BUYER_ORDER_NO, COLUMN_DISPATCH_DOC_NO, COLUMN_DELIVERY_NOTE_DATE,
                COLUMN_DISPATCH_THROUGH, COLUMN_DESTINATION, COLUMN_TERMS_DELIVERY,
                COLUMN_CONSIGNEE_NAME, COLUMN_CONSIGNEE_ADDR, COLUMN_CONSIGNEE_GST, COLUMN_CONSIGNEE_STATE,
                COLUMN_BUYER_ADDR, COLUMN_BUYER_GST, COLUMN_BUYER_STATE
            };
            for (String col : newCols) {
                try { db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN " + col + " TEXT"); } catch (Exception e) {}
            }
        }
        if (oldVersion < 11 && newVersion >= 11) {
            // Incremental Upgrade for version 11: Add COLUMN_UNIT to TABLE_INVOICE_ITEMS
            try {
                db.execSQL("ALTER TABLE " + TABLE_INVOICE_ITEMS + " ADD COLUMN " + COLUMN_UNIT + " TEXT");
            } catch (Exception e) {
                // Ignore if already exists (safe migration)
            }
        }
        if (oldVersion < 12 && newVersion >= 12) {
            // Upgrade for version 12: Add Bill of Lading, Motor Vehicle, HSN
            try { db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN " + COLUMN_BILL_OF_LADING + " TEXT"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN " + COLUMN_MOTOR_VEHICLE_NO + " TEXT"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_INVOICE_ITEMS + " ADD COLUMN " + COLUMN_INV_HSN + " TEXT"); } catch (Exception e) {}
        }
        if (oldVersion < 13 && newVersion >= 13) {
            // Upgrade for version 13: Company Support
            try {
                // Create Companies Table
                 String queryCompanies = "CREATE TABLE " + TABLE_COMPANIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COMPANY_NAME + " TEXT, " +
                    COLUMN_COMPANY_ADDRESS + " TEXT, " +
                    COLUMN_COMPANY_MOBILE + " TEXT, " +
                    COLUMN_COMPANY_EMAIL + " TEXT, " +
                    COLUMN_COMPANY_GST + " TEXT, " +
                    COLUMN_COMPANY_TAGLINE + " TEXT);";
                db.execSQL(queryCompanies);
                
                // Add company_id to existing tables
                db.execSQL("ALTER TABLE " + TABLE_GROUPS + " ADD COLUMN " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0");
            } catch (Exception e) {}
        }
        
        if (oldVersion < 25 && newVersion >= 25) {
            // Version 25: Journal, Receipt and Dr/Cr Support
            try { db.execSQL("CREATE TABLE " + TABLE_JOURNALS + " (" + COLUMN_JOURNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_JOURNAL_NO + " TEXT, " + COLUMN_JOURNAL_DATE + " TEXT, " + COLUMN_JOURNAL_NARRATION + " TEXT, " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0)"); } catch (Exception e) {}
            try { db.execSQL("CREATE TABLE " + TABLE_RECEIPTS + " (" + COLUMN_RECEIPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_RECEIPT_NO + " TEXT, " + COLUMN_RECEIPT_DATE + " TEXT, " + COLUMN_RECEIPT_NARRATION + " TEXT, " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0)"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0"); } catch (Exception e) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0");
                // Voucher Charges - linking to voucher which has company, or ledger which has company. 
                // Just in case, no specific company_id needed if linked to voucher_id which is inside an Invoice(company_id).
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 14 && newVersion >= 14) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_PHONE2 + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_STATE + " TEXT");
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        if (oldVersion < 15 && newVersion >= 15) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_LOGO + " TEXT");
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        // Fix for "column does not exist" if v15 upgrade failed silentely
        if (oldVersion < 16 && newVersion >= 16) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_LOGO + " TEXT");
             } catch (Exception e) {
                 // Ignore if already exists
             }
             try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_PHONE2 + " TEXT");
             } catch (Exception e) {}
             try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_STATE + " TEXT");
             } catch (Exception e) {}
        }
        if (oldVersion < 17 && newVersion >= 17) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_BANK_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_BANK_ACCOUNT_NO + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_BANK_IFSC + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_BANK_BRANCH + " TEXT");
                
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN bank_ledger_id INTEGER DEFAULT -1");
             } catch (Exception e) {}
        }
        if (oldVersion < 18 && newVersion >= 18) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN buyers_order_date TEXT");
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN buyer_email TEXT");
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN buyer_mobile TEXT");
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN consignee_email TEXT");
                db.execSQL("ALTER TABLE " + TABLE_INVOICES + " ADD COLUMN consignee_mobile TEXT");
                
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN company_pan TEXT");
             } catch (Exception e) {}
        }
        if (oldVersion < 19 && newVersion >= 19) {
             try {
                String queryPayments = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                        COLUMN_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PAYMENT_VOUCHER_NO + " TEXT, " +
                        COLUMN_PAYMENT_DATE + " TEXT, " +
                        COLUMN_PAYMENT_THROUGH_LEDGER + " TEXT, " + 
                        COLUMN_PAYMENT_TOTAL_AMOUNT + " REAL, " +
                        COLUMN_PAYMENT_NARRATION + " TEXT, " +
                        COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
                 db.execSQL(queryPayments);
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        if (oldVersion < 20 && newVersion >= 20) {
             try {
                String queryPayments = "CREATE TABLE IF NOT EXISTS " + TABLE_PAYMENTS + " (" +
                        COLUMN_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PAYMENT_VOUCHER_NO + " TEXT, " +
                        COLUMN_PAYMENT_DATE + " TEXT, " +
                        COLUMN_PAYMENT_THROUGH_LEDGER + " TEXT, " + 
                        COLUMN_PAYMENT_TOTAL_AMOUNT + " REAL, " +
                        COLUMN_PAYMENT_NARRATION + " TEXT, " +
                        COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
                 db.execSQL(queryPayments);
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        if (oldVersion < 21 && newVersion >= 21) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_PURCHASE_ITEMS + " ADD COLUMN " + COLUMN_PUR_UNIT + " TEXT");
             } catch (Exception e) {}
        }
        if (oldVersion < 22 && newVersion >= 22) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_ITEM_GST_RATE + " REAL DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_ITEM_GST_TYPE + " TEXT DEFAULT 'Percentage'");
             } catch (Exception e) {}
        }
        if (oldVersion < 23 && newVersion >= 23) {
            try {
                // Add Unit to Items
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_ITEM_UNIT + " TEXT");
                // Add Unit and HSN to Invoice Items
                db.execSQL("ALTER TABLE " + TABLE_INVOICE_ITEMS + " ADD COLUMN " + COLUMN_UNIT + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_INVOICE_ITEMS + " ADD COLUMN " + COLUMN_INV_HSN + " TEXT");
                
                // Add HSN to Purchase Items
                db.execSQL("ALTER TABLE " + TABLE_PURCHASE_ITEMS + " ADD COLUMN " + COLUMN_PUR_HSN + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 24 && newVersion >= 24) {
            try {
                // Ensure Round Off Ledger exists
                Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + "=?", new String[]{"Round Off"});
                if (c.getCount() == 0) {
                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_NAME, "Round Off");
                    cv.put(COLUMN_GROUP, "Duties & Taxes");
                    cv.put(COLUMN_BALANCE, 0.0);
                    cv.put(COLUMN_TYPE, "Dr");
                    db.insert(TABLE_NAME, null, cv);
                }
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 28 && newVersion >= 28) {
            try { db.execSQL("ALTER TABLE " + TABLE_JOURNALS + " ADD COLUMN " + COLUMN_JOURNAL_TYPE + " TEXT"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_PAYMENT_MODE + " TEXT"); } catch (Exception e) {}
        }
        if (oldVersion < 29 && newVersion >= 29) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_CST + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_TIN + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_COMPANIES + " ADD COLUMN " + COLUMN_COMPANY_VAT_TIN + " TEXT");
                
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_INV_DATE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_CST + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_TIN + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_BUYER_VAT_TIN + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 30 && newVersion >= 30) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_INV_NO + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 31 && newVersion >= 31) {
            try {
                // Ensure columns exist (Duplicate check for safety)
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_INV_DATE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_CST + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_SUPPLIER_TIN + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PURCHASES + " ADD COLUMN " + COLUMN_BUYER_VAT_TIN + " TEXT");
            } catch (Exception e) {
                // Ignore if they already exist
            }
        }
        if (oldVersion < 32 && newVersion >= 32) {
            try {
                // Add new columns for Receipts
                db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_THROUGH + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_TOTAL + " REAL");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 34 && newVersion >= 34) {
             try {
                 // Comprehensive Fix: Ensure Table Exists
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECEIPTS + " (" +
                         COLUMN_RECEIPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         COLUMN_RECEIPT_NO + " TEXT, " +
                         COLUMN_RECEIPT_DATE + " TEXT, " +
                         COLUMN_RECEIPT_NARRATION + " TEXT, " +
                         COLUMN_RECEIPT_THROUGH + " TEXT, " + 
                         COLUMN_RECEIPT_TOTAL + " REAL, " +   
                         COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);");
                         
                 // Ensure Columns Exist (Safe Add)
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_DATE + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_NO + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_NARRATION + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_THROUGH + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_TOTAL + " REAL"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_COMPANY_ID + " INTEGER DEFAULT 0"); } catch (Exception e) {}
                 
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        if (oldVersion < 35 && newVersion >= 35) {
            try {
                // Add payment mode to Receipts
                db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_PAYMENT_MODE + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Robustly ensure payment_mode exists for v35+ regardless of upgrade path
        if (newVersion >= 35) {
             try {
                db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_PAYMENT_MODE + " TEXT");
            } catch (Exception e) {
                // Column likely already exists, ignore
            }
        }
        
        // Final Safety Net: Ensure all tables exist for v35+ (Covers any restore scenario)
        if (newVersion >= 35) {
             try {
                 // Purchases
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PURCHASES + " (" +
                        COLUMN_PURCHASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PURCHASE_INV_NO + " TEXT, " +
                        COLUMN_PURCHASE_DATE + " TEXT, " +
                        COLUMN_SUPPLIER_NAME + " TEXT, " +
                        COLUMN_PURCHASE_TOTAL + " REAL, " +
                        COLUMN_SUPPLIER_INV_DATE + " TEXT, " +
                        COLUMN_SUPPLIER_CST + " TEXT, " +
                        COLUMN_SUPPLIER_TIN + " TEXT, " +
                        COLUMN_BUYER_VAT_TIN + " TEXT, " +
                        COLUMN_SUPPLIER_INV_NO + " TEXT, " +
                        COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);");

                 // Purchase Items
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PURCHASE_ITEMS + " (" +
                        COLUMN_PUR_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PUR_ID_FK + " INTEGER, " +
                        COLUMN_PUR_ITEM_NAME + " TEXT, " +
                        COLUMN_PUR_QTY + " REAL, " +
                        COLUMN_PUR_RATE + " REAL, " +
                        COLUMN_PUR_AMOUNT + " REAL, " +
                        COLUMN_PUR_UNIT + " TEXT, " +
                        COLUMN_PUR_HSN + " TEXT);");

                 // Receipts
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECEIPTS + " (" +
                        COLUMN_RECEIPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_RECEIPT_NO + " TEXT, " +
                        COLUMN_RECEIPT_DATE + " TEXT, " +
                        COLUMN_RECEIPT_NARRATION + " TEXT, " +
                        COLUMN_RECEIPT_THROUGH + " TEXT, " +
                        COLUMN_RECEIPT_TOTAL + " REAL, " +
                        COLUMN_RECEIPT_PAYMENT_MODE + " TEXT, " +
                        COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);");
                 
                 // Payments
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PAYMENTS + " (" +
                        COLUMN_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_PAYMENT_VOUCHER_NO + " TEXT, " +
                        COLUMN_PAYMENT_DATE + " TEXT, " +
                        COLUMN_PAYMENT_THROUGH_LEDGER + " TEXT, " + 
                        COLUMN_PAYMENT_TOTAL_AMOUNT + " REAL, " +
                        COLUMN_PAYMENT_NARRATION + " TEXT, " +
                        COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);");

                 // Voucher Charges       
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VOUCHER_CHARGES + " (" +
                        COLUMN_CHARGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CHARGE_VOUCHER_ID + " INTEGER, " +
                        COLUMN_CHARGE_VOUCHER_TYPE + " TEXT, " +
                        COLUMN_CHARGE_LEDGER_ID + " INTEGER, " +
                        COLUMN_CHARGE_LEDGER_NAME + " TEXT, " +
                        COLUMN_CHARGE_AMOUNT + " REAL, " +
                        COLUMN_CHARGE_IS_PERCENTAGE + " INTEGER, " +
                        COLUMN_CHARGE_RATE + " REAL, " +
                        COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0, " +
                        COLUMN_CHARGE_PAYMENT_MODE + " TEXT);");
                        
                 // Notifications
                 db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS + " (" +
                        COLUMN_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NOTIF_TITLE + " TEXT, " +
                        COLUMN_NOTIF_MESSAGE + " TEXT, " +
                        COLUMN_NOTIF_TYPE + " TEXT, " +
                        COLUMN_NOTIF_TIMESTAMP + " INTEGER, " +
                        COLUMN_NOTIF_IS_READ + " INTEGER DEFAULT 0);");

             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        if (oldVersion < 36 && newVersion >= 36) {
             try {
                 // Safety Check: Ensure Reporting Columns Exist by attempting to add them (ignoring errors if present)
                 try { db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_TYPE + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_PAYMENT_MODE + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_THROUGH + " TEXT"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_TOTAL + " REAL"); } catch (Exception e) {}
                 try { db.execSQL("ALTER TABLE " + TABLE_PAYMENTS + " ADD COLUMN " + COLUMN_PAYMENT_THROUGH_LEDGER + " TEXT"); } catch (Exception e) {}
             } catch (Exception e) {
                 e.printStackTrace();
             }
        }
        if (oldVersion < 37 && newVersion >= 37) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_ITEM_COST + " REAL DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_ITEM_LOW_STOCK_LIMIT + " REAL DEFAULT 0");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void updateStock(String itemName, double qtyChange) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ITEM_STOCK + " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_NAME + "=?", new String[]{itemName});
        if (cursor.moveToFirst()) {
            double currentStock = cursor.getDouble(0);
            double newStock = currentStock + qtyChange;
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ITEM_STOCK, newStock);
            db.update(TABLE_ITEMS, cv, COLUMN_ITEM_NAME + "=?", new String[]{itemName});
        }
        cursor.close();
    }

    public List<String> getAllItemNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, new String[]{COLUMN_ITEM_NAME}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
            cursor.close();
        }
        return names;
    }

    public double getItemRate(String itemName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, new String[]{COLUMN_ITEM_RATE}, COLUMN_ITEM_NAME + "=?", new String[]{itemName}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            double rate = cursor.getDouble(0);
            cursor.close();
            return rate;
        }
        return 0;
    }

    public Cursor getLedgerDetails(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, COLUMN_NAME + "=?", new String[]{name}, null, null, null);
    }
    
    public List<String> getAllLedgerGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_NAME}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String group = cursor.getString(0);
                if (group != null && !group.isEmpty())
                    groups.add(group);
            }
            cursor.close();
        }
        return groups;
    }

    public Cursor getLedgersByGroup(String group) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_NAME + " as ledger_name, " + 
                       COLUMN_BALANCE + " as opening_balance, " +
                       COLUMN_TYPE + " as balance_type " + 
                       "FROM " + TABLE_NAME + " WHERE " + COLUMN_GROUP + "=?";
        return db.rawQuery(query, new String[]{group});
    }



    public List<String> getContraLedgerNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_NAME + " FROM " + TABLE_NAME + 
                       " WHERE " + COLUMN_GROUP + " IN ('Bank Accounts', 'Cash-in-hand')";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
            cursor.close();
        }
        if (!names.contains("Cash")) names.add("Cash");
        return names;
    }


    // --- Master Methods (Groups, Ledgers, Items) ---
    public void addLedgerGroup(String name, String parent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GROUP_NAME, name);
        cv.put(COLUMN_GROUP_PARENT, parent);
        db.insert(TABLE_GROUPS, null, cv);
    }
    
    public void updateLedgerGroup(int id, String name, String parent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GROUP_NAME, name);
        cv.put(COLUMN_GROUP_PARENT, parent);
        db.update(TABLE_GROUPS, cv, COLUMN_GROUP_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public Cursor getLedgerGroup(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GROUPS, null, COLUMN_GROUP_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }



    public void addItem(String name, double rate, String unit, double stock, String hsn, String group, String category, double gstRate, String gstType, double cost, double lowStockLimit, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ITEM_NAME, name);
        cv.put(COLUMN_ITEM_RATE, rate);
        cv.put(COLUMN_ITEM_UNIT, unit);
        cv.put(COLUMN_ITEM_STOCK, stock);
        cv.put(COLUMN_ITEM_HSN, hsn);
        cv.put(COLUMN_ITEM_GROUP, group);
        cv.put(COLUMN_ITEM_CATEGORY, category);
        cv.put(COLUMN_ITEM_GST_RATE, gstRate);
        cv.put(COLUMN_ITEM_GST_TYPE, gstType);
        cv.put(COLUMN_ITEM_COST, cost);
        cv.put(COLUMN_ITEM_LOW_STOCK_LIMIT, lowStockLimit);
        cv.put(COLUMN_COMPANY_ID, companyId);
        db.insert(TABLE_ITEMS, null, cv);
    }

    public void addItem(String name, double rate, String unit, double stock, String hsn, String group, String category, double gstRate, String gstType, double cost, double lowStockLimit) {
        addItem(name, rate, unit, stock, hsn, group, category, gstRate, gstType, cost, lowStockLimit, 0);
    }

    public void addItem(String name, double rate, String unit, double stock, String hsn, String group, String category, double gstRate, String gstType) {
        addItem(name, rate, unit, stock, hsn, group, category, gstRate, gstType, 0.0, 0.0);
    }

    // Legacy overload
    public void addItem(String name, double rate, String unit, double stock, String hsn, String group, String category) {
        addItem(name, rate, unit, stock, hsn, group, category, 0.0, "Percentage");
    }
    
    public void updateItem(int id, String name, double rate, String unit, double stock, String hsn, String group, String category, double gstRate, String gstType, double cost, double lowStockLimit, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ITEM_NAME, name);
        cv.put(COLUMN_ITEM_RATE, rate);
        cv.put(COLUMN_ITEM_UNIT, unit);
        cv.put(COLUMN_ITEM_STOCK, stock);
        cv.put(COLUMN_ITEM_HSN, hsn);
        cv.put(COLUMN_ITEM_GROUP, group);
        cv.put(COLUMN_ITEM_CATEGORY, category);
        cv.put(COLUMN_ITEM_GST_RATE, gstRate);
        cv.put(COLUMN_ITEM_GST_TYPE, gstType);
        cv.put(COLUMN_ITEM_COST, cost);
        cv.put(COLUMN_ITEM_LOW_STOCK_LIMIT, lowStockLimit);
        cv.put(COLUMN_COMPANY_ID, companyId);
        db.update(TABLE_ITEMS, cv, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateItem(int id, String name, double rate, String unit, double stock, String hsn, String group, String category, double gstRate, String gstType, double cost, double lowStockLimit) {
        updateItem(id, name, rate, unit, stock, hsn, group, category, gstRate, gstType, cost, lowStockLimit, 0);
    }

    public void updateItem(int id, String name, double rate, String unit, double stock, String hsn, String group, String category, double gstRate, String gstType) {
        updateItem(id, name, rate, unit, stock, hsn, group, category, gstRate, gstType, 0.0, 0.0);
    }

    // Legacy overload
    public void updateItem(int id, String name, double rate, String unit, double stock, String hsn, String group, String category) {
        updateItem(id, name, rate, unit, stock, hsn, group, category, 0.0, "Percentage");
    }
    
    public Cursor getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Aliasing to match ItemActivity.java expectations
        String query = "SELECT " + COLUMN_ITEM_NAME + ", " +
                       COLUMN_ITEM_RATE + ", " + COLUMN_ITEM_UNIT + ", " +
                       COLUMN_ITEM_STOCK + " as stock_quantity, " + 
                       COLUMN_ITEM_HSN + " as hsn_sac, " +
                       COLUMN_ITEM_GROUP + " as stock_group, " + 
                       COLUMN_ITEM_CATEGORY + " as stock_category, " +
                        COLUMN_ITEM_GST_RATE + ", " + COLUMN_ITEM_GST_TYPE + ", " +
                       COLUMN_ITEM_COST + ", " + COLUMN_ITEM_LOW_STOCK_LIMIT +
                       " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
    }

    public Cursor getItemDetailsByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ITEMS, null, COLUMN_ITEM_NAME + "=?", new String[]{name}, null, null, null);
    }
    
    public List<String> getAllStockGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCK_GROUPS, new String[]{COLUMN_STOCK_GROUP_NAME}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                groups.add(cursor.getString(0));
            }
            cursor.close();
        }
        return groups;
    }
    
    public List<String> getAllStockCategories() {
        List<String> cats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCK_CATEGORIES, new String[]{COLUMN_STOCK_CAT_NAME}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                cats.add(cursor.getString(0));
            }
            cursor.close();
        }
        return cats;
    }
    
    public void addStockGroup(String name, String parent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOCK_GROUP_NAME, name);
        cv.put(COLUMN_STOCK_GROUP_PARENT, parent);
        db.insert(TABLE_STOCK_GROUPS, null, cv);
    }
    
    public void updateStockGroup(int id, String name, String parent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOCK_GROUP_NAME, name);
        cv.put(COLUMN_STOCK_GROUP_PARENT, parent);
        db.update(TABLE_STOCK_GROUPS, cv, COLUMN_STOCK_GROUP_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public Cursor getStockGroup(int id) {
         SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STOCK_GROUPS, null, COLUMN_STOCK_GROUP_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }
    
    public void addStockCategory(String name, String parent) {
         SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOCK_CAT_NAME, name);
        cv.put(COLUMN_STOCK_CAT_PARENT, parent);
        db.insert(TABLE_STOCK_CATEGORIES, null, cv);
    }
    
    public void updateStockCategory(int id, String name, String parent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOCK_CAT_NAME, name);
        cv.put(COLUMN_STOCK_CAT_PARENT, parent);
        db.update(TABLE_STOCK_CATEGORIES, cv, COLUMN_STOCK_CAT_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public Cursor getStockCategory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STOCK_CATEGORIES, null, COLUMN_STOCK_CAT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public List<String> getItemsByStockGroup(String groupName) {
        List<String> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, 
            new String[]{COLUMN_ITEM_NAME, COLUMN_ITEM_STOCK, COLUMN_ITEM_RATE}, 
            COLUMN_ITEM_GROUP + "=?", 
            new String[]{groupName}, null, null, null);
            
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                double stock = cursor.getDouble(1);
                double rate = cursor.getDouble(2);
                items.add(name + " (Qty: " + stock + ", Rate: " + rate + ")");
            }
            cursor.close();
        }
        return items;
    }

    public List<String> getItemsByStockCategory(String categoryName) {
        List<String> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, 
            new String[]{COLUMN_ITEM_NAME, COLUMN_ITEM_STOCK, COLUMN_ITEM_RATE}, 
            COLUMN_ITEM_CATEGORY + "=?", 
            new String[]{categoryName}, null, null, null);
            
        if (cursor != null) {
            while (cursor.moveToNext()) {
                 String name = cursor.getString(0);
                double stock = cursor.getDouble(1);
                double rate = cursor.getDouble(2);
                items.add(name + " (Qty: " + stock + ", Rate: " + rate + ")");
            }
            cursor.close();
        }
        return items;
    }

    // --- Invoice (Sales) Methods ---
    public long addInvoice(String invoiceNo, String date, String customerName, double totalAmount, double deliveryCharges, double totalTax, double grandTotal) {
        // Legacy support calling new method with defaults
        Invoice inv = new Invoice(invoiceNo, date, customerName, null, totalAmount, deliveryCharges, totalTax, grandTotal);
        return addInvoiceObject(inv);
    }

    public long addInvoiceObject(Invoice invoice) {
        return addInvoiceObject(invoice, 0); // Default to 0 if no company provided
    }

    public long addInvoiceObject(Invoice invoice, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_INVOICE_NO, invoice.getInvoiceNumber());
        cv.put(COLUMN_INVOICE_DATE, invoice.getDate());
        cv.put(COLUMN_CUSTOMER_NAME, invoice.getCustomerName());
        cv.put(COLUMN_TOTAL_AMOUNT, invoice.getTotalAmount());
        cv.put(COLUMN_DELIVERY_CHARGES, invoice.getDeliveryCharges());
        cv.put(COLUMN_TOTAL_TAX, invoice.getTotalTaxAmount());
        cv.put(COLUMN_GRAND_TOTAL, invoice.getGrandTotal());
        
        // Detailed Fields
        cv.put(COLUMN_DELIVERY_NOTE, invoice.getDeliveryNote());
        cv.put(COLUMN_MODE_PAYMENT, invoice.getModeOfPayment());
        cv.put(COLUMN_REF_NO, invoice.getReferenceNo());
        cv.put(COLUMN_OTHER_REF, invoice.getOtherReferences());
        cv.put(COLUMN_BUYER_ORDER_NO, invoice.getBuyersOrderNo());
        cv.put(COLUMN_DISPATCH_DOC_NO, invoice.getDispatchDocNo());
        cv.put(COLUMN_DELIVERY_NOTE_DATE, invoice.getDeliveryNoteDate());
        cv.put(COLUMN_DISPATCH_THROUGH, invoice.getDispatchThrough());
        cv.put(COLUMN_DESTINATION, invoice.getDestination());
        cv.put(COLUMN_TERMS_DELIVERY, invoice.getTermsOfDelivery());
        cv.put(COLUMN_BILL_OF_LADING, invoice.getBillOfLading());
        cv.put(COLUMN_MOTOR_VEHICLE_NO, invoice.getMotorVehicleNo());
        
        cv.put(COLUMN_CONSIGNEE_NAME, invoice.getConsigneeName());
        cv.put(COLUMN_CONSIGNEE_ADDR, invoice.getConsigneeAddress());
        cv.put(COLUMN_CONSIGNEE_GST, invoice.getConsigneeGst());
        cv.put(COLUMN_CONSIGNEE_STATE, invoice.getConsigneeState());
        
        cv.put(COLUMN_BUYER_ADDR, invoice.getBuyerAddress());
        cv.put(COLUMN_BUYER_GST, invoice.getBuyerGst());
        cv.put(COLUMN_BUYER_STATE, invoice.getBuyerState());
        
        cv.put("bank_ledger_id", invoice.getBankLedgerId());
        cv.put(COLUMN_COMPANY_ID, companyId);
        
        return db.insert(TABLE_INVOICES, null, cv);
    }

    // --- Voucher (Generic/Sales/Purchase) Methods ---
    public long addVoucher(String voucherNo, String date, String partyName, double totalAmount, double totalTax, double otherCharges, String type, int companyId) {
        if (type.equals("Sales")) {
            // Map to Invoice
            Invoice inv = new Invoice(voucherNo, date, partyName, null, totalAmount, otherCharges, totalTax, totalAmount + totalTax + otherCharges);
            return addInvoiceObject(inv, companyId);
        } else if (type.equals("Purchase")) {
            return addPurchase(voucherNo, date, "", "", partyName, "", "", "", totalAmount + totalTax + otherCharges, companyId);
        }
        return -1;
    }
    
    public long addVoucherItem(long voucherId, String type, String itemName, double quantity, double rate, double amount, double gstRate, double cgst, double sgst, String unit, String hsn) {
        if (type.equals("Sales")) {
            return addInvoiceItem(voucherId, itemName, quantity, rate, amount, gstRate, cgst, sgst, unit, hsn);
        } else if (type.equals("Purchase")) {
             addPurchaseItem(voucherId, itemName, quantity, rate, amount, unit, hsn);
             return 1; // Success
        }
        return -1;
    }

    public long addPayment(String voucherNo, String date, String partyName, double totalAmount, String throughLedger, String narration, int companyId) {
        // Warning: The original addPayment didn't have partyName, it seems it was stored in VoucherCharges?
        // But for this mismatch, we will ignore partyName or append to narration if needed, 
        // OR better, we use the existing addPayment which expects (No, Date, Through, Total, Narration, Company)
        // The error says: required: String,String,String,double,String,int; found: String,String,String,double,String,String,int
        // So we need an overload or fix the call. 
        // Let's overload to match the call in VoucherActivity
        return addPayment(voucherNo, date, throughLedger, totalAmount, narration + " (Party: " + partyName + ")", companyId);
    } 

    // Updated to include Unit and HSN
    public long addInvoiceItem(long invoiceId, String itemName, double quantity, double rate, double amount, double gstRate, double cgst, double sgst, String unit, String hsn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_ID_FK, invoiceId);
        values.put(COLUMN_INV_ITEM_NAME, itemName);
        values.put(COLUMN_INV_QTY, quantity);
        values.put(COLUMN_INV_RATE, rate);
        values.put(COLUMN_INV_AMOUNT, amount);
        values.put(COLUMN_INV_GST_RATE, gstRate);
        values.put(COLUMN_INV_HSN, hsn);
        values.put(COLUMN_INV_CGST, cgst);
        values.put(COLUMN_INV_SGST, sgst);
        values.put(COLUMN_UNIT, unit);
        long result = db.insert(TABLE_INVOICE_ITEMS, null, values);
        
        // Decrease Stock for Sales
        updateStock(itemName, -quantity);
        return result;
    }
    
    // Overload for backward compatibility
    public long addInvoiceItem(long invoiceId, String itemName, double quantity, double rate, double amount, double gstRate, double cgst, double sgst, String unit) {
        return addInvoiceItem(invoiceId, itemName, quantity, rate, amount, gstRate, cgst, sgst, unit, "");
    }
    
    // Overload for backward compatibility (defaults unit to empty)
    public long addInvoiceItem(long invoiceId, String itemName, double quantity, double rate, double amount, double gstRate, double cgst, double sgst) {
        return addInvoiceItem(invoiceId, itemName, quantity, rate, amount, gstRate, cgst, sgst, "");
    }    
    
    public Cursor getAllInvoices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_INVOICES + " ORDER BY " + COLUMN_INVOICE_ID + " DESC", null);
    }
    
    // --- Purchase Methods ---
    public long addPurchase(String invoiceNo, String date, String supplierInvDate, String supplierInvNo, String supplierName, String supplierCst, String supplierTin, String buyerVatTin, double totalAmount, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PURCHASE_INV_NO, invoiceNo);
        cv.put(COLUMN_PURCHASE_DATE, date);
        cv.put(COLUMN_SUPPLIER_INV_DATE, supplierInvDate);
        cv.put(COLUMN_SUPPLIER_INV_NO, supplierInvNo); // New
        cv.put(COLUMN_SUPPLIER_NAME, supplierName);
        cv.put(COLUMN_SUPPLIER_CST, supplierCst);
        cv.put(COLUMN_SUPPLIER_TIN, supplierTin);
        cv.put(COLUMN_BUYER_VAT_TIN, buyerVatTin);
        cv.put(COLUMN_PURCHASE_TOTAL, totalAmount);
        cv.put(COLUMN_COMPANY_ID, companyId);
        return db.insert(TABLE_PURCHASES, null, cv);
    }
    
    // Legacy overload
    public long addPurchase(String invoiceNo, String date, String supplierInvDate, String supplierName, String supplierCst, String supplierTin, String buyerVatTin, double totalAmount) {
         return addPurchase(invoiceNo, date, supplierInvDate, "", supplierName, supplierCst, supplierTin, buyerVatTin, totalAmount, 0);
    }
    
    // Older legacy overload
    public long addPurchase(String invoiceNo, String date, String supplierName, double totalAmount, int companyId) {
         return addPurchase(invoiceNo, date, "", "", supplierName, "", "", "", totalAmount, companyId);
    }

    public long addPurchase(String invoiceNo, String date, String supplierName, double totalAmount) {
         return addPurchase(invoiceNo, date, "", "", supplierName, "", "", "", totalAmount, 0);
    }

    public void addPurchaseItem(long purchaseId, String itemName, double qty, double rate, double amount, String unit, String hsn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PUR_ID_FK, purchaseId);
        cv.put(COLUMN_PUR_ITEM_NAME, itemName);
        cv.put(COLUMN_PUR_QTY, qty);
        cv.put(COLUMN_PUR_RATE, rate);
        cv.put(COLUMN_PUR_AMOUNT, amount);
        cv.put(COLUMN_PUR_UNIT, unit); // Added
        cv.put(COLUMN_PUR_HSN, hsn); // Added
        db.insert(TABLE_PURCHASE_ITEMS, null, cv);
        
    // Increase Stock for Purchases
        updateStock(itemName, qty);

    // Update Item Cost Price to the latest purchase rate
        try {
            ContentValues itemCv = new ContentValues();
            itemCv.put(COLUMN_ITEM_COST, rate);
            db.update(TABLE_ITEMS, itemCv, COLUMN_ITEM_NAME + "=?", new String[]{itemName});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updatePurchase(long id, String invoiceNo, String date, String supplierInvDate, String supplierInvNo, String supplierName, String supplierCst, String supplierTin, String buyerVatTin, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PURCHASE_INV_NO, invoiceNo);
        cv.put(COLUMN_PURCHASE_DATE, date);
        cv.put(COLUMN_SUPPLIER_INV_DATE, supplierInvDate);
        cv.put(COLUMN_SUPPLIER_INV_NO, supplierInvNo); // New
        cv.put(COLUMN_SUPPLIER_NAME, supplierName);
        cv.put(COLUMN_SUPPLIER_CST, supplierCst);
        cv.put(COLUMN_SUPPLIER_TIN, supplierTin);
        cv.put(COLUMN_BUYER_VAT_TIN, buyerVatTin);
        cv.put(COLUMN_PURCHASE_TOTAL, totalAmount);
        db.update(TABLE_PURCHASES, cv, COLUMN_PURCHASE_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deletePurchaseItems(long purchaseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First revert stock
        Cursor cursor = db.query(TABLE_PURCHASE_ITEMS, new String[]{COLUMN_PUR_ITEM_NAME, COLUMN_PUR_QTY}, COLUMN_PUR_ID_FK + "=?", new String[]{String.valueOf(purchaseId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String item = cursor.getString(0);
                double qty = cursor.getDouble(1);
                updateStock(item, -qty); // Remove stock as we are deleting/updating the purchase
            }
            cursor.close();
        }
        db.delete(TABLE_PURCHASE_ITEMS, COLUMN_PUR_ID_FK + "=?", new String[]{String.valueOf(purchaseId)});
    }
    
    // Legacy overload
    public void addPurchaseItem(long purchaseId, String itemName, double qty, double rate, double amount, String unit) {
        addPurchaseItem(purchaseId, itemName, qty, rate, amount, unit, "");
    }

    public void addPurchaseItem(long purchaseId, String itemName, double qty, double rate, double amount) {
        addPurchaseItem(purchaseId, itemName, qty, rate, amount, "", "");
    }
    // --- Voucher Helper Methods ---
    public List<String> getAllLedgerNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_NAME}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
            cursor.close();
        }
        return names;
    }

    public List<String> getLedgersByGroupList(String groupName) {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // 1. Get Ledgers directly in this group (Case Insensitive)
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_NAME}, COLUMN_GROUP + "=? COLLATE NOCASE", new String[]{groupName}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
            cursor.close();
        }
        
        // 2. Get Sub-groups (Recursive, Case Insensitive)
        Cursor groupCursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_NAME}, COLUMN_GROUP_PARENT + "=? COLLATE NOCASE", new String[]{groupName}, null, null, null);
        if (groupCursor != null) {
            while (groupCursor.moveToNext()) {
                String subGroup = groupCursor.getString(0);
                names.addAll(getLedgersByGroupList(subGroup));
            }
            groupCursor.close();
        }
        
        return names;
    }

    public long addPayment(String voucherNo, String date, String throughLedger, double totalAmount, String narration, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PAYMENT_VOUCHER_NO, voucherNo);
        cv.put(COLUMN_PAYMENT_DATE, date);
        cv.put(COLUMN_PAYMENT_THROUGH_LEDGER, throughLedger);
        cv.put(COLUMN_PAYMENT_TOTAL_AMOUNT, totalAmount);
        cv.put(COLUMN_PAYMENT_NARRATION, narration);
        cv.put(COLUMN_COMPANY_ID, companyId);
        return db.insert(TABLE_PAYMENTS, null, cv);
    }

    // --- Voucher Retrieval Methods ---
    public List<VoucherSummary> getAllVouchers(int companyId) {
        List<VoucherSummary> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Fetch Sales
        String querySales = "SELECT " + COLUMN_INVOICE_ID + ", " + COLUMN_INVOICE_NO + ", " + COLUMN_INVOICE_DATE + ", " + COLUMN_CUSTOMER_NAME + ", " + COLUMN_TOTAL_AMOUNT + 
                            " FROM " + TABLE_INVOICES + " WHERE " + COLUMN_COMPANY_ID + "=?";
        Cursor c1 = db.rawQuery(querySales, new String[]{String.valueOf(companyId)});
        if (c1 != null) {
            while (c1.moveToNext()) {
                list.add(new VoucherSummary(
                    c1.getInt(0), 
                    "Sales", 
                    c1.getString(1), 
                    c1.getString(2), 
                    c1.getString(3), 
                    c1.getDouble(4)
                ));
            }
            c1.close();
        }
        
        // Fetch Purchases
        String queryPurchases = "SELECT " + COLUMN_PURCHASE_ID + ", " + COLUMN_PURCHASE_INV_NO + ", " + COLUMN_PURCHASE_DATE + ", " + COLUMN_SUPPLIER_NAME + ", " + COLUMN_PURCHASE_TOTAL + 
                                " FROM " + TABLE_PURCHASES + " WHERE " + COLUMN_COMPANY_ID + "=?";
        Cursor c2 = db.rawQuery(queryPurchases, new String[]{String.valueOf(companyId)});
         if (c2 != null) {
            while (c2.moveToNext()) {
                list.add(new VoucherSummary(
                    c2.getInt(0), 
                    "Purchase", 
                    c2.getString(1), 
                    c2.getString(2), 
                    c2.getString(3), 
                    c2.getDouble(4)
                ));
            }
            c2.close();
        }

        // Fetch Payments
        String queryPayments = "SELECT " + COLUMN_PAYMENT_ID + ", " + COLUMN_PAYMENT_VOUCHER_NO + ", " + COLUMN_PAYMENT_DATE + ", " + COLUMN_PAYMENT_THROUGH_LEDGER + ", " + COLUMN_PAYMENT_TOTAL_AMOUNT + 
                                " FROM " + TABLE_PAYMENTS + " WHERE " + COLUMN_COMPANY_ID + "=?";
        Cursor c3 = db.rawQuery(queryPayments, new String[]{String.valueOf(companyId)});
         if (c3 != null) {
            while (c3.moveToNext()) {
                int vId = c3.getInt(0);
                String party = getVoucherPartyName(vId, "Payment", c3.getString(3)); // c3.getString(3) is through_ledger
                list.add(new VoucherSummary(
                    vId, 
                    "Payment", 
                    c3.getString(1), 
                    c3.getString(2), 
                    party, 
                    c3.getDouble(4)
                ));
            }
            c3.close();
        }

        // Fetch Receipts
        String queryReceipts = "SELECT " + COLUMN_RECEIPT_ID + ", " + COLUMN_RECEIPT_NO + ", " + COLUMN_RECEIPT_DATE + ", " + COLUMN_RECEIPT_NARRATION + 
                               " FROM " + TABLE_RECEIPTS + " WHERE " + COLUMN_COMPANY_ID + "=?";
        Cursor c4 = db.rawQuery(queryReceipts, new String[]{String.valueOf(companyId)});
        if (c4 != null) {
            while (c4.moveToNext()) {
                int vId = c4.getInt(0);
                double total = getVoucherTotal(vId, "Receipt");
                String party = getVoucherPartyName(vId, "Receipt", c4.getString(3));
                list.add(new VoucherSummary(
                    vId, 
                    "Receipt", 
                    c4.getString(1), 
                    c4.getString(2), 
                    party, 
                    total
                ));
            }
            c4.close();
        }

        // Fetch Journals
        String queryJournals = "SELECT " + COLUMN_JOURNAL_ID + ", " + COLUMN_JOURNAL_NO + ", " + COLUMN_JOURNAL_DATE + ", " + COLUMN_JOURNAL_NARRATION + ", " + COLUMN_JOURNAL_TYPE + 
                               " FROM " + TABLE_JOURNALS + " WHERE " + COLUMN_COMPANY_ID + "=?";
        Cursor c5 = db.rawQuery(queryJournals, new String[]{String.valueOf(companyId)});
        if (c5 != null) {
            int idIdx = c5.getColumnIndex(COLUMN_JOURNAL_ID);
            int noIdx = c5.getColumnIndex(COLUMN_JOURNAL_NO);
            int dateIdx = c5.getColumnIndex(COLUMN_JOURNAL_DATE);
            int narrIdx = c5.getColumnIndex(COLUMN_JOURNAL_NARRATION);
            int typeIdx = c5.getColumnIndex(COLUMN_JOURNAL_TYPE);

            while (c5.moveToNext()) {
                int vId = c5.getInt(c5.getColumnIndexOrThrow(COLUMN_JOURNAL_ID));
                String vType = (typeIdx != -1) ? c5.getString(typeIdx) : "Journal";
                if (vType == null || vType.isEmpty()) vType = "Journal";
                
                double total = getVoucherTotal(vId, vType);
                String party = getVoucherPartyName(vId, vType, (narrIdx != -1) ? c5.getString(narrIdx) : "");
                
                list.add(new VoucherSummary(
                    vId, 
                    vType, 
                    (noIdx != -1) ? c5.getString(noIdx) : "", 
                    (dateIdx != -1) ? c5.getString(dateIdx) : "", 
                    party, 
                    total 
                ));
            }
            c5.close();
        }
        
        return list;
    }

    private String getVoucherPartyName(long voucherId, String type, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        if (type.equalsIgnoreCase("Journal") || type.equalsIgnoreCase("Contra")) {
            String drLedger = "";
            String crLedger = "";
            
            Cursor cDr = db.rawQuery("SELECT " + COLUMN_CHARGE_LEDGER_NAME + " FROM " + TABLE_VOUCHER_CHARGES + 
                                     " WHERE " + COLUMN_CHARGE_VOUCHER_ID + "=? AND TRIM(" + COLUMN_CHARGE_VOUCHER_TYPE + ")=? AND " + COLUMN_CHARGE_IS_DEBIT + "=1 LIMIT 1", 
                                     new String[]{String.valueOf(voucherId), type.trim()});
            if (cDr != null) {
                if (cDr.moveToFirst()) drLedger = cDr.getString(0);
                cDr.close();
            }
            
            Cursor cCr = db.rawQuery("SELECT " + COLUMN_CHARGE_LEDGER_NAME + " FROM " + TABLE_VOUCHER_CHARGES + 
                                     " WHERE " + COLUMN_CHARGE_VOUCHER_ID + "=? AND TRIM(" + COLUMN_CHARGE_VOUCHER_TYPE + ")=? AND " + COLUMN_CHARGE_IS_DEBIT + "=0 LIMIT 1", 
                                     new String[]{String.valueOf(voucherId), type.trim()});
            if (cCr != null) {
                if (cCr.moveToFirst()) crLedger = cCr.getString(0);
                cCr.close();
            }
            
            if (!drLedger.isEmpty() && !crLedger.isEmpty()) {
                return drLedger + " (By) / " + crLedger + " (To)";
            } else if (!drLedger.isEmpty()) {
                return drLedger + " (By)";
            } else if (!crLedger.isEmpty()) {
                return crLedger + " (To)";
            }
        } else if (type.trim().equalsIgnoreCase("Receipt")) {
             Cursor c = db.rawQuery("SELECT " + COLUMN_CHARGE_LEDGER_NAME + " FROM " + TABLE_VOUCHER_CHARGES + 
                                     " WHERE " + COLUMN_CHARGE_VOUCHER_ID + "=? AND TRIM(" + COLUMN_CHARGE_VOUCHER_TYPE + ")=? AND " + COLUMN_CHARGE_IS_DEBIT + "=0 LIMIT 1", 
                                     new String[]{String.valueOf(voucherId), type.trim()});
             if (c != null) {
                 if (c.moveToFirst()) {
                     String party = c.getString(0);
                     c.close();
                     return party;
                 }
                 c.close();
             }
        } else if (type.equalsIgnoreCase("Payment")) {
             Cursor c = db.rawQuery("SELECT " + COLUMN_CHARGE_LEDGER_NAME + " FROM " + TABLE_VOUCHER_CHARGES + 
                                     " WHERE " + COLUMN_CHARGE_VOUCHER_ID + "=? AND " + COLUMN_CHARGE_VOUCHER_TYPE + "=? AND " + COLUMN_CHARGE_IS_DEBIT + "=1 LIMIT 1", 
                                     new String[]{String.valueOf(voucherId), type});
             if (c != null) {
                 if (c.moveToFirst()) {
                     String party = c.getString(0);
                     c.close();
                     return party;
                 }
                 c.close();
             }
        }

        return defaultValue;
    }
    
    // Helper to calculate total for Journals/Receipts/Contras
    private double getVoucherTotal(long voucherId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        
        String query = "SELECT SUM(" + COLUMN_CHARGE_AMOUNT + ") FROM " + TABLE_VOUCHER_CHARGES + 
                       " WHERE " + COLUMN_CHARGE_VOUCHER_ID + "=? AND TRIM(" + COLUMN_CHARGE_VOUCHER_TYPE + ")=?";
                       
        if(type.trim().equalsIgnoreCase("Journal") || type.trim().equalsIgnoreCase("Contra") || type.trim().equalsIgnoreCase("Receipt")) {
            // For Journal/Contra, sum Debits. For Receipt, sum Credits (party side)
            query += " AND " + COLUMN_CHARGE_IS_DEBIT + (type.trim().equalsIgnoreCase("Receipt") ? "=0" : "=1");
        }

        Cursor c = db.rawQuery(query, new String[]{String.valueOf(voucherId), type.trim()});
        if (c != null) {
            if (c.moveToFirst()) {
                total = c.getDouble(0);
            }
            c.close();
        }
        return total;
    }

    // Helper Class for Summary
    public static class VoucherSummary {
        public int id;
        public String type;
        public String voucherNo;
        public String date;
        public String partyName;
        public double amount;
        
        public VoucherSummary(int id, String type, String no, String date, String party, double amount) {
            this.id = id; this.type = type; this.voucherNo = no; this.date = date; this.partyName = party; this.amount = amount;
        }
    }

    public Cursor getVoucher(int id, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (type.equals("Sales")) {
            return db.query(TABLE_INVOICES, null, COLUMN_INVOICE_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        } else if (type.equals("Purchase")) {
             return db.query(TABLE_PURCHASES, null, COLUMN_PURCHASE_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        } else if (type.equals("Payment")) {
             return db.query(TABLE_PAYMENTS, null, COLUMN_PAYMENT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        } else if (type.equals("Journal") || type.equals("Contra")) {
             return db.query(TABLE_JOURNALS, null, COLUMN_JOURNAL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        } else if (type.equals("Receipt")) {
             return db.query(TABLE_RECEIPTS, null, COLUMN_RECEIPT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        }
        return null;
    }
    
    public Cursor getVoucherItems(int id, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (type.equals("Sales")) {
            return db.query(TABLE_INVOICE_ITEMS, null, COLUMN_INV_ID_FK + "=?", new String[]{String.valueOf(id)}, null, null, null);
        } else {
             return db.query(TABLE_PURCHASE_ITEMS, null, COLUMN_PUR_ID_FK + "=?", new String[]{String.valueOf(id)}, null, null, null);
        }
    }
    
    // --- Reporting Methods ---
    public Cursor getCategoryWiseSales() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT i." + COLUMN_ITEM_CATEGORY + ", SUM(ii." + COLUMN_INV_AMOUNT + ") as total " +
                       "FROM " + TABLE_INVOICE_ITEMS + " ii " +
                       "JOIN " + TABLE_ITEMS + " i ON ii." + COLUMN_INV_ITEM_NAME + " = i." + COLUMN_ITEM_NAME + " " +
                       "GROUP BY i." + COLUMN_ITEM_CATEGORY;
        return db.rawQuery(query, null);
    }
    // --- Voucher Charges Methods ---
    public void addVoucherCharge(long voucherId, String voucherType, int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate, boolean isDebit, String paymentMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CHARGE_VOUCHER_ID, voucherId);
        cv.put(COLUMN_CHARGE_VOUCHER_TYPE, voucherType);
        cv.put(COLUMN_CHARGE_LEDGER_ID, ledgerId);
        cv.put(COLUMN_CHARGE_LEDGER_NAME, ledgerName);
        cv.put(COLUMN_CHARGE_AMOUNT, amount);
        cv.put(COLUMN_CHARGE_IS_PERCENTAGE, isPercentage ? 1 : 0);
        cv.put(COLUMN_CHARGE_RATE, rate);
        cv.put(COLUMN_CHARGE_IS_DEBIT, isDebit ? 1 : 0);
        cv.put(COLUMN_CHARGE_PAYMENT_MODE, paymentMode);
        db.insert(TABLE_VOUCHER_CHARGES, null, cv);
    }

    public void addVoucherCharge(long voucherId, String voucherType, int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate, boolean isDebit) {
        addVoucherCharge(voucherId, voucherType, ledgerId, ledgerName, amount, isPercentage, rate, isDebit, "None");
    }


    public void addVoucherCharge(long voucherId, String voucherType, int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate) {
        addVoucherCharge(voucherId, voucherType, ledgerId, ledgerName, amount, isPercentage, rate, false);
    }

    public PaymentVoucher getPaymentVoucher(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        PaymentVoucher voucher = null;
        Cursor cursor = db.query(TABLE_PAYMENTS, null, COLUMN_PAYMENT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String colVoucherNo = "voucher_no"; // Literacy check, verify constant
            // check constants: COLUMN_PAYMENT_VOUCHER_NO = "voucher_no"
            
            String voucherNo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_VOUCHER_NO));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_DATE));
            String throughLedger = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_THROUGH_LEDGER));
            double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_TOTAL_AMOUNT));
            String narration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_NARRATION));
            int companyId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_ID));
            
            // Fetch Particulars (Charges)
            List<VoucherCharge> particulars = new ArrayList<>();
            Cursor cCharges = getVoucherCharges(id, "Payment");
            if (cCharges != null) {
                while(cCharges.moveToNext()) {
                     int lId = cCharges.getInt(cCharges.getColumnIndexOrThrow(COLUMN_CHARGE_LEDGER_ID));
                     String lName = cCharges.getString(cCharges.getColumnIndexOrThrow(COLUMN_CHARGE_LEDGER_NAME));
                     double amt = cCharges.getDouble(cCharges.getColumnIndexOrThrow(COLUMN_CHARGE_AMOUNT));
                     boolean isPct = cCharges.getInt(cCharges.getColumnIndexOrThrow(COLUMN_CHARGE_IS_PERCENTAGE)) == 1;
                     double rate = cCharges.getDouble(cCharges.getColumnIndexOrThrow(COLUMN_CHARGE_RATE));
                     particulars.add(new VoucherCharge(lId, lName, amt, isPct, rate));
                }
                cCharges.close();
            }
            
            voucher = new PaymentVoucher(id, voucherNo, date, throughLedger, totalAmount, narration, companyId, particulars);
            cursor.close();
        }
        return voucher;
    }
    
    public Cursor getVoucherCharges(long voucherId, String voucherType) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_VOUCHER_CHARGES, null, 
            COLUMN_CHARGE_VOUCHER_ID + "=? AND TRIM(" + COLUMN_CHARGE_VOUCHER_TYPE + ")=?", 
            new String[]{String.valueOf(voucherId), voucherType.trim()}, null, null, null);
    }
    
    public void deleteVoucherCharges(long voucherId, String voucherType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOUCHER_CHARGES, 
            COLUMN_CHARGE_VOUCHER_ID + "=? AND TRIM(" + COLUMN_CHARGE_VOUCHER_TYPE + ")=?", 
            new String[]{String.valueOf(voucherId), voucherType.trim()});
    }

    // --- Delete Methods ---
    public void deleteVoucher(int id, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        
        if (type.equals("Sales")) {
            // 1. Revert Stock (Add back sold items)
            cursor = db.query(TABLE_INVOICE_ITEMS, new String[]{COLUMN_INV_ITEM_NAME, COLUMN_INV_QTY}, COLUMN_INV_ID_FK + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String item = cursor.getString(0);
                    double qty = cursor.getDouble(1);
                    updateStock(item, qty); // Add back
                }
                cursor.close();
            }
            
            // 2. Delete
            deleteVoucherCharges(id, "Sales");
            db.delete(TABLE_INVOICE_ITEMS, COLUMN_INV_ID_FK + "=?", new String[]{String.valueOf(id)});
            db.delete(TABLE_INVOICES, COLUMN_INVOICE_ID + "=?", new String[]{String.valueOf(id)});
            
        } else if (type.equals("Purchase")) {
            // 1. Revert Stock (Remove purchased items)
            cursor = db.query(TABLE_PURCHASE_ITEMS, new String[]{COLUMN_PUR_ITEM_NAME, COLUMN_PUR_QTY}, COLUMN_PUR_ID_FK + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String item = cursor.getString(0);
                    double qty = cursor.getDouble(1);
                    updateStock(item, -qty); // Remove
                }
                cursor.close();
            }
            
            // 2. Delete
            deleteVoucherCharges(id, "Purchase");
            db.delete(TABLE_PURCHASE_ITEMS, COLUMN_PUR_ID_FK + "=?", new String[]{String.valueOf(id)});
            db.delete(TABLE_PURCHASES, COLUMN_PURCHASE_ID + "=?", new String[]{String.valueOf(id)});
        } else if (type.equals("Payment")) {
            // Delete Charges
            deleteVoucherCharges(id, "Payment");
            // Delete Payment
            db.delete(TABLE_PAYMENTS, COLUMN_PAYMENT_ID + "=?", new String[]{String.valueOf(id)});
        } else if (type.equals("Journal") || type.equalsIgnoreCase("Contra")) {
            deleteVoucherCharges(id, type);
            db.delete(TABLE_JOURNALS, COLUMN_JOURNAL_ID + "=?", new String[]{String.valueOf(id)});
        } else if (type.equalsIgnoreCase("Receipt")) {
            deleteVoucherCharges(id, "Receipt");
            db.delete(TABLE_RECEIPTS, COLUMN_RECEIPT_ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    public void clearAllJournals(int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete associated charges first
        db.execSQL("DELETE FROM " + TABLE_VOUCHER_CHARGES + " WHERE " + COLUMN_CHARGE_VOUCHER_TYPE + "='Journal' AND " +
                   COLUMN_CHARGE_VOUCHER_ID + " IN (SELECT " + COLUMN_JOURNAL_ID + " FROM " + TABLE_JOURNALS + " WHERE " + COLUMN_COMPANY_ID + "=? AND " + COLUMN_JOURNAL_TYPE + "='Journal')", new Object[]{companyId});
        db.delete(TABLE_JOURNALS, COLUMN_COMPANY_ID + "=? AND (" + COLUMN_JOURNAL_TYPE + "=? OR " + COLUMN_JOURNAL_TYPE + " IS NULL)", new String[]{String.valueOf(companyId), "Journal"});
    }

    public void clearAllContras(int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_VOUCHER_CHARGES + " WHERE " + COLUMN_CHARGE_VOUCHER_TYPE + "='Contra' AND " +
                   COLUMN_CHARGE_VOUCHER_ID + " IN (SELECT " + COLUMN_JOURNAL_ID + " FROM " + TABLE_JOURNALS + " WHERE " + COLUMN_COMPANY_ID + "=? AND " + COLUMN_JOURNAL_TYPE + "='Contra')", new Object[]{companyId});
        db.delete(TABLE_JOURNALS, COLUMN_COMPANY_ID + "=? AND " + COLUMN_JOURNAL_TYPE + "=?", new String[]{String.valueOf(companyId), "Contra"});
    }
    
    public void updateInvoice(long id, Invoice invoice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_INVOICE_NO, invoice.getInvoiceNumber());
        cv.put(COLUMN_INVOICE_DATE, invoice.getDate());
        cv.put(COLUMN_CUSTOMER_NAME, invoice.getCustomerName());
        cv.put(COLUMN_TOTAL_AMOUNT, invoice.getTotalAmount());
        cv.put(COLUMN_DELIVERY_CHARGES, invoice.getDeliveryCharges());
        cv.put(COLUMN_TOTAL_TAX, invoice.getTotalTaxAmount());
        cv.put(COLUMN_GRAND_TOTAL, invoice.getGrandTotal());

        // Detailed Fields
        cv.put(COLUMN_DELIVERY_NOTE, invoice.getDeliveryNote());
        cv.put(COLUMN_MODE_PAYMENT, invoice.getModeOfPayment());
        cv.put(COLUMN_REF_NO, invoice.getReferenceNo());
        cv.put(COLUMN_OTHER_REF, invoice.getOtherReferences());
        cv.put(COLUMN_BUYER_ORDER_NO, invoice.getBuyersOrderNo());
        cv.put(COLUMN_DISPATCH_DOC_NO, invoice.getDispatchDocNo());
        cv.put(COLUMN_DELIVERY_NOTE_DATE, invoice.getDeliveryNoteDate());
        cv.put(COLUMN_DISPATCH_THROUGH, invoice.getDispatchThrough());
        cv.put(COLUMN_DESTINATION, invoice.getDestination());
        cv.put(COLUMN_TERMS_DELIVERY, invoice.getTermsOfDelivery());
        
        cv.put(COLUMN_CONSIGNEE_NAME, invoice.getConsigneeName());
        cv.put(COLUMN_CONSIGNEE_ADDR, invoice.getConsigneeAddress());
        cv.put(COLUMN_CONSIGNEE_GST, invoice.getConsigneeGst());
        cv.put(COLUMN_CONSIGNEE_STATE, invoice.getConsigneeState());
        
        cv.put(COLUMN_BUYER_ADDR, invoice.getBuyerAddress());
        cv.put(COLUMN_BUYER_GST, invoice.getBuyerGst());
        cv.put(COLUMN_BUYER_STATE, invoice.getBuyerState());

        db.update(TABLE_INVOICES, cv, COLUMN_INVOICE_ID + "=?", new String[]{String.valueOf(id)});
    }

    
    public void deleteInvoiceItems(long invoiceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Note: For update, we should technically revert stock for old items first, then add new items and dedcut stock.
        // Assuming the calling activity handles stock logic or we do it here.
        // Safe approach: Revert stock for all items of this invoice, then delete. 
        // Then new items will be added via addInvoiceItem which deducts stock.
        
        Cursor cursor = db.query(TABLE_INVOICE_ITEMS, new String[]{COLUMN_INV_ITEM_NAME, COLUMN_INV_QTY}, COLUMN_INV_ID_FK + "=?", new String[]{String.valueOf(invoiceId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String item = cursor.getString(0);
                double qty = cursor.getDouble(1);
                updateStock(item, qty); // Revert stock (add back)
            }
            cursor.close();
        }
        
        db.delete(TABLE_INVOICE_ITEMS, COLUMN_INV_ID_FK + "=?", new String[]{String.valueOf(invoiceId)});
    }


    // --- Notification Methods ---
    public void addNotification(String title, String message, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NOTIF_TITLE, title);
        cv.put(COLUMN_NOTIF_MESSAGE, message);
        cv.put(COLUMN_NOTIF_TYPE, type);
        cv.put(COLUMN_NOTIF_TIMESTAMP, System.currentTimeMillis());
        cv.put(COLUMN_NOTIF_IS_READ, 0); // Unread by default
        db.insert(TABLE_NOTIFICATIONS, null, cv);
    }

    public Cursor getAllNotifications() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NOTIFICATIONS, null, null, null, null, null, COLUMN_NOTIF_TIMESTAMP + " DESC");
    }

    public void markNotificationAsRead(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NOTIF_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, cv, COLUMN_NOTIF_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public void markAllNotificationsAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NOTIF_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, cv, null, null);
    }

    public int getUnreadNotificationCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + COLUMN_NOTIF_IS_READ + " = 0";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public long getNextVoucherNumber(String type, int companyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String table = "";
        String column = "";
        
        if (type.equals("Sales")) {
            table = TABLE_INVOICES;
            column = COLUMN_INVOICE_NO;
        } else if (type.equals("Purchase")) {
            table = TABLE_PURCHASES;
            column = COLUMN_PURCHASE_INV_NO;
        } else if (type.equals("Payment")) {
            table = TABLE_PAYMENTS;
            column = COLUMN_PAYMENT_VOUCHER_NO;
        } else if (type.equals("Journal")) {
            table = TABLE_JOURNALS;
            column = COLUMN_JOURNAL_NO;
        } else if (type.equals("Contra")) {
            // Contra usually shares numbering with Journal or has its own. Assuming Journal for now or sharing? 
            // Better to have separate or same. Let's assume separate table or same? 
            // In onCreate, I don't see TABLE_CONTRA. 
            // Wait, looking at saveVoucher: if type is Journal OR Contra, it calls addJournal. 
            // So Contra uses TABLE_JOURNALS.
            table = TABLE_JOURNALS;
            column = COLUMN_JOURNAL_NO;
        } else if (type.equals("Receipt")) {
            table = TABLE_RECEIPTS;
            column = COLUMN_RECEIPT_NO;
        }
        
        if (table.isEmpty()) return 1;

        // Use MAX(CAST(column AS INTEGER)) to ensure unique numbers even after deletion
        String query = "SELECT MAX(CAST(" + column + " AS INTEGER)) FROM " + table + " WHERE " + COLUMN_COMPANY_ID + "=?";
        String[] args;
        if (type.equals("Journal") || type.equals("Contra")) {
            query += " AND " + COLUMN_JOURNAL_TYPE + "=?";
            args = new String[]{String.valueOf(companyId), type};
        } else {
            args = new String[]{String.valueOf(companyId)};
        }
        
        Cursor cursor = db.rawQuery(query, args);
        long max = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                max = cursor.getLong(0);
            }
            cursor.close();
        }
        return max + 1;
    }
    // --- Company Methods ---
    public long addCompany(String name, String address, String mobile, String phone2, String email, String state, String logoUri, String gst, String tagline, String cst, String tin, String vatTin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_COMPANY_NAME, name);
        cv.put(COLUMN_COMPANY_ADDRESS, address);
        cv.put(COLUMN_COMPANY_MOBILE, mobile);
        cv.put(COLUMN_COMPANY_PHONE2, phone2);
        cv.put(COLUMN_COMPANY_EMAIL, email);
        cv.put(COLUMN_COMPANY_STATE, state);
        cv.put(COLUMN_COMPANY_LOGO, logoUri);
        cv.put(COLUMN_COMPANY_GST, gst);
        cv.put(COLUMN_COMPANY_TAGLINE, tagline);
        cv.put(COLUMN_COMPANY_CST, cst);
        cv.put(COLUMN_COMPANY_TIN, tin);
        cv.put(COLUMN_COMPANY_VAT_TIN, vatTin);
        return db.insert(TABLE_COMPANIES, null, cv);
    }
    
    public Cursor getCompany(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_COMPANIES, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }
    
    public List<Company> getAllCompanies() {
        List<Company> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPANIES, null, null, null, null, null, COLUMN_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_ADDRESS));
                String gst = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_GST));
                list.add(new Company(id, name, address, gst));
            }
            cursor.close();
        }
        return list;
    }
    
    // --- Report Methods ---
    public Cursor getItemStockSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Return Item Name and Stock
        return db.query(TABLE_ITEMS, 
            new String[]{COLUMN_ITEM_NAME, COLUMN_ITEM_STOCK}, 
            null, null, null, null, COLUMN_ITEM_STOCK + " DESC"); // Order by stock desc
    }
    
    public static class Company {
        public int id;
        public String name;
        public String address;
        public String gst;
        
        public Company(int id, String name, String address, String gst) {
            this.id = id; this.name = name; this.address = address; this.gst = gst;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    public long addJournal(String voucherNo, String date, String narration, String type, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_JOURNAL_NO, voucherNo);
        cv.put(COLUMN_JOURNAL_DATE, date);
        cv.put(COLUMN_JOURNAL_NARRATION, narration);
        cv.put(COLUMN_JOURNAL_TYPE, type);
        cv.put(COLUMN_COMPANY_ID, companyId);
        return db.insert(TABLE_JOURNALS, null, cv);
    }
    
    public void updateJournal(long id, String voucherNo, String date, String narration, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_JOURNAL_NO, voucherNo);
        cv.put(COLUMN_JOURNAL_DATE, date);
        cv.put(COLUMN_JOURNAL_NARRATION, narration);
        cv.put(COLUMN_JOURNAL_TYPE, type);
        db.update(TABLE_JOURNALS, cv, COLUMN_JOURNAL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public long addReceipt(String no, String date, String narration, String through, double total, int companyId, String paymentMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RECEIPT_NO, no);
        cv.put(COLUMN_RECEIPT_DATE, date);
        cv.put(COLUMN_RECEIPT_NARRATION, narration);
        cv.put(COLUMN_RECEIPT_THROUGH, through);
        cv.put(COLUMN_RECEIPT_TOTAL, total);
        cv.put(COLUMN_COMPANY_ID, companyId);
        cv.put(COLUMN_RECEIPT_PAYMENT_MODE, paymentMode);
        try {
            return db.insertOrThrow(TABLE_RECEIPTS, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
            // Try adding the column if it's missing (Self-healing)
            try {
                db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_PAYMENT_MODE + " TEXT");
                // Retry insert
                return db.insertOrThrow(TABLE_RECEIPTS, null, cv);
            } catch (Exception ex) {
                ex.printStackTrace();
                return -1;
            }
        }
    }
    
    public void updateReceipt(long id, String no, String date, String narration, String through, double total, String paymentMode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RECEIPT_NO, no);
        cv.put(COLUMN_RECEIPT_DATE, date);
        cv.put(COLUMN_RECEIPT_NARRATION, narration);
        cv.put(COLUMN_RECEIPT_THROUGH, through);
        cv.put(COLUMN_RECEIPT_TOTAL, total);
        cv.put(COLUMN_RECEIPT_PAYMENT_MODE, paymentMode);
        db.update(TABLE_RECEIPTS, cv, COLUMN_RECEIPT_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public void updatePayment(long id, String voucherNo, String date, String through, double total, String narration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PAYMENT_VOUCHER_NO, voucherNo);
        cv.put(COLUMN_PAYMENT_DATE, date);
        cv.put(COLUMN_PAYMENT_THROUGH_LEDGER, through);
        cv.put(COLUMN_PAYMENT_TOTAL_AMOUNT, total);
        cv.put(COLUMN_PAYMENT_NARRATION, narration);
        db.update(TABLE_PAYMENTS, cv, COLUMN_PAYMENT_ID + "=?", new String[]{String.valueOf(id)});
    }
    // --- Advanced Reporting Methods ---

    public static class LedgerTransaction {
        public String date;
        public String voucherNo;
        public String type;
        public double debit;
        public double credit;
        public String narration;

        public LedgerTransaction(String date, String voucherNo, String type, double debit, double credit, String narration) {
            this.date = date; 
            this.voucherNo = voucherNo; 
            this.type = type; 
            this.debit = debit; 
            this.credit = credit;
            this.narration = narration;
        }
    }

    public List<LedgerTransaction> getLedgerTransactions(String ledgerName) {
        List<LedgerTransaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        verifySchema(db); // Self-Healing

        String[] args = new String[]{ledgerName};

        // 1. Sales Header (If ledger is Customer)
        String qSales = "SELECT " + COLUMN_INVOICE_DATE + ", " + COLUMN_INVOICE_NO + ", 'Sales', " + COLUMN_TOTAL_AMOUNT + ", 0, '' FROM " + TABLE_INVOICES + " WHERE " + COLUMN_CUSTOMER_NAME + "=?";
        try {
             Cursor c1 = db.rawQuery(qSales, args);
             if (c1 != null) {
                 while (c1.moveToNext()) transactions.add(new LedgerTransaction(c1.getString(0), c1.getString(1), c1.getString(2), c1.getDouble(3), c1.getDouble(4), c1.getString(5)));
                 c1.close();
             }
        } catch (Exception e) {}

        // 2. Purchase Header (If ledger is Supplier)
        String qPurch = "SELECT " + COLUMN_PURCHASE_DATE + ", " + COLUMN_PURCHASE_INV_NO + ", 'Purchase', 0, " + COLUMN_PURCHASE_TOTAL + ", '' FROM " + TABLE_PURCHASES + " WHERE " + COLUMN_SUPPLIER_NAME + "=?";
        try {
             Cursor c2 = db.rawQuery(qPurch, args);
             if (c2 != null) {
                 while (c2.moveToNext()) transactions.add(new LedgerTransaction(c2.getString(0), c2.getString(1), c2.getString(2), c2.getDouble(3), c2.getDouble(4), c2.getString(5)));
                 c2.close();
             }
        } catch (Exception e) {}

        // 3. Payment Header (If ledger is Bank/Cash - "Through")
        String qPay = "SELECT " + COLUMN_PAYMENT_DATE + ", " + COLUMN_PAYMENT_VOUCHER_NO + ", 'Payment', 0, " + COLUMN_PAYMENT_TOTAL_AMOUNT + ", " + COLUMN_PAYMENT_NARRATION + " FROM " + TABLE_PAYMENTS + " WHERE " + COLUMN_PAYMENT_THROUGH_LEDGER + "=?";
        try {
             Cursor c3 = db.rawQuery(qPay, args);
             if (c3 != null) {
                 while (c3.moveToNext()) transactions.add(new LedgerTransaction(c3.getString(0), c3.getString(1), c3.getString(2), c3.getDouble(3), c3.getDouble(4), c3.getString(5)));
                 c3.close();
             }
        } catch (Exception e) {}

        // 4. Receipt Header (If ledger is Bank/Cash - "Through")
        String qRec = "SELECT " + COLUMN_RECEIPT_DATE + ", " + COLUMN_RECEIPT_NO + ", 'Receipt', " + COLUMN_RECEIPT_TOTAL + ", 0, " + COLUMN_RECEIPT_NARRATION + " FROM " + TABLE_RECEIPTS + " WHERE " + COLUMN_RECEIPT_THROUGH + "=?";
        try {
            Cursor c4 = db.rawQuery(qRec, args);
            if (c4 != null) {
                while (c4.moveToNext()) transactions.add(new LedgerTransaction(c4.getString(0), c4.getString(1), c4.getString(2), c4.getDouble(3), c4.getDouble(4), c4.getString(5)));
                c4.close();
            }
        } catch (Exception e) {}

        // 5. Charges (Journal, Contra, Payment Party, Receipt Party, etc.)
        // 5a. Journals/Contras
        // Use SAFE query (check if columns exist via try catch or just try catch block)
        try {
            String qJ = "SELECT j." + COLUMN_JOURNAL_DATE + ", j." + COLUMN_JOURNAL_NO + ", j." + COLUMN_JOURNAL_TYPE + ", c." + COLUMN_CHARGE_IS_DEBIT + ", c." + COLUMN_CHARGE_AMOUNT + ", j." + COLUMN_JOURNAL_NARRATION + 
                        " FROM " + TABLE_VOUCHER_CHARGES + " c JOIN " + TABLE_JOURNALS + " j ON c." + COLUMN_CHARGE_VOUCHER_ID + " = j." + COLUMN_JOURNAL_ID + 
                        " WHERE c." + COLUMN_CHARGE_LEDGER_NAME + "=? AND (c." + COLUMN_CHARGE_VOUCHER_TYPE + "='Journal' OR c." + COLUMN_CHARGE_VOUCHER_TYPE + "='Contra')";
            Cursor c5 = db.rawQuery(qJ, args);

        if (c5 != null) {
            while (c5.moveToNext()) {
                boolean isDebit = c5.getInt(3) == 1;
                double amt = c5.getDouble(4);
                transactions.add(new LedgerTransaction(c5.getString(0), c5.getString(1), c5.getString(2), isDebit ? amt : 0, isDebit ? 0 : amt, c5.getString(5)));
            }
            c5.close();
        }
        } catch (Exception e) {}

        // 5b. Payment Party
        try {
            String qP = "SELECT p." + COLUMN_PAYMENT_DATE + ", p." + COLUMN_PAYMENT_VOUCHER_NO + ", 'Payment', c." + COLUMN_CHARGE_IS_DEBIT + ", c." + COLUMN_CHARGE_AMOUNT + ", p." + COLUMN_PAYMENT_NARRATION + 
                        " FROM " + TABLE_VOUCHER_CHARGES + " c JOIN " + TABLE_PAYMENTS + " p ON c." + COLUMN_CHARGE_VOUCHER_ID + " = p." + COLUMN_PAYMENT_ID + 
                        " WHERE c." + COLUMN_CHARGE_LEDGER_NAME + "=? AND c." + COLUMN_CHARGE_VOUCHER_TYPE + "='Payment'";
            Cursor c6 = db.rawQuery(qP, args);
            if (c6 != null) {
                while (c6.moveToNext()) {
                    boolean isDebit = c6.getInt(3) == 1;
                    double amt = c6.getDouble(4);
                    transactions.add(new LedgerTransaction(c6.getString(0), c6.getString(1), c6.getString(2), isDebit ? amt : 0, isDebit ? 0 : amt, c6.getString(5)));
                }
                c6.close();
            }
        } catch (Exception e) {}

        // 5c. Receipt Party
        try {
            String qR = "SELECT r." + COLUMN_RECEIPT_DATE + ", r." + COLUMN_RECEIPT_NO + ", 'Receipt', c." + COLUMN_CHARGE_IS_DEBIT + ", c." + COLUMN_CHARGE_AMOUNT + ", r." + COLUMN_RECEIPT_NARRATION + 
                        " FROM " + TABLE_VOUCHER_CHARGES + " c JOIN " + TABLE_RECEIPTS + " r ON c." + COLUMN_CHARGE_VOUCHER_ID + " = r." + COLUMN_RECEIPT_ID + 
                        " WHERE c." + COLUMN_CHARGE_LEDGER_NAME + "=? AND c." + COLUMN_CHARGE_VOUCHER_TYPE + "='Receipt'";
            Cursor c7 = db.rawQuery(qR, args);
            if (c7 != null) {
                while (c7.moveToNext()) {
                    boolean isDebit = c7.getInt(3) == 1;
                    double amt = c7.getDouble(4);
                    transactions.add(new LedgerTransaction(c7.getString(0), c7.getString(1), c7.getString(2), isDebit ? amt : 0, isDebit ? 0 : amt, c7.getString(5)));
                }
                c7.close();
            }
        } catch (Exception e) {}
        
        // 5d. Sales/Purchase Additional Charges
        try {
             String qChS = "SELECT i." + COLUMN_INVOICE_DATE + ", i." + COLUMN_INVOICE_NO + ", 'Sales', c." + COLUMN_CHARGE_IS_DEBIT + ", c." + COLUMN_CHARGE_AMOUNT + ", ''" + 
                        " FROM " + TABLE_VOUCHER_CHARGES + " c JOIN " + TABLE_INVOICES + " i ON c." + COLUMN_CHARGE_VOUCHER_ID + " = i." + COLUMN_INVOICE_ID + 
                        " WHERE c." + COLUMN_CHARGE_LEDGER_NAME + "=? AND c." + COLUMN_CHARGE_VOUCHER_TYPE + "='Sales'";
            Cursor c8 = db.rawQuery(qChS, args);
            if (c8 != null) {
                while (c8.moveToNext()) {
                    boolean isDebit = c8.getInt(3) == 1;
                    double amt = c8.getDouble(4);
                    transactions.add(new LedgerTransaction(c8.getString(0), c8.getString(1), c8.getString(2), isDebit ? amt : 0, isDebit ? 0 : amt, c8.getString(5)));
                }
                c8.close();
            }
        } catch (Exception e) {}
        
        try {
             String qChP = "SELECT p." + COLUMN_PURCHASE_DATE + ", p." + COLUMN_PURCHASE_INV_NO + ", 'Purchase', c." + COLUMN_CHARGE_IS_DEBIT + ", c." + COLUMN_CHARGE_AMOUNT + ", ''" + 
                        " FROM " + TABLE_VOUCHER_CHARGES + " c JOIN " + TABLE_PURCHASES + " p ON c." + COLUMN_CHARGE_VOUCHER_ID + " = p." + COLUMN_PURCHASE_ID + 
                        " WHERE c." + COLUMN_CHARGE_LEDGER_NAME + "=? AND c." + COLUMN_CHARGE_VOUCHER_TYPE + "='Purchase'";
            Cursor c9 = db.rawQuery(qChP, args);
            if (c9 != null) {
                while (c9.moveToNext()) {
                    boolean isDebit = c9.getInt(3) == 1;
                    double amt = c9.getDouble(4);
                    transactions.add(new LedgerTransaction(c9.getString(0), c9.getString(1), c9.getString(2), isDebit ? amt : 0, isDebit ? 0 : amt, c9.getString(5)));
                }
                c9.close();
            }
        } catch (Exception e) {}

        // Sort by Date (String Comparison or Parse)
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        java.util.Collections.sort(transactions, (o1, o2) -> {
            try {
                if (o1.date == null) return -1;
                if (o2.date == null) return 1;
                Date d1 = sdf.parse(o1.date.replace("/", "-"));
                Date d2 = sdf.parse(o2.date.replace("/", "-"));
                return d1.compareTo(d2);
            } catch (Exception e) {
                return 0; // Keep original order or logic to sort via string?
            }
        });

        return transactions;
    }


    public static class TrialBalanceRow {
        public String ledgerName;
        public String groupName;
        public double debit; 
        public double credit; 

        public TrialBalanceRow(String ledgerName, String groupName, double debit, double credit) {
            this.ledgerName = ledgerName; this.groupName = groupName; this.debit = debit; this.credit = credit;
        }
    }

    private void verifySchema(SQLiteDatabase db) {
        // Self-Healing: Ensure columns exist before running complex reports
        try { db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_TYPE + " TEXT"); } catch (Exception e) {}
        try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_IS_DEBIT + " INTEGER DEFAULT 0"); } catch (Exception e) {}
        try { db.execSQL("ALTER TABLE " + TABLE_VOUCHER_CHARGES + " ADD COLUMN " + COLUMN_CHARGE_PAYMENT_MODE + " TEXT"); } catch (Exception e) {}
        try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_THROUGH + " TEXT"); } catch (Exception e) {}
        try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_TOTAL + " REAL"); } catch (Exception e) {}
        try { db.execSQL("ALTER TABLE " + TABLE_RECEIPTS + " ADD COLUMN " + COLUMN_RECEIPT_PAYMENT_MODE + " TEXT"); } catch (Exception e) {}
    }

    public List<TrialBalanceRow> getTrialBalance() {
        List<TrialBalanceRow> rows = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        verifySchema(db); // Ensure columns exist

        // Get All Ledgers
        String q = "SELECT " + COLUMN_NAME + ", " + COLUMN_GROUP + ", " + COLUMN_BALANCE + ", " + COLUMN_TYPE + " FROM " + TABLE_NAME; 
        Cursor c = null;
        try {
            c = db.rawQuery(q, null);
        } catch (Exception e) {
             // Fallback if column missing despite verify
             q = "SELECT " + COLUMN_NAME + ", " + COLUMN_GROUP + ", " + COLUMN_BALANCE + ", 'Dr' as " + COLUMN_TYPE + " FROM " + TABLE_NAME; 
             c = db.rawQuery(q, null);
        }
        
        if (c != null) {
            while (c.moveToNext()) {
                String name = c.getString(0);
                String group = c.getString(1);
                double opening = c.getDouble(2);
                String type = c.getString(3);
                
                if (type != null && type.equalsIgnoreCase("Cr")) {
                    opening = -opening;
                }
                
                double netBalance = calculateCurrentBalance(name, opening);
                
                if (netBalance > 0) {
                    rows.add(new TrialBalanceRow(name, group, netBalance, 0));
                } else if (netBalance < 0) {
                    rows.add(new TrialBalanceRow(name, group, 0, Math.abs(netBalance)));
                } else {
                    rows.add(new TrialBalanceRow(name, group, 0, 0));
                }
            }
            c.close();
        }
        return rows;
    }

    private double calculateCurrentBalance(String ledgerName, double openingBalance) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = new String[]{ledgerName};
        double balance = openingBalance; 
        
        double totalDr = 0;
        double totalCr = 0;

        // 1. Sales (Ledger is Customer -> Debit)
        totalDr += getSum(db, "SELECT SUM(" + COLUMN_TOTAL_AMOUNT + ") FROM " + TABLE_INVOICES + " WHERE " + COLUMN_CUSTOMER_NAME + "=?", args);

        // 2. Purchase (Ledger is Supplier -> Credit)
        totalCr += getSum(db, "SELECT SUM(" + COLUMN_PURCHASE_TOTAL + ") FROM " + TABLE_PURCHASES + " WHERE " + COLUMN_SUPPLIER_NAME + "=?", args);

        // 3. Payment (Ledger is Bank -> Credit)
        totalCr += getSum(db, "SELECT SUM(" + COLUMN_PAYMENT_TOTAL_AMOUNT + ") FROM " + TABLE_PAYMENTS + " WHERE " + COLUMN_PAYMENT_THROUGH_LEDGER + "=?", args);

        // 4. Receipt (Ledger is Bank -> Debit)
        totalDr += getSum(db, "SELECT SUM(" + COLUMN_RECEIPT_TOTAL + ") FROM " + TABLE_RECEIPTS + " WHERE " + COLUMN_RECEIPT_THROUGH + "=?", args);

        // 5. Charges
        // 5a. Debit Charges
        String qDr = "SELECT SUM(" + COLUMN_CHARGE_AMOUNT + ") FROM " + TABLE_VOUCHER_CHARGES + " WHERE " + COLUMN_CHARGE_LEDGER_NAME + "=? AND " + COLUMN_CHARGE_IS_DEBIT + "=1";
        totalDr += getSum(db, qDr, args);

        // 5b. Credit Charges
        String qCr = "SELECT SUM(" + COLUMN_CHARGE_AMOUNT + ") FROM " + TABLE_VOUCHER_CHARGES + " WHERE " + COLUMN_CHARGE_LEDGER_NAME + "=? AND " + COLUMN_CHARGE_IS_DEBIT + "=0";
        totalCr += getSum(db, qCr, args);

        return balance + (totalDr - totalCr);
    }

    private double getSum(SQLiteDatabase db, String query, String[] args) {
        Cursor c = db.rawQuery(query, args);
        double val = 0;
        if (c != null) {
            if (c.moveToFirst()) val = c.getDouble(0);
            c.close();
        }
        return val;
    }
}