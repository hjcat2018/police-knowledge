<template>
  <div class="kb-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索知识库名称/编码"
            prefix-icon="Search"
            clearable
            style="width: 240px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建知识库
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="kbList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="知识库名称" min-width="150" />
        <el-table-column prop="code" label="知识库编码" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="docCount" label="文档数" width="80" align="center" />
        <el-table-column prop="viewCount" label="浏览量" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPublic" label="是否公开" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 1 ? 'success' : 'info'" size="small">
              {{ row.isPublic === 1 ? '公开' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleManage(row)">文档管理</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <KbDialog
      v-model:visible="dialogVisible"
      :kb-data="currentKb"
      @success="handleSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getKnowledgeBaseList, deleteKnowledgeBase, KnowledgeBase } from '@/api/kb'
import KbDialog from './components/KbDialog.vue'

const router = useRouter()
const loading = ref(false)
const kbList = ref<KnowledgeBase[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentKb = ref<KnowledgeBase | null>(null)

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeBaseList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    })
    kbList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取知识库列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  currentKb.value = null
  dialogVisible.value = true
}

const handleEdit = (row: KnowledgeBase) => {
  currentKb.value = { ...row }
  dialogVisible.value = true
}

const handleManage = (row: KnowledgeBase) => {
  router.push(`/doc/list?kbId=${row.id}`)
}

const handleDelete = async (row: KnowledgeBase) => {
  try {
    await ElMessageBox.confirm(`确定要删除知识库 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteKnowledgeBase(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style lang="scss" scoped>
.kb-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
