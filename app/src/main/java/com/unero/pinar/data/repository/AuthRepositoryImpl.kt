package com.unero.pinar.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.unero.pinar.domain.repository.AuthRepository

class AuthRepositoryImpl: AuthRepository {
    private val auth = Firebase.auth

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun signOut() {
        auth.signOut()
    }
}