-- ============================================================
-- PHẦN 1: XÓA DATABASE CŨ (NẾU CÓ)
-- ============================================================
USE master;
GO

IF EXISTS (SELECT 1 FROM sys.databases WHERE name = 'ProjectManagementDB')
BEGIN
    ALTER DATABASE ProjectManagementDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ProjectManagementDB;
END
GO

--  HỆ THỐNG QUẢN LÝ DỰ ÁN SINH VIÊN APTECH - SEMESTER 2
--  Database: SQL Server 2019+
--  Version: 3.2 (Updated by AntiGravity AI)
--    + Account.Role:          Thêm Role = 4 (Staff / Giáo vụ)
--    + Class.ManagerID:       Khóa ngoại tham chiếu đến Staff(StaffID) để xác định Giáo vụ quản lý lớp
--    + Student.CreatedByStaffId: Khóa ngoại tham chiếu đến Staff(StaffID) để theo dõi ai tạo sinh viên
--    + Account.PhotoUrl:      (avatar) URL/path
--    + OtpVerification:       (xác thực OTP qua email)
--    + sp_GenerateOtp:        (tạo mã OTP, trả về cho backend gửi mail)
--    + sp_VerifyOtp:          (xác thực mã OTP, đổi mật khẩu)
CREATE DATABASE ProjectManagementDB COLLATE Vietnamese_CI_AS;
GO

USE ProjectManagementDB;
GO

-- ============================================================
-- PHẦN 3: TẠO BẢNG ACCOUNT (Không phụ thuộc bảng nào)
-- ============================================================
CREATE TABLE Account (
    AccountID    INT            IDENTITY(1,1) PRIMARY KEY,
    Username     NVARCHAR(50)   NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255)  NOT NULL,
    [Role]       TINYINT        NOT NULL CHECK ([Role] IN (1, 2, 3, 4)),
    IsFirstLogin BIT            NOT NULL DEFAULT 1,
    PhotoUrl     NVARCHAR(500)  NULL,
    IsActive     BIT            NOT NULL DEFAULT 1,
    CreatedAt    DATETIME       NOT NULL DEFAULT GETDATE()
);
GO

-- PHẦN 4: TẠO BẢNG STAFF (Tham chiếu Account)
-- ============================================================
CREATE TABLE Staff (
    StaffID   INT           IDENTITY(1,1) PRIMARY KEY,
    FullName  NVARCHAR(100) NOT NULL,
    Email     NVARCHAR(100) NOT NULL UNIQUE,
    AccountID INT           NOT NULL UNIQUE REFERENCES Account(AccountID)
);
GO

