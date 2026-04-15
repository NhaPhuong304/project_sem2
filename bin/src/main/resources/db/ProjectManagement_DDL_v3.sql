USE master;
GO

ALTER DATABASE ProjectManagementDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO

DROP DATABASE ProjectManagementDB;
GO



-- ============================================================
--  HỆ THỐNG QUẢN LÝ DỰ ÁN SINH VIÊN APTECH - SEMESTER 2
--  Database: SQL Server 2019+
--  Version: 3.0
--    + Account.PhotoUrl       (avatar)
--    + OtpVerification        (xác thực OTP qua email)
--    + sp_GenerateOtp         (tạo mã OTP, trả về cho backend gửi mail)
--    + sp_VerifyOtp           (xác thực mã OTP, đổi mật khẩu)
-- ============================================================

USE master;
GO
CREATE DATABASE ProjectManagementDB
    COLLATE Vietnamese_CI_AS;
GO
USE ProjectManagementDB;
GO

-- ============================================================
-- 1. ACCOUNT  [v3: thêm PhotoUrl]
-- ============================================================
/*
  Role:        1 = Admin, 2 = Student, 3 = Teacher
  IsFirstLogin: bắt buộc đổi mật khẩu lần đầu
  PhotoUrl:    đường dẫn avatar (lưu URL/path, không lưu binary trong DB)
               NULL = dùng avatar mặc định phía frontend
               VD: '/uploads/avatars/acc_42.jpg'
                   'https://cdn.example.com/avatars/acc_42.webp'
*/
CREATE TABLE Account (
    AccountID    INT            IDENTITY(1,1) PRIMARY KEY,
    Username     NVARCHAR(50)   NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255)  NOT NULL,
    [Role]       TINYINT        NOT NULL CHECK ([Role] IN (1, 2, 3)),
    IsFirstLogin BIT            NOT NULL DEFAULT 1,
    PhotoUrl     NVARCHAR(500)  NULL,                -- [v3] avatar URL/path
    IsActive     BIT            NOT NULL DEFAULT 1,
    CreatedAt    DATETIME       NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
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
-- 3. CLASS
-- ============================================================
CREATE TABLE Class (
    ClassID      INT           IDENTITY(1,1) PRIMARY KEY,
    ClassName    NVARCHAR(100) NOT NULL,
    Semester     NVARCHAR(20)  NOT NULL,
    AcademicYear NVARCHAR(10)  NOT NULL,
    CreatedAt    DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
-- 4. STAFF
-- ============================================================
CREATE TABLE Staff (
    StaffID   INT           IDENTITY(1,1) PRIMARY KEY,
    FullName  NVARCHAR(100) NOT NULL,
    Email     NVARCHAR(100) NOT NULL UNIQUE,
    AccountID INT           NOT NULL UNIQUE REFERENCES Account(AccountID)
);
GO

-- ============================================================
-- 5. STUDENT
-- ============================================================
CREATE TABLE Student (
    StudentID   INT           IDENTITY(1,1) PRIMARY KEY,
    StudentCode NVARCHAR(20)  NOT NULL UNIQUE,
    FullName    NVARCHAR(100) NOT NULL,
    Email       NVARCHAR(100) NOT NULL UNIQUE,
    ClassID     INT           NOT NULL REFERENCES Class(ClassID),
    AccountID   INT           NOT NULL UNIQUE REFERENCES Account(AccountID)
);
GO

-- ============================================================
-- 6. PROJECT_GROUP
-- ============================================================
CREATE TABLE ProjectGroup (
    GroupID   INT           IDENTITY(1,1) PRIMARY KEY,
    ClassID   INT           NOT NULL REFERENCES Class(ClassID),
    GroupName NVARCHAR(100) NOT NULL,
    CreatedAt DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================================
-- 7. PROJECT
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
    CONSTRAINT CK_Project_Dates CHECK (StartDate <= EndDate AND EndDate <= ReportDate)
);
GO

-- ============================================================
-- 8. GROUP_MEMBER
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
-- 9. TASK
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
    AssignedTo         INT           NULL     REFERENCES Student(StudentID),
    ReviewedBy         INT           NULL     REFERENCES Student(StudentID),
    CreatedBy          INT           NOT NULL REFERENCES Student(StudentID),
    IsLate             BIT           NOT NULL DEFAULT 0,
    CreatedAt          DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_Task_Dates CHECK (EstimatedStartDate <= EstimatedEndDate)
);
GO

