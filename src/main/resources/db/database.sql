USE [master]
GO

/*
    =========================================================
    PROJECT MANAGEMENT SYSTEM - MASTER DATABASE SCRIPT
    =========================================================

    Purpose:
    - This is the only SQL file you need to run for a fresh setup.
    - It resets ProjectManagementDB and recreates everything from scratch.

    What this script includes:
    1. Database reset and creation
    2. Core schema: tables, constraints, indexes, views, triggers, procedures
    3. Built-in sample data for login, projects, tasks, and messages
    4. Submission feature tables and sample submission records
    5. Dashboard seed data for charts and overview screens

    Safe usage:
    - Use this script only when you want a clean rebuild of the database.
    - Existing ProjectManagementDB data will be deleted.

    Recommended tools:
    - SQL Server Management Studio
    - Azure Data Studio
    - DBeaver
 */
IF DB_ID(N'ProjectManagementDB') IS NOT NULL
BEGIN
    ALTER DATABASE [ProjectManagementDB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE
    DROP DATABASE [ProjectManagementDB]
END
GO

CREATE DATABASE [ProjectManagementDB] COLLATE Vietnamese_CI_AS
GO
ALTER DATABASE [ProjectManagementDB] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [ProjectManagementDB].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [ProjectManagementDB] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET ARITHABORT OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [ProjectManagementDB] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [ProjectManagementDB] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET  ENABLE_BROKER 
GO
ALTER DATABASE [ProjectManagementDB] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [ProjectManagementDB] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET RECOVERY FULL 
GO
ALTER DATABASE [ProjectManagementDB] SET  MULTI_USER 
GO
ALTER DATABASE [ProjectManagementDB] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [ProjectManagementDB] SET DB_CHAINING OFF 
GO
ALTER DATABASE [ProjectManagementDB] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [ProjectManagementDB] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [ProjectManagementDB] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [ProjectManagementDB] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'ProjectManagementDB', N'ON'
GO
ALTER DATABASE [ProjectManagementDB] SET QUERY_STORE = ON
GO
ALTER DATABASE [ProjectManagementDB] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [ProjectManagementDB]
GO
/****** Object:  Table [dbo].[Task]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Task](
	[TaskID] [int] IDENTITY(1,1) NOT NULL,
	[GroupID] [int] NOT NULL,
	[Title] [nvarchar](200) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[EstimatedStartDate] [datetime] NOT NULL,
	[EstimatedEndDate] [datetime] NOT NULL,
	[ActualStartDate] [datetime] NULL,
	[ActualEndDate] [datetime] NULL,
	[Status] [tinyint] NOT NULL,
	[AssignedTo] [int] NULL,
	[ReviewedBy] [int] NULL,
	[CreatedBy] [int] NOT NULL,
	[IsLate] [bit] NOT NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[TaskID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaskRevision]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaskRevision](
	[RevisionID] [int] IDENTITY(1,1) NOT NULL,
	[TaskID] [int] NOT NULL,
	[ReviewedBy] [int] NOT NULL,
	[Note] [nvarchar](max) NOT NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[RevisionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Account]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Account](
	[AccountID] [int] IDENTITY(1,1) NOT NULL,
	[Username] [nvarchar](50) NOT NULL,
	[PasswordHash] [nvarchar](255) NOT NULL,
	[Role] [tinyint] NOT NULL,
	[IsFirstLogin] [bit] NOT NULL,
	[PhotoUrl] [nvarchar](500) NULL,
	[IsActive] [bit] NOT NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[AccountID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Class]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Class](
	[ClassID] [int] IDENTITY(1,1) NOT NULL,
	[ClassName] [nvarchar](100) NOT NULL,
	[AcademicYear] [nvarchar](10) NOT NULL,
	[ManagerID] [int] NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ClassID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Student]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Student](
	[StudentID] [int] IDENTITY(1,1) NOT NULL,
	[StudentCode] [nvarchar](20) NOT NULL,
	[FullName] [nvarchar](100) NOT NULL,
	[Email] [nvarchar](100) NOT NULL,
	[ClassID] [int] NOT NULL,
	[AccountID] [int] NOT NULL,
	[CreatedByStaffId] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[StudentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ProjectGroup]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ProjectGroup](
	[GroupID] [int] IDENTITY(1,1) NOT NULL,
	[ClassID] [int] NOT NULL,
	[GroupName] [nvarchar](100) NOT NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[GroupID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Project]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Project](
	[ProjectID] [int] IDENTITY(1,1) NOT NULL,
	[GroupID] [int] NOT NULL,
	[Title] [nvarchar](200) NOT NULL,
	[Description] [nvarchar](max) NULL,
	[Semester] [nvarchar](20) NOT NULL,
	[StartDate] [date] NOT NULL,
	[EndDate] [date] NOT NULL,
	[ReportDate] [date] NOT NULL,
	[AdvisorID] [int] NOT NULL,
	[CreatedBy] [int] NOT NULL,
	[Status] [tinyint] NOT NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ProjectID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  View [dbo].[vw_TaskOverview]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- VIEW: Task overview
-- ============================================================
CREATE   VIEW [dbo].[vw_TaskOverview] AS
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
        WHEN 1 THEN N'Waiting to start'
        WHEN 2 THEN N'In progress'
        WHEN 3 THEN N'Under review'
        WHEN 4 THEN N'Being revised'
        WHEN 5 THEN N'Completed'
    END                  AS TaskStatusLabel,
    t.IsLate,
    -- Avatar: join Account to get PhotoUrl
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
) tr ON tr.TaskID = t.TaskID AND tr.rn = 1
GO
/****** Object:  Table [dbo].[GroupMember]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GroupMember](
	[MemberID] [int] IDENTITY(1,1) NOT NULL,
	[GroupID] [int] NOT NULL,
	[StudentID] [int] NOT NULL,
	[Role] [tinyint] NOT NULL,
	[Status] [tinyint] NOT NULL,
	[AbandonCount] [int] NOT NULL,
	[JoinedAt] [datetime] NOT NULL,
	[ExcludedAt] [datetime] NULL,
	[ExcludedBy] [int] NULL,
	[ExcludedReason] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[MemberID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Message]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Message](
	[MessageID] [int] IDENTITY(1,1) NOT NULL,
	[SenderID] [int] NOT NULL,
	[ReceiverID] [int] NOT NULL,
	[TaskID] [int] NULL,
	[Content] [nvarchar](max) NOT NULL,
	[SentAt] [datetime] NOT NULL,
	[IsRead] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[MessageID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OtpVerification]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OtpVerification](
	[OtpID] [int] IDENTITY(1,1) NOT NULL,
	[AccountID] [int] NOT NULL,
	[Email] [nvarchar](100) NOT NULL,
	[OtpCode] [nvarchar](6) NOT NULL,
	[Purpose] [tinyint] NOT NULL,
	[ExpiresAt] [datetime] NOT NULL,
	[IsUsed] [bit] NOT NULL,
	[AttemptCount] [int] NOT NULL,
	[CreatedAt] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[OtpID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Question]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Question](
	[QuestionID] [int] IDENTITY(1,1) NOT NULL,
	[StudentID] [int] NOT NULL,
	[TeacherID] [int] NOT NULL,
	[TaskID] [int] NULL,
	[QuestionContent] [nvarchar](max) NOT NULL,
	[AnswerContent] [nvarchar](max) NULL,
	[CreatedAt] [datetime] NOT NULL,
	[AnsweredAt] [datetime] NULL,
	[IsAnswered] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[QuestionID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Staff]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Staff](
	[StaffID] [int] IDENTITY(1,1) NOT NULL,
	[FullName] [nvarchar](100) NOT NULL,
	[Email] [nvarchar](100) NOT NULL,
	[AccountID] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[StaffID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaskAbandonLog]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaskAbandonLog](
	[LogID] [int] IDENTITY(1,1) NOT NULL,
	[TaskID] [int] NOT NULL,
	[StudentID] [int] NOT NULL,
	[AbandonedAt] [datetime] NOT NULL,
	[Note] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[LogID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaskStatusHistory]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaskStatusHistory](
	[HistoryID] [int] IDENTITY(1,1) NOT NULL,
	[TaskID] [int] NOT NULL,
	[FromStatus] [tinyint] NOT NULL,
	[ToStatus] [tinyint] NOT NULL,
	[ChangedBy] [int] NOT NULL,
	[ChangedAt] [datetime] NOT NULL,
	[Note] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[HistoryID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[Account] ON 

INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (1, N'admin', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 1, 0, N'uploads/avatars/avatar_acc_1_41d3cba561cb41bfbc04bb8c71ba923b.jpg', 1, CAST(N'2026-04-15T16:20:07.960' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (2, N'staff001', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 4, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.960' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (3, N'staff002', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 4, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.963' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (4, N'gv001', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 3, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.967' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (5, N'gv002', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 3, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.967' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (6, N'st001', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 2, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.967' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (7, N'st002', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 2, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.970' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (8, N'st003', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 2, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.970' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (9, N'st004', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 2, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.970' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (10, N'st005', N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm', 2, 0, N'no-image.jpg', 1, CAST(N'2026-04-15T16:20:07.970' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (11, N'staff003', N'$2a$10$Hox7xd1oOx0caWuTQnJzQ.1DkdThnO2AMnXU84E3EMFsCNlw7oeO.', 4, 1, N'no-image.jpg', 1, CAST(N'2026-04-16T15:46:16.337' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (12, N'ST006', N'$2a$10$N1Pswy9W807Lbug.lFvev.XS8Z202iGtUksJAjPR399wDxL6QoEa.', 2, 0, N'no-image.jpg', 1, CAST(N'2026-04-16T17:54:34.777' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (13, N'staff004', N'$2a$10$0y8nEM9XKkKMVhhupxu1med0nnrYWzHI1rjcTYwqm.z6vP8fNkSU2', 4, 0, N'no-image.jpg', 0, CAST(N'2026-04-16T18:04:06.967' AS DateTime))
INSERT [dbo].[Account] ([AccountID], [Username], [PasswordHash], [Role], [IsFirstLogin], [PhotoUrl], [IsActive], [CreatedAt]) VALUES (14, N'gv003', N'$2a$10$yzj96S2ExEc7BKK0WfhpqecuWziZSZS9UaWkv.Nq6kToTjVTZ.Tyy', 3, 0, N'no-image.jpg', 1, CAST(N'2026-04-21T14:55:39.483' AS DateTime))
SET IDENTITY_INSERT [dbo].[Account] OFF
GO
SET IDENTITY_INSERT [dbo].[Class] ON 

INSERT [dbo].[Class] ([ClassID], [ClassName], [AcademicYear], [ManagerID], [CreatedAt]) VALUES (1, N'T2305M01', N'2025-2026', 2, CAST(N'2026-04-15T16:20:07.963' AS DateTime))
INSERT [dbo].[Class] ([ClassID], [ClassName], [AcademicYear], [ManagerID], [CreatedAt]) VALUES (2, N'T2305M02', N'2025-2026', 2, CAST(N'2026-04-15T16:20:07.963' AS DateTime))
INSERT [dbo].[Class] ([ClassID], [ClassName], [AcademicYear], [ManagerID], [CreatedAt]) VALUES (3, N'T2401M01', N'2025-2026', 3, CAST(N'2026-04-15T16:20:07.967' AS DateTime))
INSERT [dbo].[Class] ([ClassID], [ClassName], [AcademicYear], [ManagerID], [CreatedAt]) VALUES (4, N'Unassigned class', N'N/A', NULL, CAST(N'2026-04-16T15:14:23.230' AS DateTime))
INSERT [dbo].[Class] ([ClassID], [ClassName], [AcademicYear], [ManagerID], [CreatedAt]) VALUES (5, N'C2010L', N'2025-2028', 7, CAST(N'2026-04-20T11:54:21.893' AS DateTime))
SET IDENTITY_INSERT [dbo].[Class] OFF
GO
SET IDENTITY_INSERT [dbo].[GroupMember] ON 

INSERT [dbo].[GroupMember] ([MemberID], [GroupID], [StudentID], [Role], [Status], [AbandonCount], [JoinedAt], [ExcludedAt], [ExcludedBy], [ExcludedReason]) VALUES (1, 2, 6, 1, 1, 0, CAST(N'2026-04-16T17:56:28.597' AS DateTime), NULL, NULL, NULL)
INSERT [dbo].[GroupMember] ([MemberID], [GroupID], [StudentID], [Role], [Status], [AbandonCount], [JoinedAt], [ExcludedAt], [ExcludedBy], [ExcludedReason]) VALUES (2, 2, 1, 2, 1, 2, CAST(N'2026-04-16T17:56:31.460' AS DateTime), NULL, NULL, NULL)
INSERT [dbo].[GroupMember] ([MemberID], [GroupID], [StudentID], [Role], [Status], [AbandonCount], [JoinedAt], [ExcludedAt], [ExcludedBy], [ExcludedReason]) VALUES (3, 2, 4, 2, 1, 0, CAST(N'2026-04-16T17:56:33.323' AS DateTime), NULL, NULL, NULL)
INSERT [dbo].[GroupMember] ([MemberID], [GroupID], [StudentID], [Role], [Status], [AbandonCount], [JoinedAt], [ExcludedAt], [ExcludedBy], [ExcludedReason]) VALUES (4, 2, 5, 2, 1, 1, CAST(N'2026-04-16T17:56:37.537' AS DateTime), NULL, NULL, NULL)
SET IDENTITY_INSERT [dbo].[GroupMember] OFF
GO
SET IDENTITY_INSERT [dbo].[Message] ON 

INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (1, 4, 1, 1, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-16T18:32:36.780' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (2, 4, 1, 1, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-16T18:33:36.770' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (3, 4, 1, 1, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-16T18:34:36.760' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (4, 4, 1, 1, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-16T18:35:36.763' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (5, 4, 1, 1, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-16T18:36:36.770' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (6, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:48:39.490' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (7, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:49:39.497' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (8, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:49:39.497' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (9, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:50:39.520' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (10, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:50:39.520' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (11, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:51:39.517' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (12, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:51:39.517' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (13, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:52:50.127' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (14, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:52:50.127' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (15, 6, 1, 3, N'QUESTION|I do not understand', CAST(N'2026-04-21T14:53:23.343' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (16, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:53:48.793' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (17, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:53:48.793' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (18, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:54:48.807' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (19, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:54:48.807' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (20, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:55:48.803' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (21, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:55:48.803' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (22, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:56:48.800' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (23, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:56:48.800' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (24, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:57:48.807' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (25, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T14:57:48.807' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (26, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:06:36.083' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (27, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:06:36.083' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (28, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:07:34.973' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (29, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:07:34.973' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (30, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:08:34.983' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (31, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:08:34.983' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (32, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:09:34.993' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (33, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:09:34.993' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (34, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:10:34.993' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (35, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:10:34.993' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (36, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:11:34.973' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (37, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:11:34.973' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (38, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:12:35.033' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (39, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:12:35.033' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (40, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:13:34.983' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (41, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:13:34.983' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (42, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:14:35.000' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (43, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:14:35.000' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (44, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:15:34.990' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (45, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:15:34.990' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (46, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:16:35.003' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (47, 4, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:16:55.460' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (48, 4, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:16:55.460' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (49, 6, 1, 3, N'QUESTION|I do not understand', CAST(N'2026-04-21T15:17:52.633' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (50, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:17:54.333' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (51, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:17:54.333' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (52, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:18:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (53, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:18:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (54, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:18:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (55, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:18:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (56, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:19:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (57, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:19:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (58, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:19:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (59, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:19:54.337' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (60, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:20:54.340' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (61, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:20:54.340' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (62, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:20:54.340' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (63, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:20:54.340' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (64, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:21:54.347' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (65, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:21:54.347' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (66, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:22:55.320' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (67, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:22:55.320' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (68, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:22:55.320' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (69, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:22:55.320' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (71, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:23:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (72, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:23:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (73, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:23:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (74, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:23:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (75, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:24:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (76, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:24:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (77, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:24:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (78, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:24:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (79, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:25:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (80, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:25:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (81, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:25:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (82, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:25:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (83, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:26:54.437' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (84, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:26:54.437' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (85, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:26:54.437' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (86, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:26:54.437' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (87, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:27:54.417' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (88, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:27:54.417' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (89, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:27:54.417' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (90, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:27:54.417' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (91, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:28:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (92, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:28:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (93, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:28:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (94, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:28:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (95, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:29:54.440' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (96, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:29:54.440' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (97, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:29:54.440' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (98, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:29:54.440' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (99, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:30:54.430' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (100, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:30:54.430' AS DateTime), 0)
GO
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (101, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:30:54.430' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (102, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:30:54.430' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (103, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:31:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (104, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:31:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (105, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:31:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (106, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:31:54.413' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (107, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:32:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (108, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:32:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (109, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:32:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (110, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:32:54.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (111, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:33:35.657' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (112, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:33:35.657' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (113, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:33:35.657' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (114, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:33:35.657' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (115, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:34:34.593' AS DateTime), 1)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (116, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:34:34.593' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (117, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:34:34.593' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (118, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:34:34.593' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (119, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:35:34.690' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (120, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:35:34.690' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (121, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:35:34.690' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (122, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:35:34.690' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (123, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:36:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (124, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:36:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (125, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:36:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (126, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:36:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (127, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:37:34.577' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (128, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:37:34.577' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (129, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:37:34.577' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (130, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:37:34.577' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (131, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:38:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (132, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:38:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (133, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:38:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (134, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:38:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (135, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:39:34.613' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (136, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:39:34.613' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (137, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:39:34.613' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (138, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:39:34.613' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (139, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:40:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (140, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:40:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (141, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:40:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (142, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:40:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (143, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:41:35.037' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (144, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:41:35.037' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (145, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:41:35.037' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (146, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:41:35.037' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (147, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:42:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (148, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:42:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (149, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:42:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (150, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:42:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (151, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:43:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (152, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:43:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (153, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:43:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (154, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:43:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (155, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:44:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (156, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:44:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (157, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:44:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (158, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:44:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (159, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:45:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (160, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:45:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (161, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:45:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (162, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:45:34.583' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (163, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:46:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (164, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:46:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (165, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:46:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (166, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:46:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (167, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:47:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (168, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:47:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (169, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:47:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (170, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:47:34.580' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (171, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:48:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (172, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:48:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (173, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:48:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (174, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:48:34.573' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (175, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:49:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (176, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:49:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (177, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:49:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (178, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:49:34.587' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (179, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:50:42.570' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (180, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:50:42.570' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (181, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:50:42.570' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (182, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:50:42.570' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (183, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:51:41.687' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (184, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T15:51:41.687' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (185, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:51:41.687' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (186, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T15:51:41.687' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (187, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:27:51.667' AS DateTime), 1)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (188, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:27:51.667' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (189, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:27:51.667' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (190, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:27:51.667' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (191, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:28:50.677' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (192, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:28:50.677' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (193, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:28:50.677' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (194, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:28:50.677' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (195, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:29:50.683' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (196, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:29:50.683' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (197, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:29:50.683' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (198, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:29:50.683' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (199, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:30:50.697' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (200, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:30:50.697' AS DateTime), 0)
GO
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (201, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:30:50.697' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (202, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:30:50.697' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (203, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:31:50.680' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (204, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:31:50.680' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (205, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:31:50.680' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (206, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:31:50.680' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (207, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:32:50.697' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (208, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:32:50.697' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (209, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:33:16.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (210, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:33:16.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (211, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:33:16.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (212, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:33:16.423' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (213, 8, 6, 3, N'ANSWER|eeeeeeeer', CAST(N'2026-04-21T18:33:47.283' AS DateTime), 1)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (214, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:34:15.510' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (215, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:34:15.510' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (216, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:34:15.510' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (217, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:34:15.510' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (218, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:35:15.517' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (219, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:35:15.517' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (220, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:35:15.517' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (221, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:35:15.517' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (222, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (223, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (224, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (225, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (226, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:37:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (227, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:37:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (228, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:37:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (229, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:37:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (230, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:38:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (231, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:38:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (232, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:38:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (233, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:38:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (234, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:39:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (235, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:39:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (236, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:39:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (237, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:39:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (238, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:40:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (239, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:40:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (240, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:40:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (241, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:40:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (242, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:41:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (243, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:41:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (244, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:41:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (245, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:41:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (246, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:42:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (247, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:42:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (248, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:42:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (249, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:42:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (250, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:43:15.557' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (251, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:43:15.557' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (252, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:43:15.557' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (253, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:43:15.557' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (254, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:44:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (255, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:44:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (256, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:44:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (257, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:44:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (258, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:45:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (259, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:45:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (260, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:45:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (261, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:45:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (262, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:46:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (263, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:46:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (264, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:46:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (265, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:46:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (266, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:47:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (267, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:47:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (268, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:47:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (269, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:47:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (270, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:48:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (271, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:48:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (272, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:48:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (273, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:48:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (274, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:49:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (275, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:49:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (276, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:49:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (277, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:49:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (278, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:50:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (279, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:50:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (280, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:50:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (281, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:50:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (282, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:51:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (283, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:51:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (284, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:51:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (285, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:51:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (286, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:52:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (287, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:52:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (288, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:52:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (289, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:52:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (290, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:53:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (291, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:53:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (292, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:53:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (293, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:53:15.550' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (294, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:54:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (295, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:54:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (296, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:54:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (297, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:54:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (298, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:55:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (299, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:55:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (300, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:55:15.537' AS DateTime), 0)
GO
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (301, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:55:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (302, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:56:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (303, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:56:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (304, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:56:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (305, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:56:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (306, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:57:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (307, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:57:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (308, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:57:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (309, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:57:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (310, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:58:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (311, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:58:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (312, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:58:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (313, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:58:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (314, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:59:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (315, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T18:59:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (316, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:59:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (317, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T18:59:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (318, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:00:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (319, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:00:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (320, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:00:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (321, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:00:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (322, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:01:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (323, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:01:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (324, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:01:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (325, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:01:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (326, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:02:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (327, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:02:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (328, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:02:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (329, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:02:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (330, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:03:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (331, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:03:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (332, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:03:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (333, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:03:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (334, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:04:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (335, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:04:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (336, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:04:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (337, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:04:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (338, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:05:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (339, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:05:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (340, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:05:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (341, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:05:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (342, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:06:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (343, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:06:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (344, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:06:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (345, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:06:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (346, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:07:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (347, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:07:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (348, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:07:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (349, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:07:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (350, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:08:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (351, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:08:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (352, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:08:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (353, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:08:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (354, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:09:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (355, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:09:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (356, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:09:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (357, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:09:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (358, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:10:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (359, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:10:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (360, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:10:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (361, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:10:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (362, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:11:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (363, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:11:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (364, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:11:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (365, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:11:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (366, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:12:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (367, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:12:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (368, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:12:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (369, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:12:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (370, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:13:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (371, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:13:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (372, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:13:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (373, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:13:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (374, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:14:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (375, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:14:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (376, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:14:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (377, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:14:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (378, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:15:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (379, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:15:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (380, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:15:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (381, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:15:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (382, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:16:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (383, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:16:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (384, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:16:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (385, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:16:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (386, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:17:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (387, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:17:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (388, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:17:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (389, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:17:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (390, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:18:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (391, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:18:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (392, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:18:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (393, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:18:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (394, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:19:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (395, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:19:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (396, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:19:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (397, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:19:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (398, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:20:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (399, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:20:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (400, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:20:15.533' AS DateTime), 0)
GO
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (401, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:20:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (402, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:21:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (403, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:21:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (404, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:21:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (405, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:21:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (406, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:22:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (407, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:22:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (408, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:22:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (409, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:22:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (410, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:23:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (411, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:23:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (412, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:23:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (413, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:23:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (414, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:24:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (415, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:24:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (416, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:24:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (417, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:24:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (418, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:25:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (419, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:25:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (420, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:25:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (421, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:25:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (422, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:26:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (423, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:26:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (424, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:26:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (425, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:26:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (426, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:27:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (427, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:27:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (428, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:27:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (429, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:27:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (430, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:28:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (431, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:28:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (432, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:28:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (433, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:28:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (434, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:29:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (435, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:29:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (436, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:29:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (437, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:29:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (438, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:30:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (439, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:30:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (440, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:30:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (441, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:30:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (442, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:31:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (443, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:31:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (444, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:31:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (445, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:31:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (446, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:32:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (447, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:32:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (448, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:32:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (449, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:32:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (450, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:33:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (451, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:33:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (452, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:33:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (453, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:33:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (454, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:34:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (455, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:34:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (456, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:34:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (457, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:34:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (458, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:35:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (459, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:35:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (460, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:35:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (461, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:35:15.533' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (462, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (463, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (464, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (465, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:36:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (466, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:37:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (467, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:37:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (468, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:37:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (469, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:37:15.530' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (470, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:38:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (471, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:38:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (472, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:38:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (473, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:38:15.547' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (474, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:39:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (475, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:39:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (476, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:39:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (477, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:39:15.543' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (478, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:40:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (479, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:40:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (480, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:40:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (481, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:40:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (482, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:41:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (483, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:41:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (484, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:41:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (485, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:41:15.540' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (486, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:42:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (487, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:42:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (488, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:42:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (489, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:42:15.527' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (490, 8, 6, 2, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:43:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (491, 8, 6, 3, N'SYSTEM_REMINDER|[DUE] Task cua ban sap het han trong 30 phut.', CAST(N'2026-04-21T19:43:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (492, 8, 6, 2, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:43:15.537' AS DateTime), 0)
INSERT [dbo].[Message] ([MessageID], [SenderID], [ReceiverID], [TaskID], [Content], [SentAt], [IsRead]) VALUES (493, 8, 6, 3, N'SYSTEM_REMINDER|[DAILY] Task cua ban con duoi 3 ngay nua la het han.', CAST(N'2026-04-21T19:43:15.537' AS DateTime), 0)
SET IDENTITY_INSERT [dbo].[Message] OFF
GO
SET IDENTITY_INSERT [dbo].[OtpVerification] ON 

INSERT [dbo].[OtpVerification] ([OtpID], [AccountID], [Email], [OtpCode], [Purpose], [ExpiresAt], [IsUsed], [AttemptCount], [CreatedAt]) VALUES (1, 12, N'nguyennhaphuongbl2006@gmail.com', N'303932', 2, CAST(N'2026-04-16T18:04:41.297' AS DateTime), 1, 0, CAST(N'2026-04-16T17:59:41.297' AS DateTime))
INSERT [dbo].[OtpVerification] ([OtpID], [AccountID], [Email], [OtpCode], [Purpose], [ExpiresAt], [IsUsed], [AttemptCount], [CreatedAt]) VALUES (2, 13, N'khiem132@gmail.com', N'169120', 2, CAST(N'2026-04-16T18:10:25.950' AS DateTime), 1, 0, CAST(N'2026-04-16T18:05:25.950' AS DateTime))
INSERT [dbo].[OtpVerification] ([OtpID], [AccountID], [Email], [OtpCode], [Purpose], [ExpiresAt], [IsUsed], [AttemptCount], [CreatedAt]) VALUES (3, 12, N'nguyennhaphuongbl2006@gmail.com', N'415314', 2, CAST(N'2026-04-16T18:32:34.283' AS DateTime), 1, 0, CAST(N'2026-04-16T18:27:34.283' AS DateTime))
INSERT [dbo].[OtpVerification] ([OtpID], [AccountID], [Email], [OtpCode], [Purpose], [ExpiresAt], [IsUsed], [AttemptCount], [CreatedAt]) VALUES (4, 14, N'nguyennhaphuongbl2006@gmail.com', N'379378', 2, CAST(N'2026-04-21T15:00:56.770' AS DateTime), 1, 0, CAST(N'2026-04-21T14:55:56.770' AS DateTime))
SET IDENTITY_INSERT [dbo].[OtpVerification] OFF
GO
SET IDENTITY_INSERT [dbo].[Project] ON 

INSERT [dbo].[Project] ([ProjectID], [GroupID], [Title], [Description], [Semester], [StartDate], [EndDate], [ReportDate], [AdvisorID], [CreatedBy], [Status], [CreatedAt]) VALUES (1, 1, N'Student management', N'Student management', N'2', CAST(N'2026-04-16' AS Date), CAST(N'2026-05-16' AS Date), CAST(N'2026-05-15' AS Date), 8, 2, 1, CAST(N'2026-04-16T17:49:46.287' AS DateTime))
INSERT [dbo].[Project] ([ProjectID], [GroupID], [Title], [Description], [Semester], [StartDate], [EndDate], [ReportDate], [AdvisorID], [CreatedBy], [Status], [CreatedAt]) VALUES (2, 2, N'Project management', N'Project management', N'2', CAST(N'2026-04-16' AS Date), CAST(N'2026-05-16' AS Date), CAST(N'2026-05-15' AS Date), 8, 2, 1, CAST(N'2026-04-16T17:56:19.133' AS DateTime))
SET IDENTITY_INSERT [dbo].[Project] OFF
GO
SET IDENTITY_INSERT [dbo].[ProjectGroup] ON 

INSERT [dbo].[ProjectGroup] ([GroupID], [ClassID], [GroupName], [CreatedAt]) VALUES (1, 2, N'Group - Student management', CAST(N'2026-04-16T17:49:46.270' AS DateTime))
INSERT [dbo].[ProjectGroup] ([GroupID], [ClassID], [GroupName], [CreatedAt]) VALUES (2, 2, N'Group - Project management', CAST(N'2026-04-16T17:56:19.123' AS DateTime))
SET IDENTITY_INSERT [dbo].[ProjectGroup] OFF
GO
SET IDENTITY_INSERT [dbo].[Question] ON 

INSERT [dbo].[Question] ([QuestionID], [StudentID], [TeacherID], [TaskID], [QuestionContent], [AnswerContent], [CreatedAt], [AnsweredAt], [IsAnswered]) VALUES (1, 6, 8, 3, N'I do not understand', N'Me too', CAST(N'2026-04-21T15:34:16.750' AS DateTime), CAST(N'2026-04-21T15:34:48.693' AS DateTime), 1)
INSERT [dbo].[Question] ([QuestionID], [StudentID], [TeacherID], [TaskID], [QuestionContent], [AnswerContent], [CreatedAt], [AnsweredAt], [IsAnswered]) VALUES (2, 6, 8, 3, N'ssds', N'ewe', CAST(N'2026-04-21T15:42:58.547' AS DateTime), CAST(N'2026-04-21T15:45:07.893' AS DateTime), 1)
INSERT [dbo].[Question] ([QuestionID], [StudentID], [TeacherID], [TaskID], [QuestionContent], [AnswerContent], [CreatedAt], [AnsweredAt], [IsAnswered]) VALUES (3, 6, 8, 3, N'jgtet', N'eeeeeeeer', CAST(N'2026-04-21T15:51:07.140' AS DateTime), CAST(N'2026-04-21T18:33:47.273' AS DateTime), 1)
SET IDENTITY_INSERT [dbo].[Question] OFF
GO
SET IDENTITY_INSERT [dbo].[Staff] ON 

INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (1, N'Nguyen Minh Quan', N'admin@aptech.local', 1)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (2, N'Tran Staff Office', N'giaovu1@aptech.local', 2)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (3, N'Le Coordinator', N'giaovu2@aptech.local', 3)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (4, N'Tran Van K', N'gv001@aptech.local', 4)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (5, N'Le Thi Hh', N'gv002@aptech.local', 5)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (6, N'Nguyen Nha Phuong', N'nguyennhaphuongbl2006@gmailll.com', 11)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (7, N'Khiem', N'khiem132@gmail.com', 13)
INSERT [dbo].[Staff] ([StaffID], [FullName], [Email], [AccountID]) VALUES (8, N'Nguyen Nha Phuong', N'nguyennhaphuongbl2006@gmail.com', 14)
SET IDENTITY_INSERT [dbo].[Staff] OFF
GO
SET IDENTITY_INSERT [dbo].[Student] ON 

INSERT [dbo].[Student] ([StudentID], [StudentCode], [FullName], [Email], [ClassID], [AccountID], [CreatedByStaffId]) VALUES (1, N'ST001', N'Le Quang Huyy', N'st001@aptech.local', 2, 6, 2)
INSERT [dbo].[Student] ([StudentID], [StudentCode], [FullName], [Email], [ClassID], [AccountID], [CreatedByStaffId]) VALUES (2, N'ST002', N'Pham Ngoc Lan', N'st002@aptech.local', 1, 7, 2)
INSERT [dbo].[Student] ([StudentID], [StudentCode], [FullName], [Email], [ClassID], [AccountID], [CreatedByStaffId]) VALUES (3, N'ST003', N'Vo Gia Bao', N'st003@aptech.local', 1, 8, 2)
INSERT [dbo].[Student] ([StudentID], [StudentCode], [FullName], [Email], [ClassID], [AccountID], [CreatedByStaffId]) VALUES (4, N'ST004', N'Nguyen Hoang Nam', N'st004@aptech.local', 2, 9, 2)
INSERT [dbo].[Student] ([StudentID], [StudentCode], [FullName], [Email], [ClassID], [AccountID], [CreatedByStaffId]) VALUES (5, N'ST005', N'Tran Thu Trang', N'st005@aptech.local', 2, 10, 2)
INSERT [dbo].[Student] ([StudentID], [StudentCode], [FullName], [Email], [ClassID], [AccountID], [CreatedByStaffId]) VALUES (6, N'ST006', N'Nguyen Nha Phuong', N'nguyennhaphuongbl2006@gmail.com', 2, 12, 2)
SET IDENTITY_INSERT [dbo].[Student] OFF
GO
SET IDENTITY_INSERT [dbo].[Task] ON 

INSERT [dbo].[Task] ([TaskID], [GroupID], [Title], [Description], [EstimatedStartDate], [EstimatedEndDate], [ActualStartDate], [ActualEndDate], [Status], [AssignedTo], [ReviewedBy], [CreatedBy], [IsLate], [CreatedAt]) VALUES (1, 2, N'uiux', N'uiux', CAST(N'2026-04-16T18:29:00.000' AS DateTime), CAST(N'2026-04-30T20:41:00.000' AS DateTime), NULL, NULL, 1, NULL, 6, 6, 0, CAST(N'2026-04-16T18:31:38.473' AS DateTime))
INSERT [dbo].[Task] ([TaskID], [GroupID], [Title], [Description], [EstimatedStartDate], [EstimatedEndDate], [ActualStartDate], [ActualEndDate], [Status], [AssignedTo], [ReviewedBy], [CreatedBy], [IsLate], [CreatedAt]) VALUES (2, 2, N'test ask teacher', N'test ask teacher', CAST(N'2026-04-21T14:48:00.000' AS DateTime), CAST(N'2026-04-21T15:48:00.000' AS DateTime), CAST(N'2026-04-21T14:48:35.670' AS DateTime), NULL, 3, 6, 1, 6, 0, CAST(N'2026-04-21T14:48:32.173' AS DateTime))
INSERT [dbo].[Task] ([TaskID], [GroupID], [Title], [Description], [EstimatedStartDate], [EstimatedEndDate], [ActualStartDate], [ActualEndDate], [Status], [AssignedTo], [ReviewedBy], [CreatedBy], [IsLate], [CreatedAt]) VALUES (3, 2, N'tests', N'test ask teacher', CAST(N'2026-04-21T14:48:00.000' AS DateTime), CAST(N'2026-04-21T15:48:00.000' AS DateTime), CAST(N'2026-04-21T14:49:06.480' AS DateTime), NULL, 3, 6, 1, 6, 0, CAST(N'2026-04-21T14:49:02.497' AS DateTime))
SET IDENTITY_INSERT [dbo].[Task] OFF
GO
SET IDENTITY_INSERT [dbo].[TaskAbandonLog] ON 

INSERT [dbo].[TaskAbandonLog] ([LogID], [TaskID], [StudentID], [AbandonedAt], [Note]) VALUES (1, 1, 1, CAST(N'2026-04-16T19:39:25.997' AS DateTime), N'Automatic reset: no execution confirmation after 1 hour')
INSERT [dbo].[TaskAbandonLog] ([LogID], [TaskID], [StudentID], [AbandonedAt], [Note]) VALUES (2, 1, 1, CAST(N'2026-04-21T14:30:39.483' AS DateTime), N'Automatic reset: no execution confirmation after 1 hour')
INSERT [dbo].[TaskAbandonLog] ([LogID], [TaskID], [StudentID], [AbandonedAt], [Note]) VALUES (3, 1, 5, CAST(N'2026-04-21T14:32:39.477' AS DateTime), N'Automatic reset: no execution confirmation after 1 hour')
SET IDENTITY_INSERT [dbo].[TaskAbandonLog] OFF
GO
SET IDENTITY_INSERT [dbo].[TaskStatusHistory] ON 

INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (1, 1, 1, 1, 12, CAST(N'2026-04-16T18:31:38.480' AS DateTime), NULL)
INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (2, 2, 1, 1, 12, CAST(N'2026-04-21T14:48:32.180' AS DateTime), NULL)
INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (3, 2, 1, 2, 12, CAST(N'2026-04-21T14:48:35.670' AS DateTime), NULL)
INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (4, 2, 2, 3, 12, CAST(N'2026-04-21T14:48:46.633' AS DateTime), NULL)
INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (5, 3, 1, 1, 12, CAST(N'2026-04-21T14:49:02.500' AS DateTime), NULL)
INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (6, 3, 1, 2, 12, CAST(N'2026-04-21T14:49:06.480' AS DateTime), NULL)
INSERT [dbo].[TaskStatusHistory] ([HistoryID], [TaskID], [FromStatus], [ToStatus], [ChangedBy], [ChangedAt], [Note]) VALUES (7, 3, 2, 3, 12, CAST(N'2026-04-21T15:52:21.930' AS DateTime), NULL)
SET IDENTITY_INSERT [dbo].[TaskStatusHistory] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Account__536C85E4C1F0C5DA]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Account] ADD UNIQUE NONCLUSTERED 
(
	[Username] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_GroupMember]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[GroupMember] ADD  CONSTRAINT [UQ_GroupMember] UNIQUE NONCLUSTERED 
(
	[GroupID] ASC,
	[StudentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_GroupMember_GroupID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_GroupMember_GroupID] ON [dbo].[GroupMember]
(
	[GroupID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_GroupMember_StudentID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_GroupMember_StudentID] ON [dbo].[GroupMember]
(
	[StudentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UX_GroupMember_OneLeader]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UX_GroupMember_OneLeader] ON [dbo].[GroupMember]
(
	[GroupID] ASC
)
WHERE ([Role]=(1) AND [Status]=(1))
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Message_ReceiverID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Message_ReceiverID] ON [dbo].[Message]
(
	[ReceiverID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Message_TaskID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Message_TaskID] ON [dbo].[Message]
(
	[TaskID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_OtpVerification_AccountID_Purpose]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_OtpVerification_AccountID_Purpose] ON [dbo].[OtpVerification]
(
	[AccountID] ASC,
	[Purpose] ASC
)
INCLUDE([OtpCode],[ExpiresAt],[IsUsed],[AttemptCount]) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Project__149AF30BB509EBE2]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Project] ADD UNIQUE NONCLUSTERED 
(
	[GroupID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Project_GroupID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Project_GroupID] ON [dbo].[Project]
(
	[GroupID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Project_Semester]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Project_Semester] ON [dbo].[Project]
(
	[Semester] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_ProjectGroup_ClassID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_ProjectGroup_ClassID] ON [dbo].[ProjectGroup]
(
	[ClassID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Staff__349DA587E2D8FDEB]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Staff] ADD UNIQUE NONCLUSTERED 
(
	[AccountID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Staff__A9D105344834BF7F]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Staff] ADD UNIQUE NONCLUSTERED 
(
	[Email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Student__1FC8860468DF1BED]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Student] ADD UNIQUE NONCLUSTERED 
(
	[StudentCode] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__Student__349DA5879C6840B3]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Student] ADD UNIQUE NONCLUSTERED 
(
	[AccountID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Student__A9D105343848FD37]    Script Date: 4/22/2026 11:53:49 AM ******/
ALTER TABLE [dbo].[Student] ADD UNIQUE NONCLUSTERED 
(
	[Email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Student_ClassID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Student_ClassID] ON [dbo].[Student]
(
	[ClassID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Task_AssignedTo]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Task_AssignedTo] ON [dbo].[Task]
(
	[AssignedTo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Task_EstimatedEndDate]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Task_EstimatedEndDate] ON [dbo].[Task]
(
	[EstimatedEndDate] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Task_EstimatedStartDate]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Task_EstimatedStartDate] ON [dbo].[Task]
(
	[EstimatedStartDate] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Task_GroupID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Task_GroupID] ON [dbo].[Task]
(
	[GroupID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_Task_Status]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_Task_Status] ON [dbo].[Task]
(
	[Status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_TaskAbandonLog_StudentID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_TaskAbandonLog_StudentID] ON [dbo].[TaskAbandonLog]
(
	[StudentID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_TaskAbandonLog_TaskID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_TaskAbandonLog_TaskID] ON [dbo].[TaskAbandonLog]
(
	[TaskID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_TaskRevision_TaskID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_TaskRevision_TaskID] ON [dbo].[TaskRevision]
(
	[TaskID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_TaskStatusHistory_TaskID]    Script Date: 4/22/2026 11:53:49 AM ******/
CREATE NONCLUSTERED INDEX [IX_TaskStatusHistory_TaskID] ON [dbo].[TaskStatusHistory]
(
	[TaskID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Account] ADD  DEFAULT ((1)) FOR [IsFirstLogin]
GO
ALTER TABLE [dbo].[Account] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[Account] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Class] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[GroupMember] ADD  DEFAULT ((2)) FOR [Role]
GO
ALTER TABLE [dbo].[GroupMember] ADD  DEFAULT ((1)) FOR [Status]
GO
ALTER TABLE [dbo].[GroupMember] ADD  DEFAULT ((0)) FOR [AbandonCount]
GO
ALTER TABLE [dbo].[GroupMember] ADD  DEFAULT (getdate()) FOR [JoinedAt]
GO
ALTER TABLE [dbo].[Message] ADD  DEFAULT (getdate()) FOR [SentAt]
GO
ALTER TABLE [dbo].[Message] ADD  DEFAULT ((0)) FOR [IsRead]
GO
ALTER TABLE [dbo].[OtpVerification] ADD  DEFAULT ((0)) FOR [IsUsed]
GO
ALTER TABLE [dbo].[OtpVerification] ADD  DEFAULT ((0)) FOR [AttemptCount]
GO
ALTER TABLE [dbo].[OtpVerification] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Project] ADD  DEFAULT ((1)) FOR [Status]
GO
ALTER TABLE [dbo].[Project] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[ProjectGroup] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Question] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Question] ADD  DEFAULT ((0)) FOR [IsAnswered]
GO
ALTER TABLE [dbo].[Task] ADD  DEFAULT ((1)) FOR [Status]
GO
ALTER TABLE [dbo].[Task] ADD  DEFAULT ((0)) FOR [IsLate]
GO
ALTER TABLE [dbo].[Task] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[TaskAbandonLog] ADD  DEFAULT (getdate()) FOR [AbandonedAt]
GO
ALTER TABLE [dbo].[TaskRevision] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[TaskStatusHistory] ADD  DEFAULT (getdate()) FOR [ChangedAt]
GO
ALTER TABLE [dbo].[Class]  WITH CHECK ADD FOREIGN KEY([ManagerID])
REFERENCES [dbo].[Staff] ([StaffID])
GO
ALTER TABLE [dbo].[GroupMember]  WITH CHECK ADD FOREIGN KEY([ExcludedBy])
REFERENCES [dbo].[Staff] ([StaffID])
GO
ALTER TABLE [dbo].[GroupMember]  WITH CHECK ADD FOREIGN KEY([GroupID])
REFERENCES [dbo].[ProjectGroup] ([GroupID])
GO
ALTER TABLE [dbo].[GroupMember]  WITH CHECK ADD FOREIGN KEY([StudentID])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[Message]  WITH CHECK ADD FOREIGN KEY([ReceiverID])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[Message]  WITH CHECK ADD FOREIGN KEY([SenderID])
REFERENCES [dbo].[Staff] ([StaffID])
GO
ALTER TABLE [dbo].[Message]  WITH CHECK ADD FOREIGN KEY([TaskID])
REFERENCES [dbo].[Task] ([TaskID])
GO
ALTER TABLE [dbo].[OtpVerification]  WITH CHECK ADD FOREIGN KEY([AccountID])
REFERENCES [dbo].[Account] ([AccountID])
GO
ALTER TABLE [dbo].[Project]  WITH CHECK ADD FOREIGN KEY([AdvisorID])
REFERENCES [dbo].[Staff] ([StaffID])
GO
ALTER TABLE [dbo].[Project]  WITH CHECK ADD FOREIGN KEY([CreatedBy])
REFERENCES [dbo].[Staff] ([StaffID])
GO
ALTER TABLE [dbo].[Project]  WITH CHECK ADD FOREIGN KEY([GroupID])
REFERENCES [dbo].[ProjectGroup] ([GroupID])
GO
ALTER TABLE [dbo].[ProjectGroup]  WITH CHECK ADD FOREIGN KEY([ClassID])
REFERENCES [dbo].[Class] ([ClassID])
GO
ALTER TABLE [dbo].[Question]  WITH CHECK ADD FOREIGN KEY([StudentID])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[Question]  WITH CHECK ADD FOREIGN KEY([TaskID])
REFERENCES [dbo].[Task] ([TaskID])
GO
ALTER TABLE [dbo].[Question]  WITH CHECK ADD FOREIGN KEY([TeacherID])
REFERENCES [dbo].[Staff] ([StaffID])
GO
ALTER TABLE [dbo].[Staff]  WITH CHECK ADD FOREIGN KEY([AccountID])
REFERENCES [dbo].[Account] ([AccountID])
GO
ALTER TABLE [dbo].[Student]  WITH CHECK ADD FOREIGN KEY([AccountID])
REFERENCES [dbo].[Account] ([AccountID])
GO
ALTER TABLE [dbo].[Student]  WITH CHECK ADD FOREIGN KEY([ClassID])
REFERENCES [dbo].[Class] ([ClassID])
GO
ALTER TABLE [dbo].[Task]  WITH CHECK ADD FOREIGN KEY([AssignedTo])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[Task]  WITH CHECK ADD FOREIGN KEY([CreatedBy])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[Task]  WITH CHECK ADD FOREIGN KEY([GroupID])
REFERENCES [dbo].[ProjectGroup] ([GroupID])
GO
ALTER TABLE [dbo].[Task]  WITH CHECK ADD FOREIGN KEY([ReviewedBy])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[TaskAbandonLog]  WITH CHECK ADD FOREIGN KEY([StudentID])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[TaskAbandonLog]  WITH CHECK ADD FOREIGN KEY([TaskID])
REFERENCES [dbo].[Task] ([TaskID])
GO
ALTER TABLE [dbo].[TaskRevision]  WITH CHECK ADD FOREIGN KEY([ReviewedBy])
REFERENCES [dbo].[Student] ([StudentID])
GO
ALTER TABLE [dbo].[TaskRevision]  WITH CHECK ADD FOREIGN KEY([TaskID])
REFERENCES [dbo].[Task] ([TaskID])
GO
ALTER TABLE [dbo].[TaskStatusHistory]  WITH CHECK ADD FOREIGN KEY([ChangedBy])
REFERENCES [dbo].[Account] ([AccountID])
GO
ALTER TABLE [dbo].[TaskStatusHistory]  WITH CHECK ADD FOREIGN KEY([TaskID])
REFERENCES [dbo].[Task] ([TaskID])
GO
ALTER TABLE [dbo].[Account]  WITH CHECK ADD CHECK  (([Role]=(4) OR [Role]=(3) OR [Role]=(2) OR [Role]=(1)))
GO
ALTER TABLE [dbo].[GroupMember]  WITH CHECK ADD CHECK  (([Status]=(2) OR [Status]=(1)))
GO
ALTER TABLE [dbo].[GroupMember]  WITH CHECK ADD CHECK  (([Role]=(2) OR [Role]=(1)))
GO
ALTER TABLE [dbo].[OtpVerification]  WITH CHECK ADD CHECK  (([Purpose]=(2) OR [Purpose]=(1)))
GO
ALTER TABLE [dbo].[Project]  WITH CHECK ADD CHECK  (([Status]=(2) OR [Status]=(1)))
GO
ALTER TABLE [dbo].[Project]  WITH CHECK ADD  CONSTRAINT [CK_Project_Dates] CHECK  (([StartDate]<=[EndDate] AND [ReportDate]>=dateadd(day,(-3),[EndDate]) AND [ReportDate]<=[EndDate]))
GO
ALTER TABLE [dbo].[Project] CHECK CONSTRAINT [CK_Project_Dates]
GO
ALTER TABLE [dbo].[Task]  WITH CHECK ADD CHECK  (([Status]=(5) OR [Status]=(4) OR [Status]=(3) OR [Status]=(2) OR [Status]=(1)))
GO
ALTER TABLE [dbo].[Task]  WITH CHECK ADD  CONSTRAINT [CK_Task_Dates] CHECK  (([EstimatedStartDate]<=[EstimatedEndDate]))
GO
ALTER TABLE [dbo].[Task] CHECK CONSTRAINT [CK_Task_Dates]
GO
ALTER TABLE [dbo].[TaskStatusHistory]  WITH CHECK ADD CHECK  (([FromStatus]=(5) OR [FromStatus]=(4) OR [FromStatus]=(3) OR [FromStatus]=(2) OR [FromStatus]=(1)))
GO
ALTER TABLE [dbo].[TaskStatusHistory]  WITH CHECK ADD CHECK  (([ToStatus]=(5) OR [ToStatus]=(4) OR [ToStatus]=(3) OR [ToStatus]=(2) OR [ToStatus]=(1)))
GO
/****** Object:  StoredProcedure [dbo].[sp_GenerateOtp]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- STORED PROCEDURE: Generate a new OTP [v3]
-- ============================================================
/*
  Call when:
    - A student/staff member requests a password reset -> @Purpose = 1
    - A student logs in for the first time (IsFirstLogin) -> @Purpose = 2

  Returns: OtpCode (the backend reads it and sends the email; do not store plain text on the client)

  Backend flow (Java/Spring):
    1. EXEC sp_GenerateOtp @AccountID=42, @Purpose=1, @OtpCode OUTPUT
    2. Use JavaMailSender to send: "Your OTP code is: " + @OtpCode
    3. OTP expires after 5 minutes - show a countdown on the client
*/
CREATE   PROCEDURE [dbo].[sp_GenerateOtp]
    @AccountID INT,
    @Purpose   TINYINT,           -- 1=ChangePassword, 2=FirstLogin
    @OtpCode   NVARCHAR(6) OUTPUT -- returned to the backend for email delivery
AS
BEGIN
    SET NOCOUNT ON

    -- Get the email for the account (prefer Student, fallback to Staff)
    DECLARE @Email NVARCHAR(100)
    SELECT @Email = COALESCE(
        (SELECT Email FROM Student WHERE AccountID = @AccountID),
        (SELECT Email FROM Staff   WHERE AccountID = @AccountID)
    )

    IF @Email IS NULL
    BEGIN
        RAISERROR(N'No email found for AccountID %d', 16, 1, @AccountID)
        RETURN
    END

    -- Invalidate all active older OTPs for this account and purpose
    UPDATE OtpVerification
    SET IsUsed = 1
    WHERE AccountID = @AccountID
      AND Purpose   = @Purpose
      AND IsUsed    = 0

    -- Generate a random 6-digit OTP (000000 -> 999999)
    SET @OtpCode = RIGHT('000000' + CAST(ABS(CHECKSUM(NEWID())) % 1000000 AS NVARCHAR), 6)

    -- Save the OTP (TTL = 5 minutes)
    INSERT INTO OtpVerification (AccountID, Email, OtpCode, Purpose, ExpiresAt)
    VALUES (@AccountID, @Email, @OtpCode, @Purpose, DATEADD(MINUTE, 5, GETDATE()))
END
GO
/****** Object:  StoredProcedure [dbo].[sp_ResetOverdueTasks]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- STORED PROCEDURE: Reset tasks that were not confirmed within 1 hour
-- ============================================================
CREATE   PROCEDURE [dbo].[sp_ResetOverdueTasks]
AS
BEGIN
    SET NOCOUNT ON

    DECLARE @OverdueTasks TABLE (TaskID INT, StudentID INT)

    INSERT INTO @OverdueTasks (TaskID, StudentID)
    SELECT TaskID, AssignedTo
    FROM Task
    WHERE [Status]      = 1
      AND AssignedTo    IS NOT NULL
      AND EstimatedStartDate <= DATEADD(HOUR, -1, GETDATE())

    INSERT INTO TaskAbandonLog (TaskID, StudentID, Note)
    SELECT TaskID, StudentID, N'Automatic reset: no execution confirmation after 1 hour'
    FROM @OverdueTasks

    UPDATE Task
    SET AssignedTo = NULL
    WHERE TaskID IN (SELECT TaskID FROM @OverdueTasks)
END
GO
/****** Object:  StoredProcedure [dbo].[sp_VerifyOtp]    Script Date: 4/22/2026 11:53:49 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- STORED PROCEDURE: Verify OTP and change password [v3]
-- ============================================================
/*
  Returns ResultCode:
    0 = Success - password updated
    1 = Incorrect OTP (attempts remaining)
    2 = Incorrect OTP, now locked (AttemptCount >= 5)
    3 = OTP expired or already used
    4 = No valid OTP exists for this account and purpose

  @NewPasswordHash: bcrypt hash from the backend - the SP does not hash, it only stores
*/
CREATE   PROCEDURE [dbo].[sp_VerifyOtp]
    @AccountID       INT,
    @Purpose         TINYINT,
    @InputCode       NVARCHAR(6),
    @NewPasswordHash NVARCHAR(255),
    @ResultCode      INT OUTPUT        -- 0=OK, 1=WrongCode, 2=Locked, 3=Expired, 4=NotFound
AS
BEGIN
    SET NOCOUNT ON
    BEGIN TRANSACTION

    DECLARE @OtpID        INT
    DECLARE @OtpCode      NVARCHAR(6)
    DECLARE @ExpiresAt    DATETIME
    DECLARE @IsUsed       BIT
    DECLARE @AttemptCount INT

    -- Get the latest valid OTP for this account and purpose
    SELECT TOP 1
        @OtpID        = OtpID,
        @OtpCode      = OtpCode,
        @ExpiresAt    = ExpiresAt,
        @IsUsed       = IsUsed,
        @AttemptCount = AttemptCount
    FROM OtpVerification
    WHERE AccountID = @AccountID
      AND Purpose   = @Purpose
    ORDER BY CreatedAt DESC

    -- No OTP found
    IF @OtpID IS NULL
    BEGIN
        SET @ResultCode = 4
        ROLLBACK TRANSACTION
        RETURN
    END

    -- OTP already used or expired
    IF @IsUsed = 1 OR @ExpiresAt < GETDATE()
    BEGIN
        SET @ResultCode = 3
        ROLLBACK TRANSACTION
        RETURN
    END

    -- Incorrect code
    IF @OtpCode != @InputCode
    BEGIN
        DECLARE @NewAttempt INT = @AttemptCount + 1

        UPDATE OtpVerification
        SET AttemptCount = @NewAttempt,
            IsUsed       = CASE WHEN @NewAttempt >= 5 THEN 1 ELSE 0 END
        WHERE OtpID = @OtpID

        SET @ResultCode = CASE WHEN @NewAttempt >= 5 THEN 2 ELSE 1 END
        COMMIT TRANSACTION
        RETURN
    END

    -- Correct OTP -> change password and mark OTP as used
    UPDATE OtpVerification
    SET IsUsed = 1
    WHERE OtpID = @OtpID

    UPDATE Account
    SET PasswordHash = @NewPasswordHash,
        IsFirstLogin = 0              -- disable the forced first-login password change flag
    WHERE AccountID = @AccountID

    SET @ResultCode = 0
    COMMIT TRANSACTION
END
GO
USE [master]
GO
ALTER DATABASE [ProjectManagementDB] SET  READ_WRITE 
GO

USE [ProjectManagementDB]
GO

-- ============================================================
-- Submission feature migration
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
        TemplateID         INT IDENTITY(1,1) PRIMARY KEY,
        RequirementName    NVARCHAR(100) NOT NULL UNIQUE,
        RequiredExtension  NVARCHAR(20) NOT NULL,
        SortOrder          INT NOT NULL,
        IsActive           BIT NOT NULL DEFAULT 1
    )
END
GO

IF OBJECT_ID(N'dbo.SubmissionRequirement', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SubmissionRequirement (
        RequirementID      INT IDENTITY(1,1) PRIMARY KEY,
        RequestID          INT NOT NULL,
        RequirementName    NVARCHAR(100) NOT NULL,
        RequiredExtension  NVARCHAR(20) NOT NULL,
        SortOrder          INT NOT NULL,
        IsRequired         BIT NOT NULL DEFAULT 1,
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

-- ============================================================
-- Dashboard-friendly seed data
-- ============================================================

DECLARE @DashSeedPasswordHash NVARCHAR(255) = N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm';
DECLARE @DashNow DATETIME = GETDATE();

IF NOT EXISTS (SELECT 1 FROM Account WHERE Username = N'staff003')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'staff003', @DashSeedPasswordHash, 4, 0, N'no-image.jpg', 1, DATEADD(MONTH, -3, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE Username = N'gv003')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'gv003', @DashSeedPasswordHash, 3, 0, N'no-image.jpg', 1, DATEADD(MONTH, -2, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST006')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST006', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -3, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST007')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST007', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -3, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST008')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST008', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -3, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST009')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST009', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -2, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST010')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST010', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -2, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST011')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST011', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -1, @DashNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST012')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST012', @DashSeedPasswordHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -1, @DashNow));
GO

DECLARE @DashStaff3AccountID INT = (SELECT TOP 1 AccountID FROM Account WHERE Username = N'staff003');
DECLARE @DashTeacher3AccountID INT = (SELECT TOP 1 AccountID FROM Account WHERE Username = N'gv003');

IF @DashTeacher3AccountID IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM Staff WHERE AccountID = @DashTeacher3AccountID)
BEGIN
    INSERT INTO Staff (FullName, Email, AccountID)
    VALUES (N'Mr. Ethan Vu', N'gv003@aptech.local', @DashTeacher3AccountID);
END
GO

DECLARE @DashNow DATETIME = GETDATE();
DECLARE @DashStaff3ID INT = (
    SELECT TOP 1 s.StaffID
    FROM Staff s
    INNER JOIN Account a ON a.AccountID = s.AccountID
    WHERE a.Username = N'staff003'
);

IF @DashStaff3ID IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM Class WHERE ClassName = N'T2401M02')
BEGIN
    INSERT INTO Class (ClassName, AcademicYear, ManagerID, CreatedAt)
    VALUES (N'T2401M02', N'2025-2026', @DashStaff3ID, DATEADD(MONTH, -2, @DashNow));
