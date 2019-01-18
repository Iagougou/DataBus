package com.example.demo.controller;


import com.example.demo.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("kafka")
public class KafkaSendTest {


    @Autowired
    private KafkaProducer kafkaProducer;

    @GetMapping("send")
    public void kafkaProducer(){
        this.kafkaProducer.send();
    }

}
