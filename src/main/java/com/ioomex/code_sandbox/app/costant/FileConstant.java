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

    // 每个用户的代码文件夹
    String CODE = "code";

    String MAIN_FILE_NAME = "Main.java";


    String COMPILE_COMMAND = "javac -encoding utf-8 %s";

    String RUN_COMMAND = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s";


}