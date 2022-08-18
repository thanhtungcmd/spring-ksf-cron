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

public class InvestPlusChuyenDoiThread extends Thread {

    private final Logger logger = LogManager.getLogger();

    private static String filePath;
    private String token;
    private Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queue;

    public InvestPlusChuyenDoiThread(Queue<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> queue, String token) {
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
                this.hopDongChuyenDoiMeta(item, this.token);
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void hopDongChuyenDoiMeta(OrderList.OrderListData.OrderListDataList.OrderListDataListItem item, String token) {
        try {
            Gson gson = new Gson();
            String orderItemString = CallApi.callGet(
                    "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderInfo?ord_id=" + item.getOrd_id(),
                    token
            );
            OrderItem orderItem = gson.fromJson(orderItemString, OrderItem.class);
            List<OrderItem.OrderItemData.OrderItemMeta> metaList = orderItem.getData().getOrd_metas();

            // Get Meta
            for (OrderItem.OrderItemData.OrderItemMeta metaItem : metaList) {
                if (!MysqlConnection.checkExist(metaItem.getMeta_id(), "invest_plus.convert", item.getOrd_code())) {
                    logger.info("Code:"+ item.getOrd_code() + "; Meta:"+ metaItem.getMeta_id() + "; Date:"+ item.getOrd_inv_at());
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
                    DateTime investDate = dtf.parseDateTime(item.getOrd_inv_at());

                    FileUtils.copyURLToFile(
                            new URL(metaItem.getUpload_file_url()),
                            new File(
                                    filePath
                                            + "Invest Plus" + "\\"
                                            + "Hợp đồng chuyển đổi" + "\\"
                                            + investDate.getYear() + "\\"
                                            + investDate.getMonthOfYear() + "\\"
                                            + investDate.getDayOfMonth() + "\\"
                                            + item.getBuyer_fullname() + "-" + item.getOrd_code() + "\\"
                                            + metaItem.getMeta_name() + Util.getOriginalName(metaItem.getUpload_file_url(), metaItem.getOutput_filename())
                            )
                    );
                    MysqlConnection.insertItem(
                            metaItem.getMeta_id(),
                            metaItem.getMeta_name(),
                            item.getOrd_id(),
                            metaItem.getUpload_file_url(),
                            item.getOrd_inv_at(),
                            "invest_plus.convert",
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
