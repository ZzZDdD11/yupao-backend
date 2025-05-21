package com.example.yupaobackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yupaobackend.model.domain.Team;
import com.example.yupaobackend.model.domain.User;

/**
* @author hao
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-05-19 16:08:08
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
