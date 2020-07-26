package ru.cororo.mafia;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MafiaBot extends TelegramLongPollingBot {
    private boolean gameStarting = false;
    private boolean gameStarted = false;
    private List<User> players = new ArrayList<>();
    private HashMap<User, Role> playersRoles = new HashMap<>();
    private List<User> livePlayers;
    public Message messageJoinGame;
    private User starter;
    private List<BotCommand> commands = new ArrayList<>();
    private WaitStartThread thread;
    private boolean day = false;
    private int night = 0;
    private boolean doctorHealedHimself = false;

    public MafiaBot() throws TelegramApiException {

        commands.add(new BotCommand()
        .setCommand("/join")
        .setDescription("\u0412\u043e\u0439\u0442\u0438 \u0432 \u0438\u0433\u0440\u0443"));

        commands.add(new BotCommand()
                .setCommand("/start")
                .setDescription("\u0412\u043e\u0439\u0442\u0438 \u0432 \u0438\u0433\u0440\u0443"));

        commands.add(new BotCommand()
                .setCommand("/game")
                .setDescription("\u041d\u0430\u0447\u0430\u0442\u044c \u0441\u0431\u043e\u0440 \u0432 \u0438\u0433\u0440\u0443"));

        commands.add(new BotCommand()
                .setCommand("/startgame")
                .setDescription("\u0417\u0430\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u0438\u0433\u0440\u0443"));
        execute(new SetMyCommands()
        .setCommands(commands));
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && (update.getMessage().hasText())) {
            String text = update.getMessage().getText();

            if(update.getMessage().getChat().isSuperGroupChat()) {
                if (text.equalsIgnoreCase("/game@CororoMafiaBot")) {
                    if (gameStarted || gameStarting) {
                        try {
                            execute(
                                    new SendMessage()
                                            .setChatId(String.valueOf(update.getMessage().getFrom().getId()))
                                            .setText("\u0418\u0433\u0440\u0430 \u0443\u0436\u0435 \u0437\u0430\u043f\u0443\u0449\u0435\u043d\u0430!")
                            );
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            starter = update.getMessage().getFrom();

                            startGame(update.getMessage().getChat());
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        execute(new DeleteMessage()
                                .setChatId(update.getMessage().getChatId())
                                .setMessageId(update.getMessage().getMessageId()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (update.getMessage().getText().equalsIgnoreCase("/startgame@CororoMafiaBot")) {
                    if(starter.getId().equals(update.getMessage().getFrom().getId())) {
                        thread.stop();
                        game();
                    }

                    try {
                        execute(new DeleteMessage()
                                .setChatId(update.getMessage().getChatId())
                                .setMessageId(update.getMessage().getMessageId()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    if(gameStarted) {
                        if(!livePlayers.contains(update.getMessage().getFrom())) {
                            try {
                                execute(new DeleteMessage()
                                .setMessageId(update.getMessage().getMessageId())
                                .setChatId(update.getMessage().getChatId()));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if(!day) {
                                try {
                                    execute(new DeleteMessage()
                                            .setMessageId(update.getMessage().getMessageId())
                                            .setChatId(update.getMessage().getChatId()));
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else {

                if (text.equalsIgnoreCase("/join") || text.equalsIgnoreCase("/start") || text.equalsIgnoreCase("/start keyboard")) {

                    if (gameStarting) {
                        if (players.contains(update.getMessage().getFrom())) {
                            try {
                                execute(new SendMessage()
                                        .setText("\u0422\u044b \u0443\u0436\u0435 \u0432 \u0438\u0433\u0440\u0435! :)")
                                        .setChatId(String.valueOf(update.getMessage().getFrom().getId())));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                players.add(update.getMessage().getFrom());
                                execute(new SendMessage()
                                        .setText("[" + removeBadChars(update.getMessage().getFrom().getFirstName()) + "](tg://user?id=" + update.getMessage().getFrom().getId() + "), \u0442\u044b \u043f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u043b\u0441\u044f \u043a \u0438\u0433\u0440\u0435\\. \u041e\u0436\u0438\u0434\u0430\u0439 \u043d\u0430\u0447\u0430\u043b\u0430\\.")
                                        .setChatId(String.valueOf(update.getMessage().getFrom().getId()))
                                        .setParseMode("MarkdownV2"));

                                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                                List<InlineKeyboardButton> rowInline = new ArrayList<>();

                                rowInline.add(new InlineKeyboardButton().setText("\u041f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u0442\u044c\u0441\u044f").setUrl("https://t.me/CororoMafiaBot?start=keyboard"));
                                rowsInline.add(rowInline);
                                markupInline.setKeyboard(rowsInline);
                                execute(new EditMessageText()
                                        .setChatId(messageJoinGame.getChatId())
                                        .setMessageId(messageJoinGame.getMessageId())
                                        .setText("\u0421\u0431\u043e\u0440 \u0438\u0433\u0440\u043e\u043a\u043e\u0432\\! \u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u043e\u0432\\: " + players.size() + "\\.\n\n" + getMembers() + "\n\n\u0417\u0430\u043f\u0440\u043e\u0441\u0438\u043b \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044e \u0432 \u0438\u0433\u0440\u0443\\: [" + starter.getFirstName() + "](tg://user?id=" + starter.getId() + ")\\.")
                                        .setParseMode("MarkdownV2")
                                        .setReplyMarkup(markupInline));

                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            execute(new SendMessage()
                                    .setText("\u0418\u0433\u0440\u0430 \u0443\u0436\u0435 \u0438\u0434\u0451\u0442 \u0438\u043b\u0438 \u0441\u0431\u043e\u0440 \u0432 \u0438\u0433\u0440\u0443 \u043d\u0435 \u043d\u0430\u0447\u0438\u043d\u0430\u043b\u0441\u044f!")
                                    .setChatId(String.valueOf(update.getMessage().getFrom().getId())));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            User from = update.getCallbackQuery().getFrom();

            if(update.getCallbackQuery().getData().equalsIgnoreCase("join_game_button")) {
                if(gameStarting) {
                    if (players.contains(from)) {
                        try {
                            execute(new SendMessage()
                                    .setText("\u0422\u044b \u0443\u0436\u0435 \u0432 \u0438\u0433\u0440\u0435! :)")
                                    .setChatId(String.valueOf(from.getId())));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        players.add(from);

                        try {
                            execute(new SendMessage()
                                    .setText("[" + from.getFirstName() + "](tg://user?id=" + from.getId() + "), \u0442\u044b \u043f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u043b\u0441\u044f \u043a \u0438\u0433\u0440\u0435\\. \u041e\u0436\u0438\u0434\u0430\u0439 \u043d\u0430\u0447\u0430\u043b\u0430\\.")
                                    .setChatId(String.valueOf(from.getId()))
                                    .setParseMode("MarkdownV2"));

                            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                            List<InlineKeyboardButton> rowInline = new ArrayList<>();

                            rowInline.add(new InlineKeyboardButton().setText("\u041f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u0442\u044c\u0441\u044f").setCallbackData("join_game_button"));
                            rowsInline.add(rowInline);
                            markupInline.setKeyboard(rowsInline);

                            execute(new EditMessageText()
                                    .setChatId(messageJoinGame.getChatId())
                                    .setMessageId(messageJoinGame.getMessageId())
                                    .setText("\u0421\u0431\u043e\u0440 \u0438\u0433\u0440\u043e\u043a\u043e\u0432\\! \u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u043e\u0432\\: " + players.size() + "\\.\n\n" + getMembers() + "\n\n\u0417\u0430\u043f\u0440\u043e\u0441\u0438\u043b \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044e \u0432 \u0438\u0433\u0440\u0443: [" + starter.getFirstName() + "](tg://user?id=" + starter.getId() + ")\\.")
                                    .setParseMode("MarkdownV2")
                                    .setReplyMarkup(markupInline));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        execute(new SendMessage()
                                .setText("\u0418\u0433\u0440\u0430 \u0443\u0436\u0435 \u0438\u0434\u0451\u0442 \u0438\u043b\u0438 \u0441\u0431\u043e\u0440 \u0432 \u0438\u0433\u0440\u0443 \u043d\u0435 \u043d\u0430\u0447\u0438\u043d\u0430\u043b\u0441\u044f!")
                                .setChatId(String.valueOf(update.getCallbackQuery().getFrom().getId())));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if(update.hasMessage() && (update.getMessage().hasPhoto() || update.getMessage().hasSticker())) {
            if(gameStarted) {
                if(!livePlayers.contains(update.getMessage().getFrom())) {
                    try {
                        execute(new DeleteMessage()
                                .setMessageId(update.getMessage().getMessageId())
                                .setChatId(update.getMessage().getChatId()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    if(!day) {
                        try {
                            execute(new DeleteMessage()
                                    .setMessageId(update.getMessage().getMessageId())
                                    .setChatId(update.getMessage().getChatId()));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "MafiaBot";
    }

    @Override
    public String getBotToken() {
        return "1222498397:AAHm0dmy1fkPcIILgf62Dr9VZyaK3dD4BoE";
    }

    public void startGame(Chat chat) throws TelegramApiException {
        gameStarting = true;

        SendMessage sendMessage = new SendMessage()
                .setText("\u0421\u0431\u043e\u0440 \u0438\u0433\u0440\u043e\u043a\u043e\u0432\\! \u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a\u043e\u0432\\: " + players.size() + "\\.\n\n\u0417\u0430\u043f\u0440\u043e\u0441\u0438\u043b \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044e \u0432 \u0438\u0433\u0440\u0443: [" + starter.getFirstName() + "](tg://user?id=" + starter.getId() + ")\\.")
                .setChatId(chat.getId())
                .enableMarkdownV2(true);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        rowInline.add(new InlineKeyboardButton().setText("\u041f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u0442\u044c\u0441\u044f").setUrl("https://t.me/CororoMafiaBot?start=keyboard"));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        messageJoinGame = execute(sendMessage);

        thread = new WaitStartThread(this);
        thread.start();
    }

    public String getMembers() {
        List<String> usernames = new ArrayList<>();
        players.forEach(u -> usernames.add("[" + removeBadChars(u.getFirstName()) + "](tg://user?id="+ u.getId() + ")"));

        StringBuilder builder = new StringBuilder();
        usernames.forEach(s -> {
            if(builder.length() == 0) builder.append(s);
            else builder.append("\\, ").append(s);
        });

        return builder.toString();
    }

    public void game() {
        night ++;
        try {
            execute(new EditMessageText()
                    .setText("\u0418\u0433\u0440\u0430 \u043d\u0430\u0447\u0430\u043b\u0430\u0441\u044c. \u0412\u0445\u043e\u0434 \u0432 \u0438\u0433\u0440\u0443 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d.")
                    .setChatId(messageJoinGame.getChatId())
                    .setMessageId(messageJoinGame.getMessageId()));

            if(players.size() < 4) {
                execute(new SendMessage()
                        .setText("\u041d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u0438\u0433\u0440\u043e\u043a\u043e\u0432. \u0418\u0433\u0440\u0430 \u043e\u0442\u043c\u0435\u043d\u0435\u043d\u0430.")
                        .setChatId(messageJoinGame.getChatId()));

                livePlayers = new ArrayList<>();
                players = new ArrayList<>();
                gameStarting = false;
                starter = null;
                return;
            }

            execute(new SendMessage()
            .setText("\u0418\u0433\u0440\u0430 \u043d\u0430\u0447\u0438\u043d\u0430\u0435\u0442\u0441\u044f\\!")
            .setChatId(messageJoinGame.getChatId())
            .enableMarkdownV2(true));

            gameStarted = true;
            gameStarting = false;
            livePlayers = players;

            execute(new SendMessage()
            .setChatId(messageJoinGame.getChatId())
            .setText(getLiveMembers())
            .enableMarkdownV2(true));

            setRoles();

            for (User user : playersRoles.keySet()) {
                Role role = playersRoles.get(user);

                if(role != Role.MAFIA && role != Role.DON) {
                    execute(new SendMessage()
                            .setText("\u0422\u0432\u043e\u044f \u0440\u043e\u043b\u044c - " + getRole(role) + "!")
                            .setChatId(String.valueOf(user.getId())));
                } else {
                    if (role == Role.DON) {
                        List<User> mafia = playersRoles.keySet().stream().filter(u -> (playersRoles.get(u) == Role.MAFIA) && !u.getId().equals(user.getId())).collect(Collectors.toList());
                        if(!mafia.isEmpty()) {
                            List<String> mafiaStringList = new ArrayList<>();
                            mafia.forEach(u -> mafiaStringList.add("[" + removeBadChars(u.getFirstName()) + "](tg://user?id=" + u.getId() + ") \\- " + getRole(playersRoles.get(u))));

                            String mafiaString = String.join("\n", mafiaStringList);

                            execute(new SendMessage()
                                    .setText("\u0422\u0432\u043e\u044f \u0440\u043e\u043b\u044c \\- " + getRole(role) + "\\!\n" +
                                            "\u0417\u0430\u043f\u043e\u043c\u043d\u0438 \u0441\u0432\u043e\u0438\u0445 \u0441\u043e\u044e\u0437\u043d\u0438\u043a\u043e\u0432\\: \n" + mafiaString)
                                    .setChatId(String.valueOf(user.getId()))
                                    .enableMarkdownV2(true));
                        } else {
                            execute(new SendMessage()
                                    .setText("\u0422\u0432\u043e\u044f \u0440\u043e\u043b\u044c - " + getRole(role) + "!")
                                    .setChatId(String.valueOf(user.getId())));
                        }
                    } else {
                        List<User> mafia = playersRoles.keySet().stream().filter(u -> (playersRoles.get(u) == Role.MAFIA || playersRoles.get(u) == Role.DON) && !u.getId().equals(user.getId())).collect(Collectors.toList());
                        List<String> mafiaStringList = new ArrayList<>();
                        mafia.forEach(u -> mafiaStringList.add("[" + u.getFirstName() + "](tg://user?id=" + u.getId() + ") \\- " + getRole(playersRoles.get(u))));

                        String mafiaString = String.join("\n", mafiaStringList);

                        execute(new SendMessage()
                                .setText("\u0422\u0432\u043e\u044f \u0440\u043e\u043b\u044c \\- " + getRole(role) + "\\!\n" +
                                        "\u0417\u0430\u043f\u043e\u043c\u043d\u0438 \u0441\u0432\u043e\u0438\u0445 \u0441\u043e\u044e\u0437\u043d\u0438\u043a\u043e\u0432\\: \n" + mafiaString)
                                .setChatId(String.valueOf(user.getId()))
                                .enableMarkdownV2(true));
                    }
                }
            }

            execute(new SendMessage()
            .setText("\u041f\u0435\u0440\u0432\u0430\u044f \u043d\u043e\u0447\u044c. \u041e\u043d\u0430 \u043f\u0440\u043e\u0434\u043b\u0438\u0442\u0441\u044f 30 \u0441\u0435\u043a\u0443\u043d\u0434. \u0412 \u044d\u0442\u0443 \u043d\u043e\u0447\u044c \u0432\u0441\u0435 \u0431\u0435\u0437\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0442 \u0438 \u0441\u043f\u044f\u0442...")
            .setChatId(messageJoinGame.getChatId()));
            execute(new SendAnimation()
            .setAnimation("CgACAgIAAx0CTQ_-GQACGV9fHVzBmfPiq2hBNxqkOsVbGHGrNgACmAYAAlBsuUj7-jRzlXDn2BoE")
            .setChatId(messageJoinGame.getChatId()));

            new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(30000);
                        game2();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void game2() {
        try {
            execute(new SendMessage()
            .setChatId(messageJoinGame.getChatId())
            .setText("\u041f\u0435\u0440\u0432\u044b\u0439 \u0434\u0435\u043d\u044c. \u041e\u0431\u0449\u0430\u0439\u0442\u0435\u0441\u044c, \u0441\u0435\u0433\u043e\u0434\u043d\u044f \u043d\u0438\u043a\u043e\u0433\u043e \u043d\u0435 \u043a\u0430\u0437\u043d\u0438\u043c."));
            execute(new SendAnimation()
            .setAnimation("CgACAgIAAx0CTQ_-GQACGdVfHV72GbkCUrMdVOT2lPhJ2HWsDAAC3woAAki9YEgtaqhGedqM8xoE")
            .setChatId(messageJoinGame.getChatId()));
            day = true;

            new Thread(() -> {
                try {
                    Thread.sleep(120000);
                    gameLoop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void gameLoop() {
        night++;
        try {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText("\u041f\u0435\u0440\u0435\u0439\u0442\u0438 \u043a \u0431\u043e\u0442\u0443").setUrl("https://t.me/CororoMafiaBot"));
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);

            execute(new SendMessage()
                    .setText("\u041d\u0430\u0447\u0438\u043d\u0430\u0435\u0442\u0441\u044f " + night + " \u043d\u043e\u0447\u044c... \u041d\u0430 \u0443\u043b\u0438\u0446\u0443 \u0432\u044b\u0445\u043e\u0434\u044f\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0441\u0430\u043c\u044b\u0435 \u0431\u0435\u0441\u0441\u0442\u0440\u0430\u0448\u043d\u044b\u0435.")
                    .setChatId(messageJoinGame.getChatId())
                    .setReplyMarkup(markupInline));
            execute(new SendAnimation()
                    .setAnimation("CgACAgIAAx0CTQ_-GQACGV9fHVzBmfPiq2hBNxqkOsVbGHGrNgACmAYAAlBsuUj7-jRzlXDn2BoE")
                    .setChatId(messageJoinGame.getChatId()));

            sendActiveRoleMessages();
            day = false;

            new Thread(() -> {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendActiveRoleMessages() {
        for (User u : playersRoles.keySet()) {
            switch (playersRoles.get(u)) {
                case PEACE: break;
                case DOCTOR:
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(String.valueOf(u.getId()))
                            .setText("\u041a\u043e\u0433\u043e \u0431\u0443\u0434\u0435\u043c \u043b\u0435\u0447\u0438\u0442\u044c?");

                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                    if (doctorHealedHimself) {
                        for (User user : livePlayers.stream().filter(userr -> !userr.getId().equals(u.getId())).collect(Collectors.toList())) {
                            List<InlineKeyboardButton> rowInline = new ArrayList<>();
                            if (user.getLastName() != null)
                                rowInline.add(new InlineKeyboardButton().setText(user.getFirstName() + " " + user.getLastName()).setCallbackData("doctor_" + user.getId()));
                            else
                                rowInline.add(new InlineKeyboardButton().setText(user.getFirstName()).setCallbackData("doctor_" + user.getId()));
                            rowsInline.add(rowInline);
                        }
                    } else {
                        for (User user : livePlayers) {
                            List<InlineKeyboardButton> rowInline = new ArrayList<>();
                            if (user.getLastName() != null)
                                rowInline.add(new InlineKeyboardButton().setText(user.getFirstName() + " " + user.getLastName()).setCallbackData("doctor_" + user.getId()));
                            else
                                rowInline.add(new InlineKeyboardButton().setText(user.getFirstName()).setCallbackData("doctor_" + user.getId()));
                            rowsInline.add(rowInline);
                        }
                    }

                    markupInline.setKeyboard(rowsInline);
                    sendMessage.setReplyMarkup(markupInline);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
                case SHERLOCK:
                    SendMessage sendMessagee = new SendMessage()
                            .setChatId(String.valueOf(u.getId()))
                            .setText("\u041a\u043e\u0433\u043e \u0431\u0443\u0434\u0435\u043c \u043f\u0440\u043e\u0432\u0435\u0440\u044f\u0442\u044c?");

                    InlineKeyboardMarkup markupInlinee = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInlinee = new ArrayList<>();

                        for (User user : livePlayers.stream().filter(userr -> !userr.getId().equals(u.getId())).collect(Collectors.toList())) {
                            List<InlineKeyboardButton> rowInline = new ArrayList<>();
                            if (user.getLastName() != null)
                                rowInline.add(new InlineKeyboardButton().setText(user.getFirstName() + " " + user.getLastName()).setCallbackData("sherlock_" + user.getId()));
                            else
                                rowInline.add(new InlineKeyboardButton().setText(user.getFirstName()).setCallbackData("sherlock_" + user.getId()));
                            rowsInlinee.add(rowInline);
                        }

                    markupInlinee.setKeyboard(rowsInlinee);
                    sendMessagee.setReplyMarkup(markupInlinee);
                    try {
                        execute(sendMessagee);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void setRoles() throws TelegramApiException {
        if (players.size() < 6 && players.size() >= 4) {
            for(Role role : Role.values()) {
                switch (role) {
                    case MAFIA:
                    case MURDER:
                    case PEACE:
                    case SHERLOCK:
                        break;
                    default: setRandomUser(role); break;
                }
            }

            for (User user : players.stream().filter(u -> !playersRoles.containsKey(u)).collect(Collectors.toList())) {
                playersRoles.put(user, Role.PEACE);
                System.out.println(user.getUserName() + Role.PEACE);
            }
        }

        if (players.size() < 8 && players.size() >= 6) {
            for(Role role : Role.values()) {
                switch (role) {
                    case MAFIA:
                    case MURDER:
                    case PEACE:
                        break;
                    default: setRandomUser(role); break;
                }
            }

            for (User user : players.stream().filter(u -> !playersRoles.containsKey(u)).collect(Collectors.toList())) {
                playersRoles.put(user, Role.PEACE);
                System.out.println(user.getUserName() + Role.PEACE);
            }
        }

        if (players.size() < 10 && players.size() >= 8) {
            for(Role role : Role.values()) {
                switch (role) {
                    case MURDER:
                    case PEACE:
                        break;
                    default: setRandomUser(role); break;
                }
            }

            for (User user : players.stream().filter(u -> !playersRoles.containsKey(u)).collect(Collectors.toList())) {
                playersRoles.put(user, Role.PEACE);
                System.out.println(user.getUserName() + Role.PEACE);
            }
        }

        if (players.size() < 12 && players.size() >= 10) {
            for(Role role : Role.values()) {
                switch (role) {
                    case PEACE:
                        break;
                    default: setRandomUser(role); break;
                }
            }

            for (User user : players.stream().filter(u -> !playersRoles.containsKey(u)).collect(Collectors.toList())) {
                playersRoles.put(user, Role.PEACE);
                System.out.println(user.getUserName() + Role.PEACE);
            }
        }
    }

    public void setRandomUser(Role role) {
        User user = players.get((int) (Math.random() * players.size()));
        if(!playersRoles.containsKey(user)) {
            playersRoles.put(user, role);
            System.out.println(user.getUserName() + role);
        } else {
            setRandomUser(role);
        }
    }

    public String getLiveMembers() {
        List<String> usernames = new ArrayList<>();
        livePlayers.forEach(u -> usernames.add("[" + removeBadChars(u.getFirstName()) + "](tg://user?id="+ u.getId() + ")"));

        StringBuilder builder = new StringBuilder();
        builder.append("\u0416\u0438\u0432\u044b\u0435 \u0438\u0433\u0440\u043e\u043a\u0438\\:\n");
        for(int i = 0; i < usernames.size(); i++) {
            builder.append(i+1).append("\\. ").append(usernames.toArray()[i]).append("\n");
        }
        return builder.toString();
    }

    public String getRole(Role role) {
        switch (role) {
            case DOCTOR: return "\u0414\u043e\u043a\u0442\u043e\u0440";
            case MAFIA: return "\u041c\u0430\u0444\u0438\u044f";
            case DON: return "\u0414\u043e\u043d";
            case SHERLOCK: return "\u041a\u043e\u043c\u0438\u0441\u0430\u0440";
            case PEACE: return "\u041c\u0438\u0440\u043d\u044b\u0439 \u0436\u0438\u0442\u0435\u043b\u044c";
            case MURDER: return "\u041c\u0430\u043d\u044c\u044f\u043a";
            default: return "";
        }
    }

    public String removeBadChars(String s) {
        return s.replace("-", "\\-")
                .replace("_", "\\_"
                .replace("!", "\\!")
                .replace(".", "\\."));
    }
}
