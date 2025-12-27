package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.RepoMapper;
import cn.org.alan.exam.model.entity.Category;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.repo.RepoVO;
import cn.org.alan.exam.service.ICategoryService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 题库管理服务单元测试
 * 对应源文件: RepoServiceImpl.java
 */
@ExtendWith(MockitoExtension.class)
class RepoServiceImplTest {

    @InjectMocks
    private RepoServiceImpl repoService;

    @Mock private RepoMapper repoMapper;
    @Mock private QuestionMapper questionMapper;
    @Mock private ICategoryService categoryService;

    /**
     * 测试点: 添加题库
     * 验证: 在插入前是否检查了分类是否存在
     */
    @Test
    @DisplayName("测试添加题库(addRepo)")
    void testAddRepo() {
        // 1. 准备数据
        Repo repo = new Repo();
        repo.setTitle("Java基础题库");
        repo.setCategoryId(10);

        // 2. 模拟分类存在
        when(categoryService.getById(10)).thenReturn(new Category());
        
        // 3. 模拟插入成功
        when(repoMapper.insert(repo)).thenReturn(1);

        // 4. 执行
        Result<String> result = repoService.addRepo(repo);

        // 5. 验证
        // Assertions.assertEquals(1, result.getCode());
        Assertions.assertEquals(1, result.getCode().intValue());
        verify(categoryService).getById(10); // 验证检查了分类
        verify(repoMapper).insert(repo);     // 验证执行了插入
    }

    /**
     * 测试点: 删除题库
     * 验证: 必须先清空该题库下所有试题的 repoId (级联更新)，然后再删除题库
     */
    @Test
    @DisplayName("测试删除题库(deleteRepoById)")
    void testDeleteRepoById() {
        // 1. 模拟清空题目的操作 (update question set repo_id = null where repo_id = ?)
        when(questionMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(5); // 假设更新了5道题

        // 2. 模拟删除题库操作 (MyBatis-Plus 的 ServiceImpl removeById)
        // 注意：repoService.removeById 是父类方法，直接 mock repoMapper.deleteById 更底层
        when(repoMapper.deleteById(100)).thenReturn(1);

        // 3. 执行
        Result<String> result = repoService.deleteRepoById(100);

        // 4. 验证级联逻辑
        verify(questionMapper).update(any(), any(LambdaUpdateWrapper.class)); // 验证先更新了题目
        verify(repoMapper).deleteById(100); // 验证后删除了题库
        Assertions.assertEquals("删除题库成功", result.getMsg());
    }

    /**
     * 测试点: 分页查询题库
     * 验证: 是否正确填充了“分类名称”和“题目数量”
     */
    @Test
    @DisplayName("测试分页查询题库(pagingRepo)")
    void testPagingRepo() {
        try (MockedStatic<SecurityUtil> securityMock = Mockito.mockStatic(SecurityUtil.class)) {
            securityMock.when(SecurityUtil::getRoleCode).thenReturn(1); // 管理员
            securityMock.when(SecurityUtil::getUserId).thenReturn(1001);

            // 1. 模拟 Mapper 返回的分页数据
            IPage<RepoVO> mockPage = new Page<>();
            List<RepoVO> records = new ArrayList<>();
            RepoVO vo = new RepoVO();
            vo.setId(1);
            vo.setCategoryId(10);
            records.add(vo);
            mockPage.setRecords(records);

            when(repoMapper.pagingRepo(any(), any(), anyInt(), any())).thenReturn(mockPage);

            // 2. 模拟查询分类名称
            Category category = new Category();
            category.setName("后端技术");
            when(categoryService.getById(10)).thenReturn(category);

            // 3. 模拟查询题目数量
            when(questionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L);

            // 4. 执行
            Result<IPage<RepoVO>> result = repoService.pagingRepo(1, 10, null, null);

            // 5. 验证填充结果
            RepoVO resultVO = result.getData().getRecords().get(0);
            Assertions.assertEquals("后端技术", resultVO.getCategoryName(), "分类名称未填充");
            // Assertions.assertEquals(50, resultVO.getQuestionCount(), "题目数量未填充");
            // 明确将 Long 转为 int
            Assertions.assertEquals(50, resultVO.getQuestionCount().intValue(), "题目数量未填充");
        }
    }
}