package com.imbaland.common.di

import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.domain.auth.Authenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CommonsModule {
    @Provides
    @Singleton
    internal fun providesFirebaseAuthenticator(
    ): FirebaseAuthenticator {
        return FirebaseAuthenticator()
    }
}