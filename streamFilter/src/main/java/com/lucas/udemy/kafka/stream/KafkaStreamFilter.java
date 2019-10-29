package com.lucas.udemy.kafka.stream;


import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;



public class KafkaStreamFilter {

    public boolean streamFilter(Object value){
        try {
            String string_value = value.toString();
            Integer v = Integer.parseInt(string_value.split(";")[2]);
            System.out.println(v);
            if (v > 90 || v < 40)
                return true;
            return false;
        }
        catch (Exception e){
            return false;
        }
    }

    public Topology createTopology(){
        StreamsBuilder builder = new StreamsBuilder();
        //application a filter
        KStream<Object, Object> kStream = builder.stream("data-input").filter((k, v) -> streamFilter(v));
        //sending to topic
        kStream.to("data-output");

        return builder.build();
    }


    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "data-output");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        KafkaStreamFilter filterStream = new KafkaStreamFilter();

        KafkaStreams streams = new KafkaStreams(filterStream.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        // Update:
        // print the topology every 10 seconds for learning purposes
        while(true){
            streams.localThreadsMetadata().forEach(data -> System.out.println(data));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }


    }
}