END
GO

DECLARE @DashStaff1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff001');
DECLARE @DashStaff2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @DashStaff3ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff003');
DECLARE @DashClassB INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2305M02');
DECLARE @DashClassC INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2401M01');
DECLARE @DashClassD INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2401M02');

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST006')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST006', N'Charlotte Bui', N'st006@aptech.local', @DashClassB, AccountID, @DashStaff1ID
    FROM Account WHERE UPPER(Username) = N'ST006';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST007')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST007', N'Benjamin Vu', N'st007@aptech.local', @DashClassC, AccountID, @DashStaff2ID
    FROM Account WHERE UPPER(Username) = N'ST007';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST008')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST008', N'Amelia Dang', N'st008@aptech.local', @DashClassC, AccountID, @DashStaff2ID
    FROM Account WHERE UPPER(Username) = N'ST008';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST009')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST009', N'Elijah Do', N'st009@aptech.local', @DashClassC, AccountID, @DashStaff2ID
    FROM Account WHERE UPPER(Username) = N'ST009';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST010')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST010', N'Harper Vo', N'st010@aptech.local', @DashClassD, AccountID, @DashStaff3ID
    FROM Account WHERE UPPER(Username) = N'ST010';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST011')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST011', N'James Phan', N'st011@aptech.local', @DashClassD, AccountID, @DashStaff3ID
    FROM Account WHERE UPPER(Username) = N'ST011';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST012')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST012', N'Evelyn Truong', N'st012@aptech.local', @DashClassD, AccountID, @DashStaff3ID
    FROM Account WHERE UPPER(Username) = N'ST012';
