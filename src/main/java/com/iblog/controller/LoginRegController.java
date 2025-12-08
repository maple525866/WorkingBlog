package com.iblog.controller;

import com.iblog.bean.RespBean;
import com.iblog.bean.User;
import com.iblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author iblog
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginRegController {

    private final UserService userService;

    @RequestMapping("/login_error")
    public RespBean loginError() {
        log.warn("登录失败返回 /login_error");
        return new RespBean("error", "登录失败!");
    }

    @RequestMapping("/login_success")
    public RespBean loginSuccess() {
        log.info("登录成功返回 /login_success");
        return new RespBean("success", "登录成功!");
    }

    /**
     * 如果自动跳转到这个页面，说明用户未登录，返回相应的提示即可
     * 如果要支持表单登录，可以在这个方法中判断请求的类型，进而决定返回JSON还是HTML页面
     * @return RespBean
     */
    @RequestMapping("/login_page")
    public RespBean loginPage() {
        log.debug("未登录访问 /login_page");
        return new RespBean("error", "尚未登录，请登录!");
    }

    @RequestMapping("/reg")
    public RespBean reg(User user) {
        log.debug("注册请求: {}", user == null ? "null" : user.getUsername());
        int result = userService.reg(user);
        if (result == 0) {
            //成功
            log.info("注册成功: {}", user != null ? user.getUsername() : null);
            return new RespBean("success", "注册成功!");
        } else if (result == 1) {
            log.warn("注册失败 - 用户名重复: {}", user != null ? user.getUsername() : null);
            return new RespBean("error", "用户名重复，注册失败!");
        } else {
            //失败
            log.error("注册异常失败: {}", user != null ? user.getUsername() : null);
            return new RespBean("error", "注册失败!");
        }
    }
}
