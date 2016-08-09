/**
 * Created by Administrator on 2016/5/27.
 */
//�����Ҫ�����߼�js����
//javascript ģ�黯������ά���޸�
var seckill={
    //��װ��ɱ��ص�ajax��url
    URL:{
        now:function(){
            return '../../seckill/time/now';
        },
        exposer:function(seckillId){
            return "../../seckill/"+seckillId+"/exposer";
        },
        execution:function(seckillId,md5){
            return "../../seckill/"+seckillId+"/"+md5+"/execution";
          //  return "/"+md5+"/execution";
        }

    },
    //��֤�ֻ���
    validatePhone:function(phone){
        if(phone&&phone.length==11&&!isNaN(phone)){
            return true;
        }
        else{
            return false;
        }
    },
    handleSeckill:function(seckillId,node){
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">��ɱ������</button>');
        $.post(seckill.URL.exposer(seckillId),{},function(result){
            //�ڻص�������ִ�н�������
            if(result&&result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //������ɱ
                    //��ȡ��ɱ��ַ
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId,md5);
                    console.log("killUrl="+killUrl);
                    //��һ�ε���¼�
                    $("#killBtn").one('click',function(){
                        //�Ƚ��ð�ť
                        $(this).addClass('disabled');
                        //������ɱ����ִ����ɱ
                        $.post(killUrl,{},function(result){
                            console.log('result='+result['success']);
                            if(result&&result['success']){
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //��ʾ��ɱ���
                                node.html('<span class="label label-success">'+stateInfo+'</span>');
                            }
                        });
                    });
                    node.show();
                }
                else{
                    //δ������ɱ������ƫ�
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end =exposer['end'];
                    seckill.countdown(seckillId,now,start,end);
                }
            }
            else{
                console.log("result="+result);
            }

        });
    },
    countdown:function(seckillId,nowTime,startTime,endTime){
        var seckillBox = $("#seckill-box");
        if(nowTime>endTime){
            //��ɱ����
            seckillBox.html('��ɱ������');
        }
        else if(nowTime<startTime){
            //��ɱδ������Ҫ��ʱ����ʱ�¼���
            var killTime = new Date(startTime+1000);
            seckillBox.countdown(killTime,function(event){
                //ʱ���ʽ
                var format = event.strftime('��ɱ����ʱ; %D�� %Hʱ %M�� %S��');
                seckillBox.html(format);
                /*ʱ����ɺ�ص��¼�*/
            }).on('finish.countdown',function(){
                //��ȡ��ɱ��ַ��ִ����ɱ
                seckill.handleSeckill(seckillId,seckillBox);
            });
        }
        else{
            //��ɱ�ѿ�����ִ����ɱ
            seckill.handleSeckill(seckillId,seckillBox);
        }
    },
    //����ҳ��ɱ�߼�
    detail:{
        //����ҳ��ʼ��
        init:function(params){
            //�ֻ���֤�͵�½����ʱ����
            //��cookie�в����ֻ���
            var killPhone = $.cookie('killPhone');

            //��֤�ֻ���
            if(!seckill.validatePhone(killPhone)){
                //��phone
                var killPhoneModal = $("#killPhoneModal");
                killPhoneModal.modal({
                    show:true,  //��ʾ������
                    backdrop:'static', //��ֹλ�ùر�
                    keyboard:false //�رռ����¼�
                });

                $("#killPhoneBtn").click(function(){
                    var inputPhone = $("#killPhoneKey").val();
                    console.log("inputPhone="+inputPhone);//TODO
                    if(seckill.validatePhone(inputPhone)){
                        //�绰д��cookie
                        $.cookie('killPhone',inputPhone,{expires:7,path:'/seckill'});
                        //ˢ��ҳ��
                        window.location.reload();
                    }
                    else{
                        $("#killPhoneMessage").hide().html('<label class="label label-danger">����绰����</label>').show(300);
                    }
                });
            }
            //�Ѿ���¼����ʱ����
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(),{}, function (result) {
                if(result&&result['success']){
                    var nowTime = result['data'];
                    //ʱ���ж�
                    seckill.countdown(seckillId,nowTime,startTime,endTime);
                }
                else{
                    console.log("result="+result);
                }
            });
        }
    }
}

