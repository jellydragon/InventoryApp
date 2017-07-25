package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;


/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        final TextView productName = (TextView) view.findViewById(R.id.name);
        final TextView productQuantity = (TextView) view.findViewById(R.id.quantity);
        final TextView productPrice = (TextView) view.findViewById(R.id.price);
        final Button sellButton = (Button) view.findViewById(R.id.button_sale);

        // Extract properties from cursor
        final String id = cursor.getString(cursor.getColumnIndexOrThrow(
                ProductEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_NAME));
        final Integer quantity = cursor.getInt(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_QUANTITY));
        final Integer price = cursor.getInt(cursor.getColumnIndexOrThrow(
                ProductEntry.COLUMN_PRODUCT_PRICE));

        // Populate fields with extracted properties
        productName.setText(name);
        productQuantity.setText(String.valueOf(quantity));
        productPrice.setText(String.valueOf(price));

        // Set button click listener
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);
                    String selection = ProductEntry._ID + "=?";

                    String[] selectionArgs = new String[]{String.valueOf(id)};
                    int rowsUpdated = context.getContentResolver().update(
                            ProductEntry.CONTENT_URI,
                            contentValues,
                            selection,
                            selectionArgs);
                    if (rowsUpdated > 0) {
                        productQuantity.setText(String.valueOf(quantity - 1));
                    }
                }
            }
        });
    }
}
