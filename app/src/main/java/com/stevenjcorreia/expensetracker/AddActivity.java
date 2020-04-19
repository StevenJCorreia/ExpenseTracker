package com.stevenjcorreia.expensetracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = AddActivity.class.getName();

    private static ArrayList<String> categoryList = new ArrayList<>();
    private ExpenseItemDatabase expenseItemDatabase = null;
    private Context context = this;
    private boolean hasIntentExtras = false;
    private boolean imageSet = false;

    private Spinner categories;
    private Button selectDate, submitExpense;
    TextView errorMessage;
    private EditText amountValue;
    private ImageView addCategory, addImage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case Utils.PICK_IMAGE:
                Uri imageUri = data != null ? data.getData() : null;
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    addImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e(TAG, "IOException.", e);
                }
                break;
            case Utils.TAKE_IMAGE:
                Bitmap imageBitmap = (Bitmap) (data != null ? Objects.requireNonNull(data.getExtras()).get("data") : null);
                addImage.setImageBitmap(imageBitmap);
                break;
        }

        imageSet = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        addCategory = findViewById(R.id.addCategory);
        amountValue = findViewById(R.id.amountValue);
        categories = findViewById(R.id.categories);
        selectDate = findViewById(R.id.selectDate);
        addImage = findViewById(R.id.addImage);
        submitExpense = findViewById(R.id.submitExpense);
        errorMessage = findViewById(R.id.errorMessage);

        final ExpenseItem item = (ExpenseItem) getIntent().getSerializableExtra("item");

        expenseItemDatabase = Room.databaseBuilder(getApplicationContext(), ExpenseItemDatabase.class, "Expense").allowMainThreadQueries().build();

        refreshCategoryList();

        final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, categoryList);
        categoriesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        categories.setAdapter(categoriesAdapter);

        if (item != null) {
            setTitle(R.string.edit_activity_title);
            autoFillData(item);
        } else {
            selectDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date()));
        }

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_add, null);

                alertBuilder.setView(view);
                final AlertDialog dialog = alertBuilder.create();
                dialog.show();

                final TextView categoryValue = dialog.findViewById(R.id.categoryValue);
                final TextView errorMessage = dialog.findViewById(R.id.errorMessage);
                Button submitCategory = dialog.findViewById(R.id.submitCategory);

                assert categoryValue != null;
                categoryValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (categoryValue.getText().toString().length() == 0) {
                            errorMessage.setText("Category item cannot be blank.");
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setVisibility(View.VISIBLE);
                        } else if (categoryList.indexOf(categoryValue.getText().toString()) != -1) {
                            errorMessage.setText("Category item \"" + categoryValue.getText().toString() + "\" already exists.");
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setVisibility(View.VISIBLE);
                        } else {
                            categoryValue.setTextColor(Color.BLACK);
                            errorMessage.setVisibility(View.GONE);
                        }
                    }
                });

                assert submitCategory != null;
                submitCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    if (categoryValue.getText().toString().length() == 0 || categoryList.indexOf(categoryValue.getText().toString()) != -1) {
                        return;
                    }

                    categoryList.add(categoryValue.getText().toString());
                    Category.addCategoryToFile(categoryValue.getText().toString(), context);
                    categoriesAdapter.notifyDataSetChanged();
                    categories.setSelection(categoryList.size() - 1);

                    dialog.dismiss();
                    }
                });
            }
        });

        addImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (imageSet) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    View view = getLayoutInflater().inflate(R.layout.dialog_image_edit, null);

                    alertBuilder.setView(view);
                    final AlertDialog dialog = alertBuilder.create();
                    dialog.show();

                    TextView changePicture, removePicture, cancelImageEdit;
                    changePicture = dialog.findViewById(R.id.changePicture);
                    removePicture = dialog.findViewById(R.id.removePicture);
                    cancelImageEdit = dialog.findViewById(R.id.cancelImageEdit);

                    changePicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            addImage.performClick();
                        }
                    });

                    removePicture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addImage.setImageResource(R.drawable.ic_photo_camera_black_24dp);
                            imageSet = false;

                            dialog.dismiss();
                        }
                    });

                    cancelImageEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }

                return false;
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_image_add, null);

                alertBuilder.setView(view);
                final AlertDialog dialog = alertBuilder.create();
                dialog.show();

                TextView takePicture, selectPicture;
                takePicture = dialog.findViewById(R.id.takePicture);
                selectPicture = dialog.findViewById(R.id.selectPicture);

                takePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, Utils.TAKE_IMAGE);

                        dialog.dismiss();
                    }
                });

                selectPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Utils.PICK_IMAGE);

                        dialog.dismiss();
                    }
                });
            }
        });

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        submitExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (!formValid()) {
                return;
            }

            if (!imageSet) {
                new AlertDialog.Builder(context)
                        .setTitle("No Image")
                        .setMessage("Are you sure you want to add this expense without an image?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (hasIntentExtras) {
                                    item.setPrice(Double.parseDouble(amountValue.getText().toString()));
                                    item.setCategory(categories.getSelectedItem().toString());
                                    item.setDate(selectDate.getText().toString());

                                    expenseItemDatabase.expenseItemDao().updateExpenseItem(item);
                                } else {
                                    ExpenseItem item = new ExpenseItem(Double.parseDouble(amountValue.getText().toString()), categories.getSelectedItem().toString(), selectDate.getText().toString(), null);
                                    expenseItemDatabase.expenseItemDao().insertExpenseItem(item);
                                }

                                startActivity(new Intent(context, MainActivity.class));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return;
            }

            if (hasIntentExtras) {
                item.setPrice(Double.parseDouble(amountValue.getText().toString()));
                item.setCategory(categories.getSelectedItem().toString());
                item.setDate(selectDate.getText().toString());

                expenseItemDatabase.expenseItemDao().updateExpenseItem(item);
            } else {
                ExpenseItem item = new ExpenseItem(Double.parseDouble(amountValue.getText().toString()), categories.getSelectedItem().toString(), selectDate.getText().toString(), Utils.imageViewToByteArray(addImage));
                expenseItemDatabase.expenseItemDao().insertExpenseItem(item);
            }

            startActivity(new Intent(context, MainActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        selectDate.setText(DateFormat.getDateInstance().format(c.getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_back_to_expenses) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void autoFillData(ExpenseItem item) {
        hasIntentExtras = true;

        amountValue.setText(String.valueOf(item.getPrice()));

        if (categoryList.indexOf(item.getCategory()) == -1) {
            categoryList.add(item.getCategory());
            Category.addCategoryToFile(item.getCategory(), context);
            categories.setSelection(Category.getCategoryList(context).size() - 1);
        } else {
            categories.setSelection(categoryList.indexOf(item.getCategory()));
        }

        selectDate.setText(item.getDate());

        if (item.getImage() != null) {
            addImage.setImageBitmap(Utils.byteArrayToBitmap(item.getImage()));
        }
    }

    private boolean formValid() {
        if (categories.getSelectedItem() == null) {
            Log.d(TAG, "formValid: Category not selected.");

            errorMessage.setText("Please select a category.");
            errorMessage.setTextColor(Color.RED);
            errorMessage.setVisibility(View.VISIBLE);

            return false;
        }

        if (amountValue.getText().toString().length() == 0) {
            Log.d(TAG, "formValid: Amount not defined.");

            errorMessage.setText("Please enter an amount.");
            errorMessage.setTextColor(Color.RED);
            errorMessage.setVisibility(View.VISIBLE);

            return false;
        }

        if (!isDouble(amountValue.getText().toString())) {
            Log.d(TAG, "formValid: Incorrect amount format.");

            errorMessage.setText("Please enter a non-integer amount.");
            errorMessage.setTextColor(Color.RED);
            errorMessage.setVisibility(View.VISIBLE);

            return false;
        }

        return true;
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, String.format("isDouble: %s is not a double.", s), e);
            return false;
        }
    }

    private void refreshCategoryList() {
        categoryList = Category.getCategoryList(context);
    }
}
