package ru.job4j.bmb.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.job4j.bmb.aspects.SentContentExeption;
import ru.job4j.bmb.content.Content;

@Service
public class TelegramBotService extends TelegramLongPollingBot implements SentContent {

    private final BotCommandHandler handler;
    private String botName;

    public TelegramBotService(@Value("${telegram.bot.name}") String botName,
                              @Value("${telegram.bot.token}") String botToken,
                              BotCommandHandler handler) {
        super(botToken);
        this.handler = handler;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handler.handleCallback(update.getCallbackQuery())
                    .ifPresent(this::sent);
        } else if (update.hasMessage() && update.getMessage().getText() != null) {
            handler.commands(update.getMessage())
                    .ifPresent(this::sent);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void sent(Content content) {
        try {
            if (content.getAudio() != null) {
                SendAudio sendAudio = new SendAudio();
                sendAudio.setChatId(content.getChatId());
                sendAudio.setAudio(content.getAudio());
                if (content.getText() != null) {
                    sendAudio.setCaption(content.getText());
                }
                execute(sendAudio);
            } else if (content.getText() != null && !content.getText().isEmpty()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(content.getChatId());
                sendMessage.setText(content.getText());
                if (content.getMarkup() != null) {
                    sendMessage.setReplyMarkup(content.getMarkup());
                }
                execute(sendMessage);
            } else if (content.getPhoto() != null) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(content.getChatId());
                sendPhoto.setPhoto(content.getPhoto());
                if (content.getText() != null) {
                    sendPhoto.setCaption(content.getText());
                }
                execute(sendPhoto);
            }
        } catch (Exception e) {
            throw new SentContentExeption("Ошибка отправки контента", e);
        }
    }
}
