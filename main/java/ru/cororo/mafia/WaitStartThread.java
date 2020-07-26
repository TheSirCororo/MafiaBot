package ru.cororo.mafia;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class WaitStartThread extends Thread {
    private long waitTime = 90;
    private final MafiaBot bot;

    public WaitStartThread(MafiaBot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        for (; waitTime > 0; waitTime--) {
            try {
                sleep(1000);

                switch (Math.toIntExact(waitTime)) {
                    case 60:
                        bot.execute(
                                new SendMessage()
                                        .setChatId(bot.messageJoinGame.getChatId())
                                        .setReplyToMessageId(bot.messageJoinGame.getMessageId())
                                        .setText("\u0414\u043e \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0438\u044f \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438 60 \u0441\u0435\u043a.")
                        );
                        break;
                    case 30:
                        bot.execute(
                                new SendMessage()
                                        .setChatId(bot.messageJoinGame.getChatId())
                                        .setReplyToMessageId(bot.messageJoinGame.getMessageId())
                                        .setText("\u0414\u043e \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0438\u044f \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438 30 \u0441\u0435\u043a.")
                        );
                        break;
                }

                System.out.println(waitTime);
            } catch (InterruptedException | TelegramApiException e) {
                e.printStackTrace();
            }
        }

        bot.game();
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }
}
