package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    public static abstract class ProductEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * Names for the products table
         */
        public static final String TABLE_NAME = "products";
        public final static String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_CATEGORY = "category";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_PRODUCT_SUPPLIER_EMAIL = "supplier_email";
        public static final String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * Possible values for the category.
         */
        public static final int CATEGORY_UNISEX = 0;
        public static final int CATEGORY_MALE = 1;
        public static final int CATEGORY_FEMALE = 2;

        /**
         * Returns whether or not the given category is {@link #CATEGORY_UNISEX}, {@link #CATEGORY_MALE},
         * or {@link #CATEGORY_FEMALE}.
         */
        public static boolean isValidCategory(int category) {
            return (category == CATEGORY_UNISEX || category == CATEGORY_MALE || category == CATEGORY_FEMALE);
        }
    }
}
