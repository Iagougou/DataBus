package com.example.demo.canal;


import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author haihongjia
 */
public class ClientTest {
    public static void main(String args[]) {
        // 单机模式
        /*CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("10.143.135.84",
                11111), "example", "root", "user");*/

        String zkHost = "10.143.135.13:2181,10.143.135.65:2181,10.143.135.84:2181";
        // 集群模式
        CanalConnector connector = CanalConnectors.newClusterConnector(zkHost, "example", "root", "user");

        // 每次数据的偏移量
        int batchSize = 1000;
        int emptyCount = 0;
        // 外层死循环：
        // 在canal节点宕机后，抛出异常， 等待zookeeper对canal处理机的切换，切换完后，继续创建连接处理数据
        while(true){
            try {
                connector.connect();
                connector.subscribe("test.employees_test");
                //connector.subscribe(".*\\..*");
                connector.rollback();
                int totalEmptyCount = 120;
                while (emptyCount < totalEmptyCount) {
                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    // 偏移量不等于-1 或者 获取的数据条数不为0 时，认为拿到消息，并处理
                    if (batchId == -1 || size == 0) {
                        emptyCount++;
                        System.out.println("empty count : " + emptyCount);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        emptyCount = 0;
                        printEntry(message.getEntries());
                    }

                    connector.ack(batchId); // 提交确认
                }

                System.out.println("empty too many times, exit");
            } finally {
                connector.disconnect();
            }
        }
    }

    private static void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------> before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------> after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}