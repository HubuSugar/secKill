package com.hubu.seckill.controller;

import com.hubu.seckill.dto.Exposer;
import com.hubu.seckill.dto.SeckillExecution;
import com.hubu.seckill.dto.SeckillResult;
import com.hubu.seckill.enums.SeckillStatEnum;
import com.hubu.seckill.exception.RepeatKillException;
import com.hubu.seckill.exception.SeckillCloseException;
import com.hubu.seckill.model.Seckill;
import com.hubu.seckill.service.SeckillServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 秒杀API设计的五个步骤
 * 基于Restful规则   /模块/资源/{ID细分}/资源状态、资源结果
 * 1.GET     /seckill/list    初始状态展示所有的秒杀商品
 * 2.GET     /seckill/{seckillId}/detail    进入秒杀详情页面
 * 3.GET    /seckill/time/now   如果秒杀时间还没到。获取系统时间
 * 4.POST    /seckill/{seckillId}/exposer    暴露秒杀地址
 * 5.POST    /seckill/{seckill}/{md5}/execution    将url加密
 */

@Controller
//@RequestMapping("/seckill")   //url:模块/资源/{id}细分
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SeckillServiceImpl seckillServiceImpl;

    @RequestMapping(path = {"/index"},method = {RequestMethod.GET})
    public String index(Model model){

        model.addAttribute("hello","hello thymeleaf");
        return "index";
    }


    @RequestMapping(path = {"/list","/"},method = {RequestMethod.GET})
    public String list(Model model){
             List<Seckill>  seckillList = seckillServiceImpl.getSeckillList();
             model.addAttribute("seckillList",seckillList);
             //model + list = ModelAndView
            return "list";
    }

    /**
     * 根据id取详情页
     * 基于Restful
     * @param seckillId
     * @param model
     * @return
     */
    //@RequestMapping(value="/{seckillId}/detail",method=RequestMethod.GET)
    @RequestMapping(path = {"/{seckillId}/detail"},method = {RequestMethod.GET})
    public String detail(@PathVariable("seckillId")Long seckillId,Model model){
        if(seckillId == null){
            return "redirect:/list";
        }
        Seckill seckill = seckillServiceImpl.getById(seckillId);
        if(seckill == null){
            return "forward:/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    /**
     * 获取秒杀地址，json格式
     * @param seckillId
     * @return
     */
    //ajax json
    @RequestMapping(value="/{seckillId}/exposer",
            method={RequestMethod.POST},
            produces={"application/json;charset=UTF-8"})
    public @ResponseBody
    SeckillResult<Exposer> exposer(@PathVariable("seckillId")Long seckillId){
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillServiceImpl.exportSeckillUrl(seckillId);
            result =  new SeckillResult<Exposer>(true,exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;

    }

    /**
     * 执行秒杀，json
     * @param seckillId
     * @param md5
     * @param phone
     * @return
     */
    @RequestMapping(value="/{seckillId}/{md5}/execution",
           method=RequestMethod.POST,
            produces={"application/json;charset=UTF-8"})
    public @ResponseBody SeckillResult<SeckillExecution> execute(@PathVariable("seckillId")Long seckillId,
                                                                 @PathVariable("md5")String md5,
                                                                 @CookieValue(value = "killPhone",required = false)Long phone){

        if(phone == null){
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }

        SeckillResult<SeckillExecution> result;
        try {
            SeckillExecution execution = seckillServiceImpl.executeSeckillProcedure(seckillId, phone, md5);
            result = new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillCloseException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            result = new SeckillResult<SeckillExecution>(true, execution);
        } catch (RepeatKillException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            result = new SeckillResult<SeckillExecution>(true, execution);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            result = new SeckillResult<SeckillExecution>(true, execution);
        }

        return result;
    }

    /**
     * 获取系统时间，json
     * @return
     */
    @RequestMapping(value="/time/now",
            method=RequestMethod.GET
    )
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now  = new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }



}
