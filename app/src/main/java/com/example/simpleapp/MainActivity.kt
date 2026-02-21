package com.example.simpleapp

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.simpleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedInstanceState?.let {
            counter = it.getInt(KEY_COUNTER, 0)
        }
        
        updateCounterDisplay()
        setupClickListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_COUNTER, counter)
    }

    private fun setupClickListeners() {
        binding.buttonIncrement.setOnClickListener {
            counter++
            animateCounter()
            updateCounterDisplay()
        }

        binding.buttonDecrement.setOnClickListener {
            counter--
            animateCounter()
            updateCounterDisplay()
        }

        binding.buttonReset.setOnClickListener {
            counter = 0
            animateCounter()
            updateCounterDisplay()
        }
    }

    private fun updateCounterDisplay() {
        binding.textCounter.text = counter.toString()
        
        val color = when {
            counter > 0 -> getColor(R.color.positive_green)
            counter < 0 -> getColor(R.color.negative_red)
            else -> getColor(R.color.neutral_gray)
        }
        binding.textCounter.setTextColor(color)
    }

    private fun animateCounter() {
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_pulse)
        binding.textCounter.startAnimation(scaleAnimation)
    }

    companion object {
        private const val KEY_COUNTER = "counter_value"
    }
}