-- ============================================================
-- PHẦN 5: TẠO BẢNG CLASS (Tham chiếu Staff.ManagerID)
-- ============================================================
CREATE TABLE Class (
    ClassID      INT           IDENTITY(1,1) PRIMARY KEY,
    ClassName    NVARCHAR(100) NOT NULL,
    AcademicYear NVARCHAR(10)  NOT NULL,
    ManagerID    INT           NULL REFERENCES Staff(StaffID),
    CreatedAt    DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
-- PHẦN 6: TẠO BẢNG STUDENT (Tham chiếu Account và Class)
-- 2. OTP_VERIFICATION  [v3: bảng mới]
-- ============================================================
/*
  Dùng cho 2 luồng:
    Purpose 1 = ChangePassword  (student/staff muốn đổi mật khẩu)
    Purpose 2 = FirstLogin      (sinh viên đăng nhập lần đầu bị ép đổi pass)

  Luồng chuẩn:
    1. Backend nhận yêu cầu từ client → gọi sp_GenerateOtp(@AccountID, @Purpose)
    2. SP insert bản ghi OTP, trả về OtpCode (6 chữ số)
    3. Backend gửi email chứa mã OTP đến địa chỉ của account
    4. User nhập mã → client gọi sp_VerifyOtp(@AccountID, @Purpose, @InputCode, @NewPasswordHash)
    5. SP kiểm tra mã → nếu đúng: đổi pass + đánh dấu IsUsed = 1

  Bảo vệ brute-force:
    AttemptCount tăng mỗi lần nhập sai.
    Khi AttemptCount >= 5 → OTP bị khóa (IsUsed = 1) dù chưa dùng đúng.

  Mỗi lần gọi sp_GenerateOtp sẽ vô hiệu hóa các OTP cũ cùng AccountID + Purpose
  (set IsUsed = 1) trước khi tạo mới, tránh nhiều OTP active song song.

  Không lưu trực tiếp OtpCode dạng plain — lưu hash nếu muốn bảo mật cao hơn.
  Ở đây lưu plain để đơn giản cho dự án sinh viên (OTP 6 số, TTL 5 phút, max 5 lần thử).
*/
CREATE TABLE OtpVerification (
    OtpID        INT           IDENTITY(1,1) PRIMARY KEY,
    AccountID    INT           NOT NULL REFERENCES Account(AccountID),
    Email        NVARCHAR(100) NOT NULL,               -- email nhận OTP (snapshot tại thời điểm tạo)
    OtpCode      NVARCHAR(6)   NOT NULL,               -- 6 chữ số, VD: '483921'
    Purpose      TINYINT       NOT NULL
                               CHECK (Purpose IN (1, 2)),
                                                       -- 1=ChangePassword, 2=FirstLogin
    ExpiresAt    DATETIME      NOT NULL,               -- GETDATE() + 5 phút
    IsUsed       BIT           NOT NULL DEFAULT 0,     -- 1 = đã dùng hoặc bị khóa
    AttemptCount INT           NOT NULL DEFAULT 0,     -- số lần nhập sai
    CreatedAt    DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- Index để query nhanh OTP còn hiệu lực của một account
CREATE INDEX IX_OtpVerification_AccountID_Purpose
    ON OtpVerification(AccountID, Purpose)
    INCLUDE (OtpCode, ExpiresAt, IsUsed, AttemptCount);
GO

-- ============================================================
-- 3. STAFF
-- ============================================================
CREATE TABLE Staff (
    StaffID   INT           IDENTITY(1,1) PRIMARY KEY,
    FullName  NVARCHAR(100) NOT NULL,
    Email     NVARCHAR(100) NOT NULL UNIQUE,
    AccountID INT           NOT NULL UNIQUE REFERENCES Account(AccountID)
);
GO

-- ============================================================
-- 4. CLASS [v3.1: Thêm ManagerID để Giáo vụ quản lý lớp độc lập]
-- ============================================================
CREATE TABLE Class (
    ClassID      INT           IDENTITY(1,1) PRIMARY KEY,
    ClassName    NVARCHAR(100) NOT NULL,
    AcademicYear NVARCHAR(10)  NOT NULL,
    ManagerID    INT           NULL REFERENCES Staff(StaffID), -- Giáo vụ phụ trách lớp
    CreatedAt    DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

CREATE TABLE Student (
    StudentID        INT           IDENTITY(1,1) PRIMARY KEY,
    StudentCode      NVARCHAR(20)  NOT NULL UNIQUE,
    FullName         NVARCHAR(100) NOT NULL,
    Email            NVARCHAR(100) NOT NULL UNIQUE,
    ClassID          INT           NOT NULL REFERENCES Class(ClassID),
    AccountID        INT           NOT NULL UNIQUE REFERENCES Account(AccountID),
    CreatedByStaffId INT           NULL REFERENCES Staff(StaffID)
);
GO

-- ============================================================
-- PHẦN 7: TẠO BẢNG OTP_VERIFICATION
-- ============================================================
CREATE TABLE OtpVerification (
    OtpID        INT           IDENTITY(1,1) PRIMARY KEY,
    AccountID    INT           NOT NULL REFERENCES Account(AccountID),
    Email        NVARCHAR(100) NOT NULL,
    OtpCode      NVARCHAR(6)   NOT NULL,
    Purpose      TINYINT       NOT NULL CHECK (Purpose IN (1, 2)),
    ExpiresAt    DATETIME      NOT NULL,
    IsUsed       BIT           NOT NULL DEFAULT 0,
    AttemptCount INT           NOT NULL DEFAULT 0,
    CreatedAt    DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX IX_OtpVerification_AccountID_Purpose
    ON OtpVerification(AccountID, Purpose)
    INCLUDE (OtpCode, ExpiresAt, IsUsed, AttemptCount);
GO

-- ============================================================
-- PHẦN 8: TẠO BẢNG PROJECT_GROUP
-- ============================================================
CREATE TABLE ProjectGroup (
    GroupID   INT           IDENTITY(1,1) PRIMARY KEY,
    ClassID   INT           NOT NULL REFERENCES Class(ClassID),
    GroupName NVARCHAR(100) NOT NULL,
    CreatedAt DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
-- PHẦN 9: TẠO BẢNG PROJECT
-- ============================================================
CREATE TABLE Project (
    ProjectID     INT           IDENTITY(1,1) PRIMARY KEY,
    GroupID       INT           NOT NULL UNIQUE REFERENCES ProjectGroup(GroupID),
    Title         NVARCHAR(200) NOT NULL,
    [Description] NVARCHAR(MAX) NULL,
    Semester      NVARCHAR(20)  NOT NULL,
    StartDate     DATE          NOT NULL,
    EndDate       DATE          NOT NULL,
    ReportDate    DATE          NOT NULL,
    AdvisorID     INT           NOT NULL REFERENCES Staff(StaffID),
    CreatedBy     INT           NOT NULL REFERENCES Staff(StaffID),
    [Status]      TINYINT       NOT NULL DEFAULT 1 CHECK ([Status] IN (1, 2)),
    CreatedAt     DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_Project_Dates CHECK (StartDate <= EndDate AND ReportDate >= DATEADD(day, -3, EndDate) AND ReportDate <= EndDate)
);
GO

-- ============================================================
-- PHẦN 10: TẠO BẢNG GROUP_MEMBER
-- ============================================================
CREATE TABLE GroupMember (
    MemberID       INT           IDENTITY(1,1) PRIMARY KEY,
    GroupID        INT           NOT NULL REFERENCES ProjectGroup(GroupID),
    StudentID      INT           NOT NULL REFERENCES Student(StudentID),
    [Role]         TINYINT       NOT NULL DEFAULT 2 CHECK ([Role] IN (1, 2)),
    [Status]       TINYINT       NOT NULL DEFAULT 1 CHECK ([Status] IN (1, 2)),
    AbandonCount   INT           NOT NULL DEFAULT 0,
    JoinedAt       DATETIME      NOT NULL DEFAULT GETDATE(),
    ExcludedAt     DATETIME      NULL,
    ExcludedBy     INT           NULL REFERENCES Staff(StaffID),
    ExcludedReason NVARCHAR(500) NULL,
    CONSTRAINT UQ_GroupMember UNIQUE (GroupID, StudentID)
);
GO

CREATE UNIQUE INDEX UX_GroupMember_OneLeader
    ON GroupMember(GroupID)
    WHERE [Role] = 1 AND [Status] = 1;
GO

-- ============================================================
-- PHẦN 11: TẠO BẢNG TASK
-- ============================================================
CREATE TABLE Task (
    TaskID             INT           IDENTITY(1,1) PRIMARY KEY,
    GroupID            INT           NOT NULL REFERENCES ProjectGroup(GroupID),
    Title              NVARCHAR(200) NOT NULL,
    [Description]      NVARCHAR(MAX) NULL,
    EstimatedStartDate DATETIME      NOT NULL,
    EstimatedEndDate   DATETIME      NOT NULL,
    ActualStartDate    DATETIME      NULL,
    ActualEndDate      DATETIME      NULL,
    [Status]           TINYINT       NOT NULL DEFAULT 1 CHECK ([Status] IN (1,2,3,4,5)),
    AssignedTo         INT           NULL REFERENCES Student(StudentID),
    ReviewedBy         INT           NULL REFERENCES Student(StudentID),
    CreatedBy          INT           NOT NULL REFERENCES Student(StudentID),
    IsLate             BIT           NOT NULL DEFAULT 0,
    CreatedAt          DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_Task_Dates CHECK (EstimatedStartDate <= EstimatedEndDate)
);
GO

-- ============================================================
-- PHẦN 12: TẠO BẢNG TASK_REVISION
-- ============================================================
CREATE TABLE TaskRevision (
    RevisionID INT           IDENTITY(1,1) PRIMARY KEY,
    TaskID     INT           NOT NULL REFERENCES Task(TaskID),
    ReviewedBy INT           NOT NULL REFERENCES Student(StudentID),
    Note       NVARCHAR(MAX) NOT NULL,
    CreatedAt  DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
-- PHẦN 13: TẠO BẢNG TASK_STATUS_HISTORY
-- ============================================================
CREATE TABLE TaskStatusHistory (
    HistoryID  INT           IDENTITY(1,1) PRIMARY KEY,
    TaskID     INT           NOT NULL REFERENCES Task(TaskID),
    FromStatus TINYINT       NOT NULL CHECK (FromStatus IN (1,2,3,4,5)),
    ToStatus   TINYINT       NOT NULL CHECK (ToStatus   IN (1,2,3,4,5)),
    ChangedBy  INT           NOT NULL REFERENCES Account(AccountID),
    ChangedAt  DATETIME      NOT NULL DEFAULT GETDATE(),
    Note       NVARCHAR(500) NULL
);
GO

-- ============================================================
-- PHẦN 14: TẠO BẢNG TASK_ABANDON_LOG
-- ============================================================
CREATE TABLE TaskAbandonLog (
    LogID       INT           IDENTITY(1,1) PRIMARY KEY,
    TaskID      INT           NOT NULL REFERENCES Task(TaskID),
    StudentID   INT           NOT NULL REFERENCES Student(StudentID),
    AbandonedAt DATETIME      NOT NULL DEFAULT GETDATE(),
    Note        NVARCHAR(500) NULL
);
GO

-- ============================================================
-- PHẦN 15: TẠO BẢNG MESSAGE
-- ============================================================
CREATE TABLE [Message] (
    MessageID  INT           IDENTITY(1,1) PRIMARY KEY,
    SenderID   INT           NOT NULL REFERENCES Staff(StaffID),
    ReceiverID INT           NOT NULL REFERENCES Student(StudentID),
    TaskID     INT           NULL REFERENCES Task(TaskID),
    Content    NVARCHAR(MAX) NOT NULL,
    SentAt     DATETIME      NOT NULL DEFAULT GETDATE(),
    IsRead     BIT           NOT NULL DEFAULT 0
);
GO

-- ============================================================
-- PHẦN 16: TẠO INDEXES
-- ============================================================
CREATE INDEX IX_Student_ClassID          ON Student(ClassID);
CREATE INDEX IX_Project_GroupID          ON Project(GroupID);
CREATE INDEX IX_Project_Semester         ON Project(Semester);
CREATE INDEX IX_ProjectGroup_ClassID     ON ProjectGroup(ClassID);
CREATE INDEX IX_GroupMember_GroupID      ON GroupMember(GroupID);
CREATE INDEX IX_GroupMember_StudentID    ON GroupMember(StudentID);
CREATE INDEX IX_Task_GroupID             ON Task(GroupID);
CREATE INDEX IX_Task_Status              ON Task([Status]);
CREATE INDEX IX_Task_AssignedTo          ON Task(AssignedTo);
CREATE INDEX IX_Task_EstimatedStartDate  ON Task(EstimatedStartDate);
CREATE INDEX IX_Task_EstimatedEndDate    ON Task(EstimatedEndDate);
CREATE INDEX IX_TaskRevision_TaskID      ON TaskRevision(TaskID);
CREATE INDEX IX_TaskStatusHistory_TaskID ON TaskStatusHistory(TaskID);
CREATE INDEX IX_TaskAbandonLog_TaskID    ON TaskAbandonLog(TaskID);
CREATE INDEX IX_TaskAbandonLog_StudentID ON TaskAbandonLog(StudentID);
CREATE INDEX IX_Message_ReceiverID       ON [Message](ReceiverID);
CREATE INDEX IX_Message_TaskID           ON [Message](TaskID);
GO

-- ============================================================
-- PHẦN 17: STORED PROCEDURES
-- ============================================================
CREATE OR ALTER PROCEDURE sp_GenerateOtp
    @AccountID INT,
    @Purpose   TINYINT,
    @OtpCode   NVARCHAR(6) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Email NVARCHAR(100);
    SELECT @Email = COALESCE(
        (SELECT Email FROM Student WHERE AccountID = @AccountID),
        (SELECT Email FROM Staff   WHERE AccountID = @AccountID)
    );

    IF @Email IS NULL
    BEGIN
        RAISERROR(N'Không tìm thấy email cho AccountID %d', 16, 1, @AccountID);
        RETURN;
    END

    UPDATE OtpVerification
    SET IsUsed = 1
    WHERE AccountID = @AccountID
      AND Purpose   = @Purpose
      AND IsUsed    = 0;

    SET @OtpCode = RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS NVARCHAR), 6);

    INSERT INTO OtpVerification (AccountID, Email, OtpCode, Purpose, ExpiresAt)
    VALUES (@AccountID, @Email, @OtpCode, @Purpose, DATEADD(MINUTE, 5, GETDATE()));
END;
GO

CREATE OR ALTER PROCEDURE sp_VerifyOtp
    @AccountID       INT,
    @Purpose         TINYINT,
    @InputCode       NVARCHAR(6),
    @NewPasswordHash NVARCHAR(255),
    @ResultCode      INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    DECLARE @OtpID        INT;
    DECLARE @OtpCode      NVARCHAR(6);
    DECLARE @ExpiresAt    DATETIME;
    DECLARE @IsUsed       BIT;
    DECLARE @AttemptCount INT;

    SELECT TOP 1
        @OtpID        = OtpID,
        @OtpCode      = OtpCode,
        @ExpiresAt    = ExpiresAt,
        @IsUsed       = IsUsed,
        @AttemptCount = AttemptCount
    FROM OtpVerification
    WHERE AccountID = @AccountID
      AND Purpose   = @Purpose
    ORDER BY CreatedAt DESC;

    IF @OtpID IS NULL
    BEGIN
        SET @ResultCode = 4;
        ROLLBACK TRANSACTION;
        RETURN;
    END

    IF @IsUsed = 1 OR @ExpiresAt < GETDATE()
    BEGIN
        SET @ResultCode = 3;
        ROLLBACK TRANSACTION;
        RETURN;
    END

    IF @OtpCode != @InputCode
    BEGIN
        DECLARE @NewAttempt INT = @AttemptCount + 1;

        UPDATE OtpVerification
        SET AttemptCount = @NewAttempt,
            IsUsed       = CASE WHEN @NewAttempt >= 5 THEN 1 ELSE 0 END
        WHERE OtpID = @OtpID;

        SET @ResultCode = CASE WHEN @NewAttempt >= 5 THEN 2 ELSE 1 END;
        COMMIT TRANSACTION;
        RETURN;
    END

    UPDATE OtpVerification
    SET IsUsed = 1
    WHERE OtpID = @OtpID;

    UPDATE Account
    SET PasswordHash = @NewPasswordHash,
        IsFirstLogin = 0
    WHERE AccountID = @AccountID;

    SET @ResultCode = 0;
    COMMIT TRANSACTION;
END;
GO

-- ============================================================
-- PHẦN 18: TRIGGERS
-- ============================================================
CREATE OR ALTER TRIGGER trg_TaskAbandonLog_AfterInsert
ON TaskAbandonLog
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH TotalAbandons AS (
        SELECT
            pg.GroupID,
            tal.StudentID,
            COUNT(*) AS TotalCount
        FROM TaskAbandonLog tal
        INNER JOIN Task t        ON t.TaskID   = tal.TaskID
        INNER JOIN ProjectGroup pg ON pg.GroupID = t.GroupID
        WHERE EXISTS (
            SELECT 1 FROM INSERTED i
            WHERE i.StudentID = tal.StudentID
              AND i.TaskID IN (SELECT TaskID FROM Task WHERE GroupID = pg.GroupID)
        )
        GROUP BY pg.GroupID, tal.StudentID
    )
    UPDATE gm
    SET
        gm.AbandonCount   = ta.TotalCount,
        gm.[Status]       = CASE WHEN ta.TotalCount >= 3 THEN 2 ELSE gm.[Status] END,
        gm.ExcludedAt     = CASE WHEN ta.TotalCount >= 3 AND gm.[Status] = 1
                                 THEN GETDATE() ELSE gm.ExcludedAt END,
        gm.ExcludedReason = CASE WHEN ta.TotalCount >= 3 AND gm.[Status] = 1
                                 THEN N'Tự động loại: bỏ xác nhận task 3 lần'
                                 ELSE gm.ExcludedReason END
    FROM GroupMember gm
    INNER JOIN TotalAbandons ta ON ta.GroupID   = gm.GroupID
                                AND ta.StudentID = gm.StudentID;
END;
GO

CREATE OR ALTER TRIGGER trg_Task_CheckLate
ON Task
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Task
    SET IsLate = 1
    FROM Task t
    INNER JOIN INSERTED i ON i.TaskID = t.TaskID
    WHERE i.ActualEndDate IS NOT NULL
      AND i.ActualEndDate > i.EstimatedEndDate;
END;
GO

-- ============================================================
-- PHẦN 19: VIEW
-- ============================================================
CREATE OR ALTER VIEW vw_TaskOverview AS
SELECT
    p.ProjectID,
    p.Title              AS ProjectTitle,
    p.Semester,
    c.ClassName,
    pg.GroupID,
    pg.GroupName,
    t.TaskID,
    t.Title              AS TaskTitle,
    t.[Description]      AS TaskDetail,
    t.EstimatedStartDate,
    t.EstimatedEndDate,
    t.ActualStartDate,
    t.ActualEndDate,
    t.[Status]           AS TaskStatus,
    CASE t.[Status]
        WHEN 1 THEN N'Chờ thực hiện'
        WHEN 2 THEN N'Đang thực hiện'
        WHEN 3 THEN N'Đang kiểm tra'
        WHEN 4 THEN N'Đang chỉnh sửa'
        WHEN 5 THEN N'Hoàn thành'
    END                  AS TaskStatusLabel,
    t.IsLate,
    s_exec.FullName      AS AssignedToName,
    a_exec.PhotoUrl      AS AssignedToPhoto,
    s_rev.FullName       AS ReviewedByName,
    a_rev.PhotoUrl       AS ReviewedByPhoto,
    s_cre.FullName       AS CreatedByName,
    CASE
        WHEN t.[Status] = 5 AND t.IsLate = 0                            THEN 'green'
        WHEN t.IsLate = 1
          OR (t.[Status] != 5 AND t.EstimatedEndDate < GETDATE())       THEN 'red'
        WHEN t.[Status] = 1 AND t.EstimatedStartDate <= GETDATE()       THEN 'yellow'
        ELSE 'normal'
    END                  AS DisplayColor,
    tr.Note              AS LatestRevisionNote,
    tr.CreatedAt         AS LatestRevisionAt
FROM Task t
INNER JOIN ProjectGroup pg ON pg.GroupID   = t.GroupID
INNER JOIN Project p       ON p.GroupID    = pg.GroupID
INNER JOIN Class c         ON c.ClassID    = pg.ClassID
LEFT  JOIN Student s_exec  ON s_exec.StudentID = t.AssignedTo
LEFT  JOIN Account  a_exec ON a_exec.AccountID = s_exec.AccountID
LEFT  JOIN Student s_rev   ON s_rev.StudentID  = t.ReviewedBy
LEFT  JOIN Account  a_rev  ON a_rev.AccountID  = s_rev.AccountID
LEFT  JOIN Student s_cre   ON s_cre.StudentID  = t.CreatedBy
LEFT  JOIN (
    SELECT TaskID, Note, CreatedAt,
           ROW_NUMBER() OVER (PARTITION BY TaskID ORDER BY CreatedAt DESC) AS rn
    FROM TaskRevision
) tr ON tr.TaskID = t.TaskID AND tr.rn = 1;
GO

-- ============================================================
<<<<<<< HEAD
-- PHẦN 20: SEED DATA
=======
-- DATA ENUM REFERENCE
-- ============================================================
/*
  Account.Role:         1=Admin, 2=Student, 3=Teacher, 4=Staff
  Account.PhotoUrl:     NULL = dùng avatar mặc định (frontend tự xử lý)
  GroupMember.Role:     1=Leader, 2=Member
  GroupMember.Status:   1=Active, 2=Excluded
  Project.Status:       1=Active, 2=Completed
  Task.Status:          1=Pending → 2=InProgress → 3=Reviewing → 4=Revising / 5=Completed

  OtpVerification.Purpose:
    1 = ChangePassword   (user chủ động đổi pass)
    2 = FirstLogin       (sinh viên login lần đầu bị ép đổi)

  OTP flow (backend Spring Boot):
    // Bước 1: Tạo OTP
    String otpCode = otpRepo.callGenerateOtp(accountId, purpose);
    mailService.sendOtp(email, otpCode);        // JavaMailSender

    // Bước 2: User nhập mã → verify
    int result = otpRepo.callVerifyOtp(accountId, purpose, inputCode, newHash);
    // result: 0=OK, 1=WrongCode, 2=Locked, 3=Expired, 4=NotFound

  Scheduler:
    SQL Server Agent / @Scheduled Spring: EXEC sp_ResetOverdueTasks  (mỗi 1 phút)

  Avatar:
    Upload lên server (Multipart) → lưu file → UPDATE Account SET PhotoUrl = '/uploads/...'
    Hoặc dùng cloud storage (S3, Cloudinary) → lưu URL đầy đủ
*/

-- ============================================================
-- SEED DATA: TEST ACCOUNTS
-- Chay cung file DDL nay tren may moi se co san tai khoan de test.
--
-- Password cho tat ca tai khoan: 123
-- BCrypt hash: $2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm
--
-- Tai khoan tao san:
--   Admin:
--     admin / 123
--   Staff:
--     staff001 / 123
--     staff002 / 123
--   Teacher:
--     gv001 / 123
--     gv002 / 123
--   Student:
--     st001 / 123
--     st002 / 123
--     st003 / 123
--     st004 / 123
--     st005 / 123
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git
-- ============================================================
DECLARE @SeedPasswordHash NVARCHAR(255) = N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm';
DECLARE @SeedClassID1 INT, @SeedClassID2 INT, @SeedClassID3 INT;

DECLARE @AdminAccountID   INT;
DECLARE @Staff1AccountID  INT;
DECLARE @Staff2AccountID  INT;
DECLARE @Teacher1AccountID INT;
DECLARE @Teacher2AccountID INT;
DECLARE @Student1AccountID INT, @Student2AccountID INT, @Student3AccountID INT, @Student4AccountID INT, @Student5AccountID INT;
DECLARE @Staff1ID INT, @Staff2ID INT;

-- Admin
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'admin', @SeedPasswordHash, 1, 0, N'no-image.jpg', 1);
SET @AdminAccountID = SCOPE_IDENTITY();
INSERT INTO Staff (FullName, Email, AccountID) VALUES (N'Nguyen Minh Quan', N'admin@aptech.local', @AdminAccountID);

-- Staff 1
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'staff001', @SeedPasswordHash, 4, 0, N'no-image.jpg', 1);
SET @Staff1AccountID = SCOPE_IDENTITY();
INSERT INTO Staff (FullName, Email, AccountID) VALUES (N'Tran Giao Vu', N'giaovu1@aptech.local', @Staff1AccountID);
SET @Staff1ID = SCOPE_IDENTITY();

-- Staff 2
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'staff002', @SeedPasswordHash, 4, 0, N'no-image.jpg', 1);
SET @Staff2AccountID = SCOPE_IDENTITY();
INSERT INTO Staff (FullName, Email, AccountID) VALUES (N'Le Quan Ly', N'giaovu2@aptech.local', @Staff2AccountID);
SET @Staff2ID = SCOPE_IDENTITY();

-- Classes
INSERT INTO Class (ClassName, AcademicYear, ManagerID) VALUES (N'T2305M01', N'2025-2026', @Staff1ID);
SET @SeedClassID1 = SCOPE_IDENTITY();

INSERT INTO Class (ClassName, AcademicYear, ManagerID) VALUES (N'T2305M02', N'2025-2026', @Staff1ID);
SET @SeedClassID2 = SCOPE_IDENTITY();

INSERT INTO Class (ClassName, AcademicYear, ManagerID) VALUES (N'T2401M01', N'2025-2026', @Staff2ID);
SET @SeedClassID3 = SCOPE_IDENTITY();

-- Teachers
DECLARE @Teacher1ID INT, @Teacher2ID INT;
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'gv001', @SeedPasswordHash, 3, 0, N'no-image.jpg', 1);
SET @Teacher1AccountID = SCOPE_IDENTITY();
INSERT INTO Staff (FullName, Email, AccountID)
VALUES (N'Tran Van K', N'gv001@aptech.local', @Teacher1AccountID);
SET @Teacher1ID = SCOPE_IDENTITY();

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'gv002', @SeedPasswordHash, 3, 0, N'no-image.jpg', 1);
SET @Teacher2AccountID = SCOPE_IDENTITY();
INSERT INTO Staff (FullName, Email, AccountID)
VALUES (N'Le Thi H', N'gv002@aptech.local', @Teacher2AccountID);
SET @Teacher2ID = SCOPE_IDENTITY();

