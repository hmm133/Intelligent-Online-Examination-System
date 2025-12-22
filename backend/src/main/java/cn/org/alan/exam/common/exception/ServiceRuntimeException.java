package cn.org.alan.exam.common.exception;


public class ServiceRuntimeException extends RuntimeException {
    /**
     * 自定义服务异常类构造器
     *
     * @param msg
     */
    public ServiceRuntimeException(String msg) {
        super(msg);
    }
}
