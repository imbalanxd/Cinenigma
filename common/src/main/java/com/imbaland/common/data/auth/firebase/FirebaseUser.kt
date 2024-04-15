package com.imbaland.common.data.auth.firebase

import com.google.firebase.auth.FirebaseUser as FirebaseLibraryUser
import com.imbaland.common.domain.auth.AuthenticatedUser

class FirebaseUser(
    user: FirebaseLibraryUser? = null
) : AuthenticatedUser(
    id = user?.uid ?: "",
    name = if (!user?.displayName.isNullOrEmpty()) user?.displayName!! else "User${user?.uid ?: ""}"
)