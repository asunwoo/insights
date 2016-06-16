package com.newrelic.insights.console;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by asunwoo on 5/17/16.
 */
public class InsightsQuery {
    private static final String ACCOUNT_TOKEN = "%account%";
    private static final String INSIGHTS_API_URL = "https://insights-api.newrelic.com/v1/accounts/"+ ACCOUNT_TOKEN +"/query?nrql=";

    private String delimiter = ",";

    private InsightsAccount account;
    private String query;
    private List<List<String>> queryResults;

    public List<List<String>> getQueryResults() {
        return queryResults;
    }

    public InsightsQuery(String query, InsightsAccount account){
        this.query = query;
        this.account = account;
    }

    public void runQuery() {
        try {
            String apiUrl = INSIGHTS_API_URL.replaceAll(ACCOUNT_TOKEN, account.getAccountNumber());
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(apiUrl+encodedQuery);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-Query-Key", account.getApiKey());

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String readLine = null;
//            while (((readLine = br.readLine()) != null)) {
//                System.out.println(readLine);
//
//
//            }

            try {
                 parseResults(conn.getInputStream());
            }catch(Exception e){
                e.printStackTrace();
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseResults(InputStream is) throws Exception {
        JsonFactory f = new MappingJsonFactory();
        JsonParser jp = f.createParser(is);
        JsonNode valueNode = null;

        boolean first = true;

        this.queryResults = new LinkedList<List<String>>();
        List<String> fieldList = null;
        List<String> rowList = null;

        long rowCount = 0;

        JsonToken current;
        current = jp.nextToken();
        //Move up the cursor to the events
        while (jp.getCurrentName() == null || !jp.getCurrentName().equals("events")) {
            jp.nextToken();
        }

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            // move from field name to field value
            current = jp.nextToken();
            if (fieldName.equals("events")) {
                if (current == JsonToken.START_OBJECT) {
                    // For each of the records in the array
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        // read the record into a tree model,
                        // this moves the parsing position to the end of it
                        JsonNode node = jp.readValueAsTree();
                        // And now we have random access to everything in the object

                        if(first){
                            first = false;
                            fieldList = makeList(node);
                            java.util.Collections.sort(fieldList);
//                            printList(fieldList);
                            this.queryResults.add(fieldList);
                        }

                        if(fieldList != null){
                            rowList = new ArrayList<String>();
                            for(String name:fieldList){
                                valueNode = node.findValue(name);
                                rowList.add(getNodeValueAsText(valueNode));
                            }
//                            printList(rowList);
                            this.queryResults.add(rowList);
                            rowCount++;
                        }
                    }
                } else {
                    InsightsUtil.log("Error: records should be an array: skipping.");
                    jp.skipChildren();
                }
            } else {
                InsightsUtil.log("Unprocessed property: " + fieldName);
                jp.skipChildren();
            }
        }
    }

    private String getNodeValueAsText(JsonNode node){
        if(node == null){
            return "";
        }
        else if(node.isTextual()){
            return node.textValue();
        }
        else if(node.isNumber()){
            return node.numberValue()+"";
        }
        return "";
    }

    private static <String> List<String> makeList(JsonNode node) {
        Iterator iter = node.fieldNames();
        List<String> list = new ArrayList<String>();
        while(iter.hasNext()){
            list.add((String)iter.next());
        }
        return list;
    }
}
