package com.vinted.logback.fluentd.appender;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TimeZone;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.fluentd.logger.FluentLogger;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class FluentdAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private FluentLogger fluentLogger;

    @Override
    public void start() {
        super.start();

        fluentLogger = FluentLogger.getLogger(label != null ? tag : null, remoteHost, port);
    }


    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            if (fluentLogger != null) {
                fluentLogger.close();
            }
        }
    }

    @Override
    protected void append(ILoggingEvent rawData) {
        final Map<String, Object> data = new HashMap<String, Object>();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        data.put("@timestamp", nowAsISO);

        data.put("message", rawData.getFormattedMessage());
        data.put("level", rawData.getLevel());

        if (additionalFields != null) {
            data.putAll(additionalFields);
        }

        for (Entry<String, String> entry : rawData.getMDCPropertyMap().entrySet()) {
            data.put(entry.getKey(), entry.getValue());
        }

        if (label == null) {
            fluentLogger.log(tag, data, rawData.getTimeStamp() / 1000);
        } else {
            fluentLogger.log(label, data, rawData.getTimeStamp() / 1000);
        }
    }

    private String tag;
    private String label;
    private String remoteHost;
    private int port;
    private Map<String, String> additionalFields;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addAdditionalField(Field field) {
        if (additionalFields == null) {
            additionalFields = new HashMap<String, String>();
        }
        additionalFields.put(field.getKey(), field.getValue());
    }

    public static class Field {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
