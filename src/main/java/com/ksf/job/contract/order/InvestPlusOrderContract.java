package com.ksf.job.contract.order;

import com.google.gson.Gson;
import com.ksf.job.contract.authen.Auth;
import com.ksf.job.contract.database.MysqlConnection;
import com.ksf.job.contract.dto.OrderItem;
import com.ksf.job.contract.dto.OrderList;
import com.ksf.job.contract.util.CallApi;
import com.ksf.job.contract.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

public class InvestPlusOrderContract {

    private final Logger logger = LogManager.getLogger();

    private static String pageSize;
    private static String offSet;
    private static String filePath;

    public InvestPlusOrderContract() {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            pageSize = prop.getProperty("invest_plus.page_size");
            offSet = prop.getProperty("invest_plus.offset");
            filePath = prop.getProperty("file_path");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void exec() {
        try {
            Auth auth = new Auth();
            String token = auth.exec(
                    "https://ks-invplus.ksfinance.net/",
                    "oidc.user:https://api.sunshinegroup.vn:5000:web_k_invplus_prod"
            );

            // Get List
            String orderListStr = CallApi.callGet(
                    "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderPage?filter=&gridWidth=0&offSet="+offSet+"&pageSize="+pageSize+"&branch_type=2&work_st=-1&type_data=1&prod_id=-1&open_id=-1&prod_type=homeplus",
                    token
            );
            Gson gson = new Gson();
            OrderList orderList = gson.fromJson(orderListStr, OrderList.class);
            logger.info("orderList:" + orderList);

            // Get Detail
            List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataLists = orderList.getData().getDataList().getData();

            for (OrderList.OrderListData.OrderListDataList.OrderListDataListItem item : dataLists) {
                String orderItemString = CallApi.callGet(
                        "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderInfo?ord_id=" + item.getOrd_id(),
                        token
                );
                OrderItem orderItem = gson.fromJson(orderItemString, OrderItem.class);
                List<OrderItem.OrderItemData.OrderItemMeta> metaList = orderItem.getData().getOrd_metas();

                // Get Meta
                for (OrderItem.OrderItemData.OrderItemMeta metaItem : metaList) {
                    if (!MysqlConnection.checkExist(metaItem.getMeta_id())) {
                        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
                        DateTime investDate = dtf.parseDateTime(item.getOrd_inv_at());

                        logger.info(metaItem.getUpload_file_url());
                        FileUtils.copyURLToFile(
                                new URL(metaItem.getUpload_file_url()),
                                new File(
                                        filePath
                                                + "Invest Plus" + "\\"
                                                + "Hợp đồng đặt mua" + "\\"
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
                                "invest_plus.normal"
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
