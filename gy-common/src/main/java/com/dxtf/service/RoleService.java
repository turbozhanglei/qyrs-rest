package com.dxtf.service;

import com.dxtf.util.BaseResult;
import org.g4studio.core.metatype.Dto;

public interface RoleService {

    public BaseResult deleteRoleByRoleId(Dto dto, Long memberId) throws  Exception;
    public BaseResult saveRoleMenus(Dto dto) throws  Exception;
}
