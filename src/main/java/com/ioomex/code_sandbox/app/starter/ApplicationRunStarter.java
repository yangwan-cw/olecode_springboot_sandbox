package com.ioomex.code_sandbox.app.starter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

@Slf4j
public class ApplicationRunStarter {



    /**
     * 日志输出启动信息
     * @param env 环境变量
     */
    public static void logApplicationStartup(Environment env) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String port = env.getProperty("server.port");
            String path = env.getProperty("server.servlet.context-path", "");

            String localUrl = "http://localhost:" + port + path;
            String kn4jDocUrl = "http://" + ip + ":" + port + path + "/doc.html";

            log.info("\n----------------------------------------------------------\n\t"
                            + "Application is running! Access URLs:\n\t"
                            + "Local: \t\t{}\n\t"
                            + "Kn4j-Doc: \t\t{}\n"
                            + "{} \n"
                            + "----------------------------------------------------------",
                    localUrl, kn4jDocUrl,getStartupMessage());

            log.info("启动成功 V0.0.1 {}", System.currentTimeMillis());

        } catch (UnknownHostException e) {
            log.error("The host address could not be determined.", e);
        }
    }

    /**
     *
     * @return 规范日志
     */
    private static String getStartupMessage() {
        return "\n1. 第一点,接口上必须增加 @ApiOperation 或者 @OperationLog, 为了规范以及操作日志记录\n"
                + "2. 代码提交前需要 review 确保代码质量以及正确无误的情况下提交到仓库\n"
                + "3. 尽量少用 merge,多使用 rebase 保证 git 树干净，在不得已的情况可以使用 merge";
    }
}
