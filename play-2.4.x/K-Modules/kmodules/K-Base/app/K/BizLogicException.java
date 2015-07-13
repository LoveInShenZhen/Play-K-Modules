package K;

/**
 * Created by kk on 14-6-11.
 */
public class BizLogicException extends RuntimeException {

    public int ErrCode;

    public BizLogicException(String msg) {
        super(msg);
        this.ErrCode = -1;
    }

    public BizLogicException(int errCode, String msg) {
        super(msg);
        this.ErrCode = errCode;
    }

    public BizLogicException(String format, Object... args) {
        super(String.format(format, args));
        this.ErrCode = -1;
    }
}
