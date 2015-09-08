package cloud.cave.ipc;

import cloud.cave.common.CaveException;

/**
 * Created by lalan on 08/09/15.
 */
public class CaveTimeOutException extends CaveException {
    public CaveTimeOutException(String message, Exception originalException) {
        super(message, originalException);
    }

    public CaveTimeOutException(String messages){
        super(messages);
    }
}
