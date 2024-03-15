package ru.hse.multitracker.ui.elements

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.hse.multitracker.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}