package com.app.meetup.ui.home.eventlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.R
import com.app.meetup.ui.home.HomeViewModel
import com.app.meetup.ui.home.customviews.BaseBottomSheet
import com.app.meetup.utils.priceFormat
import com.app.meetup.utils.toastFrag
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_add_bill.view.*
import kotlinx.android.synthetic.main.bottom_sheet_add_title.view.*
import kotlinx.android.synthetic.main.bottom_sheet_add_title.view.textBillAmount
import kotlinx.android.synthetic.main.bottom_sheet_invite_selection.view.btnDone
import java.lang.Exception
import kotlin.math.roundToInt

class AddBillBottomSheet : BaseBottomSheet() {

    private var listener: OnFilledListener? = null
    private lateinit var vm: HomeViewModel

    private var total = 0.0F

    companion object {
        const val KEY_CHECK_IN_SIZE = "checkedIn"
        const val TAG = "BottomSheet"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_add_bill, container, false)


        vm = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        view.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->

//            val deviceHeight = resources.displayMetrics.heightPixels
            bottomSheet.behavior.peekHeight = v.height
            bottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }



        view.textBillAmount.addTextChangedListener { editor ->
            var str = "Amount per head: Rs. 0"
            val size = vm.checkedInSize.value ?: 0
            total = -1.0F
            if(!editor.isNullOrBlank()) {
                try {
                    total = editor.toString().toFloat()
                    if(total > 0) {
                        str = "Amount per head: Rs. ${(total / size).roundToInt()}"
                    }
                } catch (e: Exception) {
                    total = -1.0F
                }
            }
            view.perHead.text = str
        }


        view.btnDone.setOnClickListener {
            if(total <= 0)
                toastFrag("Please input valid number greater than Rs. 0")
            else {
                // Upto 2 decimal places

                listener?.onAdded(total.priceFormat)
                dismiss()
            }
        }

        return view
    }

    fun setOnFilledListener(titleAdded: OnFilledListener) {
        listener = titleAdded
    }

    interface OnFilledListener {
        fun onAdded(total: String)
    }

}