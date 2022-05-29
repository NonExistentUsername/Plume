package com.unknown.plumedesktop.tdcontroller;

import com.unknown.plumedesktop.AuthSceneController;
import com.unknown.plumedesktop.PlumeApplication;
import com.unknown.plumedesktop.controllers.MainController;
import com.unknown.plumedesktop.models.ThumbnailChat;
import com.unknown.plumedesktop.tools.IObservable;
import com.unknown.plumedesktop.tools.ObservableComponent;
import com.unknown.plumedesktop.tools.IObserver;
import javafx.application.Platform;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CEHA implements Client.ExceptionHandler {

    @Override
    public void onException(Throwable e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
        System.out.println(e);
        System.out.println("CEHA.onException updateExceptionHandler");
    }

}

class CEHB implements Client.ExceptionHandler {

    @Override
    public void onException(Throwable e) {
        System.out.println(e.getMessage());
        System.out.println("CEHB.onException defaultExceptionHandler");
    }

}

public class TelegramClient implements IObservable, IObserver {
    private static class OrderedChat implements Comparable<OrderedChat> {
        final long chatId;
        final TdApi.ChatPosition position;

        OrderedChat(long chatId, TdApi.ChatPosition position) {
            this.chatId = chatId;
            this.position = position;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.position.order != o.position.order) {
                return o.position.order < this.position.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }
    }

    public class RetryResultHandler implements Client.ResultHandler {
        private static int counter = 0;

        private int id = 0;

        private final TdApi.Function query;
        private boolean flag = false;


        public RetryResultHandler(TdApi.Function query) {
            this.query = query;
            this.id = counter;
            counter++;
            System.out.println("RetryResultHandler" + Integer.toString(this.id) + "created");
        }

