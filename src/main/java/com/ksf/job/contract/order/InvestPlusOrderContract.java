package com.ksf.job.contract.order;

import com.google.gson.Gson;
import com.ksf.job.contract.authen.Auth;
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

public class InvestPlusOrderContract {

    private final Logger logger = LogManager.getLogger();

    private static Long pageSize;
    private static Long offSet;
    private static String filePath;
    private static String runAll;

    public InvestPlusOrderContract() {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            pageSize = Long.parseLong(prop.getProperty("invest_plus.page_size"));
            offSet = Long.parseLong(prop.getProperty("invest_plus.offset"));
            filePath = prop.getProperty("file_path");
            runAll = prop.getProperty("run_all");
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void execAll() {
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
                logger.info("Code:"+ item.getOrd_code());
                this.hopDongDatMuaMeta(item, token);
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

    public void hopDongDatMuaMeta(OrderList.OrderListData.OrderListDataList.OrderListDataListItem item, String token) {
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
                if (!MysqlConnection.checkExist(metaItem.getMeta_id())) {
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
                    DateTime investDate = dtf.parseDateTime(item.getOrd_inv_at());

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
                            "invest_plus.normal",
                            item.getOrd_code()
                    );
                }
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
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
                logger.info("Code:"+ item.getOrd_code());
                this.hopDongChuyenDoiMeta(item, token);
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
                if (!MysqlConnection.checkExist(metaItem.getMeta_id())) {
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
