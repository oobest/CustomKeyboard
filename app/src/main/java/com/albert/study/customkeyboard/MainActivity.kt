package com.albert.study.customkeyboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.albert.study.keyboard_lib.bindInputPopupWindow
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText.bindInputPopupWindow(
            this,
            R.layout.number_keyboard_style02
        )

        editText2.bindInputPopupWindow(
            this,
            R.layout.number_keyboard_style02
        )
    }
}
