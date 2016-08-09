package org.seckill.dao;


import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by Administrator on 2016/5/23.
 */
public interface SuccessKilledDao {

    /**
     * ���빺����ϸ�����������ɹ����ظ�
     * @param seckillId
     * @param userPhone
     * @return
     */
    int insertSuccessKilled(@Param("seckillId") int seckillId,@Param("userPhone") String userPhone,@Param("createTime") Timestamp createTime);

    /**
     * ����id��ѯSuccessKilled��Я����ɱ��Ʒ����ʵ��
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") int seckillId,@Param("userPhone") String userPhone);
}
