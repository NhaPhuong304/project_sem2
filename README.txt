=========================================================
PROJECT MANAGEMENT SYSTEM - APTECH SEMESTER 2
JavaFX 21 + SceneBuilder + SQL Server 2019 + Maven
=========================================================

1. GIOI THIEU
This project is a desktop app for managing Aptech student projects.
The system currently supports 4 user groups:
- Admin (Quan tri vien)
- Staff
- Teacher
- Student

Chuc nang chinh:
- Login / OTP / first-time password change
- Class management
- Student management
- Teacher management
- Project management
- Group management
- Task management
- Student inbox
- Upload / doi avatar
- Splash screen truoc khi vao login


=========================================================
2. CONG NGHE VA THU VIEN SU DUNG
=========================================================

2.1. Ngon ngu va UI
- Java 21
- JavaFX 21
- SceneBuilder

2.2. Database
- SQL Server 2019 hoac moi hon
- Port local mac dinh: 1433

2.3. Build tool
- Maven 3.9+

2.4. Thu vien Maven dang dung
Khai bao trong file:
- [pom.xml]

Danh sach:
- org.openjfx:javafx-controls:21
- org.openjfx:javafx-fxml:21
- org.openjfx:javafx-graphics:21
- com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11
- com.zaxxer:HikariCP:5.1.0
- com.sun.mail:jakarta.mail:2.0.1
- org.mindrot:jbcrypt:0.4
- org.slf4j:slf4j-simple:1.7.36


=========================================================
3. ENVIRONMENT REQUIREMENTS
=========================================================

Ban can cai:
- JDK 21
- Maven
- SQL Server 2019+
- Eclipse IDE for Java Developers
- SceneBuilder
- DBeaver hoac SQL Server Management Studio de chay file SQL

Khuyen nghi:
- Eclipse 2023-03 tro len
- SceneBuilder ban moi


=========================================================
4. CAU TRUC PROJECT
=========================================================

Important folders:
- src/main/java/com/aptech/projectmgmt
- src/main/resources/fxml
- src/main/resources/css/style.css
- src/main/resources/db/database.sql
- src/main/resources/database.properties
- src/main/resources/application.properties
- uploads/avatars

Entry point:
- Maven JavaFX run: `com.aptech.projectmgmt.Main`
- Eclipse Java Application nen chay: src/main/java/com/aptech/projectmgmt/AppLauncher.java


=========================================================
5. CAC FILE CAU HINH QUAN TRONG
=========================================================

5.1. Cau hinh database
File:
- src/main/resources/database.properties

Format hien tai:
  db.url=jdbc:sqlserver://DESKTOP-QMKQ2M3:1433;databaseName=ProjectManagementDB;encrypt=true;trustServerCertificate=true
  db.username=sa
  db.password=Password123!
  db.poolSize=10

Ban phai doi:
- `db.username`
- `db.password`
cho dung voi may cua ban.

5.2. Cau hinh mail OTP
File:
- src/main/resources/application.properties

Can co cac key:
  smtp.host=smtp.gmail.com
  smtp.port=587
  smtp.username=your_email@gmail.com
  smtp.password=your_gmail_app_password
  smtp.from=your_email@gmail.com

Luu y:
- Do not keep real email/password credentials when sharing the project
- If SMTP is not configured correctly, OTP and email notification features will fail


=========================================================
6. HUONG DAN CHAY DATABASE TU DAU
=========================================================

File SQL chinh:
- src/main/resources/db/database.sql

File nay se:
- Tao database `ProjectManagementDB`
- Tao tat ca bang, index, trigger, stored procedure, view
- Create test accounts for immediate sign-in

6.1. Cach chay
1. Mo DBeaver hoac SQL Server Management Studio
2. Ket noi toi SQL Server local
3. Mo file:
   `src/main/resources/db/database.sql`
4. Chay toan bo file

6.2. Luu y quan trong
- File nay co `CREATE DATABASE ProjectManagementDB`
- Neu may ban da co database trung ten roi thi can xoa DB cu hoac doi ten DB truoc khi chay lai

6.3. Sau khi chay xong
Check that the following already exist:
- Database `ProjectManagementDB`
- Bang `Account`, `Staff`, `Student`, `Project`, `Task`, ...
- Stored procedure `sp_GenerateOtp`, `sp_VerifyOtp`, `sp_ResetOverdueTasks`


=========================================================
7. TAI KHOAN TEST CO SAN SAU KHI CHAY FILE SQL
=========================================================

