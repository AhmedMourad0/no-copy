package dev.ahmedmourad.nocopy.sample_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.ahmedmourad.nocopy.annotations.LeastVisibleCopy

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("lololololololol", Some.of("lololol").copy().toString())
    }
}

@LeastVisibleCopy
data class Some private constructor(val v: String) {
    companion object {
        fun of(v: String) = Some(v)
    }
}