-- ============================================================
-- 10. TASK_REVISION
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
-- 11. TASK_STATUS_HISTORY
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
-- 12. TASK_ABANDON_LOG
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
-- 13. MESSAGE
-- ============================================================
CREATE TABLE [Message] (
    MessageID  INT           IDENTITY(1,1) PRIMARY KEY,
    SenderID   INT           NOT NULL REFERENCES Staff(StaffID),
    ReceiverID INT           NOT NULL REFERENCES Student(StudentID),
    TaskID     INT           NULL     REFERENCES Task(TaskID),
    Content    NVARCHAR(MAX) NOT NULL,
    SentAt     DATETIME      NOT NULL DEFAULT GETDATE(),
    IsRead     BIT           NOT NULL DEFAULT 0
);
GO

-- ============================================================
-- INDEXES
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
-- STORED PROCEDURE: Tạo OTP mới  [v3]
-- ============================================================
/*
  Gọi khi:
    - Sinh viên/Staff yêu cầu đổi mật khẩu    → @Purpose = 1
    - Sinh viên đăng nhập lần đầu (IsFirstLogin) → @Purpose = 2

  Trả về: OtpCode (backend đọc và gửi email, không lưu plain trên client)

  Backend flow (Java/Spring):
    1. EXEC sp_GenerateOtp @AccountID=42, @Purpose=1, @OtpCode OUTPUT
    2. Dùng JavaMailSender gửi email: "Mã OTP của bạn là: " + @OtpCode
    3. OTP hết hạn sau 5 phút — hiển thị countdown ở client
*/
CREATE OR ALTER PROCEDURE sp_GenerateOtp
    @AccountID INT,
    @Purpose   TINYINT,           -- 1=ChangePassword, 2=FirstLogin
    @OtpCode   NVARCHAR(6) OUTPUT -- trả về cho backend gửi mail
AS
BEGIN
    SET NOCOUNT ON;

    -- Lấy email tương ứng với account (ưu tiên Student, fallback Staff)
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

    -- Vô hiệu hóa tất cả OTP cũ còn active của account này + purpose này
    UPDATE OtpVerification
    SET IsUsed = 1
    WHERE AccountID = @AccountID
      AND Purpose   = @Purpose
      AND IsUsed    = 0;

    -- Sinh mã OTP 6 chữ số ngẫu nhiên (000000 → 999999)
    SET @OtpCode = RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS NVARCHAR), 6);

    -- Lưu OTP vào bảng (TTL = 5 phút)
    INSERT INTO OtpVerification (AccountID, Email, OtpCode, Purpose, ExpiresAt)
    VALUES (@AccountID, @Email, @OtpCode, @Purpose, DATEADD(MINUTE, 5, GETDATE()));
END;
GO

-- ============================================================
-- STORED PROCEDURE: Xác thực OTP và đổi mật khẩu  [v3]
-- ============================================================
/*
  Trả về ResultCode:
    0 = Thành công — đổi mật khẩu xong
    1 = Mã OTP không đúng (còn lượt thử)
    2 = Mã OTP không đúng, đã bị khóa (AttemptCount >= 5)
    3 = OTP hết hạn hoặc đã dùng rồi
    4 = Không tồn tại OTP hợp lệ cho account + purpose này

  @NewPasswordHash: bcrypt hash từ backend — SP không hash, chỉ lưu
*/
CREATE OR ALTER PROCEDURE sp_VerifyOtp
    @AccountID       INT,
    @Purpose         TINYINT,
    @InputCode       NVARCHAR(6),
    @NewPasswordHash NVARCHAR(255),
    @ResultCode      INT OUTPUT        -- 0=OK, 1=WrongCode, 2=Locked, 3=Expired, 4=NotFound
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;

    DECLARE @OtpID        INT;
    DECLARE @OtpCode      NVARCHAR(6);
    DECLARE @ExpiresAt    DATETIME;
    DECLARE @IsUsed       BIT;
    DECLARE @AttemptCount INT;

    -- Lấy OTP mới nhất còn hiệu lực của account + purpose
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

    -- Không tìm thấy OTP nào
    IF @OtpID IS NULL
    BEGIN
        SET @ResultCode = 4;
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- OTP đã dùng hoặc hết hạn
    IF @IsUsed = 1 OR @ExpiresAt < GETDATE()
    BEGIN
        SET @ResultCode = 3;
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Mã không đúng
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

    -- OTP đúng → đổi mật khẩu + đánh dấu OTP đã dùng
    UPDATE OtpVerification
    SET IsUsed = 1
    WHERE OtpID = @OtpID;

    UPDATE Account
    SET PasswordHash = @NewPasswordHash,
        IsFirstLogin = 0              -- tắt cờ bắt đổi mật khẩu lần đầu
    WHERE AccountID = @AccountID;

    SET @ResultCode = 0;
    COMMIT TRANSACTION;
