package com.ioomex.code_sandbox.app.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
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
import com.ioomex.code_sandbox.app.utils.StatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Component
public class JavaDockerCodeSandBox extends AbstractCodeSandBoxTemplate {

    private static final Logger log = LoggerFactory.getLogger(JavaDockerCodeSandBox.class);
    public final static DockerClient docker = DockerClientBuilder.getInstance().build();


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


    @Override
    public List<ProcessResult> runClassFile(File codeFile, List<String> inputCase) {
        List<ProcessResult> processResults = new ArrayList<>();
        String  userCodePath= codeFile.getParentFile().getAbsolutePath();
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
            String containerId = DockerUtil.createContainerInter(MagicConstant.DOCKER_JAVA11_IMAGE, String.valueOf(UUID.randomUUID()), userCodePath);

            // 启动前查看容器状态
            if (containerId != null) {
                log.info("启动前容器状态: {}", DockerUtil.getContainerStatus(containerId));
                DockerUtil.startContainer(containerId);
                // 启动后查看容器状态
                log.info("启动后容器状态: {}", DockerUtil.getContainerStatus(containerId));
                DockerUtil.logContainerSync(containerId);
            }


            // 说明已经创建了容器，并且使用了唯一容器，而不是反反复复的去创建容器
            if (StrUtil.isNotEmpty(containerId)) {



                final long[] maxMemory = {0L};
                if (CollUtil.isNotEmpty(inputCase)) {
                    for (String item : inputCase) {
                        StopWatch stopWatch = new StopWatch();
                        String[] args = item.split(" ");
                        String[] command = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, args);
                        log.info("创建命令 {}", Arrays.toString(command));
                        // 执行命令
                        try {
                            // 创建 Exec 实例
                            ExecCreateCmdResponse execCreateCmdResponse = docker
                                    .execCreateCmd(containerId)
                                    .withAttachStderr(true)
                                    .withAttachStdin(true)
                                    .withAttachStdout(true)
                                    .withCmd(command)
                                    .exec();

                            stopWatch.start();

                            ProcessResult executeMessage = new ProcessResult();
                            final String[] message = {null};
                            final String[] errorMessage = {null};
                            long time = 0L;
                            String execId = execCreateCmdResponse.getId();
                            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                                @Override
                                public void onNext(Frame frame) {
                                    StreamType streamType = frame.getStreamType();
                                    if (StreamType.STDERR.equals(streamType)) {
                                        errorMessage[0] = new String(frame.getPayload());
                                        System.out.println("输出错误结果：" + errorMessage[0]);
                                    } else {
                                        message[0] = new String(frame.getPayload());
                                        System.out.println("输出结果：" + message[0]);
                                    }
                                    super.onNext(frame);
                                }
                            };
                            // 获取占用的内存
                            StatsCmd statsCmd = docker.statsCmd(containerId);
                            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                                @Override
                                public void onNext(Statistics statistics) {
                                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                                }

                                @Override
                                public void close() throws IOException {
                                }

                                @Override
                                public void onStart(Closeable closeable) {
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                            statsCmd.exec(statisticsResultCallback);

                            // 启动 Exec 实例并读取输出
                            docker.execStartCmd(execCreateCmdResponse.getId())
                                    .exec(new ExecStartResultCallback() {
                                        @Override
                                        public void onNext(Frame frame) {
                                            StreamType streamType = frame.getStreamType();

                                            if (StreamType.STDERR.equals(streamType)) {
                                                errorMessage[0] = new String(frame.getPayload());

                                                System.out.println("错误输出: " + new String(frame.getPayload()));
                                            } else {
                                                message[0] = new String(frame.getPayload());

                                                System.out.println("输出结果: " + new String(frame.getPayload()));
                                            }
                                            super.onNext(frame);
                                        }
                                    }).awaitCompletion();
                            stopWatch.stop();
                            time = stopWatch.getLastTaskTimeMillis();
                            statsCmd.close();
                            executeMessage.setMessage(message[0]);
                            executeMessage.setErrorMessage(errorMessage[0]);
                            executeMessage.setTime(time);
                            executeMessage.setMemory(maxMemory[0]);
                            processResults.add(executeMessage);
                        } catch (Exception e) {
                            log.info("执行命令出现异常 e", e);
                        }

                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("操作被中断: {}", e.getMessage(), e);
            throw new RuntimeException(e);

        } catch (Exception e) {
            log.error("执行过程中发生错误: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return processResults;
    }

    public static void main(String[] args) {
        AbstractCodeSandBoxTemplate abstractCodeSandBoxTemplate=new JavaDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("10 2", "3 4"));
        executeCodeRequest.setCode(getTestCode());
        executeCodeRequest.setLanguage("java");
        abstractCodeSandBoxTemplate.executeCode(executeCodeRequest);
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