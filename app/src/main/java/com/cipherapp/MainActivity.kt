package com.cipherapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cipherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Real-time encrypt
        b.encryptInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val result = CipherEngine.encrypt(s?.toString() ?: "")
                b.encryptOutput.text = result
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {}
        })

        // Real-time decrypt
        b.decryptInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val result = CipherEngine.decrypt(s?.toString() ?: "")
                b.decryptOutput.text = result
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {}
        })

        // Copy buttons
        b.copyEncrypt.setOnClickListener {
            copy(b.encryptOutput.text?.toString() ?: "")
        }
        b.copyDecrypt.setOnClickListener {
            copy(b.decryptOutput.text?.toString() ?: "")
        }

        // Tab switching
        b.tabEncrypt.setOnClickListener { showTab(true) }
        b.tabDecrypt.setOnClickListener { showTab(false) }
    }

    private fun showTab(encrypt: Boolean) {
        if (encrypt) {
            b.encryptSection.visibility = View.VISIBLE
            b.decryptSection.visibility = View.GONE
            b.tabEncrypt.setBackgroundResource(R.drawable.bg_tab_encrypt)
            b.tabDecrypt.setBackgroundResource(R.drawable.bg_tab_off)
            b.tabEncryptText.setTextColor(0xFFFFFFFF.toInt())
            b.tabDecryptText.setTextColor(0xFF7878AA.toInt())
        } else {
            b.encryptSection.visibility = View.GONE
            b.decryptSection.visibility = View.VISIBLE
            b.tabEncrypt.setBackgroundResource(R.drawable.bg_tab_off)
            b.tabDecrypt.setBackgroundResource(R.drawable.bg_tab_decrypt)
            b.tabEncryptText.setTextColor(0xFF7878AA.toInt())
            b.tabDecryptText.setTextColor(0xFFFFFFFF.toInt())
        }
    }

    private fun copy(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
            return
        }
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("cipher", text))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
