package org.jeecg.modules.wrj.utils;


/**
 * 中间件服务_api
 *
 * @author 冯程骞
 * @date 2024/9/18 11:27
 */
public class MiddleServiceApi {

    static class Zsk {

        /**
         * 知识库前缀
         */
        private static final String ZSK = "/huizhi/vectorstore/zsk";

        /**
         * 知识库创建表
         */
        public static final String ZSK_CREATE_TABLE = ZSK + "/createTable";

        /**
         * 知识库删除表
         */
        public static final String ZSK_DEL_TABLE = ZSK + "/delTable";

        /**
         * 知识库批量删除表
         */
        public static final String ZSK_DEL_BATCH_TABLE = ZSK + "/delBatchTable";

        /**
         * 修改表名称
         */
        public static final String ZSK_UPDATE_TABLE_NAME = ZSK + "/updateTableName";

        /**
         * 插入知识库文档数据
         */
        public static final String ZSK_INSERT_ZSK_DATA = ZSK + "/insertZskData";

        /**
         * 插入知识库文档数据
         */
        public static final String ZSK_DEL_BY_WD_ID = ZSK + "/delByWdId";

        /**
         * 修改知识库文档数据
         */
        public static final String ZSK_EDIT_WD_NR = ZSK + "/editZskNr";
    }


    static class ZjkPkgl {

        /**
         * ZJK_PKGL 前缀
         */
        private static final String ZJK_PKGL = "/huizhi/vectorstore/zjkPkgl";


        /**
         * 添加配块模型管理
         */
        public static final String ZJK_PKGL_ADD_ZJK_PKGL = ZJK_PKGL + "/addZjkPkgl";
        /**
         * 修改配块模型管理
         */
        public static final String ZJK_PKGL_EDIT_ZJK_PKGL = ZJK_PKGL + "/editZjkPkgl";
        /**
         * 删除配块模型管理
         */
        public static final String ZJK_PKGL_DEL_BAYCH_ZJK_PKGL = ZJK_PKGL + "/delBatchZjkPkgl";


        /**
         * 初始化配块模型管理表
         */
        public static final String ZJK_PKGL_INIT_WJK_PKGL = ZJK_PKGL + "/initZjkPkgl";

        /**
         * 插入外部配快
         */
        public static final String ZJK_PKGL_INSERT_WB_PKMXGL = ZJK_PKGL + "/insertWbPkmxgl";

        /**
         * 删除外部配快
         */
        public static final String ZJK_PKGL_DEL_WB_PKMXGL = ZJK_PKGL + "/delWbPkmxgl";
    }


    String PKMX = "/huizhi/vectorstore/pkmx";


    /**
     * 指挥流程
     */
    class Zhlc {

        /**
         * 指挥流程前缀
         */
        private static final String ZHLC = "/huizhi/vectorstore/zhlc";

        /**
         * 插入
         */
        public static final String ZHLC_INSERT = ZHLC + "/insert";

        /**
         * 删除
         */
        public static final String ZHLC_DLE_BY_ID_LIST = ZHLC + "/delByIdList";

        /**
         * 修改
         */
        public static final String ZHLC_UPDATE = ZHLC + "/update";

    }

}
