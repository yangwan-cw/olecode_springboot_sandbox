/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ioomex.code_sandbox.app.controller;

import com.ioomex.code_sandbox.app.model.po.ExecuteCodeRequest;
import com.ioomex.code_sandbox.app.model.po.ExecuteCodeResponse;
import com.ioomex.code_sandbox.app.service.CodeSandbox;
import org.springframework.beans.factory.annotation.Autowired;
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
    private CodeSandbox codeSandbox;

    // http://127.0.0.1:8080/hello?name=lisi
    // 测试
    @RequestMapping("/execute")
    @ResponseBody
    public ExecuteCodeResponse codeSandbox(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse response = codeSandbox.executeCode(executeCodeRequest);
        return response;
    }

}