<<<<<<< HEAD
-- Students for Class 1
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st001', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
=======
-- Students cho Lop 1 (Created by Staff 1 - Giáo vụ 1)
DECLARE @St1ID INT, @St2ID INT, @St3ID INT, @St4ID INT, @St5ID INT;
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) VALUES (N'st001', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git
SET @Student1AccountID = SCOPE_IDENTITY();
<<<<<<< HEAD
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST001', N'Le Quang Huy', N'st001@aptech.local', @SeedClassID1, @Student1AccountID);
=======
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId) VALUES (N'ST001', N'Le Quang Huy', N'st001@aptech.local', @SeedClassID1, @Student1AccountID, @Staff1ID);
SET @St1ID = SCOPE_IDENTITY();
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st002', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student2AccountID = SCOPE_IDENTITY();
<<<<<<< HEAD
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST002', N'Pham Ngoc Lan', N'st002@aptech.local', @SeedClassID1, @Student2AccountID);
=======
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId) VALUES (N'ST002', N'Pham Ngoc Lan', N'st002@aptech.local', @SeedClassID1, @Student2AccountID, @Staff1ID);
SET @St2ID = SCOPE_IDENTITY();
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st003', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student3AccountID = SCOPE_IDENTITY();
<<<<<<< HEAD
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST003', N'Vo Gia Bao', N'st003@aptech.local', @SeedClassID1, @Student3AccountID);
=======
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId) VALUES (N'ST003', N'Vo Gia Bao', N'st003@aptech.local', @SeedClassID1, @Student3AccountID, @Staff1ID);
SET @St3ID = SCOPE_IDENTITY();
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git

