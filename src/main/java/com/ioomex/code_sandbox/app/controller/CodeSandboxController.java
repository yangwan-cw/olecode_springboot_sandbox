

package com.ioomex.code_sandbox.app.controller;

import cn.hutool.core.util.ObjUtil;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.model.po.User;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import com.ioomex.code_sandbox.app.template.JavaDockerCodeSandBox;
import com.ioomex.code_sandbox.app.template.JavaNativeCodeSandBox;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@RestController
@RequestMapping("/code")
public class CodeSandboxController {

    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

    @Resource
    private JavaDockerCodeSandBox javaDockerCodeSandBox;

    private final static String AUTH = "auth";

    private final static String MD5_HASH = "fa53b91ccc1b78668d5af58e1ed3a485";


    /*
     * @param executeCodeRequest 请求验证的代码
     * @return 返回参数
     */
    @RequestMapping("/native/execute")
    @ResponseBody

    public ExecuteCodeResponse nativeCodeSandbox(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) {
        String head = httpServletRequest.getHeader(AUTH);
        if(ObjUtil.isEmpty(head)){
            httpServletResponse.setStatus(401);
            return null;
        }
        if (!head.equals(MD5_HASH)) {
            httpServletResponse.setStatus(401);
            return null;
        }
        ExecuteCodeResponse response = javaNativeCodeSandBox.executeCode(executeCodeRequest);
        return response;
    }

    /**
     * 1. 日志记录和监控
     * 2. 防止拒绝服务攻
     * 3. 参数校验
     * 4. 授权一定的用户
     *
     * @param executeCodeRequest 请求验证的代码
     * @return 返回参数
     */
    @RequestMapping("/docker/execute")
    @ResponseBody
    public ExecuteCodeResponse dockerCodeSandBox(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                                 HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) {
        String head = httpServletRequest.getHeader(AUTH);
        if(ObjUtil.isEmpty(head)){
            httpServletResponse.setStatus(401);
            return null;
        }
        if (!head.equals(MD5_HASH)) {
            httpServletResponse.setStatus(401);
            return null;
        }
        ExecuteCodeResponse response = javaDockerCodeSandBox.executeCode(executeCodeRequest);
        return response;
    }

}