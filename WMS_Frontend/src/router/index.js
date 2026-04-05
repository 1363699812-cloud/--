import { createRouter, createWebHistory } from 'vue-router'
import Cookies from 'js-cookie'
import { useUserStore } from '@/store/user'

const Layout = () => import('@/layout/index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
    ],
  },
  {
    path: '/basic',
    component: Layout,
    meta: { title: '基础数据', icon: 'Grid' },
    children: [
      {
        path: 'warehouse',
        name: 'Warehouse',
        component: () => import('@/views/basic/warehouse.vue'),
        meta: { title: '仓库管理', icon: 'House', roles: ['admin', 'warehouse_keeper'] },
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/basic/category.vue'),
        meta: { title: '分类管理', icon: 'Menu', roles: ['admin', 'warehouse_keeper'] },
      },
      {
        path: 'material',
        name: 'Material',
        component: () => import('@/views/basic/material.vue'),
        meta: { title: '物资管理', icon: 'Box', roles: ['admin', 'warehouse_keeper'] },
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('@/views/basic/supplier.vue'),
        meta: { title: '供应商管理', icon: 'Van', roles: ['admin', 'warehouse_keeper', 'purchaser'] },
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/basic/customer.vue'),
        meta: { title: '客户管理', icon: 'User', roles: ['admin', 'warehouse_keeper', 'seller'] },
      },
    ],
  },
  {
    path: '/inbound',
    component: Layout,
    meta: { title: '入库管理', icon: 'Download', roles: ['admin', 'warehouse_keeper', 'purchaser'] },
    children: [
      {
        path: 'order',
        name: 'InboundOrder',
        component: () => import('@/views/inbound/order.vue'),
        meta: { title: '入库单管理', icon: 'Document', roles: ['admin', 'warehouse_keeper', 'purchaser'] },
      },
    ],
  },
  {
    path: '/outbound',
    component: Layout,
    meta: { title: '出库管理', icon: 'Upload', roles: ['admin', 'warehouse_keeper', 'seller'] },
    children: [
      {
        path: 'order',
        name: 'OutboundOrder',
        component: () => import('@/views/outbound/order.vue'),
        meta: { title: '出库单管理', icon: 'Document', roles: ['admin', 'warehouse_keeper', 'seller'] },
      },
    ],
  },
  {
    path: '/inventory',
    component: Layout,
    meta: { title: '库存管理', icon: 'Coin' },
    children: [
      {
        path: 'list',
        name: 'InventoryList',
        component: () => import('@/views/inventory/list.vue'),
        meta: { title: '库存查询', icon: 'Search' },
      },
      {
        path: 'transaction',
        name: 'InventoryTransaction',
        component: () => import('@/views/inventory/transaction.vue'),
        meta: { title: '库存流水', icon: 'Odometer' },
      },
      {
        path: 'alert',
        name: 'InventoryAlert',
        component: () => import('@/views/inventory/alert.vue'),
        meta: { title: '库存预警', icon: 'Bell', roles: ['admin', 'warehouse_keeper', 'purchaser'] },
      },
      {
        path: 'transfer',
        name: 'TransferOrder',
        component: () => import('@/views/inventory/transfer.vue'),
        meta: { title: '调拨管理', icon: 'Switch', roles: ['admin', 'warehouse_keeper'] },
      },
      {
        path: 'scrap',
        name: 'ScrapOrder',
        component: () => import('@/views/inventory/scrap.vue'),
        meta: { title: '报损报废', icon: 'Delete', roles: ['admin', 'warehouse_keeper'] },
      },
      {
        path: 'stocktake',
        name: 'StocktakeOrder',
        component: () => import('@/views/inventory/stocktake.vue'),
        meta: { title: '库存盘点', icon: 'Notebook', roles: ['admin', 'warehouse_keeper'] },
      },
    ],
  },
  {
    path: '/report',
    component: Layout,
    meta: { title: '报表统计', icon: 'DataAnalysis' },
    children: [
      {
        path: 'inbound-stats',
        name: 'InboundStats',
        component: () => import('@/views/report/inbound-stats.vue'),
        meta: { title: '入库统计', icon: 'TrendCharts' },
      },
      {
        path: 'outbound-stats',
        name: 'OutboundStats',
        component: () => import('@/views/report/outbound-stats.vue'),
        meta: { title: '出库统计', icon: 'TrendCharts' },
      },
      {
        path: 'category-stats',
        name: 'CategoryStats',
        component: () => import('@/views/report/category-stats.vue'),
        meta: { title: '分类统计', icon: 'PieChart' },
      },
      {
        path: 'abc-analysis',
        name: 'AbcAnalysis',
        component: () => import('@/views/report/abc-analysis.vue'),
        meta: { title: 'ABC分类分析', icon: 'DataLine' },
      },
      {
        path: 'turnover-rate',
        name: 'TurnoverRate',
        component: () => import('@/views/report/turnover-rate.vue'),
        meta: { title: '库存周转率', icon: 'Refresh' },
      },
    ],
  },
  {
    path: '/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'Setting', roles: ['admin'] },
    children: [
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/system/user.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] },
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('@/views/system/log.vue'),
        meta: { title: '操作日志', icon: 'Tickets', roles: ['admin'] },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - WMS仓库管理系统` : 'WMS仓库管理系统'
  const token = Cookies.get('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    const userStore = useUserStore()
    const role = userStore.role
    if (to.meta.roles && to.meta.roles.length > 0 && !to.meta.roles.includes(role)) {
      next('/dashboard')
    } else {
      next()
    }
  }
})

export default router
