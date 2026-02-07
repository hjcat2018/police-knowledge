<template>
  <div class="search-container">
    <el-card shadow="never">
      <template #header>
        <div class="search-header">
          <h2>智能搜索</h2>
          <p class="subtitle">输入问题，智能检索相关文档</p>
        </div>
      </template>

      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="输入搜索关键词，如：如何办理身份证"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-section">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-cascader
              v-model="filters.kbId"
              :options="kbTreeData"
              :props="{ value: 'id', label: 'name', children: 'children', emitPath: false, checkStrictly: true }"
              placeholder="选择知识库"
              clearable
              filterable
              style="width: 100%"
              @change="handleSearch" />
          </el-col>
          <el-col :span="5">
            <el-select
              v-model="filters.originScope"
              placeholder="归属地"
              clearable
              style="width: 100%"
              @change="handleSearch">
              <el-option
                v-for="item in originScopeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-cascader
              v-model="filters.originDepartment"
              :options="originDepartmentTree"
              :props="{ value: 'code', label: 'name', children: 'children', emitPath: false, checkStrictly: true }"
              placeholder="来源部门"
              clearable
              filterable
              style="width: 100%"
              @change="handleSearch" />
          </el-col>
          <el-col :span="4">
            <el-button @click="clearFilters">清空筛选</el-button>
          </el-col>
        </el-row>
      </div>

      <div class="search-tips">
        <el-text type="info" size="small">
          提示：支持语义搜索，输入自然语言问题即可找到相关文档
        </el-text>
      </div>
    </el-card>

    <el-card v-if="results.length > 0" shadow="never" class="results-card">
      <template #header>
        <div class="results-header">
          <span>搜索结果 ({{ results.length }})</span>
          <el-radio-group v-model="searchType" size="small" @change="handleSearch">
            <el-radio-button value="semantic">语义搜索</el-radio-button>
            <el-radio-button value="hybrid">混合搜索</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div class="results-list">
        <div v-for="(result, index) in results" :key="result.documentId" class="result-item">
          <div class="result-header">
            <el-link type="primary" @click="viewDocument(result.documentId)">
              <h3>{{ result.title }}</h3>
            </el-link>
            <el-tag type="success" size="small">
              相似度 {{ (result.score * 100).toFixed(1) }}%
            </el-tag>
          </div>
          
          <p class="result-summary">{{ result.summary || '暂无摘要' }}</p>
          
          <div class="result-content" v-if="result.content">
            <el-text type="info" size="small" class="content-text">
              {{ result.content.substring(0, 200) }}...
            </el-text>
          </div>

          <div class="result-actions">
            <el-button type="primary" link @click="viewDocument(result.documentId)">
              查看详情
            </el-button>
          </div>

          <el-divider v-if="index < results.length - 1" />
        </div>
      </div>
    </el-card>

    <el-card v-else-if="searched && !loading" shadow="never" class="empty-card">
      <el-empty description="未找到相关文档，请尝试其他关键词" />
    </el-card>

    <DocDetailDialog
      v-model:visible="detailVisible"
      :doc-id="currentDocId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { semanticSearch, hybridSearch, SearchResult } from '@/api/search'
import { getKbCategoryList, getOriginScopeList, getOriginDepartmentList, type SysDict } from '@/api/dict'
import { type OriginScope } from '@/types/api'
import DocDetailDialog from '@/views/doc/list/components/DocDetailDialog.vue'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref<SearchResult[]>([])
const searchType = ref<'semantic' | 'hybrid'>('semantic')
const detailVisible = ref(false)
const currentDocId = ref<number>(0)

const originScopeOptions = ref<{ value: string; label: string }[]>([])
const originDepartmentTree = ref<any[]>([])
const kbTreeData = ref<any[]>([])

const filters = ref({
  kbId: null as number | null | any[],
  categoryId: null as number | null,
  originScope: null as OriginScope | null,
  originDepartment: null as string | null
})

const transformKbTree = (list: SysDict[]): any[] => {
  return list.map(item => ({
    id: item.id,
    name: item.detail,
    children: item.children && item.children.length > 0 ? transformKbTree(item.children) : undefined
  }))
}

const buildDepartmentTree = (list: SysDict[]): any[] => {
  const tree: any[] = []
  const map = new Map<string, any>()

  list.forEach(item => {
    map.set(item.code, {
      id: item.id,
      code: item.code,
      name: item.detail,
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

const fetchData = async () => {
  try {
    const [kbList, originScopeList, originDepartmentList] = await Promise.all([
      getKbCategoryList(1),
      getOriginScopeList(1),
      getOriginDepartmentList()
    ])
    kbTreeData.value = transformKbTree(kbList)
    originScopeOptions.value = originScopeList.map(item => ({
      value: item.code,
      label: item.detail
    }))
    originDepartmentTree.value = buildDepartmentTree(originDepartmentList)
  } catch (error) {
    console.error('获取数据失败', error)
  }
}

const clearFilters = () => {
  filters.value.kbId = null
  filters.value.categoryId = null
  filters.value.originScope = null
  filters.value.originDepartment = null
  handleSearch()
}

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  searched.value = true

  try {
    const kbId = Array.isArray(filters.value.kbId) ? filters.value.kbId[0] : filters.value.kbId
    const params = {
      keyword: keyword.value,
      kbId: kbId,
      categoryId: filters.value.categoryId,
      originScope: filters.value.originScope,
      originDepartment: filters.value.originDepartment,
      topK: 10
    }
    
    if (searchType.value === 'semantic') {
      results.value = await semanticSearch(params)
    } else {
      results.value = await hybridSearch(params)
    }

    if (results.value.length === 0) {
      ElMessage.info('未找到相关文档')
    }
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewDocument = (id: number) => {
  currentDocId.value = id
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.search-container {
  padding: 20px;
}

.search-header {
  h2 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
  }
  
  .subtitle {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.search-box {
  margin-bottom: 16px;
}

.filter-section {
  margin-bottom: 16px;
}

.search-tips {
  padding: 0 8px;
}

.results-card {
  margin-top: 20px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.results-list {
  .result-item {
    padding: 8px 0;
    
    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
      }
    }
    
    .result-summary {
      margin: 0 0 8px 0;
      color: #606266;
      font-size: 14px;
      line-height: 1.6;
    }
    
    .result-content {
      background: #f5f7fa;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 8px;
      
      .content-text {
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
    
    .result-actions {
      margin-top: 8px;
    }
  }
}

.empty-card {
  margin-top: 20px;
}
</style>
