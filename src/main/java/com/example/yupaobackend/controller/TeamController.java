package com.example.yupaobackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.yupaobackend.common.BaseResponse;
import com.example.yupaobackend.common.ErrorCode;
import com.example.yupaobackend.common.ResultUtils;
import com.example.yupaobackend.exception.BusinessException;
import com.example.yupaobackend.model.domain.Team;
import com.example.yupaobackend.model.domain.User;
import com.example.yupaobackend.model.request.UserLoginRequest;
import com.example.yupaobackend.model.request.UserRegisterRequest;
import com.example.yupaobackend.service.TeamService;
import com.example.yupaobackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.yupaobackend.contant.UserConstant.ADMIN_ROLE;
import static com.example.yupaobackend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody Team team) {
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean save = teamService.save(team);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入失败");
        }
        return ResultUtils.success(team.getId());

    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id) {
        if (id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.removeById(id);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);

    }

}
