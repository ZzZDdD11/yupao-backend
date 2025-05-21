package com.example.yupaobackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupaobackend.common.ErrorCode;
import com.example.yupaobackend.exception.BusinessException;
import com.example.yupaobackend.mapper.TeamMapper;
import com.example.yupaobackend.model.domain.Team;
import com.example.yupaobackend.model.domain.User;
import com.example.yupaobackend.model.domain.UserTeam;
import com.example.yupaobackend.model.enums.TeamStatusEnum;
import com.example.yupaobackend.service.TeamService;
import com.example.yupaobackend.service.UserTeamService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 假设你项目中有这个，或者替换为 StringUtils.hasText

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 检查参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        // 2.是否登陆
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = loginUser.getId();

        // 校验信息
        // 1. 队伍人数大于1 < 20
        // 注意：如果你的Team实体中maxNum是int原始类型，则team.getMaxNum()不可能为null，Optional.ofNullable意义不大
        // 如果是Integer类型，则Optional.ofNullable是合适的
        int maxNumVal = Optional.ofNullable(team.getMaxNum()).orElse(0); // 假设如果 DTO 中 maxNum 为 null，则默认为 0
        log.info("DEBUG: 从 DTO 获取的原始 team.getMaxNum(): {}", team.getMaxNum());
        log.info("DEBUG: 计算后的 maxNumVal 变量 (int): {}", maxNumVal);
        if (maxNumVal < 1 || maxNumVal > 20) {
            log.error("错误: 队伍人数不符合要求。maxNumVal: {}", maxNumVal);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求(1-20人)");
        }

        // 2. 队伍标题<20
        String name = team.getName();
        // 建议使用 !StringUtils.hasText(name) 来判断空或空白字符串
        if (name == null || name.trim().isEmpty() || name.length() > 20) {
            log.error("错误: 队伍名称不符合要求。name: '{}', length: {}", name, (name != null ? name.length() : "null"));
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不符合要求(1-20字符，且不能为空)");
        }

        // 3. 描述 <= 512
        String description = team.getDescription();
        if (description != null && description.length() > 512) {
            log.error("错误: 队伍描述过长。description length: {}", description.length());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长(最多512字符)");
        }

        // 4.status是否公开（int）不传默认为0
        Integer originalStatusFromDto = team.getStatus();
        log.info("DEBUG: 从 DTO 获取的原始 team.getStatus(): {}", originalStatusFromDto);

        int status = Optional.ofNullable(originalStatusFromDto).orElse(0);
        log.info("DEBUG: 计算后用于 getByValue 的 status 变量 (int): {}", status);

        // 直接检查枚举常量的值，确保它们没有在运行时被意外改变
        log.info("DEBUG: TeamStatusEnum.PUBLIC.getValue() 当前值: {}", TeamStatusEnum.PUBLIC.getValue());
        log.info("DEBUG: TeamStatusEnum.PRIVATE.getValue() 当前值: {}", TeamStatusEnum.PRIVATE.getValue());
        log.info("DEBUG: TeamStatusEnum.SECRET.getValue() 当前值: {}", TeamStatusEnum.SECRET.getValue());
        log.info("DEBUG: 所有 TeamStatusEnum 常量及其内部 value:");
        for (TeamStatusEnum enumVal : TeamStatusEnum.values()) {
            // 注意：之前日志格式字符串中的空格可能导致输出不美观，这里移除了 `\t` 样式的空格
            log.info("DEBUG:   {}({})", enumVal.name(), enumVal.getValue());
        }

        TeamStatusEnum statusEnum = TeamStatusEnum.getByValue(status); // 调用 getByValue
        log.info("DEBUG: TeamStatusEnum.getByValue({}) 返回结果: {}", status, statusEnum);

        if (statusEnum == null) { // 这是你说的第54行
            log.error("错误: statusEnum 为 null，即将抛出 BusinessException。输入 status: {}, DTO status: {}", status, originalStatusFromDto);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }

        // 5.如果status是加密状态， 一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            // 建议使用 !StringUtils.hasText(password)
            if (password == null || password.trim().isEmpty() || password.length() > 32) {
                log.error("错误: 加密队伍密码不符合要求。password: '{}'", (password == null ? "null" : "****")); // 不打印真实密码
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码(1-32位)");
            }
        }

        // 6. 超时时间大于当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime == null) {
            log.error("错误: 过期时间不能为空。");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不能为空");
        }
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate localExpireTime = expireTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        log.info("DEBUG: 当前日期: {}, 过期日期: {}", today, localExpireTime);

        if (localExpireTime.isBefore(today)) {
            log.error("错误: 过期时间必须晚于当前日期。expireTime: {}, today: {}", localExpireTime, today);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间必须晚于当前日期");
        }

        // 7. 用户最多创建 5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        log.info("DEBUG: 用户 {} 已有队伍数量: {}", userId, hasTeamNum);
        if (hasTeamNum >= 5) {
            log.error("错误: 用户 {} 创建队伍已达上限 ({})。", userId, hasTeamNum);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您创建的队伍数量已达上限");
        }

        // 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        // 确保Team实体中的status字段与statusEnum.getValue()的类型一致（Integer或int）
        // 如果Team实体中是Integer status, 用 team.setStatus(statusEnum.getValue());
        // 如果Team实体中是int status, 用 team.setStatus(statusEnum.getValue()); (这里可以不变，因为原始status已经是int了)
        // 考虑到team对象是传入的，如果它的status字段是Integer, 并且可能被Optional.ofNullable(originalStatusFromDto).orElse(0)影响
        // 最佳实践是明确地将从枚举获得的值设置回去
        team.setStatus(statusEnum.getValue()); // 明确设置从枚举获取的int值

        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            log.error("错误: 插入队伍信息失败。save result: {}, teamId: {}", result, teamId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        log.info("INFO: 队伍创建成功，teamId: {}", teamId);

        // 插入用户队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            log.error("错误: 插入用户队伍关系失败。teamId: {}, userId: {}", teamId, userId);
            // 由于@Transactional，此处的失败会导致上面的team表插入回滚
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败（关联用户失败）");
        }
        log.info("INFO: 用户队伍关系创建成功。teamId: {}, userId: {}", teamId, userId);
        return teamId;
    }
}