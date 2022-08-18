package com.ksf.job.contract.order;

import com.google.gson.Gson;
import com.ksf.job.contract.authen.Auth;
import com.ksf.job.contract.dto.OrderList;
import com.ksf.job.contract.thread.InvestPlusDatMuaThread;
import com.ksf.job.contract.util.CallApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class InvestPlusOrderContract extends Thread {

    private final Logger logger = LogManager.getLogger();

    private static Long pageSize;
    private static Long offSet;
    private static String filePath;
    private static String runAll;
    private static Long numberThread;
    private static String enable;
    public Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queueDatMua = new LinkedBlockingQueue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem>();
    public Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queueChuyenDoi = new LinkedBlockingQueue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem>();

    public InvestPlusOrderContract() {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            pageSize = Long.parseLong(prop.getProperty("invest_plus.page_size"));
            offSet = Long.parseLong(prop.getProperty("invest_plus.offset"));
            numberThread = Long.parseLong(prop.getProperty("invest_plus.number_thread"));
            filePath = prop.getProperty("file_path");
            runAll = prop.getProperty("run_all");
            enable = prop.getProperty("invest_plus.enable");
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
                    "https://ks-invplus.ksfinance.net/",
                    "oidc.user:https://api.sunshinegroup.vn:5000:web_k_invplus_prod"
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
            boolean conOne = this.hopDongDatMua(token);
            boolean conTwo = this.hopDongChuyenDoi(token);

            if (conOne || conTwo) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean hopDongDatMua(String token) {
        try {
            // Get List
            String orderListStr = CallApi.callGet(
                    "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderPage?filter=&gridWidth=0&offSet="+offSet.toString()+"&pageSize="+pageSize.toString()+"&branch_type=2&work_st=-1&type_data=1&prod_id=-1&open_id=-1&prod_type=homeplus",
                    token
            );
            Gson gson = new Gson();
            OrderList orderList = gson.fromJson(orderListStr, OrderList.class);

            // Get Detail
            List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataLists = orderList.getData().getDataList().getData();

            for (OrderList.OrderListData.OrderListDataList.OrderListDataListItem item : dataLists) {
                this.queueDatMua.add(item);

            }
            for (int i = 0; i < numberThread; i++) {
                InvestPlusDatMuaThread investThread = new InvestPlusDatMuaThread(queueDatMua, token);
                investThread.start();
            }
            while (this.queueDatMua.size() > 0) {
                Thread.sleep(10000);
                logger.info("queueSize DatMua:" + this.queueDatMua.size());
            }
            if (dataLists.size() > 0) {
                logger.info("Offset:"+ offSet);
                return runAll.equals("true") ? true : false;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

        return false;
    }



    public boolean hopDongChuyenDoi(String token) {
        try {
            // Get List
            String orderListStr = CallApi.callGet(
                    "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderPage?filter=&gridWidth=0&offSet=" + offSet.toString() + "&pageSize=" + pageSize.toString() + "&branch_type=2&work_st=-1&type_data=1&prod_id=-1&open_id=-1&prod_type=homecoop",
                    token
            );
            Gson gson = new Gson();
            OrderList orderList = gson.fromJson(orderListStr, OrderList.class);

            // Get Detail
            List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataLists = orderList.getData().getDataList().getData();

            for (OrderList.OrderListData.OrderListDataList.OrderListDataListItem item : dataLists) {
                this.queueChuyenDoi.add(item);
            }
            for (int i = 0; i < numberThread; i++) {
                InvestPlusDatMuaThread investThread = new InvestPlusDatMuaThread(queueChuyenDoi, token);
                investThread.start();
            }
            while (this.queueChuyenDoi.size() > 0) {
                Thread.sleep(10000);
                logger.info("queueSize ChuyenDoi:" + this.queueChuyenDoi.size());
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
