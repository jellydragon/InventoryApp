package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.inventoryapp.data.ProductDbHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.inventoryapp.R.string.category_male;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 1;
    private static int PRODUCT_LOADER = 2;
    private EditText mNameEditText;
    private EditText mSupplierEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEmailEditText;
    private EditText mQuantityEditText;
    private Spinner mCategorySpinner;
    private Button mIncreaseQuantityButton;
    private Button mDecreaseQuantityButton;
    private Button mOrderMoreButton;
    private Button mChooseImageButton;
    private ImageView mProductImageView;

    private int mCategory = 0;
    private SQLiteOpenHelper mDbHelper;
    private Uri currentProductUri;
    private boolean mProductHasChanged;
    private boolean mImageAdded = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    /**
     * Helper method for hiding the keyboard
     */
    private static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        setupUI(findViewById(R.id.main_layout));
        Intent intent = getIntent();
        currentProductUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_product_supplier_email);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mCategorySpinner = (Spinner) findViewById(R.id.spinner_category);
        mOrderMoreButton = (Button) findViewById(R.id.button_order_more);
        mIncreaseQuantityButton = (Button) findViewById(R.id.button_increase_quantity);
        mDecreaseQuantityButton = (Button) findViewById(R.id.button_decrease_quantity);
        mProductImageView = (ImageView) findViewById(R.id.product_image_view);
        mChooseImageButton = (Button) findViewById(R.id.button_choose_image);

        if (currentProductUri == null) {
            setTitle(getString(R.string.add_a_product));
            mQuantityEditText.setText("0");
            mOrderMoreButton.setVisibility(View.GONE);
            mProductImageView.setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_empty_image));
            invalidateOptionsMenu();
        } else {
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
            setTitle(getString(R.string.edit_product));
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mIncreaseQuantityButton.setOnTouchListener(mTouchListener);
        mDecreaseQuantityButton.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mChooseImageButton.setOnTouchListener(mTouchListener);

        mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(mQuantityEditText.getText().toString());
                mQuantityEditText.setText(Integer.toString(quantity + 1));
            }
        });

        mDecreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.valueOf(mQuantityEditText.getText().toString());
                if (quantity > 0) {
                    mQuantityEditText.setText(Integer.toString(quantity - 1));
                }
            }
        });

        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        setupSpinner();

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new ProductDbHelper(this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the category of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(category_male))) {
                        mCategory = ProductEntry.CATEGORY_MALE;
                    } else if (selection.equals(getString(R.string.category_female))) {
                        mCategory = ProductEntry.CATEGORY_FEMALE;
                    } else {
                        mCategory = ProductEntry.CATEGORY_UNISEX;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = ProductEntry.CATEGORY_UNISEX;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (saveProduct()) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveProduct() {
        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        Bitmap bitmap = ((BitmapDrawable) mProductImageView.getDrawable()).getBitmap();
        byte[] imageByteArray = ImageUtils.getBytes(bitmap);

        if (currentProductUri == null) {
            if (TextUtils.isEmpty(nameString)
                    && TextUtils.isEmpty(supplierString)
                    && TextUtils.isEmpty(supplierEmailString)
                    && TextUtils.isEmpty(priceString)) {
                // Since no fields were modified, we can return early without creating a new product.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                return true;
            }
        }

        if (TextUtils.isEmpty(nameString)
                || TextUtils.isEmpty(supplierString)
                || TextUtils.isEmpty(supplierEmailString)
                || TextUtils.isEmpty(priceString)
                || Integer.valueOf(quantityString) <= 0
                || mImageAdded == false) {
            Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_CATEGORY, mCategory);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // there was an error with insertion
                Toast.makeText(this, R.string.editor_insert_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, R.string.editor_insert_product_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            int updateCount = getContentResolver().update(currentProductUri,
                    values,
                    null,
                    null);
            // Show a toast message depending on whether or not the update was successful
            if (updateCount < 1) {
                // there was an error with insertion
                Toast.makeText(this, R.string.error_update, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, R.string.editor_insert_product_successful, Toast.LENGTH_SHORT).show();
            }

        }
        return true;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // make sure it is using a URI for one product.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_CATEGORY,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, final Cursor data) {
        if (data.moveToFirst()) {
            // Fetch values from the db
            final String mNameString = data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
            final String mPriceString = data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));
            final String mSupplierNameString = data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME));
            final String mSupplierEmailString = data.getString(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL));
            final Integer mQuantity = data.getInt(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));
            final Integer mCategory = data.getInt(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_CATEGORY));
            final byte[] mImage = data.getBlob(
                    data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE));

            //update the inputs with the data for the product.
            mNameEditText.setText(mNameString);
            mPriceEditText.setText(mPriceString);
            mSupplierEditText.setText(mSupplierNameString);
            mSupplierEmailEditText.setText(mSupplierEmailString);
            mQuantityEditText.setText(Integer.toString(mQuantity));
            mCategorySpinner.setSelection(mCategory);

            mProductImageView.setImageBitmap(ImageUtils.getImage(mImage));
            mImageAdded = true;

            mOrderMoreButton.setVisibility(View.VISIBLE);
            mOrderMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String emailBody = getString(R.string.email_hello)
                            + " " + mSupplierNameString + ", \n\n"
                            + getString(R.string.email_send_more)
                            + " " + mNameString
                            + "? \n\n"
                            + getString(R.string.email_ending)
                            + "\n";

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + mSupplierEmailString));
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.new_order));
                    intent.putExtra(Intent.EXTRA_TEXT, emailBody);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Set up touch listener for non-text box views to hide keyboard and cursor
     */
    private void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(EditorActivity.this);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        //clear the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierEmailEditText.setText("");
        mQuantityEditText.setText("0");
        mCategorySpinner.setSelection(0);
        mProductImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_empty_image));
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        if (currentProductUri != null) {
            int deleteCount = getContentResolver().delete(currentProductUri, null, null);
            if (deleteCount == 0) {
                Toast.makeText(this, R.string.error_delete, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri mUri = resultData.getData();
                Bitmap image = getBitmapFromUri(mUri);
                mImageAdded = true;
                mProductImageView.setImageBitmap(image);
            }
        }
    }

    private void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mProductImageView.getWidth();
        int targetH = mProductImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}