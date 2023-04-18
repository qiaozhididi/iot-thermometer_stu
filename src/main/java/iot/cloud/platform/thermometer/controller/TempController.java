package iot.cloud.platform.thermometer.controller;

import iot.cloud.platform.thermometer.config.Const;
import iot.cloud.platform.thermometer.service.ConfigService;
import iot.cloud.platform.thermometer.service.HttpService;
import iot.cloud.platform.thermometer.service.TempEmojiService;
import iot.cloud.platform.thermometer.vo.DeviceMsgVo;
import iot.cloud.platform.thermometer.vo.ResMsg;
import iot.cloud.platform.thermometer.vo.TempEmojiVo;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TempController {

    private HttpService httpService = HttpService.RETROFIT.create(HttpService.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private TempEmojiService tempEmojiServiceImpl;

    /**
     * 展示温度和表情
     *
     * @param temp 温度
     * @return
     */
    @RequestMapping("/showTempEmoji")
    @ResponseBody
    public ResMsg showTempEmoji(@RequestParam("temp") String temp) {
        ResMsg result = new ResMsg();
        boolean isValidInt = false;
        if (NumberUtils.isNumber(temp)) {
            Integer itemp = null;
            try {
                itemp = Integer.valueOf(temp);
                isValidInt = true;
            } catch (NumberFormatException e) {
            }
            if (isValidInt) { //如果温度的值是合法的
                //TODO:请修改完善这里的代码，获取温度和表情。
                //调用TempEmojiService.getFaceByTemp方法，获取温度和表情。
                //返回温度和表情。
                result.setErrcode("0");
                result.setErrmsg("更新温度成功");
                String face = tempEmojiServiceImpl.getFaceByTemp(itemp);
                TempEmojiVo tempEmojiVo = new TempEmojiVo();
                tempEmojiVo.setTemp(itemp);
                tempEmojiVo.setEmoji(face);
                result.setData(tempEmojiVo);
            } else {
                result.setErrmsg("温度值不合法");
            }
        }
        return result;
    }

    /**
     * 发送设备消息到物联网云平台并保存
     *
     * @param temp 当前温度
     * @return
     */
    @RequestMapping("/sendDeviceMsg")
    @ResponseBody
    public ResMsg sendDeviceMsg(@RequestParam("temp") String temp) {
        ResMsg result = new ResMsg();
        Map<String, Object> map = new HashMap<>();
        map.put("temp", Integer.valueOf(temp));
        map.put("time", new Date());
        DeviceMsgVo msg = new DeviceMsgVo();
        msg.setMsg(map);
        msg.setTag("temp");
        String iotId = configService.getV(Const.CONFIG_K_IOTID);
        String devSecret = configService.getV(Const.CONFIG_K_DEVSECRET);
        //TODO:物联网云平台ID和设备密钥不能为空
        msg.setIotId(iotId);
        if (iotId != null && devSecret != null) {
            //调用发送设备消息的开放API(HttpService.sendDeviceMsg)，保存设备消息到物联网云平台
            //返回保存消息结果
            Call<ResMsg> call = httpService.sendDeviceMsg(iotId, devSecret, msg);
            try {
                Response<ResMsg> response = call.execute();
                if (response.isSuccessful()) {
                    result.setData(temp);
                    result.setErrcode("0");
                    result.setErrmsg("发送消息成功");
                } else {
                    result.setErrmsg("发送消息失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result.setErrmsg("iotId和设备密钥不能为空");
        }
        return result;
    }

}
