package com.admin.kasironline.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.admin.kasironline.fragment.cashbank.PaymentListFragment
import com.admin.kasironline.fragment.cashbank.ReceptionListFragment

class PagerAdapterCash(fm: Fragment) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(
            ReceptionListFragment(),
            PaymentListFragment()
        )

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}