<template>
  <div class="doc-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-cascader
            v-model="selectedKbId"
            placeholder="请选择知识库"
            clearable
            filterable
            style="width: 200px"
            :options="kbList"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: false, checkStrictly: true }"
            @change="handleFilterChange" />
          <el-select
            v-model="filters.originScope"
            placeholder="归属地"
            clearable
            style="width: 120px"
            @change="handleFilterChange">
            <el-option
              v-for="item in originScopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value" />
          </el-select>
          <el-cascader
            v-model="filters.originDepartment"
            :options="originDepartmentTree"
            :props="{ value: 'dictValue', label: 'dictName', children: 'children', emitPath: false, checkStrictly: true }"
            placeholder="来源部门"
            clearable
            filterable
            style="width: 180px"
            @change="handleFilterChange" />
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文档标题"
            prefix-icon="Search"
            clearable
            style="width: 180px"
            @clear="handleSearch"
            @keyup.enter="handleSearch" />
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新建文档
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="docList" stripe style="width: 100%">
        <el-table-column prop="title" label="文档标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{
              row.title
            }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="kbName" label="所属知识库" width="150" />
        <el-table-column prop="originScope" label="归属地" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.originScope" size="small" type="info">
              {{ getOriginScopeLabel(row.originScope) }}
            </el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="originDepartment" label="来源部门" width="150" />
        <el-table-column
          prop="viewCount"
          label="浏览量"
          width="80"
          align="center" />
        <el-table-column
          prop="statusName"
          label="状态"
          width="80"
          align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isTop === 1"
              @change="
                val => {
                  if (typeof val === 'boolean') handleToggleTop(row, val)
                }
              " />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="下载文件" width="100" align="center">
          <template #default="{ row }">
            <el-link
              v-if="row.docUrl"
              type="primary"
              :href="`/api/v1/documents/${row.id}/download`"
              target="_blank">
              下载
            </el-link>
            <span v-else style="color: #909399">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)"
              >编辑</el-button
            >
            <el-button type="success" link @click="handleView(row)"
              >查看</el-button
            >
            <el-button type="danger" link @click="handleDelete(row)"
              >删除</el-button
            >
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
          @current-change="handleSearch" />
      </div>
    </el-card>

    <DocDialog
      v-model:visible="dialogVisible"
      :doc-data="currentDoc"
      :kb-id="Array.isArray(selectedKbId) ? selectedKbId[0] : selectedKbId"
      @success="handleSearch" />

    <el-drawer v-model="viewDrawerVisible" title="文档详情" size="50%">
      <div v-if="currentDoc" class="doc-detail">
        <h1 class="doc-title">{{ currentDoc.title }}</h1>
        <div class="doc-meta">
          <span>作者：{{ currentDoc.author || '未知' }}</span>
          <span>来源：{{ currentDoc.source || '原创' }}</span>
          <span>浏览：{{ currentDoc.viewCount }} 次</span>
          <span>发布时间：{{ currentDoc.createdTime }}</span>
        </div>
        <el-divider />
        <div v-if="currentDoc.summary" class="doc-summary">
          {{ currentDoc.summary }}
        </div>
        <div class="doc-content">
          {{ currentDoc.content || '暂无内容' }}
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocumentList, deleteDocument, Document } from '@/api/doc'
import { getKbCategoryList, getOriginDepartmentList, type SysDict } from '@/api/dict'
import { ORIGIN_SCOPE_OPTIONS, type OriginScope } from '@/types/api'
import DocDialog from './components/DocDialog.vue'

const loading = ref(false)
const docList = ref<Document[]>([])
const kbList = ref<any[]>([])
const searchKeyword = ref('')
const selectedKbId = ref<number | null | any[]>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const currentDoc = ref<Document | null>(null)
const viewDrawerVisible = ref(false)

const originScopeOptions = ref(ORIGIN_SCOPE_OPTIONS)
const originDepartmentTree = ref<any[]>([])

const filters = ref({
  originScope: null as OriginScope | null,
  originDepartment: null as string | null
})

const buildDepartmentTree = (list: SysDict[]): any[] => {
  const tree: any[] = []
  const map = new Map<string, any>()
  
  list.forEach(item => {
    map.set(item.code, {
      dictValue: item.code,
      dictName: item.detail,
      children: []
    })
  })
  
  list.forEach(item => {
    const node = map.get(item.code)
    if (item.parentCode && map.has(item.parentCode)) {
      map.get(item.parentCode).children.push(node)
    } else {
      tree.push(node)
    }
  })
  
  return tree
}

const fetchOriginDepartments = async () => {
  try {
    const departments = await getOriginDepartmentList()
    originDepartmentTree.value = buildDepartmentTree(departments)
  } catch (error) {
    console.error('获取来源部门列表失败', error)
  }
}

const getStatusType = (status: number) => {
  switch (status) {
    case 1:
      return 'success'
    case 0:
      return 'warning'
    case 2:
      return 'info'
    default:
      return 'info'
  }
}

const getOriginScopeLabel = (scope: string) => {
  const option = ORIGIN_SCOPE_OPTIONS.find((o: { value: string }) => o.value === scope)
  return option ? option.label : scope
}

const fetchKbList = async () => {
  try {
    const kbTree = await getKbCategoryList()
    kbList.value = transformKbTree(kbTree)
  } catch (error) {
    console.error('获取知识库列表失败', error)
  }
}

const transformKbTree = (list: SysDict[]): any[] => {
  return list.map(item => ({
    id: item.id,
    name: item.detail,
    children: item.children && item.children.length > 0 ? transformKbTree(item.children) : undefined
  }))
}

const handleFilterChange = () => {
  currentPage.value = 1
  handleSearch()
}

const handleSearch = async () => {
  loading.value = true
  try {
    const kbId = Array.isArray(selectedKbId.value) ? selectedKbId.value[0] : selectedKbId.value
    const res = await getDocumentList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      kbId: kbId || undefined,
      keyword: searchKeyword.value,
      originScope: filters.value.originScope || undefined,
      originDepartment: filters.value.originDepartment || undefined
    })
    docList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  } finally {
    loading.value = false
  }
}

  onMounted(() => {
    fetchKbList()
    fetchOriginDepartments()
    handleSearch()
  })

  const handleAdd = () => {
    currentDoc.value = null
    dialogVisible.value = true
  }

  const handleEdit = (row: Document) => {
    currentDoc.value = { ...row }
    dialogVisible.value = true
  }

  const handleView = async (row: Document) => {
    currentDoc.value = row
    viewDrawerVisible.value = true
  }

  const handleDelete = async (row: Document) => {
    try {
      await ElMessageBox.confirm(`确定要删除文档 "${row.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await deleteDocument(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleToggleTop = async (_row: Document, _val: boolean) => {
    ElMessage.info('置顶功能待开发')
  }
</script>

<style lang="scss" scoped>
  .doc-container {
    padding: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .doc-detail {
    padding: 20px;
  }

  .doc-title {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 20px;
  }

  .doc-meta {
    display: flex;
    gap: 20px;
    color: #909399;
    font-size: 14px;
    margin-bottom: 20px;
  }

  .doc-content {
    line-height: 1.8;
    color: #303133;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .doc-summary {
    line-height: 1.8;
    color: #606266;
    font-style: italic;
    margin-bottom: 20px;
    padding: 15px;
    background: #fafafa;
    border-left: 3px solid #409eff;
    white-space: pre-wrap;
    word-break: break-word;
  }
</style>
