package com.lucas.ppgcc.ufjf;


import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

public class ProducerAndReadFile {
    public static void main(String[] args) throws InterruptedException {

        final Logger logger = LoggerFactory.getLogger(ProducerAndReadFile.class);

        String bootstrapServers = "127.0.0.1:9092";
        String topic = "data-input";
        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        String pathFile = "/home/lucas/Documents/data_e-health/heart_rate/46343_heartrate.txt";
        String id = "0";
        if(args.length > 0)
        {
            pathFile = args[0];
            id = args[1];
            System.out.println(pathFile);
            System.out.println("id: "+id);

        }

        ArrayList<Integer> time = new ArrayList<Integer>();
        ArrayList<Integer> bpm = new ArrayList<Integer>();
        //read file
        try {
            File file = new File(pathFile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;


            Boolean firstLine = true;
            int dataAcress = 0;

            while ((st = br.readLine()) != null) {

                if (firstLine) {
                    dataAcress = (int) Float.parseFloat(st.split(",")[0]);
                    dataAcress = dataAcress * (-1);
                    //System.out.println("DA: " + dataAcress);
                    firstLine = false;
                }
                time.add((int) Float.parseFloat(st.split(",")[0]) + dataAcress);
                bpm.add((int) Float.parseFloat(st.split(",")[1]));
                //System.out.println("time: " + time.get(time.size() - 1) + " - bpm: " + bpm.get(bpm.size() - 1));
            }
        }
        catch (Exception e){
            System.out.println("The file cant be found");
        }




        for (int i=0; i<bpm.size(); i++ ) {
            // create a producer record
            ProducerRecord<String, String> record =
                    new ProducerRecord<String, String>(topic, id+";"+i+";"+Integer.toString(bpm.get(i)));


            // send data - asynchronous
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // executes every time a record is successfully sent or an exception is thrown

                    if (e == null) {
                        // the record was successfully sent
                        logger.info("Received new metadata. \n" +
                                "Topic:" + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp());
                    } else {
                        logger.error("Error while producing", e);
                    }
                }
            });


            Thread.sleep(1000);
        }

        // flush data
        producer.flush();
        // flush and close producer
        producer.close();
    }
}
