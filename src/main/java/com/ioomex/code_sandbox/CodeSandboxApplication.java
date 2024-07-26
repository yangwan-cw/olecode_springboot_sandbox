package com.ioomex.code_sandbox;

import cn.hutool.core.thread.ThreadUtil;
import com.ioomex.code_sandbox.app.starter.ApplicationRunStarter;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class CodeSandboxApplication {

    public static void main(String[] args) {

        // 创建 SpringApplicationBuilder 对象并进行相关设置
        SpringApplicationBuilder builder = new SpringApplicationBuilder(CodeSandboxApplication.class)
                .main(SpringVersion.class)
                .bannerMode(Banner.Mode.CONSOLE);

        // 运行 Spring 应用
        ConfigurableApplicationContext run = builder.run(args);

        // 获取环境对象
        Environment env = run.getEnvironment();
        ApplicationRunStarter.logApplicationStartup(env);
    }



//    public static void main(String[] args) throws IOException, InterruptedException {
//        String userDir = System.getProperty("user.dir"); // 获取当前用户的工作目录
//        String filePath = userDir + File.separator + "src/main/resources/木马.bat"; // 构建文件路径
//        Process exec = Runtime.getRuntime().exec(filePath);
//        exec.waitFor();
//
//        String errorPargram = "java -version 2>&1";
//        Files.write(Paths.get(filePath), Arrays.asList(errorPargram));
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
//        String compileOutputLine;
//        while ((compileOutputLine = bufferedReader.readLine()) != null){
//            System.out.printf(compileOutputLine);
//        }
//        System.out.printf("程序执行成功");
//    }

}
