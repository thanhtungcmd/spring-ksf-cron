package com.ksf.job.contract.order;

import com.google.gson.Gson;
import com.ksf.job.contract.authen.Auth;
import com.ksf.job.contract.database.MysqlConnection;
import com.ksf.job.contract.dto.OrderItem;
import com.ksf.job.contract.dto.OrderList;
import com.ksf.job.contract.thread.BondThread;
import com.ksf.job.contract.thread.InvestThread;
import com.ksf.job.contract.util.CallApi;
import com.ksf.job.contract.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class BondOrderContract extends Thread {

    private final Logger logger = LogManager.getLogger();

    private static Long pageSize;
    private static Long offSet;
    private static String filePath;
    private static String runAll;
    private static Long numberThread;
    public Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queueTransaction = new LinkedBlockingQueue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem>();

    public BondOrderContract() {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            pageSize = Long.parseLong(prop.getProperty("bond.page_size"));
            offSet = Long.parseLong(prop.getProperty("bond.offset"));
            numberThread = Long.parseLong(prop.getProperty("bond.number_thread"));
            filePath = prop.getProperty("file_path");
            runAll = prop.getProperty("run_all");
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            execAll();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void execAll() {
        Auth auth = new Auth();
        String token = auth.exec(
                "https://ks-bond.ksfinance.net/",
                "oidc.user:https://api.sunshinegroup.vn:5000:web_s_sipt_prod"
        );

        boolean isLoop;
        try {
            do {
                isLoop = this.exec(token);
                offSet = offSet + pageSize;
            } while (isLoop);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public boolean exec(String token) {
        try {
            // Get List
            String orderListStr = CallApi.callGet(
                    "https://apibond.sunshinetech.com.vn/api/v1/order/GetOrderPage?branch_type=2&filter=&gridWidth=0&offSet="+ offSet.toString() +"&open_id=-1&pageSize="+ pageSize.toString() +"&prod_id=-1&work_st=-1",
                    token
            );
            Gson gson = new Gson();
            OrderList orderList = gson.fromJson(orderListStr, OrderList.class);

            // Get Detail
            List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataLists = orderList.getData().getDataList().getData();

            for (OrderList.OrderListData.OrderListDataList.OrderListDataListItem item : dataLists) {
                this.queueTransaction.add(item);
            }
            for (int i = 0; i < numberThread; i++) {
                BondThread bondThread = new BondThread(queueTransaction, token);
                bondThread.start();
            }
            while (this.queueTransaction.size() > 0) {
                Thread.sleep(1000);
                logger.info("queueSize:" + this.queueTransaction.size());
            }

            if (dataLists.size() > 0) {
                logger.info("Offset:"+ offSet);
                return runAll.equals("true") ? true : false;
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        return false;
    }

}
