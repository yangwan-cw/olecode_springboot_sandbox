package com.ioomex.code_sandbox.app.costant;

/**
 * FileConstant
 *
 * @author sutton
 * @since 2024-07-20 17:59
 */
public interface FileConstant {

    // 获取用户的根目录
    String ENV = "user.dir";

    // 代码文件夹
    String CODE = "code";

    String MAIN_FILE_NAME = "Main.java";


    String COMPILE_COMMAND="javac -encoding utf-8 %s";

}