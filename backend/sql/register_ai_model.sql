-- ============================================================
-- OceanBase SeekDB AI 模型注册脚本
-- 执行时间: 2026-01-21
-- ============================================================

USE police_kb;

-- ============================================================
-- Step 1: 清理旧模型和端点
-- ============================================================

-- 删除旧模型（如果存在）
CALL DBMS_AI_SERVICE.DROP_AI_MODEL('ob_embed');
CALL DBMS_AI_SERVICE.DROP_AI_MODEL('ob_rerank');
CALL DBMS_AI_SERVICE.DROP_AI_MODEL_ENDPOINT('ob_embed_endpoint');
CALL DBMS_AI_SERVICE.DROP_AI_MODEL_ENDPOINT('ob_rerank_endpoint');

SELECT 'Step 1: 清理完成' AS status;

-- ============================================================
-- Step 2: 注册 AI 模型 (text-embedding-v4)
-- ============================================================

CALL DBMS_AI_SERVICE.CREATE_AI_MODEL(
    'ob_embed',
    '{
      "type": "dense_embedding",
      "model_name": "text-embedding-v4"
    }'
);

SELECT 'Step 2: AI 模型创建完成' AS status;

-- ============================================================
-- Step 3: 注册 AI 端点 (DashScope)
-- ============================================================

CALL DBMS_AI_SERVICE.CREATE_AI_MODEL_ENDPOINT(
    'ob_embed_endpoint',
    '{
      "ai_model_name": "ob_embed",
      "url": "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings",
      "access_key": "sk-e8d86d11e21540d9b72bc83f78bf1154",
      "provider": "openai"
    }'
);

SELECT 'Step 3: AI 端点创建完成' AS status;

-- ============================================================
-- Step 4: 测试 AI_EMBED 函数
-- ============================================================

-- 测试向量生成（使用模型名称）
SELECT AI_EMBED('ob_embed', 'Hello world') AS embedding;

SELECT 'Step 4: AI_EMBED 测试完成' AS status;

-- ============================================================
-- Step 5: 检查已注册的模型和端点
-- ============================================================

SELECT '检查注册的 AI 模型:' AS info;
SELECT * FROM oceanbase.v$OB_MODEL_REGISTRY WHERE model_name LIKE '%ob_%';

SELECT '检查注册的 AI 端点:' AS info;
SELECT * FROM oceanbase.v$OB_ENDPOINT_REGISTRY WHERE endpoint_name LIKE '%ob_%';

-- ============================================================
-- Step 6: 检查向量表是否存在
-- ============================================================

SHOW TABLES LIKE 'document_vectors';

SELECT 'AI 模型注册脚本执行完成!' AS final_status;
