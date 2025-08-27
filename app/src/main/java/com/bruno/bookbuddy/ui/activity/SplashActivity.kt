package com.bruno.bookbuddy.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bruno.bookbuddy.MainActivity
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.databinding.ActivitySplashBinding
import com.bruno.bookbuddy.utils.LocaleHelper

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashTimeOut: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
        navigateToMainActivity()
    }

    private fun startAnimations() {
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.ivSplashLogo.startAnimation(fadeInAnimation)

        val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.tvAppName.startAnimation(slideUpAnimation)

        val progressFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_delayed)
        binding.progressBar.startAnimation(progressFadeIn)
    }

    private fun navigateToMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

            fadeOutAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })

            binding.root.startAnimation(fadeOutAnimation)
        }, splashTimeOut)
    }

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getCurrentLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, language)
        super.attachBaseContext(context)
    }
}