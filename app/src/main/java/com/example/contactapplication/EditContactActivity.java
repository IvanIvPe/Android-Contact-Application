package com.example.contactapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditContactActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA_PERMISSION = 123;
    private static final int REQUEST_CODE_GALLERY = 1;

    private EditText editTextName;
    private EditText editTextPhone;
    private Button btnUpdate;
    private ImageView imgProfile;

    private DBHelper dbHelper;
    private Contact contact;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (imageUri != null) {
                        imgProfile.setImageURI(imageUri);
                        contact.setProfilePictureUri(imageUri.toString());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        btnUpdate = findViewById(R.id.btnUpdate);
        imgProfile = findViewById(R.id.imgProfile);

        dbHelper = new DBHelper(this);

        contact = (Contact) getIntent().getSerializableExtra(ContactDetailActivity.EXTRA_CONTACT);

        if (contact != null) {

            editTextName.setText(contact.getName());
            editTextPhone.setText(contact.getPhone());

            btnUpdate.setOnClickListener(v -> updateContact());

            imgProfile.setOnClickListener(this::onProfileImageClick);

            String profilePictureUri = contact.getProfilePictureUri();

            if (profilePictureUri != null) {
                loadProfileImage(Uri.parse(profilePictureUri));
            } else {
                imgProfile.setImageResource(R.drawable.ic_default_contact);
            }
        } else {
            Toast.makeText(this, "Invalid contact data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateContact() {

        String updatedName = editTextName.getText().toString().trim();
        String updatedPhone = editTextPhone.getText().toString().trim();

        if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
            Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        contact.setName(updatedName);
        contact.setPhone(updatedPhone);

        int rowsAffected = dbHelper.updateContact(contact);

        if (rowsAffected > 0) {
            Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent().putExtra(ContactDetailActivity.EXTRA_CONTACT, contact));
            finish();
        } else {
            Toast.makeText(this, "Failed to update contact", Toast.LENGTH_SHORT).show();
        }
    }

    public void onProfileImageClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");

        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    launchCamera();
                    break;
                case 1:
                    openGallery();
                    break;
            }
        });

        builder.show();
    }

    private void launchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    imageUri = FileProvider.getUriForFile(
                            this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile
                    );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    takePictureLauncher.launch(takePictureIntent);
                } else {
                    Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                loadProfileImage(selectedImageUri);
                contact.setProfilePictureUri(selectedImageUri.toString());
            }
        }
    }

    private void loadProfileImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_default_contact)
                .error(R.drawable.ic_default_contact)
                .into(imgProfile);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("contact", contact);
        outState.putParcelable("imageUri", imageUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        contact = (Contact) savedInstanceState.getSerializable("contact");
        imageUri = savedInstanceState.getParcelable("imageUri");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
