package com.admin.kasironline.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.admin.kasironline.fragment.home.AccountListFragment
import com.admin.kasironline.fragment.home.CustomerListFragment
import com.admin.kasironline.fragment.home.InventoryFragment
import com.admin.kasironline.fragment.home.SupplierListFragment

class PagerAdapterHome(fm: Fragment) :
    FragmentStateAdapter(fm) {

    private val pages =
        listOf(
            AccountListFragment(),
            CustomerListFragment(),
            SupplierListFragment(),
            InventoryFragment()
        )

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}