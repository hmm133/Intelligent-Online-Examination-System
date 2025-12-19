<template>
  <div class="app-container">
    <h3>组卷信息</h3>
    <el-tabs type="border-card" @tab-click="handleClick" v-model="activeName">
      <el-tab-pane label="自己选题" name="first">
        <ChooseQuestion
          @selected-change="handleSelectedChange"
          ref="questionSelector"
        ></ChooseQuestion>
      </el-tab-pane>
      <el-tab-pane label="随机抽题" name="second">
        <!-- 新增：AI补题按钮 -->
        <div style="margin-bottom: 15px;">
          <el-button 
            type="success" 
            size="small"
            icon="el-icon-magic-stick"
            @click="openAIDialog"
          >
            题目不够?AI智能补题
          </el-button>
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            💡 快速为当前题库生成题目
          </span>
        </div>

        <!-- AI生成对话框 -->
        <ai-question-generator
          :visible.sync="aiDialogVisible"
          :default-repo-id="repoList[0].repoId"
          @success="handleAIGenerateSuccess"
          @view-questions="handleViewQuestions"
        />

        <!-- <el-card style="margin-top: 20px"> -->
        <div style="float: right; font-weight: bold; color: #ff0000">
          试卷总分：{{ postForm.totalScore }}分
        </div>

        <div>
          <!-- <el-button class="filter-item" size="small" type="primary" icon="el-icon-plus" @click="handleAdd">
          添加题库
        </el-button> -->

          <el-table
            :data="repoList"
            :border="false"
            empty-text="请点击上面的`添加题库`进行设置"
            style="width: 100%; margin-top: 15px"
          >
            <el-table-column label="题库" width="200">
              <template v-slot="scope">
                <repo-select
                  v-model="scope.row.repoId"
                  :multi="false"
                  :excludes="excludes"
                  @change="repoChange($event, scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="单选数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.radioCount"
                  :min="0"
                  :max="scope.row.totalRadio"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalRadio != undefined">
                  / {{ scope.row.totalRadio }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="单选分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.radioScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>

            <el-table-column label="多选数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.multiCount"
                  :min="0"
                  :max="scope.row.totalMulti"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalMulti != undefined">
                  / {{ scope.row.totalMulti }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="多选分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.multiScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>

            <el-table-column label="判断题数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.judgeCount"
                  :min="0"
                  :max="scope.row.totalJudge"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalJudge != undefined">
                  / {{ scope.row.totalJudge }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="判断题分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.judgeScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>
            <el-table-column label="简答题数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.saqCount"
                  :min="0"
                  :max="scope.row.totalSaq"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalSaq != undefined">
                  / {{ scope.row.totalSaq }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="简答题分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.saqScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>
            <!-- <el-table-column label="删除" align="center" width="80px">
            <template v-slot="scope">
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                @click="removeItem(scope.$index)"
              />
            </template>
          </el-table-column> -->
          </el-table>
        </div>
        <!-- </el-card> -->
      </el-tab-pane>
    </el-tabs>

    <h3>考试配置</h3>
    <el-card style="margin-top: 20px">
      <el-form
        ref="postForm"
        :model="postForm"
        :rules="rules"
        label-position="left"
        label-width="120px"
      >
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="postForm.title" />
        </el-form-item>

        <!-- <el-form-item label="考试描述" prop="content">
          <el-input v-model="postForm.content" type="textarea" />
        </el-form-item> -->

        <el-form-item label="总分数" prop="totalScore">
          <el-input-number :value="postForm.totalScore" disabled />
        </el-form-item>

        <el-form-item label="及格分" prop="passedScore">
          <el-input-number
            v-model="postForm.passedScore"
            :max="postForm.totalScore"
          />
        </el-form-item>

        <el-form-item label="最多切屏次数" prop="maxCount">
          <el-input-number v-model="postForm.maxCount" />
        </el-form-item>
        <el-form-item label="证书" prop="maxCount">
          <CertificateSelect
            v-model="postForm.certificateId"
            is-multiple
            @change="onCertificateChange"
          />
          <!-- <el-input-number v-model="postForm.maxCount"  /> -->
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="examDuration">
          <el-input-number v-model="postForm.examDuration" />
        </el-form-item>
        <el-form-item label="考试时间范围" prop="start">
          <el-date-picker
            v-model="postForm.start"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <!--
        <el-form-item label="是否限时">
          <el-checkbox v-model="postForm.timeLimit" />
        </el-form-item> -->

        <!-- <el-form-item
          v-if="postForm.timeLimit"
          label="考试时间"
          prop="examDuration"
        >
          <el-date-picker
            v-model="dateValues"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item> -->
      </el-form>
    </el-card>

    <h3>权限配置</h3>
    <el-card style="margin-top: 20px">
      <div style="display: flex">
        <div style="margin-left: 10px">
          <el-form
            ref="postForm"
            :model="postForm"
            :rules="rules"
            label-position="left"
            label-width="120px"
          >
            <el-form-item label="考试班级" prop="classIds">
              <ClassSelect
                v-model="postForm.classIds"
                is-multiple
                @change="onClassChange"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <div style="margin-top: 20px">
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script>
// import { fetchDetail } from '@/api/exam'
import RepoSelect from "@/components/RepoSelect";
import ClassSelect from "@/components/ClassSelect";
import CertificateSelect from "@/components/CertificateSelect";
import { saveData } from "@/api/exam";
import ChooseQuestion from "@/components/ExamComponents/ChooseQuestion";
import AIQuestionGenerator from "@/components/AIQuestionGenerator";
export default {
  name: "ExamDetail",

  components: {
    RepoSelect,
    ChooseQuestion,
    ClassSelect,
    CertificateSelect,
    AIQuestionGenerator,
  },
  data() {
    return {
      aiDialogVisible: false,
      activeName: "first",
      input: "",
      treeData: [],
      defaultProps: {
        label: "deptName",
      },
      filterText: "",
      treeLoading: false,
      // dateValues: [],
      // 题库
      repoList: [
        {
          addQuType: "0",
          queIds: "",
          id: "",
          rowId: 0,
          radioCount: 0,
          radioScore: 0,
          multiCount: 0,
          multiScore: 0,
          judgeCount: 0,
          judgeScore: 0,
          saqCount: 0,
          saqScore: 0,
        },
      ],
      // 已选择的题库
      excludes: [],
      postForm: {
        start: [],
        // 总分数
        totalScore: 0,
        // 题库列表
        repoList: [],
        // 开放类型
        openType: 1,
        // 考试班级列表
        departIds: [],
        // 初始化班级列表
        classIds: [],
      },
      rules: {
        title: [{ required: true, message: "考试名称不能为空！" }],

        // content: [{ required: true, message: '考试描述不能为空！' }],

        open: [{ required: true, message: "考试权限不能为空！" }],

        totalScore: [{ required: true, message: "考试分数不能为空！" }],

        passedScore: [{ required: true, message: "及格分不能为空！" }],

        examDuration: [{ required: true, message: "考试时间不能为空！" }],

        start: [{ required: true, message: "考试时间范围不能为空！" }],

        maxCount: [{ required: false, message: "最多切屏次数" }],
        classIds: [
          {
            required: true,
            message: "请选择考试班级！",
            type: "array",
            min: 1,
          },
        ],
      },
    };
  },

  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },

    // dateValues: {
    //   handler() {
    //     this.postForm.startTime = this.dateValues[0];
    //     this.postForm.endTime = this.dateValues[1];
    //   },
    // },

    // 题库变换
    repoList: {
      handler(val) {
        let totalScore = 0;
        this.excludes = [];
        for (let i = 0; i < val.length; i++) {
          const item = val[i];
          if (item.radioCount > 0 && item.radioScore > 0) {
            totalScore += item.radioCount * item.radioScore;
          }

          if (item.multiCount > 0 && item.multiScore > 0) {
            totalScore += item.multiCount * item.multiScore;
          }

          if (item.judgeCount > 0 && item.judgeScore > 0) {
            totalScore += item.judgeCount * item.judgeScore;
          }
          if (item.saqCount > 0 && item.saqScore > 0) {
            totalScore += item.saqCount * item.saqScore;
          }
          this.excludes.push(item.id);
        }

        // 赋值
        this.postForm.totalScore = totalScore;
        this.postForm.repoList = val;
        this.$forceUpdate();
      },
      deep: true,
    },
  },
  methods: {
      // 打开AI生成对话框
    openAIDialog() {
      // 检查是否选择了题库
      if (!this.repoList[0].repoId) {
        this.$message.warning('请先选择题库')
        return
      }
      this.aiDialogVisible = true
    },

    // AI生成成功回调
    handleAIGenerateSuccess(result) {
      this.$message.success(`AI成功生成 ${result.successCount} 道题目！`)
      // 可以选择刷新题库信息或提示用户重新选择题库
      this.$notify({
        title: '提示',
        message: '题目已生成到题库，可在随机抽题中配置使用',
        type: 'success',
        duration: 3000
      })
    },

    // 查看生成的题目
    handleViewQuestions(questionIds) {
      console.log('生成的题目IDs:', questionIds)
      this.$message.info('题目已生成，请在题库管理中查看')
    },

    // 点击tab
    handleClick(tab, event) {
      this.$refs.questionSelector.clearSelection();
      // console.log(event)
      this.repoList[0].addQuType = tab.index;
      this.repoList[0].queIds = "";
      this.repoList[0].id = "";
      this.repoList[0].rowId = 0;
      this.repoList[0].radioCount = 0;
      this.repoList[0].radioScore = 0;
      this.repoList[0].multiCount = 0;
      this.repoList[0].judgeCount = 0;
      this.repoList[0].judgeScore = 0;
      this.repoList[0].saqCount = 0;
      this.repoList[0].saqScore = 0;
      console.log(tab, event);
    },
    // 子组件选择的ids
    handleSelectedChange(selectedIds) {
      var ids = [];
      selectedIds.selectedRows.forEach((item) => {
        ids.push(item.id);
      });

      this.repoList[0].queIds = ids.join(",");
      this.repoList[0].radioCount = selectedIds.questionList.radioCount;
      this.repoList[0].radioScore = selectedIds.questionList.radioScore;
      this.repoList[0].multiCount = selectedIds.questionList.multiCount;
      this.repoList[0].multiScore = selectedIds.questionList.multiScore;
      this.repoList[0].judgeCount = selectedIds.questionList.judgeCount;
      this.repoList[0].judgeScore = selectedIds.questionList.judgeScore;
      this.repoList[0].saqCount = selectedIds.questionList.saqCount;
      this.repoList[0].saqScore = selectedIds.questionList.saqScore;
      console.log("从子组件接收到的选中ID:", this.repoList);
      // 在这里你可以将选中的ID保存到父组件的数据中
      this.selectedQuestionIds = selectedIds;
      // 或者执行其他需要的操作
    },
    handleSave() {
      if (this.repoList[0].addQuType === "1") {
        this.$refs.postForm.validate((valid) => {
          if (!valid) {
            return;
          }
          if (this.postForm.totalScore === 0) {
            this.$notify({
              title: "提示信息",
              message: "考试规则设置不正确，请确认！",
              type: "warning",
              duration: 2000,
            });

            return;
          }

          // 验证班级是否选择
          if (!this.postForm.classIds || this.postForm.classIds.length === 0) {
            this.$notify({
              title: "提示信息",
              message: "请选择考试班级！",
              type: "warning",
              duration: 2000,
            });
            return;
          }

          for (let i = 0; i < this.postForm.repoList.length; i++) {
            const repo = this.postForm.repoList[i];
            if (!repo.repoId) {
              this.$notify({
                title: "提示信息",
                message: "考试题库选择不正确！",
                type: "warning",
                duration: 2000,
              });
              return;
            }

            if (
              (repo.radioCount > 0 && repo.radioScore === 0) ||
              (repo.radioCount === 0 && repo.radioScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的单选题配置！",
                type: "warning",
                duration: 2000,
              });

              return;
            }

            if (
              (repo.multiCount > 0 && repo.multiScore === 0) ||
              (repo.multiCount === 0 && repo.multiScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的多选题配置！",
                type: "warning",
                duration: 2000,
              });

              return;
            }

            if (
              (repo.judgeCount > 0 && repo.judgeScore === 0) ||
              (repo.judgeCount === 0 && repo.judgeScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的判断题配置！",
                type: "warning",
                duration: 2000,
              });
              return;
            }

            if (
              (repo.saqCount > 0 && repo.saqScore === 0) ||
              (repo.saqCount === 0 && repo.saqScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的简答题配置！",
                type: "warning",
                duration: 2000,
              });
              return;
            }
          }

          this.$confirm("确实要提交保存吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }).then(() => {
            this.submitForm();
          });
        });
      }
      if (this.repoList[0].addQuType === "0") {
        this.$confirm("确实要提交保存吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }).then(() => {
          this.submitForm();
        });
      }
    },

    handleCheckChange() {
      const that = this;
      // 置空
      this.postForm.departIds = [];
      const nodes = this.$refs.tree.getCheckedNodes();
      nodes.forEach(function (item) {
        that.postForm.departIds.push(item.id);
      });
    },

    // 添加子项
    handleAdd() {
      this.repoList.push();
    },

    removeItem(index) {
      this.repoList.splice(index, 1);
    },
    formatDateToISOString(date) {
      if (!(date instanceof Date)) {
        return null;
      }

      // 获取本地时间的各部分（不进行时区转换）
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");

      // 格式化为本地时间字符串（不含时区信息）
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
      // 输出示例："2025-05-14 09:00:00"（保留用户选择的本地时间）
    },

    submitForm() {
      console.log("postForm", this.postForm);
      // 校验和处理数据
      let cerTemp = "";
      if (
        this.postForm.certificateId != null &&
        this.postForm.certificateId != ""
      ) {
        console.log(this.postForm.certificateId);
        cerTemp = this.postForm.certificateId.join(",");
      }
      this.postForm.repoList = this.repoList;

      const params = {
        title: this.postForm.title,
        // content: this.postForm.content, // 添加考试描述字段
        examDuration: this.postForm.examDuration,
        maxCount: this.postForm.maxCount,
        passedScore: this.postForm.passedScore,
        startTime: this.formatDateToISOString(this.postForm.start[0]),
        endTime: this.formatDateToISOString(this.postForm.start[1]),
        gradeIds: this.postForm.classIds.join(","),
        repoId: this.postForm.repoList[0].repoId,
        certificateId: cerTemp,
        addQuype: this.postForm.repoList[0].addQuType,
        quIds: this.postForm.repoList[0].queIds,
        radioCount: this.postForm.repoList[0].radioCount,
        radioScore: this.postForm.repoList[0].radioScore,
        multiCount: this.postForm.repoList[0].multiCount,
        multiScore: this.postForm.repoList[0].multiScore,
        judgeCount: this.postForm.repoList[0].judgeCount,
        judgeScore: this.postForm.repoList[0].judgeScore,
        saqCount: this.postForm.repoList[0].saqCount,
        saqScore: this.postForm.repoList[0].saqScore,
      };
      saveData(params).then((res) => {
        if (res.code) {
          this.$notify({
            title: "成功",
            message: "考试保存成功！",
            type: "success",
            duration: 2000,
          });

          this.$router.push({ name: "exam-management" });
        } else {
          this.$notify({
            title: "失败",
            message: res.msg,
            type: "error",
            duration: 2000,
          });
        }
      });
    },

    filterNode(value, data) {
      if (!value) return true;
      return data.deptName.indexOf(value) != -1;
    },
    onCertificateChange() {
      // 方法实现...
    },
    onClassChange() {},
    repoChange(e, row) {
      // 赋值ID
      row.id = e.id;
      if (e != null) {
        row.totalRadio = e.radioNum;
        row.totalMulti = e.multiNum;
        row.totalJudge = e.judgeNum;
        row.totalSaq = e.saqNum;
      } else {
        row.totalRadio = 0;
        row.totalMulti = 0;
        row.totalJudge = 0;
        row.totalSaq = 0;
      }
    },
  },
};
</script>
