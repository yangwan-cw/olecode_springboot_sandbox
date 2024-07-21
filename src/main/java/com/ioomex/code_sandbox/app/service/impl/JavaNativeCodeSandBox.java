package com.ioomex.code_sandbox.app.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
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
        String compileCmd = String.format("javac -encoding utf-8 %s", finalFile.getAbsoluteFile());

        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            int compileResult = compileProcess.waitFor();

            if (compileResult == 0) {
                System.out.print("编译成功");
                BufferedReader bufferedReader =
                  new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }


            } else {
                System.out.print("编译失败");

                BufferedReader errorBufferedReader =
                  new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));

                String line;
                while ((line = errorBufferedReader.readLine()) != null) {
                    System.out.println(line);
                }

            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 生成文件之后
//        FileUtil.del(userCodePath);
        return null;
    }

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

    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Collections.singletonList("1 2"));
        executeCodeRequest.setCode(getTestCode());
        executeCodeRequest.setLanguage("java");
        javaNativeCodeSandBox.executeCode(executeCodeRequest);
    }


    private static String getTestCode() {
        return ResourceUtil.readStr("code/test.java", StandardCharsets.UTF_8);
    }
}