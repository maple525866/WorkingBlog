<template>
  <el-container class="my_home">
    <el-aside width="260px">
      <el-card>
        <div style="text-align:center">
          <img v-if="user.userface" :src="user.userface" alt="avatar" style="width:200px;height:200px;object-fit:cover"/>
          <div v-else style="width:200px;height:200px;background:#f2f2f2;display:flex;align-items:center;justify-content:center">暂无头像</div>
        </div>
        <h3 style="margin-top:10px;text-align:center">{{user.nickname}}</h3>
        <p>用户名: {{user.username}}</p>
        <p>邮箱: {{user.email}}</p>
        <p>注册时间: <span v-if="user.regTime">{{user.regTime | formatDateTime}}</span></p>
      </el-card>
    </el-aside>
    <el-container>
      <el-main>
        <h2 style="text-align:left;margin-bottom:10px">个人文章</h2>
        <!-- 使用已有的 blog_table 组件显示当前用户的已发布文章 -->
        <blog_table :state="1" :showEdit="false" :showDelete="false"></blog_table>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import {getRequest} from '@/utils/api'
  import BlogTable from '@/components/BlogTable'

  export default {
    components: {
      'blog_table': BlogTable
    },
    mounted() {
      this.loadCurrentUser();
    },
    methods: {
      loadCurrentUser() {
        var _this = this;
        getRequest('/currentUser').then(resp => {
          if (resp.status == 200) {
            _this.user = resp.data;
            console.log(resp.data);
        
          }
        }, () => {
          // ignore
        })
      }
    },
    data() {
      return {
        user: {}
      }
    }
  }
</script>

<style>
.my_home > .el-aside {
  padding: 10px;
}
.my_home .el-card img {
  border-radius: 4px;
}
</style>
