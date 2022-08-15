package com.ksf.job.contract.dto;

import java.util.List;

public class OrderItem {
    public OrderItemData data;

    public OrderItemData getData() {
        return data;
    }

    public static class OrderItemData {
        public List<OrderItemMeta> ord_metas;

        public List<OrderItemMeta> getOrd_metas() {
            return ord_metas;
        }

        public static class OrderItemMeta {
            public long meta_id;
            public String meta_name;
            public String meta_file_id;
            public String meta_file_id_2b;
            public String upload_file_url;
            public String output_filename;
            public String temp_view_url;
            public String meta_title;

            public long getMeta_id() {
                return meta_id;
            }

            public String getMeta_name() {
                return meta_name;
            }

            public String getMeta_file_id() {
                return meta_file_id;
            }

            public String getMeta_file_id_2b() {
                return meta_file_id_2b;
            }

            public String getOutput_filename() {
                return output_filename;
            }

            public String getTemp_view_url() {
                return temp_view_url;
            }

            public String getMeta_title() {
                return meta_title;
            }

            public String getUpload_file_url() {
                return upload_file_url;
            }
        }
    }

}