GO

DECLARE @DashNow DATETIME = GETDATE();
DECLARE @DashClassA INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2305M01');
DECLARE @DashClassB INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2305M02');
DECLARE @DashClassC INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2401M01');
DECLARE @DashClassD INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2401M02');

IF NOT EXISTS (SELECT 1 FROM ProjectGroup WHERE GroupName = N'Alpha Team')
BEGIN
    INSERT INTO ProjectGroup (ClassID, GroupName, CreatedAt)
    VALUES
        (@DashClassA, N'Alpha Team', DATEADD(MONTH, -4, @DashNow)),
        (@DashClassB, N'Beta Builders', DATEADD(MONTH, -3, @DashNow)),
        (@DashClassC, N'Gamma Studio', DATEADD(MONTH, -2, @DashNow)),
        (@DashClassD, N'Delta Works', DATEADD(MONTH, -1, @DashNow));
END
GO

DECLARE @DashNow DATETIME = GETDATE();
DECLARE @DashTeacher1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv001');
DECLARE @DashTeacher2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv002');
DECLARE @DashTeacher3ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv003');
DECLARE @DashStaff1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff001');
DECLARE @DashStaff2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @DashStaff3ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff003');
DECLARE @DashGroupAlpha INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Alpha Team');
DECLARE @DashGroupBeta INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Beta Builders');
DECLARE @DashGroupGamma INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Gamma Studio');
DECLARE @DashGroupDelta INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Delta Works');

