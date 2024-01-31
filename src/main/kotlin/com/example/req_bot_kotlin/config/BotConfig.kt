package com.example.req_bot_kotlin.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig {
    @Value("\${bot.name}")
    lateinit var botName: String
    @Value("\${bot.token}")
    lateinit var token: String

}