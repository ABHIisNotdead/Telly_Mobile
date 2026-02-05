package com.example.tellymobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "TellyMobile.db";
    private static final int DATABASE_VERSION = 17; // Incremented to force schema Fix

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

    // Purchases Table
    public static final String TABLE_PURCHASES = "purchases";
    public static final String COLUMN_PURCHASE_ID = "_id";
    public static final String COLUMN_PURCHASE_INV_NO = "purchase_inv_no";
    public static final String COLUMN_PURCHASE_DATE = "purchase_date";
    public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
    public static final String COLUMN_PURCHASE_TOTAL = "purchase_total";

    // Purchase Items Table
    public static final String TABLE_PURCHASE_ITEMS = "purchase_items";
    public static final String COLUMN_PUR_ITEM_ID = "_id";
    public static final String COLUMN_PUR_ID_FK = "purchase_id";
    public static final String COLUMN_PUR_ITEM_NAME = "item_name";
    public static final String COLUMN_PUR_QTY = "quantity";
    public static final String COLUMN_PUR_RATE = "rate";
    public static final String COLUMN_PUR_AMOUNT = "amount";

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
    
    // Notifications Table
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIF_ID = "_id";
    public static final String COLUMN_NOTIF_TITLE = "title";
    public static final String COLUMN_NOTIF_MESSAGE = "message";
    public static final String COLUMN_NOTIF_TYPE = "type"; // Info, Success, Warning
    public static final String COLUMN_NOTIF_TIMESTAMP = "timestamp";
    public static final String COLUMN_NOTIF_IS_READ = "is_read";
    
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
                COLUMN_COMPANY_TAGLINE + " TEXT);";
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
                COLUMN_COMPANY_ID + " INTEGER DEFAULT 0);";
        db.execSQL(queryPurchases);

        String queryPurchaseItems = "CREATE TABLE " + TABLE_PURCHASE_ITEMS + " (" +
                COLUMN_PUR_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PUR_ID_FK + " INTEGER, " +
                COLUMN_PUR_ITEM_NAME + " TEXT, " +
                COLUMN_PUR_QTY + " REAL, " +
                COLUMN_PUR_RATE + " REAL, " +
                COLUMN_PUR_AMOUNT + " REAL);";
        db.execSQL(queryPurchaseItems);
        
        String queryVoucherCharges = "CREATE TABLE " + TABLE_VOUCHER_CHARGES + " (" +
                COLUMN_CHARGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CHARGE_VOUCHER_ID + " INTEGER, " +
                COLUMN_CHARGE_VOUCHER_TYPE + " TEXT, " +
                COLUMN_CHARGE_LEDGER_ID + " INTEGER, " +
                COLUMN_CHARGE_LEDGER_NAME + " TEXT, " +
                COLUMN_CHARGE_AMOUNT + " REAL, " +
                COLUMN_CHARGE_IS_PERCENTAGE + " INTEGER, " +
                COLUMN_CHARGE_RATE + " REAL);";
        db.execSQL(queryVoucherCharges);

        String queryNotifications = "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                COLUMN_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTIF_TITLE + " TEXT, " +
                COLUMN_NOTIF_MESSAGE + " TEXT, " +
                COLUMN_NOTIF_TYPE + " TEXT, " +
                COLUMN_NOTIF_TIMESTAMP + " INTEGER, " +
                COLUMN_NOTIF_IS_READ + " INTEGER DEFAULT 0);";
        db.execSQL(queryNotifications);
        
        insertDefaultGroups(db);
    }

    private void insertDefaultGroups(SQLiteDatabase db) {
        String[] defaults = {
            "Sundry Debtors", "Sundry Creditors", "Duties & Taxes", "Bank Accounts", 
            "Cash-in-hand", "Sales Accounts", "Purchase Accounts", 
            "Direct Expenses", "Indirect Expenses", "Indirect Incomes"
        };
        for (String group : defaults) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_GROUP_NAME, group);
            cv.put(COLUMN_GROUP_PARENT, "Primary");
            db.insert(TABLE_GROUPS, null, cv);
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

    public List<String> getLedgersByGroupList(String groupName) {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_NAME}, COLUMN_GROUP + "=?", new String[]{groupName}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
            cursor.close();
        }
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



    public void addItem(String name, double rate, String unit, double stock, String hsn, String group, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ITEM_NAME, name);
        cv.put(COLUMN_ITEM_RATE, rate);
        cv.put(COLUMN_ITEM_UNIT, unit);
        cv.put(COLUMN_ITEM_STOCK, stock);
        cv.put(COLUMN_ITEM_HSN, hsn);
        cv.put(COLUMN_ITEM_GROUP, group);
        cv.put(COLUMN_ITEM_CATEGORY, category);
        db.insert(TABLE_ITEMS, null, cv);
    }
    
    public void updateItem(int id, String name, double rate, String unit, double stock, String hsn, String group, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ITEM_NAME, name);
        cv.put(COLUMN_ITEM_RATE, rate);
        cv.put(COLUMN_ITEM_UNIT, unit);
        cv.put(COLUMN_ITEM_STOCK, stock);
        cv.put(COLUMN_ITEM_HSN, hsn);
        cv.put(COLUMN_ITEM_GROUP, group);
        cv.put(COLUMN_ITEM_CATEGORY, category);
        db.update(TABLE_ITEMS, cv, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
    }
    
    public Cursor getItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Aliasing to match ItemActivity.java expectations
        String query = "SELECT " + COLUMN_ITEM_NAME + ", " +
                       COLUMN_ITEM_RATE + ", " + COLUMN_ITEM_UNIT + ", " +
                       COLUMN_ITEM_STOCK + " as stock_quantity, " + 
                       COLUMN_ITEM_HSN + " as hsn_sac, " +
                       COLUMN_ITEM_GROUP + " as stock_group, " + 
                       COLUMN_ITEM_CATEGORY + " as stock_category " +
                       " FROM " + TABLE_ITEMS + " WHERE " + COLUMN_ITEM_ID + "=?";
        return db.rawQuery(query, new String[]{String.valueOf(id)});
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
        cv.put(COLUMN_DESTINATION, invoice.getDestination());
        cv.put(COLUMN_TERMS_DELIVERY, invoice.getTermsOfDelivery());
        cv.put(COLUMN_BILL_OF_LADING, invoice.getBillOfLading());
        cv.put(COLUMN_MOTOR_VEHICLE_NO, invoice.getMotorVehicleNo());
        
        cv.put(COLUMN_CONSIGNEE_NAME, invoice.getConsigneeName());
        cv.put(COLUMN_CONSIGNEE_ADDR, invoice.getConsigneeAddress());
        cv.put(COLUMN_CONSIGNEE_GST, invoice.getConsigneeGst());
        cv.put(COLUMN_CONSIGNEE_STATE, invoice.getConsigneeState());
        
        cv.put(COLUMN_BUYER_GST, invoice.getBuyerGst());
        cv.put(COLUMN_BUYER_STATE, invoice.getBuyerState());
        
        cv.put("bank_ledger_id", invoice.getBankLedgerId());
        
        // Inherit Company ID from invoice object or pass as argument? 
        // Better to pass as argument to keep POJO clean or add field to POJO.
        // For now, overloading/modifying:
        return db.insert(TABLE_INVOICES, null, cv);
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
        cv.put(COLUMN_DELIVERY_NOTE, invoice.getDeliveryNote());
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
        
        cv.put(COLUMN_COMPANY_ID, companyId);
        
        return db.insert(TABLE_INVOICES, null, cv);
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
    public long addPurchase(String invoiceNo, String date, String supplierName, double totalAmount, int companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PURCHASE_INV_NO, invoiceNo);
        cv.put(COLUMN_PURCHASE_DATE, date);
        cv.put(COLUMN_SUPPLIER_NAME, supplierName);
        cv.put(COLUMN_PURCHASE_TOTAL, totalAmount);
        cv.put(COLUMN_COMPANY_ID, companyId);
        return db.insert(TABLE_PURCHASES, null, cv);
    }
    
    // Legacy overload
    public long addPurchase(String invoiceNo, String date, String supplierName, double totalAmount) {
         return addPurchase(invoiceNo, date, supplierName, totalAmount, 0);
    }

    public void addPurchaseItem(long purchaseId, String itemName, double qty, double rate, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PUR_ID_FK, purchaseId);
        cv.put(COLUMN_PUR_ITEM_NAME, itemName);
        cv.put(COLUMN_PUR_QTY, qty);
        cv.put(COLUMN_PUR_RATE, rate);
        cv.put(COLUMN_PUR_AMOUNT, amount);
        db.insert(TABLE_PURCHASE_ITEMS, null, cv);
        
        // Increase Stock for Purchases
        updateStock(itemName, qty);
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
        
        // Sorting could be done here or in SQL UNION, but List sort is easier for now
        // list.sort(...) based on date
        return list;
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
        } else {
             return db.query(TABLE_PURCHASES, null, COLUMN_PURCHASE_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        }
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
    public void addVoucherCharge(long voucherId, String voucherType, int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CHARGE_VOUCHER_ID, voucherId);
        cv.put(COLUMN_CHARGE_VOUCHER_TYPE, voucherType);
        cv.put(COLUMN_CHARGE_LEDGER_ID, ledgerId);
        cv.put(COLUMN_CHARGE_LEDGER_NAME, ledgerName);
        cv.put(COLUMN_CHARGE_AMOUNT, amount);
        cv.put(COLUMN_CHARGE_IS_PERCENTAGE, isPercentage ? 1 : 0);
        cv.put(COLUMN_CHARGE_RATE, rate);
        db.insert(TABLE_VOUCHER_CHARGES, null, cv);
    }
    
    public Cursor getVoucherCharges(long voucherId, String voucherType) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_VOUCHER_CHARGES, null, 
            COLUMN_CHARGE_VOUCHER_ID + "=? AND " + COLUMN_CHARGE_VOUCHER_TYPE + "=?", 
            new String[]{String.valueOf(voucherId), voucherType}, null, null, null);
    }
    
    public void deleteVoucherCharges(long voucherId, String voucherType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOUCHER_CHARGES, 
            COLUMN_CHARGE_VOUCHER_ID + "=? AND " + COLUMN_CHARGE_VOUCHER_TYPE + "=?", 
            new String[]{String.valueOf(voucherId), voucherType});
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
            db.delete(TABLE_PURCHASE_ITEMS, COLUMN_PUR_ID_FK + "=?", new String[]{String.valueOf(id)});
            db.delete(TABLE_PURCHASES, COLUMN_PURCHASE_ID + "=?", new String[]{String.valueOf(id)});
        }
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

    public long getNextVoucherNumber(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = 0;
        if (type.equals("Sales")) {
            count = android.database.DatabaseUtils.queryNumEntries(db, TABLE_INVOICES);
        } else if (type.equals("Purchase")) {
            count = android.database.DatabaseUtils.queryNumEntries(db, TABLE_PURCHASES);
        }
        return count + 1;
    }
    // --- Company Methods ---
    public long addCompany(String name, String address, String mobile, String phone2, String email, String state, String logoUri, String gst, String tagline) {
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
}