Password for all test accounts:
- `123`

7.1. Admin
- username: `admin`
- password: `123`

7.2. Staff
- username: `staff001`
- password: `123`
- username: `staff002`
- password: `123`

7.3. Teacher
- username: `gv001`
- password: `123`
- username: `gv002`
- password: `123`

7.4. Student
- username: `st001`, `st002`, `st003`, `st004`, `st005`
- password: `123` (cho tat ca)

Luu y:
- Cac tai khoan seed trong DDL dang de `IsFirstLogin = 0`
- This means test login works directly without forcing first-login OTP


=========================================================
8. HUONG DAN IMPORT VA CHAY TRONG ECLIPSE
=========================================================

8.1. Import project
1. Mo Eclipse
2. Chon:
   File > Import > Maven > Existing Maven Projects
3. Choose the project folder:
   (duong dan toi project-sem2 tren may cua ban)
4. Finish
5. Right-click the project > Maven > Update Project

8.2. Chon dung JDK
1. Right-click the project > Properties
2. Vao Java Build Path
3. Dam bao project dang dung JDK 21
4. Vao Java Compiler
5. Dam bao compiler level la 21

8.3. Cau hinh SceneBuilder
1. Cai SceneBuilder
2. Trong Eclipse vao:
   Window > Preferences > JavaFX
   - Help > Eclipse Marketplace > e(fx)clipse > Restart Eclipse
3. Set duong dan SceneBuilder executable

Vi du macOS:
  /Applications/SceneBuilder.app/Contents/MacOS/SceneBuilder

8.4. Mo file FXML bang SceneBuilder
- Right-click the `.fxml` file
- Open With > SceneBuilder
You need to install first:
	Open the browser
	Go to the Gluon Scene Builder download page:
	https://gluonhq.com/products/scene-builder/
	Download the Windows version
	After installation, the file is usually located at:
	C:\Program Files\SceneBuilder\SceneBuilder.exe
	Then return to Eclipse:
	
	Right-click the .fxml file
	Open With > Other...
	Choose External programs
	Browse...
	Point to:
	C:\Program Files\SceneBuilder\SceneBuilder.exe
	Tick Use it for all '*.fxml' files
	OK


=========================================================
9. HUONG DAN CHAY APP
=========================================================

9.1. Cach khuyen dung: Maven
Mo terminal tai root project va chay:
  mvn clean javafx:run

Hoac:
  mvn javafx:run

9.2. Chay trong Eclipse bang Maven Build
1. Right-click the project
2. Run As > Maven build...
3. Goals:
   clean javafx:run
4. Run

9.3. Chay trong Eclipse bang Java Application
Neu muon bam Run truc tiep trong Eclipse:
- Hay chay src/main/java/com/aptech/projectmgmt/AppLauncher.java
- Do not run `Main.java` directly if the Eclipse launch configuration is incorrect

Main class nen chon:
  com.aptech.projectmgmt.AppLauncher

Ly do:
- Avoid the `JavaFX runtime components are missing` error


=========================================================
10. LUONG KHOI DONG APP
=========================================================

Khi chay app:
1. Splash screen hien logo Aptech
2. Thanh progress chay tu 0 den 100%
3. Sau khoang 5 giay moi vao man login

File lien quan:
- src/main/java/com/aptech/projectmgmt/Main.java
- src/main/java/com/aptech/projectmgmt/controller/SplashController.java
- src/main/resources/fxml/splash.fxml


=========================================================
11. CAC ROLE VA QUYEN HIEN TAI
=========================================================

11.1. Admin
- Sign in to the admin dashboard
- Quan tri he thong

11.2. Staff
- Sign in to the staff dashboard
- Class management (ManagerID identifies which staff member is responsible for each class)
- Student management
- Teacher management
- Project and group management
- View and manage tasks across groups

11.3. Teacher
- Sign in to the teacher dashboard
- Can only view classes and projects assigned for supervision
- Cannot create classes, add students, or create projects like admin/staff

11.4. Student
- Sign in to the student dashboard
- Xem project cua minh
- Xem task
- Create tasks if you are the group leader and the conditions are met
- Xem hop thu den


=========================================================
12. AVATAR
=========================================================

12.1. Avatar mac dinh
- He thong dung `no-image.jpg` neu account chua co avatar

12.2. Khi doi avatar
- User bam vao avatar o header
- Chon anh tu may
- App se copy anh vao:
  uploads/avatars
