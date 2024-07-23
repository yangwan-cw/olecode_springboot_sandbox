package com.ioomex.code_sandbox.app.model.po;

import lombok.Data;
import lombok.ToString;

/**
 * 判题信息
 *
 * @author yangwan
 * @from <a href="https://github.com/yangwan-cw">yangwan-cw仓库</a>
 */
@Data
@ToString
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存
     */
    private Long memory;

    /**
     * 消耗时间（KB）
     */
    private Long time;
}
