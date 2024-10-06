package org.dromara.workflow.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 减签请求对象
 *
 * @author may
 */
@Data
public class ReductionSignatureBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 减签人id
     */
    @NotNull(message = "减签id不能为空", groups = {AddGroup.class})
    private List<String> userIds;

    /**
     * 任务id
     */
    @NotNull(message = "任务id不能为空", groups = {AddGroup.class})
    private Long taskId;

    /**
     * 意见
     */
    private String message;
}