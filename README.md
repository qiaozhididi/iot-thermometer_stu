# 物联网云平台设计与开发实训

<div style="text-align: center">最近一次更新：2023年4月19日（实验已全部完成）</div>

#### 介绍

#### iot-thermometer设备端代码

项目文档：https://heis.gitee.io/docs/iot-cloud-training/iot-cloud-training-02/

该项目与物联网云平台进行交互，实现以下功能：
<ol>
<li>实验2.1 - 构建设备端项目表情温度计</li>
<li>实验2.2 - 完成表情温度计温度更新功能</li>
<li>实验3.3 - 设备端实现调用物联网云平台API获取用户令牌</li>
<li>实验4.2 - 表情温度计项目调用开放API实现注册设备</li>
<li>实验5.1 - 表情温度计项目新增 MQTT 调用接口——修改表情</li>
<li>实验6.2 - 表情温度计项目调用开放API实现保存新增设备消息到物联网云平台</li>
</ol>

如需查看云平台代码请跳转：https://gitee.com/qiaozhididi/iot-cloud_stu

#### 软件架构

使用了一下框架

- SpringBoot
- MyBatis
- Flyway
- Thymeleaf

#### 安装教程

1. 安装 Maven、IDEA、MariaDB、Mosquitto
2. 使用 IDEA 导入项目
3. 运行`V000_CREATE_DB_AND_USER.sql`创建数据库和数据库用户
4. 运行 Flyway 命令`mvn flyway:migrate`开始实现数据库的迁移，构建数据库
5. 启动Mosquitto，运行在默认1883端口，账号密码都设置为iotweb。
6. 构建项目，并启动`IoTThermometerApplication`，访问`http://localhost:8100`查看首页。

#### 需要完成的功能说明

1. 【首页】__温度更新功能__，模拟温度检测传感器，刷新当前温度显示和表情显示，表情的显示是根据数据库的温度范围进行显示。
2. 【配置页】__获取物联网云平台的用户令牌功能__，配置了用户ID和用户密钥以后可以获取用户令牌并保存在设置内。
3. 【配置页】__注册设备功能__，根据物联网云平台的API，实现设备注册功能，获得设备的物联网云平台ID和设备密钥。
4. 【首页】__发送温度消息功能__ 的温度和时间到物联网云平台进行保存。
5. 【MQTT客户端】__通过MQTT消息修改表情的功能__ ，通过向主题`/iot/cloud/thermometer001/receive`发送类似以下的消息

```json
{
  "eventId": "0759",
  "eventName": "updateEmojiFace",
  "eventTime": 1647155485187,
  "params": [
    "burn",
    "_(´□`」 ∠)_"
  ]
}
```

实现对应表情的更新，并且向主题`/iot/cloud/thermometer001/receive_reply`回复是否更新成功的回复消息。

注意修改`pom.xml`和`application.yml`中的数据库连接端口。（本人使用的3308）