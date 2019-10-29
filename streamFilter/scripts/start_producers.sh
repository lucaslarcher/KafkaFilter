#data of consumer 1
gnome-terminal -x sh -c 'java -jar Producer.jar ~/Documents/data_e-health/heart_rate/46343_heartrate.txt 46343'

#data of consumer 2
gnome-terminal -x sh -c 'java -jar Producer.jar ~/Documents/data_e-health/heart_rate/759667_heartrate.txt 759667'

#data of consumer 3
gnome-terminal -x sh -c 'java -jar Producer.jar ~/Documents/data_e-health/heart_rate/781756_heartrate.txt 781756'
