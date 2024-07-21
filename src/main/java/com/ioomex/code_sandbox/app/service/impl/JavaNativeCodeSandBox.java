package com.ioomex.code_sandbox.app.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.ioomex.code_sandbox.app.costant.FileConstant;
import com.ioomex.code_sandbox.app.costant.MagicConstant;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.model.po.ProcessResult;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import com.ioomex.code_sandbox.app.utils.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
public class JavaNativeCodeSandBox implements CodeSandbox {

    private static final Logger log = LoggerFactory.getLogger(JavaNativeCodeSandBox.class);

    static {
        file();
    }

    /**
     * 如果目录不存在那么就创建临时资源
     */
    private static void file() {
        String codePath = getCodePath();
        if (!FileUtil.exist(codePath)) {
            FileUtil.mkdir(codePath);
        }
    }


    /**
     * 获取代码文件夹路径
     *
     * @return 获取 code 文件夹路径
     */
    private static String getCodePath() {
        String env = System.getProperty(FileConstant.ENV);
        return env + File.separator + FileConstant.CODE;
    }

    private static void runResult(ProcessResult result) {
        log.info("运行结果  {}", result.getMessage());
        ;
    }


    /**
     * 释放临时资源
     */
    private static void delTemporarilyFile(String userCodePath) {
        FileUtil.del(userCodePath);
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        // 代码路径
        String codePath = getCodePath();

        // 为每个请求创建对应的文件夹
        String userCodePath = codePath + File.separator + UUID.randomUUID();

        // 文件名
        String fileName = userCodePath + File.separator + FileConstant.MAIN_FILE_NAME;
        // TODO: 1. 生成的路径之后，根据用户的代码去将代码写入到文件
        File finalFile = FileUtil.writeString(executeCodeRequest.getCode(), fileName, StandardCharsets.UTF_8);

        // TODO: 2. 编译对应的文件
        try {
            String compileCmd = String.format(FileConstant.COMPILE_COMMAND, finalFile.getAbsoluteFile());

            Process compileProcess = Runtime.getRuntime().exec(compileCmd);

            ProcessResult processResult = ProcessUtil.processRunOrCompile(compileProcess, MagicConstant.COMPILE);
            runResult(processResult);
        } catch (Exception e) {
            log.error("编译过程中出现异常", e);
        }


        // TODO: 3. 运行对应的文件
        try {
            List<String> inputList = executeCodeRequest.getInputList();
            for (String s : inputList) {
                String finalRunCmd = String.format(FileConstant.RUN_COMMAND, userCodePath, s);
                Process runProcess = Runtime.getRuntime().exec(finalRunCmd);
                ProcessResult processResult = ProcessUtil.processRunOrCompile(runProcess, MagicConstant.RUN);
                runResult(processResult);
            }
        } catch (Exception e) {
            log.error("运行过程中出现异常", e);
        }



        return executeCodeResponse;
    }


    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setCode(getTestCode());
        executeCodeRequest.setLanguage("java");
        javaNativeCodeSandBox.executeCode(executeCodeRequest);
    }


    /**
     * 获取测试代码，用于方便调试
     *
     * @return 返回
     */
    private static String getTestCode() {
        return ResourceUtil.readStr("code/Main.java", StandardCharsets.UTF_8);
    }
}