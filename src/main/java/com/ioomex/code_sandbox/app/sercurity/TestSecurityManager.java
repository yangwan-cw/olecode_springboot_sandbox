package com.ioomex.code_sandbox.app.sercurity;

import cn.hutool.core.io.FileUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {

    public static void main(String[] args) {
        System.setSecurityManager(new AllSecurityManager());
        List<String> readFile= FileUtil.readLines("/workspaces/olecode_springboot_sandbox/src/main/resources/application.yml", StandardCharsets.UTF_8);

    }
}