IF NOT EXISTS (SELECT 1 FROM Project WHERE Title = N'Campus Marketplace')
BEGIN
    INSERT INTO Project (GroupID, Title, Description, Semester, StartDate, EndDate, ReportDate, AdvisorID, CreatedBy, Status, CreatedAt)
    VALUES
        (@DashGroupAlpha, N'Campus Marketplace', N'Java web marketplace for student trading.', N'S2',
            CAST(DATEADD(MONTH, -4, @DashNow) AS DATE), CAST(DATEADD(MONTH, 1, @DashNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, 1, @DashNow)) AS DATE),
            @DashTeacher1ID, @DashStaff1ID, 1, DATEADD(MONTH, -4, @DashNow)),
        (@DashGroupBeta, N'Fitness Booking App', N'Desktop and mobile workflow for coach booking.', N'S2',
            CAST(DATEADD(MONTH, -3, @DashNow) AS DATE), CAST(DATEADD(MONTH, 2, @DashNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, 2, @DashNow)) AS DATE),
            @DashTeacher2ID, @DashStaff1ID, 1, DATEADD(MONTH, -3, @DashNow)),
        (@DashGroupGamma, N'Smart Attendance', N'Attendance analytics and QR scanning platform.', N'S2',
            CAST(DATEADD(MONTH, -2, @DashNow) AS DATE), CAST(DATEADD(MONTH, 3, @DashNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, 3, @DashNow)) AS DATE),
            @DashTeacher2ID, @DashStaff2ID, 1, DATEADD(MONTH, -2, @DashNow)),
        (@DashGroupDelta, N'Library Insights', N'Data-driven library management and reporting.', N'S2',
            CAST(DATEADD(MONTH, -6, @DashNow) AS DATE), CAST(DATEADD(MONTH, -1, @DashNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, -1, @DashNow)) AS DATE),
            @DashTeacher3ID, @DashStaff3ID, 2, DATEADD(MONTH, -6, @DashNow));
