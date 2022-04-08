package com.android.sensors.di.module

import android.content.Context
import com.android.sensors.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GlideRequestManagerModule {

    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions()
            .placeholder(R.drawable.ic_error_placeholder)
            .error(R.drawable.ic_error_no_image)
    }

    @Singleton
    @Provides
    fun provideGlideRequestManger(
        @ApplicationContext context: Context,
        requestOptions: RequestOptions
    ): RequestManager {
        return Glide.with(context).applyDefaultRequestOptions(requestOptions)
    }
}