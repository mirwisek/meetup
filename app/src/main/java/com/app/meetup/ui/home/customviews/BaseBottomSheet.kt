package com.app.meetup.ui.home.customviews

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import com.app.meetup.R
import com.app.meetup.utils.getColorCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheet : BottomSheetDialogFragment() {

    lateinit var bottomSheet: BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheet = BottomSheetDialog(requireContext(), theme)

//        bottomSheet.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

//        //setting Peek at the 16:9 ratio keyline of its parent
        bottomSheet.behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO, true)
//        bottomSheet.behavior.peekHeight = 300
        bottomSheet.setCancelable(false)
        bottomSheet.dismissWithAnimation = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bottomSheet.window?.statusBarColor = requireContext().getColorCompat(R.color.colorPrimaryDark)
        }

        return bottomSheet
    }

    override fun getTheme(): Int {
        // Return Custom Style that we made
        return R.style.BottomSheetPaymentTheme
    }

}