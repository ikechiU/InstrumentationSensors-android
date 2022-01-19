package com.android.sensors.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.android.sensors.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun loadImage(context: Context, url: String, imageView: ImageView) {
    val options: RequestOptions = RequestOptions()
//    .centerCrop()
        .placeholder(R.drawable.ic_error_placeholder)
        .error(R.drawable.ic_error_no_image)

    Glide.with(context)
        .load(url.trim())
        .apply(options)
        .into(imageView)
}

fun isInternetAvailable(context: Context): Boolean {
    val mConMgr =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return (mConMgr.activeNetworkInfo != null && mConMgr.activeNetworkInfo!!.isAvailable
            && mConMgr.activeNetworkInfo!!.isConnected)
}

fun visitUrl(context: Context, url: String) {
    val link  = if (!url.startsWith("http://") && !url.startsWith("https://"))
        "http://$url"
    else
        url

    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(link)
    val chooser = Intent.createChooser(i, "Visit page")
    context.startActivity(chooser)
}