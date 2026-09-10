package com.attendance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FrontendActionIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void teacherCreatesEditsDisablesRestoresAndDeletesOwnCourse() throws Exception {
        String token = login("teacher1", "password123");
        String created = mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson("前端操作测试课", "UI-ACTION-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        long courseId = objectMapper.readTree(created).path("data").asLong();

        mockMvc.perform(put("/courses/" + courseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson("前端操作测试课（已编辑）", "UI-ACTION-1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(put("/courses/" + courseId + "/status").header("Authorization", "Bearer " + token).param("status","0"))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(put("/courses/" + courseId + "/status").header("Authorization", "Bearer " + token).param("status","1"))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(delete("/courses/" + courseId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        String admin=login("admin","admin123");
        mockMvc.perform(post("/courses").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON).content(courseJson("越权课程","ADMIN-NO")))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanCreateUpdateDisableAndDeleteManagedUsers() throws Exception {
        String admin = login("admin", "admin123");
        String created = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("ui_student", "界面学生", "UI20260909", "STUDENT", "Password123")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        long userId = objectMapper.readTree(created).path("data").path("id").asLong();

        mockMvc.perform(put("/users/" + userId + "/admin")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("ui_student", "界面学生已修改", "UI20260909", "STUDENT", "")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(put("/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + admin).param("status", "0"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(delete("/users/" + userId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void teacherCanAddAStudentAndEditOwnCourseInfo() throws Exception {
        String admin = login("admin", "admin123");
        String teacher = login("teacher1", "password123");
        String userBody = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("class_student", "课堂学生", "CLASS20260909", "STUDENT", "Password123")))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getContentAsString();
        long studentId = objectMapper.readTree(userBody).path("data").path("id").asLong();

        mockMvc.perform(post("/courses/1/students").header("Authorization", "Bearer " + teacher)
                        .param("studentNumber", "CLASS20260909"))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/courses/1/students").header("Authorization", "Bearer " + teacher))
                .andExpect(jsonPath("$.data[?(@.id == " + studentId + ")]").exists());
        mockMvc.perform(put("/courses/1").header("Authorization", "Bearer " + teacher)
                        .contentType(MediaType.APPLICATION_JSON).content(courseJson("教师越权修改", "CS101")))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void teacherAssignsGroupMembersAndLeader() throws Exception {
        String token = login("teacher1", "password123");
        String created = mockMvc.perform(post("/groups")
                        .header("Authorization", "Bearer " + token).param("courseId","1").param("name", "接口自检小组").param("leaderId","4"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        long groupId = objectMapper.readTree(created).path("data").path("id").asLong();

        mockMvc.perform(post("/groups/" + groupId + "/members")
                        .header("Authorization", "Bearer " + token).param("studentId", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.members.length()").value(2));
        mockMvc.perform(put("/groups/" + groupId + "/leader/5").header("Authorization","Bearer "+token))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(put("/groups/" + groupId + "/leader/4").header("Authorization","Bearer "+token))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(delete("/groups/" + groupId + "/members/5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void leaveSubmitAndReviewButtonsPersistTheWorkflow() throws Exception {
        String student = login("student1", "password123");
        String teacher = login("teacher1", "password123");
        LocalDate leaveDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        String submitted = mockMvc.perform(post("/attendance/leaves")
                        .header("Authorization", "Bearer " + student)
                        .param("courseId", "1")
                        .param("leaveDate", leaveDate.toString())
                        .param("reason", "前端操作自检"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        long leaveId = objectMapper.readTree(submitted).path("data").path("id").asLong();

        mockMvc.perform(put("/attendance/leaves/" + leaveId + "/review")
                        .header("Authorization", "Bearer " + teacher)
                        .param("approved", "true").param("comment", "同意"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/attendance/leaves").header("Authorization", "Bearer " + student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));
    }

    @Test
    void teacherCanModifyAndCancelAttendanceSession() throws Exception {
        String teacher=login("teacher1","password123");
        mockMvc.perform(post("/attendance/sessions/1").header("Authorization","Bearer "+teacher).param("minutes","10"))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(put("/attendance/sessions/1").header("Authorization","Bearer "+teacher).param("minutes","15").param("lateMinutes","5"))
                .andExpect(jsonPath("$.code").value(200)).andExpect(jsonPath("$.data.active").value(true));
        mockMvc.perform(delete("/attendance/sessions/1").header("Authorization","Bearer "+teacher))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void studentRequestsAndTeacherApprovesOneFaceReEnrollment() throws Exception {
        jdbc.update("UPDATE user SET face_features='REGISTERED' WHERE id=4");
        String student=login("student1","password123"),teacher=login("teacher1","password123");
        mockMvc.perform(post("/face/re-enroll/request").header("Authorization","Bearer "+student).param("reason","原照片不清晰"))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/face/re-enroll/requests").header("Authorization","Bearer "+teacher))
                .andExpect(jsonPath("$.code").value(200)).andExpect(jsonPath("$.data[0].status").value("PENDING"));
        mockMvc.perform(put("/face/re-enroll/4/approve").header("Authorization","Bearer "+teacher))
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/face/features/4").header("Authorization","Bearer "+student))
                .andExpect(jsonPath("$.data.reEnrollAllowed").value(true));
    }

    private String courseJson(String name, String code) {
        return "{\"courseName\":\"" + name + "\",\"courseCode\":\"" + code
                + "\",\"classroom\":\"自检教室\",\"startTime\":\"08:00\",\"endTime\":\"09:40\""
                + ",\"teacherId\":2,\"weekDay\":1,\"semester\":\"2026-2027-1\",\"maxStudents\":50}";
    }

    private String userJson(String username, String realName, String number, String role, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password
                + "\",\"realName\":\"" + realName + "\",\"studentId\":\"" + number
                + "\",\"className\":\"计算机2401班\",\"role\":\"" + role + "\",\"status\":1}";
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.path("data").path("token").asText();
    }
}
