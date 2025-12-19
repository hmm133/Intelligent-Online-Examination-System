<template>
  <div class="navbar">
    <div class="navbar-header">
      <hamburger
        :is-active="sidebar.opened"
        class="hamburger-container"
        @toggleClick="toggleSideBar"
      />

      <breadcrumb class="breadcrumb-container" />

      <div class="right-menu">
        <el-dropdown class="avatar-container" trigger="click">
          <div class="avatar-wrapper">
            <img :src="user.avatar" class="user-avatar">
            <i class="el-icon-caret-bottom" />
          </div>
          <el-dropdown-menu slot="dropdown" class="user-dropdown">
            <router-link to="/myself">
              <el-dropdown-item> 个人中心 </el-dropdown-item>
            </router-link>
            <router-link to="/change-password">
              <el-dropdown-item> 修改密码 </el-dropdown-item>
            </router-link>

            <el-dropdown-item divided @click.native="logout">
              <span style="display: block">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
    <!-- tags -->
    <div class="tags-container">
      <template v-for="(item,index) in tags">
        <el-tag
          v-if="item.title"
          :key="index"
          closable
          disable-transitions
          :class="{ active: item.checked }"
          @click="$router.push(item.path)"
          @close="handleTagClose(item)"
        >
          {{ item.title }}
        </el-tag>
      </template>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import { getToken } from '@/utils/auth'
import { parseJwt } from '@/utils/jwtUtils'
export default {
  components: {
    Breadcrumb,
    Hamburger
  },
  data() {
    return {
      user: {}

    }
  },
  computed: {
    ...mapGetters(['sidebar', 'avatar', 'tags'])
  },
  created() {
    this.decode()
  },
  methods: {
    handleTagClose(item) {
      if (this.$route.path === item.path) {
        this.$store.commit('menu/REMOVE_TAG', item)
        const tags = this.$store.state.menu.tags
        if (tags.length > 0) {
          this.$router.push(tags[tags.length - 1].path).then(() => {
            window.location.reload()
          })
        }
      } else {
        this.$store.commit('menu/REMOVE_TAG', item)
      }
    },
    decode() {
      const token = getToken()
      const user = parseJwt(token)
      this.user = JSON.parse(user.userInfo)
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$store.dispatch('logoutUser')
      await this.$store.dispatch('user/logout')
      this.$router.push(`/login`)
    }
  }
}
</script>

<style lang="scss" scoped>
.el-tag {
  background-color: #ffffff;
  border: 1.5px solid #e4e7ed;
  display: inline-block;
  height: 32px;
  padding: 0 15px;
  line-height: 30px;
  margin-left: 8px;
  font-size: 13px;
  color: #606266;
  border-radius: 16px;
  box-sizing: border-box;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: #f5f7fa;
    border-color: #1abc9c;
    color: #1abc9c;
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(26, 188, 156, 0.2);
  }

  ::v-deep .el-icon-close {
    color: #909399;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #1abc9c;
      color: #fff;
    }
  }
}

.active {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  color: #ffffff;
  border: 1.5px solid transparent;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(26, 188, 156, 0.4);

  ::v-deep .el-icon-close {
    color: rgba(255, 255, 255, 0.8);
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }
}

.navbar {
  height: 110px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 999;

  .navbar-header {
    width: 100%;
    height: 66px;
    display: flex;
    align-items: center;
    background: linear-gradient(90deg, #ffffff 0%, #fafafa 100%);
    border-bottom: 1px solid #f0f0f0;
  }

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    cursor: pointer;
    transition: all 0.3s ease;
    -webkit-tap-highlight-color: transparent;
    padding: 0 15px;
    flex-shrink: 0;

    &:hover {
      background: linear-gradient(135deg, rgba(26, 188, 156, 0.1) 0%, rgba(22, 160, 133, 0.1) 100%);
    }
  }

  .breadcrumb-container {
    flex: 1;
    min-width: 0;
  }

  .right-menu {
    height: 100%;
    line-height: 50px;
    display: flex;
    align-items: center;
    flex-shrink: 0;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(26, 188, 156, 0.1);
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;
        display: flex;
        align-items: center;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          .user-avatar {
            box-shadow: 0 0 0 3px rgba(26, 188, 156, 0.3);
            transform: scale(1.05);
          }
        }

        .user-avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          border: 2px solid #1abc9c;
          transition: all 0.3s ease;
          object-fit: cover;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 50%;
          transform: translateY(-50%);
          font-size: 12px;
          color: #909399;
          transition: all 0.3s ease;
        }

        &:hover .el-icon-caret-bottom {
          color: #1abc9c;
        }
      }
    }
  }

  .tags-container {
    width: 100%;
    height: 45px;
    background: linear-gradient(90deg, #fafafa 0%, #ffffff 100%);
    overflow: hidden;
    display: flex;
    align-items: center;
    padding: 0 15px;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
  }
}

// 下拉菜单样式优化
::v-deep .user-dropdown {
  margin-top: 10px !important;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .el-dropdown-menu__item {
    padding: 12px 20px;
    transition: all 0.3s ease;
    
    &:hover {
      background: linear-gradient(90deg, rgba(26, 188, 156, 0.1) 0%, rgba(22, 160, 133, 0.1) 100%);
      color: #1abc9c;
    }
  }

  .el-dropdown-menu__item--divided {
    border-top: 1px solid #f0f0f0;
    
    &:before {
      display: none;
    }
  }
}
</style>