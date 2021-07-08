package com.admin.kasironline.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.admin.kasironline.GlideApp
import com.admin.kasironline.R
import com.admin.kasironline.adapter.PagerAdapterHome
import com.admin.kasironline.model.AccountModel
import com.admin.kasironline.model.ProductModel
import com.admin.kasironline.utils.Constant
import com.admin.kasironline.utils.Validation.Companion.validateFields
import com.firebase.ui.auth.AuthUI
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.add_account_dialog.view.*
import kotlinx.android.synthetic.main.add_account_dialog.view.btn_add
import kotlinx.android.synthetic.main.add_product_dialog.view.*
import kotlinx.android.synthetic.main.fragment_tab_layout.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment() {

    private var mContext: Context? = null
    private var alertDialog: AlertDialog? = null
    private lateinit var dialogView: View
    private var mProductCount = 0
    private var resultUri: Uri? = null
    private val imageReference = FirebaseStorage.getInstance().reference

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
        (mContext as AppCompatActivity).setSupportActionBar(toolbar)
        (mContext as AppCompatActivity).supportActionBar?.title =
            view.context.getString(R.string.app_name)

        val tabMenus = arrayOf(
            getString(R.string.tab_home_text_1),
            getString(R.string.tab_home_text_2),
            getString(R.string.tab_home_text_3),
            getString(R.string.tab_home_text_4)
        )

        val pageAdapter = PagerAdapterHome(this)

        viewPager.adapter = pageAdapter

        TabLayoutMediator(
            tabs,
            viewPager
        ) { tab, position ->
            tab.text = tabMenus[position]
        }.attach()

        tabs.tabGravity = TabLayout.GRAVITY_FILL
        tabs.tabMode = TabLayout.MODE_SCROLLABLE

        fab.setOnClickListener {
            addAccountDialog()
        }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        fab.show()
                        fab.setOnClickListener {
                            addAccountDialog()
                        }
                    }
                    1 -> fab.hide()
                    2 -> fab.hide()
                    3 -> {
                        fab.show()
                        fab.setOnClickListener {
                            addProductDialog()
                        }
                    }
                }
            }

        })
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
                            progressBar?.visibility = VISIBLE

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
                val dialog = builder?.create()
                dialog?.show()
            }
        }
        return true
    }

    @SuppressLint("InflateParams")
    private fun addAccountDialog() {
        val builder = context.let { it?.let { it1 -> MaterialAlertDialogBuilder(it1) } }
        dialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_account_dialog, null
        )

        bindProgressButton(dialogView.btn_add)
        dialogView.btn_add.attachTextChangeAnimator()

        dialogView.btn_add.setOnClickListener {
            val username = dialogView.input_username?.text.toString()
            val password = dialogView.input_password?.text.toString()

            if (validateFields(username) || validateFields(password)) {
                Toast.makeText(context, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                dialogView.btn_add.showProgress { progressColor = Color.WHITE }
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(username)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            Toast.makeText(
                                context,
                                "Username sudah terdaftar",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            saveAccount(username, password)
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        builder?.setView(dialogView)
        builder?.setTitle("Tambah Kasir")

        alertDialog = builder?.create()
        alertDialog?.show()
    }

    private fun saveAccount(username: String, password: String) {
        val data = AccountModel()
        data.username = username
        data.password = password

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(username)
            .set(data)
            .addOnSuccessListener {
                dialogView.btn_add.hideProgress(R.string.btn_add)
                Toast.makeText(
                    context,
                    "Kasir berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                ).show()
                alertDialog?.dismiss()
            }.addOnFailureListener {
                dialogView.btn_add.hideProgress(R.string.btn_add)
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    @SuppressLint("InflateParams")
    private fun addProductDialog() {
        val builder = context.let { it?.let { it1 -> MaterialAlertDialogBuilder(it1) } }
        dialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_product_dialog, null
        )

        bindProgressButton(dialogView.btn_add)
        dialogView.btn_add.attachTextChangeAnimator()

        dialogView.input_price.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    dialogView.input_price.removeTextChangedListener(this)
                    val local = Locale("in", "ID")

                    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    val replaceable = java.lang.String.format(
                        "[Rp,.\\s]",
                        NumberFormat.getCurrencyInstance().currency
                            .getSymbol(local)
                    )
                    val cleanString = s.toString().replace(
                        replaceable.toRegex(),
                        ""
                    )
                    val parsed: Double = try {
                        cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.00
                    }
                    val formatter: NumberFormat = NumberFormat
                        .getCurrencyInstance(local)
                    formatter.maximumFractionDigits = 0
                    formatter.isParseIntegerOnly = true
                    val formatted: String = formatter.format(parsed)

                    val replace = java.lang.String.format(
                        "[Rp\\s]",
                        NumberFormat.getCurrencyInstance(local)
                    )
                    val clean = formatted.replace(replace.toRegex(), "")
                    current = formatted
                    dialogView.input_price.setText(clean)
                    dialogView.input_price.setSelection(clean.length)
                    dialogView.input_price.addTextChangedListener(this)
                }
            }
        })

        dialogView.btn_add.setOnClickListener {
            val name = dialogView.input_product.text.toString()
            val price = dialogView.input_price.text.toString()
            val discount = dialogView.input_discount.text.toString()
            val stock = dialogView.tv_stock_value.text.toString()

            if (validateFields(name) || validateFields(price) || validateFields(discount)
                || validateFields(stock) || validateFields(resultUri?.path.toString())
            ) {
                Toast.makeText(context, "Tidak boleh ada data yang kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (discount.toInt() > 100) {
                    Toast.makeText(
                        context,
                        "Diskon tidak boleh lebih dari 100%",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    val priceValue = dialogView.input_price.text.toString().replace(".", "").toInt()

                    dialogView.btn_add.showProgress { progressColor = Color.WHITE }

                    val thumbImage = File(resultUri?.path.toString())

                    val thumbBitmap = Compressor(context)
                        .setMaxHeight(200)
                        .setMaxWidth(700)
                        .setQuality(100)
                        .compressToBitmap(thumbImage)

                    val bios = ByteArrayOutputStream()
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bios)
                    val imageByte = bios.toByteArray()

                    val thumbURL = "product/${UUID.randomUUID()}.jpg"
                    val thumbPath = imageReference.child(thumbURL)

                    resultUri?.let { it1 ->
                        thumbPath.putFile(it1).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                imageReference.child(thumbURL).downloadUrl.addOnSuccessListener { imageUri: Uri? ->

                                    val uploadTask: UploadTask = thumbPath.putBytes(imageByte)

                                    uploadTask.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val data = ProductModel()
                                            data.name = name
                                            data.price = price
                                            data.priceValue = priceValue
                                            data.discount = discount.toInt()
                                            data.stock = stock.toInt()
                                            data.image = imageUri.toString()

                                            val db = FirebaseFirestore.getInstance()
                                            db.collection("product")
                                                .document()
                                                .set(data)
                                                .addOnSuccessListener {
                                                    dialogView.btn_add.hideProgress(R.string.btn_add)
                                                    Toast.makeText(
                                                        context,
                                                        "Produk berhasil ditambahkan",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                    alertDialog?.dismiss()
                                                    resultUri = null
                                                }.addOnFailureListener {
                                                    dialogView.btn_add.hideProgress(R.string.btn_add)
                                                    Toast.makeText(
                                                        context,
                                                        it.localizedMessage,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            dialogView.btn_add.hideProgress(R.string.btn_add)
                                            Toast.makeText(
                                                context,
                                                task.exception?.localizedMessage.toString(),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                }.addOnFailureListener {
                                    dialogView.btn_add.hideProgress(R.string.btn_add)
                                    Toast.makeText(
                                        context,
                                        it.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } else {
                                dialogView.btn_add.hideProgress(R.string.btn_add)
                                Toast.makeText(
                                    context,
                                    task.exception?.localizedMessage.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        dialogView.btn_plus.setOnClickListener {
            mProductCount += 1
            dialogView.tv_stock_value.text = mProductCount.toString()
            checkCount(dialogView)
        }

        dialogView.btn_min.setOnClickListener {
            mProductCount -= 1
            dialogView.tv_stock_value.text = mProductCount.toString()
            checkCount(dialogView)
        }

        dialogView.fab_add_photo.setOnClickListener {
            getPhotoFromStorage()
        }

        builder?.setView(dialogView)
        builder?.setTitle("Tambah Barang")

        alertDialog = builder?.create()
        alertDialog?.show()
    }

    private fun getPhotoFromStorage() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ContextCompat.checkSelfPermission(
                    it, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {

            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    Constant.PERMISSION_STORAGE
                )
            }

        } else {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                Constant.GALLERY_PICK
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.PERMISSION_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent()
                    galleryIntent.type = "image/*"
                    galleryIntent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                        Constant.GALLERY_PICK
                    )
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            context?.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it, this@HomeFragment)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            resultUri = CropImage.getActivityResult(data)?.uri
            context?.let {
                GlideApp.with(it)
                    .load(resultUri)
                    .into(dialogView.img_product)
            }
        }
    }

    private fun checkCount(dialogView: View) {
        if (mProductCount > 0) {
            dialogView.btn_min.isEnabled = true
            dialogView.btn_add.isEnabled = true
        } else {
            dialogView.btn_min.isEnabled = false
            dialogView.btn_add.isEnabled = false
        }
    }
}