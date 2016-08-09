package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnums;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/5/26.
 */
@Controller
@RequestMapping("/seckill")  // url:  /ģ��/��Դ/{id}ϸ��
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model){
        //��ȡ�б�ҳ
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list",list);

        //list.jsp + model = ModelAndView
        return  "list";  /*   /WEB-INF/jsp/"list".jsp  */
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") int seckillId,Model model){
        if(String.valueOf(seckillId)==null){   // int --> String
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if(seckill==null){
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }

    //ajax json
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody  //SpringMVC�ὫSeckillResult<Exposer>��װΪjson
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") int seckillId){

        SeckillResult<Exposer> result;

        try {
            Exposer exposer =  seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true,exposer);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            result = new SeckillResult<Exposer>(false,e.getMessage());
        }

        return result;
    }

    //dto��web���service������ݴ���
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") int seckillId,
                                                   @CookieValue(value = "killPhone",required = false)String phone,
                                                   @PathVariable("md5") String md5){
        if(phone==null){
            return  new SeckillResult<SeckillExecution>(false,"δע��");
        }

        SeckillResult<SeckillExecution> result;

        try{
            SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId,phone,md5);
            return new SeckillResult<SeckillExecution>(true,seckillExecution);
        }
        catch (RepeatKillException e){
            //����executeSeckill�����׳����ظ���ɱ�쳣���������쳣�������͵�ǰ��
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStateEnums.REPEAT_KILL);
            return  new SeckillResult<SeckillExecution>(true,seckillExecution);
        }
        catch (SeckillCloseException e){
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStateEnums.END);
            return  new SeckillResult<SeckillExecution>(true,seckillExecution);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            SeckillExecution seckillExecution = new SeckillExecution(seckillId, SeckillStateEnums.INNER_ERROR);
            return  new SeckillResult<SeckillExecution>(true,seckillExecution);
        }

    }

    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now = new Date();
        return new SeckillResult(true,now.getTime());
    }
}
