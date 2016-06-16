package com.newrelic.insights.console;

import java.util.List;
import java.util.ArrayList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigObject;

/**
 * Created by asunwoo on 4/28/16.
 */
public class AccountConfig {

    private InsightsAccount primaryAccount;
    private List<InsightsAccount> secondaryAccounts;

    public void loadConfig() {

        // Load our own config values from the default location,
        // application.conf
        Config conf = ConfigFactory.load();
        String accountNumber = conf.getString("insights.primary.account");
        String apiKey = conf.getString("insights.primary.api_key");

        primaryAccount = new InsightsAccount(accountNumber, apiKey);

        ConfigList socials = conf.getList("insights.secondary");

        secondaryAccounts = new ArrayList<InsightsAccount>();
        InsightsAccount currentAccount = null;
        for (ConfigValue cv : socials) {
            Config c = ((ConfigObject)cv).toConfig();

            accountNumber = c.getString("account");
            apiKey = c.getString("api_key");

            secondaryAccounts.add(new InsightsAccount(accountNumber, apiKey));
        }

    }

    public InsightsAccount getPrimaryAccount() {
        return primaryAccount;
    }

    public List<InsightsAccount> getSecondaryAccounts() {
        return secondaryAccounts;
    }
}