- File names are generated randomly and kept unique
- If the user already has an old avatar managed by the project, the app removes the old file
- DB chi luu path tuong doi, vi du:
  `uploads/avatars/avatar_acc_3_xxxxxxxxxxxxx.jpg`

12.3. Khi copy project sang may khac
Neu muon giu avatar da upload:
- You must copy the full folder:
  `uploads/avatars`


=========================================================
13. LUU Y VE MAIL / OTP
=========================================================

Tinh nang can mail:
- Forgot password
- OTP for first-time password change
- Send a notification when creating a new student
- Send a notification when creating a new teacher
- Send task reminders / task-related emails

If email cannot be sent:
- Check `application.properties`
- Check the Gmail App Password
- Check the Internet connection


=========================================================
14. CAC MAN HINH CHINH
=========================================================

Admin:
- Login
- OTP
- System setup

Staff:
- Staff Dashboard
- Class management
- Student management
- Teacher management
- Project management
- Chi tiet project
- Group details
- Danh sach task

Teacher:
- Teacher Dashboard
- My classes
- Project huong dan

Student:
- Student Dashboard
- My projects
- Cong viec cua toi
- Hop thu den


=========================================================
15. CAU TRUC MA NGUON
=========================================================

Java source:
- src/main/java/com/aptech/projectmgmt

Package chinh:
- `config`     : cau hinh DB
- `model`      : model va enum
- `repository` : query SQL
- `service`    : business logic
- `controller` : controller JavaFX
- `util`       : SceneManager, SessionManager, AlertUtil, AvatarUtil...

Resource:
- src/main/resources/fxml
- src/main/resources/css/style.css
- src/main/resources/images
- src/main/resources/db


=========================================================
16. LENH MAVEN HUU ICH
=========================================================

Compile:
  mvn compile

Compile bo qua test:
  mvn -q -DskipTests compile

Chay app:
  mvn javafx:run

Clean:
  mvn clean

Clean va run:
  mvn clean javafx:run


=========================================================
17. CACH CHUYEN PROJECT SANG MAY KHAC
=========================================================

17.1. Copy source code
Copy the entire project folder, including:
- source code
- resources
- uploads/avatars neu muon giu avatar da upload

17.2. Tren may moi, can cai:
- JDK 21
- Maven
- SQL Server
- Eclipse
- SceneBuilder

17.3. Chay database
1. Mo SQL tool
2. Chay file:
   src/main/resources/db/database.sql
3. File nay tao DB va seed luon account test

17.4. Sua cau hinh
Sua file:
- src/main/resources/database.properties
- src/main/resources/application.properties

17.5. Import vao Eclipse
- Import Maven project
- Update Project
- Chay bang Maven hoac `AppLauncher`


=========================================================
18. COMMON ISSUES
=========================================================

18.1. Database connection error
Check:
- SQL Server da mo chua
- dung port 1433 chua
- username/password trong `database.properties` dung chua

18.2. JavaFX runtime components are missing
Cach khac phuc:
- Chay bang `mvn javafx:run`
hoac
- Chay AppLauncher.java thay vi `Main.java`

18.3. Email sending failure
Check:
- `application.properties`
- Gmail App Password
- ket noi Internet

18.4. Login succeeds but the dashboard does not open
Check:
- file SQL da chay xong chua
- DB da co `Account`, `Staff`, `Student`
- whether the role is correct


=========================================================
19. GHI CHU QUAN TRONG
=========================================================

- This project uses JavaFX + FXML + SceneBuilder, not Swing
- Do not query the database on the FX thread
- Repository chi chua SQL
- Service chua business logic
- Scene chuyen bang `SceneManager`
- The login session is stored in `SessionManager`
- Password luu bang BCrypt
- Splash hien truoc login
- Avatar upload luu trong project


=========================================================
20. TOM TAT NHANH DE CHAY DUOC NGAY
=========================================================

Neu ban chi muon chay nhanh tren may moi:
1. Cai JDK 21
2. Cai Maven
3. Cai SQL Server
4. Chay file:
   `src/main/resources/db/database.sql`
5. Sua:
   `src/main/resources/database.properties`
6. Neu can OTP/mail thi sua:
   `src/main/resources/application.properties`
7. Mo terminal tai project root
8. Chay:
   `mvn clean javafx:run`
9. Login bang:
   `admin / 123`

=========================================================
END OF README
=========================================================

