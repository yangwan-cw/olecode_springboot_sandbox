package com.ioomex.code_sandbox.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.ioomex.code_sandbox.app.costant.FileConstant;
import com.ioomex.code_sandbox.app.costant.MagicConstant;
import com.ioomex.code_sandbox.app.docker.DockerUtil;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.model.po.JudgeInfo;
import com.ioomex.code_sandbox.app.model.po.ProcessResult;
import com.ioomex.code_sandbox.app.sercurity.DefaultSecurityManager;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import com.ioomex.code_sandbox.app.utils.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
public class JavaDockerCodeSandBox implements CodeSandbox {

    private static final Logger log = LoggerFactory.getLogger(JavaDockerCodeSandBox.class);

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
        boolean del = FileUtil.del(userCodePath);
        if (del) {
            log.info("删除成功");
        } else {
            log.error("删除失败");
        }
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.setSecurityManager(new DefaultSecurityManager());
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

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
            return getResponse(e);

        }


        // TODO: 3. 运行对应的文件
        try {
            // 检查镜像是否存在
            boolean imageExists = DockerUtil.isImageExists(MagicConstant.DOCKER_JAVA11_IMAGE);

            // 如果镜像不存在,那么拉取镜像，反之
            if (!imageExists) {
                // 镜像不存在，拉取镜像
                log.info("镜像 {} 不存在，开始拉取...", MagicConstant.DOCKER_JAVA11_IMAGE);
                DockerUtil.pullImage(MagicConstant.DOCKER_JAVA11_IMAGE);
                log.info("镜像 {} 拉取成功", MagicConstant.DOCKER_JAVA11_IMAGE);
            } else {
                log.info("镜像 {} 已存在，无需拉取。", MagicConstant.DOCKER_JAVA11_IMAGE);
            }

            // 根据容器名创建容器
            String containerInterId = DockerUtil.createContainerInter(MagicConstant.DOCKER_JAVA11_IMAGE, String.valueOf(UUID.randomUUID()), userCodePath);



            DockerUtil.startContainer(containerInterId);


            DockerUtil.logContainerSync(containerInterId);

            // 根据参数去循环
            List<String> inputList = executeCodeRequest.getInputList();

            if (CollUtil.isNotEmpty(inputList)) {
                inputList.forEach(item -> {
                    String[] args = item.split(" ");

                    String[] command = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, args);

                    log.info("创建命令 {}", Arrays.toString(command));
                    // 执行命令
                    try {
                        DockerUtil.executeCommandInContainer(containerInterId, command);
                    } catch (Exception e) {
                        log.info("执行命令出现异常 e", e);
                    }
                });
            }

        } catch (InterruptedException e) {
            log.error("操作被中断: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("执行过程中发生错误: {}", e.getMessage(), e);
        }

        // 删除临时文件
        if (finalFile.getParentFile() != null) {
            delTemporarilyFile(fileName);
        }

        return executeCodeResponse;
    }

    private static void monitorThread(Process runProcess) {
        new Thread(() -> {
            try {
                Thread.sleep(MagicConstant.TIME_OUT);
                log.error("超时");
                runProcess.destroy();
            } catch (InterruptedException e) {
                log.error("超时");
            }

        }).start();
    }


    public static void main(String[] args) {
        JavaDockerCodeSandBox javaNativeCodeSandBox = new JavaDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("10 2", "3 4"));
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

    private ExecuteCodeResponse getResponse(Throwable a) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        executeCodeResponse.setMessage(a.getMessage());
        return executeCodeResponse;
    }
}