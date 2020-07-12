package com.ishansong.diablo.admin.utils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class AuditLogUtil {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    public static void reportedTransaction(long durationStart, String type, String name, String authorized, String data) {

        String transactionName = new StringJoiner("_").add(name).add(type).add(formatter.format(LocalDateTime.now())).add(authorized).toString();

        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction("Audit_Log", transactionName);
        transaction.setDurationStart(durationStart);

        transaction.setData(data);

        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
    }

    public static void reportedTransaction(String type, String name, String authorized, String data) {

        long durationStart = System.nanoTime();

        String transactionName = new StringJoiner("_").add(name).add(type).add(formatter.format(LocalDateTime.now())).add(authorized).toString();

        DefaultTransaction transaction = (DefaultTransaction) Cat.newTransaction("Audit_Log", transactionName);
        transaction.setDurationStart(durationStart);

        transaction.setData(data);

        transaction.setStatus(Message.SUCCESS);
        transaction.complete();
    }
}
