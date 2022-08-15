package com.ksf.job.contract.order;

import com.google.gson.Gson;
import com.ksf.job.contract.authen.Auth;
import com.ksf.job.contract.dto.OrderList;
import com.ksf.job.contract.util.CallApi;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class InvestPlusRenderContract {

    private final Logger logger = LogManager.getLogger();

    public void exec() {
        try {
            Auth auth = new Auth();
            String token = auth.exec(
                    "https://ks-invplus.ksfinance.net/",
                    "oidc.user:https://api.sunshinegroup.vn:5000:web_k_invplus_prod"
            );

            try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Get List
                    String orderListStr = CallApi.callGet(
//                            "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderPage?filter="+line+"&gridWidth=0&offSet=0&pageSize=15&branch_type=1&work_st=-1&type_data=1&prod_id=-1&open_id=-1&prod_type=homecoop",
                            "https://apiinvplus.sunshinetech.com.vn/api/v2/final/GetOrderFinalPage?filter="+ line +"&toDate=&projectCd=&gridWidth=1500&liq_st=0&offSet=0&pageSize=15&prod_type=homecoop",
                            token
                    );

                    Gson gson = new Gson();
                    OrderList orderList = gson.fromJson(orderListStr, OrderList.class);

                    // Get Detail
                    List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataLists = orderList.getData().getDataList().getData();

                    if (dataLists.size() > 0) {
//                        logger.info("Chờ xử lý:"+ line);
                        logger.info("Đáo hạn:" + line);
                        FileUtils.writeStringToFile(new File("daohan.txt"), line +"\n", StandardCharsets.UTF_8, true);
                        /*OrderList.OrderListData.OrderListDataList.OrderListDataListItem item = dataLists.get(0);
                        boolean resOne = CallApi.callPut(
                                "https://apiinvplus.sunshinetech.com.vn/api/v2/order/SetContract",
                                token,
                                String.valueOf(item.getOrd_id())
                        );
                        if (resOne) {
                            logger.info("Thành công gửi duyệt:" + line);
                        } else {
                            logger.info("Lỗi gửi duyệt:" + line);
                        }
                        boolean resTwo = CallApi.callPost(
                                "https://apiinvplus.sunshinetech.com.vn/api/v2/order/SetOrderApproveReg",
                                token,
                                String.valueOf(item.getOrd_id())
                        );
                        if (resTwo) {
                            logger.info("Thành công duyệt:" + line);
                        } else {
                            logger.info("Lỗi duyệt:" + line);
                        }
                        if (resOne && resTwo) {
                            logger.info("Success: " + line);
                            FileUtils.writeStringToFile(new File("success.txt"), StandardCharsets.UTF_8, line +"\n", true);
                        } else {
                            logger.info("False: " + line);
                            FileUtils.writeStringToFile(new File("error.txt"), StandardCharsets.UTF_8, line +"\n", true);
                        }*/
                    } else {
                        //logger.info("Hợp đồng:"+ line);
                        //https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderPage?filter=IC%C4%90%2FP-NOTE%2F03715%2FB-PRO-M.BGICH2124001-KSS%09&gridWidth=0&offSet=0&pageSize=15&branch_type=0&work_st=-1&type_data=1&prod_id=-1&open_id=-1&prod_type=homecoop

                        /*String orderListStrTwo = CallApi.callGet(
                                "https://apiinvplus.sunshinetech.com.vn/api/v2/order/GetOrderPage?filter="+ line +"&gridWidth=0&offSet=0&pageSize=15&branch_type=0&work_st=-1&type_data=1&prod_id=-1&open_id=-1&prod_type=homecoop",
                                token
                        );

                        OrderList orderListTwo = gson.fromJson(orderListStr, OrderList.class);

                        // Get Detail
                        List<OrderList.OrderListData.OrderListDataList.OrderListDataListItem> dataListsTwo = orderListTwo.getData().getDataList().getData();
                        if (dataListsTwo.size() > 0) {
                            logger.info("Sổ lệnh:"+ line);
                        }*/
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
