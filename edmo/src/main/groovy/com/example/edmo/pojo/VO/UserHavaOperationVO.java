package com.example.edmo.pojo.VO;

import com.example.edmo.pojo.entity.OperationLog;
import com.example.edmo.pojo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHavaOperationVO {
    private Integer id;

    private String name;

    private List<OperationLog> operationLogs;

    public  UserHavaOperationVO(User user, List<OperationLog> operationLogs) {
        id = user.getId();
        name = user.getName();
        this.operationLogs = operationLogs;
    }
}
