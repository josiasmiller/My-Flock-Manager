package com.weyr_associates.animaltrakkerfarmmobile.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Display version information
        val pi = packageManager.getPackageInfo(packageName, 0)
        binding.version.text = "Version: ${pi.versionName}"

        val displayMetrics = DisplayMetrics().also {
            windowManager.defaultDisplay.getRealMetrics(it)
        }
        //Display screen metrics information
        val screenDensity = displayMetrics.density
        val screenWidthPx = displayMetrics.widthPixels
        val screenHeightPx = displayMetrics.heightPixels
        val screenWidthDp = screenWidthPx / screenDensity
        val screenHeightDp = screenHeightPx / screenDensity
        binding.textScreenDimensionsPx.text = "Screen Dimensions (px): $screenWidthPx x $screenHeightPx"
        binding.textScreenDimensionsDp.text = "Screen Dimensions (dp): $screenWidthDp x $screenHeightDp"
        binding.textScreenDensityRatio.text = "Screen Density Ratio: $screenDensity"

        val screenDensityDpi = resources.configuration.densityDpi
        binding.textScreenDensityDpi.text = "Screen Density DPI: $screenDensityDpi"

        val screenDensityClass = getString(R.string.screen_density_class)
        binding.textScreenDensityClass.text = "Screen Density Class: $screenDensityClass"

        val fontScalingFactor = resources.configuration.fontScale
        binding.textScaledPixelDensity.text = "Font Scale Factor: $fontScalingFactor"
    }
}
