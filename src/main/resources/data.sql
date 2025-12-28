-- 初始化测试数据
-- 插入测试用户 (密码都是 123456)
-- 买家用户
INSERT INTO users (id, username, password, full_name, email, phone, role, created_at, updated_at)
VALUES
(1, 'buyer', '$2a$10$BVuSir5tn3USI7Pg0NDy0e3fj21jyUS4AR2ECNkZuzx8cEQ0rOeKS', '测试买家', 'buyer@test.com', '13800000001', 'BUYER', NOW(), NOW()),
(2, 'seller', '$2a$10$BVuSir5tn3USI7Pg0NDy0e3fj21jyUS4AR2ECNkZuzx8cEQ0rOeKS', '测试卖家', 'seller@test.com', '13800000002', 'SELLER', NOW(), NOW()),
(3, 'user1', '$2a$10$BVuSir5tn3USI7Pg0NDy0e3fj21jyUS4AR2ECNkZuzx8cEQ0rOeKS', '张三', 'user1@test.com', '13800000003', 'BUYER', NOW(), NOW()),
(4, 'user2', '$2a$10$BVuSir5tn3USI7Pg0NDy0e3fj21jyUS4AR2ECNkZuzx8cEQ0rOeKS', '李四', 'user2@test.com', '13800000004', 'BUYER', NOW(), NOW());

-- 插入测试商品
INSERT INTO products (id, name, description, price, image_url, stock, seller_id, is_available, created_at, updated_at)
VALUES
(1, '智能手机', '高性能智能手机，8GB内存，128GB存储', 2999.00, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400', 50, 2, true, NOW(), NOW()),
(2, '笔记本电脑', '轻薄笔记本电脑，i7处理器，16GB内存', 6999.00, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w-400', 30, 2, true, NOW(), NOW()),
(3, '无线耳机', '蓝牙无线耳机，降噪功能，24小时续航', 399.00, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400', 100, 2, true, NOW(), NOW()),
(4, '运动手表', '智能运动手表，心率监测，GPS定位', 1299.00, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400', 40, 2, true, NOW(), NOW()),
(5, '咖啡机', '全自动咖啡机，一键制作多种咖啡', 1999.00, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400', 20, 2, true, NOW(), NOW()),
(6, '背包', '旅行背包，防水材质，多隔层设计', 199.00, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400', 80, 2, true, NOW(), NOW());

-- 插入测试订单
INSERT INTO orders (id, order_number, user_id, total_amount, shipping_address, status, created_at, updated_at, payment_method, payment_status)
VALUES
(1, 'ORD2023122000001', 1, 3398.00, '北京市海淀区中关村大街1号', 'PAID', DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW()), '微信支付', '已支付'),
(2, 'ORD2023121900002', 3, 1299.00, '上海市浦东新区陆家嘴环路100号', 'SHIPPED', DATEADD('DAY', -3, NOW()), DATEADD('DAY', -1, NOW()), '微信支付', '已支付'),
(3, 'ORD2023121800003', 4, 6999.00, '广州市天河区体育西路123号', 'DELIVERED', DATEADD('DAY', -7, NOW()), DATEADD('DAY', -2, NOW()), '微信支付', '已支付');

-- 插入订单项
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, subtotal, created_at)
VALUES
(1, 1, 1, 1, 2999.00, 2999.00, DATEADD('DAY', -5, NOW())),
(2, 1, 3, 1, 399.00, 399.00, DATEADD('DAY', -5, NOW())),
(3, 2, 4, 1, 1299.00, 1299.00, DATEADD('DAY', -3, NOW())),
(4, 3, 2, 1, 6999.00, 6999.00, DATEADD('DAY', -7, NOW()));

-- 插入用户活动日志
INSERT INTO user_activity_logs (id, user_id, activity_type, product_id, details, created_at)
VALUES
(1, 1, 'VIEW_PRODUCT', 1, '查看商品：智能手机', DATEADD('HOUR', -2, NOW())),
(2, 1, 'ADD_TO_CART', 1, '加入购物车：智能手机', DATEADD('HOUR', -1, NOW())),
(3, 1, 'PURCHASE', 1, '购买订单：ORD2023122000001', DATEADD('DAY', -5, NOW())),
(4, 3, 'VIEW_PRODUCT', 4, '查看商品：运动手表', DATEADD('HOUR', -4, NOW())),
(5, 3, 'PURCHASE', 4, '购买订单：ORD2023121900002', DATEADD('DAY', -3, NOW())),
(6, 4, 'VIEW_PRODUCT', 2, '查看商品：笔记本电脑', DATEADD('HOUR', -6, NOW())),
(7, 4, 'PURCHASE', 2, '购买订单：ORD2023121800003', DATEADD('DAY', -7, NOW()));

ALTER TABLE users ALTER COLUMN id RESTART WITH 5;
ALTER TABLE products ALTER COLUMN id RESTART WITH 7;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 4;
ALTER TABLE order_items ALTER COLUMN id RESTART WITH 5;
ALTER TABLE user_activity_logs ALTER COLUMN id RESTART WITH 8;