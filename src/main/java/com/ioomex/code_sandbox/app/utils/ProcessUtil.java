package com.ioomex.code_sandbox.app.utils;



import com.ioomex.code_sandbox.app.model.po.ProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {


    private static final Logger log = LoggerFactory.getLogger(ProcessUtil.class);

    /**
     * 执行进程并返回执行结果
     * ProcessBuilder.start() 和 Runtime.exec 方法创建一个本机进程，并返回 Process 子类的一个实例，该实例可用来控制进程并获取相关信息。
     *
     * @param process          要执行的进程
     * @param runOrCompileName 运行或编译的名称，用于日志记录
     * @return 执行结果对象
     */
    public static ProcessResult processRunOrCompile(Process process, String runOrCompileName) {
        ProcessResult processResult = new ProcessResult();
        try {
            int exitCode = process.waitFor();
            processResult.setRunCode(exitCode);
            if (exitCode == 0) {
                log.info("{} 成功", runOrCompileName);
                String successLog = readStream(process.getInputStream());
                processResult.setMessage(successLog);
            } else {
                log.error("{} 失败", runOrCompileName);
                String errorLog = readStream(process.getErrorStream());
                processResult.setMessage(errorLog);
            }
        } catch (IOException | InterruptedException e) {
            log.error("执行 {} 进程时发生异常", runOrCompileName, e);
            throw new RuntimeException("执行进程时发生异常", e);
        } finally {
            closeProcessStreams(process);
        }

        return processResult;
    }

    /**
     * 读取输入流内容为字符串
     *
     * @param inputStream 输入流
     * @return 输入流内容的字符串表示
     * @throws IOException 读取过程中的IO异常
     */
    private static String readStream(java.io.InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 关闭进程的输入流和错误流
     *
     * @param process 要关闭流的进程
     */
    private static void closeProcessStreams(Process process) {
        try {
            process.getInputStream().close();
            process.getErrorStream().close();
        } catch (IOException e) {
            log.error("关闭进程流时发生异常", e);
        }
    }

}