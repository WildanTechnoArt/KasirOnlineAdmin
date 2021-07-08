package com.admin.kasironline.fragment.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.admin.kasironline.GlideApp
import com.admin.kasironline.R
import com.admin.kasironline.adapter.ProductListAdapter
import com.admin.kasironline.model.ProductModel
import com.admin.kasironline.utils.Constant
import com.admin.kasironline.utils.Validation
import com.admin.kasironline.view.ProductListView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.add_product_dialog.view.*
import kotlinx.android.synthetic.main.fragment_tab_item.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

class InventoryFragment : Fragment(), ProductListView {

    private var alertDialog: AlertDialog? = null
    private lateinit var dialogView: View
    private var mProductCount = 0
    private var resultUri: Uri? = null
    private val imageReference = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepare()
        checkClass()
    }

    private fun prepare() {
        tv_not_data.text = getString(R.string.tv_not_product_list)

        rv_data_list?.layoutManager = GridLayoutManager(context, 2)
        rv_data_list?.setHasFixedSize(true)

        swipe_refresh?.setOnRefreshListener {
            checkClass()
        }
    }

    private fun requestData() {
        val query = FirebaseFirestore.getInstance()
            .collection("product")

        val options = FirestoreRecyclerOptions.Builder<ProductModel>()
            .setQuery(query, ProductModel::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = context?.let { ProductListAdapter(options, this) }
        rv_data_list?.adapter = adapter
    }

    private fun checkClass() {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.isEmpty == true) {
                    tv_not_data?.visibility = View.VISIBLE
                    rv_data_list?.visibility = View.GONE
                } else {
                    tv_not_data?.visibility = View.GONE
                    rv_data_list?.visibility = View.VISIBLE
                    requestData()
                }

                swipe_refresh?.isRefreshing = false
            }
    }

    override fun onDelete(id: String) {
        swipe_refresh?.isRefreshing = true

        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .document(id)
            .delete()
            .addOnSuccessListener {
                swipe_refresh?.isRefreshing = false
            }.addOnFailureListener {
                swipe_refresh?.isRefreshing = false
                Toast.makeText(context, it.localizedMessage?.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("InflateParams")
    override fun onEditProductDialog(id: String, data: ProductModel) {
        val builder = context?.let { MaterialAlertDialogBuilder(it) }
        dialogView = (context as AppCompatActivity).layoutInflater.inflate(
            R.layout.add_product_dialog, null
        )

        (context as AppCompatActivity).bindProgressButton(dialogView.btn_add)
        dialogView.btn_add.attachTextChangeAnimator()
        dialogView.btn_add.text = context?.getString(R.string.btn_edit)

        dialogView.input_discount.setText(data.discount.toString())
        dialogView.input_product.setText(data.name)
        dialogView.tv_stock_value.text = data.stock.toString()

        mProductCount = data.stock ?: 0
        checkCount(dialogView)

        GlideApp.with(context as AppCompatActivity)
            .load(data.image)
            .into(dialogView.img_product)

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

            if (Validation.validateFields(name) || Validation.validateFields(price) || Validation.validateFields(
                    discount
                )
                || Validation.validateFields(stock) || Validation.validateFields(
                    resultUri.toString()
                )
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

                    if (resultUri != null) {
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
                                                val product = ProductModel()
                                                product.name = name
                                                product.price = price
                                                product.priceValue = priceValue
                                                product.discount = discount.toInt()
                                                product.stock = stock.toInt()
                                                product.image = imageUri.toString()

                                                val db = FirebaseFirestore.getInstance()
                                                db.collection("product")
                                                    .document(id)
                                                    .set(product)
                                                    .addOnSuccessListener {
                                                        dialogView.btn_add.hideProgress(R.string.btn_edit)
                                                        Toast.makeText(
                                                            context,
                                                            "Produk berhasil diedit",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                        alertDialog?.dismiss()
                                                    }.addOnFailureListener {
                                                        dialogView.btn_add.hideProgress(R.string.btn_edit)
                                                        Toast.makeText(
                                                            context,
                                                            it.localizedMessage,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            } else {
                                                dialogView.btn_add.hideProgress(R.string.btn_edit)
                                                Toast.makeText(
                                                    context,
                                                    task.exception?.localizedMessage.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    }.addOnFailureListener {
                                        dialogView.btn_add.hideProgress(R.string.btn_edit)
                                        Toast.makeText(
                                            context,
                                            it.localizedMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                } else {
                                    dialogView.btn_add.hideProgress(R.string.btn_edit)
                                    Toast.makeText(
                                        context,
                                        task.exception?.localizedMessage.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        val product = HashMap<String, Any>()
                        product["name"] = name
                        product["price"] = price
                        product["priceValue"] = priceValue
                        product["discount"] = discount.toInt()
                        product["stock"] = stock.toInt()

                        val db = FirebaseFirestore.getInstance()
                        db.collection("product")
                            .document(id)
                            .update(product)
                            .addOnSuccessListener {
                                dialogView.btn_add.hideProgress(R.string.btn_edit)
                                Toast.makeText(
                                    context,
                                    "Produk berhasil diedit",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                alertDialog?.dismiss()
                            }.addOnFailureListener {
                                dialogView.btn_add.hideProgress(R.string.btn_edit)
                                Toast.makeText(
                                    context,
                                    it.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
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
        builder?.setTitle("Edit Barang")

        alertDialog = builder?.create()
        alertDialog?.show()
    }

    private fun getPhotoFromStorage() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val galleryIntent = Intent()
                    galleryIntent.type = "image/*"
                    galleryIntent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(galleryIntent, "SELECT IMAGE"),
                        Constant.GALLERY_PICK
                    )
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    dialogView.btn_add.hideProgress(R.string.btn_edit)
                    alertDialog?.dismiss()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            context?.let {
                CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(200, 200)
                    .start(it, this@InventoryFragment)
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