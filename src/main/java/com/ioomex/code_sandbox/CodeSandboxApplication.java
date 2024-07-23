package com.ioomex.code_sandbox;

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

//    public static void main(String[] args) {
//        SpringApplication.run(CodeSandboxApplication.class, args);
//
//    }
//


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

    public static void main(String[] args) {
        // 获取堆和非堆内存使用情况
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsageBefore = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsageBefore = memoryMXBean.getNonHeapMemoryUsage();

        // 执行用户代码或命令
        try {
            String command = "your_command_here"; // 替换为你的运行命令
            Process process = Runtime.getRuntime().exec(command);

            // 等待命令执行完成
            int exitCode = process.waitFor();

            // 获取执行后的内存使用情况
            MemoryUsage heapMemoryUsageAfter = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsageAfter = memoryMXBean.getNonHeapMemoryUsage();

            // 计算内存使用量的差异
            long heapMemoryUsed = heapMemoryUsageAfter.getUsed() - heapMemoryUsageBefore.getUsed();
            long nonHeapMemoryUsed = nonHeapMemoryUsageAfter.getUsed() - nonHeapMemoryUsageBefore.getUsed();

            // 打印内存使用情况
            System.out.println("Heap Memory Used: " + heapMemoryUsed);
            System.out.println("Non-Heap Memory Used: " + nonHeapMemoryUsed);

            // 处理命令执行结果
            // ...

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
