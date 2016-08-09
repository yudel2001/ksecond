package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnums;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/5/25.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //ע��Service����,�Զ�װ��,����Ҫ�ֶ��½���Ӧʵ��
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5��ֵ�ַ��������ڻ���MD5
    private final String slat = "asd$%^$156120#BbK0-%^%*&!&*fef~{}@##VJ*{))&@@@@#";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(int seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(int seckillId) {
        //�Ż�����
        //1.����redis,�������ݿ����ѹ��
        Seckill seckill = redisDao.getSeckill(seckillId);

        if(seckill==null){
            //2.�������ݿ�
            System.out.println("assess database");
            seckill = seckillDao.queryById(seckillId);
            if(seckill==null){
                return new Exposer(false,seckillId);
            }
            else {
                //3.����redis
                System.out.println("put into redis");
                redisDao.putSeckill(seckill);
            }
        }

      /*  Seckill seckill = seckillDao.queryById(seckillId);
        if(seckill==null){
            return new Exposer(false,seckillId);
        }*/

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //ϵͳ��ǰʱ��
        Date nowTime = new Date();
        if(nowTime.getTime()<startTime.getTime()
                || nowTime.getTime()>endTime.getTime()){
            return new Exposer(startTime.getTime(),endTime.getTime(),nowTime.getTime(),false,seckillId);
        }
        //ת���ض��ַ����Ĺ��̣�������
        String md5 = getMD5(seckillId);//
        return new Exposer(md5,true,seckillId);

    }

    //���ɲ�����һ��md5
    private  String getMD5(int seckillId){

        String base = seckillId+"/"+slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }


    @Transactional
    /**
     * ʹ��ע���������
     */
    public SeckillExecution executeSeckill(int seckillId, String userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if(md5==null|| !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //ִ����ɱ�߼�; �����+��¼������ϸ
        Timestamp nowTime = new Timestamp(System.currentTimeMillis());
        try {
            int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
            if(updateCount<=0){
                //������̫�ߣ��п����ڵ��м�����ʱ����û����,������ɱʱ��������ǰ���Ѿ���֤��
                throw new SeckillCloseException("seckill is closed");
            }
            else{

                int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone,nowTime);
                //Ψһ��seckillId,userPhone������������
                if(insertCount<=0){
                    //�ظ���ɱ
                    throw new RepeatKillException("seckill repeated");
                }
                else {
                    //��ɱ�ɹ�
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnums.SUCCESS,successKilled);  //ö��
                }
            }
        }
        catch (SeckillCloseException e1){
            throw e1;  //�׳��쳣
        }
        catch (RepeatKillException e2){
            throw e2;   //�׳��쳣
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            //���б������쳣ת��Ϊ�������쳣    �Ա�rollback�ع�
            throw new SeckillException("seckill inner error:"+e.getMessage());    //�׳��쳣
        }
    }
}
