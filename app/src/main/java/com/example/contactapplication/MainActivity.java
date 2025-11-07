package com.example.contactapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        contactList = dbHelper.getAllContacts();

        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        contactAdapter = new ContactAdapter(this, contactList);
        contactAdapter.setOnItemClickListener(contact -> openContactDetailActivity(contact));
        recyclerViewContacts.setAdapter(contactAdapter);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> openAddContactActivity());
    }

    @Override
    protected void onResume() {
        super.onResume();
        contactList = dbHelper.getAllContacts();
        contactAdapter.setContactList(contactList);
    }

    private void openAddContactActivity() {
        Intent intent = new Intent(this, AddContactActivity.class);
        startActivityForResult(intent, AddContactActivity.ADD_CONTACT_REQUEST);
    }

    private void openContactDetailActivity(Contact contact) {
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT, contact);
        startActivityForResult(intent, ContactDetailActivity.EDIT_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == AddContactActivity.ADD_CONTACT_REQUEST || requestCode == ContactDetailActivity.EDIT_CONTACT_REQUEST) {
                contactList = dbHelper.getAllContacts();
                contactAdapter.setContactList(contactList);

                if (requestCode == AddContactActivity.ADD_CONTACT_REQUEST) {
                    Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                } else if (requestCode == ContactDetailActivity.EDIT_CONTACT_REQUEST) {
                    Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
