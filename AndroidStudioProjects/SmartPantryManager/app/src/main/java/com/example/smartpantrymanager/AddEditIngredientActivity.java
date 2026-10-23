package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

/** Add / Edit Ingredient screen, with input validation. */
public class AddEditIngredientActivity extends BaseActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private EditText etName;
    private EditText etQuantity;
    private EditText etExpiry;
    private Spinner spUnit;
    private ArrayAdapter<CharSequence> unitAdapter;
    private DatabaseHelper db;
    private PantryItem editing; // null means we are adding a new item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = DatabaseHelper.getInstance(this);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);

        unitAdapter = ArrayAdapter.createFromResource(this, R.array.units,
                android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        // Did the pantry list send us an item id? Then we are editing that item.
        int id = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (id != -1) {
            editing = db.getPantryItem(id);
        }
        if (editing != null) {
            ((TextView) findViewById(R.id.tvFormTitle)).setText("Edit ingredient");
            etName.setText(editing.name);
            etQuantity.setText(Fmt.qty(editing.quantity));
            spUnit.setSelection(unitAdapter.getPosition(editing.unit));
            if (editing.expiry != null) {
                etExpiry.setText(editing.expiry);
            }
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        etExpiry.setOnLongClickListener(v -> {
            etExpiry.setText("");
            return true;
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> etExpiry.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        boolean valid = true;

        // ---- Input validation ----
        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches("[\\p{L} '\\-]+")) {
            etName.setError("Use letters only");
            valid = false;
        }

        double qty = 0;
        try {
            qty = Double.parseDouble(qtyText);
            if (qty <= 0) {
                etQuantity.setError("Quantity must be greater than 0");
                valid = false;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            valid = false;
        }

        if (!valid) {
            return; // errors are showing on the fields, so do not save
        }

        String unit = spUnit.getSelectedItem().toString();
        String expiryValue = expiry.isEmpty() ? null : expiry;

        if (editing == null) {
            db.insertPantryItem(new PantryItem(0, name, qty, unit, expiryValue));   // CREATE
        } else {
            editing.name = name;
            editing.quantity = qty;
            editing.unit = unit;
            editing.expiry = expiryValue;
            db.updatePantryItem(editing);                                          // UPDATE
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish(); // close this screen and go back to the list
    }
}
