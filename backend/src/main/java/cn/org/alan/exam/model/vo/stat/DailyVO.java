package cn.org.alan.exam.model.vo.stat;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;


@Data
public class DailyVO {
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 登录日期
     */
    private LocalDate loginDate;

    /**
     * 累积在线秒数
     */
    private Integer totalSeconds;
}
