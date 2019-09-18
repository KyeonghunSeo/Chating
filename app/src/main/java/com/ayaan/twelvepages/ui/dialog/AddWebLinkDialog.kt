package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.bumptech.glide.Glide
import io.github.ponnamkarthik.richlinkpreview.MetaData
import io.github.ponnamkarthik.richlinkpreview.ResponseListener
import io.github.ponnamkarthik.richlinkpreview.RichPreview
import io.realm.Realm
import kotlinx.android.synthetic.main.container_web_link_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import org.json.JSONObject
import java.util.*


class AddWebLinkDialog(activity: Activity, private val onResult: (Link) -> Unit) : BaseDialog(activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_web_link_dlg, dpToPx(325))
        setLayout()
        setOnShowListener { showKeyPad(input) }
    }

    private fun setLayout() {
        titleText.text = str(R.string.weblink)
        titleIcon.setImageResource(R.drawable.website)
        confirmBtn.visibility = View.GONE
        optionBtn.visibility = View.VISIBLE
        optionBtn.text = str(R.string.search)
        optionBtn.setOnClickListener {
            startSearch()
        }
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_SEARCH) {
                startSearch()
            }
            return@setOnEditorActionListener false
        }
        cancelBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun startSearch() {
        var url = input.text.toString()
        if(!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
            input.setText(url)
        }

        val richPreview = RichPreview(object : ResponseListener {
            override fun onData(metaData: MetaData) {
                progressBar.visibility = View.GONE
                if(metaData.title.isNullOrEmpty()) {
                    toast(R.string.not_found_web)
                }else {
                    linkLy.visibility = View.VISIBLE
                    if(!metaData.imageurl.isNullOrBlank()){
                        linkImg.clearColorFilter()
                        Glide.with(context).load(metaData.imageurl).into(linkImg)
                    }else if(!metaData.favicon.isNullOrBlank()){
                        linkImg.clearColorFilter()
                        Glide.with(context).load(metaData.favicon).into(linkImg)
                    }else {
                        linkImg.setColorFilter(AppTheme.icon)
                        Glide.with(context).load(R.drawable.website).into(linkImg)
                    }
                    linkText.text = metaData.title
                    confirmBtn.visibility = View.VISIBLE
                    confirmBtn.setOnClickListener {
                        val link = Link(UUID.randomUUID().toString(), Link.Type.WEB.ordinal,
                                metaData.title, url, metaData.imageurl, metaData.favicon)
                        onResult.invoke(link)
                        dismiss()
                    }
                }
            }
            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })
        progressBar.visibility = View.VISIBLE
        confirmBtn.visibility = View.GONE
        linkLy.visibility = View.GONE
        try{
            richPreview.getPreview(url)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