END;
GO

-- ============================================================
-- TRIGGER: Xử lý khi sinh viên bỏ task
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

-- ============================================================
-- TRIGGER: Đặt IsLate khi task hoàn thành trễ
-- ============================================================
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
-- STORED PROCEDURE: Reset task quá 1 giờ không xác nhận
-- ============================================================
CREATE OR ALTER PROCEDURE sp_ResetOverdueTasks
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @OverdueTasks TABLE (TaskID INT, StudentID INT);

    INSERT INTO @OverdueTasks (TaskID, StudentID)
    SELECT TaskID, AssignedTo
    FROM Task
    WHERE [Status]      = 1
      AND AssignedTo    IS NOT NULL
      AND EstimatedStartDate <= DATEADD(HOUR, -1, GETDATE());

    INSERT INTO TaskAbandonLog (TaskID, StudentID, Note)
    SELECT TaskID, StudentID, N'Tự động: quá 1 giờ không xác nhận thực hiện'
    FROM @OverdueTasks;

    UPDATE Task
    SET AssignedTo = NULL
    WHERE TaskID IN (SELECT TaskID FROM @OverdueTasks);
END;
GO

-- ============================================================
-- VIEW: Tổng quan task
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
    -- Avatar: join về Account để lấy PhotoUrl
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
-- DATA ENUM REFERENCE
-- ============================================================
/*
  Account.Role:         1=Admin, 2=Student, 3=Teacher
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
--   Teacher:
--     gv001 / 123
--     gv002 / 123
--   Student:
--     st001 / 123
--     st002 / 123
--     st003 / 123
--     st004 / 123
--     st005 / 123
-- ============================================================
DECLARE @SeedPasswordHash NVARCHAR(255) = N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm';
DECLARE @SeedClassID INT;

DECLARE @AdminAccountID   INT;
DECLARE @Teacher1AccountID INT;
DECLARE @Teacher2AccountID INT;
DECLARE @Student1AccountID INT;
DECLARE @Student2AccountID INT;
DECLARE @Student3AccountID INT;
DECLARE @Student4AccountID INT;
DECLARE @Student5AccountID INT;

INSERT INTO Class (ClassName, Semester, AcademicYear)
VALUES (N'T2305M_SEM2', N'Semester 2', N'2025-2026');
SET @SeedClassID = SCOPE_IDENTITY();

-- Admin
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'admin', @SeedPasswordHash, 1, 0, N'no-image.jpg', 1);
SET @AdminAccountID = SCOPE_IDENTITY();

INSERT INTO Staff (FullName, Email, AccountID)
VALUES (N'Nguyen Minh Quan', N'admin@aptech.local', @AdminAccountID);

-- Teachers
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'gv001', @SeedPasswordHash, 3, 0, N'no-image.jpg', 1);
SET @Teacher1AccountID = SCOPE_IDENTITY();

INSERT INTO Staff (FullName, Email, AccountID)
VALUES (N'Tran Van K', N'gv001@aptech.local', @Teacher1AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'gv002', @SeedPasswordHash, 3, 0, N'no-image.jpg', 1);
SET @Teacher2AccountID = SCOPE_IDENTITY();

INSERT INTO Staff (FullName, Email, AccountID)
VALUES (N'Le Thi H', N'gv002@aptech.local', @Teacher2AccountID);

-- Students
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'st001', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student1AccountID = SCOPE_IDENTITY();

INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID)
VALUES (N'ST001', N'Le Quang Huy', N'st001@aptech.local', @SeedClassID, @Student1AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'st002', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student2AccountID = SCOPE_IDENTITY();

INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID)
VALUES (N'ST002', N'Pham Ngoc Lan', N'st002@aptech.local', @SeedClassID, @Student2AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'st003', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student3AccountID = SCOPE_IDENTITY();

INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID)
VALUES (N'ST003', N'Vo Gia Bao', N'st003@aptech.local', @SeedClassID, @Student3AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'st004', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student4AccountID = SCOPE_IDENTITY();

INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID)
VALUES (N'ST004', N'Nguyen Hoang Nam', N'st004@aptech.local', @SeedClassID, @Student4AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive)
VALUES (N'st005', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student5AccountID = SCOPE_IDENTITY();

INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID)
VALUES (N'ST005', N'Tran Thu Trang', N'st005@aptech.local', @SeedClassID, @Student5AccountID);
GO


