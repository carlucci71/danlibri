package it.daniele.danlibri;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {
    private Map<String, String> addresses;

    public Map<String, String> getAddresses() {
        return addresses;
    }
    @SuppressWarnings("unused")
    public void setAddresses(Map<String, String> addresses) {
        this.addresses = addresses;
    }
}

