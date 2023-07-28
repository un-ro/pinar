package com.unero.pinar.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}