USE [ProjectManagementDB]
GO

-- ============================================================
-- Submission feature migration
-- Adds submission request tables without dropping existing data.
-- Safe to run again: existing tables/data are kept.
-- ============================================================

IF OBJECT_ID(N'dbo.SubmissionRequest', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionRequest (
        RequestID        INT IDENTITY(1,1) PRIMARY KEY,
        Title            NVARCHAR(200) NOT NULL,
        Description      NVARCHAR(MAX) NULL,
        Deadline         DATETIME NOT NULL,
        CreatedByStaffID INT NOT NULL,
        Status           TINYINT NOT NULL DEFAULT 1,
        CreatedAt        DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT CK_SubmissionRequest_Status
            CHECK (Status IN (1, 2))
    )
END
GO

IF OBJECT_ID(N'dbo.SubmissionRequirementTemplate', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionRequirementTemplate (
        TemplateID        INT IDENTITY(1,1) PRIMARY KEY,
        RequirementName  NVARCHAR(100) NOT NULL UNIQUE,
        RequiredExtension NVARCHAR(20) NOT NULL,
        SortOrder        INT NOT NULL,
        IsActive         BIT NOT NULL DEFAULT 1
    )
END
GO

IF OBJECT_ID(N'dbo.SubmissionRequirement', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionRequirement (
        RequirementID    INT IDENTITY(1,1) PRIMARY KEY,
        RequestID        INT NOT NULL,
        RequirementName  NVARCHAR(100) NOT NULL,
        RequiredExtension NVARCHAR(20) NOT NULL,
        SortOrder        INT NOT NULL,
        IsRequired       BIT NOT NULL DEFAULT 1,
        CONSTRAINT FK_SubmissionRequirement_Request
            FOREIGN KEY (RequestID) REFERENCES dbo.SubmissionRequest(RequestID),
        CONSTRAINT UQ_SubmissionRequirement_Request_Name
            UNIQUE (RequestID, RequirementName)
    )
END
GO

IF OBJECT_ID(N'dbo.SubmissionTarget', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionTarget (
        TargetID        INT IDENTITY(1,1) PRIMARY KEY,
        RequestID       INT NOT NULL,
        GroupID         INT NOT NULL,
        LeaderStudentID INT NOT NULL,
        Status          TINYINT NOT NULL DEFAULT 1,
        NotifiedAt      DATETIME NULL,
        SubmittedAt     DATETIME NULL,
        CreatedAt       DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_SubmissionTarget_Request
            FOREIGN KEY (RequestID) REFERENCES dbo.SubmissionRequest(RequestID),
        CONSTRAINT UQ_SubmissionTarget_Request_Group
            UNIQUE (RequestID, GroupID),
        CONSTRAINT CK_SubmissionTarget_Status
            CHECK (Status IN (1, 2, 3))
    )
END
GO

IF OBJECT_ID(N'dbo.SubmissionFile', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionFile (
        FileID              INT IDENTITY(1,1) PRIMARY KEY,
        TargetID            INT NOT NULL,
        RequirementID       INT NOT NULL,
        OriginalFileName    NVARCHAR(255) NOT NULL,
        StoredFileName      NVARCHAR(255) NOT NULL,
        FilePath            NVARCHAR(1000) NOT NULL,
        FileSize            BIGINT NOT NULL,
        UploadedByStudentID INT NOT NULL,
        UploadedAt          DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_SubmissionFile_Target
            FOREIGN KEY (TargetID) REFERENCES dbo.SubmissionTarget(TargetID),
        CONSTRAINT FK_SubmissionFile_Requirement
            FOREIGN KEY (RequirementID) REFERENCES dbo.SubmissionRequirement(RequirementID),
        CONSTRAINT UQ_SubmissionFile_Target_Requirement
            UNIQUE (TargetID, RequirementID)
    )
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'Database')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'Database', N'.zip', 1)
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'eProjects Status report')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'eProjects Status report', N'.xlsx', 2)
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'eProjects Feedback Form')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'eProjects Feedback Form', N'.xlsx', 3)
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'Project report')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'Project report', N'.docx', 4)
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'Source Code')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'Source Code', N'.zip', 5)
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'Slide PowerPoint')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'Slide PowerPoint', N'.pptx', 6)
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequirementTemplate WHERE RequirementName = N'Video')
BEGIN
    INSERT INTO dbo.SubmissionRequirementTemplate (RequirementName, RequiredExtension, SortOrder)
    VALUES (N'Video', N'.zip', 7)
END
GO

SELECT RequirementName, RequiredExtension, SortOrder
FROM dbo.SubmissionRequirementTemplate
ORDER BY SortOrder
GO
