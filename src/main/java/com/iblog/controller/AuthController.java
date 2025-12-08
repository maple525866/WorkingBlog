package com.iblog.controller;

import com.iblog.bean.RespBean;
import com.iblog.bean.User;
import com.iblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * REST style authentication endpoints (login/register) so frontend can call clear API paths
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public RespBean login(HttpServletRequest request, User loginUser) {
        String username = loginUser.getUsername();
        String password = loginUser.getPassword();
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            // ensure session created so browser receives JSESSIONID
            request.getSession(true);
            log.info("用户登录成功: {}", username);
            return new RespBean("success", "登录成功");
        } catch (Exception e) {
            log.warn("登录失败: {}", username);
            return new RespBean("error", "登录失败");
        }
    }

    @PostMapping("/register")
    public RespBean register(User user) {
        log.debug("注册请求(api): {}", user == null ? "null" : user.getUsername());
        int result = userService.reg(user);
        if (result == 0) {
            log.info("注册成功(api): {}", user != null ? user.getUsername() : null);
            return new RespBean("success", "注册成功!");
        } else if (result == 1) {
            log.warn("注册失败 - 用户名重复(api): {}", user != null ? user.getUsername() : null);
            return new RespBean("error", "用户名重复，注册失败!");
        } else {
            log.error("注册异常失败(api): {}", user != null ? user.getUsername() : null);
            return new RespBean("error", "注册失败!");
        }
    }

    @RequestMapping("/logout")
    public RespBean logout(javax.servlet.http.HttpServletRequest request) {
        try {
            javax.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            return new RespBean("success", "注销成功");
        } catch (Exception e) {
            log.warn("logout error", e);
            return new RespBean("error", "注销失败");
        }
    }
}
