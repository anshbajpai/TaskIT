package com.example.taskit.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.example.taskit.MainActivity
import com.example.taskit.R
import com.example.taskit.data.viewmodel.ToDoViewModel
import kotlinx.android.synthetic.main.onboarding_view.*

class OnBoardingActivity : AppCompatActivity() {

    private val mToDoViewModel: ToDoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_activity)

        skipBtn.setOnClickListener {
            mToDoViewModel.saveBolDataStore(false)
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        }

        startBtn.setOnClickListener {
            mToDoViewModel.saveBolDataStore(false)
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        }
    }
}