package com.example.contactapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class ContactDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT = "extra_contact";
    public static final int EDIT_CONTACT_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CALL_PHONE = 2;

    private Contact contact;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        dbHelper = new DBHelper(this);

        contact = (Contact) getIntent().getSerializableExtra(EXTRA_CONTACT);

        if (contact != null) {
            Log.d("ContactDetailActivity", "Contact ID: " + contact.getId());
            displayContactDetails(contact);

            findViewById(R.id.btnEdit).setOnClickListener(v -> openEditContactActivity());
            findViewById(R.id.imgProfile).setOnClickListener(v -> openImagePicker());

            ImageButton btnCall = findViewById(R.id.btnCall);
            btnCall.setOnClickListener(v -> initiateCall(contact.getPhone()));


            Button btnDelete = findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(v -> deleteContact());
        } else {
            Toast.makeText(this, "Invalid contact data", Toast.LENGTH_SHORT).show();
            finish();
        }

        checkAndRequestPermission();
    }

    private void displayContactDetails(Contact contact) {
        ImageView imgProfile = findViewById(R.id.imgProfile);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtPhone = findViewById(R.id.txtPhone);

        if (contact.getProfilePictureUri() != null) {
            Glide.with(this)
                    .load(Uri.parse(contact.getProfilePictureUri()))
                    .placeholder(R.drawable.ic_default_contact)
                    .error(R.drawable.ic_default_contact)
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.ic_default_contact);
        }

        txtName.setText(contact.getName());
        txtPhone.setText(contact.getPhone());
    }

    private void checkAndRequestPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        }
    }

    private void initiateCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            String dialUri = "tel:" + phoneNumber;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dialUri)));
        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        }
    }


    private void openEditContactActivity() {
        Intent intent = new Intent(this, EditContactActivity.class);
        intent.putExtra(EXTRA_CONTACT, contact);
        startActivityForResult(intent, EDIT_CONTACT_REQUEST);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Contact updatedContact = (Contact) data.getSerializableExtra(EXTRA_CONTACT);

            if (updatedContact != null) {
                contact = updatedContact;
                displayContactDetails(contact);
                saveContact();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            if (contact != null) {
                contact.setProfilePictureUri(selectedImageUri.toString());
                saveContact();
                updateProfilePicture(selectedImageUri);
            }
        }
    }

    private void updateProfilePicture(Uri imageUri) {
        ImageView imgProfile = findViewById(R.id.imgProfile);

        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_default_contact)
                .error(R.drawable.ic_default_contact)
                .into(imgProfile);
    }

    private void saveContact() {
        Log.d("ContactDetailActivity", "Saving contact: " + contact.getName());
        if (contact != null) {
            int rowsAffected = dbHelper.updateContact(contact);

            if (rowsAffected > 0) {
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update contact", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid contact data", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this contact?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                performContactDeletion();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.create().show();
    }

    private void performContactDeletion() {
        if (contact != null) {
            int rowsAffected = dbHelper.deleteContactById(contact.getId());

            if (rowsAffected > 0) {
                Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("DeleteContact", "Contact is null. Cannot delete.");
        }
    }
}
