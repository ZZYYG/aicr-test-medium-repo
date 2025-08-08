package api

import (
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"github.com/gorilla/mux"
)

// UserRequest 表示用户请求
type UserRequest struct {
	Username  string            `json:"username"`
	Email     string            `json:"email"`
	Password  string            `json:"password,omitempty"`
	FirstName string            `json:"first_name"`
	LastName  string            `json:"last_name"`
	Metadata  map[string]string `json:"metadata,omitempty"`
}

// UserResponse 表示用户响应
type UserResponse struct {
	ID        string            `json:"id"`
	Username  string            `json:"username"`
	Email     string            `json:"email"`
	FirstName string            `json:"first_name"`
	LastName  string            `json:"last_name"`
	Metadata  map[string]string `json:"metadata,omitempty"`
	CreatedAt time.Time         `json:"created_at"`
	UpdatedAt time.Time         `json:"updated_at"`
}

// UserHandler 处理用户相关的API请求
type UserHandler struct {
	service UserService
}

// UserService 定义用户服务接口
type UserService interface {
	CreateUser(req UserRequest) (*UserResponse, error)
	GetUser(id string) (*UserResponse, error)
	UpdateUser(id string, req UserRequest) (*UserResponse, error)
	DeleteUser(id string) error
	ListUsers(limit, offset int) ([]*UserResponse, int, error)
}

// NewUserHandler 创建新的用户处理器
func NewUserHandler(service UserService) *UserHandler {
	return &UserHandler{
		service: service,
	}
}

// RegisterRoutes 注册路由
func (h *UserHandler) RegisterRoutes(router *mux.Router) {
	router.HandleFunc("/users", h.CreateUser).Methods("POST")
	router.HandleFunc("/users/{id}", h.GetUser).Methods("GET")
	router.HandleFunc("/users/{id}", h.UpdateUser).Methods("PUT")
	router.HandleFunc("/users/{id}", h.DeleteUser).Methods("DELETE")
	router.HandleFunc("/users", h.ListUsers).Methods("GET")
}

// CreateUser 创建新用户
func (h *UserHandler) CreateUser(w http.ResponseWriter, r *http.Request) {
	var req UserRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "无效的请求数据", http.StatusBadRequest)
		return
	}

	resp, err := h.service.CreateUser(req)
	if err != nil {
		http.Error(w, "创建用户失败: "+err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(resp)
}

// GetUser 获取用户详情
func (h *UserHandler) GetUser(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]

	resp, err := h.service.GetUser(id)
	if err != nil {
		http.Error(w, "获取用户失败: "+err.Error(), http.StatusInternalServerError)
		return
	}

	if resp == nil {
		http.Error(w, "用户不存在", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// UpdateUser 更新用户
func (h *UserHandler) UpdateUser(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]

	var req UserRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "无效的请求数据", http.StatusBadRequest)
		return
	}

	resp, err := h.service.UpdateUser(id, req)
	if err != nil {
		http.Error(w, "更新用户失败: "+err.Error(), http.StatusInternalServerError)
		return
	}

	if resp == nil {
		http.Error(w, "用户不存在", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// DeleteUser 删除用户
func (h *UserHandler) DeleteUser(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]

	if err := h.service.DeleteUser(id); err != nil {
		http.Error(w, "删除用户失败: "+err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// ListUsers 列出所有用户
func (h *UserHandler) ListUsers(w http.ResponseWriter, r *http.Request) {
	limit := 10
	offset := 0

	if limitStr := r.URL.Query().Get("limit"); limitStr != "" {
		if val, err := strconv.Atoi(limitStr); err == nil && val > 0 {
			limit = val
		}
	}

	if offsetStr := r.URL.Query().Get("offset"); offsetStr != "" {
		if val, err := strconv.Atoi(offsetStr); err == nil && val >= 0 {
			offset = val
		}
	}

	users, total, err := h.service.ListUsers(limit, offset)
	if err != nil {
		http.Error(w, "获取用户列表失败: "+err.Error(), http.StatusInternalServerError)
		return
	}

	response := struct {
		Total int             `json:"total"`
		Users []*UserResponse `json:"users"`
	}{
		Total: total,
		Users: users,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}
