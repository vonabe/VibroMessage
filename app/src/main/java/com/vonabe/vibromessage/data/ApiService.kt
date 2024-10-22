package com.vonabe.vibromessage.data

import com.vonabe.vibromessage.data.model.MessageResponse
import com.vonabe.vibromessage.data.model.User
import com.vonabe.vibromessage.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("/api/auth")
    suspend fun authUser(@Body user: User): UserResponse

    @POST("/api/addFriend")
    suspend fun addFriend(@Query("userId") userId: String, @Query("friendId") friendId: String): MessageResponse

    @POST("/api/removeFriend")
    suspend fun removeFriend(@Query("userId") userId: String, @Query("friendId") friendId: String): MessageResponse

    @POST("/api/sendMessage")
    suspend fun sendMessage(
        @Query("userId") userId: String,
        @Query("friendId") friendId: String,
        @Query("messageBody") messageBody: String
    ): MessageResponse

}