package com.ioomex.code_sandbox.app.docker;

import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DockerDemo {
    private final static DockerClient docker = DockerClientBuilder.getInstance().build();

    public static void main(String[] args) throws InterruptedException {


//        PingCmd pingCmd= docker.pingCmd();
//        pingCmd.exec();

        // 拉取镜像
        String nginx = "nginx:latest";
        // 检查镜像是否存在
        if (!isImageExists(nginx)) {
            // 拉取镜像
            DockerDemo.pullImage(nginx);
        } else {
            log.info("镜像 {} 已存在，无需拉取。", nginx);
        }

        // 根据镜像创建容器
        String containerId = DockerDemo.createContainer(nginx);

        if (StrUtil.isNotEmpty(containerId)) {
            startContainer(containerId);
        }

    }

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     */
    private static void startContainer(String containerId) {
        docker.startContainerCmd(containerId).exec();
        log.info("容器 {} 启动成功", containerId);
    }


    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名
     * @return 是否存在
     */
    private static boolean isImageExists(String imageName) {
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
     * @param imageName 镜像名
     * @return 容器ID
     */
    private static String createContainer(String imageName) {
        CreateContainerCmd createContainerCmd = docker.createContainerCmd(imageName);
        String containerId = createContainerCmd.withCmd("echo", "hello world").exec().getId();
        log.info("容器 {} 创建成功", containerId);
        return containerId;
    }

    /**
     * 拉取镜像
     *
     * @param imageName 根据名字拉取镜像
     * @throws InterruptedException 异常
     */
    private static void pullImage(String imageName) throws InterruptedException {
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
