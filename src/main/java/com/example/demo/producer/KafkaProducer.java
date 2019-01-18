package com.example.demo.producer;

import com.example.demo.domain.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class KafkaProducer {


      @Autowired
      private KafkaTemplate<String,String> kafkaTemplate;

      private Gson gson = new GsonBuilder().create();

    //发送消息方法
      public void send() {
           Message message = new Message();
           message.setId(System.currentTimeMillis());
           message.setMsg(UUID.randomUUID().toString());
           message.setSendTime(new Date());
           //topic-ideal为主题
           kafkaTemplate.send("haihongjia", gson.toJson(message));
       }
}

