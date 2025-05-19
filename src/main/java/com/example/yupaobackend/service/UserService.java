package com.example.yupaobackend.service;

import com.example.yupaobackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.example.yupaobackend.contant.UserConstant.ADMIN_ROLE;
import static com.example.yupaobackend.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author Administrator
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-03-03 09:21:20
*/
public interface UserService extends IService<User> {


    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param
     * @return
     */

    List<User> searchUserByTags(List<String> tagList);

    /**
     * 更新用户信息
     * @return
     */
    Integer updateUser(User user, User loginUser);

    /**
     * 获取当前登陆用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 是否为管理员
     * @param request
     * @return
     */

     boolean isAdmin(HttpServletRequest request);
    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);
}
