package org.seckill.exception;

/**�ظ���ɱ�쳣���������쳣��
 * Created by Administrator on 2016/5/24.
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
