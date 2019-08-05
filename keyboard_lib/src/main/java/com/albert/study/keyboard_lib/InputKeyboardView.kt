package com.albert.study.keyboard_lib

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * 自定义键盘
 */
class InputKeyboardView : KeyboardView {

    private var xmlLayoutResId: Int = -1  //键盘xml布局

    private var specialKeyBackground: Drawable? = null   //特殊按钮背景颜色

    private var deleteKeyDrawable: Drawable? = null  //删除按钮icon

    private var specialKeyCodes: List<Int>? = null  //特殊按钮的keycode

    var onKeyCallback: OnKeyCallback? = null

    private var keyIconRect: Rect? = null

    private var deleteKeyColor: Int? = null

    private var specialKeyTextSize: Int? = null

    private lateinit var specialKeyTextPaint: Paint

    private lateinit var specialKeyPadding: Rect

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(attrs, defStyleAttr)
    }


    private fun init(attrs: AttributeSet?, defStyleRes: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.InputKeyboardView, defStyleRes, 0
        )

        if (a.hasValue(R.styleable.InputKeyboardView_xmlLayoutResId)) {
            xmlLayoutResId = a.getResourceId(R.styleable.InputKeyboardView_xmlLayoutResId, -1)
        }

        if (a.hasValue(R.styleable.InputKeyboardView_specialKeyBackground)) {
            specialKeyBackground = a.getDrawable(R.styleable.InputKeyboardView_specialKeyBackground)
        }

        if (a.hasValue(R.styleable.InputKeyboardView_specialKeyCodes)) {
            specialKeyCodes = a.getString(R.styleable.InputKeyboardView_specialKeyCodes)?.split(",")?.map { it.toInt() }
        }

        if (a.hasValue(R.styleable.InputKeyboardView_deleteKeyColor)) {
            deleteKeyColor = a.getColor(R.styleable.InputKeyboardView_deleteKeyColor, Color.parseColor("#747995"))
        }

        if (a.hasValue(R.styleable.InputKeyboardView_specialKeyTextSize)) {
            specialKeyTextSize = a.getDimensionPixelSize(R.styleable.InputKeyboardView_specialKeyTextSize, 18)
        }

        a.recycle()
        keyboard = Keyboard(context, xmlLayoutResId)
        isEnabled = true
        isFocusable = true
        isPreviewEnabled = false
        onKeyboardActionListener = object : OnKeyboardActionListener {
            override fun swipeRight() {

            }

            override fun onPress(primaryCode: Int) {

            }

            override fun onRelease(primaryCode: Int) {

            }

            override fun swipeLeft() {

            }

            override fun swipeUp() {

            }

            override fun swipeDown() {

            }

            override fun onText(text: CharSequence?) {

            }

            override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
                onKeyCallback?.let {
                    when (primaryCode) {
                        Keyboard.KEYCODE_DELETE -> it.onDelete()
                        Keyboard.KEYCODE_DONE -> it.onNext()
                        else -> it.onInput(primaryCode.toChar().toString())
                    }
                }
            }
        }

        specialKeyTextPaint = Paint()
        specialKeyTextPaint.isAntiAlias = true
        specialKeyTextSize?.let {
            specialKeyTextPaint.textSize = it.toFloat()
        }
        deleteKeyColor?.let {
            specialKeyTextPaint.color = it
        }
        specialKeyTextPaint.textAlign = Paint.Align.CENTER
        specialKeyTextPaint.alpha = 255

        specialKeyPadding = Rect(0, 0, 0, 0)
        specialKeyBackground?.getPadding(specialKeyPadding)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            keyboard.keys.forEach { key ->
                if (specialKeyCodes?.contains(element = key.codes[0]) == true) {
                    drawSpecialKeyBackground(key, this)

                    key.label?.let {
                        if (key.codes[0] != Keyboard.KEYCODE_DONE) {
                            canvas.drawText(
                                it.toString(),
                                (key.x + (key.width - specialKeyPadding.left - specialKeyPadding.right) / 2 + specialKeyPadding.left).toFloat(),
                                (key.y + (key.height - specialKeyPadding.top - specialKeyPadding.bottom) / 2).toFloat()
                                        + (specialKeyTextPaint.textSize - specialKeyTextPaint.descent()) / 2 + specialKeyPadding.top.toFloat(),
                                specialKeyTextPaint
                            )
                        } else {
                            val tempTextSize = specialKeyTextPaint.textSize
                            specialKeyTextPaint.textSize = tempTextSize * 0.7f
                            canvas.drawText(
                                it.toString(),
                                (key.x + (key.width - specialKeyPadding.left - specialKeyPadding.right) / 2 + specialKeyPadding.left).toFloat(),
                                (key.y + (key.height - specialKeyPadding.top - specialKeyPadding.bottom) / 2).toFloat()
                                        + (specialKeyTextPaint.textSize - specialKeyTextPaint.descent()) / 2 + specialKeyPadding.top.toFloat(),
                                specialKeyTextPaint
                            )
                            specialKeyTextPaint.textSize = tempTextSize
                        }
                    }
                }

                if (key.codes[0] == Keyboard.KEYCODE_DELETE) {
                    if (deleteKeyDrawable == null) {
                        deleteKeyDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_key)
                        deleteKeyColor?.let {
                            deleteKeyDrawable?.setTint(it)
                        }
                    }
                    deleteKeyDrawable?.let {
                        drawKeyIcon(key, canvas, deleteKeyDrawable)
                    }
                }
            }
        }
    }

    private fun drawSpecialKeyBackground(key: Keyboard.Key, canvas: Canvas) {
        specialKeyBackground?.let {
            val drawable = it as StateListDrawable
            drawable.state = key.currentDrawableState
            drawable.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
            drawable.draw(canvas)
        }
    }

    private fun drawKeyIcon(key: Keyboard.Key, canvas: Canvas, iconDrawable: Drawable?) {
        iconDrawable?.let { drawable ->
            if (keyIconRect == null || keyIconRect!!.isEmpty) {
                val intrinsicWidth = drawable.intrinsicWidth
                val intrinsicHeight = drawable.intrinsicHeight

                var drawWidth = intrinsicWidth
                var drawHeight = intrinsicHeight

                if (drawWidth > key.width) {
                    drawWidth = key.width
                    drawHeight = (drawWidth * 1.0f / intrinsicWidth * intrinsicHeight).toInt()
                } else if (drawHeight > key.height) {
                    drawHeight = key.height
                    drawWidth = (drawHeight * 1.0f / intrinsicHeight * intrinsicWidth).toInt()
                }

                val left = key.x + key.width / 2 - drawWidth / 2
                val top = key.y + key.height / 2 - drawHeight / 2
                this.keyIconRect = Rect(left, top, left + drawWidth, top + drawHeight)
            }

            keyIconRect?.let { rect ->
                if (!rect.isEmpty) {
                    drawable.bounds = rect
                    drawable.draw(canvas)
                }
            }
        }
    }

    interface OnKeyCallback {
        fun onInput(text: String)

        fun onDelete()

        fun onNext()
    }
}

