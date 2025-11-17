package com.iblog.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iblog.bean.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author maple
 */
public class Util {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) return null;
        if (principal instanceof User) {
            return (User) principal;
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails ud = (org.springframework.security.core.userdetails.UserDetails) principal;
            User u = new User();
            u.setUsername(ud.getUsername());
            u.setNickname(ud.getUsername());
            return u;
        }
        return null;
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "<json 错误: " + e.getMessage() + ">";
        }
    }
}
