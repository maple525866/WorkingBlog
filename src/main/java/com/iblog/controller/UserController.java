package com.iblog.controller;

import com.iblog.bean.RespBean;
import com.iblog.bean.User;
import com.iblog.service.UserService;
import com.iblog.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author iblog
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @RequestMapping("/currentUserName")
    public String currentUserName() {
        com.iblog.bean.User u = Util.getCurrentUser();
        if (u == null) {
            log.debug("当前用户: 游客");
            return "游客";
        }
        String name = u.getNickname() != null ? u.getNickname() : u.getUsername();
        log.debug("当前用户名: {}", name);
        return name;
    }

    @RequestMapping("/currentUserId")
    public Long currentUserId() {
        com.iblog.bean.User u = Util.getCurrentUser();
        if (u == null) {
            return null;
        }
        Long id = u.getId();
        log.debug("当前用户ID: {}", id);
        return id;
    }

    @RequestMapping("/currentUserEmail")
    public String currentUserEmail() {
        com.iblog.bean.User u = Util.getCurrentUser();
        if (u == null) {
            return "";
        }
        String email = u.getEmail();
        log.debug("当前用户邮箱: {}", email);
        return email;
    }

    @RequestMapping("/currentUser")
    public User currentUser() {
        User user = Util.getCurrentUser();
        if (user == null) {
            User guest = new User();
            // 尽量避免返回 null，前端可以检查字段
            guest.setNickname("游客");
            log.debug("返回游客用户信息");
            return guest;
        }
        log.debug("当前用户完整信息: {}", Util.toJson(user));
        return user;
    }
    
    /**
     * session keepalive / heartbeat endpoint
     * 前端定时调用此接口以续期会话（更新 lastAccessedTime 或重设超时时间）
     */
    @RequestMapping(value = "/session/keepalive", method = RequestMethod.GET)
    public RespBean keepAlive(javax.servlet.http.HttpSession session) {
        if (session == null) {
            return new RespBean("error", "未认证");
        }
        // 触发 session 访问以续期；可选地重设超时时间（秒）
        try {
            // 这里不改变 maxInactiveInterval，默认容器会以最后访问时间为准续期
            session.setAttribute("lastKeepAlive", System.currentTimeMillis());
            return new RespBean("success", "续期成功");
        } catch (Exception e) {
            log.warn("keepAlive 处理异常", e);
            return new RespBean("error", "续期失败");
        }
    }

    @RequestMapping("/isAdmin")
    public Boolean isAdmin() {
        User u = Util.getCurrentUser();
        if (u == null) {
            log.debug("用户不是超级管理员（未登录）");
            return false;
        }
        List<GrantedAuthority> authorities = u.getAuthorities();
        for (Object authority : authorities) {
            if (authority.toString().contains("超级管理员")) {
                log.debug("用户是超级管理员");
                return true;
            }
        }
        log.debug("用户不是超级管理员");
        return false;
    }

    @RequestMapping(value = "/updateUserEmail",method = RequestMethod.PUT)
    public RespBean updateUserEmail(String email) {
        log.debug("更新用户邮箱调用: email={}", email);
        if (userService.updateUserEmail(email) == 1) {
            log.info("用户邮箱更新成功: email={}", email);
            return new RespBean("success", "开启成功!");
        }
        log.warn("用户邮箱更新失败: email={}", email);
        return new RespBean("error", "开启失败!");
    }
}
