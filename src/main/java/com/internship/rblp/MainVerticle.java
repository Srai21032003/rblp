package com.internship.rblp;

import com.internship.rblp.config.AppDatabaseConfig;
import com.internship.rblp.handlers.admin.*;
import com.internship.rblp.handlers.admin.bulkupload.GetBulkErrorsHandler;
import com.internship.rblp.handlers.admin.bulkupload.GetBulkStatusHandler;
import com.internship.rblp.handlers.admin.bulkupload.StartBulkUploadHandler;
import com.internship.rblp.handlers.kyc.ApproveKycHandler;
import com.internship.rblp.handlers.kyc.GetAllKycHandler;
import com.internship.rblp.handlers.kyc.GetKycDetailHandler;
import com.internship.rblp.handlers.kyc.RejectKycHandler;
import com.internship.rblp.handlers.student.GetStudentKycStatusHandler;
import com.internship.rblp.handlers.student.SubmitStudentKycHandler;
import com.internship.rblp.handlers.teacher.SubmitTeacherKycHandler;
import com.internship.rblp.handlers.student.UpdateStudentProfileHandler;
import com.internship.rblp.handlers.teacher.GetTeacherKycStatusHandler;
import com.internship.rblp.handlers.teacher.UpdateTeacherProfileHandler;
import com.internship.rblp.repository.*;
import com.internship.rblp.routers.*;
import com.internship.rblp.handlers.auth.AuthHandler;
import com.internship.rblp.routers.AuthRouter;
import com.internship.rblp.routers.UserRouter;
import com.internship.rblp.service.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public Completable rxStart() {
        Dotenv dotenv = Dotenv.load();
        JsonObject config = new JsonObject()
                .put("SERVER_PORT",dotenv.get("SERVER_PORT", "8080"));
        return startApplication(config);
    }


    private Completable startApplication(JsonObject config) {
        try {
            AppDatabaseConfig.init(config);
        } catch (Exception e) {
            return Completable.error(e);
        }

        UserRepository userRepository = new UserRepository();
        KycRepository kycRepository = new KycRepository();
        KycAiAnalysisRepository aiRepo = new KycAiAnalysisRepository();
        AuditLogsRepository auditRepo = new AuditLogsRepository();

        BulkUploadRepository bulkRepo = new BulkUploadRepository();

        AuditLogsService audService = new AuditLogsService(auditRepo);
        FileStorageService fileStorageService = new FileStorageService(vertx);
        AiKycServiceGemini aiService = new AiKycServiceGemini(vertx,aiRepo, kycRepository, audService);


        AuthService authService = new AuthService(userRepository);
        AuthHandler.init(authService,audService);

        AdminService adminService = new AdminService(userRepository);
        UpdateAdminProfileHandler.init(adminService);
        OnboardTeacherHandler.init(adminService);
        OnboardStudentHandler.init(adminService);
        ToggleUserStatusHandler.init(adminService);
        GetUserListHandler.init(adminService);

        KycService kycService = new KycService(kycRepository, userRepository,vertx, aiService, aiRepo);
        GetAllKycHandler.init(kycService);
        GetKycDetailHandler.init(kycService);
        ApproveKycHandler.init(kycService);
        RejectKycHandler.init(kycService);

        StudentService studentService = new StudentService(userRepository);
        UpdateStudentProfileHandler.init(studentService, audService);
        SubmitStudentKycHandler.init(kycService,fileStorageService, audService);
        GetStudentKycStatusHandler.init(kycService, audService);

        TeacherService teacherService = new TeacherService(userRepository);
        UpdateTeacherProfileHandler.init(teacherService, audService);
        SubmitTeacherKycHandler.init(kycService,fileStorageService, audService);
        GetTeacherKycStatusHandler.init(kycService, audService);

        BulkUploadService bulkService = new BulkUploadService(bulkRepo,adminService,vertx);

        StartBulkUploadHandler.init(bulkService);
        GetBulkStatusHandler.init(bulkService);
        GetBulkErrorsHandler.init(bulkService);

        Router router = Router.router(vertx);

        router.get("/health").handler(ctx -> ctx.json(new JsonObject().put("status", "UP")));

        // Mount Sub-Routers
        //PUBLIC
        router.route("/api/auth/*").subRouter(AuthRouter.INSTANCE.create(vertx));

        //PROTECTED
        router.route("/api/user/*").subRouter(UserRouter.INSTANCE.create(vertx));
        router.route("/api/admin/*").subRouter(AdminRouter.INSTANCE.create(vertx));
        router.route("/api/student/*").subRouter(StudentRouter.INSTANCE.create(vertx));
        router.route("/api/teacher/*").subRouter(TeacherRouter.INSTANCE.create(vertx));


//        try {
//            System.out.println("DEBUG: Attempting to mount AuthRouter...");
////            router.mountSubRouter("/api/auth", AuthRouter.create(vertx));
//            router.route("/api/auth/*").subRouter(AuthRouter.create(vertx));
//            System.out.println("DEBUG: AuthRouter mounted successfully!");
//        } catch (Throwable t) {
//            System.err.println("CRITICAL ERROR: Failed to initialize Auth Module!");
//            t.printStackTrace(); // <--- This will print the REAL error to your console
//            return Completable.error(t);
//        }


        int port = Integer.parseInt(config.getString("SERVER_PORT", "8080"));


        HttpServer server = vertx.createHttpServer();

        return server.requestHandler(router)
                .rxListen(port)
                .doOnSuccess(s -> logger.info("HTTP Server started on port {}", port))
                .doOnError(err -> logger.error("Failed to start HTTP server", err))
                .ignoreElement();
    }
}