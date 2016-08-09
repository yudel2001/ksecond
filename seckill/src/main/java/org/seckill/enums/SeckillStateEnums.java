package org.seckill.enums;

/**ʹ��ö�ٱ�ʾ���������ֶ�
 * Created by Administrator on 2016/5/25.
 */
public enum  SeckillStateEnums {
    SUCCESS(1,"Seckill success!"),
    END(0,"Seckill ended!"),
    REPEAT_KILL(-1,"Seckill repeated!"),
    INNER_ERROR(-2,"System error!"),   //ϵͳ�쳣
    DATA_REWRITE(-3,"Data tampered!");   //���ݴ۸�

    private int state;

    private String stateInfo;

    SeckillStateEnums(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static SeckillStateEnums stateOf(int index){
        for(SeckillStateEnums state:values()){
            if(state.getState()==index){
                return state;
            }
        }
        return null;
    }
}
