package pl.kacperk.routerapp.testutil;

import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.message.model.MessageStatus;
import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.model.RouterStatus;

public class TestUtils {

    public static Message getTestMessage(String ip, long time, MessageStatus status) {
        Message testMessage = new Message();
        testMessage.setIpAddress(ip);
        testMessage.setTimestamp(time);
        testMessage.setStatus(status);
        return testMessage;
    }

    public static Router getTestRouter(Long id, String ip, RouterStatus status) {
        Router router = new Router();
        router.setId(id);
        router.setIpAddress(ip);
        router.setStatus(status);
        return router;
    }

}
