package com.example.demo.consumer;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = {"haihongjia"})
    public void consumer(ConsumerRecord<?, ?> record){
       Optional<?> kafkaMessage = Optional.ofNullable(record.value());
       if (kafkaMessage.isPresent()) {
           Object message = kafkaMessage.get();
           System.out.println("----------------- record =" + record);
           System.out.println("------------------ message =" + message);
       }
   }


}
