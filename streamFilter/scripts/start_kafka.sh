#quickstart https://kafka.apache.org/quickstart
#start zookeeper
cd ~/kafka_2.12-2.3.0
gnome-terminal -x sh -c 'bin/zookeeper-server-start.sh ~/kafka_2.12-2.3.0/config/zookeeper.properties'

sleep 4
#start kafka server
gnome-terminal -x sh -c 'bin/kafka-server-start.sh ~/kafka_2.12-2.3.0/config/server.properties'


sleep 10

#delete old topic
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic data-input --delete
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic data-output --delete

#create a new topic
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic data-input --create --partitions 3 --replication-factor 1
kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic data-output --create --partitions 3 --replication-factor 1

#start mongoDB
mongod --dbpath /var/lib/mongo --logpath /var/log/mongodb/mongod.log --fork

#start shell
mongo

