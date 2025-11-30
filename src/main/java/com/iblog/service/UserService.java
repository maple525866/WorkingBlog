package com.iblog.service;

import com.iblog.bean.Role;
import com.iblog.bean.User;
import com.iblog.mapper.UserMapper;
import com.iblog.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author maple
 * @Description
 * @createTime:2025-12-01 00:13
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    private final UserMapper userMapper;
    private final RolesMapper rolesMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        log.debug("loadUserByUsername: {}", s);
        User user = userMapper.loadUserByUsername(s);
        if (user == null) {
            log.warn("未找到用户: {}", s);
            //避免返回null，这里返回一个不含有任何值的User对象，在后期的密码比对过程中一样会验证失败
            return new User();
        }
        //查询用户的角色信息，并返回存入user中
        List<Role> roles = rolesMapper.getRolesByUid(user.getId());
        user.setRoles(roles);
        log.debug("loadUserByUsername 返回 user={}", Util.toJson(user));
        return user;
    }

    /**
     * @param user 注册用户
     * @return 0表示成功
     * 1表示用户名重复
     * 2表示失败
     */
    public int reg(User user) {
        log.debug("reg called: {}", Util.toJson(user));
        User loadUserByUsername = userMapper.loadUserByUsername(user.getUsername());
        if (loadUserByUsername != null) {
            log.warn("注册失败，用户名已存在: {}", user.getUsername());
            return 1;
        }
        //插入用户,插入之前先对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //用户可用
        user.setEnabled(true);
        user.setRegTime(LocalDateTime.now());
        long result = userMapper.reg(user);
        //配置用户的角色，默认都是普通用户
        String[] roles = new String[]{"2"};
        int i = rolesMapper.addRoles(roles, user.getId());
        boolean b = i == roles.length && result == 1;
        if (b) {
            log.info("注册成功: {}", user.getUsername());
            return 0;
        } else {
            log.error("注册失败: {}", user.getUsername());
            return 2;
        }
    }

    /**
     * 管理员添加用户并可指定角色
     * @param user 要添加的用户
     * @param rids 角色 id 列表（可为 null 表示使用默认角色）
     * @return 0 成功, 1 用户名重复, 2 失败
     */
    public int adminAddUser(User user, List<Long> rids) {
        log.debug("adminAddUser called: {} rids={}", Util.toJson(user), Util.toJson(rids));
        User loadUserByUsername = userMapper.loadUserByUsername(user.getUsername());
        if (loadUserByUsername != null) {
            log.warn("添加用户失败，用户名已存在: {}", user.getUsername());
            return 1;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRegTime(LocalDateTime.now());
        long result = userMapper.reg(user);
        // 如果管理员指定了角色，则使用指定角色，否则使用默认普通用户角色
        String[] rolesArr;
        if (rids != null && !rids.isEmpty()) {
            rolesArr = new String[rids.size()];
            for (int i = 0; i < rids.size(); i++) {
                rolesArr[i] = String.valueOf(rids.get(i));
            }
        } else {
            rolesArr = new String[]{"2"};
        }
        int i = rolesMapper.addRoles(rolesArr, user.getId());
        boolean ok = i == rolesArr.length && result == 1;
        if (ok) {
            log.info("管理员添加用户成功: {}", user.getUsername());
            return 0;
        } else {
            log.error("管理员添加用户失败: {}", user.getUsername());
            return 2;
        }
    }

    public int updateUserEmail(String email) {
        log.debug("updateUserEmail: email={}, uid={}", email, Util.getCurrentUser().getId());
        return userMapper.updateUserEmail(email, Util.getCurrentUser().getId());
    }

    public List<User> getUserByNickname(String nickname) {
        log.debug("getUserByNickname: {}", nickname);
        return userMapper.getUserByNickname(nickname);
    }

    public List<Role> getAllRole() {
        log.debug("getAllRole called");
        return userMapper.getAllRole();
    }

    public int updateUserEnabled(Boolean enabled, Long uid) {
        log.debug("updateUserEnabled: enabled={}, uid={}", enabled, uid);
        return userMapper.updateUserEnabled(enabled, uid);
    }

    public int deleteUserById(Long uid) {
        log.debug("deleteUserById: uid={}", uid);
        return userMapper.deleteUserById(uid);
    }

    public int updateUserRoles(List<Long> rids, Long id) {
        log.debug("updateUserRoles: rids={}, id={}", Util.toJson(rids), id);
        userMapper.deleteUserRolesByUid(id);
        return userMapper.setUserRoles(rids, id);
    }

    public User getUserById(Long id) {
        log.debug("getUserById: id={}", id);
        return userMapper.getUserById(id);
    }
}
