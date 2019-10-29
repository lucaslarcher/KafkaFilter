package com.lucas.udemy.kafka.stream;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SaveOnMongoDB {
    public static void main(String[] args) {
        MongoClient mongo = new MongoClient("localhost",27017);
        System.out.println("Connected");

        //create connection
        MongoClient mongoClient = new MongoClient();
        //acess database
        MongoDatabase dataBase = mongoClient.getDatabase("filter");
        //acess collection
        MongoCollection<Document> mongoCollection = dataBase.getCollection("data");
        //insert with insertone()
        mongoCollection.insertOne(
                new Document("id","1")
                        .append("time",1)
                        .append("bpm",50)
                        );
    }
}
