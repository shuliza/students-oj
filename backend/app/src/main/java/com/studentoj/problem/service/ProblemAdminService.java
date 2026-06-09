package com.studentoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.studentoj.problem.dto.ProblemAdminResponse;
import com.studentoj.problem.dto.ProblemSaveRequest;
import com.studentoj.problem.entity.ProblemEntity;
import com.studentoj.problem.entity.ProblemTestcaseEntity;
import com.studentoj.problem.mapper.ProblemMapper;
import com.studentoj.problem.mapper.ProblemTestcaseMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProblemAdminService {

    private static final Logger log = LoggerFactory.getLogger(ProblemAdminService.class);

    private final ProblemMapper problemMapper;
    private final ProblemTestcaseMapper testcaseMapper;

    public ProblemAdminService(ProblemMapper problemMapper, ProblemTestcaseMapper testcaseMapper) {
        this.problemMapper = problemMapper;
        this.testcaseMapper = testcaseMapper;
    }

    public List<ProblemAdminResponse> list() {
        List<ProblemEntity> problems = problemMapper.selectList(
                new QueryWrapper<ProblemEntity>().orderByAsc("id"));
        return problems.stream().map(p -> toResponse(p, false)).toList();
    }

    public ProblemAdminResponse detail(Long id) {
        ProblemEntity entity = problemMapper.selectById(id);
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        return toResponse(entity, true);
    }

    @Transactional
    public ProblemAdminResponse create(ProblemSaveRequest request) {
        validate(request);
        ProblemEntity entity = new ProblemEntity();
        applyRequest(entity, request);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        problemMapper.insert(entity);
        saveTestcases(entity.getId(), request.testcases());
        syncFirstTestcaseToProblem(entity, request.testcases());
        return detail(entity.getId());
    }

    @Transactional
    public ProblemAdminResponse update(Long id, ProblemSaveRequest request) {
        ProblemEntity entity = problemMapper.selectById(id);
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        validate(request);
        applyRequest(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());
        problemMapper.updateById(entity);
        testcaseMapper.deleteByProblem(id);
        saveTestcases(id, request.testcases());
        syncFirstTestcaseToProblem(entity, request.testcases());
        return detail(id);
    }

    /** 软删除：status=0，保留 submission 历史引用。 */
    public void delete(Long id) {
        ProblemEntity entity = problemMapper.selectById(id);
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在");
        }
        entity.setStatus(0);
        entity.setUpdatedAt(LocalDateTime.now());
        problemMapper.updateById(entity);
    }

    private void validate(ProblemSaveRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "题目标题不能为空");
        }
        if (request.answerSql() == null || request.answerSql().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参考答案 SQL 不能为空");
        }
        boolean hasTestcase = request.testcases() != null
                && request.testcases().stream().anyMatch(s -> s != null && !s.isBlank());
        if (!hasTestcase) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少需要一个测试用例（建表与造数据 SQL）");
        }
    }

    private void applyRequest(ProblemEntity entity, ProblemSaveRequest request) {
        entity.setTitle(request.title().trim());
        entity.setDifficulty(normalizeDifficulty(request.difficulty()));
        entity.setTags(request.tags() == null ? "" : String.join(",", request.tags().stream()
                .filter(t -> t != null && !t.isBlank()).map(String::trim).toList()));
        entity.setDescription(request.description() == null ? "" : request.description());
        entity.setSampleInput(request.sampleInput() == null ? "" : request.sampleInput());
        entity.setSampleOutput(request.sampleOutput() == null ? "" : request.sampleOutput());
        entity.setAnswerSql(request.answerSql().trim());
        entity.setStatus(request.status() == null ? 1 : request.status());
    }

    private void saveTestcases(Long problemId, List<String> testcases) {
        if (testcases == null) {
            return;
        }
        int ordinal = 1;
        for (String sql : testcases) {
            if (sql == null || sql.isBlank()) {
                continue;
            }
            ProblemTestcaseEntity tc = new ProblemTestcaseEntity();
            tc.setProblemId(problemId);
            tc.setOrdinal(ordinal++);
            tc.setInitSql(sql.trim());
            tc.setCreatedAt(LocalDateTime.now());
            testcaseMapper.insert(tc);
        }
    }

    /** 把第 1 个测试用例的 init_sql 回写到 problem.init_sql，保证旧判题回退路径仍可用。 */
    private void syncFirstTestcaseToProblem(ProblemEntity entity, List<String> testcases) {
        if (testcases == null) {
            return;
        }
        String first = testcases.stream().filter(s -> s != null && !s.isBlank()).findFirst().orElse(null);
        if (first != null) {
            entity.setInitSql(first.trim());
            problemMapper.updateById(entity);
        }
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String normalizeDifficulty(String raw) {
        if (raw == null || raw.isBlank()) {
            return "EASY";
        }
        String up = raw.trim().toUpperCase();
        return switch (up) {
            case "EASY", "MEDIUM", "HARD" -> up;
            case "简单" -> "EASY";
            case "中等" -> "MEDIUM";
            case "困难" -> "HARD";
            default -> "EASY";
        };
    }

    private ProblemAdminResponse toResponse(ProblemEntity entity, boolean includeTestcases) {
        List<String> testcases = new ArrayList<>();
        if (includeTestcases) {
            testcaseMapper.selectByProblem(entity.getId())
                    .forEach(tc -> testcases.add(tc.getInitSql()));
            if (testcases.isEmpty() && entity.getInitSql() != null && !entity.getInitSql().isBlank()) {
                testcases.add(entity.getInitSql());
            }
        }
        return new ProblemAdminResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDifficulty(),
                splitTags(entity.getTags()),
                entity.getDescription(),
                entity.getSampleInput(),
                entity.getSampleOutput(),
                entity.getAnswerSql(),
                testcases,
                entity.getStatus(),
                null,
                null
        );
    }

    private static final String[] IMPORT_COLS =
            {"标题", "难度", "标签", "描述", "输入结构", "期望输出", "参考答案SQL", "建表与造数据SQL"};

    public byte[] exportTemplate() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("题目导入模板");
            Row header = sheet.createRow(0);
            for (int i = 0; i < IMPORT_COLS.length; i++) {
                header.createCell(i).setCellValue(IMPORT_COLS[i]);
            }
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("查询高分学生名单");
            example.createCell(1).setCellValue("EASY");
            example.createCell(2).setCellValue("SELECT,WHERE");
            example.createCell(3).setCellValue("从 student 与 score 表中查询成绩不低于 80 分的记录。");
            example.createCell(4).setCellValue("student(id, name), score(student_id, course, score)");
            example.createCell(5).setCellValue("name | course | score");
            example.createCell(6).setCellValue("SELECT s.name, c.course, c.score FROM student s JOIN score c ON s.id = c.student_id WHERE c.score >= 80;");
            example.createCell(7).setCellValue("CREATE TABLE student(id INT, name VARCHAR(32)); INSERT INTO student VALUES (1,'张三');");
            for (int i = 0; i < IMPORT_COLS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "生成模板失败");
        }
    }

    @Transactional
    public int importProblems(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请上传文件");
        }
        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String title = cellStr(row.getCell(0));
                String answerSql = cellStr(row.getCell(6));
                String initSql = cellStr(row.getCell(7));
                if (title.isEmpty() || answerSql.isEmpty() || initSql.isEmpty()) {
                    continue;
                }
                ProblemSaveRequest req = new ProblemSaveRequest(
                        title,
                        cellStr(row.getCell(1)),
                        splitTags(cellStr(row.getCell(2))),
                        cellStr(row.getCell(3)),
                        cellStr(row.getCell(4)),
                        cellStr(row.getCell(5)),
                        answerSql,
                        List.of(initSql),
                        1);
                try {
                    create(req);
                    count++;
                } catch (Exception e) {
                    log.warn("Skip problem row {}: {}", i, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件解析失败");
        }
        return count;
    }

    private String cellStr(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }
}
