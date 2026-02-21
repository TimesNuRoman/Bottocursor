package com.example.simpleapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simpleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.buttonClick.setOnClickListener {
            clickCount++
            binding.textCounter.text = getString(R.string.click_count, clickCount)
            
            if (clickCount % 10 == 0) {
                Toast.makeText(
                    this,
                    getString(R.string.milestone_message, clickCount),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.buttonReset.setOnClickListener {
            clickCount = 0
            binding.textCounter.text = getString(R.string.click_count, clickCount)
            Toast.makeText(this, R.string.counter_reset, Toast.LENGTH_SHORT).show()
        }
    }
}
