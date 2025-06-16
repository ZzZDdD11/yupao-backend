package com.example.yupaobackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupaobackend.common.ErrorCode;
import com.example.yupaobackend.exception.BusinessException;
import com.example.yupaobackend.mapper.TeamMapper;
import com.example.yupaobackend.model.domain.Team;
import com.example.yupaobackend.model.domain.User;
import com.example.yupaobackend.model.domain.UserTeam;
import com.example.yupaobackend.model.dto.TeamQuery;
import com.example.yupaobackend.model.enums.TeamStatusEnum;
import com.example.yupaobackend.model.vo.TeamUserVo;
import com.example.yupaobackend.model.vo.UserVo;
import com.example.yupaobackend.service.TeamService;
import com.example.yupaobackend.service.UserService;
import com.example.yupaobackend.service.UserTeamService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;

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

    /**
     * 搜索队伍
     * @param teamQuery
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        // 组合查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        // 标记是否尝试过根据特定状态进行查询
        boolean specificStatusQueryAttempted = false;

        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }

            // 建议点 1: 审视 searchText 与特定 name/description 的查询逻辑
            // 当前逻辑是将 searchText 的结果（name 或 description 匹配）与特定 name 和 description 的 LIKE 查询结果进行 AND 操作。
            // 这可能会导致查询条件过于严格。需要考虑 searchText 是否应该作为独立的查询条件，
            // 或者明确这些字段之间如何交互。
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(wrapper -> wrapper.like("name", searchText).or().like("description", searchText));
            }

            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }

            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }

            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }

            Long userId = teamQuery.getUserId(); // 假设这是队伍创建者的ID
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }

            // 问题 1 (也是 问题 3 的一部分): 状态查询与授权逻辑
            Integer statusInput = teamQuery.getStatus();
            if (statusInput != null) { // 如果查询中明确指定了状态
                specificStatusQueryAttempted = true;
                TeamStatusEnum statusEnum = TeamStatusEnum.getByValue(statusInput);

                if (statusEnum == null) { // 如果提供的状态值无效
                    if (!isAdmin) {
                        // 非管理员尝试使用无效状态码（这个状态码肯定不是 PUBLIC）进行查询
                        throw new BusinessException(ErrorCode.NO_AUTH, "查询状态无效或无权限");
                    }
                    // 管理员使用无效状态查询；查询会继续，但很可能找不到结果。
                    queryWrapper.eq("status", statusInput);
                } else { // 如果提供的状态值有效
                    if (!isAdmin && !TeamStatusEnum.PUBLIC.equals(statusEnum)) {
                        throw new BusinessException(ErrorCode.NO_AUTH, "非管理员用户只能查询公开队伍");
                    }
                    queryWrapper.eq("status", statusEnum.getValue());
                }
            }
            // 如果 statusInput 为 null，则不会从 teamQuery.getStatus() 应用特定的状态过滤器。
            // 针对非管理员的通用规则将在该代码块之后应用。
        }

        // 如果没有通过 teamQuery.getStatus() 查询特定状态，或者 teamQuery 本身为 null，
        // 则为非管理员应用默认的状态过滤器。
        // 这是 问题 3 (安全隐患) 的核心修复：确保非管理员总是受限于公开队伍。
        if (!isAdmin && !specificStatusQueryAttempted) {
            queryWrapper.eq("status", TeamStatusEnum.PUBLIC.getValue());
        }

        // 不展示已过期的队伍 (这个过滤条件很好)
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if (teamList == null) { // 防御性检查，虽然 this.list 通常返回空列表而不是null
            log.info("Query returned null for teamList.");
            return new ArrayList<>();
        }
        log.info("Fetched teamList size: {}", teamList.size()); // <--- 关键日志点 1

        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        // 问题 2: N+1 查询问题
        // userService.getById(userId) 在循环中被调用。
        // 首先收集所有用户ID
        List<Long> creatorUserIds = teamList.stream()
                .map(Team::getUserId) // 获取每个队伍的创建者ID
                .filter(Objects::nonNull) // 过滤掉null的ID
                .distinct() // 去重
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(creatorUserIds)) {
            // 假设 userService 有类似 listByIds 的方法，或者你可以适配一个。
            // 这样可以在一次数据库查询中获取所有需要的用户。
            List<User> users = userService.listByIds(creatorUserIds);
            if (users != null) { // 防御性编程，确保 users 列表不为 null
                userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            }
        }

        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long creatorUserId = team.getUserId(); // 为了清晰，重命名了你的 'userId' 变量

            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo); // 复制队伍基本信息

            if (creatorUserId != null) {
                User user = userMap.get(creatorUserId); // 从预先获取的 map 中获取用户
                if (user != null) { // 检查用户是否存在
                    User safetyUser = userService.getSafetyUser(user); // 用户信息脱敏
                    if (safetyUser != null) { // 确保脱敏后的用户不为null
                        UserVo userVo = new UserVo();
                        BeanUtils.copyProperties(safetyUser, userVo); // 复制脱敏后的用户信息
                        teamUserVo.setCreateUser(userVo); // 设置创建者信息
                    }
                }
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    }