END
GO

DECLARE @DashNow DATETIME = GETDATE();
DECLARE @DashGroupAlpha INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Alpha Team');
DECLARE @DashGroupBeta INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Beta Builders');
DECLARE @DashGroupGamma INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Gamma Studio');
DECLARE @DashGroupDelta INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Delta Works');
DECLARE @DashSt1 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST001');
DECLARE @DashSt2 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST002');
DECLARE @DashSt3 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST003');
DECLARE @DashSt4 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST004');
DECLARE @DashSt5 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST005');
DECLARE @DashSt6 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST006');
DECLARE @DashSt7 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST007');
DECLARE @DashSt8 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST008');
DECLARE @DashSt9 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST009');
DECLARE @DashSt10 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST010');
DECLARE @DashSt11 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST011');
DECLARE @DashSt12 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST012');

IF NOT EXISTS (SELECT 1 FROM GroupMember WHERE GroupID = @DashGroupAlpha AND StudentID = @DashSt1)
BEGIN
    INSERT INTO GroupMember (GroupID, StudentID, Role, Status, JoinedAt)
    VALUES
        (@DashGroupAlpha, @DashSt1, 1, 1, DATEADD(MONTH, -4, @DashNow)),
        (@DashGroupAlpha, @DashSt2, 2, 1, DATEADD(MONTH, -4, @DashNow)),
        (@DashGroupAlpha, @DashSt3, 2, 1, DATEADD(MONTH, -4, @DashNow)),
        (@DashGroupBeta, @DashSt4, 1, 1, DATEADD(MONTH, -3, @DashNow)),
        (@DashGroupBeta, @DashSt5, 2, 1, DATEADD(MONTH, -3, @DashNow)),
        (@DashGroupBeta, @DashSt6, 2, 1, DATEADD(MONTH, -3, @DashNow)),
        (@DashGroupGamma, @DashSt7, 1, 1, DATEADD(MONTH, -2, @DashNow)),
        (@DashGroupGamma, @DashSt8, 2, 1, DATEADD(MONTH, -2, @DashNow)),
        (@DashGroupGamma, @DashSt9, 2, 1, DATEADD(MONTH, -2, @DashNow)),
        (@DashGroupDelta, @DashSt10, 1, 1, DATEADD(MONTH, -6, @DashNow)),
        (@DashGroupDelta, @DashSt11, 2, 1, DATEADD(MONTH, -6, @DashNow)),
        (@DashGroupDelta, @DashSt12, 2, 1, DATEADD(MONTH, -6, @DashNow));