<<<<<<< HEAD
-- Students for Class 2
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st004', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
=======
-- Thêm sinh viên chưa có nhóm
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) VALUES (N'stUnassigned1', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
DECLARE @UnAcc1 INT = SCOPE_IDENTITY();
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId) VALUES (N'STU01', N'Van A', N'stu1@aptech.local', @SeedClassID1, @UnAcc1, @Staff1ID);

-- Students cho Lop 2 (Created by Staff 2 - Giáo vụ 2)
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) VALUES (N'st004', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git
SET @Student4AccountID = SCOPE_IDENTITY();
<<<<<<< HEAD
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST004', N'Nguyen Hoang Nam', N'st004@aptech.local', @SeedClassID2, @Student4AccountID);
=======
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId) VALUES (N'ST004', N'Nguyen Hoang Nam', N'st004@aptech.local', @SeedClassID2, @Student4AccountID, @Staff2ID);
SET @St4ID = SCOPE_IDENTITY();
>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st005', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student5AccountID = SCOPE_IDENTITY();
<<<<<<< HEAD
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST005', N'Tran Thu Trang', N'st005@aptech.local', @SeedClassID2, @Student5AccountID);
=======
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId) VALUES (N'ST005', N'Tran Thu Trang', N'st005@aptech.local', @SeedClassID2, @Student5AccountID, @Staff2ID);
SET @St5ID = SCOPE_IDENTITY();

