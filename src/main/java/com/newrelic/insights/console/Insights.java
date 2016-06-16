package com.newrelic.insights.console;


import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

public class Insights {

    private static final int PAGE_ROW_LIMIT = 100;

    private static final String QUIT = "quit";
    private static final String DELIMITER_COMMAND = "\\delimiter";
    private static final String OUTPUT_COMMAND = "\\output_file";
    private static final String ACCOUNT_TOGGLE = "\\account_toggle";
    private static final String HELP = "help";

    private String delimiter = "\t";

    private boolean outputToFile;
    private String outputFile;
    private boolean useAccounts;
    private AccountConfig accounts;
    private boolean usingSysOut;

    public static void main(String args[]) {
        Insights insights = new Insights();
        insights.run();
    }

    public Insights() {
        this.outputToFile = false;
        this.outputFile = null;
        this.useAccounts = false;
        this.accounts = new AccountConfig();
        this.accounts.loadConfig();
        this.usingSysOut = true;
    }

    public void run(){
        String query = "";
        long rowCount = 0;

        while(true) {
            query = receiveInput();
            int page = 0;

            PrintStream stream = getPrintStream();

            log("Executing: " + query);

            executeQuery(query, this.accounts.getPrimaryAccount(), stream);

            if(useAccounts){
                for(InsightsAccount account:this.accounts.getSecondaryAccounts()){
                    executeQuery(query, account, stream);
                }
            }

            closePrintStream(stream);
        }
    }

    private void executeQuery(String query, InsightsAccount account, PrintStream stream){
        InsightsQuery queryObject;

        queryObject = new InsightsQuery(query, account);
        queryObject.runQuery();
        List<List<String>> results = queryObject.getQueryResults();

        if(results != null) {
            for (List<String> row : results) {
                InsightsUtil.printList(row, delimiter, stream);
            }
        }

    }

    private PrintStream getPrintStream(){
        PrintStream stream = System.out;
        this.usingSysOut = true;
        try {
            if (this.outputFile != null) {
                FileOutputStream os = new FileOutputStream(this.outputFile);
                stream = new PrintStream(os);
                this.usingSysOut = false;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return stream;
    }

    private void closePrintStream(PrintStream stream){
        if (!this.usingSysOut) {
            stream.flush();
            stream.close();
        }
    }

    public void log(String something) {
        System.out.println(something);
    }

    public String receiveInput() {
        InsightsConsoleReader c = InsightsConsoleReader.getConsole();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }

        boolean query = false;
        String queryString = null;
        System.out.println("Enter Query or Command: ");
        while(!query) {
            String command = c.readLine();

            if (command.toLowerCase().startsWith("select")) {
                queryString = command;
                query = true;
            } else{
                parseCommand(command);
            }
        }

        return queryString;
    }

    private void parseCommand(String command){
        log(command);
        if(command.startsWith(DELIMITER_COMMAND)) {
            this.delimiter = parseCommandArgument(command);
            if(this.delimiter.equals("\\t")){
                this.delimiter = "\t";
            }
        }else if(command.startsWith(OUTPUT_COMMAND)) {
            this.outputFile = parseCommandArgument(command);
            log("Output file is now: " + this.outputFile);
        }else if(command.startsWith(ACCOUNT_TOGGLE)) {
            this.useAccounts = !this.useAccounts;
            log("Use all accounts: " + this.useAccounts);
        }else if(command.toLowerCase().startsWith(HELP)) {
            System.out.println(DELIMITER_COMMAND + " [delimiter character]: sets the delimiter character");
            System.out.println(OUTPUT_COMMAND + " [output file]: output results to specified file");
            System.out.println(ACCOUNT_TOGGLE + ": query against sub-accounts specified in the configuration");
            System.out.println(QUIT + ": quit the application");
        }else if(QUIT.equals(command.toLowerCase())) {
            System.out.println("Thank you for using New Relic Insights!");
            System.exit(0);
        }
    }

    private String parseCommandArgument(String command){
        return command.substring(command.indexOf(' ')+1, command.length());
    }
}
