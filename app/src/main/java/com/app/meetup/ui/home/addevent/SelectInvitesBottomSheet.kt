package com.app.meetup.ui.home.addevent

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.meetup.Invite
import com.app.meetup.R
import com.app.meetup.ui.home.customviews.BaseBottomSheet
import com.app.meetup.ui.home.customviews.InviteSelectionAdapter
import com.app.meetup.utils.gone
import com.app.meetup.utils.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_invite_selection.view.*

class SelectInvitesBottomSheet : BaseBottomSheet() {

    private var listener: OnInviteSelectionListener? = null
    private lateinit var adapter: InviteSelectionAdapter
    private lateinit var viewModel: BottomSheetViewModel
    private var invites: List<Invite>? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_invite_selection, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(BottomSheetViewModel::class.java)

        view.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->

//            val deviceHeight = resources.displayMetrics.heightPixels
            bottomSheet.behavior.peekHeight = v.height
            bottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        adapter = InviteSelectionAdapter(requireContext())
        view.rvInvites.adapter = adapter

        if(adapter.itemCount == 0){
            view.placeHolder.visible()
        }

        viewModel.inviteList.observe(viewLifecycleOwner,  { list ->
            if(list.isEmpty()) {
                view.placeHolder.visible()
            } else {
                view.placeHolder.gone()
                invites = list.toMutableList()
                adapter.updateList(invites!!)
            }
        })

        adapter.setOnClickListener(object: InviteSelectionAdapter.OnItemClickListener {

            override fun onClicked(invite: Invite, index: Int) {
                invites?.let { list ->
                    list[index].isInvited = !list[index].isInvited
                    adapter.updateList(list)
                }
            }

        })

        view.btnDone.setOnClickListener {
            invites?.let { list ->
                val result = list.filter { it.isInvited }
                viewModel.selectedInvites.postValue(result)
                listener?.onComplete(result)
            }
            dismiss()
        }

        return view
    }

    fun setOnInviteSelectionListener(inviteSelectionListener: OnInviteSelectionListener) {
        listener = inviteSelectionListener
    }

    interface OnInviteSelectionListener {
        fun onComplete(list: List<Invite>)
    }

}