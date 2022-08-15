package com.ksf.job.contract.dto;

import java.util.List;

public class OrderList {

    public OrderListData data;

    public OrderListData getData() {
        return data;
    }

    public static class OrderListData {
        public OrderListDataList dataList;
        public OrderListDataList getDataList() {
            return dataList;
        }

        public static class OrderListDataList {
            public long recordsTotal;
            public List<OrderListDataListItem> data;

            public List<OrderListDataListItem> getData() {
                return data;
            }

            public static class OrderListDataListItem {
                public long ord_id;
                public String ord_code;
                public String ord_inv_at;
                public String buyer_fullname;
                public String buy_fullname;

                public long soi_id;
                public String soi_code;
                public String soi_begin_at;

                public long getOrd_id() {
                    return ord_id;
                }

                public String getOrd_code() {
                    return ord_code;
                }

                public String getOrd_inv_at() {
                    return ord_inv_at;
                }

                public String getBuyer_fullname() {
                    return buyer_fullname;
                }

                public long getSoi_id() {
                    return soi_id;
                }

                public String getSoi_code() {
                    return soi_code;
                }

                public String getSoi_begin_at() {
                    return soi_begin_at;
                }

                public String getBuy_fullname() {
                    return buy_fullname;
                }
            }
        }
    }
}


