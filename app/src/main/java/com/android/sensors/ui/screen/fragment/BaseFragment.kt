package com.android.sensors.ui.screen.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.sensors.ui.viewmodel.SensorsViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    lateinit var viewModel: SensorsViewModel
    @Inject lateinit var glideRequestManager: RequestManager
    val TAG = "AppDebug"
    lateinit var getActivity: FragmentActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = activity?.let {
            getActivity = it
            ViewModelProvider(it)
        }?.get(SensorsViewModel::class.java) ?: throw Exception("Invalid Activity")

    }

    fun loadImage(url: String, imageView: ImageView) {
        glideRequestManager.load(url).into(imageView)
    }

    fun loadImage(uri: Uri, imageView: ImageView) {
        glideRequestManager.load(uri).into(imageView)
    }

    fun isInternetAvailable(context: Context): Boolean {
        val mConMgr =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return (mConMgr.activeNetworkInfo != null && mConMgr.activeNetworkInfo!!.isAvailable
                && mConMgr.activeNetworkInfo!!.isConnected)
    }

    fun showProgressBar(progressBar: ProgressBar) {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.startLoading()
            viewModel.shouldLoadSensors.observe(viewLifecycleOwner, Observer { loadingState ->
                if (loadingState)
                    progressBar.visibility = View.VISIBLE
                else
                    progressBar.visibility = View.GONE
            })
        }
    }

}