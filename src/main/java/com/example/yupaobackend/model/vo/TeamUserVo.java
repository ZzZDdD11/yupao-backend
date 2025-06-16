package com.example.yupaobackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类
 */

@Data
public class TeamUserVo {
        /**
         * id
         */
        @TableId(type = IdType.AUTO)
        private Long id;

        /**
         * 队伍名称
         */
        private String name;

        /**
         * 描述
         */
        private String description;

        /**
         * 最大人数
         */
        private Integer maxNum;

        /**
         * 过期时间
         */
        private Date expireTime;

        /**
         * 用户id
         */
        private Long userId;

        /**
         * 0 - 公开，1 - 私有，2 - 加密
         */
        private Integer status;

        /**
         * 创建时间
         */
        private Date createTime;

        /**
         * 更新时间
         */
        private Date updateTime;

        /**
         * 是否删除
         */
        @TableLogic
        private Integer isDelete;

        /**
         * 创建人  用户列表
         */
        UserVo createUser;
}