END
GO

DECLARE @DashNow DATETIME = GETDATE();
DECLARE @DashGroupAlpha INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Alpha Team');
DECLARE @DashGroupBeta INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Beta Builders');
DECLARE @DashGroupGamma INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Gamma Studio');
DECLARE @DashGroupDelta INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Delta Works');
DECLARE @DashSt1 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST001');
DECLARE @DashSt2 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST002');
DECLARE @DashSt3 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST003');
DECLARE @DashSt4 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST004');
DECLARE @DashSt5 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST005');
DECLARE @DashSt6 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST006');
DECLARE @DashSt7 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST007');
DECLARE @DashSt8 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST008');
DECLARE @DashSt9 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST009');
DECLARE @DashSt10 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST010');
DECLARE @DashSt11 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST011');
DECLARE @DashSt12 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST012');

IF NOT EXISTS (SELECT 1 FROM Task WHERE Title = N'Gather marketplace requirements')
BEGIN
    INSERT INTO Task (GroupID, Title, Description, EstimatedStartDate, EstimatedEndDate, ActualStartDate, ActualEndDate, Status, AssignedTo, ReviewedBy, CreatedBy, IsLate, CreatedAt)
    VALUES
        (@DashGroupAlpha, N'Gather marketplace requirements', N'Interview users and validate scope.', DATEADD(MONTH, -4, @DashNow), DATEADD(MONTH, -3, @DashNow), DATEADD(MONTH, -4, @DashNow), DATEADD(DAY, -75, @DashNow), 5, @DashSt2, @DashSt1, @DashSt1, 0, DATEADD(MONTH, -4, @DashNow)),
        (@DashGroupAlpha, N'Build seller onboarding', N'Create onboarding flow and approval form.', DATEADD(MONTH, -2, @DashNow), DATEADD(DAY, -10, @DashNow), DATEADD(MONTH, -2, @DashNow), NULL, 3, @DashSt3, @DashSt1, @DashSt1, 0, DATEADD(MONTH, -2, @DashNow)),
        (@DashGroupBeta, N'Design booking dashboard', N'Staff and trainer dashboard wireframes.', DATEADD(MONTH, -3, @DashNow), DATEADD(MONTH, -2, @DashNow), DATEADD(MONTH, -3, @DashNow), DATEADD(MONTH, -2, @DashNow), 5, @DashSt5, @DashSt4, @DashSt4, 0, DATEADD(MONTH, -3, @DashNow)),
        (@DashGroupBeta, N'Implement booking calendar', N'Interactive booking calendar and rules.', DATEADD(DAY, -45, @DashNow), DATEADD(DAY, -5, @DashNow), DATEADD(DAY, -40, @DashNow), NULL, 2, @DashSt6, @DashSt4, @DashSt4, 0, DATEADD(DAY, -45, @DashNow)),
        (@DashGroupGamma, N'Prepare QR attendance module', N'QR generation and attendance scanner.', DATEADD(DAY, -30, @DashNow), DATEADD(DAY, 5, @DashNow), DATEADD(DAY, -28, @DashNow), NULL, 2, @DashSt8, @DashSt7, @DashSt7, 0, DATEADD(DAY, -30, @DashNow)),
        (@DashGroupGamma, N'Build attendance analytics', N'Charts for attendance heatmaps.', DATEADD(DAY, -20, @DashNow), DATEADD(DAY, -2, @DashNow), DATEADD(DAY, -18, @DashNow), NULL, 4, @DashSt9, @DashSt7, @DashSt7, 1, DATEADD(DAY, -20, @DashNow)),
        (@DashGroupDelta, N'Data model for library stock', N'Finalize ERD and migration scripts.', DATEADD(MONTH, -6, @DashNow), DATEADD(MONTH, -5, @DashNow), DATEADD(MONTH, -6, @DashNow), DATEADD(MONTH, -5, @DashNow), 5, @DashSt11, @DashSt10, @DashSt10, 0, DATEADD(MONTH, -6, @DashNow)),
        (@DashGroupDelta, N'Usage reporting module', N'Usage charts and report exports.', DATEADD(MONTH, -5, @DashNow), DATEADD(MONTH, -2, @DashNow), DATEADD(MONTH, -5, @DashNow), DATEADD(MONTH, -1, @DashNow), 5, @DashSt12, @DashSt10, @DashSt10, 1, DATEADD(MONTH, -5, @DashNow));
END
GO

DECLARE @DashNow DATETIME = GETDATE();
DECLARE @DashTask2 INT = (SELECT TOP 1 TaskID FROM Task WHERE Title = N'Build seller onboarding');
DECLARE @DashTask4 INT = (SELECT TOP 1 TaskID FROM Task WHERE Title = N'Implement booking calendar');
DECLARE @DashTask5 INT = (SELECT TOP 1 TaskID FROM Task WHERE Title = N'Prepare QR attendance module');
DECLARE @DashTask6 INT = (SELECT TOP 1 TaskID FROM Task WHERE Title = N'Build attendance analytics');
DECLARE @DashTask8 INT = (SELECT TOP 1 TaskID FROM Task WHERE Title = N'Usage reporting module');
DECLARE @DashSt1 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST001');
DECLARE @DashSt3 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST003');
DECLARE @DashSt4 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST004');
DECLARE @DashSt7 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST007');
DECLARE @DashSt9 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST009');
DECLARE @DashSt10 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST010');
DECLARE @DashSt12 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST012');
DECLARE @DashTeacher1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv001');
DECLARE @DashTeacher2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv002');
DECLARE @DashStaff2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @DashStaff3ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff003');

IF NOT EXISTS (SELECT 1 FROM TaskRevision WHERE TaskID = @DashTask2)
BEGIN
    INSERT INTO TaskRevision (TaskID, ReviewedBy, Note, CreatedAt)
    VALUES
        (@DashTask2, @DashSt1, N'Please shorten the onboarding steps and add validation messages.', DATEADD(DAY, -8, @DashNow)),
        (@DashTask6, @DashSt7, N'Analytics needs clearer legends and daily breakdown.', DATEADD(DAY, -3, @DashNow));
END

IF NOT EXISTS (SELECT 1 FROM TaskStatusHistory WHERE TaskID = @DashTask2)
BEGIN
    INSERT INTO TaskStatusHistory (TaskID, FromStatus, ToStatus, ChangedBy, ChangedAt, Note)
    VALUES
        (@DashTask2, 1, 2, (SELECT TOP 1 AccountID FROM Student WHERE StudentID = @DashSt3), DATEADD(MONTH, -2, @DashNow), N'Work started'),
        (@DashTask2, 2, 3, (SELECT TOP 1 AccountID FROM Student WHERE StudentID = @DashSt3), DATEADD(DAY, -9, @DashNow), N'Ready for review'),
        (@DashTask6, 2, 4, (SELECT TOP 1 AccountID FROM Student WHERE StudentID = @DashSt9), DATEADD(DAY, -3, @DashNow), N'Needs revision after review'),
        (@DashTask8, 2, 5, (SELECT TOP 1 AccountID FROM Student WHERE StudentID = @DashSt12), DATEADD(MONTH, -1, @DashNow), N'Completed after fixes');
END

IF NOT EXISTS (SELECT 1 FROM TaskAbandonLog WHERE TaskID = @DashTask6 AND StudentID = @DashSt9)
BEGIN
    INSERT INTO TaskAbandonLog (TaskID, StudentID, AbandonedAt, Note)
    VALUES (@DashTask6, @DashSt9, DATEADD(DAY, -6, @DashNow), N'Missed hand-off and failed to confirm progress');
END

IF NOT EXISTS (SELECT 1 FROM [Message] WHERE Content LIKE N'%progress summary%')
BEGIN
    INSERT INTO [Message] (SenderID, ReceiverID, TaskID, Content, SentAt, IsRead)
    VALUES
        (@DashTeacher1ID, @DashSt1, @DashTask2, N'Please prepare a concise progress summary for tomorrow.', DATEADD(DAY, -4, @DashNow), 1),
        (@DashTeacher2ID, @DashSt4, @DashTask4, N'Update the booking calendar demo before Friday.', DATEADD(DAY, -2, @DashNow), 0),
        (@DashStaff2ID, @DashSt7, @DashTask5, N'Remember the upcoming interim checkpoint next week.', DATEADD(DAY, -1, @DashNow), 0),
        (@DashStaff3ID, @DashSt10, @DashTask8, N'Upload the final reporting screenshots for archive.', DATEADD(DAY, -20, @DashNow), 1);
END
GO

-- ============================================================
-- Submission sample data
-- Depends on dashboard seed data above
-- ============================================================

