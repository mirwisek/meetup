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

    private var mListener: OnToggleListener? = null

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    override fun toggle() {
        // Do nothing
        return
//        if (!isChecked) {
//            super.toggle()
//        }
//        if(mListener == null)
//            super.toggle()
//        else {
//            if(!mListener!!.onToggle()) {
//                super.toggle()
//            }
//        }

    }

    fun addOnToggleListener(listener: OnToggleListener ) {
        mListener = listener
    }

    interface OnToggleListener {
        // If true then do nothing otherwise call super
        fun onToggle(): Boolean
    }

//    override fun onCheckedChanged(button: MaterialButton?, isChecked: Boolean) {
//        mListener?.onCheckedChanged(button, isChecked)
//    }

}