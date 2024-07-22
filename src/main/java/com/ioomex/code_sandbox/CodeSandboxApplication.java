package com.ioomex.code_sandbox;

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class CodeSandboxApplication {

//    public static void main(String[] args) {
//        SpringApplication.run(CodeSandboxApplication.class, args);
//
//    }
//


    public static void main(String[] args) throws IOException, InterruptedException {
        String userDir = System.getProperty("user.dir"); // 获取当前用户的工作目录
        String filePath = userDir + File.separator + "src/main/resources/木马.bat"; // 构建文件路径
        Process exec = Runtime.getRuntime().exec(filePath);
        exec.waitFor();

        String errorPargram = "java -version 2>&1";
        Files.write(Paths.get(filePath), Arrays.asList(errorPargram));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String compileOutputLine;
        while ((compileOutputLine = bufferedReader.readLine()) != null){
            System.out.printf(compileOutputLine);
        }
        System.out.printf("程序执行成功");
    }
}
