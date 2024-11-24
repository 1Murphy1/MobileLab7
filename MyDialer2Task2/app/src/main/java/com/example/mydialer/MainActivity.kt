package com.example.mydialer

import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException

data class Contact(
    val name: String,
    val phone: String,
    val type: String
)

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var contactsList: List<Contact>
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        searchEditText = findViewById(R.id.et_search)
        val recyclerView = findViewById<RecyclerView>(R.id.rView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter()
        recyclerView.adapter = contactAdapter

        loadContacts("https://drive.google.com/uc?export=download&id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR")

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, after: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val searchText = editable.toString()
                Timber.d("Searching for: $searchText")

                if (searchText.isEmpty()) {
                    contactAdapter.submitList(contactsList)
                } else {
                    val filteredContacts = contactsList.filter { contact ->
                        contact.name.contains(searchText, ignoreCase = true)
                    }
                    contactAdapter.submitList(filteredContacts)
                }
            }
        })
    }

    private fun loadContacts(url: String) {
        Thread {
            val request = Request.Builder().url(url).build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    if (json != null) {
                        val contacts = parseContacts(json)
                        runOnUiThread {
                            contactsList = contacts
                            contactAdapter.submitList(contactsList)
                        }
                    }
                } else {
                    Timber.e("Failed to access file: ${response.message}")
                }
            } catch (e: IOException) {
                Timber.e(e, "Error loading contacts")
            }
        }.start()
    }

    private fun parseContacts(json: String): List<Contact> {
        val listType = object : TypeToken<List<Contact>>() {}.type
        return com.google.gson.Gson().fromJson(json, listType)
    }
}
