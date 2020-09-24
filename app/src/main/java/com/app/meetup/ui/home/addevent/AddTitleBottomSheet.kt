package com.app.meetup.ui.home.addevent

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.R
import com.app.meetup.ui.home.HomeViewModel
import com.app.meetup.ui.home.customviews.BaseBottomSheet
import com.app.meetup.utils.toastFrag
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_add_title.*
import kotlinx.android.synthetic.main.bottom_sheet_add_title.view.*
import kotlinx.android.synthetic.main.bottom_sheet_invite_selection.view.btnDone

class AddTitleBottomSheet : BaseBottomSheet() {

    private var listener: OnTitleAddedListener? = null
    private var detachListener: OnDetachedComplete? = null
    private lateinit var vmHome: HomeViewModel

    companion object {
        const val KEY_TITLE = "titleString"
        const val TAG = "BottomSheet"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_add_title, container, false)

//        arguments?.getString(KEY_TITLE)?.let { title ->
//
//        }

        vmHome = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        vmHome.currentEventTitle.observe(viewLifecycleOwner) {
            it?.let {  title ->
                view.textTitle.setText(title)
                view.textTitle.selectAll()
            }
        }

        view.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->

//            val deviceHeight = resources.displayMetrics.heightPixels
            bottomSheet.behavior.peekHeight = v.height
            bottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


        view.btnDone.setOnClickListener {
            val title = view.textTitle.text.toString()
            if(title.isEmpty())
                toastFrag("Title can't be empty")
            else {
                vmHome.currentEventTitle.postValue(title)
                listener?.onAdded(view.textTitle.text.toString())
                dismiss()
            }

        }

        return view
    }

    fun setOnTitleAddedListener(titleAdded: OnTitleAddedListener) {
        listener = titleAdded
    }

    fun setOnDetachedListener(onDetachedComplete: OnDetachedComplete) {
        detachListener = onDetachedComplete
    }

    override fun onDetach() {
        super.onDetach()
        detachListener?.onDetached()
    }

    interface OnTitleAddedListener {
        fun onAdded(title: String)
    }

    interface OnDetachedComplete {
        fun onDetached()
    }

}