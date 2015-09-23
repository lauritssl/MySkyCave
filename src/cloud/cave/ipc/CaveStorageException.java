package cloud.cave.ipc;

import cloud.cave.common.CaveException;

/**
 * Created by lalan on 08/09/15.
 */
public class CaveStorageException extends CaveException {
    public CaveStorageException(String message, Exception originalException) {
        super(message, originalException);
    }

    public CaveStorageException(String messages){
        super(messages);
    }
}
