package iot.cloud.platform.thermometer.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iot.cloud.platform.thermometer.controller.EmojiController;
import iot.cloud.platform.thermometer.entity.EmojiEntity;
import iot.cloud.platform.thermometer.utils.ExcptUtil;
import iot.cloud.platform.thermometer.vo.MqttMsg;
import iot.cloud.platform.thermometer.vo.ResMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * mqtt回调处理类
 */

@Component
@Slf4j
public class MqttConsumerCallback implements MqttCallbackExtended {

    @Autowired
    private MqttClient client;
    @Autowired
    private MqttConnectOptions options;

    @Autowired
    @Qualifier("mqttSubTopics")
    private Map<String,Integer> mqttSubTopics;

    @Autowired
    @Qualifier("mqttTopicReplyMap")
    private Map<String,String> mqttTopicReplyMap;

    @Autowired
    private EmojiController emojiController;
    /**
     * 断开重连
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.info("MQTT连接断开",cause);
        int tryTimes=3,count=0;
        while(!client.isConnected()) {
            try {
                client.reconnect();
                count=0;
            } catch (Exception e) {
                count++;
                log.error(ExcptUtil.filterStack(e));
            }
            log.info("尝试重新连接"+count+"次");
            if(count>=tryTimes){
                break;
            }
        }

    }

    /**
     * 消息处理
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String msg = new String(message.getPayload());
            log.info("收到主题[" + topic + "]消息 ->" + msg);
            ObjectMapper objMapper=new ObjectMapper();
            MqttMsg<HashMap> resvMsg=null;
            try {
                //ObjectMapper 是 Jackson包用于解析 Json 为java 对象
                resvMsg = objMapper.readValue(msg, MqttMsg.class);
            } catch (JsonProcessingException e) {
                log.error(ExcptUtil.filterStack(e));
            }

            HashMap data=resvMsg.getData();
            ResMsg returnVal=null;

            //TODO:完成此处代码实现更新表情
            //回复消息
            MqttMsg<ResMsg> replyMsg=new MqttMsg();
            replyMsg.setEventId(resvMsg.getEventId());
            replyMsg.setEventName(resvMsg.getEventName());
            replyMsg.setEventTime(resvMsg.getEventTime());
            replyMsg.setData(returnVal);
            String receiveReplyMsg="{}";
            try {
                //把replyMsg对象转换为 JSON 消息
                receiveReplyMsg=objMapper.writeValueAsString(replyMsg);
                log.info("回复MQTT消息："+receiveReplyMsg);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(),e);
            }
            String sendTopic=mqttTopicReplyMap.get(topic);
            if(StringUtils.isNotBlank(sendTopic)) {
                log.info("send topic :{} -> {}", sendTopic,receiveReplyMsg);
                client.publish(sendTopic, new MqttMessage(receiveReplyMsg.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            log.error(ExcptUtil.filterStack(e));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void connectComplete(boolean b, String s) {
        mqttSubTopics.forEach((k,v) ->{
            try {
                client.subscribe(k,v);
                log.info("订阅主题:"+k);
            } catch (MqttException e) {
                log.error(ExcptUtil.filterStack(e));
            }
        });
    }
}