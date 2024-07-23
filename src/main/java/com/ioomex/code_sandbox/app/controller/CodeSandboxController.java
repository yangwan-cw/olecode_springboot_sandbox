

package com.ioomex.code_sandbox.app.controller;

import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import com.ioomex.code_sandbox.app.template.JavaNativeCodeSandBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@RestController
@RequestMapping("/code")
public class CodeSandboxController {

    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;


    @RequestMapping("/execute")
    @ResponseBody
    public ExecuteCodeResponse codeSandbox(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse response = javaNativeCodeSandBox.executeCode(executeCodeRequest);
        return response;
    }

}