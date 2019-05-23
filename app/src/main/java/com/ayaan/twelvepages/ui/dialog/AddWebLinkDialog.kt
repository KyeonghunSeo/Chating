package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.bumptech.glide.Glide
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.setGlobalTheme
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import kotlinx.android.synthetic.main.dialog_edit_web_link.*


class AddWebLinkDialog(activity: Activity, private val onResult: (Link) -> Unit) : Dialog(activity) {
    private var webData: MetaData? = null

    private val richPreview = RichPreview(object : ResponseListener {
        override fun onData(metaData: MetaData) {
            webData = metaData
            linkLy.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            if(!metaData.imageurl.isNullOrBlank())
                Glide.with(context).load(metaData.imageurl).into(linkImg)
            else if(!metaData.favicon.isNullOrBlank())
                Glide.with(context).load(metaData.favicon).into(linkImg)
            else {
                linkImg.setColorFilter(AppTheme.iconColor)
                Glide.with(context).load(R.drawable.website).into(linkImg)
            }
            linkText.text = metaData.title
        }

        override fun onError(e: Exception) {
            e.printStackTrace()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_edit_web_link)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                linkLy.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                val url = input.text.toString()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        confirmBtn.setOnClickListener {

            dismiss()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }
    }

}
