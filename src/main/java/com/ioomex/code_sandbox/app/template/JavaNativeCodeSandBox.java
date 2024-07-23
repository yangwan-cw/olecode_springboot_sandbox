package com.ioomex.code_sandbox.app.template;


import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import org.springframework.stereotype.Component;


@Component
public class JavaNativeCodeSandBox extends AbstractCodeSandBoxTemplate {


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}