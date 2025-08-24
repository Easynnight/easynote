<template>
  <div class="note-app">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="app-title">
        <h1>轻笔记</h1>
        <div class="subtitle">记录生活的每一刻</div>
      </div>
      <div class="header-actions">
        <div class="search-bar">
          <input type="text" v-model="searchQuery" placeholder="搜索笔记..." @input="debouncedSearch">
          <button @click="searchNotes" class="search-btn">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <button @click="exportNotes" class="export-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15V9C21 7.89543 20.1046 7 19 7H5C3.89543 7 3 7.89543 3 9V15C3 16.1046 3.89543 17 5 17H19C20.1046 17 21 16.1046 21 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M7 17V19C7 19.5304 7.21071 20.0391 7.58579 20.4142C7.96086 20.7893 8.46957 21 9 21H15C15.5304 21 16.0391 20.7893 16.4142 20.4142C16.7893 20.0391 17 19.5304 17 19V17M12 12L12 17M12 12L9 9M12 12L15 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          导出笔记
        </button>
        <button @click="logout" class="logout-btn">退出</button>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 左侧分类栏 -->
      <aside class="sidebar">
        <div class="category-section">
          <h3>分类</h3>
          <div class="category-item all-categories" :class="selectedCategory === 'all' ? 'active' : ''" @click="selectCategory('all')">
            所有笔记
            <span class="count">{{ notes.length }}</span>
          </div>

          <div v-for="category in categories" :key="category.categoryId" class="category-item" :class="selectedCategory === category.categoryId ? 'active' : ''" @click="selectCategory(category.categoryId)" :style="{ borderLeftColor: category.color }">
            <span class="category-name">{{ category.name }}</span>
            <div class="category-actions">
              <span class="count">{{ getNotesCountByCategory(category.categoryId) }}</span>
              <button class="delete-category-btn" @click.stop="deleteCategory(category.categoryId)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 6H5M5 6H21M5 6V20C5 21.1046 5.89543 22 7 22H17C18.1046 22 19 21.1046 19 20V6M5 6V4C5 2.89543 5.89543 2 7 2H17C18.1046 2 19 2.89543 19 4V6M10 11V17M14 11V17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>
          <button @click="showAddCategoryModal = true" class="add-category-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            添加分类
          </button>
        </div>
      </aside>

      <!-- 右侧笔记列表 -->
      <div class="notes-container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>正在加载笔记...</p>
        </div>

        <!-- 错误提示 -->
        <div v-else-if="error" class="error">
          <p>{{ error }}</p>
          <button @click="fetchNotes">重试</button>
        </div>

        <!-- 笔记网格 -->
        <div v-else class="notes-grid">
          <!-- 添加新笔记卡片 -->
          <div class="note-card add-note-card" @click="navigateToAddNote">
            <div class="add-note-content">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 5V19M5 12H19" stroke="#a0aec0" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span>添加新笔记</span>
            </div>
          </div>

          <!-- 笔记卡片 -->
          <div v-for="note in filteredNotes" :key="note.noteId" class="note-card" :style="getNoteCategoryStyle(note)" @click="editNote(note.noteId)">
            <div class="note-header">
              <h3 :title="note.title">{{ truncate(note.title, 25) }}</h3>
            </div>
            <p class="note-content" :title="note.content">{{ truncate(note.content, 100) }}</p>
            <div class="note-footer">
              <span class="note-date">{{ formatDate(note.createdAt) }}</span>
              <span v-if="note.categoryName" class="note-category">{{ note.categoryName }}</span>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredNotes.length === 0" class="empty-state">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 19V13M12 7H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="#a0aec0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <p>暂无笔记</p>
            <button @click="navigateToAddNote" class="create-first-note">创建第一条笔记</button>
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
  name: 'NoteList',
  data() {
    return {
      notes: [],
      categories: [],
      loading: true,
      error: null,
      selectedCategory: 'all',
      searchQuery: '',
      debounceTimer: null,
      showAddCategoryModal: false,
      newCategoryName: '',
      selectedCategoryColor: '#7f9cf5',
      categoryColors: ['#7f9cf5', '#63b3ed', '#4fd1c5', '#68d391', '#f6ad55', '#fc8181', '#9f7aea', '#ed64a6']
    }
  },
  computed: {
    filteredNotes() {
      let filtered = [...this.notes];
      
      // 按分类筛选
      if (this.selectedCategory === 'all') {
        // 显示所有笔记
      } else {
        filtered = filtered.filter(note => 
          note.category?.categoryId === this.selectedCategory || 
          note.categoryId === this.selectedCategory
        );
      }
      
      // 搜索筛选
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase().trim();
        filtered = filtered.filter(note => 
          note.title.toLowerCase().includes(query) || 
          note.content.toLowerCase().includes(query)
        );
      }
      
      // 按创建时间倒序排序
      return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }
  },
  created() {
    this.fetchNotes();
    this.fetchCategories();
  },
  methods: {
    async deleteCategory(categoryId) {
      if (confirm('确定要删除这个分类吗？删除后，该分类下的笔记将移至未分类。')) {
        try {
          const response = await window.fetchWithAuth(`/api/categories/${categoryId}`, {
            method: 'DELETE',
            credentials: 'include'
          });
          if (!response.ok) throw new Error('删除分类失败');
          this.fetchCategories();
          // 如果删除的是当前选中的分类，则切换到全部笔记
          if (this.selectedCategory === categoryId) {
            this.selectCategory('all');
          }
        } catch (error) {
          console.error('Error deleting category:', error);
          alert('删除分类失败: ' + error.message);
        }
      }
    },

    getNoteCategoryStyle(note) {
      // 如果笔记有category对象且包含color字段，则使用该颜色
      if (note.category && note.category.color) {
        return { borderLeftColor: note.category.color };
      }
      // 否则使用默认的透明颜色
      return {};
    },
    
    async fetchNotes() {
      this.loading = true;
      this.error = null;
      try {
        const response = await window.fetchWithAuth('/api/notes', {
          credentials: 'include'
        });
        if (!response.ok) throw new Error('获取笔记列表失败: ' + response.statusText);
        this.notes = await response.json();
      } catch (error) {
        console.error('Error fetching notes:', error);
        this.error = '获取笔记列表失败: ' + error.message;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchCategories() {
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
      }
    },
    
    selectCategory(category) {
      this.selectedCategory = category;
    },
    
    navigateToAddNote() {
      this.$router.push('/notes/add');
    },
    
    editNote(noteId) {
      this.$router.push(`/notes/edit/${noteId}`);
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
            name: this.newCategoryName.trim(),
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
    
    getNotesCountByCategory(categoryId) {
      return this.notes.filter(note => 
        note.category?.categoryId === categoryId || 
        note.categoryId === categoryId
      ).length;
    },
    
    truncate(value, length) {
      if (!value) return '';
      if (value.length <= length) return value;
      return value.substring(0, length) + '...';
    },
    
    formatDate(dateString) {
      const date = new Date(dateString);
      const now = new Date();
      
      // 处理东八区时间问题：比较日期部分（年、月、日），不考虑具体时间
      const dateYear = date.getFullYear();
      const dateMonth = date.getMonth();
      const dateDay = date.getDate();
      
      const nowYear = now.getFullYear();
      const nowMonth = now.getMonth();
      const nowDay = now.getDate();
      
      // 计算日期差异（考虑时区）
      const diffTime = now - date;
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
      
      // 如果是同一年、同一月、同一天，则显示为'今天'
      if (dateYear === nowYear && dateMonth === nowMonth && dateDay === nowDay) {
        return '今天';
      } 
      // 如果是昨天
      else if (dateYear === nowYear && 
               dateMonth === nowMonth && 
               dateDay === nowDay - 1) {
        return '昨天';
      }
      // 如果是30天内
      else if (diffDays < 30 && diffDays > 0) {
        return `${diffDays}天前`;
      }
      // 否则显示具体日期
      else {
        return date.toLocaleDateString('zh-CN');
      }
    },
    
    searchNotes() {
      // 搜索功能由计算属性filteredNotes实现
    },
    
    debouncedSearch() {
      clearTimeout(this.debounceTimer);
      this.debounceTimer = setTimeout(() => {
        this.searchNotes();
      }, 300);
    },
    
    logout() {
      localStorage.removeItem('token');
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('username');
      this.$router.push('/login');
    },
    
    async exportNotes() {
      try {
        // 显示加载提示
        this.loading = true;
        
        // 调用后端API导出笔记
        const response = await window.fetchWithAuth('/api/export/notes/word', {
          method: 'GET',
          credentials: 'include'
        });
        
        if (!response.ok) {
          throw new Error('导出笔记失败: ' + response.statusText);
        }
        
        // 获取响应内容为Blob
        const blob = await response.blob();
        
        // 创建一个临时的URL指向Blob
        const url = window.URL.createObjectURL(blob);
        
        // 提取文件名
        const contentDisposition = response.headers.get('content-disposition');
        let filename = '我的笔记导出.docx';
        if (contentDisposition) {
          const match = contentDisposition.match(/filename="([^"]*)"/);
          if (match && match[1]) {
            filename = match[1];
          }
        }
        
        // 创建一个<a>标签用于下载
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        
        // 模拟点击下载
        link.click();
        
        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
      } catch (error) {
        console.error('Error exporting notes:', error);
        alert('导出笔记失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
.note-app {
  min-height: 100vh;
  background-color: #f7fafc;
  color: #2d3748;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

/* 顶部导航栏 */
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.app-title h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
}

.app-title .subtitle {
  font-size: 0.9rem;
  opacity: 0.9;
  margin-top: 0.25rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-bar {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 0.5rem 1rem;
  backdrop-filter: blur(4px);
}

.search-bar input {
  background: transparent;
  border: none;
  color: white;
  outline: none;
  width: 200px;
  padding: 0.25rem;
}

.search-bar input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.search-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0.25rem;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.search-btn:hover {
  opacity: 1;
}

.logout-btn {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: white;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.85rem;
      transition: background-color 0.2s;
    }

    .logout-btn:hover {
      background: rgba(255, 255, 255, 0.3);
    }
    
    .export-btn {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: white;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.85rem;
      transition: background-color 0.2s;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    
    .export-btn:hover {
      background: rgba(255, 255, 255, 0.3);
    }

/* 主内容区 */
.main-content {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  gap: 2rem;
}

/* 左侧分类栏 */
.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.category-section {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.category-section h3 {
  margin: 0 0 1.5rem 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: #4a5568;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  margin-bottom: 0.25rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95rem;
  border-left: 3px solid transparent;
}

.category-name {
  flex: 1;
}

.category-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.delete-category-btn {
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 0.25rem;
  opacity: 0;
  transition: opacity 0.2s;
}

.category-item:hover .delete-category-btn {
  opacity: 1;
}

.delete-category-btn:hover {
  color: #fc8181;
}

.category-item:hover {
  background: #f7fafc;
}

.category-item.active {
  background: #edf2f7;
  font-weight: 500;
}

.category-item.all-categories.active {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.count {
  background: #e2e8f0;
  color: #4a5568;
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 500;
}

.category-item.all-categories .count {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.add-category-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem 1rem;
  margin-top: 1rem;
  background: #f7fafc;
  border: 1px dashed #cbd5e0;
  border-radius: 6px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.add-category-btn:hover {
  background: #edf2f7;
  border-color: #a0aec0;
}

/* 右侧笔记列表 */
.notes-container {
  flex: 1;
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

/* 笔记网格 */
.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

/* 添加新笔记卡片 */
.add-note-card {
  background: white;
  border: 2px dashed #cbd5e0;
  border-radius: 8px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.2s;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-note-card:hover {
  background: #f7fafc;
  border-color: #a0aec0;
}

.add-note-content {
  text-align: center;
  color: #a0aec0;
}

.add-note-content span {
  display: block;
  margin-top: 1rem;
  font-size: 0.95rem;
}

/* 笔记卡片 */
.note-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  height: 200px;
  display: flex;
  flex-direction: column;
  border-left: 3px solid transparent;
}

.note-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.note-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
  color: #2d3748;
  flex: 1;
  margin-right: 0.5rem;
}

.important-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #a0aec0;
  padding: 0.25rem;
  transition: color 0.2s;
}

.note-card:nth-child(n) .important-btn svg {
  color: #f6ad55;
  fill: #f6ad55;
}

.note-content {
  flex: 1;
  color: #718096;
  line-height: 1.5;
  font-size: 0.95rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  font-size: 0.8rem;
  color: #a0aec0;
}

.note-category {
  background: #e2e8f0;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
  color: #a0aec0;
}

.empty-state p {
  margin: 1rem 0;
  font-size: 1rem;
}

.create-first-note {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: opacity 0.2s;
}

.create-first-note:hover {
  opacity: 0.9;
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
@media (max-width: 1024px) {
  .main-content {
    padding: 1rem;
  }
  
  .sidebar {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
  }
  
  .search-bar input {
    width: 150px;
  }
  
  .main-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .notes-grid {
    grid-template-columns: 1fr;
  }
}
</style>