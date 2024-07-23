package com.ioomex.code_sandbox.app.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.ioomex.code_sandbox.app.costant.FileConstant;
import com.ioomex.code_sandbox.app.costant.MagicConstant;
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
public class JavaNativeCodeSandBox implements CodeSandbox {

    private static final Logger log = LoggerFactory.getLogger(JavaNativeCodeSandBox.class);

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

//        // 校验用户的code是否存在违规操作
//        String code = executeCodeRequest.getCode();
//
//        WordTree wordTree = new WordTree();
//        wordTree.addWord("File");
//        FoundWord foundWord = wordTree.matchWord(code);
//        if (foundWord != null) {
//            System.out.printf(foundWord.getFoundWord());
//            return null;
//        }


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


        List<ProcessResult> processResults = new ArrayList<>();
        Long maxTime = 0L;
        // TODO: 3. 运行对应的文件
        try {
            List<String> inputList = executeCodeRequest.getInputList();

            for (String s : inputList) {
                String finalRunCmd = String.format(FileConstant.RUN_COMMAND, userCodePath, s);
                Process runProcess = Runtime.getRuntime().exec(finalRunCmd);
//                     monitorThread(runProcess);
                ProcessResult processResult = ProcessUtil.processRunOrCompile(runProcess, MagicConstant.RUN);
//                ProcessResult processResult = ProcessUtil.runInteractProcessAndGetMessage(runProcess,s);
                runResult(processResult);
                processResults.add(processResult);
            }
        } catch (Exception e) {
            log.error("运行过程中出现异常", e);
            return getResponse(e);

        }

        List<String> outputList = new ArrayList<>();
        for (ProcessResult processResult : processResults) {
            String errorMessage = processResult.getErrorMessage();
            if (StrUtil.isNotEmpty(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(errorMessage);
            Long time = processResult.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }

        if (outputList.size() == processResults.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
//        judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);

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
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
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