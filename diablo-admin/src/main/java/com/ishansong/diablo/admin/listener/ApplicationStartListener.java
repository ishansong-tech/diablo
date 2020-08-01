package com.ishansong.diablo.admin.listener;

import com.ishansong.diablo.config.DiabloConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ApplicationStartListener implements ApplicationListener<WebServerInitializedEvent> {

    private String diabloHttpPath;

    @Autowired
    public ApplicationStartListener(DiabloConfig diabloConfig) {

        String diabloHttpPath=diabloConfig.getAdmin().getDomain();
        this.diabloHttpPath = diabloHttpPath;
    }

    @Override
    public void onApplicationEvent(final WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        final String host = getHost();
        final String domain = diabloHttpPath;
        if (StringUtils.isBlank(domain)) {
            DiabloDomain.getInstance()
                    .setHttpPath("http://" + String.join(":", host, String.valueOf(port)));
        } else {
            DiabloDomain.getInstance()
                    .setHttpPath(domain);
        }
    }

    private String getHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}
