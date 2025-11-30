package com.iblog.mapper;

import com.iblog.bean.Role;
import com.iblog.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maple
 * @Description
 * @createTime:2025-12-01 00:08
 */
@Mapper
public interface UserMapper {
    User loadUserByUsername(@Param("username") String username);

    long reg(User user);

    int updateUserEmail(@Param("email") String email, @Param("id") Long id);

    List<User> getUserByNickname(@Param("nickname") String nickname);

    List<Role> getAllRole();

    int updateUserEnabled(@Param("enabled") Boolean enabled, @Param("uid") Long uid);

    int deleteUserById(Long uid);

    int deleteUserRolesByUid(Long id);

    int setUserRoles(@Param("rids") List<Long> rids, @Param("id") Long id);

    User getUserById(@Param("id") Long id);
}