-- Test Data: Project Groups, Projects, Group Members
DECLARE @GroupID1 INT, @GroupID2 INT;

-- Group 1
INSERT INTO ProjectGroup (ClassID, GroupName) VALUES (@SeedClassID1, N'Nhom Alpha (Project Da Bat Dau)');
SET @GroupID1 = SCOPE_IDENTITY();

-- Group 2
INSERT INTO ProjectGroup (ClassID, GroupName) VALUES (@SeedClassID2, N'Nhom Beta (Project Chua Bat Dau)');
SET @GroupID2 = SCOPE_IDENTITY();

---------- Projects ----------
-- Project 1 (Thực tế, đã bắt đầu từ 2024, bạn không có quyền sửa thành viên)
INSERT INTO Project (GroupID, Title, Description, Semester, StartDate, EndDate, ReportDate, AdvisorID, CreatedBy, Status)
VALUES (@GroupID1, N'Website TMDT Ban Hang', N'Xay dung ung dung web TMDT', N'S2', '2024-01-01', '2027-12-31', '2027-12-29', @Teacher1ID, @Staff1ID, 1);

-- Project 2 (Dự kiến, chưa bắt đầu, ở tương lai 2030, có thể Test Xóa/Thêm thành viên)
INSERT INTO Project (GroupID, Title, Description, Semester, StartDate, EndDate, ReportDate, AdvisorID, CreatedBy, Status)
VALUES (@GroupID2, N'Ung Dung Java Quan Ly', N'Viet app Java desktop', N'S2', '2030-01-01', '2030-06-30', '2030-06-28', @Teacher2ID, @Staff2ID, 1);

---------- Members ----------
-- Members for Group 1 
INSERT INTO GroupMember (GroupID, StudentID, Role, Status) VALUES (@GroupID1, @St1ID, 1, 1); -- Leader
INSERT INTO GroupMember (GroupID, StudentID, Role, Status) VALUES (@GroupID1, @St2ID, 2, 1); -- Member

-- Members for Group 2
INSERT INTO GroupMember (GroupID, StudentID, Role, Status) VALUES (@GroupID2, @St4ID, 1, 1); -- Leader
INSERT INTO GroupMember (GroupID, StudentID, Role, Status) VALUES (@GroupID2, @St5ID, 2, 1); -- Member

>>>>>>> branch 'master' of https://github.com/NhaPhuong304/project_sem2.git
GO

PRINT 'Database created successfully!';
PRINT 'Test accounts:';
PRINT '  admin/123, staff001/123, staff002/123, gv001/123, gv002/123, st001/123, st002/123, st003/123, st004/123, st005/123';
GO