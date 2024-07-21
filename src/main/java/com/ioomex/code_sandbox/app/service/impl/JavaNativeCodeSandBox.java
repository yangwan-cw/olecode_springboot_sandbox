package com.ioomex.code_sandbox.app.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.ioomex.code_sandbox.app.costant.FileConstant;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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

    /**
     * 释放临时资源
     */
    private static void delTemporarilyFile(String userCodePath) {
        FileUtil.del(userCodePath);
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 代码路径
        String codePath = getCodePath();

        // 为每个请求创建对应的文件夹
        String userCodePath = codePath + File.separator + UUID.randomUUID();

        // 文件名
        String fileName = userCodePath + File.separator + FileConstant.MAIN_FILE_NAME;
        // TODO: 1. 生成的路径之后，根据用户的代码去将代码写入到文件
        File finalFile = FileUtil.writeString(executeCodeRequest.getCode(), fileName, StandardCharsets.UTF_8);


        // TODO: 2. 编译对应的文件
        String compileCmd = String.format(FileConstant.COMPILE_COMMAND, finalFile.getAbsoluteFile());

        try {
            // ProcessBuilder.start() 和 Runtime.exec 方法创建一个本机进程，并返回 Process 子类的一个实例，该实例可用来控制进程并获取相关信息。
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            int compileResult = compileProcess.waitFor();


            // 进程
            if (compileResult == 0) {
                log.info("编译成功 ");
                StringBuilder successLog = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    successLog.append(line).append("\n");
                    System.out.println(line);
                }

                if (StrUtil.isNotEmpty(successLog)) {
                    log.error("成功日志  {}", successLog);
                }
            } else {
                log.error("编译失败 ");
                StringBuilder errorLog = new StringBuilder();
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));

                String line;
                while ((line = errorBufferedReader.readLine()) != null) {
                    errorLog.append(line).append("\n");
                    System.out.println(line);
                }
                log.error("错误日志  {}", errorLog);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        return null;
    }


    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Collections.singletonList("1 2"));
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