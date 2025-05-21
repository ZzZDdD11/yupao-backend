package com.example.yupaobackend.model.enums;

/**
 * 队伍状态枚举
 */
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private final int value; // 声明为 final
    private final String text; // 声明为 final

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据传入的 Integer 值获取对应的枚举常量
     * @param value 状态的整数值
     * @return 对应的 TeamStatusEnum，如果找不到则返回 null
     */
    public static TeamStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            // 比较 int == Integer 时，Integer 会自动拆箱为 int
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}