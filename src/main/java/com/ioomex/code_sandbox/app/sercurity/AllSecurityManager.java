package com.ioomex.code_sandbox.app.sercurity;

import javax.sql.rowset.serial.SerialException;
import java.security.Permission;

public class AllSecurityManager extends SecurityManager {


    @Override
    public void checkRead(String file) {
        throw new SecurityException("权限异常： " + file);
    }

    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("权限异常： " + cmd);
    }

    @Override
    public void checkDelete(String file) {
        throw new SecurityException("权限异常： " + file);
    }

    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("权限异常： " + host + "port " + port);
    }

}
