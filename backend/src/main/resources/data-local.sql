MERGE INTO user (id, username, password, real_name, student_id, role, status) KEY(id) VALUES
  (1, 'admin', '$2b$10$IUl32ayOlyDjf0MbPjKztOB.m3Mgtn2G8XeqF1U3RDtX67RPwnVnK', '系统管理员', NULL, 'ADMIN', 1),
  (2, 'teacher1', '$2b$10$AuMjWDuWvQxQGFHGycj/VObi9Zxe8NDxRHuOLMktsrL5mzy12ZkPq', '张老师', NULL, 'TEACHER', 1),
  (3, 'teacher2', '$2b$10$AuMjWDuWvQxQGFHGycj/VObi9Zxe8NDxRHuOLMktsrL5mzy12ZkPq', '李老师', NULL, 'TEACHER', 1),
  (4, 'student1', '$2b$10$AuMjWDuWvQxQGFHGycj/VObi9Zxe8NDxRHuOLMktsrL5mzy12ZkPq', '王同学', '2024001', 'STUDENT', 1),
  (5, 'student2', '$2b$10$AuMjWDuWvQxQGFHGycj/VObi9Zxe8NDxRHuOLMktsrL5mzy12ZkPq', '李同学', '2024002', 'STUDENT', 1);

MERGE INTO course (id, course_name, course_code, teacher_id, classroom, start_time, end_time, week_day, semester, max_students, status) KEY(id) VALUES
  (1, '高等数学', 'CS101', 2, '教学楼A101', '08:00:00', '09:40:00', 1, '2024-2025-1', 50, 1),
  (2, '数据结构', 'CS102', 3, '教学楼B202', '10:00:00', '11:40:00', 2, '2024-2025-1', 50, 1),
  (100, '无限制签到测试课（可重复签到）', 'TEST-UNLIMITED', 2, '测试教室', '00:00:00', '23:59:59', 1, '功能测试', 100, 1),
  (101, '全天测试课（星期一）', 'TEST-DAY-1', 2, '测试教室', '00:15:00', '23:29:00', 1, '功能测试', 100, 1),
  (102, '全天测试课（星期二）', 'TEST-DAY-2', 2, '测试教室', '00:15:00', '23:29:00', 2, '功能测试', 100, 1),
  (103, '全天测试课（星期三）', 'TEST-DAY-3', 2, '测试教室', '00:15:00', '23:29:00', 3, '功能测试', 100, 1),
  (104, '全天测试课（星期四）', 'TEST-DAY-4', 2, '测试教室', '00:15:00', '23:29:00', 4, '功能测试', 100, 1),
  (105, '全天测试课（星期五）', 'TEST-DAY-5', 2, '测试教室', '00:15:00', '23:29:00', 5, '功能测试', 100, 1),
  (106, '全天测试课（星期六）', 'TEST-DAY-6', 2, '测试教室', '00:15:00', '23:29:00', 6, '功能测试', 100, 1),
  (107, '全天测试课（星期日）', 'TEST-DAY-7', 2, '测试教室', '00:15:00', '23:29:00', 7, '功能测试', 100, 1);

MERGE INTO student_course (id, student_id, course_id, status) KEY(id) VALUES
  (1, 4, 1, 1), (2, 4, 2, 1), (3, 5, 1, 1),
  (100, 4, 100, 1), (110, 5, 100, 1),
  (101, 4, 101, 1), (102, 4, 102, 1), (103, 4, 103, 1), (104, 4, 104, 1),
  (105, 4, 105, 1), (106, 4, 106, 1), (107, 4, 107, 1),
  (111, 5, 101, 1), (112, 5, 102, 1), (113, 5, 103, 1), (114, 5, 104, 1),
  (115, 5, 105, 1), (116, 5, 106, 1), (117, 5, 107, 1);
