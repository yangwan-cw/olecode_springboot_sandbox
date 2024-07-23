package com.ioomex.code_sandbox.app.model.po;


import lombok.Data;

@Data
public class ProcessResult {


    private Integer runCode;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}