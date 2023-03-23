package pl.kacperk.routerapp.message.util;

import pl.kacperk.routerapp.message.model.Message;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MessageUtils {
    private static final int MESSAGES_MAX_DELAY_SEC = 30;

    private static void removeDelayedMessages(List<Message> messages) {
        ListIterator<Message> iterator = messages.listIterator();
        Message previousMsg = iterator.next();
        while (iterator.hasNext()) {
            Message currentMsg = iterator.next();
            if (previousMsg.getTimestamp() - currentMsg.getTimestamp() > MESSAGES_MAX_DELAY_SEC) {
                iterator.previous();
                iterator.previous();
                iterator.remove();
            }
            previousMsg = currentMsg;
        }
    }

    public static Map<String, List<Message>> prepareMapCopy(
            Map<String, List<Message>> messagesMapCopy
    ) {
        messagesMapCopy.values()
                .forEach(MessageUtils::removeDelayedMessages);
        messagesMapCopy.values()
                .forEach(messages -> messages.sort(Comparator.comparing(Message::getTimestamp)));
        return messagesMapCopy;
    }

}
