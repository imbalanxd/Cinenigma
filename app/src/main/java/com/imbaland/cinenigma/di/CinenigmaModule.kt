package com.imbaland.cinenigma.di

import com.imbaland.cinenigma.data.remote.CinenigmaFirestoreImpl
import com.imbaland.cinenigma.domain.remote.CinenigmaFirestore
import com.imbaland.common.data.auth.firebase.FirebaseAuthenticator
import com.imbaland.common.data.auth.firebase.FirebaseUser
import com.imbaland.movies.domain.repository.MoviesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CinenigmaModule() {
    @Provides
    internal fun providesCinenigmaFirestore(
        firebaseAuth: FirebaseAuthenticator,
        moviesRepository: MoviesRepository
    ): CinenigmaFirestore {
        return CinenigmaFirestoreImpl(firebaseAuth.account, moviesRepository)
    }
}
