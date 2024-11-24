package io.inzure.app.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import io.inzure.app.data.model.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import io.inzure.app.viewmodel.GlobalUserSession

class UserRepository {

    private var listenerRegistration: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()

    suspend fun addUser(user: User): Boolean {
        return try {
            db.collection("Users")
                .add(user)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding user: ${e.message}")
            false
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            db.collection("Users")
                .document(user.id)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user: ${e.message}")
            false
        }
    }

    suspend fun deleteUser(user: User): Boolean {
        return try {
            db.collection("Users")
                .document(user.id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deleting user: ${e.message}")
            false
        }
    }

    fun getUsers(onUsersChanged: (List<User>) -> Unit) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection("Users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "Error fetching users: ${error.message}")
                    return@addSnapshotListener
                }

                val usersList = snapshot?.documents?.mapNotNull { document ->
                    val user = document.toObject<User>()
                    user?.copy(id = document.id)
                } ?: emptyList()

                onUsersChanged(usersList)
            }
    }

    suspend fun updateProfileImage(userId: String, imageUri: String) {
        try {
            val storageReference = FirebaseStorage.getInstance().reference
            val fileUri = Uri.parse(imageUri)

            // Obtener la URL de la imagen anterior
            val userDoc = FirebaseFirestore.getInstance().collection("Users").document(userId).get().await()
            val oldImageUrl = userDoc.getString("image") // URL anterior, puede ser nula o vacía

            // Eliminar la imagen anterior solo si existe
            if (!oldImageUrl.isNullOrEmpty()) {
                try {
                    val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl)
                    oldImageRef.delete().await() // Elimina la imagen anterior
                } catch (e: Exception) {
                    Log.e("UserRepository", "Error eliminando la imagen anterior: ${e.message}")
                }
            }

            // Subir la nueva imagen con el nombre del `userId` y su extensión original
            val fileExtension = fileUri.lastPathSegment?.substringAfterLast('.', "jpg") ?: "jpg" // Extensión predeterminada
            val newImageRef = storageReference.child("profile_images/$userId.$fileExtension")

            // Subir archivo
            newImageRef.putFile(fileUri).await()

            // Obtener URL de descarga
            val downloadUrl = newImageRef.downloadUrl.await()

            // Actualizar el campo `image` en Firestore
            FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .update("image", downloadUrl.toString())
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error actualizando imagen: ${e.message}")
            throw e
        }
    }

    suspend fun updateEmail(newEmail: String): Boolean {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Enviar correo de verificación antes de actualizar el email
                currentUser.verifyBeforeUpdateEmail(newEmail).await()

                // Cerrar sesión después de enviar el correo de verificación
                FirebaseAuth.getInstance().signOut()

                true
            } else {
                Log.e("UserRepository", "No hay usuario autenticado.")
                false
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al actualizar el correo: ${e.message}")
            false
        }
    }

    suspend fun verifyEmail(email: String, userId: String): Boolean {
        return try {
            val result = FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).await()
            val signInMethods = result.signInMethods
            // Si hay métodos de inicio de sesión asociados al email, está registrado
            if (signInMethods != null && signInMethods.isNotEmpty()) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                // Si el email pertenece al usuario actual, se considera válido
                if (currentUser != null && currentUser.uid == userId && currentUser.email == email) {
                    true // El email es del usuario actual
                } else {
                    false // El email pertenece a otro usuario
                }
            } else {
                true // El email no está registrado
            }
        } catch (e: Exception) {
            Log.e("verifyEmail", "Error al verificar el email: ${e.message}")
            false
        }
    }


    fun removeListener() {
        listenerRegistration?.remove()
    }


}
