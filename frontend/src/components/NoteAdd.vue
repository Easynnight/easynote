<template>
  <div class="note-edit-app">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-left">
        <button @click="goBack" class="back-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5M5 12L12 5M5 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>添加新笔记</h1>
      </div>
      <div class="header-actions">
        <button @click="saveNote" class="save-btn">保存</button>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="note-edit-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 错误提示 -->
      <div v-else-if="error" class="error">
        <p>{{ error }}</p>
        <button @click="fetchCategories">重试</button>
      </div>

      <!-- 笔记编辑表单 -->
      <div v-else class="note-edit-form">
        <div class="form-group">
          <input type="text" v-model="note.title" placeholder="输入标题..." class="note-title-input">
        </div>
        
        <div class="form-group">
          <textarea v-model="note.content" placeholder="开始记录你的想法..." class="note-content-input" rows="15"></textarea>
          <div class="char-count">字数: {{ note.content.length }}</div>
        </div>
        
        <div class="form-footer">
          <div class="form-actions">
            <div class="category-selector">
              <label>选择分类：</label>
              <select v-model="note.categoryId" class="category-dropdown">
                <option value="">无分类</option>
                <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">{{ category.name }}</option>
              </select>
              <button @click="showAddCategoryModal = true" class="add-category-btn">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 添加分类模态框 -->
    <div v-if="showAddCategoryModal" class="modal-overlay" @click="showAddCategoryModal = false">
      <div class="modal-content" @click.stop>
        <h3>添加新分类</h3>
        <input type="text" v-model="newCategoryName" placeholder="分类名称" @keydown.enter="addCategory">
        <div class="color-picker">
          <span>选择颜色：</span>
          <div v-for="color in categoryColors" :key="color" class="color-option" :style="{ backgroundColor: color }" @click="selectCategoryColor(color)"></div>
        </div>
        <div class="modal-actions">
          <button @click="showAddCategoryModal = false">取消</button>
          <button @click="addCategory">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NoteAdd',
  data() {
    return {
      note: {
        title: '',
        content: '',
        categoryId: ''
      },
      categories: [],
      loading: false,
      error: null,
      showAddCategoryModal: false,
      newCategoryName: '',
      selectedCategoryColor: '#7f9cf5',
      categoryColors: ['#7f9cf5', '#63b3ed', '#4fd1c5', '#68d391', '#f6ad55', '#fc8181', '#9f7aea', '#ed64a6']
    }
  },
  created() {
    this.fetchCategories();
  },
  methods: {
    async fetchCategories() {
      this.loading = true;
      this.error = null;
      try {
        const response = await window.fetchWithAuth('/api/categories', {
          credentials: 'include'
        });
        if (response.ok) {
          // API直接返回分类列表
          const data = await response.json();
          this.categories = data || [];
        }
      } catch (error) {
        console.error('Error fetching categories:', error);
        this.error = '获取分类失败: ' + error.message;
      } finally {
        this.loading = false;
      }
    },
    
    async saveNote() {
      // 基本验证
      if (!this.note.title.trim() && !this.note.content.trim()) {
        alert('请输入标题或内容');
        return;
      }
      
      try {
        const response = await window.fetchWithAuth('/api/notes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.note)
        });
        
        if (!response.ok) throw new Error('保存笔记失败');
        
        alert('笔记添加成功');
        this.$router.push('/notes');
      } catch (error) {
        console.error('Error saving note:', error);
        alert('保存笔记失败: ' + error.message);
      }
    },
    
    async addCategory() {
      if (!this.newCategoryName.trim()) {
        alert('请输入分类名称');
        return;
      }
      
      try {
        const response = await window.fetchWithAuth('/api/categories', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            categoryName: this.newCategoryName.trim(),
            color: this.selectedCategoryColor
          })
        });
        
        if (!response.ok) throw new Error('添加分类失败');
        
        this.fetchCategories();
        this.newCategoryName = '';
        this.showAddCategoryModal = false;
      } catch (error) {
        console.error('Error adding category:', error);
        alert('添加分类失败: ' + error.message);
      }
    },
    
    selectCategoryColor(color) {
      this.selectedCategoryColor = color;
    },
    
    goBack() {
      // 如果有内容，提示用户是否放弃编辑
      if ((this.note.title.trim() || this.note.content.trim()) && !confirm('确定要放弃编辑吗？')) {
        return;
      }
      this.$router.push('/notes');
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
.note-edit-app {
  min-height: 100vh;
  background-color: #f7fafc;
  color: #2d3748;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 顶部导航栏 */
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  opacity: 0.9;
  transition: opacity 0.2s;
}

.back-btn:hover {
  opacity: 1;
}

.header-left h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 500;
}

