package com.example.req_bot_kotlin.repository

import com.example.req_bot_kotlin.models.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun existsByUserID(userID: Long): Boolean
    fun existsByPhone(phone: String): Boolean
}