DECLARE @SubmissionNow DATETIME = GETDATE();
DECLARE @SubmissionStaff1ID INT = (SELECT TOP 1 StaffID FROM dbo.Staff s INNER JOIN dbo.Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff001');
DECLARE @SubmissionStaff2ID INT = (SELECT TOP 1 StaffID FROM dbo.Staff s INNER JOIN dbo.Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @SubmissionGroupAlpha INT = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Alpha Team');
DECLARE @SubmissionGroupBeta INT = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Beta Builders');
DECLARE @SubmissionGroupGamma INT = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Gamma Studio');
DECLARE @SubmissionLeaderAlpha INT = (SELECT TOP 1 StudentID FROM dbo.GroupMember WHERE GroupID = @SubmissionGroupAlpha AND [Role] = 1 AND [Status] = 1);
DECLARE @SubmissionLeaderBeta INT = (SELECT TOP 1 StudentID FROM dbo.GroupMember WHERE GroupID = @SubmissionGroupBeta AND [Role] = 1 AND [Status] = 1);
DECLARE @SubmissionLeaderGamma INT = (SELECT TOP 1 StudentID FROM dbo.GroupMember WHERE GroupID = @SubmissionGroupGamma AND [Role] = 1 AND [Status] = 1);

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequest WHERE Title = N'Checkpoint 1 submission pack')
   AND @SubmissionGroupAlpha IS NOT NULL
   AND @SubmissionGroupBeta IS NOT NULL
BEGIN
    DECLARE @RequestAlpha INT;
    INSERT INTO dbo.SubmissionRequest (Title, Description, Deadline, CreatedByStaffID, Status, CreatedAt)
    VALUES (N'Checkpoint 1 submission pack', N'Upload the first review package for the current sprint.', DATEADD(DAY, 5, @SubmissionNow), @SubmissionStaff1ID, 1, DATEADD(DAY, -7, @SubmissionNow));
    SET @RequestAlpha = SCOPE_IDENTITY();

    INSERT INTO dbo.SubmissionRequirement (RequestID, RequirementName, RequiredExtension, SortOrder, IsRequired)
    SELECT @RequestAlpha, RequirementName, RequiredExtension, SortOrder, 1
    FROM dbo.SubmissionRequirementTemplate
    WHERE RequirementName IN (N'Database', N'Project report', N'Source Code', N'Slide PowerPoint');

    INSERT INTO dbo.SubmissionTarget (RequestID, GroupID, LeaderStudentID, Status, NotifiedAt, CreatedAt)
    VALUES
        (@RequestAlpha, @SubmissionGroupAlpha, @SubmissionLeaderAlpha, 2, DATEADD(DAY, -6, @SubmissionNow), DATEADD(DAY, -7, @SubmissionNow)),
        (@RequestAlpha, @SubmissionGroupBeta, @SubmissionLeaderBeta, 1, DATEADD(DAY, -6, @SubmissionNow), DATEADD(DAY, -7, @SubmissionNow));
END
GO

DECLARE @SubmissionNow DATETIME = GETDATE();
DECLARE @SubmissionStaff2ID INT = (SELECT TOP 1 StaffID FROM dbo.Staff s INNER JOIN dbo.Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @SubmissionGroupGamma INT = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Gamma Studio');
DECLARE @SubmissionLeaderGamma INT = (SELECT TOP 1 StudentID FROM dbo.GroupMember WHERE GroupID = @SubmissionGroupGamma AND [Role] = 1 AND [Status] = 1);

IF NOT EXISTS (SELECT 1 FROM dbo.SubmissionRequest WHERE Title = N'Final demo package')
   AND @SubmissionGroupGamma IS NOT NULL
BEGIN
    DECLARE @RequestBeta INT;
    INSERT INTO dbo.SubmissionRequest (Title, Description, Deadline, CreatedByStaffID, Status, CreatedAt)
    VALUES (N'Final demo package', N'Final report, slides, and supporting demo files.', DATEADD(DAY, 14, @SubmissionNow), @SubmissionStaff2ID, 1, DATEADD(DAY, -2, @SubmissionNow));
    SET @RequestBeta = SCOPE_IDENTITY();

    INSERT INTO dbo.SubmissionRequirement (RequestID, RequirementName, RequiredExtension, SortOrder, IsRequired)
    SELECT @RequestBeta, RequirementName, RequiredExtension, SortOrder, 1
    FROM dbo.SubmissionRequirementTemplate
    WHERE RequirementName IN (N'eProjects Status report', N'eProjects Feedback Form', N'Project report', N'Video');

    INSERT INTO dbo.SubmissionTarget (RequestID, GroupID, LeaderStudentID, Status, NotifiedAt, CreatedAt)
    VALUES (@RequestBeta, @SubmissionGroupGamma, @SubmissionLeaderGamma, 1, DATEADD(DAY, -1, @SubmissionNow), DATEADD(DAY, -2, @SubmissionNow));
END
GO

DECLARE @TargetAlpha INT = (
    SELECT TOP 1 t.TargetID
    FROM dbo.SubmissionTarget t
    INNER JOIN dbo.SubmissionRequest r ON r.RequestID = t.RequestID
    WHERE r.Title = N'Checkpoint 1 submission pack'
      AND t.GroupID = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Alpha Team')
);
DECLARE @TargetBeta INT = (
    SELECT TOP 1 t.TargetID
    FROM dbo.SubmissionTarget t
    INNER JOIN dbo.SubmissionRequest r ON r.RequestID = t.RequestID
    WHERE r.Title = N'Checkpoint 1 submission pack'
      AND t.GroupID = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Beta Builders')
);
DECLARE @LeaderAlpha INT = (SELECT TOP 1 StudentID FROM dbo.GroupMember WHERE GroupID = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Alpha Team') AND [Role] = 1 AND [Status] = 1);
DECLARE @LeaderBeta INT = (SELECT TOP 1 StudentID FROM dbo.GroupMember WHERE GroupID = (SELECT TOP 1 GroupID FROM dbo.ProjectGroup WHERE GroupName = N'Beta Builders') AND [Role] = 1 AND [Status] = 1);

IF @TargetAlpha IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.SubmissionFile WHERE TargetID = @TargetAlpha)
BEGIN
    INSERT INTO dbo.SubmissionFile (TargetID, RequirementID, OriginalFileName, StoredFileName, FilePath, FileSize, UploadedByStudentID, UploadedAt)
    SELECT
        @TargetAlpha,
        RequirementID,
        RequirementName + RequiredExtension,
        N'alpha_' + CAST(RequirementID AS NVARCHAR(10)) + RequiredExtension,
        N'submissions/alpha/' + CAST(RequirementID AS NVARCHAR(10)) + RequiredExtension,
        1024 + (RequirementID * 256),
        @LeaderAlpha,
        DATEADD(DAY, -3, GETDATE())
    FROM dbo.SubmissionRequirement
    WHERE RequestID = (SELECT TOP 1 RequestID FROM dbo.SubmissionRequest WHERE Title = N'Checkpoint 1 submission pack')
      AND RequirementName IN (N'Database', N'Project report', N'Source Code', N'Slide PowerPoint');

    UPDATE dbo.SubmissionTarget
    SET Status = 3,
        SubmittedAt = DATEADD(DAY, -3, GETDATE())
    WHERE TargetID = @TargetAlpha;
END

IF @TargetBeta IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.SubmissionFile WHERE TargetID = @TargetBeta)
BEGIN
    INSERT INTO dbo.SubmissionFile (TargetID, RequirementID, OriginalFileName, StoredFileName, FilePath, FileSize, UploadedByStudentID, UploadedAt)
    SELECT TOP 2
        @TargetBeta,
        RequirementID,
        RequirementName + RequiredExtension,
        N'beta_' + CAST(RequirementID AS NVARCHAR(10)) + RequiredExtension,
        N'submissions/beta/' + CAST(RequirementID AS NVARCHAR(10)) + RequiredExtension,
        2048 + (RequirementID * 300),
        @LeaderBeta,
        DATEADD(DAY, -1, GETDATE())
    FROM dbo.SubmissionRequirement
    WHERE RequestID = (SELECT TOP 1 RequestID FROM dbo.SubmissionRequest WHERE Title = N'Checkpoint 1 submission pack')
    ORDER BY SortOrder;

    UPDATE dbo.SubmissionTarget
    SET Status = 2
    WHERE TargetID = @TargetBeta;
END
GO

-- ============================================================
-- Additional dashboard variation seed
-- Adds more spread across managers, advisors, statuses, and months
-- ============================================================

DECLARE @ChartSeedHash NVARCHAR(255) = N'$2a$10$rBOX8JhuiuGuuyuBNltmNuloJgp0MSCFercS7fNY.toW4tV0tpafm';
DECLARE @ChartNow DATETIME = GETDATE();

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST013')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST013', @ChartSeedHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -5, @ChartNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST014')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST014', @ChartSeedHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -4, @ChartNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST015')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST015', @ChartSeedHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -4, @ChartNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST016')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST016', @ChartSeedHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -3, @ChartNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST017')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST017', @ChartSeedHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -2, @ChartNow));

IF NOT EXISTS (SELECT 1 FROM Account WHERE UPPER(Username) = N'ST018')
    INSERT INTO Account (Username, PasswordHash, [Role], IsFirstLogin, PhotoUrl, IsActive, CreatedAt)
    VALUES (N'ST018', @ChartSeedHash, 2, 0, N'no-image.jpg', 1, DATEADD(MONTH, -1, @ChartNow));
GO

DECLARE @ChartStaff1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff001');
DECLARE @ChartStaff2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');

IF NOT EXISTS (SELECT 1 FROM Class WHERE ClassName = N'T2405M01')
    INSERT INTO Class (ClassName, AcademicYear, ManagerID, CreatedAt)
    VALUES (N'T2405M01', N'2025-2026', @ChartStaff1ID, DATEADD(MONTH, -3, GETDATE()));

IF NOT EXISTS (SELECT 1 FROM Class WHERE ClassName = N'T2405M02')
    INSERT INTO Class (ClassName, AcademicYear, ManagerID, CreatedAt)
    VALUES (N'T2405M02', N'2025-2026', @ChartStaff2ID, DATEADD(MONTH, -2, GETDATE()));
GO

DECLARE @ChartStaff1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff001');
DECLARE @ChartStaff2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @ChartClassE INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2405M01');
DECLARE @ChartClassF INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2405M02');

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST013')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST013', N'Mason Le', N'st013@aptech.local', @ChartClassE, AccountID, @ChartStaff1ID
    FROM Account WHERE UPPER(Username) = N'ST013';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST014')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST014', N'Sophia Nguyen', N'st014@aptech.local', @ChartClassE, AccountID, @ChartStaff1ID
    FROM Account WHERE UPPER(Username) = N'ST014';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST015')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST015', N'Lucas Tran', N'st015@aptech.local', @ChartClassE, AccountID, @ChartStaff1ID
    FROM Account WHERE UPPER(Username) = N'ST015';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST016')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST016', N'Emma Ho', N'st016@aptech.local', @ChartClassF, AccountID, @ChartStaff2ID
    FROM Account WHERE UPPER(Username) = N'ST016';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST017')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST017', N'Noah Pham', N'st017@aptech.local', @ChartClassF, AccountID, @ChartStaff2ID
    FROM Account WHERE UPPER(Username) = N'ST017';

IF NOT EXISTS (SELECT 1 FROM Student WHERE StudentCode = N'ST018')
    INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID, CreatedByStaffId)
    SELECT N'ST018', N'Olivia Vu', N'st018@aptech.local', @ChartClassF, AccountID, @ChartStaff2ID
    FROM Account WHERE UPPER(Username) = N'ST018';
GO

DECLARE @ChartClassA INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2305M01');
DECLARE @ChartClassC INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2401M01');
DECLARE @ChartClassE INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2405M01');
DECLARE @ChartClassF INT = (SELECT TOP 1 ClassID FROM Class WHERE ClassName = N'T2405M02');

IF NOT EXISTS (SELECT 1 FROM ProjectGroup WHERE GroupName = N'Omega Lab')
    INSERT INTO ProjectGroup (ClassID, GroupName, CreatedAt)
    VALUES (@ChartClassA, N'Omega Lab', DATEADD(MONTH, -5, GETDATE()));

IF NOT EXISTS (SELECT 1 FROM ProjectGroup WHERE GroupName = N'Nova Crew')
    INSERT INTO ProjectGroup (ClassID, GroupName, CreatedAt)
    VALUES (@ChartClassC, N'Nova Crew', DATEADD(MONTH, -4, GETDATE()));

IF NOT EXISTS (SELECT 1 FROM ProjectGroup WHERE GroupName = N'Pixel Forge')
    INSERT INTO ProjectGroup (ClassID, GroupName, CreatedAt)
    VALUES (@ChartClassE, N'Pixel Forge', DATEADD(MONTH, -3, GETDATE()));

IF NOT EXISTS (SELECT 1 FROM ProjectGroup WHERE GroupName = N'Insight Hub')
    INSERT INTO ProjectGroup (ClassID, GroupName, CreatedAt)
    VALUES (@ChartClassF, N'Insight Hub', DATEADD(MONTH, -2, GETDATE()));
GO

DECLARE @ChartNow DATETIME = GETDATE();
DECLARE @ChartTeacher1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv001');
DECLARE @ChartTeacher2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv002');
DECLARE @ChartTeacher3ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'gv003');
DECLARE @ChartStaff1ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff001');
DECLARE @ChartStaff2ID INT = (SELECT TOP 1 s.StaffID FROM Staff s INNER JOIN Account a ON a.AccountID = s.AccountID WHERE a.Username = N'staff002');
DECLARE @ChartGroupOmega INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Omega Lab');
DECLARE @ChartGroupNova INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Nova Crew');
DECLARE @ChartGroupPixel INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Pixel Forge');
DECLARE @ChartGroupInsight INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Insight Hub');

