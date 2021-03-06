package com.gy.resource.request.rest;

import com.gy.resource.request.TokenRequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: gaolanyu
 * @date: 2020-02-14
 * @remark:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest extends TokenRequest {

    private static final long serialVersionUID = 6088214497383157656L;

    @ApiModelProperty(notes = "登录的用户id")
    private String loginUserId;

    private Integer start = 1;
    private Integer limit = 10;
}
