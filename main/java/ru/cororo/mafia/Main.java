package ru.cororo.mafia;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args) throws TelegramApiRequestException {
        ApiContextInitializer.init();

        TelegramBotsApi api = new TelegramBotsApi();

        try {
            api.registerBot(new MafiaBot());
        } catch (TelegramApiRequestException e) {
            throw new TelegramApiRequestException(e.getMessage());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        System.out.println("Bot started!");
    }
}