IF NOT EXISTS (SELECT 1 FROM Project WHERE Title = N'Event Check-in Kiosk')
BEGIN
    INSERT INTO Project (GroupID, Title, Description, Semester, StartDate, EndDate, ReportDate, AdvisorID, CreatedBy, Status, CreatedAt)
    VALUES
        (@ChartGroupOmega, N'Event Check-in Kiosk', N'Self-service check-in tool for school events.', N'S1',
            CAST(DATEADD(MONTH, -5, @ChartNow) AS DATE), CAST(DATEADD(MONTH, -1, @ChartNow) AS DATE), CAST(DATEADD(DAY, -3, DATEADD(MONTH, -1, @ChartNow)) AS DATE),
            @ChartTeacher1ID, @ChartStaff1ID, 2, DATEADD(MONTH, -5, @ChartNow)),
        (@ChartGroupNova, N'Course Feedback Portal', N'Collect and analyze end-of-course feedback.', N'S2',
            CAST(DATEADD(MONTH, -4, @ChartNow) AS DATE), CAST(DATEADD(MONTH, 1, @ChartNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, 1, @ChartNow)) AS DATE),
            @ChartTeacher2ID, @ChartStaff2ID, 1, DATEADD(MONTH, -4, @ChartNow)),
        (@ChartGroupPixel, N'Internship Tracker', N'Track company applications and mentor feedback.', N'S2',
            CAST(DATEADD(MONTH, -3, @ChartNow) AS DATE), CAST(DATEADD(MONTH, 2, @ChartNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, 2, @ChartNow)) AS DATE),
            @ChartTeacher3ID, @ChartStaff1ID, 1, DATEADD(MONTH, -3, @ChartNow)),
        (@ChartGroupInsight, N'Lab Asset Monitor', N'Monitor equipment issues and usage trends.', N'S2',
            CAST(DATEADD(MONTH, -2, @ChartNow) AS DATE), CAST(DATEADD(MONTH, 2, @ChartNow) AS DATE), CAST(DATEADD(DAY, -2, DATEADD(MONTH, 2, @ChartNow)) AS DATE),
            @ChartTeacher2ID, @ChartStaff2ID, 1, DATEADD(MONTH, -2, @ChartNow));
END
GO

DECLARE @ChartNow DATETIME = GETDATE();
DECLARE @ChartGroupOmega INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Omega Lab');
DECLARE @ChartGroupNova INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Nova Crew');
DECLARE @ChartGroupPixel INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Pixel Forge');
DECLARE @ChartGroupInsight INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Insight Hub');
DECLARE @ChartSt2 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST002');
DECLARE @ChartSt3 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST003');
DECLARE @ChartSt7 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST007');
DECLARE @ChartSt8 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST008');
DECLARE @ChartSt13 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST013');
DECLARE @ChartSt14 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST014');
DECLARE @ChartSt15 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST015');
DECLARE @ChartSt16 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST016');
DECLARE @ChartSt17 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST017');
DECLARE @ChartSt18 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST018');
DECLARE @ChartSt10 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST010');
DECLARE @ChartSt11 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST011');

IF NOT EXISTS (SELECT 1 FROM GroupMember WHERE GroupID = @ChartGroupOmega AND StudentID = @ChartSt2)
BEGIN
    INSERT INTO GroupMember (GroupID, StudentID, Role, Status, JoinedAt)
    VALUES
        (@ChartGroupOmega, @ChartSt2, 1, 1, DATEADD(MONTH, -5, @ChartNow)),
        (@ChartGroupOmega, @ChartSt3, 2, 1, DATEADD(MONTH, -5, @ChartNow)),
        (@ChartGroupOmega, @ChartSt13, 2, 1, DATEADD(MONTH, -5, @ChartNow)),
        (@ChartGroupNova, @ChartSt7, 1, 1, DATEADD(MONTH, -4, @ChartNow)),
        (@ChartGroupNova, @ChartSt8, 2, 1, DATEADD(MONTH, -4, @ChartNow)),
        (@ChartGroupNova, @ChartSt14, 2, 1, DATEADD(MONTH, -4, @ChartNow)),
        (@ChartGroupPixel, @ChartSt15, 1, 1, DATEADD(MONTH, -3, @ChartNow)),
        (@ChartGroupPixel, @ChartSt16, 2, 1, DATEADD(MONTH, -3, @ChartNow)),
        (@ChartGroupPixel, @ChartSt17, 2, 1, DATEADD(MONTH, -3, @ChartNow)),
        (@ChartGroupInsight, @ChartSt18, 1, 1, DATEADD(MONTH, -2, @ChartNow)),
        (@ChartGroupInsight, @ChartSt10, 2, 1, DATEADD(MONTH, -2, @ChartNow)),
        (@ChartGroupInsight, @ChartSt11, 2, 1, DATEADD(MONTH, -2, @ChartNow));
END
GO

DECLARE @ChartNow DATETIME = GETDATE();
DECLARE @ChartGroupOmega INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Omega Lab');
DECLARE @ChartGroupNova INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Nova Crew');
DECLARE @ChartGroupPixel INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Pixel Forge');
DECLARE @ChartGroupInsight INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Insight Hub');
DECLARE @ChartSt2 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST002');
DECLARE @ChartSt3 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST003');
DECLARE @ChartSt7 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST007');
DECLARE @ChartSt8 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST008');
DECLARE @ChartSt10 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST010');
DECLARE @ChartSt11 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST011');
DECLARE @ChartSt13 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST013');
DECLARE @ChartSt14 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST014');
DECLARE @ChartSt15 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST015');
DECLARE @ChartSt16 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST016');
DECLARE @ChartSt17 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST017');
DECLARE @ChartSt18 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST018');

IF NOT EXISTS (SELECT 1 FROM Task WHERE Title = N'Prepare kiosk layout')
BEGIN
    INSERT INTO Task (GroupID, Title, Description, EstimatedStartDate, EstimatedEndDate, ActualStartDate, ActualEndDate, Status, AssignedTo, ReviewedBy, CreatedBy, IsLate, CreatedAt)
    VALUES
        (@ChartGroupOmega, N'Prepare kiosk layout', N'Design the event check-in screen flow.', DATEADD(MONTH, -5, @ChartNow), DATEADD(MONTH, -4, @ChartNow), DATEADD(MONTH, -5, @ChartNow), DATEADD(MONTH, -4, @ChartNow), 5, @ChartSt3, @ChartSt2, @ChartSt2, 0, DATEADD(MONTH, -5, @ChartNow)),
        (@ChartGroupOmega, N'Add QR validation', N'Validate QR payload and access logs.', DATEADD(MONTH, -4, @ChartNow), DATEADD(MONTH, -3, @ChartNow), DATEADD(MONTH, -4, @ChartNow), DATEADD(MONTH, -3, @ChartNow), 5, @ChartSt13, @ChartSt2, @ChartSt2, 0, DATEADD(MONTH, -4, @ChartNow)),
        (@ChartGroupNova, N'Collect survey requirements', N'Interview students and map survey fields.', DATEADD(MONTH, -4, @ChartNow), DATEADD(MONTH, -3, @ChartNow), DATEADD(MONTH, -4, @ChartNow), DATEADD(MONTH, -3, @ChartNow), 5, @ChartSt8, @ChartSt7, @ChartSt7, 0, DATEADD(MONTH, -4, @ChartNow)),
        (@ChartGroupNova, N'Build sentiment dashboard', N'Create charts for ratings and comments.', DATEADD(MONTH, -2, @ChartNow), DATEADD(DAY, -12, @ChartNow), DATEADD(MONTH, -2, @ChartNow), NULL, 3, @ChartSt14, @ChartSt7, @ChartSt7, 0, DATEADD(MONTH, -2, @ChartNow)),
        (@ChartGroupPixel, N'Create internship pipeline', N'Define application stages and notifications.', DATEADD(MONTH, -3, @ChartNow), DATEADD(MONTH, -2, @ChartNow), DATEADD(MONTH, -3, @ChartNow), DATEADD(MONTH, -2, @ChartNow), 5, @ChartSt16, @ChartSt15, @ChartSt15, 0, DATEADD(MONTH, -3, @ChartNow)),
        (@ChartGroupPixel, N'Integrate mentor feedback', N'Store mentor remarks and score history.', DATEADD(DAY, -40, @ChartNow), DATEADD(DAY, 3, @ChartNow), DATEADD(DAY, -35, @ChartNow), NULL, 2, @ChartSt17, @ChartSt15, @ChartSt15, 0, DATEADD(DAY, -40, @ChartNow)),
        (@ChartGroupInsight, N'Create asset issue board', N'Track broken lab assets and repair tickets.', DATEADD(DAY, -20, @ChartNow), DATEADD(DAY, -4, @ChartNow), DATEADD(DAY, -19, @ChartNow), NULL, 4, @ChartSt10, @ChartSt18, @ChartSt18, 1, DATEADD(DAY, -20, @ChartNow)),
        (@ChartGroupInsight, N'Publish maintenance report', N'Finalize summary report for lab managers.', DATEADD(DAY, -10, @ChartNow), DATEADD(DAY, 6, @ChartNow), NULL, NULL, 1, @ChartSt11, @ChartSt18, @ChartSt18, 0, DATEADD(DAY, -10, @ChartNow));
END
GO

DECLARE @ChartNow DATETIME = GETDATE();
DECLARE @ChartGroupOmega INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Omega Lab');
DECLARE @ChartGroupNova INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Nova Crew');
DECLARE @ChartGroupPixel INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Pixel Forge');
DECLARE @ChartGroupInsight INT = (SELECT TOP 1 GroupID FROM ProjectGroup WHERE GroupName = N'Insight Hub');
DECLARE @ChartSt2 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST002');
DECLARE @ChartSt7 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST007');
DECLARE @ChartSt8 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST008');
DECLARE @ChartSt10 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST010');
DECLARE @ChartSt11 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST011');
DECLARE @ChartSt13 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST013');
DECLARE @ChartSt14 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST014');
DECLARE @ChartSt15 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST015');
DECLARE @ChartSt16 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST016');
DECLARE @ChartSt17 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST017');
DECLARE @ChartSt18 INT = (SELECT TOP 1 StudentID FROM Student WHERE StudentCode = N'ST018');

IF NOT EXISTS (SELECT 1 FROM Task WHERE Title = N'Event report reconciliation')
BEGIN
    INSERT INTO Task (GroupID, Title, Description, EstimatedStartDate, EstimatedEndDate, ActualStartDate, ActualEndDate, Status, AssignedTo, ReviewedBy, CreatedBy, IsLate, CreatedAt)
    VALUES
        (@ChartGroupOmega, N'Event report reconciliation', N'Close out event records and export summaries.', DATEADD(MONTH, -3, @ChartNow), DATEADD(MONTH, -2, @ChartNow), DATEADD(MONTH, -3, @ChartNow), DATEADD(MONTH, -2, @ChartNow), 5, @ChartSt13, @ChartSt2, @ChartSt2, 0, DATEADD(MONTH, -3, @ChartNow)),
        (@ChartGroupOmega, N'Kiosk support handbook', N'Document operator guide and troubleshooting.', DATEADD(MONTH, -1, @ChartNow), DATEADD(DAY, 8, @ChartNow), DATEADD(MONTH, -1, @ChartNow), NULL, 2, @ChartSt2, @ChartSt13, @ChartSt13, 0, DATEADD(MONTH, -1, @ChartNow)),
        (@ChartGroupNova, N'Feedback heatmap export', N'Export heatmap-ready metrics for classrooms.', DATEADD(MONTH, -1, @ChartNow), DATEADD(DAY, -6, @ChartNow), DATEADD(MONTH, -1, @ChartNow), NULL, 4, @ChartSt14, @ChartSt7, @ChartSt7, 1, DATEADD(MONTH, -1, @ChartNow)),
        (@ChartGroupNova, N'Survey reminder workflow', N'Build reminder sequence for pending respondents.', DATEADD(DAY, -12, @ChartNow), DATEADD(DAY, 10, @ChartNow), DATEADD(DAY, -10, @ChartNow), NULL, 3, @ChartSt8, @ChartSt7, @ChartSt7, 0, DATEADD(DAY, -12, @ChartNow)),
        (@ChartGroupPixel, N'Application scoring matrix', N'Create scoring rules for internship applications.', DATEADD(MONTH, -2, @ChartNow), DATEADD(MONTH, -1, @ChartNow), DATEADD(MONTH, -2, @ChartNow), DATEADD(DAY, -18, @ChartNow), 5, @ChartSt16, @ChartSt15, @ChartSt15, 0, DATEADD(MONTH, -2, @ChartNow)),
        (@ChartGroupPixel, N'Mentor note digest', N'Weekly digest of mentor comments for students.', DATEADD(DAY, -16, @ChartNow), DATEADD(DAY, 12, @ChartNow), DATEADD(DAY, -15, @ChartNow), NULL, 2, @ChartSt17, @ChartSt15, @ChartSt15, 0, DATEADD(DAY, -16, @ChartNow)),
        (@ChartGroupInsight, N'Asset warranty audit', N'Check warranty status for damaged equipment.', DATEADD(MONTH, -2, @ChartNow), DATEADD(DAY, -9, @ChartNow), DATEADD(MONTH, -2, @ChartNow), NULL, 4, @ChartSt10, @ChartSt18, @ChartSt18, 1, DATEADD(MONTH, -2, @ChartNow)),
        (@ChartGroupInsight, N'Inventory snapshot API', N'Expose current lab inventory for dashboards.', DATEADD(DAY, -8, @ChartNow), DATEADD(DAY, 14, @ChartNow), DATEADD(DAY, -7, @ChartNow), NULL, 2, @ChartSt11, @ChartSt18, @ChartSt18, 0, DATEADD(DAY, -8, @ChartNow));
END
GO

