package iot.cloud.platform.thermometer.controller;

import iot.cloud.platform.thermometer.config.Const;
import iot.cloud.platform.thermometer.service.ConfigService;
import iot.cloud.platform.thermometer.service.HttpService;
import iot.cloud.platform.thermometer.vo.DeviceRegisterVo;
import iot.cloud.platform.thermometer.vo.ResMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ConfigController {

    private HttpService httpService = HttpService.RETROFIT.create(HttpService.class);

    @Autowired
    private ConfigService configService;

    /**
     * 配置页面
     *
     * @param model
     * @return
     */
    @RequestMapping("/config")
    public String configIndex(Model model) {
        model.addAttribute(Const.CONFIG_K_IOTID, configService.getV(Const.CONFIG_K_IOTID));
        model.addAttribute(Const.CONFIG_K_USERSECRET, configService.getV(Const.CONFIG_K_USERSECRET));
        model.addAttribute(Const.CONFIG_K_USERID, configService.getV(Const.CONFIG_K_USERID));
        model.addAttribute(Const.CONFIG_K_TOKEN, configService.getV(Const.CONFIG_K_TOKEN));
        model.addAttribute(Const.CONFIG_K_DEVSECRET, configService.getV(Const.CONFIG_K_DEVSECRET));
        model.addAttribute(Const.CONFIG_K_DEVNAME, configService.getV(Const.CONFIG_K_DEVNAME));
        model.addAttribute(Const.CONFIG_K_DEVTYPE, configService.getV(Const.CONFIG_K_DEVTYPE));
        model.addAttribute(Const.CONFIG_K_DESCRIPTION, configService.getV(Const.CONFIG_K_DESCRIPTION));
        model.addAttribute(Const.CONFIG_K_DEVTOPICPOST, configService.getV(Const.CONFIG_K_DEVTOPICPOST));
        model.addAttribute(Const.CONFIG_K_DEVTOPICPOSTREPLY, configService.getV(Const.CONFIG_K_DEVTOPICPOSTREPLY));
        model.addAttribute(Const.CONFIG_K_DEVTOPICRECEIVE, configService.getV(Const.CONFIG_K_DEVTOPICRECEIVE));
        model.addAttribute(Const.CONFIG_K_DEVTOPICRECEIVEREPLY, configService.getV(Const.CONFIG_K_DEVTOPICRECEIVEREPLY));
        return "config";
    }

    /**
     * 保存配置
     *
     * @param map
     * @return
     */
    @RequestMapping("/saveConfig")
    @ResponseBody
    public ResMsg saveConfig(@RequestBody Map<String, String> map) {
        ResMsg msg = new ResMsg();
        if (configService.saveConfigs(map)) {
            msg.setErrcode("0");
            msg.setErrmsg("配置保存成功");
        } else {
            msg.setErrcode("9001");
            msg.setErrmsg("配置保存失败");
        }
        return msg;
    }

    /**
     * 调用物联网云平台 API 获取用户令牌
     *
     * @param map
     * @return
     */
    @RequestMapping("/getToken")
    @ResponseBody
    public ResMsg getToken(@RequestBody Map<String, String> map) {
        ResMsg result = new ResMsg();
        result.setErrcode("8004");
        result.setErrmsg("用户ID，用户密钥不能为空");
        //从请求参数中读取 userId 和 userSecret
        String userId = map.get(Const.CONFIG_K_USERID);
        String userSecret = map.get(Const.CONFIG_K_USERSECRET);
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userSecret)) {
            //TODO:调用物联网云平台的获取用户令牌的接口，获取令牌，并且保存到 config 表。
            Map<String, String> params = new HashMap<>();
            params.put("userId", userId);
            params.put("userSecret", userSecret);
            Call<ResMsg> call = httpService.getUserToken(userId, userSecret);
            try {
                Response<ResMsg> response = call.execute();
                if (response.isSuccessful()) {
                    ResMsg resMsg = response.body();
                    if (resMsg != null) {
                        if ("0".equals(resMsg.getErrcode())) {
                            //保存令牌到数据库
                            Map<String, String> configMap = new HashMap<>();
                            configMap.put(Const.CONFIG_K_USERID, userId);
                            configMap.put(Const.CONFIG_K_USERSECRET, userSecret);
                            Map tokenMap = (Map) resMsg.getData();
                            String token = tokenMap.get("token").toString();
                            configMap.put(Const.CONFIG_K_TOKEN, token);
                            configService.saveConfigs(configMap);
                        }
                        return resMsg;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 调用物联网云平台 API 注册设备，并保存物联网云平台设备IotID和设备密钥
     *
     * @param map
     * @return
     */
    @RequestMapping("/registerDevice")
    @ResponseBody
    public ResMsg registerDevice(@RequestBody Map<String, String> map) {
        ResMsg result = new ResMsg();
        result.setErrcode("8003");
        result.setErrmsg("设备名称，类型，描述，用户令牌不能为空");
        String devName = map.get(Const.CONFIG_K_DEVNAME);
        String devType = map.get(Const.CONFIG_K_DEVTYPE);
        String description = map.get(Const.CONFIG_K_DESCRIPTION);
        String token = map.get(Const.CONFIG_K_TOKEN);
        if (StringUtils.isNotBlank(token)
                && StringUtils.isNotBlank(devName)
                && StringUtils.isNotBlank(devType)
                && StringUtils.isNotBlank(description)
        ) {
            DeviceRegisterVo vo = new DeviceRegisterVo();
            vo.setDescription(description);
            vo.setDevName(devName);
            vo.setDevType(devType);
            //TODO:完善调用注册设备API，获取设备物联网云平台ID和设备密钥，并保存到配置表。
            Call<ResMsg> call = httpService.registerDevice(token, vo);
            try {
                Response<ResMsg> response = call.execute();
                if (response.isSuccessful()) {
                    ResMsg resMsg = response.body();
                    if (resMsg != null) {
                        if ("0".equals(resMsg.getErrcode())) {
                            Map<String, String> registerMap = new HashMap<>();
                            registerMap.put(Const.CONFIG_K_DEVNAME, devName);
                            registerMap.put(Const.CONFIG_K_DEVTYPE, devType);
                            registerMap.put(Const.CONFIG_K_DESCRIPTION, description);
                            registerMap.put(Const.CONFIG_K_TOKEN, token);
                            Map dataMap = (Map) resMsg.getData();
                            String iotId = dataMap.get("iotId").toString();
                            String devSecret = dataMap.get("devSecret").toString();
                            registerMap.put(Const.CONFIG_K_IOTID, iotId);
                            registerMap.put(Const.CONFIG_K_DEVSECRET, devSecret);
                            //MQTT主题配置
                            registerMap.put(Const.CONFIG_K_DEVTOPICPOST, "/iot/cloud/" + iotId + "/post");
                            registerMap.put(Const.CONFIG_K_DEVTOPICPOSTREPLY, "/iot/cloud/" + iotId + "/post_reply");
                            registerMap.put(Const.CONFIG_K_DEVTOPICRECEIVE, "/iot/cloud/" + iotId + "/receive");
                            registerMap.put(Const.CONFIG_K_DEVTOPICRECEIVEREPLY, "/iot/cloud/" + iotId + "/receive_reply");
                            //通过saveConfigs接口保存到数据库
                            configService.saveConfigs(registerMap);
                            resMsg.setData(registerMap);
                        }
                        return resMsg;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }
}
