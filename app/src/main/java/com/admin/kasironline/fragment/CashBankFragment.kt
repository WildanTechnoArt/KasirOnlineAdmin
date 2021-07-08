package com.admin.kasironline.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.admin.kasironline.R
import com.admin.kasironline.adapter.PagerAdapterCash
import com.firebase.ui.auth.AuthUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_layout.*

class CashBankFragment : Fragment() {

    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        mContext = view.context

        val toolbar = view.findViewById(R.id.toolbar) as MaterialToolbar
        val viewPager = view.findViewById(R.id.view_pager) as ViewPager2
        val tabs = view.findViewById(R.id.tabs) as TabLayout
        (view.context as AppCompatActivity).setSupportActionBar(toolbar)
        (view.context as AppCompatActivity).supportActionBar?.title =
            view.context.getString(R.string.app_name)

        val tabMenus = arrayOf(
            getString(R.string.tab_cash_text_1),
            getString(R.string.tab_cash_text_2)
        )

        val pageAdapter = PagerAdapterCash(this)

        viewPager.adapter = pageAdapter

        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.tabMode = TabLayout.MODE_FIXED

        TabLayoutMediator(
            tabs,
            viewPager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu -> {
                val builder = context?.let {
                    MaterialAlertDialogBuilder(it)
                        .setTitle("Konfirmasi")
                        .setMessage("Anda yakin ingin keluar?")
                        .setPositiveButton("Ya") { _, _ ->
                            progressBar?.visibility = View.VISIBLE

                            mContext?.let { it1 ->
                                AuthUI.getInstance()
                                    .signOut(it1)
                                    .addOnSuccessListener {
                                        progressBar?.visibility = GONE

                                        Toast.makeText(
                                            mContext,
                                            getString(R.string.request_logout),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        (mContext as AppCompatActivity).finish()
                                    }.addOnFailureListener {
                                        progressBar?.visibility = GONE

                                        Toast.makeText(
                                            mContext,
                                            getString(R.string.request_error),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .setNegativeButton("Tidak") { dialog, _ ->
                            dialog.dismiss()
                        }
                }
                builder?.setCancelable(false)
                val dialog = builder?.create()
                dialog?.show()
            }
        }
        return true
    }
}