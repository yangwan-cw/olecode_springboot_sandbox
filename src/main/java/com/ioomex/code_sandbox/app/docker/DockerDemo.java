package com.ioomex.code_sandbox.app.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DockerDemo {

    public static void main(String[] args) throws InterruptedException {

        DockerClient docker = DockerClientBuilder.getInstance().build();
//        PingCmd pingCmd= docker.pingCmd();
//        pingCmd.exec();
        String nginx = "nginx:latest";
        PullImageCmd pullImageCmd = docker.pullImageCmd(nginx);
        PullImageResultCallback pullImageResultCallback=new PullImageResultCallback(){
            @Override
            public void onNext(PullResponseItem item) {
               log.info(item.getStatus());
            }
        };
        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();

    }
}
