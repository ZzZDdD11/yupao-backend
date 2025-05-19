package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupaobackend.model.domain.UserTeam;
import generator.service.UserTeamService;
import com.example.yupaobackend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author hao
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-05-19 16:14:45
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




