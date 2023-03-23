package pl.kacperk.routerapp.router.util;

import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.RouterStatus;

import java.util.List;

import static pl.kacperk.routerapp.message.model.MessageStatus.GONE;
import static pl.kacperk.routerapp.router.model.RouterStatus.LOST;
import static pl.kacperk.routerapp.router.model.RouterStatus.MALFUNCTION;
import static pl.kacperk.routerapp.router.model.RouterStatus.WORKING;

public class RouterUtils {

    public static RouterStatus findPossibleRouterStatus(List<Message> messages, int messagesRateSec) {
        int numMessages = messages.size();
        for (int i = 0; i < numMessages; i++) {
            Message firstMsg = messages.get(i);
            if (firstMsg.getStatus().equals(GONE)) {
                long firstMsgTimestamp = firstMsg.getTimestamp();
                if (numMessages - i == 1
                        || messages.get(i + 1).getTimestamp() - firstMsgTimestamp > messagesRateSec) {
                    return LOST;
                }
                if (numMessages - i >= 6) {
                    if (messages.get(i + 5).getTimestamp() - firstMsgTimestamp <= messagesRateSec) {
                        return MALFUNCTION;
                    }
                }
            }
        }
        return WORKING;
    }

}
