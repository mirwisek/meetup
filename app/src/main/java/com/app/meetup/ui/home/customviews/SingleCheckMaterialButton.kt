package com.app.meetup.ui.home.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton


class SingleCheckMaterialButton : MaterialButton {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    override fun toggle() {
        if (!isChecked) {
            super.toggle()
        }
    }

}