        @Override
        public void onResult(TdApi.Object object) {
            System.out.println("RetryResultHandler" + Integer.toString(this.id) + "started");
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    TdApi.Error error = (TdApi.Error) object;
                    System.err.println("Receive an error:\n" + object);
                    if(!flag) {
                        send(query, this);
                        flag = true;
                    } else {
                        System.err.println("Skipped");
                    }
                }
                case TdApi.Ok.CONSTRUCTOR -> System.err.println("Receive an OK\n");
                default -> System.err.println("Receive wrong response from TDLib:\n" + object);
            }
            System.out.println("RetryResultHandler" + Integer.toString(this.id) + "done");
        }
    }

    private class ChatHistoryHandler implements Client.ResultHandler {
        private final MainController mainController;
        private final ArrayList<TdApi.Message> ms;
        private final int limit;
        private final Long chatId;

        public ChatHistoryHandler(ArrayList<TdApi.Message> ms, Long chatId, int limit, MainController mainController) {
            this.mainController = mainController;
            this.ms = ms;
            this.chatId = chatId;
            this.limit = limit;
        }

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    TdApi.Error error = (TdApi.Error) object;
                    System.err.println("Receive an error:\n" + object);
                }
                case TdApi.Messages.CONSTRUCTOR -> {
                    this.ms.addAll(Arrays.asList(((TdApi.Messages)object).messages));
                    if(((TdApi.Messages)object).totalCount > 0 && this.ms.size() < this.limit) {

                        TdApi.Function query = new TdApi.GetChatHistory(chatId, this.ms.get(this.ms.size() - 1).id, 0, this.limit - this.ms.size(), false);
                        send(
                                query,
                                new TelegramClient.ChatHistoryHandler(this.ms, chatId, this.limit, mainController));
                    } else {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                mainController.addChatHistory(ms, ((TdApi.Messages)object).totalCount);
                            }
                        });
                    }
                }
                default -> System.err.println("Receive wrong response from TDLib:\n" + object);
            }
        }
    }

    private TdApi.User Me = null;
    private Lock getMeLock = new ReentrantLock();

    private class GetMeHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR -> {
                    TdApi.Error error = (TdApi.Error) object;
                    System.err.println("Receive an error:\n" + object);
                }
                case TdApi.User.CONSTRUCTOR -> {
                    Me = (TdApi.User)object;
                }
                default -> System.err.println("Receive wrong response from TDLib:\n" + object);
            }
        }
    }

    public Long getMyId() {
        TdApi.Function query = new TdApi.GetMe();
        getMeLock.lock();
        client.send(
                query,
                new TelegramClient.GetMeHandler());
        while(Me == null)
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        return Me.id;
    }

    public void logout() {
        TdApi.Function query = new TdApi.LogOut();
        send(query, new TelegramClient.RetryResultHandler(query));
    }

    public void quit() {
        TdApi.Function query = new TdApi.Close();
        send(query, new TelegramClient.RetryResultHandler(query));
    }

    private final  ObservableComponent observable_component = new ObservableComponent();

    private Client client;
    private final Lock lock = new ReentrantLock();

    private ITDParams params;
    private Client.ResultHandler updateHandler;

    private final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<Long, TdApi.User>();
    private final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Long, TdApi.BasicGroup>();
    private final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Long, TdApi.Supergroup>();
    private final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    private static final ConcurrentMap<Long, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Long, TdApi.UserFullInfo>();
    private static final ConcurrentMap<Long, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>();
    private static final ConcurrentMap<Long, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>();

    private boolean haveFullMainChatList = false;
    private boolean isModifiedChatList = false;
    private Long currentChatId = Long.valueOf(0);
    private final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();

    public boolean resetIsModifiedChatList() {
        lock.lock();
        boolean result = this.isModifiedChatList;
        this.isModifiedChatList = false;
        lock.unlock();
        return result;
    }

    public boolean resetIsModifiedChatList(boolean newValue) {
        lock.lock();
        boolean result = this.isModifiedChatList;
        this.isModifiedChatList = newValue;
        lock.unlock();
        return result;
    }

    public TdApi.Chat getChat(Long chatId) {
        return chats.get(chatId);
    }

    public ArrayList<ThumbnailChat> getChatList() {
        ArrayList<ThumbnailChat> result = new ArrayList<>();
        this.getMainChatList(512);
        synchronized (mainChatList) {
            mainChatList.forEach(chat -> {
                TdApi.Chat c = chats.get(chat.chatId);
                TdApi.Message m = c.lastMessage;
                String lastMessage = "";
                if (m != null)
                    if (m.content instanceof TdApi.MessageText)
                        lastMessage = ((TdApi.MessageText) m.content).text.text;
                result.add(new ThumbnailChat(c.title, lastMessage, c.id));
            });
        }
        return result;
    }

    private MainController mainController = null;
    private boolean _isLoggedIn = false;

    public boolean isLogggedIn() {
        this.lock.lock();
        boolean result = _isLoggedIn;
        this.lock.unlock();
        return result;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setCurrentChatId(final Long chatId) {
        this.currentChatId = chatId;
    }

    public void sendMessage(String message) {
        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        TdApi.Function query = new TdApi.SendMessage(this.currentChatId, 0, 0, null, null, content);
        send(query, new TelegramClient.RetryResultHandler(query));
    }

    public void getChatMessageList(final Long chatId) {
        int offset = 0;
        int limit = 40;

        TdApi.Function query = new TdApi.GetChatHistory(chatId, 0, 0, limit, false);
        send(
                query,
                new TelegramClient.ChatHistoryHandler(new ArrayList<>(), chatId, limit, mainController));
    }

    private void getMainChatList(final int limit) {
        synchronized (mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size()) {
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), limit - mainChatList.size()), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (mainChatList) {
                                        haveFullMainChatList = true;
                                    }
                                } else {
                                    System.err.println("Receive an error for LoadChats:\n" + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                getMainChatList(limit);
                                break;
                            default:
                                System.err.println("Receive wrong response from TDLib:\n" + object);
                        }
                    }
                });
            }
        }
    }

    private void setChatPositions(TdApi.Chat chat, TdApi.ChatPosition[] positions) {
        synchronized (mainChatList) {
            synchronized (chat) {
                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isRemoved = mainChatList.remove(new OrderedChat(chat.id, position));
                        assert isRemoved;
                    }
                }

                chat.positions = positions;

                for (TdApi.ChatPosition position : chat.positions) {
                    if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                        boolean isAdded = mainChatList.add(new OrderedChat(chat.id, position));
                        assert isAdded;
                    }
                }
            }
        }
    }

    protected void putChat(TdApi.Chat chat) {
        this.lock.lock();
        chats.put(chat.id, chat);
        this.lock.unlock();
    }

    static {
        try {
            System.loadLibrary("libcrypto-1_1-x64");
            System.loadLibrary("libssl-1_1-x64");
            System.loadLibrary("zlib1");
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }
    public TelegramClient() {}

    @Override
    public void addObserver(IObserver observer) {
        observable_component.addObserver(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observable_component.removeObserver(observer);
    }

    public void create(Client.ResultHandler authUpdateHandler, ITDParams params) {
        this.params = params;
        this.updateHandler = new UpdateHandler(authUpdateHandler);

        Client.execute(new TdApi.SetLogVerbosityLevel(5));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }
        this.client = Client.create(updateHandler, new CEHA(), new CEHB());
    }

    public TdApi.TdlibParameters getTdlibParameters() {
        return this.params.getParameters();
    }

    public void sendNumber(String number) {
        System.out.println("Send number");
        TdApi.Function query = new TdApi.SetAuthenticationPhoneNumber(number, null);
        send(
                query,
                new TelegramClient.RetryResultHandler(query));
    }

    public void sendCode(String code) {
        TdApi.Function query = new TdApi.CheckAuthenticationCode(code);
        send(
                query,
                new TelegramClient.RetryResultHandler(query));
    }

    public void sendPassword(String password) {
        TdApi.Function query = new TdApi.CheckAuthenticationPassword(password);
        send(
                query,
                new TelegramClient.RetryResultHandler(query));
    }

    public void send(TdApi.Function query, Client.ResultHandler resultHandler) {
        System.out.println("Called  send");
        lock.lock();
        client.send(
                query,
                resultHandler);
        lock.unlock();
    }

    @Override
    public void notify(String message) {
        if(message.equals("authorized")) {
            this.lock.lock();
            _isLoggedIn = true;
            this.lock.unlock();
        } else
        if(message.equals("closed")) {
            this.lock.lock();
            this._isLoggedIn = false;
            this.client = Client.create(updateHandler, new CEHA(), new CEHB());
            this.lock.unlock();
            System.err.println("New Client created");
        }
    }

    public class UpdateHandler implements Client.ResultHandler {
        private final Client.ResultHandler authUpdateHandler;

        public UpdateHandler(Client.ResultHandler authUpdateHandler) {
            this.authUpdateHandler = authUpdateHandler;
        }

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    this.authUpdateHandler.onResult(object);
                    break;
                case TdApi.UpdateUser.CONSTRUCTOR:
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                    lock.lock();
                    users.put(updateUser.user.id, updateUser.user);
                    lock.unlock();
                    break;
                case TdApi.UpdateUserStatus.CONSTRUCTOR:
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    TdApi.User user = users.get(updateUserStatus.userId);
                    synchronized (user) {
                        lock.lock();
                        user.status = updateUserStatus.status;
                        lock.unlock();
                    }
                    break;
                case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                    TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                    lock.lock();
                    basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                    lock.unlock();
                    break;
                case TdApi.UpdateSupergroup.CONSTRUCTOR:
                    TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                    lock.lock();
                    supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                    lock.unlock();
                    break;
                case TdApi.UpdateSecretChat.CONSTRUCTOR:
                    TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                    lock.lock();
                    secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                    lock.unlock();
                    break;
                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                    TdApi.Chat chat = updateNewChat.chat;
                    synchronized (chat) {
                        lock.lock();
                        chats.put(chat.id, chat);

                        TdApi.ChatPosition[] positions = chat.positions;
                        chat.positions = new TdApi.ChatPosition[0];
                        setChatPositions(chat, positions);
                        isModifiedChatList = true;
                        lock.unlock();
                    }
                    break;
                }
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.title = updateChat.title;
                    }
                    break;
                }
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.photo = updateChat.photo;
                    }
                    break;
                }
                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastMessage = updateChat.lastMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPosition.CONSTRUCTOR: {
                    TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
                    if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                        break;
                    }

                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        int i;
                        for (i = 0; i < chat.positions.length; i++) {
                            if (chat.positions[i].list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
                                break;
                            }
                        }
                        TdApi.ChatPosition[] new_positions = new TdApi.ChatPosition[chat.positions.length + (updateChat.position.order == 0 ? 0 : 1) - (i < chat.positions.length ? 1 : 0)];
                        int pos = 0;
                        if (updateChat.position.order != 0) {
                            new_positions[pos++] = updateChat.position;
                        }
                        for (int j = 0; j < chat.positions.length; j++) {
                            if (j != i) {
                                new_positions[pos++] = chat.positions[j];
                            }
                        }
                        assert pos == new_positions.length;

                        setChatPositions(chat, new_positions);
                    }
                    break;
                }
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                    TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.draftMessage = updateChat.draftMessage;
                        setChatPositions(chat, updateChat.positions);
                    }
                    break;
                }
                case TdApi.UpdateChatPermissions.CONSTRUCTOR: {
                    TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.permissions = update.permissions;
                    }
                    break;
                }
                case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
                    break;
                }
                case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                    TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification;
                    }
                    break;
                }
                case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                    TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isMarkedAsUnread = update.isMarkedAsUnread;
                    }
                    break;
                }
                case TdApi.UpdateChatIsBlocked.CONSTRUCTOR: {
                    TdApi.UpdateChatIsBlocked update = (TdApi.UpdateChatIsBlocked) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isBlocked = update.isBlocked;
                    }
                    break;
                }
                case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: {
                    TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.hasScheduledMessages = update.hasScheduledMessages;
                    }
                    break;
                }

                case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                    usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;
                case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                    basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                    break;

                case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                    supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                    break;

                case TdApi.UpdateNewMessage.CONSTRUCTOR:
                    TdApi.UpdateNewMessage newMessage = (TdApi.UpdateNewMessage) object;
                    if(currentChatId == newMessage.message.chatId) {
                        getChatMessageList(currentChatId);
                    }
//                    resetIsModifiedChatList(true);
                    //TODO
                    break;
                case TdApi.UpdateDeleteMessages.CONSTRUCTOR:
                    TdApi.UpdateDeleteMessages deletedMessages = (TdApi.UpdateDeleteMessages) object;
                    if(currentChatId == deletedMessages.chatId) {
                        getChatMessageList(currentChatId);
                    }
//                    resetIsModifiedChatList(true);
                    //TODO
                    break;
                case TdApi.UpdateMessageContent.CONSTRUCTOR:
                    TdApi.UpdateMessageContent updadeMessageContent = (TdApi.UpdateMessageContent) object;
                    if(currentChatId == updadeMessageContent.chatId) {
                        getChatMessageList(currentChatId);
                    }
//                    resetIsModifiedChatList(true);
                    //TODO
                    break;
                case TdApi.ProcessPushNotification.CONSTRUCTOR:
                    System.out.println("Notification");
                    break;
                default:
                    break;
            }
        }
    }
}