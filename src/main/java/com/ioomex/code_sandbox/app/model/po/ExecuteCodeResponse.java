package com.ioomex.code_sandbox.app.model.po;

import lombok.*;

import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {



    private List<String> outputList;

    /**
     * 接口消息，一般用于异常处理，接口正常的时候这个为空,反之接受错误
     */
    private String message;

    /**
     * 执行状态: 0 - 待判题、1 - 判题中、2 - 成功、3 - 失败
     */
    private Integer status;


    /**
     * 执行状态字符串: 0 - 待判题、1 - 判题中、2 - 成功、3 - 失败
     */
    private String statusStr;

    /**
     * 判题信息: 用于总的判题消息
     */
    private JudgeInfo judgeInfo;
}
