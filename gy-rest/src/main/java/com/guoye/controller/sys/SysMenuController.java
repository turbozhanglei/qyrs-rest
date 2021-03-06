package com.guoye.controller.sys;

import com.guoye.base.BizAction;
import com.guoye.util.*;
import org.bouncycastle.jcajce.provider.symmetric.DES;
import org.g4studio.core.metatype.Dto;
import org.g4studio.core.metatype.impl.BaseDto;
import org.g4studio.core.resource.util.StringUtils;
import org.g4studio.core.web.util.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单控制器
 *
 * @author Louis
 * @date Oct 29, 2018
 */
@RestController
@RequestMapping("menu")
public class SysMenuController extends BizAction {


    @RequestMapping(value = "/findNavTree")
    public BaseResult findNavTree(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();
        try {
            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);

            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }

            Long userid = member.getAsLong("id");

            List<Dto> resultList = findByUser(userid);

            resultList.sort((o1, o2) -> o1.getAsInteger("id").compareTo(o2.getAsInteger("id")));

            result.setData(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }


    @RequestMapping(value = "/editUser")
    public BaseResult editUser(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {

            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);

            DESWrapper Des = new DESWrapper();
            String password = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";
            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }
            Dto user=new BaseDto();
            user.put("mobile",Des.encrypt(dto.getAsString("mobile"),password));
            user.put("identity_type",dto.getAsString("identity_type"));
            String id = dto.getAsString("id");
            dto.put("tableName", "sysUser");
            //判断手机号码是否重复
            Dto mobile=(BaseDto)bizService.queryForDto("sysUser.getInfo",user);
            //判断用户名是否重复
            Dto username=(BaseDto)bizService.queryForDto("sysUser.getInfo",new BaseDto("username",dto.getAsString("username")));

            Dto udto=(BaseDto)bizService.queryForDto("sysUser.getInfo",new BaseDto("id",dto.getAsString("id")));

            if (StringUtils.isNotEmpty(id)) {
                // 修改
                if (Des.decrypt(udto.getAsString("mobile"),password).equals(dto.getAsString("mobile"))==false){
                    Dto mem=new BaseDto();
                    mem.put("mobile",Des.encrypt(dto.getAsString("mobile"),password));
                    mem.put("identity_type",dto.getAsString("identity_type"));
                    Dto mobileup=(BaseDto)bizService.queryForDto("sysUser.getInfo",mem);
                    if (null !=mobileup){
                        throw new Exception("手机号码重复，请重试");
                    }

                }else if(udto.getAsString("username").equals(dto.getAsString("username"))==false){
                    Dto usernameup=(BaseDto)bizService.queryForDto("sysUser.getInfo",new BaseDto("username",dto.getAsString("username")));
                    if(null !=usernameup){
                        throw new Exception("用户名已被注册，请重试");
                    }
                }

                dto.put("updator", member == null ? "" : member.get("id"));
                String mob=dto.getAsString("mobile");
                dto.put("mobile",Des.encrypt(mob,password));//加密后手机号
                bizService.updateInfo(dto);
            } else {
                if (null !=mobile){
                    throw new Exception("手机号码重复，请重试");
                }else if(null !=username){
                    throw new Exception("用户名已被注册，请重试");
                }
                //插入
                dto.put("creator", member == null ? "" : member.get("id"));
                dto.put("updator", member == null ? "" : member.get("id"));
                String mob=dto.getAsString("mobile");
                //Des.encrypt(mob,password);
                dto.put("mobile",Des.encrypt(mob,password));//加密后手机号

                bizService.saveInfo(dto);
            }


