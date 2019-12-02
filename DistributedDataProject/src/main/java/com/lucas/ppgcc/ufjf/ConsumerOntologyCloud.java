package com.lucas.ppgcc.ufjf;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import openllet.owlapi.OpenlletReasonerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bson.Document;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerOntologyCloud {
    public static void main(String[] args) {
        new ConsumerOntologyCloud().run();
    }

    private ConsumerOntologyCloud() {

    }

    private void run() {
        Logger logger = LoggerFactory.getLogger(ConsumerOntologyCloud.class.getName());

        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "stream-filter-results";
        String topic = "data-output";

        // latch for dealing with multiple threads
        CountDownLatch latch = new CountDownLatch(1);

        // create the consumer runnable
        logger.info("Creating the consumer thread");
        Runnable myConsumerRunnable = new ConsumerRunnable(
                bootstrapServers,
                groupId,
                topic,
                latch
        );

        // start the thread
        Thread myThread = new Thread(myConsumerRunnable);
        myThread.start();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook");
            ((ConsumerRunnable) myConsumerRunnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited");
        }

        ));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable {

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class.getName());

        public ConsumerRunnable(String bootstrapServers,
                                String groupId,
                                String topic,
                                CountDownLatch latch) {
            this.latch = latch;

            // create consumer configs
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            // create consumer
            consumer = new KafkaConsumer<String, String>(properties);
            // subscribe consumer to our topic(s)
            consumer.subscribe(Arrays.asList(topic));
        }

        @Override
        public void run() {
            // poll for new data

            //ONTOLOGY CONFIGURATIONS
            try {
                ontologyFunctions of = new ontologyFunctions();
                of.manager = OWLManager.createOWLOntologyManager();
                of.dataFactory = of.manager.getOWLDataFactory();
                of.renderer = new DLSyntaxObjectRenderer();
                of.ontology = of.createOntology();
                String pathIRI = "http://www.ontology.com/teste/teste.owl";
                of.iri = IRI.create(pathIRI);
                of.prefixManager = new DefaultPrefixManager(pathIRI+"#");
                of.prefixDocumentFormat = of.manager.getOntologyFormat(of.ontology).asPrefixOWLDocumentFormat();
                of.prefixDocumentFormat.setDefaultPrefix(pathIRI + "#");
                //String pizzaOntologyURL = "https://protege.stanford.edu/ontologies/pizza/pizza.owl";
                of.ontology = of.loadOntologyFile("teste.owl");
                of.openlletReasonerFactory = new OpenlletReasonerFactory();
                of.openlletReasoner = of.openlletReasonerFactory.createReasoner(of.ontology, new SimpleConfiguration());

                //MYSQL CONFIGURATIONS
                String driverName = "com.mysql.jdbc.Driver";
                Class.forName(driverName); // here is the ClassNotFoundException

                String serverName = "db4free.net";
                String mydatabase = "kafkastreamfilte";
                String url = "jdbc:mysql://" + serverName + "/" + mydatabase;

                String username = "testekafkastream";
                String password = "password";

                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://"+serverName+":3306/"+mydatabase, username, password);

                Statement statement = conn.createStatement();

                if (conn != null) {
                    System.out.println("Connected to the database!");
                } else {
                    System.out.println("Failed to make connection!");
                }



                Statement stmt = null;

                while (true) {
                    ConsumerRecords<String, String> records =
                            consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0

                    for (ConsumerRecord<String, String> record : records) {
                        //logger.info("Key: " + record.key() + ", Value: " + record.value());
                        //logger.info("Partition: " + record.partition() + ", Offset:" + record.offset());

                        String id = record.value().split(";")[0];
                        OWLNamedIndividual individual = of.dataFactory.getOWLNamedIndividual(id,of.prefixManager);
                        OWLDataProperty dataProperty = of.dataFactory.getOWLDataProperty("haveBPM",of.prefixManager);
                        OWLDatatype datatypeString = of.dataFactory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
                        OWLDatatype datatypeInt = of.dataFactory.getOWLDatatype(OWL2Datatype.XSD_INT.getIRI());
                        if(individual == null)
                            individual = of.createNamedIndividual(id);
                        ArrayList<OWLLiteral> list = of.getLiteralsDataPropiertyAndIndividual(dataProperty, individual);
                        if (list.size() > 0) {
                            of.removeLiteral(dataProperty, individual, list.get(0));
                        }
                        of.createLiteral(dataProperty,individual,record.value().split(";")[2],datatypeInt);
                        of.openlletReasoner.prepareReasoner();
                        of.openlletReasoner.refresh();
                        List<OWLLiteral> ls = of.getDataProperty("saveData", id);
                        List<OWLLiteral> ls1 = of.getDataProperty("haveBPM", id);
                        if(ls.size()>0 && ls1.size()>0) {
                            System.out.println("id:"+id+" save? " + ls.get(0).getLiteral() + "  BPM: " + ls1.get(0).getLiteral());
                            if(ls.get(0).getLiteral() == "true"){
                                String SQL_INSERT = "INSERT INTO `Patient` (`name`, `bpm`) VALUES ('"+id+"', '"+ls1.get(0).getLiteral()+"');";
                                statement.executeUpdate(SQL_INSERT);
                            }
                        }

                    }
                }
            } catch (WakeupException | ClassNotFoundException | SQLException e) {
                logger.info("Received shutdown signal!");
            } finally {
                consumer.close();
                // tell our main code we're done with the consumer
                latch.countDown();
            }
        }

        public void shutdown() {
            // the wakeup() method is a special method to interrupt consumer.poll()
            // it will throw the exception WakeUpException
            consumer.wakeup();
        }
    }
}