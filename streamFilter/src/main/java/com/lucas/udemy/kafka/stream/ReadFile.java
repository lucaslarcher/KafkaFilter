package com.lucas.udemy.kafka.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadFile {


        public static void main(String[] args)throws Exception {

            String pathFile = "/home/lucas/Documents/data_e-health/heart_rate/46343_heartrate.txt";
            if(args.length > 0)
            {
                pathFile = args[0];
                System.out.println(pathFile);
            }

            File file = new File(pathFile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;

            ArrayList<Integer> time = new ArrayList<Integer>();
            ArrayList<Integer> bpm = new ArrayList<Integer>();

            Boolean firstLine = true;
            int dataAcress = 0;

            while ((st = br.readLine()) != null) {

                if(firstLine){
                    dataAcress = (int)Float.parseFloat(st.split(",")[0]);
                    dataAcress = dataAcress*(-1);
                    System.out.println("DA: "+dataAcress);
                    firstLine = false;
                }
                time.add((int)Float.parseFloat(st.split(",")[0])+dataAcress);
                bpm.add((int)Float.parseFloat(st.split(",")[1]));
                System.out.println("time: "+time.get(time.size()-1)+" - bpm: "+bpm.get(bpm.size()-1));
            }
            System.out.println(bpm.size());
        }

}
