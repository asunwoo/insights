package com.newrelic.insights.console;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 * Created by asunwoo on 6/15/16.
 */
public class InsightsConsoleReader {

    private static InsightsConsoleReader insightsConsole;
    private ConsoleReader console;

    private InsightsConsoleReader(){
        try {
            this.console = new ConsoleReader();
            this.addCompleters();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void addCompleters(){
        String[] keyWords = new String[] {
                "SELECT",
                "FROM",
                "WHERE",
                "AS",
                "FACET",
                "LIMIT",
                "SINCE",
                "UNTIL",
                "WITH TIMEZONE",
                "COMPARE WITH",
                "TIMESERIES",
                "QUIT"
        };

        String[] collections = new String[]{
                "Transaction",
                "PageView",
                "TransactionError",
                "PageAction",
                "Mobile",
                "SyntheticCheck",
                "SyntheticRequest"
        };

        String[] functions = new String[]{
                "apdex(",
                "average(",
                "count(",
                "filter(",
                "histogram(",
                "latest(",
                "max(",
                "min(",
                "percentage(",
                "percentile(",
                "stddev",
                "sum(",
                "uniqueCount(",
                "uniques("
        };

        List<String> keyWordList = new ArrayList<String>((keyWords.length*2) + collections.length + functions.length);

        for(String keyWord:keyWords){
            keyWordList.add(keyWord);
            keyWordList.add(keyWord.toLowerCase());
        }

        for(String collection:collections){
            keyWordList.add(collection);
        }

        for(String function:functions){
            keyWordList.add(function);
        }

        //this.console.addCompleter(new StringsCompleter(keyWords));
        ArgumentCompleter argCompleter = new ArgumentCompleter(
                new StringsCompleter(keyWordList.toArray(new String[keyWordList.size()])));
        argCompleter.setStrict(false);
        this.console.addCompleter(argCompleter);
        this.console.addCompleter(new FileNameCompleter());
        //this.console.addTriggeredAction('q', new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
        //    System.out.println(">>> EXECUTED"); System.exit(0); } });
        this.console.setPrompt("insights> ");
        //System.out.println("Completers Added");
    }

    public static InsightsConsoleReader getConsole(){
        if(insightsConsole == null){
            insightsConsole = new InsightsConsoleReader();
        }
        return insightsConsole;
    }

    public String readLine(){
        String retVal = "";
        try {
            retVal = this.console.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static void main(String[] args) {
        try {
            InsightsConsoleReader console = InsightsConsoleReader.getConsole();

            String line = null;
            while ((line = console.readLine()) != null) {

            }
        } finally {
            try {
                TerminalFactory.get().restore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