/**
 * 绑定自定义键盘
 * @param layoutResId 自定义键盘布局
 */
fun EditText.bindInputPopupWindow(activity: Activity, @LayoutRes layoutResId: Int) {
    val popupWindow: PopupWindow by lazy {
        val contentView = LayoutInflater.from(activity).inflate(layoutResId, null)
        val keyboardView: InputKeyboardView = contentView.findViewById(R.id.input_keyboard_view)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val temp = PopupWindow(contentView).apply {
            isOutsideTouchable = true
            elevation = activity.resources.getDimension(R.dimen.popup_elevation)
            height = contentView.measuredHeight
            width = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        Timber.d("width=${contentView.measuredWidth},height=${contentView.measuredHeight}")

        keyboardView.onKeyCallback = object : InputKeyboardView.OnKeyCallback {
            override fun onInput(text: String) {
                Timber.d("onInput: text=$text")
                if (isCursorVisible) {
                    val index = selectionStart
                    editableText.insert(index, text)
                } else {
                    append(text)
                }
                Timber.d("onInput: content = $text")
            }

            override fun onDelete() {
                Timber.d("onDelete")
                if (text.isNotEmpty()) {
                    if (selectionStart == selectionEnd && selectionStart > 0) {
                        editableText.delete(selectionStart - 1, selectionStart)
                    } else {
                        editableText.delete(selectionStart, selectionEnd)
                    }
                }
            }

            override fun onNext() {
                onEditorAction(imeActionId)
                temp.dismiss()
            }
        }
        temp
    }

    setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            v.post {
                hideSystemKeyboard(activity, v as EditText)
                popupWindow.showAsDropDown(v)
            }
        }
        false
    }

    onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (!hasFocus) {
            popupWindow.dismiss()
        } else {
            v.post {
                hideSystemKeyboard(activity, v as EditText)
                popupWindow.showAsDropDown(v)
            }
        }
    }
}

fun hideSystemKeyboard(activity: Activity, v: EditText) {
    (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
        if (it.isActive) {
            it.hideSoftInputFromWindow(v.windowToken, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                v.showSoftInputOnFocus = false
            } else {
                v.inputType = 0
            }
        }
    }
}

