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
import java.util.Queue;

public class BondThread extends Thread {

    private final Logger logger = LogManager.getLogger();

    private static String filePath;
    private String token;
    private Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queue;

    public BondThread(Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queue, String token) {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            filePath = prop.getProperty("file_path");
            this.token = token;
            this.queue = queue;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            OrderList.OrderListData.OrderListDataList.OrderListDataListItem item;
            while ((item = this.queue.poll()) != null) {
                this.getMeta(item, this.token);
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void getMeta(OrderList.OrderListData.OrderListDataList.OrderListDataListItem item, String token) {
        try {
            Gson gson = new Gson();
            String orderItemString = CallApi.callGet(
                    "https://apibond.sunshinetech.com.vn/api/v1/order/GetOrderInfo?soi_id=" + item.getSoi_id(),
                    token
            );
            OrderItem orderItem = gson.fromJson(orderItemString, OrderItem.class);
            List<OrderItem.OrderItemData.OrderItemMeta> metaList = orderItem.getData().getOrd_metas();

            // Get Meta
            for (OrderItem.OrderItemData.OrderItemMeta metaItem : metaList) {
                if (!MysqlConnection.checkExist(metaItem.getMeta_id(), "bond", item.getSoi_code())) {
                    logger.info("Code:"+ item.getSoi_code() +"; Meta:"+ metaItem.getMeta_id() + "; Date:"+ item.getSoi_begin_at());
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
                    DateTime investDate = dtf.parseDateTime(item.getSoi_begin_at());

                    FileUtils.copyURLToFile(
                            new URL(metaItem.getTemp_view_url()),
                            new File(
                                    filePath
                                            + "Bond" + "\\"
                                            + investDate.getYear() + "\\"
                                            + investDate.getMonthOfYear() + "\\"
                                            + investDate.getDayOfMonth() + "\\"
                                            + item.getBuy_fullname() + "-" + item.getSoi_code() + "\\"
                                            + metaItem.getMeta_title() + Util.getOriginalName(metaItem.getTemp_view_url(), metaItem.getOutput_filename())
                            )
                    );
                    MysqlConnection.insertItem(
                            metaItem.getMeta_id(),
                            metaItem.getMeta_title(),
                            item.getSoi_id(),
                            metaItem.getTemp_view_url(),
                            item.getSoi_begin_at(),
                            "bond",
                            item.getSoi_code()
                    );
                }
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

}
