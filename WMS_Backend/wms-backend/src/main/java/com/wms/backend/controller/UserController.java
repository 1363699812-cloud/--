// src/main/java/com/wms/backend/controller/UserController.java
package com.wms.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.backend.annotation.RequireRole;
import com.wms.backend.common.Result;
import com.wms.backend.dto.LoginDTO;
import com.wms.backend.entity.User;
import com.wms.backend.service.IUserService;
import com.wms.backend.utils.JwtUtils;
import com.wms.backend.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDTO.getUsername());
        User user = userService.getOne(wrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        // 生成Token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());

        return Result.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 设置默认角色和状态
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("warehouse_keeper");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        boolean saved = userService.save(user);
        if (saved) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/list")
    @RequireRole({"admin"})
    public Result list(@RequestParam(defaultValue = "1") Integer current,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String username,
                       @RequestParam(required = false) String email) {
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        if (email != null && !email.isEmpty()) {
            wrapper.like("email", email);
        }

        Page<User> result = userService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 创建新用户（管理员）
     */
    @PostMapping
    @RequireRole({"admin"})
    public Result createUser(@RequestBody User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 设置默认密码（如果未提供）
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456"); // 默认密码
        }

        boolean saved = userService.save(user);
        if (saved) {
            return Result.success("用户创建成功");
        }
        return Result.error("用户创建失败");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @RequireRole({"admin"})
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);

        // 检查用户名是否与其他用户冲突
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername()).ne("id", id);
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        boolean updated = userService.updateById(user);
        if (updated) {
            return Result.success("用户更新成功");
        }
        return Result.error("用户更新失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RequireRole({"admin"})
    public Result deleteUser(@PathVariable Long id) {
        boolean deleted = userService.removeById(id);
        if (deleted) {
            return Result.success("用户删除成功");
        }
        return Result.error("用户删除失败");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @RequireRole({"admin"})
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean deleted = userService.removeBatchByIds(ids);
        if (deleted) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }
}