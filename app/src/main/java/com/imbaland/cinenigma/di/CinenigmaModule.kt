package com.imbaland.cinenigma.di

import com.imbaland.cinenigma.data.remote.CinenigmaFirestoreImpl
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CinenigmaModule() {
    @Provides
    internal fun providesCinenigmaFirestore(
    ): CinenigmaFirestore {
        return CinenigmaFirestoreImpl()
    }
}
