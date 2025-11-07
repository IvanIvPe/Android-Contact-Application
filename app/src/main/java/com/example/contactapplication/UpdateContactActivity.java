package com.example.contactapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateContactActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT = "extra_contact";

    private EditText editTextName;
    private EditText editTextPhone;

    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        Button btnUpdate = findViewById(R.id.btnUpdate);

        contact = (Contact) getIntent().getSerializableExtra(EXTRA_CONTACT);

        if (contact != null) {

            editTextName.setText(contact.getName());
            editTextPhone.setText(contact.getPhone());

            btnUpdate.setOnClickListener(v -> updateContact());
        } else {
            Toast.makeText(this, "Invalid contact data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateContact() {
        String updatedName = editTextName.getText().toString().trim();
        String updatedPhone = editTextPhone.getText().toString().trim();

        if (!updatedName.isEmpty() && !updatedPhone.isEmpty()) {

            Contact updatedContact = new Contact(contact.getId(), updatedName, updatedPhone, null);

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_CONTACT, updatedContact);
            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        } else {

            Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }
}

