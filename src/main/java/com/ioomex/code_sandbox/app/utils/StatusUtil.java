package com.ioomex.code_sandbox.app.utils;

import cn.hutool.core.util.ObjUtil;

public class StatusUtil {


    /**
     * 根据状态值获取状态字符串
     * @param status 状态值
     * @return 返回状态字符串
     */
    public static String getStatusStr(Integer status) {
        if (status.equals(0)) {
            return "待判题";
        }
        if (status.equals(1)) {
            return "判题中";
        }
        if (status.equals(2)) {
            return "成功";
        }
        if (status.equals(3)) {
            return "失败";
        }
        return "";
    }
}
