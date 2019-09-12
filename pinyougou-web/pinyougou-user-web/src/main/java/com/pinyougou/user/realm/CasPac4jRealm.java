package com.pinyougou.user.realm;

import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * 认证域
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-04<p>
 */
public class CasPac4jRealm extends Pac4jRealm {

    /** 认证方法(CAS已认证) */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        AuthenticationInfo authenticationInfo = super.doGetAuthenticationInfo(authenticationToken);
        // 获取登录用户名
        Pac4jPrincipal  principal = (Pac4jPrincipal)authenticationInfo
                .getPrincipals().getPrimaryPrincipal();
        System.out.println("登录用户名： " + principal.getName());
        return authenticationInfo;
    }

    /** 授权方法 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        // 获取登录用户名
        Pac4jPrincipal  principal = (Pac4jPrincipal)principals.getPrimaryPrincipal();
        System.out.println("登录用户名： " + principal.getName());

        // 查询用户角色与权限数据

        return super.doGetAuthorizationInfo(principals);
    }
}
