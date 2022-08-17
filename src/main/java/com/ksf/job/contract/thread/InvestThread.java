package com.ksf.job.contract.thread;

import com.google.gson.Gson;
import com.ksf.job.contract.database.MysqlConnection;
import com.ksf.job.contract.dto.OrderItem;
import com.ksf.job.contract.dto.OrderList;
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

public class InvestThread extends Thread {

    private final Logger logger = LogManager.getLogger();

    private Long pageSize;
    private Long offSet;
    private static String filePath;
    private static String runAll;
    private String token;

    public InvestThread(String token, Long offSet, Long pageSize) {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            filePath = prop.getProperty("file_path");
            runAll = prop.getProperty("run_all");
            this.token = token;
            this.offSet = offSet;
            this.pageSize = pageSize;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            exec(this.token);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public boolean exec(String token) {
        try {

            // Get List
            String orderListStr = CallApi.callGet(
                    "https://apiinvest.sunshinetech.com.vn/api/v2/order/GetOrderPage?branch_type=2&filter=&gridWidth=1217&offSet="+this.offSet.toString()+"&open_id=-1&ord_st=-1&pageSize="+this.pageSize.toString()+"&prod_id=-1&type_data=1&work_st=-1",
                    token
            );
            Gson gson = new Gson();
            OrderList orderList = gson.fromJson(orderListStr, OrderList.class);

            // Get Detail
            List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataLists = orderList.getData().getDataList().getData();

            for (OrderList.OrderListData.OrderListDataList.OrderListDataListItem item : dataLists) {
                this.execMeta(item, token);
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

    public void execMeta(OrderList.OrderListData.OrderListDataList.OrderListDataListItem item, String token) {
        try {
            Gson gson = new Gson();
            String orderItemString = CallApi.callGet(
                    "https://apiinvest.sunshinetech.com.vn/api/v2/order/GetOrderInfo?action=View&ord_id=" + item.getOrd_id(),
                    token
            );
            OrderItem orderItem = gson.fromJson(orderItemString, OrderItem.class);
            List<OrderItem.OrderItemData.OrderItemMeta> metaList = orderItem.getData().getOrd_metas();

            // Get Meta
            for (OrderItem.OrderItemData.OrderItemMeta metaItem : metaList) {
                if (!MysqlConnection.checkExist(metaItem.getMeta_id(), "invest", item.getOrd_code())) {
                    logger.info("Code:"+ item.getOrd_code() +" ;Meta:"+ metaItem.getMeta_id());
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
                    DateTime investDate = dtf.parseDateTime(item.getOrd_inv_at());

                    FileUtils.copyURLToFile(
                            new URL(metaItem.getMeta_file_id_2b()),
                            new File(
                                    filePath
                                            + "Invest" + "\\"
                                            + investDate.getYear() + "\\"
                                            + investDate.getMonthOfYear() + "\\"
                                            + investDate.getDayOfMonth() + "\\"
                                            + item.getBuyer_fullname() + "-" + item.getOrd_code() + "\\"
                                            + metaItem.getMeta_name() + Util.getOriginalName(metaItem.getMeta_file_id_2b(), metaItem.getOutput_filename())
                            )
                    );
                    MysqlConnection.insertItem(
                            metaItem.getMeta_id(),
                            metaItem.getMeta_name(),
                            item.getOrd_id(),
                            metaItem.getMeta_file_id_2b(),
                            item.getOrd_inv_at(),
                            "invest",
                            item.getOrd_code()
                    );
                }
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

}
