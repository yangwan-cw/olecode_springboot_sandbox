package com.ioomex.code_sandbox.app.template;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.ioomex.code_sandbox.app.costant.FileConstant;
import com.ioomex.code_sandbox.app.costant.MagicConstant;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.model.po.JudgeInfo;
import com.ioomex.code_sandbox.app.model.po.ProcessResult;
import com.ioomex.code_sandbox.app.sercurity.DefaultSecurityManager;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import com.ioomex.code_sandbox.app.service.impl.JavaNativeCodeSandBox;
import com.ioomex.code_sandbox.app.utils.ProcessUtil;
import com.ioomex.code_sandbox.app.utils.StatusUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Slf4j
public  class AbstractCodeSandBoxTemplate implements CodeSandbox {


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


    public File saveToFile(String code) {
        System.setSecurityManager(new DefaultSecurityManager());
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        // 代码路径
        String codePath = getCodePath();

        // 为每个请求创建对应的文件夹
        String userCodePath = codePath + File.separator + UUID.randomUUID();

        // 文件名
        String fileName = userCodePath + File.separator + FileConstant.MAIN_FILE_NAME;
        File finalFile = FileUtil.writeString(code, fileName, StandardCharsets.UTF_8);
        return finalFile;
    }

    public ProcessResult compileFile(File userCodeFile) {
        try {
            String compileCmd = String.format(FileConstant.COMPILE_COMMAND, userCodeFile.getAbsoluteFile());
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ProcessResult processResult = ProcessUtil.processRunOrCompile(compileProcess, MagicConstant.COMPILE);
            runResult(processResult);

            if (processResult.getRunCode() != 0) {
                throw new RuntimeException("编译异常");
            }

            return processResult;
        } catch (Exception e) {
            log.error("编译过程中出现异常", e);
//            return getResponse(e);
        }
        return null;
    }


    public List<ProcessResult> runClassFile(File codeFile,List<String> inputCase) {
        List<ProcessResult> processResults = new ArrayList<>();

        String  userCodePath= codeFile.getParentFile().getAbsolutePath();

        try {
            // 测试用例
            for (String testCase : inputCase) {
                String finalRunCmd = String.format(FileConstant.RUN_COMMAND, userCodePath, testCase);
                Process runProcess = Runtime.getRuntime().exec(finalRunCmd);
                ProcessResult processResult = ProcessUtil.processRunOrCompile(runProcess, MagicConstant.RUN);
                runResult(processResult);
                processResults.add(processResult);
            }
        } catch (Exception e) {
            log.error("运行过程中出现异常", e);
            throw new RuntimeException("程序异常");

        }

        return processResults;
    }


    public ExecuteCodeResponse getOutResponse(List<ProcessResult> processResults) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        Long maxTime = 0L;
        // 拼装结果
        List<String> outputList = new ArrayList<>();
        for (ProcessResult processResult : processResults) {
            String errorMessage = processResult.getErrorMessage();
            if (StrUtil.isNotEmpty(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                executeCodeResponse.setStatusStr(StatusUtil.getStatusStr(3));
                break;
            }
            outputList.add(processResult.getMessage());
            Long time = processResult.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }

        if (outputList.size() == processResults.size()) {
            executeCodeResponse.setStatus(2);
            executeCodeResponse.setStatusStr(StatusUtil.getStatusStr(2));
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(1L);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }


    private void clearFile(File finalFile) {
        // 代码路径
        String codePath = getCodePath();
        // 为每个请求创建对应的文件夹
        String userCodePath = codePath + File.separator + UUID.randomUUID();

        // 文件名
        String fileName = userCodePath + File.separator + FileConstant.MAIN_FILE_NAME;
        // 文件清理
        if (finalFile.getParentFile() != null) {
            delTemporarilyFile(fileName);
        }

    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        File file = saveToFile(executeCodeRequest.getCode());

        ProcessResult executeCodeResponse = compileFile(file);


        List<ProcessResult> processResults = runClassFile(file,executeCodeRequest.getInputList());


        ExecuteCodeResponse outResponse = getOutResponse(processResults);
        clearFile(file);

        log.info("executeCodeResponse {}", outResponse.toString());
        return outResponse;
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
        AbstractCodeSandBoxTemplate abstractCodeSandBoxTemplate = new AbstractCodeSandBoxTemplate();
        ExecuteCodeResponse executeCodeResponse = abstractCodeSandBoxTemplate.executeCode(executeCodeRequest);
        log.info("executeCodeResponse {}",executeCodeResponse);
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
