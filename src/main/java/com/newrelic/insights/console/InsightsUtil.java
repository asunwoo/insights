package com.newrelic.insights.console;

import java.util.List;
import java.io.PrintStream;

/**
 * Created by asunwoo on 5/17/16.
 */
public class InsightsUtil {
    public static void log(String something) {
        System.out.println(something);
    }

    public static void printList(List<String> printList, String delimiter, PrintStream out){

        int size = printList.size();
        int count = 1;
        StringBuilder sb = new StringBuilder();
        for(String item:printList){
            sb.append(item);
            if(count < size){
                sb.append(delimiter);
                sb.append(' ');
            }
            count++;
        }
        out.println(sb.toString());
    }
}
