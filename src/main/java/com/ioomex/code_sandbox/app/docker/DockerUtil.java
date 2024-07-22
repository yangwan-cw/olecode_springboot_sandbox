package com.ioomex.code_sandbox.app.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.ioomex.code_sandbox.app.costant.MagicConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class DockerUtil {
    public final static DockerClient docker = DockerClientBuilder.getInstance().build();

    public static void main(String[] args) throws InterruptedException {
        // 拉取镜像
        String nginx = "nginx:latest";
        String containerName = "nginx-container-002";

        // 检查镜像是否存在
        if (!isImageExists(nginx)) {
            // 拉取镜像
            DockerUtil.pullImage(nginx);
        } else {
            log.info("镜像 {} 已存在，无需拉取。", nginx);
        }

        // 检查容器是否存在
        if (!isContainerExists(containerName)) {
            // 根据镜像创建容器
            String containerId = createContainer(nginx, containerName);

            // 启动前查看容器状态
            if (containerId != null) {
                log.info("启动前容器状态: {}", getContainerStatus(containerId));
                startContainer(containerId);
                // 启动后查看容器状态
                log.info("启动后容器状态: {}", getContainerStatus(containerId));

                logContainerLogs(containerId);
            }
        } else {
            log.info("容器 {} 已存在，无需创建。", containerName);
        }


        // 停止并删除容器
//        deleteContainer(containerName);

    }

    /**
     * 在容器中执行命令
     *
     * @param containerId 容器ID
     * @param command     要执行的命令
     * @throws Exception 异常
     */
    public static void executeCommandInContainer(String containerId, String[] command) throws InterruptedException {
        // 创建 Exec 实例
        ExecCreateCmdResponse execCreateCmdResponse = docker.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(command)
                .exec();

        // 启动 Exec 实例并读取输出
        docker.execStartCmd(execCreateCmdResponse.getId())
                .withDetach(false)
                .exec(new ExecStartResultCallback() {
                    @Override
                    public void onStart(Closeable closeable) {
                        log.info("开始执行命令...");
                    }

                    @Override
                    public void onNext(Frame frame) {
                        log.info("输出: {}", new String(frame.getPayload(), StandardCharsets.UTF_8));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("执行命令时发生错误: {}", throwable.getMessage(), throwable);
                    }

                    @Override
                    public void onComplete() {
                        log.info("命令执行完毕");
                    }
                }).awaitCompletion();
    }

    /**
     * 删除容器
     *
     * @param containerName 容器名
     */
    public static void deleteContainer(String containerName) {
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (name.equals("/" + containerName)) {
                    try {
                        log.info("停止容器: {}", container.getId());
                        docker.stopContainerCmd(container.getId()).exec();

                        log.info("删除容器: {}", container.getId());
                        docker.removeContainerCmd(container.getId()).exec();
                    } catch (Exception e) {
                        log.error("删除容器 {} 时出错: {}", container.getId(), e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * 打印容器日志
     *
     * @param containerId 容器ID
     */
    public static void logContainerLogs(String containerId) {
        LogContainerCmd logContainerCmd = docker.logContainerCmd(containerId).withStdOut(true).withStdErr(true).withFollowStream(true);

        ResultCallback.Adapter<Frame> resultCallback = new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                log.info("容器日志: {}", new String(frame.getPayload(), StandardCharsets.UTF_8));
            }
        };

        try {
            logContainerCmd.exec(resultCallback).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("获取容器日志时出错: {}", e.getMessage(), e);
        }
    }


    /**
     * 获取容器状态
     *
     * @param containerId 容器ID
     * @return 容器状态
     */
    public static String getContainerStatus(String containerId) {
        InspectContainerResponse containerResponse = docker.inspectContainerCmd(containerId).exec();
        return containerResponse.getState().getStatus();
    }

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     */
    public static void startContainer(String containerId) {
        docker.startContainerCmd(containerId).exec();
        log.info("容器 {} 启动成功", containerId);
    }


    /**
     * 检查容器是否存在
     *
     * @param containerName 容器名
     * @return 是否存在
     */
    public static boolean isContainerExists(String containerName) {
        List<Container> containers = docker.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            for (String name : container.getNames()) {
                if (name.equals("/" + containerName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名
     * @return 是否存在
     */
    public static boolean isImageExists(String imageName) {
        List<Image> images = docker.listImagesCmd().exec();
        for (Image image : images) {
            for (String tag : image.getRepoTags()) {
                if (imageName.equals(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 创建容器根据镜像名
     *
     * @param imageName     镜像名
     * @param containerName 容器名
     * @return 容器ID
     */
    public static String createContainer(String imageName, String containerName) {
        CreateContainerCmd createContainerCmd = docker.createContainerCmd(imageName);
        String containerId = createContainerCmd.withName(containerName).withCmd("echo", "hello world").exec().getId();
        log.info("容器 {} 创建成功", containerId);
        return containerId;
    }

    /**
     * 根据镜像名字去创建容器，那么容器名可采用随机或者指定
     *
     * @param imageName     镜像名
     * @param containerName 容器名
     * @return 返回容器id
     */
    public static String createContainerInter(String imageName, String containerName, String codePath) {
        CreateContainerCmd createContainerCmd = docker.createContainerCmd(imageName);
        String dockerDocFilename = MagicConstant.DOCKER_DOC_FILENAME;


        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(codePath, new Volume(dockerDocFilename)));
        hostConfig.withCpuCount(1L);
        hostConfig.withMemory(100 * 1000 * 1000L);


        String containerId = createContainerCmd
                .withName(containerName)
                .withHostConfig(hostConfig)
                .withTty(true)                  // 开启伪终端
                .withAttachStdin(true)          // 附加标准输入
                .withAttachStdout(true)         // 附加标准输出
                .withAttachStderr(true)         // 附加标准错误
                .exec()
                .getId();


        log.info("交互式容器 {} 创建成功", containerId);
        return containerId;
    }

    /**
     * 拉取镜像
     *
     * @param imageName 根据名字拉取镜像
     * @throws InterruptedException 异常
     */
    public static void pullImage(String imageName) throws InterruptedException {
        PullImageCmd pullImageCmd = docker.pullImageCmd(imageName);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                log.info(item.getStatus());
            }
        };
        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
    }
}