.save-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 0.5rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background-color 0.2s;
}

.save-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 主内容区 */
.note-edit-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

/* 加载状态 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #718096;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误提示 */
.error {
  padding: 1.5rem;
  background: #fff5f5;
  border-left: 4px solid #fc8181;
  border-radius: 4px;
  color: #c53030;
}

.error p {
  margin: 0 0 1rem 0;
}

.error button {
  background: #fc8181;
  border: none;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}

/* 笔记编辑表单 */
.note-edit-form {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.form-group {
  margin-bottom: 1.5rem;
}

.note-title-input {
  width: 100%;
  padding: 0.75rem 0;
  border: none;
  border-bottom: 2px solid #e2e8f0;
  font-size: 1.5rem;
  font-weight: 500;
  color: #2d3748;
  outline: none;
  transition: border-color 0.2s;
}

.note-title-input:focus {
  border-bottom-color: #667eea;
}

.note-title-input::placeholder {
  color: #a0aec0;
  font-weight: 400;
}

.note-content-input {
  width: 100%;
  padding: 0.75rem 0;
  border: none;
  font-size: 1rem;
  line-height: 1.6;
  color: #4a5568;
  resize: none;
  outline: none;
  font-family: inherit;
}

.note-content-input::placeholder {
  color: #a0aec0;
}

.char-count {
  text-align: right;
  font-size: 0.85rem;
  color: #718096;
  margin-top: 0.5rem;
  padding: 0.25rem 0;
}

.form-footer {
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-selector {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.category-selector label {
  font-size: 0.95rem;
  color: #4a5568;
}

.category-dropdown {
  padding: 0.5rem;
  border: 1px solid #cbd5e0;
  border-radius: 4px;
  font-size: 0.95rem;
  background: white;
  color: #4a5568;
  outline: none;
  cursor: pointer;
}

.add-category-btn {
  background: #f7fafc;
  border: 1px dashed #cbd5e0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  color: #4a5568;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.add-category-btn:hover {
  background: #edf2f7;
  border-color: #a0aec0;
}

.important-toggle {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.important-toggle label {
  font-size: 0.95rem;
  color: #4a5568;
  cursor: pointer;
}

.important-toggle input {
  display: none;
}

.toggle-slider {
  width: 40px;
  height: 20px;
  background: #e2e8f0;
  border-radius: 20px;
  position: relative;
  transition: background-color 0.2s;
}

.toggle-slider::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  transition: transform 0.2s;
}

.toggle-slider.active {
  background: #667eea;
}

.toggle-slider.active::after {
  transform: translateX(20px);
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.modal-content h3 {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  color: #2d3748;
}

.modal-content input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #cbd5e0;
  border-radius: 4px;
  font-size: 1rem;
  margin-bottom: 1.5rem;
  box-sizing: border-box;
}

.color-picker {
  margin-bottom: 1.5rem;
}

.color-picker span {
  display: block;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
  color: #4a5568;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-block;
  margin-right: 8px;
}

.color-option:hover,
.color-option.selected {
  border-color: #2d3748;
  transform: scale(1.1);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.modal-actions button {
  padding: 0.5rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.modal-actions button:first-child {
  background: #e2e8f0;
  border: none;
  color: #4a5568;
}

.modal-actions button:first-child:hover {
  background: #cbd5e0;
}

.modal-actions button:last-child {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.modal-actions button:last-child:hover {
  opacity: 0.9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }
  
  .note-edit-content {
    padding: 1rem;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>