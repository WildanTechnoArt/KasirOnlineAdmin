package com.admin.kasironline.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.admin.kasironline.fragment.transaction.PurchaseListFragment
import com.admin.kasironline.fragment.transaction.SalesListFragment

class PagerAdapterTransaction(fm: Fragment) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(
            SalesListFragment(),
            PurchaseListFragment()
        )

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}