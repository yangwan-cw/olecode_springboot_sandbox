package com.ioomex.code_sandbox.app.sercurity;

import java.security.Permission;

public class DefaultSecurityManager extends  SecurityManager{


    @Override
    public void checkPermission(Permission permission){
//        System.out.println("默认不做任何权限");
//        super.checkPermission(permission);
    }
}
