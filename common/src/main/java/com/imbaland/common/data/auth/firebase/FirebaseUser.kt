package com.imbaland.common.data.auth.firebase

import com.google.firebase.auth.FirebaseUser as FirebaseLibraryUser
import com.imbaland.common.domain.auth.AuthenticatedUser

class FirebaseUser(private val user: FirebaseLibraryUser): AuthenticatedUser {
    override val id: String
        get() = user.uid
    override val name: String
        get() = user.displayName?:"User$id"
}