package com.newrelic.insights.console;

/**
 * Created by asunwoo on 4/26/16.
 */
public class InsightsAccount {

    private String accountNumber;
    private String apiKey;

    InsightsAccount(String accountNumber, String apiKey){
        this.accountNumber = accountNumber;
        this.apiKey = apiKey;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getApiKey() {
        return apiKey;
    }
}
