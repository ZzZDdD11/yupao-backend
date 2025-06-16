// Java
package com.example.yupaobackend.service;

import com.example.yupaobackend.model.dto.TeamQuery;
import com.example.yupaobackend.model.vo.TeamUserVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
public class TeamlistTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Test
    public void testListTeamsWithEmptyQuery() {
        TeamQuery teamQuery = new TeamQuery();
        teamQuery.setName("name");
        teamQuery.setStatus(0);
        // 模拟非管理员场景，可根据需要mock userService
        boolean isAdmin = true;
        List<TeamUserVo> result = teamService.listTeams(teamQuery, isAdmin);
        // 如果数据表中没有队伍记录，返回空列表是正常的
        assertNotNull(result);
        // 根据实际业务逻辑可以进一步断言结果
    }
}