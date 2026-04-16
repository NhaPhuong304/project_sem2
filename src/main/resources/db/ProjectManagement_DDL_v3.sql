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

-- ============================================================
-- PHẦN 2: TẠO DATABASE MỚI
-- ============================================================
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

-- ============================================================
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
-- PHẦN 20: SEED DATA
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

-- Students for Class 1
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st001', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student1AccountID = SCOPE_IDENTITY();
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST001', N'Le Quang Huy', N'st001@aptech.local', @SeedClassID1, @Student1AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st002', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student2AccountID = SCOPE_IDENTITY();
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST002', N'Pham Ngoc Lan', N'st002@aptech.local', @SeedClassID1, @Student2AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st003', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student3AccountID = SCOPE_IDENTITY();
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST003', N'Vo Gia Bao', N'st003@aptech.local', @SeedClassID1, @Student3AccountID);

-- Students for Class 2
INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st004', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student4AccountID = SCOPE_IDENTITY();
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST004', N'Nguyen Hoang Nam', N'st004@aptech.local', @SeedClassID2, @Student4AccountID);

INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive) 
VALUES (N'st005', @SeedPasswordHash, 2, 0, N'no-image.jpg', 1);
SET @Student5AccountID = SCOPE_IDENTITY();
INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) 
VALUES (N'ST005', N'Tran Thu Trang', N'st005@aptech.local', @SeedClassID2, @Student5AccountID);
GO

PRINT 'Database created successfully!';
PRINT 'Test accounts:';
PRINT '  admin/123, staff001/123, staff002/123, gv001/123, gv002/123, st001/123, st002/123, st003/123, st004/123, st005/123';
GO