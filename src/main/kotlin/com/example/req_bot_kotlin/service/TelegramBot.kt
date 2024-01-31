package com.example.req_bot_kotlin.service

import com.example.req_bot_kotlin.config.BotConfig
import com.example.req_bot_kotlin.models.User
import com.example.req_bot_kotlin.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class TelegramBot(private val userRepository: UserRepository, private val config: BotConfig) :
    TelegramLongPollingBot(config.token) {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    init {
        val listOfCommands = mutableListOf<BotCommand>()
        listOfCommands.add(BotCommand("/start", "Приветственное сообщение"))
        listOfCommands.add(BotCommand("/registration", "Регистрация в сервисе"))
        try {
            execute(SetMyCommands(listOfCommands, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException) {
            log.error("Error setting bot's command list: " + e.message)
        }
    }

    override fun getBotUsername(): String {
        return config.botName
    }

    override fun getBotToken(): String {
        return config.token
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasContact()) {
            val chatID = update.message.chatId
            val fromId = update.message.from.id
            var phoneNumber = update.message.contact.phoneNumber
            if (phoneNumber[0] == '+') {
                phoneNumber = phoneNumber.substring(1)
            }
            val userID = update.message.contact.userId
            if (fromId == userID && !userRepository.existsByPhone(phoneNumber)) {
                val user = User(null,userID,phoneNumber)
                userRepository.save(user)
                sendMessage(chatID, "Регистрация прошла успешно")
            } else if (userRepository.existsByPhone(phoneNumber)) {
                sendMessage(chatID, "Ваш номер телефона уже зарегистрирован")
            } else {
                sendMessage(chatID, "Что-то пошло не так")
            }
        }
        if (update.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatID = update.message.chatId
            when (messageText) {
                "/start" -> startCommandReceived(chatID, update.message.chat.firstName)
                "/registration" -> registrationCommand(chatID)
                else -> sendMessage(chatID, "Прости, я тебя не понимаю")
            }
        }
    }

    private fun registrationCommand(chatID: Long) {
        val contactButton = KeyboardButton("Поделиться контактом")
        contactButton.requestContact = true
        val row = KeyboardRow()
        row.add(contactButton)

        val keyboard = mutableListOf<KeyboardRow>()
        keyboard.add(row)

        val markup = ReplyKeyboardMarkup()
        markup.keyboard = keyboard
        markup.oneTimeKeyboard = true
        markup.resizeKeyboard = true

        val message = SendMessage()
        message.chatId = chatID.toString()
        message.text = "Нажми на кнопку 'Поделиться контактом'!"
        message.replyMarkup = markup

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            log.error("Error occurred: " + e.message)
        }
    }
    private fun startCommandReceived(chatID: Long, name: String) {
        val answer = "Привет, $name приятно познакомиться!\n" +
                "Я бот для регистрации в *название сервиса*.\n" +
                "Чтобы зарегистрироваться введи команду /registration"
        sendMessage(chatID, answer)
    }

    private fun sendMessage(chatID: Long, textToSend: String) {
        val message = SendMessage()
        message.chatId = chatID.toString()
        message.text = textToSend
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            log.error("Error occurred: " + e.message)
        }
    }

}

