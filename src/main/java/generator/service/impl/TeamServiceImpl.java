package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yupaobackend.model.domain.Team;
import generator.service.TeamService;
import com.example.yupaobackend.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author hao
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-05-19 16:08:08
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




