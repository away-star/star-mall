
package star.xingxing.mall.exception;

public class StarMallException extends RuntimeException {

    public StarMallException() {
    }

    public StarMallException(String message) {
        super(message);
    }

    /**
     * 丢出一个异常
     *
     * @param message
     */
    public static void fail(String message) {
        throw new StarMallException(message);
    }

}
