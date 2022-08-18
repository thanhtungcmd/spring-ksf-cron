package com.ksf.job.contract.order;

import com.google.gson.Gson;
import com.ksf.job.contract.authen.Auth;
import com.ksf.job.contract.dto.OrderList;
import com.ksf.job.contract.thread.InvestThread;
import com.ksf.job.contract.util.CallApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class InvestOrderContract extends Thread {

    private final Logger logger = LogManager.getLogger();

    private static Long pageSize;
    private static Long offSet;
    private static String filePath;
    private static String runAll;
    private static Long numberThread;
    private static String enable;
    public Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queueTransaction = new LinkedBlockingQueue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem>();

    public InvestOrderContract() {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            pageSize = Long.parseLong(prop.getProperty("invest.page_size"));
            offSet = Long.parseLong(prop.getProperty("invest.offset"));
            numberThread = Long.parseLong(prop.getProperty("invest.number_thread"));
            filePath = prop.getProperty("file_path");
            runAll = prop.getProperty("run_all");
            enable = prop.getProperty("invest.enable");
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
        if (enable.equals("true")) {
            Auth auth = new Auth();
            String token = auth.exec(
                    "https://ks-invest.ksfinance.net/",
                    "oidc.user:https://api.sunshinegroup.vn:5000:web_ks_invest_prod"
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
    }

    public boolean exec(String token) {
        try {
            // Get List
            String orderListStr = CallApi.callGet(
                    "https://apiinvest.sunshinetech.com.vn/api/v2/order/GetOrderPage?branch_type=2&filter=&gridWidth=1217&offSet="+offSet.toString()+"&open_id=-1&ord_st=-1&pageSize="+pageSize.toString()+"&prod_id=-1&type_data=1&work_st=-1",
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
                InvestThread investThread = new InvestThread(queueTransaction, token);
                investThread.start();
            }
            while (this.queueTransaction.size() > 0) {
                Thread.sleep(10000);
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