            if (StringUtils.isNotEmpty(dto.getAsString("id"))) {
                Dto delete = new BaseDto();
                delete.put("tableName", "sysRoleUser");
                delete.put("method", "deleteInfo");
                delete.put("userid", dto.getAsString("id"));
                bizService.delete(delete);

                String userRoles = dto.getAsString("userRoles");
                if (StringUtils.isNotEmpty(userRoles)) {
                    for (String roleId : userRoles.split(",")) {
                        if (StringUtils.isNotEmpty(roleId)) {

                            Dto insert = new BaseDto();
                            insert.put("roleid", roleId);
                            insert.put("userid", dto.getAsString("id"));
                            insert.put("tableName", "sysRoleUser");
                            bizService.saveInfo(insert);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }


    @RequestMapping(value = "/findDeptTree")
    public BaseResult findDeptTree(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {
            List<Dto> sysDepts = new ArrayList<>();

            List<Dto> depts = bizService.queryForList("sysDept.queryList", dto);
            for (Dto dept : depts) {
                if (dept.getAsInteger("pid") == null || dept.getAsInteger("pid") == 0) {
                    dept.put("level", 0);
                    sysDepts.add(dept);
                }
            }
            findChildren(sysDepts, depts);

            depts.sort((o1, o2) -> o1.getAsInteger("sort").compareTo(o2.getAsInteger("sort")));

            result.setData(sysDepts);
        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }

    @RequestMapping(value = "/selectDeptTree")
    public BaseResult selectDeptTree(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {
            if(!StringUtils.isNotEmpty(dto.getAsString("dept_name"))){
                dto.put("dept_name","");
            }
            List<Dto> depts = bizService.queryForList("sysDept.queryDeptList", dto);
            Set<String> parentList = new HashSet<>();
            depts.forEach(element -> {
                String parent = element.getAsString("parentList");
                if (StringUtils.isNotEmpty(parent)) {
                    parentList.addAll(Arrays.asList(parent.split(",")));
                }
            });
            result.setData(parentList);
        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }


    private void findChildren(List<Dto> sysDepts, List<Dto> depts) {
        for (Dto sysDept : sysDepts) {
            List<Dto> children = new ArrayList<>();
            for (Dto dept : depts) {
                if (sysDept.getAsInteger("id") != null && sysDept.getAsInteger("id").equals(dept.getAsInteger("pid"))) {
                    dept.put("parentName", sysDept.getAsString("dept_name"));
                    dept.put("level", sysDept.getAsInteger("level") + 1);
                    children.add(dept);
                }
            }
            sysDept.put("children", children);
            children.sort((o1, o2) -> o1.getAsInteger("sort").compareTo(o2.getAsInteger("sort")));
            findChildren(children, depts);
        }
    }


    /**
     * 获取按钮信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/findPermissions")
    public BaseResult findPermissions(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();
        try {
            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);

            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }

            String userid = member.getAsString("id");

            //获取角色id
            List<Dto> roleList = bizService.queryForList("sysMenu.getListByRole", new BaseDto("userid", userid));
            if (roleList.isEmpty()) {
                result.setCode(StatusConstant.CODE_9999);
                result.setMsg("请联系管理员配置权限");
                return result;
            }

            Set<String> perms = new HashSet<>();
            for (Dto sysMenu : roleList) {
                if (StringUtils.isNotEmpty(sysMenu.getAsString("perms"))) {
                    String[] permsList = sysMenu.getAsString("perms").split(",");
                    perms.addAll(Arrays.asList(permsList));
                }
            }

            result.setData(perms);
        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }


    /**
     * 根据角色id获取菜单列表
     *
     * @param userId
     * @return
     */
    public List<Dto> findByUser(Long userId) {
        List<Dto> menus = new ArrayList<>();
        menus = bizService.queryForList("sysMenu.queryList", new BaseDto());
        List<Dto> sysMenus = new ArrayList<>();

        for (Dto menu : menus) {
            //遍历集合
            if (menu.getAsInteger("pid") == null || menu.getAsInteger("pid") == 0) {
                menu.put("level", 0);
                if (!exists(sysMenus, menu) && menu.getAsString("delete_flag").equals("0")) {
                    sysMenus.add(menu);
                }
            }
        }

        findChildren(sysMenus, menus, 1);

        return sysMenus;
    }

    private void findChildren(List<Dto> SysMenus, List<Dto> menus, int menuType) {
        for (Dto SysMenu : SysMenus) {
            List<Dto> children = new ArrayList<>();
            for (Dto menu : menus) {
                if (SysMenu.getAsInteger("id") != null && SysMenu.getAsInteger("id").equals(menu.getAsInteger("pid"))) {
                    menu.put("parentName", SysMenu.getAsString("name"));
                    menu.put("level", SysMenu.getAsInteger("level") + 1);
                    if (!exists(children, menu)) {
                        children.add(menu);
                    }
                }
            }
            SysMenu.put("children", children);
            children.sort((o1, o2) -> o1.getAsInteger("id").compareTo(o2.getAsInteger("id")));
            findChildren(children, menus, menuType);
        }
    }

    /**
     * 判断当前权限是否添加过
     *
     * @param sysMenus
     * @param sysMenu
     * @return
     */
    private boolean exists(List<Dto> sysMenus, Dto sysMenu) {
        boolean exist = false;
        for (Dto menu : sysMenus) {
            if (menu.getAsString("id").equals(sysMenu.getAsString("id"))) {
                exist = true;
            }
        }
        return exist;
    }

    @RequestMapping(value = "/editRole")
    public BaseResult editRole(HttpServletRequest request) {
        Dto dto = WebUtils.getParamAsDto(request);
        BaseResult result = new BaseResult();

        try {

            Dto member = redisService.getObject(dto.getAsString("token"), BaseDto.class);

            if (null == member) {
                result.setCode(StatusConstant.CODE_4000);
                result.setMsg("请登录");
                return result;
            }


            String id = dto.getAsString("id");
            dto.put("tableName", "sysRole");


            if (StringUtils.isNotEmpty(id)) {
                //更新
                dto.put("updator", member == null ? "" : member.get("id"));
                bizService.updateInfo(dto);
            } else {
                //插入
                dto.put("creator", member == null ? "" : member.get("id"));
                dto.put("updator", member == null ? "" : member.get("id"));
                bizService.saveInfo(dto);
            }

            String deptid = member.getAsString("deptid");
            if (StringUtils.isNotEmpty(deptid)) {

                Dto delete = new BaseDto();
                delete.put("tableName", "sysRole");
                delete.put("method", "deleteInfo");
                delete.put("role_id", dto.getAsString("id"));
                bizService.delete(delete);

                Dto parent = (Dto) bizService.queryForDto("sysRole.getFirstParent", new BaseDto("dept_id", deptid));

                Dto insert = new BaseDto();
                insert.put("method", "saveSysRoleDept");
                insert.put("tableName", "sysRole");
                insert.put("dept_id", parent.getAsString("deptid"));
                insert.put("role_id", dto.getAsString("id"));
                insert.put("creator", member == null ? "" : member.get("id"));
                insert.put("updator", member == null ? "" : member.get("id"));
                bizService.save(insert);

            }


        } catch (Exception e) {
            e.printStackTrace();
            result = reduceErr(e.getLocalizedMessage());
        }
        return result;
    }

//    public static void main(String[] args) {
//        //待加密内容
//     String str = "66666666";
//     //密码，长度要是8的倍数
//     String password = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456";
//     DESWrapper Des = new DESWrapper();
//     String result = Des.encrypt(str,password);
//     System.out.println("加密后："+result);
//     //直接将如上内容解密
//     try {
//         String decryResult = Des.decrypt(result, password);
//             System.out.println("解密后："+new String(decryResult));
//     } catch (Exception e1) {
//             e1.printStackTrace();
//     }
//    }
}
