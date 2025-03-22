package com.pathfinder.khushu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    private var step = 0
    private var tutorialText = arrayOf(
        "Welcome to Khushu! This tutorial will guide you through the app. Register masjids where you pray and don't want to be disturbed by your phone.",
        "This app requires DnD, notification and location permissions in the background to work.",
        "You may need to restart the app to prompt 'allow all the time' location permissions.",
        "When you first load the app there will be no places registered. Head to the map and search for places to register.",
        "The map allows you to search from the center of the screen. Edit the search settings if needed, then hit search.",
        "Tap on the markers to register places, or tap on a spot on the map to register a custom location.",
        "All set! Now when you enter a registered place, your phone will automatically enter Do Not Disturb mode."
    )
    private var tutorialImages = arrayOf(
        R.drawable.khushu,
        R.drawable.tutorial_dnd,
        R.drawable.tutorial_location,
        R.drawable.tutorial_1,
        R.drawable.tutorial_2,
        R.drawable.tutorial_3,
        R.drawable.tutorial_4,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val prevButton = findViewById<Button>(R.id.btnPrevTutorial)
        val nextButton = findViewById<Button>(R.id.btnNextTutorial)
        val finishButton = findViewById<Button>(R.id.btnFinishTutorial)
        val tutorialImageView = findViewById<ImageView>(R.id.tutorialImageView)
        val tutorialTextView = findViewById<TextView>(R.id.tutorialTextView)

        finishButton.setOnClickListener {
            editor.putBoolean("hasSeenTutorial", true)
            editor.apply()

            // Go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close tutorial activity
        }

        nextButton.setOnClickListener {
            step += 1
            tutorialTextView.text = tutorialText[step]
            tutorialImageView.setImageResource(tutorialImages[step])
            prevButton.isEnabled = true

            if (step == tutorialText.size - 1) {
                finishButton.isEnabled = true
                nextButton.isEnabled = false
            }
        }

        prevButton.setOnClickListener {
            step -= 1
            tutorialImageView.setImageResource(tutorialImages[step])
            tutorialTextView.text = tutorialText[step]
            nextButton.isEnabled = true

            if (step == 0) {
                prevButton.isEnabled = false
            }
        }
    }
}
